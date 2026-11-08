---
name: backend-spring-modular
description: >-
  Guides the implementation of the Java 21 + Spring Boot 3 Modular Monolith, PostgreSQL + pgvector
  schema, Flyway migrations, Quartz/JobRunr idempotent task scheduler, Redis distributed locks,
  and Web Push / Email delivery. Activate this skill when working on backend APIs, database entities,
  background workers, or scheduling logic.
---

# Backend Spring Boot Modular Monolith Skill

Activate this skill when building or modifying the Spring Boot backend (`backend-core`), database migrations, task scheduling, or notification pipelines.

## 1. Modular Monolith Package Structure

Keep strict package boundaries inside `com.aifp.core` so modules only communicate via public service interfaces and domain events:

```text
com.aifp.core
├── common/          # ApiResponse, GlobalExceptionHandler, SecurityContextUtils, HMACSigner
├── auth/            # JWT, RefreshTokenRotation, Argon2id, OAuth/Session management
├── user/            # User, UserProfile, preferences, quota counters
├── chat/            # Conversation, Message, SSE streaming proxy to Python AI Service
├── task/            # AiTask, TaskSchedule, TaskExecution, Idempotency & Delta fingerprinter
├── scheduler/       # JobRunr / Quartz triggers, Redis distributed lock, retry & misfire handler
├── tool/            # ToolRegistry, UserToolPermission, 4-Level Permission Gate, HITL Approvals
├── memory/          # PersonalMemory CRUD, explicit vs candidate memories, pgvector search
├── notification/    # Inbox notifications, Web Push (VAPID nl.martijndwars:web-push), Email (Resend/SMTP)
└── security/        # SecurityAuditLog, RateLimitingFilter (Bucket4j + Redis), EmergencyKillSwitch
```

## 2. Idempotent Scheduler & Worker Execution Rules

Every scheduled or manual task execution MUST follow the **Idempotent State Machine**:

1. **Deterministic Idempotency Key**:
   - Compute `execution_key = SHA256(task_id + ":" + scheduled_slot_utc_iso)`.
   - Enforce `UNIQUE (task_id, idempotency_key)` in `task_executions`.
2. **Distributed Lock**:
   - Acquire Redis lock `lock:task_exec:{execution_key}` with TTL `300s` before dispatching to worker.
3. **State Transitions**:
   - `QUEUED` -> `RUNNING` -> (`WAITING_FOR_APPROVAL` | `COMPLETED` | `COMPLETED_NO_DELTA` | `FAILED_RETRYABLE` | `FAILED_FINAL` | `CANCELLED`).
4. **Delta Deduplication (Chống Gửi Trùng Báo Cáo)**:
   - Each research item discovered produces a `content_fingerprint = SHA256(canonical_url + "|" + normalized_title)`.
   - Compare against fingerprints stored from the last `N` executions of the same `task_id`.
   - If `task.delivery_mode == 'DELTA_ONLY'` and zero new items exist, mark execution `COMPLETED_NO_DELTA` and skip sending redundant email.
5. **Retry with Exponential Backoff**:
   - Max 3 retries (`30s`, `2m`, `10m`) for transient network/LLM 5xx errors.
   - Never resend an email if `email_logs` already records `status = 'SENT'` for `execution_id`.

## 3. Internal Service Communication (`Spring Boot <-> Python AI Service`)

- Spring Boot is the **Single Gatekeeper** for authentication and database ownership.
- When calling the Python AI Service (`/internal/v1/agent/plan`, `/internal/v1/agent/execute`):
  - Propagate `X-Request-Id`, `X-User-Id`, `X-User-Timezone`, `X-Allowed-Tools`, and `X-Quota-Remaining-Tokens`.
  - Sign requests with `X-Internal-Signature: HMAC_SHA256(timestamp + "." + body, INTERNAL_SHARED_SECRET)`.

## 4. Lessons Learned & Self-Evolved Patterns

1. **Audit Log Transaction Isolation (`REQUIRES_NEW`)**:
   - `SecurityAuditService.logEvent()` uses `@Transactional(propagation = Propagation.REQUIRES_NEW)` so security events (`SECURITY_TOKEN_REUSE_DETECTED`, `TOOL_DENIED_LEVEL4`) are committed even when the calling service throws an exception.
   - Therefore, `security_audit_logs.user_id` is stored as an uncoupled `UUID` column (without a blocking FK to `users`), preventing cross-transaction lock/FK conflicts during registration and preserving the audit trail if a user is later deleted.
2. **Maven JDK 17/21 Toolchain Compatibility**:
   - Set `<java.version>17</java.version>` in `backend-core/pom.xml` so builds pass out-of-the-box whether Maven's `JAVA_HOME` points to JDK 17 or JDK 21 LTS.
3. **Flyway SQL Standard Timestamp (`TIMESTAMP WITH TIME ZONE`)**:
   - Always use ANSI SQL `TIMESTAMP WITH TIME ZONE` (never the Postgres-only shorthand `TIMESTAMPTZ`) in `src/main/resources/db/migration/V*.sql` so Flyway migrations execute identically on both PostgreSQL 16 in production and H2 in-memory integration tests.

