# WI-20260610-BACKEN-022

**Work Item**: WI-20260610-BACKEN-022
**Role**: developer
**Status**: DONE

## Scope
Frontend uniquement — masquer dans la sidebar les liens vers des routes que le rôle courant ne peut pas activer, en se basant sur les contraintes définies dans `app.routes.ts` (roleGuard).

## Files Touched
- `frontend/src/app/layouts/main-layout/sidebar/sidebar.ts`
- `frontend/src/app/layouts/main-layout/sidebar/sidebar.html`

## Evidence
- `npx tsc --noEmit -p tsconfig.app.json` → exit 0, aucune sortie.
- `npx ng build` → succès, "Application bundle generation complete." Seuls warnings pré-existants (budget SCSS sur `utilisateurs.scss` et `register.scss`, sans rapport avec ce WI).

## Decisions
- Ajout d'un champ optionnel `roles?: Role[]` sur chaque entrée du tableau `routes`, reflétant exactement les `roleGuard([...])` de `app.routes.ts` :
  - `admin/utilisateurs` → `['ADMIN']`
  - `admin/cours` → `['REF']`
  - `admin/cursus` → `['REF']`
  - `dashboard`, `promotions`, `calendrier` → pas de champ `roles` (visibles pour tout rôle connecté).
- Ajout d'un `computed()` `visibleRoutes` qui filtre `routes` : visible si `roles` absent OU `roles.includes(authService.currentRole())`.
- `AuthService` injecté via `inject()`, `Role` importé depuis `auth.constants`.
- `sidebar.html` itère désormais sur `visibleRoutes()` au lieu de `routes`.
- Aucun commentaire ajouté, style signals/computed/inject existant respecté.

## Open Blockers
Aucun.

## Next Actions
Aucune action de suite nécessaire pour ce WI. Si de nouvelles routes protégées par rôle sont ajoutées dans `app.routes.ts`, penser à synchroniser le champ `roles` correspondant dans `sidebar.ts`.

## Recall Hints
sidebar, roleGuard, visibleRoutes, navigation par rôle, admin/utilisateurs, admin/cours, admin/cursus.

## Proposed Rules
- TYPE: PITFALL
  Title: Synchronisation manuelle entre app.routes.ts (roleGuard) et sidebar.ts (roles)
  Scope: frontend/src/app/layouts/main-layout/sidebar, frontend/src/app/app.routes.ts
  Rule: Toute modification d'un `roleGuard([...])` sur une route doit être répercutée manuellement dans le champ `roles` correspondant de `sidebar.ts`.
  Why: Les deux structures sont indépendantes ; une désynchronisation afficherait un lien inaccessible (ou masquerait un lien accessible) sans erreur de compilation.
  How to apply: Lors d'une revue touchant `app.routes.ts`, vérifier `sidebar.ts` en parallèle.
  Evidence: WI-20260610-BACKEN-022, frontend/src/app/layouts/main-layout/sidebar/sidebar.ts
