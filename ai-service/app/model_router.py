"""
Multi-Provider & Multi-Key AI Failover Router for AIFP.
Automatically rotates across multiple API keys and falls back across:
1. Multiple Gemini models (gemini-2.5-pro -> gemini-2.5-flash -> gemini-2.0-flash -> gemini-2.0-flash-lite)
2. Multiple Free & Paid Providers (Google Gemini -> Groq -> OpenRouter -> OpenAI -> Ollama Local)
whenever a key or model hits 429 Quota Exhausted / Rate Limit or 80% user daily token budget.
"""
from __future__ import annotations

import os
import time
from dataclasses import dataclass, field
from typing import Any

# Ordered Gemini Model Fallback Chains (when one model quota is exhausted, switch to the next)
GEMINI_REASONING_CHAIN: list[str] = [
    "gemini-2.5-pro",
    "gemini-2.5-flash",
    "gemini-2.0-flash",
    "gemini-2.0-flash-lite",
]

GEMINI_FAST_CHAIN: list[str] = [
    "gemini-2.5-flash",
    "gemini-2.0-flash",
    "gemini-2.0-flash-lite",
    "gemini-1.5-flash",
]

GROQ_FALLBACK_CHAIN: list[str] = [
    "groq/llama-3.3-70b-versatile",
    "groq/llama-3.1-8b-instant",
]

OPENROUTER_FALLBACK_CHAIN: list[str] = [
    "openrouter/deepseek/deepseek-chat",
    "openrouter/google/gemini-2.0-flash-001",
    "openrouter/google/gemini-2.0-flash-exp:free",
    "openrouter/deepseek/deepseek-chat:free",
    "openrouter/meta-llama/llama-3.3-70b-instruct:free",
]

OPENAI_FALLBACK_CHAIN: list[str] = [
    "openai/gpt-4o-mini",
    "openai/gpt-4o",
]

OLLAMA_LOCAL_CHAIN: list[str] = [
    "ollama/qwen2.5:7b",
    "ollama/llama3.2:3b",
]


def _parse_comma_keys(env_var_plural: str, env_var_singular: str) -> list[str]:
    raw = os.getenv(env_var_plural, "") or os.getenv(env_var_singular, "")
    keys = [k.strip() for k in raw.split(",") if k.strip() and not k.strip().startswith("your_")]
    return keys


def mask_api_key(key: str) -> str:
    if not key or len(key) < 8:
        return "configured"
    return f"{key[:4]}...{key[-4:]}"


@dataclass
class RouteSelection:
    provider: str
    model: str
    api_key_masked: str
    tier: str
    quota_downgraded: bool
    failover_hop: int
    fallback_chain_available: list[str] = field(default_factory=list)


