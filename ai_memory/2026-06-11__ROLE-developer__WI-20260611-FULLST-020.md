# WI-20260611-FULLST-020 — Frontend+Backend : CRUD complet Cursus

## Status
READY_FOR_REVIEW (implementation complete, build PASS, tests PASS, visual verification
mostly OK — PUT endpoint not exercised live due to backend restart required, see Open
Blockers).

## Scope
CRUD complet sur les cards Cursus de /app/admin/cursus :
1. Bouton "Modifier" (modal nom + filiere) -> nouveau `PUT /api/cursus/{id}`.
2. Bouton "Supprimer" (modal confirmation) -> `DELETE /api/cursus/{id}` (existait deja).
3. "+ Ajouter un cours" (select natif) -> `POST /api/cursus/{id}/cours` (existait deja).
4. Bouton "Retirer" par ligne de cours -> `DELETE /api/cursus/{id}/cours/{coursId}`
   (existait deja).
5. Nettoyage : suppression du cursus de test "Test Cursus QA".

## Files Touched

### Backend
- `backend/src/main/java/fr/eni/gestionformation/service/CursusService.java`
  - Nouvelle methode `update(Long id, String name, Long filiereId, Filiere filiere)` :
    findById (404 si absent), controle anti-doublon de nom (exclut le cursus
    lui-meme, IllegalArgumentException si collision — meme convention que `save`),
    setName + setFiliere (null si filiereId == null), save.
  - Import ajoute : `fr.eni.gestionformation.entity.Filiere`.
- `backend/src/main/java/fr/eni/gestionformation/controller/CursusController.java`
  - Nouveau `PUT /api/cursus/{id}` : resout la Filiere via `filiereService.findById`
    si `filiereId != null`, appelle `cursusService.update(...)`, retourne
    `toResponse(updated)`.
- `backend/src/test/java/fr/eni/gestionformation/service/CursusServiceTest.java` (NOUVEAU)
  - 5 tests : update valide (nom+filiere), filiereId null efface la filiere, nom
    deja pris par un autre cursus -> IllegalArgumentException, meme nom que
    soi-meme -> OK, id inconnu -> CursusNotFoundException.

### Frontend
- `frontend/src/app/core/adapters/cursus.adapter.ts` : ajout `update(cursusId, req)`
  et `delete(cursusId)` abstraits.
- `frontend/src/app/core/adapters/cursus-http.adapter.ts` : implementation HTTP
  (`PUT /api/cursus/{id}`, `DELETE /api/cursus/{id}`).
- `frontend/src/app/core/adapters/cursus-mock.ts` : implementation mock (anti-doublon
  nom simule en 422, MOCK_FILIERES local pour resoudre filiereName).
- `frontend/src/app/features/administration/cursus/cursus.ts` :
  - Modale "Modifier le cursus" (editingCursus signal, editCursusForm,
    open/close/submitEditCursus).
  - Modale "Supprimer le cursus" (deletingCursus signal, open/close/confirmDeleteCursus).
  - `availableCoursForCursus(cursus)`, `addCoursToCursus(cursus, coursId)`,
    `removeCoursFromCursus(cursus, coursId)`.
- `frontend/src/app/features/administration/cursus/cursus.html` :
  - Header de carte cursus avec boutons Modifier/Supprimer (icones pencil/trash2,
    pattern repris de la section Filieres).
  - Bouton "Retirer" (icone X) ajoute a chaque ligne `<li>` de cours, a cote de
    Monter/Descendre.
  - Select natif "+ Ajouter un cours" en pied de carte (affiche seulement si
    `availableCoursForCursus(cursus).length > 0`), avec `<label class="sr-only">`
    associe (id `add-cours-{cursusId}`).
  - Deux nouvelles modales en fin de fichier : "Modifier le cursus" (nom + select
    filiere, meme structure que "Modifier la filiere") et "Supprimer le cursus"
    (confirmation, meme structure que "Supprimer la filiere").
