# Rules Curator — Triage Proposed Rules (FULLST-001..010)

## Work Item
WI-20260611-FULLST-001 to WI-20260611-FULLST-010 (batch closure)

## Role
rules-curator

## Mode
triage

## Inputs received
- ai_memory/2026-06-11__ROLE-solution-architect__WI-20260611-FULLST-001.md
- ai_memory/2026-06-11__ROLE-solution-architect__WI-20260611-FULLST-005.md
- ai_memory/2026-06-11__ROLE-solution-architect__WI-20260611-FULLST-006.md
- ai_memory/2026-06-11__ROLE-solution-architect__WI-20260611-FULLST-007.md
- ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-001.md
- ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-002.md
- ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-005.md
- ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-006.md
- ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-008.md
- ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-009.md
- ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-010.md

## Per-item decisions

1. **EntitySelectorComponent generic selector** (architect FULLST-001, dev FULLST-001 confirmation, dev FULLST-006 reuse)
   - MERGE -> new entry **CONV-004** (conventions.md). Combined the architect's design rule with the developer's confirmed-in-code contract (SelectableEntity, modes, disabledIds).

2. **Badge truncation `<details>/<summary>`** (architect FULLST-006, dev FULLST-006)
   - MERGE -> new entry **CONV-005** (conventions.md).

3. **PromotionController.toResponse warnings=[] on GET** (architect FULLST-005, confirmed dev FULLST-005, re-affirmed dev FULLST-010 "remains valid and unchanged")
   - ACCEPT -> new entry **PIT-008** (pitfalls.md). Three independent confirmations, clearly durable.

4. **CoursPlanifie decoupled from Promotion (optional link), InscriptionCours separate entity** (architect FULLST-007)
   - ACCEPT -> new entry **DEC-003** (decisions.md).

5. **InscriptionCours unique constraint (user_id, cours_planifie_id)** (architect FULLST-007)
   - ACCEPT -> new entry **PIT-009** (pitfalls.md). Cross-referenced from DEC-003.

6. **PromotionCours -> CoursPlanifie rename, naming convention** (dev FULLST-008)
   - ACCEPT, scoped as a naming-convention companion to item 4 -> new entry **CONV-006** (conventions.md), cross-referenced with DEC-003.

7. **ddl-auto=update + entity rename leaves orphan table** (dev FULLST-008)
   - ACCEPT -> new entry **PIT-010** (pitfalls.md).

8. **Concurrent agents editing same backend file without worktree isolation** (dev FULLST-001)
   - ACCEPT -> new entry **PIT-011** (pitfalls.md). Process/orchestration pitfall, durable for future multi-agent batches.

9. **DevTools "[issue]" can point to shared layout, not page under test** (dev FULLST-002)
   - ACCEPT -> new entry **PIT-012** (pitfalls.md).

10. **mon-calendrier / planning grid logic duplication** (dev FULLST-005)
    - ACCEPT -> new entry **PIT-013** (pitfalls.md).

11. **ng serve (esbuild) fails project-wide on any TS error** (dev FULLST-005)
    - ACCEPT -> new entry **PIT-014** (pitfalls.md).

12. **AuthService must rehydrate currentUser/currentUserId (incl. uid) from localStorage** (dev FULLST-009)
    - ACCEPT -> new entry **PIT-015** (pitfalls.md).

13. **admin@admin.com login response has uid: null** (dev FULLST-009)
    - REJECT. This is a seeded test-data bug report / actionable backend task, not a durable codebase rule. Recommend the manager open a small backend WI to fix `AuthResponse.uid` mapping for the seeded admin account, or seed a non-admin test account with a populated uid. Not written to ai_rules/.

14. **FULLST-010 "(none beyond what FULLST-005 already proposed re: warnings=[])"**
    - No new proposal — folded into item 3 (PIT-008) as a third confirmation source.

## Files touched (ai_rules/ paths only)
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\conventions.md (added CONV-004, CONV-005, CONV-006)
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\decisions.md (added DEC-003)
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\pitfalls.md (added PIT-008 .. PIT-015)
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\INDEX.md (added 11 new lines)

## INDEX diff summary
Added 11 new entries: CONV-004, CONV-005, CONV-006, DEC-003, PIT-008, PIT-009, PIT-010, PIT-011, PIT-012, PIT-013, PIT-014, PIT-015. No deprecations — no conflicts with existing entries detected.

## Next Actions for the manager
- Consider opening a backend WI to fix `AuthResponse.uid` returning `null` for the seeded `admin@admin.com` account (item 13, rejected from ai_rules as a code-fix task, not a rule).
- No REPO_STATE.md fields need deletion from this triage pass.
