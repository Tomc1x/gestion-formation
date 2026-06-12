# Rules Curator Report

Work Item: WI-20260610-BACKEN-007
Role: rules-curator
Mode: triage

Inputs received:
- ai_memory/2026-06-10__ROLE-developer__WI-20260610-BACKEN-007.md (Proposed Rules section)
- backend/src/main/java/fr/eni/gestionformation/controller/FiliereController.java (verification)

Per-item decisions:
1. PITFALL "Filière endpoint is `/api/filiere` (singular)" — ACCEPT.
   Verified `@RequestMapping("/api/filiere")` at FiliereController.java:14. New entry PIT-003 written.

Files touched:
- ai_rules/pitfalls.md (added PIT-003)
- ai_rules/INDEX.md (added PIT-003 line)

INDEX diff summary:
- Added row: PITFALL | PIT-003 | Filière API endpoint is singular `/api/filiere` | pitfalls.md#pit-003 | Verified: 2026-06-10

Next Actions for the manager:
- None.
