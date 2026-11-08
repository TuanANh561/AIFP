"""LangGraph 7-Node Orchestrator, Model Router, Loop Detector, and Delta Fingerprinter."""
from __future__ import annotations

import hashlib
import json
from dataclasses import dataclass, field
from typing import Any

from app.model_router import MultiProviderFailoverRouter, RouteSelection
from app.security import enforce_citation_grounding, validate_safe_external_url, wrap_untrusted_external_content
from app.tools_open_source import query_searxng_json

MAX_AGENT_STEPS = 8

default_failover_router = MultiProviderFailoverRouter()

TOOL_PERMISSION_LEVELS: dict[str, int] = {
    "searxng_search": 1,
    "rss_fetch": 1,
    "webpage_extract": 1,
    "doc_parse": 1,
    "translate_text": 1,
    "memory_candidate_save": 2,
    "notification_send": 2,
    "email_send_self": 2,
    "email_send_external": 3,
    "calendar_event_write": 3,
    "task_bulk_delete": 3,
    "shell_exec": 4,
}


def select_model_tier(prompt: str, daily_tokens_used: int, daily_token_quota: int) -> tuple[str, bool]:
    """Routes between Fast (gemini-2.5-flash) and Reasoning (gemini-2.5-pro) with automatic 80% quota downgrade."""
    quota_ratio = (daily_tokens_used / daily_token_quota) if daily_token_quota > 0 else 1.0
    quota_downgraded = quota_ratio >= 0.80
    needs_deep_reasoning = len(prompt) > 280 or any(
        kw in prompt.lower() for kw in ("phân tích sâu", "so sánh chi tiết", "kiến trúc", "tổng hợp")
    )
    if needs_deep_reasoning and not quota_downgraded:
        return "gemini-2.5-pro", False
    return "gemini-2.5-flash", quota_downgraded


def compute_item_fingerprint(canonical_url: str, title: str) -> str:
    norm_url = (canonical_url or "").strip().lower().rstrip("/")
    norm_title = " ".join((title or "").strip().lower().split())
    return hashlib.sha256(f"{norm_url}|{norm_title}".encode("utf-8")).hexdigest()


@dataclass
class AgentState:
    user_id: str
    prompt: str
    allowed_tools: list[str]
    daily_tokens_used: int = 0
    daily_token_quota: int = 150000
    prior_fingerprints: set[str] = field(default_factory=set)
    delivery_mode: str = "DELTA_ONLY"
    # Runtime state across the 7 nodes
    intent: str = "GENERAL_CHAT"
    selected_model: str = "gemini-2.5-flash"
    active_provider: str = "GEMINI"
    api_key_masked: str = ""
    failover_hop: int = 0
    fallback_chain: list[str] = field(default_factory=list)
    quota_downgraded: bool = False
    planned_steps: list[dict[str, Any]] = field(default_factory=list)
    executed_call_signatures: set[str] = field(default_factory=set)
    discovered_urls: list[str] = field(default_factory=list)
    raw_items: list[dict[str, str]] = field(default_factory=list)
    delta_items: list[dict[str, str]] = field(default_factory=list)
    new_fingerprints: list[str] = field(default_factory=list)
    status: str = "INIT"
    approval_required_tool: str | None = None
    approval_payload: dict[str, Any] | None = None
    final_markdown: str = ""
    verified_citations: list[str] = field(default_factory=list)
    stripped_hallucinated_urls: list[str] = field(default_factory=list)


