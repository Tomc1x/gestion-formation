# Work Item: WI-20260611-FULLST-030

## Role
rules-curator

## Mode
triage

## Inputs received
- Role note: ai_memory/2026-06-11__ROLE-devops-engineer__WI-20260611-FULLST-030.md (Proposed Rules section, 3 entries)
- ai_rules/INDEX.md (checked for duplicates/Java 21 mentions — none found)

## Per-item decisions

1. **ACCEPT** — Java 25 vs Java 21 pitfall -> written as PIT-021 (pitfalls.md). No existing ai_rules entry mentioned Java 21, so no conflict/deprecation needed within ai_rules. STACK_SPEC.md / brief docs outside ai_rules scope may still say Java 21 — flagged for manager below.

2. **ACCEPT** — package-lock.json desync / `npm ci` EUSAGE -> written as PIT-022 (pitfalls.md). Marked with a counter-indication: once the lock file is regenerated and `npm ci` verified, this entry should be deprecated.

3. **ACCEPT** — `.env` `$` -> `$$` escaping for docker-compose -> written as CONV-010 (conventions.md).

No MERGE/AMEND/REJECT/DEPRECATE needed — no overlapping existing entries found.

## Files touched
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\pitfalls.md (PIT-021, PIT-022 appended)
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\conventions.md (CONV-010 appended)
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\INDEX.md (3 new lines added)

## INDEX diff summary
Added 3 lines after CONV-009:
- PIT-021 | backend/build.gradle pins Java 25, not Java 21
- PIT-022 | frontend/package-lock.json desynced from package.json breaks `npm ci`
- CONV-010 | Escape `$` as `$$` in `.env` values for docker-compose

## Next Actions for the manager
- If `STACK_SPEC.md` (or any brief outside ai_rules/) documents "Java 21" for the backend, it is stale vs `backend/build.gradle` (Java 25, confirmed working in WI-20260611-FULLST-030). Out of rules-curator scope — manager should create a follow-up to correct STACK_SPEC.md / briefs to Java 25.
- Optional follow-up (frontend): regenerate `frontend/package-lock.json` via `npm install` + commit, then switch `frontend/Dockerfile`/CI back to `npm ci`, then deprecate PIT-022.
