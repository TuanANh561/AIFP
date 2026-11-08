# System Architecture & Open-Source Integration Blueprint

**Project**: AI Life Assistant Platform (`AIFP`)  
**Architecture Style**: Spring Boot 3 Modular Monolith + Dedicated Python LangGraph AI Service + Open-Source Self-Hosted Tooling

---

## 1. High-Level 3-Tier Architecture

The platform separates **User Experience (Tier 1)**, **Business & Security Gatekeeping + AI Intelligence (Tier 2)**, and **Execution & Open-Source Integrations (Tier 3)**. We intentionally avoid premature microservices sprawl while isolating the Python AI orchestration runtime from the Java business/security core.

```mermaid
flowchart TD
    subgraph Tier1["Tier 1: User Experience (Web & Mobile)"]
        PWA["React 18 + TypeScript + Vite PWA<br/>(Desktop & iPhone Safari Home Screen)"]
        Flutter["Flutter Native App<br/>(Phase 2: iOS / Android)"]
    end

    subgraph Tier2["Tier 2: Core Backend & AI Intelligence"]
        SB["Spring Boot 3 Modular Monolith<br/>(Auth, Tenant Isolation, Task CRUD,<br/>4-Level Tool Gate, Scheduler, Audit)"]
        PY["Python FastAPI + LangGraph Service<br/>(Planner, 7-Node Agent Graph,<br/>LiteLLM Router, Citation Validator)"]
    end

    subgraph Tier3["Tier 3: Data, Execution & Open-Source Engines"]
        PG[("PostgreSQL 16 + pgvector<br/>(Multi-tenant Data & Vector Memory)")]
        RD[("Redis 7<br/>(Cache, Rate Limit, Locks, Kill-Switch PubSub)")]
        SX["SearXNG + Trafilatura<br/>(Open-Source Metasearch & Scraper)"]
        DOC["PyMuPDF4LLM / Docling<br/>(Open-Source Document Parser)"]
        S3[("MinIO / S3 Object Storage<br/>(Encrypted User Uploads)")]
        NOTIF["Web Push (VAPID) & Resend/SMTP<br/>(Notification & Email Delivery)"]
    end

    PWA -->|HTTPS + JWT / SSE| SB
    Flutter -.->|HTTPS + JWT| SB
    SB <-->|Internal HMAC-Signed HTTP/SSE| PY
    SB --> PG
    SB --> RD
    SB --> S3
    SB --> NOTIF
    PY --> SX
    PY --> DOC
    PY --> PG
    PY --> RD
```

---

## 2. Strategic Open-Source Stack (Tối Ưu Chi Phí & Chất Lượng Bằng Mã Nguồn Mở)

Instead of paying for expensive proprietary APIs for every sub-function, AIFP integrates battle-tested open-source projects:

| Capability | Open-Source Project | License | Role in AIFP & Why Chosen |
| :--- | :--- | :--- | :--- |
| **Web Search Engine** | **SearXNG** (Dockerized) | AGPLv3 | Self-hosted metasearch aggregating Google, Bing, DuckDuckGo, GitHub, ArXiv, and Google News via JSON API at **$0/query**. Fallback to Tavily/Brave API when needed. |
| **Web Content Extraction** | **Trafilatura** + **BeautifulSoup4** | Apache-2.0 | Strips ads, navbars, and tracking scripts from HTML; extracts clean Markdown, author, date, and canonical URL locally in Python without paid scraping APIs. |
| **RSS / Atom Aggregation** | **feedparser** + **httpx** | BSD | Fast, lightweight polling of tech blogs, recruitment feeds, and news portals with ETag / `If-Modified-Since` caching. |
| **Agent Orchestration** | **LangGraph** | MIT | Stateful, cyclic graph execution with built-in checkpointing (`PostgresSaver`), ideal for **Human-in-the-Loop (HITL)** pause/resume on Level 3 tools. |
| **Model Router & Cost Guard** | **LiteLLM** / Custom Router | MIT | Unified interface over **Gemini 2.5 Flash/Pro** and **OpenAI GPT-4o-mini/4o**, providing automatic fallback, token counting, and budget enforcement. |
| **Document Parsing** | **PyMuPDF4LLM** | AGPL/Commercial | Converts PDFs and office documents into clean Markdown with page metadata in milliseconds on CPU. |
| **Vector Search & Hybrid RAG** | **pgvector** + PostgreSQL FTS | PostgreSQL | Runs inside the primary PostgreSQL instance (`HNSW` index + `tsvector` BM25 hybrid ranking)—eliminating external vector DB hosting costs. |
| **Distributed Job Scheduling** | **JobRunr** (or **Quartz**) | LGPL / Apache-2.0 | Persistent background jobs in Spring Boot backed by PostgreSQL + Redis locks, supporting cron timezones and retry backoff. |
| **Rate Limiting** | **Bucket4j** + Redis | Apache-2.0 | Token-bucket rate limiting per `user_id` and IP address at the Spring Boot API gateway filter. |
| **Web Push Protocol** | **web-push-java** (`nl.martijndwars`) | MIT | Standards-compliant RFC 8030 / VAPID encrypted Web Push delivery directly to Safari iOS 16.4+ and desktop browsers without requiring Firebase on Web. |

