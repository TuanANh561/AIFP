"""Security guards: HMAC verification, SSRF protection, Untrusted Data quarantine, and Citation Grounding."""
from __future__ import annotations

import hashlib
import hmac
import ipaddress
import os
import re
import time
from typing import Iterable
from urllib.parse import urlparse

INTERNAL_SHARED_SECRET = os.getenv(
    "INTERNAL_SHARED_SECRET",
    "aifp_default_dev_only_hmac_shared_secret_between_spring_and_python_2026",
)


def compute_hmac_signature(timestamp_sec: int, user_id: str, raw_body: str, secret: str = INTERNAL_SHARED_SECRET) -> str:
    canonical = f"{timestamp_sec}.{user_id}.{raw_body or ''}".encode("utf-8")
    return hmac.new(secret.encode("utf-8"), canonical, hashlib.sha256).hexdigest()


def verify_hmac_signature(
    timestamp_sec: int,
    user_id: str,
    raw_body: str,
    provided_signature: str,
    secret: str = INTERNAL_SHARED_SECRET,
    max_skew_seconds: int = 60,
) -> bool:
    if not provided_signature:
        return False
    now = int(time.time())
    if abs(now - timestamp_sec) > max_skew_seconds:
        return False
    expected = compute_hmac_signature(timestamp_sec, user_id, raw_body, secret)
    return hmac.compare_digest(expected, provided_signature)


def validate_safe_external_url(url: str) -> str:
    """Blocks SSRF attacks targeting loopback, private RFC1918, link-local (169.254.169.254), or non-HTTP schemes."""
    parsed = urlparse(url.strip())
    if parsed.scheme not in ("http", "https"):
        raise ValueError(f"Blocked unsafe URL scheme: {parsed.scheme}")
    hostname = (parsed.hostname or "").lower()
    if not hostname or hostname in ("localhost", "0.0.0.0", "metadata.google.internal"):
        raise ValueError(f"Blocked restricted host: {hostname}")

    try:
        ip = ipaddress.ip_address(hostname)
        if ip.is_private or ip.is_loopback or ip.is_link_local or ip.is_multicast or ip.is_reserved:
            raise ValueError(f"Blocked private/internal IP address: {ip}")
    except ValueError as exc:
        if "Blocked" in str(exc):
            raise
        # Hostname is a domain name, not an IP literal
    return url.strip()


def wrap_untrusted_external_content(source_url: str, raw_text: str) -> str:
    """Quarantines external scraped/search/RSS text to defend against Indirect Prompt Injection."""
    cleaned = re.sub(r"<\s*/?\s*untrusted_external_content[^>]*>", "", raw_text, flags=re.IGNORECASE)
    cleaned = re.sub(r"[\u200b-\u200f\u202a-\u202e]", "", cleaned)  # Strip zero-width & bidi override chars
    return (
        f'<untrusted_external_content source="{source_url}" trust_level="none">\n'
        f"{cleaned.strip()}\n"
        f"</untrusted_external_content>"
    )


MARKDOWN_LINK_PATTERN = re.compile(r"\[([^\]]+)\]\((https?://[^\s)]+)\)")


def enforce_citation_grounding(markdown_text: str, discovered_urls: Iterable[str]) -> tuple[str, list[str], list[str]]:
    """
    Verifies every Markdown link [label](url) against the actual tool-discovered URL allowlist.
    Returns (sanitized_markdown, verified_urls, stripped_hallucinated_urls).
    """
    allowed_set = {u.strip().rstrip("/") for u in discovered_urls if u}
    verified: list[str] = []
    stripped: list[str] = []

    def _replace(match: re.Match[str]) -> str:
        label, url = match.group(1), match.group(2)
        normalized = url.strip().rstrip("/")
        if normalized in allowed_set:
            if url not in verified:
                verified.append(url)
            return f"[{label}]({url})"
        stripped.append(url)
        return f"{label} [Link chưa kiểm chứng đã gỡ]"

    sanitized = MARKDOWN_LINK_PATTERN.sub(_replace, markdown_text)
    return sanitized, verified, stripped