class MultiProviderFailoverRouter:
    """
    Maintains health/cooldown state per (provider, key_index, model) and automatically
    switches to the next API key, next Gemini model, or next AI provider on 429/quota errors.
    """

    def __init__(
        self,
        gemini_keys: list[str] | None = None,
        groq_keys: list[str] | None = None,
        openrouter_keys: list[str] | None = None,
        openai_keys: list[str] | None = None,
        ollama_enabled: bool | None = None,
    ) -> None:
        self.gemini_keys = gemini_keys if gemini_keys is not None else _parse_comma_keys("GEMINI_API_KEYS", "GEMINI_API_KEY")
        self.groq_keys = groq_keys if groq_keys is not None else _parse_comma_keys("GROQ_API_KEYS", "GROQ_API_KEY")
        self.openrouter_keys = (
            openrouter_keys
            if openrouter_keys is not None
            else _parse_comma_keys("OPENROUTER_API_KEYS", "OPENROUTER_API_KEY")
        )
        self.openai_keys = openai_keys if openai_keys is not None else _parse_comma_keys("OPENAI_API_KEYS", "OPENAI_API_KEY")
        self.ollama_enabled = (
            ollama_enabled
            if ollama_enabled is not None
            else bool(os.getenv("OLLAMA_BASE_URL", "").strip())
        )
        # Cooldown map: signature "provider:key_idx:model" -> unix timestamp when cooldown expires
        self._cooldown_until: dict[str, float] = {}

    def mark_exhausted(
        self, provider: str, key_index: int, model: str, cooldown_seconds: int = 300
    ) -> None:
        """Marks a specific (provider, key_index, model) slot as rate-limited/exhausted."""
        slot_id = f"{provider.upper()}:{key_index}:{model}"
        self._cooldown_until[slot_id] = time.time() + cooldown_seconds

    def is_slot_available(self, provider: str, key_index: int, model: str) -> bool:
        slot_id = f"{provider.upper()}:{key_index}:{model}"
        until = self._cooldown_until.get(slot_id, 0.0)
        return time.time() >= until

    def build_candidate_slots(self, needs_deep_reasoning: bool, quota_downgraded: bool) -> list[dict[str, Any]]:
        """
        Builds the ordered failover list of (provider, key_index, masked_key, model) slots.
        Priority:
        1. Google Gemini (all keys x Gemini model chain)
        2. Groq Free/Fast Cloud (all keys x Llama 3.3 70B / 8B)
        3. OpenRouter Free/Paid Pool (all keys x Free/Paid models)
        4. OpenAI (all keys x gpt-4o-mini / gpt-4o)
        5. Ollama Local (zero-cost offline fallback)
        """
        gemini_models = (
            GEMINI_REASONING_CHAIN if (needs_deep_reasoning and not quota_downgraded) else GEMINI_FAST_CHAIN
        )
        effective_gemini_keys = self.gemini_keys or ["env-default-gemini-key"]

        slots: list[dict[str, Any]] = []
        # For each Gemini model in the chain, rotate across all configured Gemini API keys
        for model in gemini_models:
            for idx, key in enumerate(effective_gemini_keys):
                slots.append(
                    {
                        "provider": "GEMINI",
                        "key_index": idx,
                        "api_key_masked": mask_api_key(key),
                        "model": model,
                    }
                )

        for model in GROQ_FALLBACK_CHAIN:
            for idx, key in enumerate(self.groq_keys):
                slots.append(
                    {
                        "provider": "GROQ",
                        "key_index": idx,
                        "api_key_masked": mask_api_key(key),
                        "model": model,
                    }
                )

        for model in OPENROUTER_FALLBACK_CHAIN:
            for idx, key in enumerate(self.openrouter_keys):
                slots.append(
                    {
                        "provider": "OPENROUTER",
                        "key_index": idx,
                        "api_key_masked": mask_api_key(key),
                        "model": model,
                    }
                )

        for model in OPENAI_FALLBACK_CHAIN:
            for idx, key in enumerate(self.openai_keys):
                slots.append(
                    {
                        "provider": "OPENAI",
                        "key_index": idx,
                        "api_key_masked": mask_api_key(key),
                        "model": model,
                    }
                )

        if self.ollama_enabled:
            for model in OLLAMA_LOCAL_CHAIN:
                slots.append(
                    {
                        "provider": "OLLAMA_LOCAL",
                        "key_index": 0,
                        "api_key_masked": "local-free",
                        "model": model,
                    }
                )

        return slots

    def select_route(
        self,
        prompt: str,
        daily_tokens_used: int = 0,
        daily_token_quota: int = 150000,
    ) -> RouteSelection:
        quota_ratio = (daily_tokens_used / daily_token_quota) if daily_token_quota > 0 else 1.0
        quota_downgraded = quota_ratio >= 0.80
        needs_deep_reasoning = len(prompt) > 280 or any(
            kw in prompt.lower() for kw in ("phân tích sâu", "so sánh chi tiết", "kiến trúc", "tổng hợp")
        )
        tier = "REASONING" if (needs_deep_reasoning and not quota_downgraded) else "FAST"

        candidates = self.build_candidate_slots(needs_deep_reasoning, quota_downgraded)
        chain_names = [c["model"] for c in candidates]

        for hop, slot in enumerate(candidates):
            if self.is_slot_available(slot["provider"], slot["key_index"], slot["model"]):
                return RouteSelection(
                    provider=slot["provider"],
                    model=slot["model"],
                    api_key_masked=slot["api_key_masked"],
                    tier=tier,
                    quota_downgraded=quota_downgraded,
                    failover_hop=hop,
                    fallback_chain_available=chain_names,
                )

        # If every single slot is in cooldown, reset cooldowns and use the safest lightweight model
        self._cooldown_until.clear()
        last_resort = candidates[-1]
        return RouteSelection(
            provider=last_resort["provider"],
            model=last_resort["model"],
            api_key_masked=last_resort["api_key_masked"],
            tier="FAST_EMERGENCY",
            quota_downgraded=True,
            failover_hop=len(candidates),
            fallback_chain_available=chain_names,
        )

