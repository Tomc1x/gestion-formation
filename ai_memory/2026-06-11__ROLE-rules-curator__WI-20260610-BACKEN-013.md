# Memory Note — WI-20260610-BACKEN-013 (rules-curator triage)

**Role:** rules-curator
**Mode:** triage
**Status:** DONE

## Inputs received
- ai_memory/2026-06-11__ROLE-developer__WI-20260610-BACKEN-012-013-014.md (Proposed Rules section)
- ai_rules/decisions.md (existing DEC-001)
- ai_rules/INDEX.md

## Per-item decisions

- Proposal: "PromotionService cascade-cleanup methods are called from sibling services, not via events" (DECISION)
  - Decision: **ACCEPT** (as new entry, not MERGE into DEC-001)
  - Reasoning: DEC-001 documents a specific *outcome* (Cours deletion silently removes CursusCours links) within a single service. The new proposal documents a distinct *architectural pattern* — cross-service direct-call cascade cleanup between unrelated entities/aggregates (PromotionService <-> CursusService). DEC-001 is an instance that implicitly follows this pattern but doesn't state it generally. Treating it as a generalization rather than a duplicate avoids overloading DEC-001's scope and gives future agents (e.g. WI-017 Rythme/Promotion work) a directly applicable rule.
  - Action: written as DEC-002 in ai_rules/decisions.md, with a "See also" cross-reference added to DEC-001 pointing to DEC-002.

## Files touched
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\decisions.md (added DEC-002, added cross-reference in DEC-001)
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\INDEX.md (added DEC-002 row)

## INDEX diff summary
- Added: `| DECISION | DEC-002 | Cross-entity cascade-cleanup via direct sibling-service calls | decisions.md#dec-002 | 2026-06-11 |`

## Next Actions for the manager
- None blocking. DEC-002 is available for WI-015/016/017 (Promotion/Rythme controller work) as the canonical reference for any further cascade-cleanup additions.
