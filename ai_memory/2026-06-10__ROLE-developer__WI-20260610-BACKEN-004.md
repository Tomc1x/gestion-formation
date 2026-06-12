# WI-20260610-BACKEN-004

## Work Item
WI-20260610-BACKEN-004 — Relation prerequis pedagogique (ManyToMany auto-reference) sur Cours, avec detection anti-cycle.

## Role
developer

## Status
DONE

## Scope
- Entite `Cours` : ajout `Set<Cours> prerequis` (`@ManyToMany`, table `cours_prerequis`, exclue de `@ToString`/`@EqualsAndHashCode`).
- Nouvelle exception `CycleDetectedException` -> mappee en HTTP 422 (Unprocessable Entity) dans `GlobalExceptionHandler`.
- `CoursService.setPrerequis(Long coursId, List<Long> prerequisIds)` + helper prive `wouldCreateCycle(Cours cours, Cours candidat)` (BFS sur le graphe des prerequis charge recursivement via `coursRepository`).
- `CoursRequest.prerequisIds` (nullable, non touche si absent).
- `CoursResponse.prerequisIds` (liste plate d'ids — version provisoire, sera remplacee par WI-005).
- `CoursController` : `POST /api/cours` applique `setPrerequis` si `prerequisIds` fourni ; nouvel endpoint `PUT /api/cours/{id}/prerequis` (body `List<Long>`, meme convention que `assignFormateurs`).
- Aucune annotation `@PreAuthorize` ajoutee (coherent avec l'etat existant du controller, deja sans securite specifique post-WI-003).

## Files Touched
- backend/src/main/java/fr/eni/gestionformation/entity/Cours.java
- backend/src/main/java/fr/eni/gestionformation/exception/CycleDetectedException.java (nouveau)
- backend/src/main/java/fr/eni/gestionformation/exception/GlobalExceptionHandler.java
- backend/src/main/java/fr/eni/gestionformation/service/CoursService.java
- backend/src/main/java/fr/eni/gestionformation/dto/CoursRequest.java
- backend/src/main/java/fr/eni/gestionformation/dto/CoursResponse.java
- backend/src/main/java/fr/eni/gestionformation/controller/CoursController.java
- backend/src/test/java/fr/eni/gestionformation/service/CoursServiceTest.java (nouveau)

## Evidence
- `./gradlew compileJava` -> BUILD SUCCESSFUL (warning deprecation pre-existant sur GlobalExceptionHandler, non lie a ce WI).
- `./gradlew test --tests "fr.eni.gestionformation.service.CoursServiceTest"` -> BUILD SUCCESSFUL, 4 tests passes :
  - `setPrerequis_selfPrerequisite_throwsCycleDetected`
  - `setPrerequis_directCycle_throwsCycleDetected` (A<->B)
  - `setPrerequis_indirectCycle_throwsCycleDetected` (A->B->C->A)
  - `setPrerequis_validPrerequis_savesCours`
- `./gradlew test` (suite complete) -> BUILD SUCCESSFUL, aucune regression.

## Decisions
- `wouldCreateCycle` prend un seul `candidat` a la fois (appele en boucle pour chaque prerequis propose), plus simple a tester et suffisant car on verifie que `coursId` n'est pas atteignable depuis chaque candidat.
- BFS en memoire avec `coursRepository.findById` pour charger recursivement les `prerequis` de chaque noeud visite (acceptable pour la taille du catalogue, pas d'optimisation N+1 demandee).
- Cas trivial "auto-prerequis" verifie explicitement avant l'appel a `wouldCreateCycle` (sinon le BFS le detecterait aussi via le premier `pop`, mais le check explicite donne un message d'erreur plus clair).
- `CoursResponse.prerequisIds` reste une liste plate d'ids pour ce WI (decision actee dans le brief) — **WI-005 doit remplacer ce champ par `List<CoursResponse> prerequis` (recursif)**.
- `setPrerequis(coursId, null)` -> `prerequisCandidats = List.of()`, vide donc la liste de prerequis (comportement explicite de la methode si appelee avec null ; le controller `POST` ne l'appelle que si `request.getPrerequisIds() != null`, donc pas d'effet de bord a la creation).

## Open Blockers
Aucun.

## Next Actions
- WI-20260610-BACKEN-005 : remplacer `CoursResponse.prerequisIds` (List<Long>) par `prerequis: List<CoursResponse>` recursif. Attention a la profondeur/cycles potentiels lors de la serialisation recursive (meme si l'ecriture est protegee anti-cycle, prevoir une limite de profondeur ou DTO non recursif a l'infini par securite de serialisation JSON).
- Verifier si une migration de schema (Flyway/Liquibase) est necessaire pour la nouvelle table `cours_prerequis` — le projet semble utiliser `ddl-auto` (a confirmer), aucune migration ajoutee dans ce WI.

## Recall Hints
- "prerequis cours", "cours_prerequis", "CycleDetectedException", "setPrerequis", "wouldCreateCycle", "422 prerequis cycle"

## Proposed Rules
- TYPE: CONVENTION
  Title: Mapping HTTP des exceptions metier dans GlobalExceptionHandler
  Scope: backend/src/main/java/fr/eni/gestionformation/exception/
  Rule: Toute nouvelle exception metier doit avoir un handler dedie dans GlobalExceptionHandler avec le code HTTP semantiquement correct (404 NotFound, 409 Conflict, 422 UnprocessableEntity pour violation de regle metier/graphe comme un cycle).
  Why: Pattern deja suivi de maniere coherente pour Filiere/Cursus/Cours/User ; la coherence facilite la consommation cote frontend.
  How to apply: Ajouter `@ExceptionHandler(XxxException.class)` retournant `ResponseEntity<String>` avec le statut approprie.
  Evidence: backend/src/main/java/fr/eni/gestionformation/exception/GlobalExceptionHandler.java
