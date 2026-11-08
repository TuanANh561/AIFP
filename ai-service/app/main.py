"""FastAPI entrypoint for AIFP Python AI Orchestrator Service."""
from __future__ import annotations

from typing import Any

from fastapi import FastAPI, Header, HTTPException, Request
from pydantic import BaseModel, Field

from app.orchestrator import AgentState, AifpLangGraphOrchestrator
from app.security import verify_hmac_signature
from app.tools_open_source import DocumentAndTranslationAssistant, extract_clean_markdown_from_html, parse_rss_or_atom_xml

app = FastAPI(
    title="AIFP Python AI Orchestrator Service",
    version="0.2.0",
    description="LangGraph 7-Node Orchestrator with HMAC Security, SSRF Guard, Citation Grounding, and Doc/Translation Assistant",
)

orchestrator = AifpLangGraphOrchestrator()
doc_assistant = DocumentAndTranslationAssistant()


class ParseTaskRequest(BaseModel):
    user_id: str
    natural_language_prompt: str = Field(..., min_length=3, max_length=4000)
    timezone: str = "Asia/Ho_Chi_Minh"


class ExecuteAgentRequest(BaseModel):
    user_id: str
    prompt: str = Field(..., min_length=1, max_length=16000)
    allowed_tools: list[str] = Field(
        default_factory=lambda: ["searxng_search", "rss_fetch", "webpage_extract", "email_send_external"]
    )
    daily_tokens_used: int = 0
    daily_token_quota: int = 150000
    prior_fingerprints: list[str] = Field(default_factory=list)
    delivery_mode: str = "DELTA_ONLY"
    search_fixtures: list[dict[str, str]] = Field(default_factory=list)


async def require_internal_hmac(
    request: Request,
    x_internal_timestamp: int = Header(...),
    x_user_id: str = Header(...),
    x_internal_signature: str = Header(...),
) -> None:
    raw_bytes = await request.body()
    raw_body = raw_bytes.decode("utf-8")
    if not verify_hmac_signature(x_internal_timestamp, x_user_id, raw_body, x_internal_signature):
        raise HTTPException(status_code=401, detail="Invalid or expired internal HMAC signature")


@app.get("/health")
async def health() -> dict[str, str]:
    return {"status": "ok", "service": "aifp-ai-service"}


@app.post("/internal/v1/agent/parse-task")
async def parse_natural_language_task(
    payload: ParseTaskRequest,
    request: Request,
    x_internal_timestamp: int = Header(...),
    x_user_id: str = Header(...),
    x_internal_signature: str = Header(...),
) -> dict[str, Any]:
    await require_internal_hmac(request, x_internal_timestamp, x_user_id, x_internal_signature)
    lower = payload.natural_language_prompt.lower()
    cron = "0 0 8 * * *" if "8 giờ" in lower or "8h" in lower or "mỗi sáng" in lower else "0 0 9 * * *"
    return {
        "title": payload.natural_language_prompt[:72].strip(),
        "cron_expression": cron,
        "timezone": payload.timezone,
        "delivery_mode": "DELTA_ONLY",
        "required_tools": ["searxng_search", "webpage_extract", "email_send_self"],
    }


@app.post("/internal/v1/agent/execute")
async def execute_agent(
    payload: ExecuteAgentRequest,
    request: Request,
    x_internal_timestamp: int = Header(...),
    x_user_id: str = Header(...),
    x_internal_signature: str = Header(...),
) -> dict[str, Any]:
    await require_internal_hmac(request, x_internal_timestamp, x_user_id, x_internal_signature)
    state = AgentState(
        user_id=payload.user_id,
        prompt=payload.prompt,
        allowed_tools=payload.allowed_tools,
        daily_tokens_used=payload.daily_tokens_used,
        daily_token_quota=payload.daily_token_quota,
        prior_fingerprints=set(payload.prior_fingerprints),
        delivery_mode=payload.delivery_mode,
    )
    final_state = orchestrator.run(state, simulated_search_results=payload.search_fixtures)
    return {
        "status": final_state.status,
        "selected_model": final_state.selected_model,
        "active_provider": final_state.active_provider,
        "api_key_masked": final_state.api_key_masked,
        "failover_hop": final_state.failover_hop,
        "quota_downgraded": final_state.quota_downgraded,
        "final_markdown": final_state.final_markdown,
        "verified_citations": final_state.verified_citations,
        "stripped_hallucinated_urls": final_state.stripped_hallucinated_urls,
        "new_fingerprints": final_state.new_fingerprints,
        "approval_required_tool": final_state.approval_required_tool,
        "approval_payload": final_state.approval_payload,
    }


class DocumentQaRequest(BaseModel):
    user_id: str
    doc_title: str = Field(..., min_length=1, max_length=200)
    raw_text: str = Field(..., min_length=1, max_length=100000)
    question: str = Field(..., min_length=1, max_length=2000)


class TranslateRequest(BaseModel):
    user_id: str
    text: str = Field(..., min_length=1, max_length=20000)
    target_lang: str = Field(default="vi", min_length=2, max_length=16)


class WriteDraftRequest(BaseModel):
    user_id: str
    title: str = Field(..., min_length=1, max_length=255)
    doc_type: str = Field(default="EMAIL", max_length=40)
    tone: str = Field(default="CONCISE", max_length=40)
    prompt_brief: str = Field(..., min_length=1, max_length=8000)


@app.post("/internal/v1/agent/document-qa")
async def document_qa_endpoint(
    payload: DocumentQaRequest,
    request: Request,
    x_internal_timestamp: int = Header(...),
    x_user_id: str = Header(...),
    x_internal_signature: str = Header(...),
) -> dict[str, Any]:
    await require_internal_hmac(request, x_internal_timestamp, x_user_id, x_internal_signature)
    return doc_assistant.answer_question_on_document(payload.doc_title, payload.raw_text, payload.question)


@app.post("/internal/v1/agent/translate")
async def translate_endpoint(
    payload: TranslateRequest,
    request: Request,
    x_internal_timestamp: int = Header(...),
    x_user_id: str = Header(...),
    x_internal_signature: str = Header(...),
) -> dict[str, Any]:
    await require_internal_hmac(request, x_internal_timestamp, x_user_id, x_internal_signature)
    return doc_assistant.translate_text(payload.text, payload.target_lang)


@app.post("/internal/v1/agent/write-draft")
async def write_draft_endpoint(
    payload: WriteDraftRequest,
    request: Request,
    x_internal_timestamp: int = Header(...),
    x_user_id: str = Header(...),
    x_internal_signature: str = Header(...),
) -> dict[str, Any]:
    await require_internal_hmac(request, x_internal_timestamp, x_user_id, x_internal_signature)
    return doc_assistant.generate_structured_draft(
        payload.title, payload.doc_type, payload.tone, payload.prompt_brief
    )


class InternalVisionAnalyzeRequest(BaseModel):
    user_id: str
    image_name: str = "camera_capture.png"
    mode: str = "RECEIPT_OCR"
    prompt: str = ""
    image_data_base64: str = ""


@app.post("/internal/v1/agent/vision-analyze")
async def vision_analyze_endpoint(
    payload: InternalVisionAnalyzeRequest,
    request: Request,
    x_internal_timestamp: int = Header(...),
    x_user_id: str = Header(...),
    x_internal_signature: str = Header(...),
) -> dict[str, Any]:
    await require_internal_hmac(request, x_internal_timestamp, x_user_id, x_internal_signature)
    return doc_assistant.analyze_image_or_receipt(
        payload.image_name, payload.mode, payload.image_data_base64, payload.prompt
    )