class AifpLangGraphOrchestrator:
    """Deterministic 7-Node Workflow: Understand -> Plan -> SelectTools -> Authorize -> Execute -> Verify -> Respond."""

    def run(self, state: AgentState, simulated_search_results: list[dict[str, str]] | None = None) -> AgentState:
        self.understand_node(state)
        self.plan_node(state)
        self.select_tools_node(state)
        self.authorize_node(state)
        if state.status in ("WAITING_FOR_APPROVAL", "DENIED_LEVEL_4"):
            return state
        self.execute_node(state, simulated_search_results or [])
        self.verify_node(state)
        self.respond_node(state)
        return state

    def understand_node(self, state: AgentState) -> None:
        route = default_failover_router.select_route(
            state.prompt, state.daily_tokens_used, state.daily_token_quota
        )
        state.selected_model = route.model
        state.active_provider = route.provider
        state.api_key_masked = route.api_key_masked
        state.failover_hop = route.failover_hop
        state.fallback_chain = route.fallback_chain_available
        state.quota_downgraded = route.quota_downgraded

        lower = state.prompt.lower()
        if any(k in lower for k in ("tìm", "tuyển dụng", "tin tức", "nghiên cứu", "intern")):
            state.intent = "RESEARCH_AND_MONITOR"
        elif any(k in lower for k in ("gửi email cho", "external@")):
            state.intent = "EXTERNAL_EMAIL_ACTION"
        elif "shell_exec" in lower:
            state.intent = "RESTRICTED_ACTION"
        else:
            state.intent = "GENERAL_CHAT"

    def plan_node(self, state: AgentState) -> None:
        steps: list[dict[str, Any]] = []
        if state.intent == "RESEARCH_AND_MONITOR":
            steps.append({"tool": "searxng_search", "args": {"query": state.prompt[:120]}})
        elif state.intent == "EXTERNAL_EMAIL_ACTION":
            steps.append(
                {
                    "tool": "email_send_external",
                    "args": {"to_email": "partner@company.com", "subject": "Báo cáo tổng hợp", "body": state.prompt},
                }
            )
        elif state.intent == "RESTRICTED_ACTION":
            steps.append({"tool": "shell_exec", "args": {"command": "whoami"}})
        # Hard cap at MAX_AGENT_STEPS = 8
        state.planned_steps = steps[:MAX_AGENT_STEPS]

    def select_tools_node(self, state: AgentState) -> None:
        for step in state.planned_steps:
            tool_code = step["tool"]
            if tool_code not in TOOL_PERMISSION_LEVELS:
                raise ValueError(f"Unregistered tool requested: {tool_code}")

    def authorize_node(self, state: AgentState) -> None:
        for step in state.planned_steps:
            tool_code = step["tool"]
            level = TOOL_PERMISSION_LEVELS.get(tool_code, 4)
            if level >= 4 or tool_code not in state.allowed_tools:
                state.status = "DENIED_LEVEL_4"
                state.final_markdown = f"Từ chối thực thi công cụ `{tool_code}` theo chính sách bảo mật Level 4."
                return
            if level == 3:
                state.status = "WAITING_FOR_APPROVAL"
                state.approval_required_tool = tool_code
                state.approval_payload = step["args"]
                state.final_markdown = f"Tác vụ tạm dừng chờ bạn duyệt công cụ `{tool_code}` (Level 3 HITL)."
                return
        state.status = "AUTHORIZED"

    def execute_node(self, state: AgentState, search_results: list[dict[str, str]]) -> None:
        for step in state.planned_steps:
            signature = f"{step['tool']}:{json.dumps(step['args'], sort_keys=True)}"
            if signature in state.executed_call_signatures:
                # Duplicate call loop detector triggered
                continue
            state.executed_call_signatures.add(signature)

            if step["tool"] == "searxng_search":
                items_to_process = (
                    search_results
                    if search_results
                    else query_searxng_json(step["args"].get("query", state.prompt), max_results=5)
                )
                for item in items_to_process:
                    safe_url = validate_safe_external_url(item["url"])
                    state.discovered_urls.append(safe_url)
                    snippet = item.get("snippet", "")
                    quarantined_snippet = wrap_untrusted_external_content(safe_url, snippet)
                    state.raw_items.append(
                        {
                            "title": item["title"],
                            "url": safe_url,
                            "snippet": snippet,
                            "quarantined_content": quarantined_snippet,
                        }
                    )

    def verify_node(self, state: AgentState) -> None:
        for item in state.raw_items:
            fp = compute_item_fingerprint(item["url"], item["title"])
            if fp not in state.prior_fingerprints:
                state.delta_items.append(item)
                state.new_fingerprints.append(fp)

        if state.intent == "RESEARCH_AND_MONITOR" and state.delivery_mode == "DELTA_ONLY" and not state.delta_items:
            state.status = "COMPLETED_NO_DELTA"
            state.final_markdown = "Không có tin mới phát sinh so với các lần chạy trước."
            return

        if state.delta_items:
            lines = [
                f"### Bản tin tổng hợp chọn lọc ({len(state.delta_items)} tin mới)\n",
                f"Trợ lý đã rà soát và đối chiếu nguồn tin theo yêu cầu của bạn qua mô hình **{state.selected_model}**:\n",
            ]
            for idx, item in enumerate(state.delta_items, start=1):
                snippet = item.get("snippet", "")
                if snippet:
                    lines.append(f"**{idx}. [{item['title']}]({item['url']})**\n> {snippet[:200]}...\n")
                else:
                    lines.append(f"**{idx}. [{item['title']}]({item['url']})**\n")
            raw_synthesis = "\n".join(lines)
        elif state.final_markdown:
            raw_synthesis = state.final_markdown
        else:
            raw_synthesis = (
                f"Chào bạn! Tôi là Trợ lý Cá nhân AIFP ({state.selected_model}). "
                f"Tôi có thể hỗ trợ bạn theo dõi tin tức công nghệ, tài chính, việc làm, săn deal khuyến mãi "
                f"và tự động gửi bản tin tóm tắt vào email của bạn mỗi sáng mà không cần mở ứng dụng. "
                f"Bạn có thể yêu cầu tôi tìm kiếm thông tin mới hoặc thiết lập lịch tự động hóa ngay tại đây."
            )

        sanitized, verified, stripped = enforce_citation_grounding(raw_synthesis, state.discovered_urls)
        state.final_markdown = sanitized
        state.verified_citations = verified
        state.stripped_hallucinated_urls = stripped
        state.status = "COMPLETED"

    def respond_node(self, state: AgentState) -> None:
        if not state.final_markdown:
            state.final_markdown = "Hoàn tất."

