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

last_toa_run: 2026-06-02

stack_spec_hash: ~

active_work_items:
  - WI-20260602-APIAUT-001 (DONE)
  - WI-20260602-APIAUT-002 (DONE)

blocking_questions: ~
