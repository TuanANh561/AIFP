---
name: agile-state-manager
description: >-
  Manages session continuity, Agile Sprint backlog progression, quality verification gates,
  and continuous self-improvement of skills. Activate this skill at the beginning and end of
  every coding or planning session to read prior progress from docs/PROJECT_STATE.md, pick the
  next task, and update state/skills upon completion.
---

# Agile State Manager & Self-Evolving Skill Protocol

Use this skill to ensure seamless continuity across sessions so the agent always picks up exactly where the previous session left off, tracks progress against the 1-Month Agile Roadmap, and evolves new skills as the codebase grows.

## 1. Pre-Session Startup Procedure (Đầu mỗi phiên làm việc)

1. Read [`docs/PROJECT_STATE.md`](../../../docs/PROJECT_STATE.md) to check:
   - **Current Sprint & Active Task**: Which Sprint (1–4) and Story ID is currently in progress.
   - **Last Session Summary**: What files were created/modified and what tests passed or failed.
   - **Open Issues / Blockers**: Any unfinished migrations, failing tests, or pending user decisions.
2. Read [`docs/AGILE_1MONTH_ROADMAP.md`](../../../docs/AGILE_1MONTH_ROADMAP.md) to align the user's request with the current Sprint backlog.
3. If the user says *"tiếp tục làm"* (continue working) without specifying a task:
   - Automatically select the highest-priority `TODO` or `IN_PROGRESS` task from the current Sprint in `docs/PROJECT_STATE.md`.
   - Announce the selected Story ID, its Acceptance Criteria, and begin implementation immediately.

## 2. Definition of Done (DoD) Verification Gate

Before marking any task as `[x] DONE` in `docs/PROJECT_STATE.md` or `docs/AGILE_1MONTH_ROADMAP.md`:

- **Code Completeness**: No `TODO` placeholders in security, tenant isolation, or error-handling paths.
- **Security Check**: Verified against `.agents/skills/security-guardian/SKILL.md` (`user_id` isolation, input validation, no leaked secrets).
- **UI Check** (if frontend touched): Verified against `.agents/skills/human-ui-craft/SKILL.md` (zero AI-slop gradients, max 1–3 words on buttons, responsive `100dvh` on mobile).
- **Build & Test**: Run the relevant build/test command (e.g., `npm run build`, `./mvnw test`, `pytest`) and confirm exit code `0`.

## 3. Post-Session Update & Skill Evolution Procedure (Cuối mỗi phiên làm việc)

At the end of every task, perform these updates before replying to the user:

1. **Update `docs/PROJECT_STATE.md`**:
   - Move completed Story IDs to `Completed in Current Sprint`.
   - Set the next Story ID as `Next Up`.
   - Append a concise entry to the `Session Changelog` table with timestamp, completed work, verified commands, and next step.
2. **Update `docs/AGILE_1MONTH_ROADMAP.md`**:
   - Check off `[x]` completed tasks and update the Sprint completion percentage.
3. **Evolve Skills (`.agents/skills/`)**:
   - Ask: *"Did we introduce a new open-source tool, database pattern, security rule, or debugging workflow during this session?"*
   - If **YES**:
     - Update the corresponding `SKILL.md` in `.agents/skills/<skill-name>/SKILL.md`, OR
     - Create a new skill folder `.agents/skills/<new-skill-name>/SKILL.md` with proper YAML frontmatter (`name`, `description`) and register it in `docs/PROJECT_STATE.md`.

