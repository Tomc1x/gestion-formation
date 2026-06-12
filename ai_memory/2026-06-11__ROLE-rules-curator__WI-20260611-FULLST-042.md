# WI-20260611-FULLST-042 — Rules Curator Triage Note

## Work Item
WI-20260611-FULLST-042 (plus cross-check of WI-20260611-FULLST-039)

## Role
rules-curator

## Mode
triage

## Inputs received
- ai_memory/2026-06-11__ROLE-solution-architect__WI-20260611-FULLST-042.md (Proposed Rules: CONVENTION)
- ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-042.md (Proposed Rules: PITFALL)
- ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-039.md (cross-check for bootRun hot-reload proposal)
- ai_rules/INDEX.md, ai_rules/pitfalls.md, ai_rules/conventions.md

## Per-item decisions

1. **ACCEPT** — CONVENTION "Pattern warning non bloquant" (from solution-architect note, lines 129-137).
   Written as **CONV-011** in `ai_rules/conventions.md` ("Non-blocking warning" pattern for soft business
   rules — forcer flag + warnings list). Title/scope/wording lightly tightened (added explicit
   counter-indication referencing PIT-009 for hard rules that must never be bypassable).

2. **ACCEPT** — PITFALL "Seed eleve with promotion+cursus makes hors-ordre validation untestable via UI"
   (from developer note, lines 158-171). Written as **PIT-026** in `ai_rules/pitfalls.md`. No conflicts with
   existing entries.

3. **NO ACTION NEEDED (already covered)** — bootRun hot-reload / SecurityConfig staleness proposal referenced
   from WI-20260611-FULLST-039. On inspection, the WI-039 memory note (full read) contains NO "Proposed Rules"
   section and no explicit bootRun-hot-reload rule text — only a passing mention of `gradlew bootRun
   --spring.profiles.active=local` in the scope line. The substantive pitfall ("verify the running backend
   process is up to date before diagnosing a SecurityConfig bug / stale jar vs source mismatch") is already
   captured as **PIT-018** (origin WI-20260611-FULLST-019, re-confirmed pattern previously seen in BACKEN-024),
   already present in `ai_rules/pitfalls.md` and `ai_rules/INDEX.md` before this session. The WI-042 developer
   note's own "Backend dev server restart" section (mid-session bootRun restart needed because the running
   process served stale bytecode without the new `warnings` field) is consistent with PIT-018 and does not
   require a new/separate entry. No write performed for this item.

## Files touched
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\conventions.md (added CONV-011)
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\pitfalls.md (added PIT-026)
- C:\Users\user\IdeaProjects\gestion-formation\ai_rules\INDEX.md (added 2 lines: CONV-011, PIT-026)

## INDEX diff summary
Added two rows after PIT-025:
- CONVENTION | CONV-011 | "Non-blocking warning" pattern for soft business rules (forcer + warnings) | conventions.md#conv-011 | 2026-06-11
- PITFALL | PIT-026 | Seed eleve with promotion+cursus makes hors-ordre validation untestable via UI | pitfalls.md#pit-026 | 2026-06-11

## Next Actions for the manager
None — no REPO_STATE.md fields to remove (triage mode, not migration). Item 3 confirmed as a duplicate of
existing PIT-018; no further action required.
