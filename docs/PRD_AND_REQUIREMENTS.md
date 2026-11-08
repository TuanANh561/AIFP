# Product Vision & Requirements Specification (PRD)

**Project**: AI Life Assistant Platform (`AIFP`)  
**Version**: 1.0 (Production Blueprint)  
**Target Platforms**: Web App & iPhone Safari PWA (Phase 1) · Flutter iOS/Android & PC Companion (Phase 2+)

---

## 1. Product Vision & Core Philosophy

**AI Life Assistant Platform (AIFP)** is a multi-purpose personal AI assistant platform where users describe their goals in natural language, and the system autonomously plans, selects appropriate AI models and registered tools, executes background schedules on the server, and delivers verified results via Web/PWA, Email, and Push Notifications.

### Core Design Pillars
1. **One Platform, Multiple Personas**: Serves students, office workers, software engineers, business owners, shoppers, content creators, job seekers, and general users through configurable **Task Templates**, **Tool Packs**, and **Personal Memory**—without fragmenting into separate apps.
2. **Server-Side Autonomy, Light Client Experience**: Heavy AI workflows, web research, and cron schedules run reliably on the server; the user's iPhone (via Safari PWA + Web Push) receives actionable digests without draining battery or fighting iOS background execution limits.
3. **Zero-Trust AI Execution**: AI Agents never hold raw system credentials or unrestricted device access. Every action passes through a **4-Level Backend Permission Gate** with **Human-in-the-Loop (HITL)** approval for sensitive operations.
4. **Human-Crafted Minimalist UI**: Calm, high-signal interface free of generic AI visual clichés (no neon purple gradients, no verbose helper text on every screen, concise 1–3 word action buttons).

---

## 2. User Personas & Reusable Capability Mapping

| Persona | Core Needs | Example Automated Workflow | Tools Activated |
| :--- | :--- | :--- | :--- |
| **Student** | Study materials, exam reminders, paper summaries | *"Summarize uploaded lecture PDF and quiz me every Tuesday at 20:00"* | `doc_parser`, `memory_store`, `scheduler`, `web_push` |
| **Office Worker** | Email drafting, daily briefing, schedule prep | *"Every morning at 07:30, summarize my priority tasks and draft status report"* | `task_reader`, `email_draft`, `scheduler` |
| **Software Developer** | Tech release tracking, changelogs, doc search | *"Track Spring Boot, LangGraph, and Flutter security advisories weekly"* | `searxng_search`, `rss_reader`, `delta_filter`, `email_send` |
| **Job Seeker** | Job board monitoring, JD matching | *"Find new Java/Flutter Intern jobs in HCMC every morning at 08:00; send only new listings"* | `searxng_search`, `page_extractor`, `delta_filter`, `email_send` |
| **Business / Market Analyst** | Competitor news, industry shifts | *"Monitor 5 industry RSS/news sources daily and flag regulatory changes"* | `searxng_search`, `rss_reader`, `citation_verifier` |
| **Shopper** | Product comparison, price drop monitoring | *"Compare mechanical keyboards under 2.5M VND with cited reviews"* | `searxng_search`, `structured_extractor` |
| **Content Creator** | Trend discovery, outline generation | *"Every Monday at 09:00, compile top 5 AI engineering topics with source links"* | `searxng_search`, `rss_reader`, `writing_assistant` |
| **General User** | Translation, life admin, personal memory | *"Remember my passport expires on 12/2027 and remind me 6 months prior"* | `memory_store`, `scheduler`, `web_push` |

---

## 3. Explicit + Unstated Practical Requirements (Yêu Cầu Thực Tế Bổ Sung)

To prevent real-world failures common in AI Agent platforms, the following **10 critical operational requirements** are mandatory in addition to the core feature list:

### 3.1. Content Deduplication & "Delta-Only" Reporting (Chống Gửi Trùng Kết Quả)
- **Problem**: A daily scheduled task *"Find Flutter Intern jobs at 08:00"* will find the same 8 listings 5 days in a row, spamming the user's inbox.
- **Requirement**:
  - Every extracted item must generate a deterministic `content_fingerprint = SHA256(canonical_url + "|" + normalized_title)`.
  - Tasks support two delivery modes: `DELTA_ONLY` (default for recurring monitors—only notify/email when genuinely new items appear compared to the last 30 days of fingerprints) and `ALWAYS_SEND` (send digest even if unchanged, with a clear note `"Không có tin mới hôm nay"`).

