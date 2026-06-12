# WI-20260610-BACKEN-008 — rules-curator triage

## Work Item
WI-20260610-BACKEN-008

## Role
rules-curator

## Mode
triage

## Inputs received
- ai_memory/2026-06-10__ROLE-developer__WI-20260610-BACKEN-008.md (Proposed Rules section, 2 PITFALL entries)

## Per-item decisions
1. "Angular CSS budgets measured on compiled CSS, not SCSS" — **ACCEPT**.
   Generic, durable measurement pitfall applicable to any frontend SCSS budget work.
   Written as PIT-004 in ai_rules/pitfalls.md.
2. "Modal/form pattern duplicated in 3 admin components (utilisateurs/cours/cursus)" — **ACCEPT**.
   Genuine cross-file tech debt with a concrete grep-verifiable signature and a
   direct causal link to a recurring budget problem (utilisateurs.scss residual
   overflow after angular.json bump 8kB->12kB). Worth surfacing for future WIs
   rather than re-discovering. Written as PIT-005 in ai_rules/pitfalls.md, with
   a counter-indication referencing the visual-parity / palette constraint.

## Files touched
- ai_rules/pitfalls.md (added PIT-004, PIT-005)
- ai_rules/INDEX.md (added 2 lines)

## INDEX diff summary
+ PITFALL | PIT-004 | Angular CSS budgets measured on compiled CSS, not SCSS | pitfalls.md#pit-004 | 2026-06-10
+ PITFALL | PIT-005 | Modal/form SCSS pattern duplicated across admin components | pitfalls.md#pit-005 | 2026-06-10

## Next Actions for the manager
- No REPO_STATE.md changes needed.
- Consider opening a dedicated tier simple/medium WI for extracting `_admin-modal.scss`
  shared partial across utilisateurs/cours/cursus (referenced in PIT-005).
