# Rules Curator — Triage Report

Work Item: WI-20260610-BACKEN-023
Role: rules-curator
Mode: triage

## Inputs received
- ai_memory/2026-06-10__ROLE-developer__WI-20260610-BACKEN-023.md (Proposed Rules section)
- ai_rules/INDEX.md
- ai_rules/conventions.md (CONV-003)
- ai_rules/pitfalls.md

## Per-item decisions

1. PITFALL "HttpStatus 422 constant renamed in Spring Framework 7"
   - Decision: ACCEPT
   - Reason: Concrete, verifiable, and directly impacts CONV-003 (HTTP exception mapping). Written as PIT-007.
   - Cross-reference: added a "Note" pointer from CONV-003 to PIT-007 in conventions.md.

## Files touched (ai_rules/)
- ai_rules/pitfalls.md (added PIT-007)
- ai_rules/conventions.md (added cross-ref note under CONV-003)
- ai_rules/INDEX.md (added PIT-007 row)

## INDEX diff summary
- New row: `PITFALL | PIT-007 | HttpStatus constants renamed in Spring Framework 7 | pitfalls.md#pit-007 | Verified: 2026-06-10`
- No deprecations.

## Next Actions for the manager
- None outstanding for ai_memory ledgers from this triage.
