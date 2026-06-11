# WI-20260611-FULLST-023 — Rules Curator Memory Note

## Work Item
WI-20260611-FULLST-023

## Role
rules-curator

## Mode
triage

## Inputs received
- ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-023.md (Proposed Rules section)

## Per-item decisions
- PITFALL "Pas de pattern centralise pour recuperer l'utilisateur authentifie courant" → ACCEPT as PIT-019. No duplicate found in pitfalls.md/INDEX.md (PIT-006 covers an unrelated roleGuard/sidebar sync issue). Documents the cast-to-`User`-via-`Authentication.getPrincipal()` pattern used to fix the IDOR on `GET /api/eleves/{id}/planning`, with a counter-indication noting it should be deprecated if a `CurrentUserService`/`@AuthenticationPrincipal` abstraction is introduced later.

## Files touched
- ai_rules/pitfalls.md (added PIT-019)
- ai_rules/INDEX.md (added PIT-019 line)

## INDEX diff summary
+1 line: `PITFALL | PIT-019 | No centralized current-user helper; cast Authentication.getPrincipal() to User for IDOR checks | pitfalls.md#pit-019 | 2026-06-11`

## Next Actions for the manager
None.
