# Work Item
WI-20260605-WRAPPE-001

# Role
rules-curator

# Mode
triage

# Inputs received
- role_note: ai_memory/2026-06-05__ROLE-developer__WI-20260605-WRAPPE-001.md
- ai_rules/INDEX.md: did not exist (created)
- ai_rules/pitfalls.md: did not exist (created)

# Per-item decisions

## PITFALL 1 — Lucide Angular: imports[] vs data usage
Decision: ACCEPT
Reason: No existing entry covered this. The rule is precise, actionable, backed by a real build failure (NG8113). Wording tightened to include the counter-indication (icons used both as data and as directives must stay in imports[]).
Assigned ID: PIT-001

## PITFALL 2 — Angular SCSS: root-level selectors inside @media blocks
Decision: ACCEPT
Reason: No existing entry covered this. The rule explains the Angular view-encapsulation mechanism precisely and prevents a class of silent CSS bugs. Counter-indication added for global stylesheets.
Assigned ID: PIT-002

# Files touched (ai_rules/ only)
- ai_rules/INDEX.md (created)
- ai_rules/pitfalls.md (created)

# INDEX diff summary
Two new lines added:
  PITFALL | PIT-001 | Lucide Angular: imports[] vs data usage         | pitfalls.md#pit-001 | 2026-06-05
  PITFALL | PIT-002 | Angular SCSS: root-level selectors inside @media | pitfalls.md#pit-002 | 2026-06-05

# Next Actions for the manager
None required. No REPO_STATE.md migration was performed (mode is triage, not migrate). No source fields to delete.
