# Rules Curator — WI-20260610-BACKEN-001

- Work Item: WI-20260610-BACKEN-001
- Role: rules-curator
- Mode: triage

## Inputs received
- ai_memory/2026-06-10__ROLE-developer__WI-20260610-BACKEN-001.md (Proposed Rules section)
- ai_rules/INDEX.md, ai_rules/pitfalls.md (existing topic files checked, no conflicting CONV entry)
- Verified frontend/src/app/core/adapters/ contains calendar.adapter.ts, calendar-mock.ts,
  calendar-api.adapter.ts, user-admin.adapter.ts, user-admin-http.adapter.ts, user-admin-mock.ts

## Per-item decisions
- Proposal "Adapter pattern for feature data access (frontend)": **ACCEPT (with minor amendment)**.
  Pattern confirmed already established for calendar and replicated for user-admin.
  Amended wording to acknowledge two naming variants observed for the real-backend
  implementation (`Http<Feature>Adapter` vs `<Feature>ApiAdapter`) instead of prescribing
  only `Http<Feature>Adapter`, since calendar uses `CalendarApiAdapter`.
  New entry created: CONV-001.

## Files touched
- ai_rules/conventions.md (created, new entry CONV-001)
- ai_rules/INDEX.md (added CONV-001 row)

## INDEX diff summary
Added one row: `CONVENTION | CONV-001 | Adapter pattern for feature data access (frontend) | conventions.md#conv-001 | 2026-06-10`

## Next Actions for the manager
None.