### 3.2. End-to-End Idempotency & Crash Recovery (Chống Chạy Lặp Khi Lỗi Giữa Chừng)
- **Problem**: If a background worker crashes after sending an email but before updating the database, a retry will send duplicate emails.
- **Requirement**:
  - Every scheduled execution generates `idempotency_key = SHA256(task_id + ":" + scheduled_slot_utc)`.
  - Step-level state checkpointing records `search_completed_at`, `synthesis_completed_at`, `email_sent_at`, and `push_sent_at`. Retries resume from the first incomplete step and check `email_logs` by `execution_id` before dispatching SMTP/Resend requests.

### 3.3. Timezone, DST & Misfire Policy (Quản Lý Múi Giờ & Lịch Bị Trễ)
- **Problem**: Server runs in `UTC`, user lives in `Asia/Ho_Chi_Minh` (`UTC+7`). Server downtime of 15 minutes at 08:00 could cause tasks to be skipped forever or fired 10 times upon restart.
- **Requirement**:
  - Every `task_schedules` row stores an explicit IANA timezone (`Asia/Ho_Chi_Minh`) and `misfire_policy`:
    - `FIRE_ONCE_WITHIN_WINDOW` (default: if delayed `< 2 hours`, run once immediately; if delayed `>= 2 hours`, mark `SKIPPED_STALE` and schedule next slot).
    - Never fire catch-up bursts for multiple missed slots.

### 3.4. Citation Grounding & Anti-Hallucination Validator (Kiểm Chứng Nguồn Thật)
- **Problem**: LLMs frequently fabricate plausible-looking URLs when summarizing research.
- **Requirement**:
  - The Python AI Service maintains an immutable set of `discovered_urls` collected directly from Search/RSS tool responses during that execution.
  - Before any response or email is persisted, a post-processing **Citation Grounding Validator** verifies every link `[label](url)` against `discovered_urls`. Hallucinated URLs are stripped and flagged.
  - Every research report separates **Verified Facts (with Source URL + Retrieval Timestamp)** from **AI Synthesis/Analysis**.

### 3.5. Human-in-the-Loop (HITL) Approval State Machine
- **Problem**: Level 3 tools (sending external emails to third parties, modifying calendars, deleting records) require user confirmation, which may happen minutes or hours later.
- **Requirement**:
  - LangGraph checkpoints the exact execution state to PostgreSQL (`task_approval_requests`) with status `WAITING_FOR_APPROVAL` and a `24-hour TTL`.
  - User receives an interactive card in Inbox / Chat showing the exact tool name, parameters (e.g., recipient, subject, body), and 3 concise actions: `Duyệt` (Approve), `Sửa` (Edit & Approve), `Từ chối` (Reject).

### 3.6. Personal Memory Hygiene, Confidence & Conflict Resolution
- **Problem**: Auto-saving every chat turn into vector memory pollutes context with temporary remarks, contradictions, or prompt injections.
- **Requirement**:
  - Separate memory into 3 distinct tiers:
    1. **Explicit Profile (`user_profiles`)**: Deterministic settings (Timezone, Language, Job Role, Preferred Channels).
    2. **Verified Memories (`ai_memories` with `status = 'ACTIVE'`)**: Long-term facts approved by user or explicitly commanded (*"Hãy nhớ rằng..."*).
    3. **Candidate Memories (`ai_memories` with `status = 'CANDIDATE'`)**: AI-extracted facts presented unobtrusively for 1-click user confirmation or dismissal.
  - When a new memory contradicts an existing memory on the same topic key, supersede the old memory (`status = 'SUPERSEDED'`) rather than keeping conflicting vectors.

