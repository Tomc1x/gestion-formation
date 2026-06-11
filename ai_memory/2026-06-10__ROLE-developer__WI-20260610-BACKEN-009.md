## Work Item
WI-20260610-BACKEN-009

## Role
developer

## Status
DONE

## Scope
Backend uniquement — compléter le CRUD de l'entité Filiere : ajout PUT et DELETE sur `/api/filiere/{id}`, gestion du conflit de nom et du blocage de suppression si la filière est utilisée par des cursus.

## Files Touched
- backend/src/main/java/fr/eni/gestionformation/exception/FiliereInUseException.java (nouveau)
- backend/src/main/java/fr/eni/gestionformation/exception/GlobalExceptionHandler.java (handler 409 pour FiliereInUseException)
- backend/src/main/java/fr/eni/gestionformation/service/FiliereService.java (méthodes `update(Long id, String name)` et `deleteById(Long id)`)
- backend/src/main/java/fr/eni/gestionformation/controller/FiliereController.java (endpoints `PUT /api/filiere/{id}` et `DELETE /api/filiere/{id}`)
- backend/src/test/java/fr/eni/gestionformation/service/FiliereServiceTest.java (nouveau, tests service)

## Evidence
- `./gradlew test --tests "fr.eni.gestionformation.service.FiliereServiceTest"` → BUILD SUCCESSFUL (7 tests)
- `./gradlew test` (suite complète) → BUILD SUCCESSFUL

## Decisions
- `update()` : recherche un conflit de nom via `findByName`, exclut la filière elle-même (par id) avant de lever `FiliereAlreadyExistsException`. Permet de renommer une filière vers son propre nom actuel sans erreur (sémantique "no-op" attendue).
- `deleteById()` : vérifie `findById` (404 via `FiliereNotFoundException` existant) puis `getCursus().isEmpty()` ; si non vide, lève `FiliereInUseException` → 409 via `GlobalExceptionHandler`, sinon `repository.deleteById`.
- `FiliereInUseException` suit exactement le pattern des exceptions existantes (RuntimeException + message construit dans le constructeur), mappée en HTTP 409 (CONFLICT) comme `FiliereAlreadyExistsException`.
- Controller : `PUT` retourne `FiliereResponse` (200 OK), `DELETE` retourne `204 No Content`, conformément aux conventions `ResponseEntity` + mapping vers DTO observées sur `getAll`/`getById`.
- Tests uniquement côté service (mockés avec Mockito, suivant le style de `CoursServiceTest`) — pas de tests controller car aucun test controller existant pour Filiere à reproduire en pattern, et la logique métier critique (conflit/409/404) est entièrement dans le service.

## Open Blockers
Aucun.

## Next Actions
Aucune action backend supplémentaire requise pour ce WI. Le frontend (utilisateurs.ts modifié hors scope) n'a pas été touché.

## Recall Hints
- FiliereInUseException, FiliereService.update, FiliereService.deleteById, /api/filiere/{id} PUT/DELETE, GlobalExceptionHandler 409.

## Proposed Rules
Aucune nouvelle règle durable identifiée — le pattern exception/handler suivi était déjà bien établi (CONV-003) et a été reproduit à l'identique.
