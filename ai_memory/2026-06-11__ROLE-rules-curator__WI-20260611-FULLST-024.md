# Rules curation — WI-20260611-FULLST-024

## Work Item
WI-20260611-FULLST-024

## Role
rules-curator

## Mode
triage

## Inputs received
- ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-024.md (Proposed Rules section, 2 entries)
- ai_rules/INDEX.md
- ai_rules/pitfalls.md (PIT-010, PIT-019)
- ai_rules/decisions.md (DEC-002)

## Per-item decisions

1. **PITFALL — empty-body 403 caused by unhandled DataIntegrityViolationException (orphan promotion_cours)**
   - Decision: ACCEPT as new entry **PIT-020**.
   - Reason: distinct from PIT-010 (PIT-010 documents that ddl-auto=update renames leave orphan tables; PIT-020 documents the *symptom* — a misleading empty-body 403 — caused by such an orphan table's FK still being live). Both entries cross-reference each other.

2. **CONVENTION — manual cascade-delete pattern for required @ManyToOne FKs without JPA cascade**
   - Decision: ACCEPT as new entry **CONV-007**.
   - Reason: distinct from DEC-002 (DEC-002 covers cross-aggregate reference-nulling via sibling-service injection; CONV-007 covers deep multi-level deletion chains for owned/required FK dependents). Cross-referenced both ways (CONV-007 counter-indication points to DEC-002).

## Files touched
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\pitfalls.md (added PIT-020)
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\conventions.md (added CONV-007)
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\INDEX.md (2 new lines)

## INDEX diff summary
Added:
- `PITFALL | PIT-020 | Empty-body 403 on delete may be unhandled DataIntegrityViolationException (orphan promotion_cours) | pitfalls.md#pit-020 | 2026-06-11`
- `CONVENTION | CONV-007 | Manual cascade-delete pattern for required @ManyToOne FKs without JPA cascade | conventions.md#conv-007 | 2026-06-11`

## Next Actions for the manager
None — no REPO_STATE.md fields need updating for this triage.
