# 2026-06-10 — rules-curator — WI-20260610-BACKEN-004

## Work Item
WI-20260610-BACKEN-004

## Role
rules-curator

## Mode
triage

## Inputs received
- ai_memory/2026-06-10__ROLE-developer__WI-20260610-BACKEN-004.md (Proposed Rules section)
- ai_rules/INDEX.md
- ai_rules/conventions.md
- backend/src/main/java/fr/eni/gestionformation/exception/GlobalExceptionHandler.java

## Per-item decisions

1. TYPE: CONVENTION — "Mapping HTTP des exceptions metier dans GlobalExceptionHandler"
   - Decision: ACCEPT (written as CONV-003)
   - Reason: Verified against current GlobalExceptionHandler.java — pattern is already consistently applied across 7 handlers (Filiere, Cursus, Cours, User, InvalidInvitationToken, CycleDetected) with correct semantic codes (404/409/422/400). Slight wording amendment vs. proposal: explicitly enumerated the 400 case (invalid input, e.g. invitation token) which the proposal omitted, to fully cover existing precedent.

## Files touched (ai_rules/)
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\conventions.md (added CONV-003)
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\INDEX.md (added CONV-003 line)

## INDEX diff summary
+ CONVENTION | CONV-003 | HTTP mapping of business exceptions in GlobalExceptionHandler | conventions.md#conv-003 | Verified: 2026-06-10

## Next Actions for the manager
None. No REPO_STATE.md or ai_memory ledger changes required.
