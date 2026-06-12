# Rules Curator — WI-20260611-FULLST-032 (triage)

## Work Item
WI-20260611-FULLST-032

## Role
rules-curator

## Mode
triage

## Inputs received
- ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-032.md (Proposed Rules section)
- ai_rules/INDEX.md, ai_rules/pitfalls.md

## Per-item decisions
1. PITFALL "AuthService signals must be set explicitly in login()" — **ACCEPT** as PIT-025. Durable, generalizable rule for AuthService signal maintenance, root cause clearly documented.
2. PITFALL "ng serve/HMR serves stale singleton" — **REJECT**. This is a dev-environment/verification artifact, not a code rule; not durable enough to warrant a pitfalls entry (and overlaps conceptually with general "hard reload before testing singleton state" practice already implied by PIT-012). No entry written.
3. DECISION "logout() resets _currentRole to 'REF' instead of unauthenticated state" — **REJECT** as a rule entry (it's an explicit out-of-scope observation, not a decision or pitfall to apply going forward). Recommend manager open a follow-up WI to evaluate `FrontendRole | null` or `isAuthenticated` guard before any conditional render on `currentRole()`.

## Files touched
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\pitfalls.md (added PIT-025)
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\INDEX.md (added PIT-025 line)

## INDEX diff summary
+1 line: PITFALL | PIT-025 | AuthService derived signals must be set explicitly in login(), not only at init | pitfalls.md#pit-025 | 2026-06-11

## Next Actions for the manager
- Consider opening a new WI for logout() `_currentRole` reset to a non-authenticated state (item #3 above) — not a rule, a code-quality follow-up.
