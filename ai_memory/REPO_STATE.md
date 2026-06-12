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
  - WI-20260611-FULLST-023 (DONE - restriction routes/acces par role API+front+sidebar, fix IDOR planning eleve)
  - WI-20260611-FULLST-024 (DONE - suppression cascade cours planifie/promotion/cours catalogue, fix 403 promotion 4)
  - WI-20260611-FULLST-025 (OPEN - audit table orpheline promotion_cours)
  - WI-20260611-FULLST-026 (DONE - 8 fichiers de test crees, ./gradlew test PASS)
  - WI-20260611-FULLST-027 (DONE - page detail cursus admin/cursus/:id + alertes prerequis mal ordonnes, ng build PASS)
  - WI-20260611-FULLST-028 (DONE - cartes cursus liste simplifiees, badge alerte + navigation vers detail, ng build PASS)
  - WI-20260611-FULLST-029 (DONE - cursus DWWM verifie, 14 alertes "prereq absent du cursus" = probleme catalogue, pas d'ordre a corriger)
  - WI-20260611-FULLST-030 (DONE - dockerisation backend+frontend, images buildees et testees OK)
  - WI-20260611-FULLST-031 (DONE - prerequis cours id 10 "Web Client / HTML & CSS" vide, resout 14 alertes DWWM sans casser CDA, PIT-023/024)
  - WI-20260611-FULLST-032 (DONE - fix AuthService._currentRole non mis a jour au login -> sidebar/roleGuard restaient sur role figé, PIT-025)
  - WI-20260611-FULLST-033 (DONE - endpoint GET /api/formateurs cree, securise REF+ADMIN, cours-planifies-tab.ts bascule sur ce nouvel adapter)
  - WI-20260611-FULLST-034 (DONE - root cause = @keyframes fadeUp transform:translateY(0) + forwards cree containing block sur .page, casse position:fixed des modales ; fix 1 ligne dans styles.scss)
  - WI-20260611-FULLST-035 (DONE - AuthService.isAuthenticated derive maintenant de l'expiration JWT (isTokenExpired), nouveau guestGuard sur route /login)
  - WI-20260611-FULLST-036 (DONE - main { min-height:0; height:0; overflow-y:auto } dans main-layout.scss, scroll confine au router-outlet, sidebar/header fixes, build OK)
  - WI-20260611-FULLST-037 (DONE - endpoint GET /api/eleves cree, securise REF+ADMIN, stagiaires-tab.ts bascule sur ce nouvel adapter)
  - WI-20260611-FULLST-038 (DONE - aucun fix necessaire, positionnement modale deja correct apres FULLST-034, verifie chrome-devtools)
  - WI-20260611-FULLST-039 (DONE - campagne verification fonctionnelle par role : scenarios 1-6 PASS, scenario 7/8/10 FAIL (planning formateur, formateur par cours, inscription a l'unite+forcer absentes), scenario 9 BLOCKED (code OK, pas d'UI). Side-effect: mdp formateur1@eni.fr=Formateur123, eleve1@eni.fr=Eleve123)
  - WI-20260611-FULLST-040 (DONE - endpoint GET /api/formateurs/{id}/planning + adapter calendrier role-aware FORMATEUR + panneau detail avec liste eleves par cours)
  - WI-20260611-FULLST-041 (DONE - PlanningEleveResponse.formateurId/formateurNom, affiche dans le panneau detail calendrier ELEVE)
  - WI-20260611-FULLST-042 (DONE - inscription a l'unite + option forcer hors-ordre cursus implementee back+front, gradle test + ng build OK, scenario 9 non-regression et happy-path 201 confirmes via chrome-devtools ; hors-ordre 409/forcer couverts par 3 nouveaux tests unitaires mockes, non rejoues en live par manque de donnees seed)

blocking_questions:
  - "Quel ordre de priorisation pour FULLST-001/005/006/007-009 ?"
# Resolu 2026-06-11: WI-020 utilisera FullCalendar Angular (@fullcalendar/angular + core/daygrid/timegrid/interaction).

known_issues: []
# Note 2026-06-11 (WI-20260611-FULLST-040/041) : pour la verification visuelle, eleve1 (uid=11)
# a ete ajoute a la promotion 12 "TEST CDA 2" et formateur1 (uid=10) a ete assigne au
# cours-planifie id=29 (Algorithmique / Pseudo-Code, promotion 12). Etat laisse en place
# volontairement (jeu de donnees demontrable) - voir Decisions de WI-20260611-FULLST-041
# pour revert si necessaire.
# Resolu 2026-06-11 (WI-20260611-FULLST-024) : DELETE /api/promotions/{id} 403 vide
# -> cause = lignes orphelines dans la table promotion_cours (residu PIT-010) avec
# FK active vers promotion(id=4). Lignes supprimees, promotion 4 ("TEST CDA 2")
# definitivement supprimee. Voir PIT-020. Audit des autres promotions -> WI-20260611-FULLST-025.