- `frontend/src/app/features/administration/cursus/cursus.scss` :
  - `.cursus-card__header` / `.cursus-card__actions` / `.cursus-card__add-cours`.
  - `.sr-only` ajoute (absent de ce fichier, present dans d'autres features).

## Decisions
- DEC : `update()` reutilise le meme pattern anti-doublon "IllegalArgumentException
  sans handler dedie" que `save()` existant, pour rester coherent (pas de nouvelle
  exception `CursusAlreadyExistsException` introduite — aurait ete plus propre mais
  hors perimetre minimal, et `save()` n'a pas non plus de handler).
- DEC : le select "+ Ajouter un cours" est un `<select>` natif avec un handler
  `(change)` qui reset sa propre valeur a `""` apres l'ajout, pour rester
  reutilisable comme un bouton (pas de FormControl dedie par carte).
- DEC : `availableCoursForCursus` filtre simplement par id deja present dans
  `cursus.cours` — pas de logique anti-cycle/prerequis ici (contrairement au
  builder de la modale "Nouveau cursus"), car l'origine QA ne le demandait pas et
  cela resterait coherent avec le comportement actuel de `addCours`/`removeCours`
  backend (pas de validation prerequis a ce niveau).

## Evidence

### Backend
- `cd backend && ./gradlew compileJava -q` -> OK (aucune erreur).
- `cd backend && ./gradlew test --tests "*CursusServiceTest*" -q` -> PASS (5/5).
- `cd backend && ./gradlew test -q` -> BUILD SUCCESSFUL (suite complete, aucune
  regression).

### Frontend
- `cd frontend && npx ng build` -> BUILD SUCCESSFUL. Chunk `cursus` 35.80 kB
  raw / 7.38 kB transfer (legere augmentation par rapport a avant, attendue).
  Seuls warnings preexistants (register.scss, promotions.scss, utilisateurs.scss)
  — aucun nouveau warning sur cursus.scss.

### Verification visuelle (chrome-devtools, ng serve :4200, login ref@ref.com)
- `/app/admin/cursus` : boutons "Modifier"/"Supprimer" presents sur chaque carte
  cursus (snapshot a11y confirme : "Modifier Concepteur developpeur d'application",
  "Supprimer Test Cursus QA", etc.), bouton "Retirer {nom}" present sur chaque ligne
  de cours, select "+ Ajouter un cours" present en pied de carte avec options
  filtrees (cours non encore dans le cursus).
- Modal "Modifier le cursus" s'ouvre avec nom/filiere prerempli (snapshot confirme
  `textbox value="Test Cursus QA"`, `combobox value="Developpement"`).
- DELETE cours d'un cursus existant : `DELETE /api/cursus/2/cours/3` -> 200 (CSS
  Basique retire de "Test Cursus QA", carte passe a "Aucun cours dans ce cursus.").
- DELETE cursus : `DELETE /api/cursus/2` -> 204 ("Test Cursus QA" disparait de la
  liste, compteur filiere Developpement passe de 2 a 1). NETTOYAGE EFFECTUE.
- PUT cursus (renommage) : `PUT /api/cursus/2` -> **403 Forbidden**. Voir Open
  Blockers.

## Open Blockers
- **PUT /api/cursus/{id} renvoie 403 sur le backend local actuellement lance** (port
  8080). Cause tres probable : le processus backend en cours d'execution a ete
  demarre AVANT ce changement de code (le mapping `PUT /api/cursus/{id}` n'existe
  pas dans le JAR/classes charges) -> requete non mappee sur un path deja couvert
  par une regle de securite -> 403 (meme symptome documente dans
  WI-20260611-BACKEN-024 pour un autre endpoint).
  - SecurityConfig.java verifiee : `/api/cursus/**` (hors GET) ->
    `hasRole("REFERENTE_ADMINISTRATIVE")`, ref@ref.com a ce role -> pas un probleme
    de permission.
  - `./gradlew compileJava` confirme que le code compile et que la methode
    `CursusController.update` existe.
  - Pas de redemarrage du backend effectue dans ce WI : un autre agent (FULLST-019)
    travaille en parallele sur le meme processus backend (PromotionController/
    PromotionService/SecurityConfig) — un restart aurait pu interrompre sa session
    de verification.
  - **Action recommandee** : redemarrer le backend local (`./gradlew bootRun
    --args='--spring.profiles.active=local'`) une fois FULLST-019 termine, puis
    revalider `PUT /api/cursus/{id}` (renommage + changement de filiere) via
    chrome-devtools sur /app/admin/cursus.

## Next Actions
- Manager : sequencer un redemarrage backend (apres FULLST-019) puis re-tester le
  flux "Modifier un cursus" (renommage + changement de filiere) en conditions
  reelles.
- Si le 403 persiste apres redemarrage, investiguer plus avant (CSRF ? methode PUT
  bloquee ailleurs dans SecurityConfig ?).

## Recall Hints
- `CursusController.update` / `CursusService.update` : nouveau PUT /api/cursus/{id}.
- cursus.ts : `editingCursus`, `deletingCursus`, `availableCoursForCursus`,
  `addCoursToCursus`, `removeCoursFromCursus`.
- Verifier `ai_memory/2026-06-11__ROLE-developer__WI-20260611-BACKEN-024.md` pour le
  pattern 403/dataintegrity deja documente (meme symptome possible).

## Proposed Rules
- TYPE: PITFALL
  Title: 403 Forbidden sur un endpoint nouvellement ajoute = backend pas redemarre
  Scope: backend Spring Boot local (profil `local`, ddl-auto=update, pas de hot
  reload garanti)
  Rule: Si un nouvel endpoint (controller method + mapping) renvoie 403 alors que la
  regle SecurityConfig pour ce path/role est correcte et que le code compile, verifier
  EN PREMIER si le processus backend en cours d'execution a ete demarre avant
  l'ajout du code — un PUT/POST/DELETE non mappe sur un path deja couvert par une
  regle de securite (hors GET) renvoie 403 plutot que 404/405.
  Why: deja observe 2 fois (BACKEN-024 et FULLST-020) avec le meme symptome
  trompeur (ressemble a un probleme de role/permission alors que c'est un probleme
  de redemarrage).
  How to apply: avant de creer un ticket "bug 403" sur un endpoint tout juste
  ajoute, demander/planifier un redemarrage du backend local et re-tester.
  Evidence: ai_memory/2026-06-11__ROLE-developer__WI-20260611-BACKEN-024.md,
  ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-020.md
