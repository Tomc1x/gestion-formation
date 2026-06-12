# WI-20260611-FULLST-026 — rules-curator (triage)

## Work Item
WI-20260611-FULLST-026

## Role
rules-curator

## Mode
triage

## Inputs received
- ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-026.md (Proposed Rules section, 1 entry, TYPE: CONVENTION)
- ai_rules/INDEX.md
- ai_rules/conventions.md
- ai_rules/pitfalls.md (checked PIT-017, found closely related existing entry)

## Per-item decisions

1. Proposal: "Controller test pattern for WebMvcTest with security" (CONVENTION)
   - Decision: ACCEPT (as new entry CONV-008) + MERGE (cross-reference into existing PIT-017)
   - Reason: PIT-017 already documents the narrower pitfall (`@WithMockUser` fails, use `.with(user(...))`). The new proposal describes the broader, now-repeated setup pattern (`@WebMvcTest` + `@Import(SecurityConfig.class)` + `@MockitoBean` for service/JwtService/UserDetailsServiceImpl + helper), confirmed across 6 new controller test classes in this WI. Kept both entries distinct (pitfall = "what fails and why", convention = "how to write new tests") and added a bidirectional "See also" cross-reference. Bumped PIT-017 Verified date to 2026-06-11 with re-confirmation note.

## Files touched
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\conventions.md (added CONV-008)
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\pitfalls.md (added "See also" cross-ref + re-verification note to PIT-017)
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\INDEX.md (added CONV-008 line)

## INDEX diff summary
- Added: `| CONVENTION | CONV-008 | Controller test pattern for @WebMvcTest with SecurityConfig | conventions.md#conv-008 | 2026-06-11 |`
- No deprecations.

## Next Actions for the manager
None — proposal fully triaged, no REPO_STATE.md changes needed.
