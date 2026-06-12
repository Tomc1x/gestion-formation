# WI-20260610-BACKEN-006 — Page "Catalogue de cours" (frontend)

## Work Item
WI-20260610-BACKEN-006 (tier complex, branche feature/backend/admin-user)

## Role
developer

## Status
DONE

## Scope
Créer la page Angular "Catalogue de cours" (CRUD + gestion formateurs/prérequis) avec
adapter pattern (CONV-001), réutilisant le design system global (WI-20260610-BACKEN-002).

## Files Touched
- `frontend/src/app/core/models/cours.model.ts` (nouveau) : `FormateurInfo`, `Cours`,
  `CreateCoursRequest`.
- `frontend/src/app/core/adapters/cours.adapter.ts` (nouveau) : `BaseCoursAdapter` abstrait
  — `getAll`, `create`, `delete`, `setFormateurs`, `setPrerequis`.
- `frontend/src/app/core/adapters/cours-http.adapter.ts` (nouveau) : `HttpCoursAdapter`
  (`@Injectable({providedIn:'root'})`), appelle `/api/cours/...` selon le contrat WI-005.
- `frontend/src/app/core/adapters/cours-mock.ts` (nouveau) : `MockCoursAdapter`, 6 cours
  réalistes (TypeScript Bases, Angular Avancé, HTML/CSS Fondamentaux, Angular Bases,
  Spring Boot Avancé, Java Bases) avec relations prérequis non cycliques. `setPrerequis`
  reproduit la validation anti-cycle (rejette via `throwError({status:422, error:'...'})`
  si ajouter un prérequis créerait un cycle).
- `frontend/src/app/app.config.ts` : ajout provider
  `{provide: BaseCoursAdapter, useClass: HttpCoursAdapter}`.
- `frontend/src/app/features/administration/cours/cours.ts` (nouveau) : composant
  standalone, OnPush, signals (`coursList`, `formateurs`, `loading`, `loadError`,
  `submitting`, `formError`, modales création/édition/suppression), `computed()`
  `requiredByMap` pour les badges "Requis par", détection de cycle côté front pour griser
  les checkboxes prérequis dans la modale d'édition.
- `frontend/src/app/features/administration/cours/cours.html` (nouveau) : template externe
  — table `.tbl`, badges `.pill`/`.badge`, modales `.card`/`.modal` (création, édition avec
  checkboxes formateurs/prérequis, suppression avec avertissement "Requis par").
- `frontend/src/app/features/administration/cours/cours.scss` (nouveau) : mise en page
  spécifique uniquement (grilles, badges colorés, modales, checkbox-list) — réutilise
  `.btn`, `.card`, `.field`, `.input`, `table.tbl`, `.pill`, `.badge`, `.anim-up`/`.anim-in`.
- `frontend/src/app/app.routes.ts` : route `admin/cours` (titre "Catalogue de cours"),
  `loadComponent` lazy vers `CoursComponent`.
- `frontend/src/app/layouts/main-layout/sidebar/sidebar.ts` : entrée sidebar
  `/app/admin/cours` "Catalogue de cours", icône `LucideBookOpen` (importée pour le typage
  `LucideIconInput` du tableau `routes`, conformément au pattern existant pour
  `LucideUsers`/`LucideCalendar` — pas ajoutée au tableau `imports` du composant car
  `LucideDynamicIcon` rend l'icône dynamiquement).

## Evidence
- `cd frontend && npx tsc --noEmit -p tsconfig.app.json` → exit 0, aucune sortie.
- `npx ng build` → bundle `cours` généré (chunk-OE4HKJ3D.js, 19.35 kB / 4.63 kB transfer),
  aucune nouvelle erreur/warning. Le seul échec de build (`ERROR ... utilisateurs.scss
  exceeded maximum budget`) est pré-existant : vérifié via `git stash` + `npx ng build` sur
  l'état avant ce WI → même erreur identique, donc hors scope (confirmé par l'énoncé du WI).