### 3.7. Token Budget, Cost Circuit Breaker & Loop Detection
- **Problem**: An autonomous agent stuck in a retry loop or processing huge web pages can burn $20 of API credits in minutes.
- **Requirement**:
  - Hard per-execution limits: `max_steps = 8`, `max_tool_calls = 10`, `max_input_tokens = 32,000`, `execution_timeout = 90s`.
  - **Duplicate Call Detector**: If the agent invokes the same tool with identical normalized arguments twice in one run, block the second call and force synthesis.
  - **User Quota & Model Downgrade**: Track daily token/cost usage per user in Redis/PostgreSQL. At `80%` daily quota, automatically switch the user's requests from Pro models to Flash/Lite models; at `100%`, pause non-critical background tasks until reset.

### 3.8. iOS Safari PWA Standalone & Web Push Onboarding Reality
- **Problem**: On iOS (16.4+), Web Push API (`PushManager`) is completely unavailable in regular Safari tabs and only works after the user adds the web app to the Home Screen AND taps a button inside the standalone app to grant permission.
- **Requirement**:
  - Frontend detects `isIOS()` and `isStandalone()` (`window.navigator.standalone === true` or `matchMedia('(display-mode: standalone)').matches`).
  - When an iOS user enables Push Notifications while in regular Safari, display a clean 3-step visual drawer guiding them to `Share -> Add to Home Screen -> Open App`.
  - Inside Standalone mode, trigger `Notification.requestPermission()` strictly inside a synchronous user `onClick` handler.

### 3.9. Self-Hosted Open-Source First (Giảm Phụ Thuộc API Đắt Đỏ)
- **Requirement**:
  - Use **SearXNG** (Dockerized open-source metasearch) + **Trafilatura** (open-source article extraction) as the default zero-cost research engine, with pluggable fallback to commercial APIs (Tavily, Brave, Serper).
  - Use **PyMuPDF4LLM** for fast local PDF/document-to-Markdown extraction without sending raw binary files to paid third-party OCR APIs unless Vision is explicitly requested.

### 3.10. Account Data Export & Right to Erasure
- **Requirement**:
  - Users can export their tasks, memories, and chat history as JSON/Markdown and permanently wipe all memories or their entire account with cascading deletion across PostgreSQL, `pgvector`, Redis, and S3 object storage.

---

## 4. MVP & 1-Month Agile Acceptance Criteria

| Module | Acceptance Criteria (Definition of Done) |
| :--- | :--- |
| **1. Auth & Tenant Isolation** | Register, login, Argon2id hashing, 15m JWT + rotating HttpOnly Refresh Token. User A cannot read/write User B's resources under any API manipulation (`403/404` enforced by integration test). |
| **2. AI Chat & Model Router** | Multi-turn streaming chat (SSE), automatic routing between fast (`gemini-2.5-flash`) and reasoning (`gemini-2.5-pro`) models, graceful error banner on provider timeout. |
| **3. AI Research (Open-Source Pipeline)** | Queries SearXNG + RSS, extracts clean article text via Trafilatura, deduplicates URLs, synthesizes structured report, and passes Citation Grounding check (`100%` of links verified). |
| **4. AI Tools & 4-Level Security Gate** | Tool Registry in DB; Level 1–2 execute within quota; Level 3 pauses execution and creates a `WAITING_FOR_APPROVAL` request in UI; Level 4 is rejected and logged. |
| **5. Custom Tasks (NL-to-Config)** | User types natural language request -> AI returns structured preview card (Goal, Sources, Cron Schedule, Timezone, Delivery Mode `DELTA_ONLY`, Required Tools) -> User confirms to activate. |
| **6. Scheduler & Idempotent Worker** | Server executes active tasks on schedule while browser/iPhone is off; handles retries without duplicate emails; skips email when `DELTA_ONLY` finds zero new items. |
| **7. Email Center** | Sends clean HTML/Plaintext digest via Resend/SMTP to verified user email; logs delivery status in `email_logs`. |
| **8. Notification Center & iOS PWA** | In-app notification inbox + Web Push via VAPID Service Worker; tested on desktop and iPhone Home Screen PWA (`100dvh` responsive layout). |
| **9. Personal Memory & Doc Assistant** | User can view, edit, approve/reject candidate memories, delete memories, and upload PDF/Markdown documents for cited Q&A and translation. |

