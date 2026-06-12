# ai_rules INDEX

All durable rules for the gestion-formation project.
Deprecated entries are prefixed with `# DEPRECATED`.

| TOPIC    | ID      | Short title                                          | File & anchor                    | Verified   |
|----------|---------|------------------------------------------------------|----------------------------------|------------|
| PITFALL  | PIT-001 | Lucide Angular: imports[] vs data usage              | pitfalls.md#pit-001              | 2026-06-05 |
| PITFALL  | PIT-002 | Angular SCSS: root-level selectors inside @media     | pitfalls.md#pit-002              | 2026-06-05 |
| CONVENTION | CONV-001 | Adapter pattern for feature data access (frontend) | conventions.md#conv-001        | 2026-06-10 |
| CONVENTION | CONV-002 | Single source of truth for design tokens (SCSS+CSS var) | conventions.md#conv-002    | 2026-06-10 |
| DECISION | DEC-001 | Catalogue Cours deletion silently removes CursusCours links | decisions.md#dec-001     | 2026-06-10 |
| DECISION | DEC-002 | Cross-entity cascade-cleanup via direct sibling-service calls | decisions.md#dec-002    | 2026-06-11 |
| CONVENTION | CONV-003 | HTTP mapping of business exceptions in GlobalExceptionHandler | conventions.md#conv-003 | 2026-06-10 |
| PITFALL  | PIT-003 | Filière API endpoint is singular `/api/filiere`     | pitfalls.md#pit-003              | 2026-06-10 |
| PITFALL  | PIT-004 | Angular CSS budgets measured on compiled CSS, not SCSS | pitfalls.md#pit-004           | 2026-06-10 |
| PITFALL  | PIT-005 | Modal/form SCSS pattern duplicated across admin components | pitfalls.md#pit-005      | 2026-06-10 |
| PITFALL  | PIT-006 | Manual sync between roleGuard routes and sidebar.ts roles | pitfalls.md#pit-006       | 2026-06-10 |
| PITFALL  | PIT-007 | HttpStatus constants renamed in Spring Framework 7  | pitfalls.md#pit-007              | 2026-06-10 |
| CONVENTION | CONV-004 | EntitySelectorComponent for entity multi-selection (search + pagination) | conventions.md#conv-004 | 2026-06-11 |
| CONVENTION | CONV-005 | Badge-list truncation in admin tables via `<details>/<summary>` | conventions.md#conv-005 | 2026-06-11 |
| CONVENTION | CONV-006 | CoursPlanifie naming for planning pivot entity (formerly PromotionCours) | conventions.md#conv-006 | 2026-06-11 |
| DECISION | DEC-003 | CoursPlanifie decoupled from Promotion (optional link) | decisions.md#dec-003 | 2026-06-11 |
| PITFALL  | PIT-008 | PromotionController.toResponse always returns warnings=[] on GET | pitfalls.md#pit-008 | 2026-06-11 |
| PITFALL  | PIT-009 | InscriptionCours must have unique constraint (user_id, cours_planifie_id) | pitfalls.md#pit-009 | 2026-06-11 |
| PITFALL  | PIT-010 | ddl-auto=update + entity rename leaves orphan table | pitfalls.md#pit-010 | 2026-06-11 |
| PITFALL  | PIT-011 | Concurrent agents editing same backend file without worktree isolation | pitfalls.md#pit-011 | 2026-06-11 |
| PITFALL  | PIT-012 | DevTools issue can point to shared layout, not page under test | pitfalls.md#pit-012 | 2026-06-11 |
| PITFALL  | PIT-013 | Calendar grid logic duplicated between mon-calendrier and planning | pitfalls.md#pit-013 | 2026-06-11 |
| PITFALL  | PIT-014 | ng serve (esbuild) fails project-wide on any TS error | pitfalls.md#pit-014 | 2026-06-11 |
| PITFALL  | PIT-015 | AuthService must rehydrate currentUser/currentUserId (incl. uid) from localStorage | pitfalls.md#pit-015 | 2026-06-11 |
| PITFALL  | PIT-016 | Frontend models lack Filiere.couleur/Promotion.statut/dateFin assumed by design refs | pitfalls.md#pit-016 | 2026-06-11 |
| PITFALL  | PIT-017 | @WithMockUser unreliable with @WebMvcTest + SecurityConfig on this project | pitfalls.md#pit-017 | 2026-06-11 |
| PITFALL  | PIT-018 | Verify backend process freshness before diagnosing SecurityConfig bug | pitfalls.md#pit-018 | 2026-06-11 |
| PITFALL  | PIT-019 | No centralized current-user helper; cast Authentication.getPrincipal() to User for IDOR checks | pitfalls.md#pit-019 | 2026-06-11 |
| PITFALL  | PIT-020 | Empty-body 403 on delete may be unhandled DataIntegrityViolationException (orphan promotion_cours) | pitfalls.md#pit-020 | 2026-06-11 |
| CONVENTION | CONV-007 | Manual cascade-delete pattern for required @ManyToOne FKs without JPA cascade | conventions.md#conv-007 | 2026-06-11 |
| CONVENTION | CONV-008 | Controller test pattern for @WebMvcTest with SecurityConfig | conventions.md#conv-008 | 2026-06-11 |
| CONVENTION | CONV-009 | Shared calculation logic in Angular goes to core/utils/*.util.ts as pure TS | conventions.md#conv-009 | 2026-06-11 |
| PITFALL  | PIT-021 | backend/build.gradle pins Java 25, not Java 21 | pitfalls.md#pit-021 | 2026-06-11 |
| PITFALL  | PIT-022 | frontend/package-lock.json desynced from package.json breaks `npm ci` | pitfalls.md#pit-022 | 2026-06-11 |
| CONVENTION | CONV-010 | Escape `$` as `$$` in `.env` values for docker-compose | conventions.md#conv-010 | 2026-06-11 |
| PITFALL  | PIT-023 | Catalogue cours id 26 is a cross-cursus junction point (DWWM/CDA) | pitfalls.md#pit-023 | 2026-06-11 |
| PITFALL  | PIT-024 | PUT /api/cours/{id} silently ignores `prerequisIds` | pitfalls.md#pit-024 | 2026-06-11 |
