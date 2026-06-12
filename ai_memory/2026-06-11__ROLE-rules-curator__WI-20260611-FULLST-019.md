# WI-20260611-FULLST-019 — rules-curator memory note

## Work Item
WI-20260611-FULLST-019

## Role
rules-curator

## Mode
triage

## Inputs received
- ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-019.md (Proposed Rules section)
- ai_rules/INDEX.md (checked for existing duplicate of "stale backend process" pattern from BACKEN-024 — none found; PIT-011 covers a different topic, concurrent agent edits)

## Per-item decisions
1. PITFALL "@WithMockUser peu fiable avec SecurityConfig + AuthenticationProvider custom"
   -> ACCEPT. New entry PIT-017 in ai_rules/pitfalls.md. No existing entry covered this.

2. PITFALL "Verifier la fraicheur du process backend avant de diagnostiquer un bug de SecurityConfig"
   -> ACCEPT. New entry PIT-018 in ai_rules/pitfalls.md. Searched ai_rules/pitfalls.md for
   "BACKEN-024", "stale", "perime", "rebuild", "restart" — only PIT-011 matched ("stale read"
   in concurrent-agent context), which is a different pattern (concurrent edits, not stale
   running process). No duplicate found, so no merge — created as new entry, but referenced
   BACKEN-024 in the "Why" section as prior occurrence.

## Files touched
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\pitfalls.md (added PIT-017, PIT-018)
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\INDEX.md (added 2 lines)

## INDEX diff summary
+ PITFALL | PIT-017 | @WithMockUser unreliable with @WebMvcTest + SecurityConfig on this project | pitfalls.md#pit-017 | 2026-06-11
+ PITFALL | PIT-018 | Verify backend process freshness before diagnosing SecurityConfig bug | pitfalls.md#pit-018 | 2026-06-11

## Next Actions for the manager
None — both proposals accepted as new entries, no REPO_STATE.md or ai_memory ledger changes needed from this pass.