---

## 3. End-to-End Task Execution Sequence (Luồng Xử Lý Thực Tế)

Example: User creates task *"Kiểm tra tin tuyển dụng Flutter Intern tại TP.HCM mỗi sáng lúc 08:00 và gửi email nếu có tin mới."*

```mermaid
sequenceDiagram
    autonumber
    actor User as User (iPhone PWA / Web)
    participant SB as Spring Boot Core
    participant PY as Python AI Service (LangGraph)
    participant DB as PostgreSQL + Redis
    participant SX as SearXNG + Trafilatura
    participant Notify as Email & Web Push

    User->>SB: POST /api/v1/tasks/parse ("Kiểm tra tin Flutter Intern 8h sáng...")
    SB->>PY: HMAC-Signed POST /internal/v1/agent/parse-task
    PY-->>SB: Structured TaskConfig (cron: "0 0 8 * * *", tz: "Asia/Ho_Chi_Minh", mode: "DELTA_ONLY", tools: ["searxng_search", "email_digest"])
    SB-->>User: Return Task Preview Card for confirmation
    User->>SB: POST /api/v1/tasks (Confirm & Activate Task)
    SB->>DB: Save ai_tasks + task_schedules (ACTIVE)

    Note over SB,DB: Next Day 08:00:00 (Asia/Ho_Chi_Minh)
    SB->>DB: Compute idempotency_key = SHA256(task_id + slot) & Acquire Redis Lock
    SB->>DB: Insert task_executions (status = RUNNING)
    SB->>PY: POST /internal/v1/agent/execute-task (with prior 30d content_fingerprints)
    PY->>SX: Query SearXNG JSON API + Fetch pages via Trafilatura (SSRF-guarded)
    SX-->>PY: Clean Markdown articles + discovered_urls
    PY->>PY: Deduplicate against prior fingerprints & Validate Citations
    alt New items found (Delta > 0)
        PY-->>SB: ExecutionResult (status: COMPLETED, new_items: 4, verified_citations: 4)
        SB->>DB: Save execution result + new fingerprints
        SB->>Notify: Send HTML Email (idempotent check on email_logs) + iOS Web Push
        Notify-->>User: Push Notification & Email Digest received at 08:01
    else No new items (Delta == 0 & mode == DELTA_ONLY)
        PY-->>SB: ExecutionResult (status: COMPLETED_NO_DELTA)
        SB->>DB: Mark COMPLETED_NO_DELTA (Skip redundant email spam)
    end
```

---

## 4. Service-to-Service Security Boundary (`Spring Boot <-> Python AI Service`)

1. **Network Isolation**: In Docker Compose / production VPC, `ai-service` (`port 8001`) is bound only to the internal backend network and is never exposed to the public internet.
2. **Signed Request Envelope**:
   - Every call from Spring Boot to Python includes:
     - `X-Internal-Timestamp`: Unix epoch seconds (reject if skew `> 60s`).
     - `X-User-Id`: Verified UUID from JWT.
     - `X-Execution-Id`: UUID for tracing and kill-switch cancellation.
     - `X-Internal-Signature`: `HMAC-SHA256(timestamp + "." + user_id + "." + raw_body, INTERNAL_SHARED_SECRET)`.
3. **Real-Time Kill Switch Propagation**:
   - Python `ai-service` subscribes to Redis channel `aifp:kill_switch`.
   - Before each LangGraph node transition and inside streaming loops, the worker checks if `user_id` or `execution_id` has been revoked. If triggered, it raises `TaskAbortedByKillSwitchException` within `< 1 second`.