## Decisions
1. **Liste des formateurs** : récupérée via `BaseUserAdminAdapter.getAll()` (déjà fourni par
   WI précédent), filtrée côté front sur `role === 'FORMATEUR'`, mappée vers
   `FormateurInfo {id, firstName, lastName}` (id = `uid`). Pas de nouvel endpoint backend.
2. **Édition formateurs + prérequis en 2 appels séquentiels** : `submitEdit()` appelle
   d'abord `setFormateurs()`, puis `setPrerequis()` (chaîné dans le `next` du premier
   subscribe). Si `setPrerequis` renvoie 422, l'erreur est affichée dans `formError` via
   `extractError()` qui lit `err.error` (texte brut renvoyé par
   `GlobalExceptionHandler`/`CycleDetectedException`, cf. CONV-003).
3. **Détection de cycle côté front (modale édition)** : un cours candidat `C` est désactivé
   (disabled) comme prérequis de `cours` (= A) si `A` apparaît, directement ou
   transitivement, dans l'arbre `prerequis` de `C` (`isTransitivePrerequis(targetId=A.id,
   candidate=C)` parcourt récursivement `candidate.prerequis`). Un message explicatif
   (`.hint`) s'affiche si au moins un cours est désactivé (`hasDisabledPrerequis()`).
4. **"Requis par"** : calculé via `computed()` `requiredByMap` — pour chaque cours de
   `coursList()`, on parcourt ses `prerequis` directs et on indexe l'inverse
   (prereq.id → liste des cours qui le requièrent). Utilisé pour les badges violets
   dans la table et pour l'avertissement de suppression.
5. **Couleurs badges "Requis par"** : pas de variable SCSS violette dans
   `_variables.scss` — utilisé des hex littéraux (`#F3E8FF`/`#7E22CE`) directement dans
   `cours.scss`, cohérent avec le scope "mise en page spécifique au composant" (pas une
   nouvelle variable de design system globale).
6. **Mock anti-cycle** : `MockCoursAdapter.setPrerequis` fait un parcours DFS sur
   `MOCK_DATA` pour vérifier si `coursId` est déjà (transitivement) prérequis de
   `prereqId` avant d'accepter — sinon `throwError({status:422, error:'...'})`, simulant
   le comportement backend (CycleDetectedException → 422).
7. **Suppression** : après `delete()`, on retire le cours de `coursList` ET on filtre les
   références à cet id dans `prerequis` de tous les autres cours côté front (cohérent avec
   DEC-001 — le backend supprime aussi les `CursusCours` correspondants, hors scope front).

## Open Blockers
Aucun.

## Next Actions
- WI-20260610-BACKEN-007 (cursus) peut réutiliser :
  - `BaseCoursAdapter`/`HttpCoursAdapter`/`MockCoursAdapter` (`frontend/src/app/core/adapters/cours*`)
    pour lister tous les cours du catalogue (`getAll()`) et construire la liste de cours
    disponibles à ajouter à un cursus.
  - `Cours` model (`frontend/src/app/core/models/cours.model.ts`) — `id`/`name` suffisent
    pour un sélecteur de cours dans un cursus.
  - Pattern de checkbox-list / modale `.card`/`.modal` de `cours.html`/`cours.scss`
    directement réutilisable pour une UI de sélection multiple de cours.

## Recall Hints
- Adapter Cours : `frontend/src/app/core/adapters/{cours.adapter.ts, cours-http.adapter.ts,
  cours-mock.ts}`, provider dans `app.config.ts` (actuellement `HttpCoursAdapter`).
- Page : `frontend/src/app/features/administration/cours/{cours.ts, cours.html, cours.scss}`,
  route `/app/admin/cours`.
- Formateurs : filtrer `BaseUserAdminAdapter.getAll()` sur `role === 'FORMATEUR'`.

## Proposed Rules
Aucune règle durable supplémentaire identifiée (les conventions CONV-001/CONV-003 et
DEC-001 existantes couvrent déjà les patterns utilisés).
