# WI-20260611-FULLST-023 — Developer Memory Note

## Work Item
WI-20260611-FULLST-023 — Hardening matrice de roles (backend SecurityConfig, IDOR planning eleve, frontend routes/sidebar)

## Role
developer

## Status
DONE

## Scope
1. backend/src/main/java/fr/eni/gestionformation/security/SecurityConfig.java — durcissement des regles GET sur filiere/cursus/cours/promotions et restriction de GET /api/admin/users a ADMINISTRATEUR seul.
2. backend/src/main/java/fr/eni/gestionformation/controller/InscriptionCoursController.java — correction IDOR sur GET /api/eleves/{id}/planning.
3. frontend/src/app/app.routes.ts — ajout de roleGuard sur /app/calendrier (FORMATEUR, ELEVE) et /app/promotions + /app/promotions/:id (REF).
4. frontend/src/app/layouts/main-layout/sidebar/sidebar.ts — roles ajoutes pour dashboard (ADMIN) et calendrier (FORMATEUR, ELEVE), suppression entree top-level "Promotions".

## Files Touched
- backend/src/main/java/fr/eni/gestionformation/security/SecurityConfig.java
- backend/src/main/java/fr/eni/gestionformation/controller/InscriptionCoursController.java
- frontend/src/app/app.routes.ts
- frontend/src/app/layouts/main-layout/sidebar/sidebar.ts

## Evidence
- `cd backend && .\gradlew compileJava -q` : succes (warnings Lombok/Unsafe non bloquants, pas d'erreur de compilation).
- `cd frontend && npx ng build` : "Application bundle generation complete." — PASS. Seuls des warnings de budget SCSS preexistants (utilisateurs.scss, promotions.scss, cursus.scss, register.scss) — non lies a ce WI.
- Verification chrome-devtools NON effectuee (non bloquant, marquee optionnelle dans la consigne) — a faire en revue si necessaire.

## Decisions
- Pattern d'identification de l'utilisateur courant (point 2) : aucun pattern existant `@AuthenticationPrincipal`/`CurrentUserService` trouve dans le codebase (grep sur SecurityContextHolder/Authentication/getUid/CurrentUser ne retourne que JwtAuthenticationFilter et des faux positifs d'imports).
  - `UserDetailsServiceImpl.loadUserByUsername` retourne directement l'entite `User` (qui implemente `UserDetails`), donc `Authentication.getPrincipal()` est une instance de `User` exposant `getUid()`.
  - Implementation choisie : injection du parametre `Authentication authentication` dans `getPlanningEleve`, cast du principal en `User`, comparaison `currentUser.getUid().equals(id)`. Si mismatch ou cast invalide -> `throw new AccessDeniedException(...)`.
  - Pas de handler dedie dans GlobalExceptionHandler pour AccessDeniedException — le comportement par defaut de Spring Security (ExceptionTranslationFilter) traduit cette exception en 403 Forbidden, ce qui correspond a l'attendu.
- SecurityConfig : seules les regles listees dans la consigne ont ete modifiees ; les regles non-GET (hasRole REFERENTE_ADMINISTRATIVE) sur cursus/cours/promotions/filiere, ainsi que cours-planifies/** et eleves/*/planning, restent inchangees.
- Frontend routes : /app/dashboard volontairement laisse sans guard (route de repli du roleGuard, decision deja validee dans la consigne).
- Sidebar : import `LucideUsers` conserve car toujours utilise par /app/admin/utilisateurs et /app/admin/promotions.

## Open Blockers
Aucun.

## Next Actions
- (Optionnel) Verification manuelle chrome-devtools avec les 4 roles (ADMIN, REF, ELEVE, FORMATEUR) pour confirmer l'affichage de la sidebar et les redirections sur acces direct par URL a une route interdite.
- Tests backend cibles (ex: test d'integration sur GET /api/eleves/{id}/planning avec un autre eleve -> 403) pourraient etre ajoutes dans un futur WI de couverture de tests.

## Recall Hints
- "SecurityConfig GET filiere cursus cours promotions admin/users roles"
- "IDOR planning eleve InscriptionCoursController AccessDeniedException"
- "roleGuard calendrier promotions app.routes.ts"
- "sidebar.ts suppression entree Promotions top-level"

## Proposed Rules
- TYPE: PITFALL
  Title: Pas de pattern centralise pour recuperer l'utilisateur authentifie courant
  Scope: backend/src/main/java/fr/eni/gestionformation/controller/**
  Rule: En l'absence de service utilitaire (CurrentUserService), recuperer l'utilisateur courant via le parametre `Authentication authentication` injecte par Spring puis caster `authentication.getPrincipal()` en `fr.eni.gestionformation.entity.User` (qui implemente UserDetails et expose `getUid()`).
  Why: Plusieurs endpoints (ex: GET /api/eleves/{id}/planning) doivent verifier que l'utilisateur authentifie correspond a la ressource demandee (protection IDOR), et il n'existait aucun pattern documente avant ce WI.
  How to apply: Ajouter `Authentication authentication` comme parametre de la methode de controller, faire `if (!(authentication.getPrincipal() instanceof User currentUser) || !currentUser.getUid().equals(id)) { throw new AccessDeniedException(...); }`.
  Evidence: backend/src/main/java/fr/eni/gestionformation/controller/InscriptionCoursController.java (methode getPlanningEleve), WI-20260611-FULLST-023
