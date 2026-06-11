# 2026-06-10 — rules-curator — GLOBAL (triage)

## Work Item
GLOBAL (triage of two closed WIs)

## Role
rules-curator

## Mode
triage

## Inputs received
- ai_memory/2026-06-10__ROLE-developer__WI-20260610-BACKEN-003.md (Proposed Rules: DECISION)
- ai_memory/2026-06-10__ROLE-developer__WI-20260610-BACKEN-002.md (Proposed Rules: CONVENTION)
- Verified against: backend/src/main/java/fr/eni/gestionformation/service/CoursService.java, frontend/src/styles/_variables.scss, frontend/src/styles.scss

## Per-item decisions

1. WI-20260610-BACKEN-003 — DECISION "Suppression de cours catalogue avec liaisons existantes"
   - Decision: ACCEPT
   - Reason: Verified `CoursService.deleteById` (lines 38-45) matches the proposal exactly — `cursusCoursRepository.deleteAll(findByCoursId(id))` then `coursRepository.deleteById(id)`. No existing entry conflicts.
   - Written as: ai_rules/decisions.md#dec-001

2. WI-20260610-BACKEN-002 — CONVENTION "Source de vérité unique pour les design tokens"
   - Decision: ACCEPT
   - Reason: Verified `_variables.scss` defines `$e1/$e2/$e3` (lines 32-34) and `styles.scss` `:root` exposes `--e1/--e2/--e3` via `#{$e1}` etc. (lines 51-53), confirming the pattern described. No existing entry conflicts.
   - Written as: ai_rules/conventions.md#conv-002

## Files touched (ai_rules/ only)
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\decisions.md (created, DEC-001)
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\conventions.md (added CONV-002)
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\INDEX.md (added 2 lines: CONV-002, DEC-001)

## INDEX diff summary
+ CONVENTION | CONV-002 | Single source of truth for design tokens (SCSS+CSS var) | conventions.md#conv-002 | Verified: 2026-06-10
+ DECISION | DEC-001 | Catalogue Cours deletion silently removes CursusCours links | decisions.md#dec-001 | Verified: 2026-06-10

## Next Actions for the manager
- None for REPO_STATE.md (no fields to delete in this triage).
- Both WIs can be marked fully closed; their Proposed Rules sections have been actioned.
