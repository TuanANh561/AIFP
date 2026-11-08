---
name: ai-langgraph-orchestrator
description: >-
  Guides the implementation of the Python 3.11 + FastAPI + LangGraph AI Orchestrator Service,
  including Model Router (Gemini / OpenAI via LiteLLM), Open-Source Research Pipeline (SearXNG +
  Trafilatura/Crawl4AI), Document Assistant (PyMuPDF4LLM/Docling), HITL Checkpointing, and Citation
  Grounding Validator. Activate this skill when working on Python AI agents, prompts, or tools.
---

# AI LangGraph Orchestrator & Open-Source Tooling Skill

Activate this skill when implementing or debugging the Python AI Service (`ai-service`), LangGraph state graphs, LLM routing, or open-source tool integrations.

## 1. LangGraph 7-Node State Machine

All user requests and scheduled tasks execute through a typed LangGraph state machine (`AgentState`):

1. **`understand_node`**: Parses user intent, extracts structured task parameters (schedule cron, timezone, filters, output channels), or detects ambiguity to ask a concise clarifying question.
2. **`plan_node`**: Breaks the goal into a bounded list of steps (`max_steps <= 8`).
3. **`select_tools_node`**: Maps each step to registered tools in `ToolRegistry` with JSON Schema validation.
4. **`authorize_node`**: Evaluates each selected tool against user's `allowed_tools` and `permission_level`:
   - Level 1–2: Proceed to `execute_node`.
   - Level 3 (`APPROVAL_REQUIRED`): Persist LangGraph checkpoint, emit `WAITING_FOR_APPROVAL` payload to Spring Boot, and halt graph execution cleanly.
   - Level 4 (`RESTRICTED`): Reject immediately and log security event.
5. **`execute_node`**: Runs approved tools with per-tool timeout (`15s`), retry (`max 2`), and loop detector (blocks identical `tool_name + args` called twice in the same run).
6. **`verify_node`**:
   - Validates output against user constraints.
   - Runs **Citation Grounding Validator**: ensures every URL cited in the synthesis exists in `state.discovered_urls`.
   - Computes delta fingerprints against prior task runs.
7. **`respond_node`**: Formats clean, concise output (Markdown for UI/Email, compact summary `< 140 chars` for Web Push notification).

## 2. Open-Source Stack for High Quality & Low Cost

Leverage proven open-source components to minimize recurring SaaS API costs:

- **Search & Research (`SearXNG` + `Trafilatura` / `Jina Reader`)**:
  - Primary search via self-hosted Dockerized **SearXNG** JSON API (aggregates Google, Bing, DuckDuckGo, Qwant, GitHub, ArXiv with zero per-query cost), with configurable fallback to **Tavily / Brave Search API**.
  - Content extraction via **Trafilatura** (Python library) to strip boilerplate HTML/ads and extract clean Markdown + publication date + canonical URL.
- **Model Router (`LiteLLM` / Native SDKs)**:
  - **Tier Fast/Cheap (`gemini-2.5-flash` / `gpt-4o-mini`)**: Used for intent classification, query expansion, initial page filtering, translation, and memory extraction.
  - **Tier Reasoning (`gemini-2.5-pro` / `gpt-4o`)**: Used for complex multi-source synthesis, code analysis, and multi-step planning.
  - **Automatic Quota Fallback**: If a user reaches 80% of their daily token budget, automatically route all remaining tasks to the Fast/Cheap tier.
- **Document Parsing (`PyMuPDF4LLM` / `Docling`)**:
  - Convert uploaded PDFs, DOCX, and Markdown files into structured chunks with page numbers for cited Q&A.
  - Always preserve core technical terminology (`Spring Boot`, `LangGraph`, `pgvector`, `Flutter`, `Argon2id`, `HMAC-SHA256`) during bilingual translation.
- **XXE & Hidden-DOM Hardening in Scrapers**:
  - Reject any RSS/Atom XML payload containing `<!DOCTYPE` or `<!ENTITY` declarations before parsing.
  - Strip `<script>`, `<style>`, and hidden `style="display:none"` elements from HTML before wrapping in `<untrusted_external_content>`.

