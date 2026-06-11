# REPO_STATE

project_purpose: Application de gestion de formations (cours ENI) — monorepo Spring Boot 3 / Angular + PostgreSQL + Docker

run_commands:
  docker_full: "docker compose up --build"
  docker_db_only: "docker compose up db mailhog -d"
  local_backend: "./gradlew bootRun --args='--spring.profiles.active=local'"
  frontend: "cd frontend && ng serve"

test_commands:
  backend: "./gradlew test"

lint_commands: ~

last_toa_run: 2026-06-10

stack_spec_hash: ~

active_work_items:
  - WI-20260602-APIAUT-001 (DONE)
  - WI-20260602-APIAUT-002 (DONE)
  - WI-20260605-WRAPPE-001 (DONE)
  - WI-20260608-BACKEN-001 (DONE)
  - WI-20260608-BACKEN-002 (DONE)
  - WI-20260608-BACKEN-003 (DONE)
  - WI-20260608-FRONTE-002 (DONE)
  - WI-20260608-BACKEN-004 (DONE)
  - WI-20260608-BACKEN-005 (DONE)
  - WI-20260608-BACKEN-006 (DONE)
  - WI-20260610-BACKEN-001 (DONE)
  - WI-20260610-BACKEN-002 (DONE)
  - WI-20260610-BACKEN-003 (DONE)
  - WI-20260610-BACKEN-004 (DONE)
  - WI-20260610-BACKEN-005 (DONE)
  - WI-20260610-BACKEN-006 (DONE)
  - WI-20260610-BACKEN-007 (DONE)
  - WI-20260610-BACKEN-008 (DONE)
  - WI-20260610-BACKEN-009 (DONE)
  - WI-20260610-BACKEN-010 (DONE)
  - WI-20260610-BACKEN-011 (DONE)
  - WI-20260610-BACKEN-012 (DONE)
  - WI-20260610-BACKEN-013 (DONE)
  - WI-20260610-BACKEN-014 (DONE)
  - WI-20260610-BACKEN-015 (DONE)
  - WI-20260610-BACKEN-016 (DONE)
  - WI-20260610-BACKEN-017 (DONE)
  - WI-20260610-BACKEN-018 (DONE)
  - WI-20260610-BACKEN-019 (DONE)
  - WI-20260610-BACKEN-020 (DONE)
  - WI-20260610-BACKEN-021 (DONE)
  - WI-20260610-BACKEN-022 (DONE)
  - WI-20260610-BACKEN-023 (DONE)
  - WI-20260611-BACKEN-024 (DONE)
  - WI-20260611-FULLST-001 (OPEN)
  - WI-20260611-FULLST-002 (OPEN)
  - WI-20260611-FULLST-003 (CANCELLED - superseded by FULLST-005)
  - WI-20260611-FULLST-004 (CANCELLED - superseded by FULLST-005)
  - WI-20260611-FULLST-005 (OPEN)
  - WI-20260611-FULLST-006 (DONE)
  - WI-20260611-FULLST-007 (OPEN)
  - WI-20260611-FULLST-008 (OPEN)
  - WI-20260611-FULLST-009 (READY_FOR_REVIEW)
  - WI-20260611-FULLST-010 (SUPERSEDED par FULLST-012)
  - WI-20260611-FULLST-011 (READY_FOR_REVIEW)
  - WI-20260611-FULLST-012 (READY_FOR_REVIEW)
  - WI-20260611-FULLST-017 (DONE)
  - WI-20260611-FULLST-016 (READY_FOR_REVIEW, via FULLST-018)
  - WI-20260611-FULLST-018 (READY_FOR_REVIEW)
  - WI-20260611-FULLST-013 (READY_FOR_REVIEW)
  - WI-20260611-FULLST-014 (MERGED dans FULLST-012)
  - WI-20260611-FULLST-015 (PARTIELLEMENT COUVERT par FULLST-019)
  - WI-20260611-FULLST-019 (DONE)
  - WI-20260611-FULLST-020 (DONE - PUT /api/cursus/{id} et modal "Ajouter un cours" revalides via chrome-devtools)
  - WI-20260611-FULLST-021 (DONE - refonte visuelle Cursus & Filieres, maquette React fournie ; build PASS, revalide chrome-devtools)
  - WI-20260611-FULLST-022 (DONE - cursus dropdown vide, fix promotion 3 corrompue + nettoyage debris de tests)

blocking_questions:
  - "Quel ordre de priorisation pour FULLST-001/005/006/007-009 ?"
# Resolu 2026-06-11: WI-020 utilisera FullCalendar Angular (@fullcalendar/angular + core/daygrid/timegrid/interaction).

known_issues:
  - "DELETE /api/promotions/{id} retourne 403 (corps vide) pour une promotion
     qui a (ou a eu) des eleves rattaches (repro sur promotion id=4 'TEST CDA
     2'). GET/POST/PUT et add/retrait eleve fonctionnent (200). Cause non
     identifiee, voir WI-20260611-FULLST-022 section 'Bug additionnel'.
     Promotion id=4 reste presente dans /app/admin/promotions comme debris."
