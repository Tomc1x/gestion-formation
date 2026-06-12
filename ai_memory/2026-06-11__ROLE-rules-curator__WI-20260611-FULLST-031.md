# WI-20260611-FULLST-031 — rules-curator triage

## Work Item
WI-20260611-FULLST-031

## Role
rules-curator

## Mode
triage

## Inputs received
- role_notes: ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-031.md (Proposed Rules section)

## Per-item decisions
1. ACCEPT — "Catalogue cours 26 cross-cursus junction" -> new entry PIT-023 (pitfalls.md). No existing entry covers DWWM/CDA cursus-alerts cross-cursus interaction; distinct from PIT-008/009/020.
2. ACCEPT — "PUT /api/cours/{id} ignores prerequisIds" -> new entry PIT-024 (pitfalls.md). Distinct from PIT-003 (filiere endpoint naming); no existing entry on CoursController.update / prerequisIds.

## Files touched
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\pitfalls.md (added PIT-023, PIT-024)
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\INDEX.md (added 2 index lines)

## INDEX diff summary
+ PITFALL | PIT-023 | Catalogue cours id 26 is a cross-cursus junction point (DWWM/CDA) | pitfalls.md#pit-023 | 2026-06-11
+ PITFALL | PIT-024 | PUT /api/cours/{id} silently ignores `prerequisIds` | pitfalls.md#pit-024 | 2026-06-11

## Next Actions for manager
None — no REPO_STATE.md fields to remove, no deprecations.
