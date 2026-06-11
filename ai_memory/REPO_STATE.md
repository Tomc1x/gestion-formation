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
  - WI-20260610-BACKEN-012 (OPEN)
  - WI-20260610-BACKEN-013 (OPEN)
  - WI-20260610-BACKEN-014 (OPEN)
  - WI-20260610-BACKEN-015 (OPEN)
  - WI-20260610-BACKEN-016 (OPEN)
  - WI-20260610-BACKEN-017 (OPEN)
  - WI-20260610-BACKEN-018 (OPEN)
  - WI-20260610-BACKEN-019 (OPEN)
  - WI-20260610-BACKEN-020 (OPEN)
  - WI-20260610-BACKEN-021 (OPEN)
  - WI-20260610-BACKEN-022 (DONE)
  - WI-20260610-BACKEN-023 (DONE)

blocking_questions:
  - "WI-20260610-BACKEN-020: choix de la librairie de calendrier drag-and-drop (FullCalendar Angular vs autre) — a trancher avant developpement."
