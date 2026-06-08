# Work Item WI-20260608-BACKEN-006

| Field | Value |
|-------|-------|
| Work Item | WI-20260608-BACKEN-006 |
| Role | developer |
| Status | DONE |
| Scope | Postman collection + environnement dans docs/postman/ |

## Files Touched
- `docs/postman/gestion-formation.environment.json` — created
- `docs/postman/gestion-formation.collection.json` — created

## Evidence
Files created and verified in docs/postman/ directory. Both are valid Postman JSON (Collection v2.1 format, environment format with _postman_variable_scope).

## Decisions
- Collection format: Postman Collection v2.1.0 (most widely supported)
- Environment type for token: `secret` (hides value in Postman UI)
- Auto-save scripts on POST requests: login → token, create user → userId, create filiere → filiereId, create cursus → cursusId, create cours → coursId
- GET filiere endpoints left without Authorization header (public endpoints per spec)
- GET cursus/cours endpoints include Authorization header (authenticated per spec)
- register-invitation left without Authorization header (public endpoint per spec)
- Body values use realistic examples: admin@eni.fr, Admin1234!, ENI domain emails

## Open Blockers
None

## Next Actions
None — import both files into Postman: first the environment, then the collection.

## Recall Hints
- docs/postman/ directory created from scratch (did not exist)
- Variable auto-capture relies on res.id for filiere/cursus/cours and res.uid for users
