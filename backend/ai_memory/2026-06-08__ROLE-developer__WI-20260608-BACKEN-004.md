# Work Item: WI-20260608-BACKEN-004
- Role: developer
- Status: DONE
- Scope: API Admin utilisateurs — modification User.java, DTOs, exceptions, service, controller, SecurityConfig

## Files Touched
- `src/main/java/fr/eni/gestionformation/entity/User.java` — ajout champ `enabled` (@Builder.Default = true) et surcharge `isEnabled()`
- `src/main/java/fr/eni/gestionformation/dto/UserAdminCreateRequest.java` — créé
- `src/main/java/fr/eni/gestionformation/dto/UserAdminUpdateRoleRequest.java` — créé
- `src/main/java/fr/eni/gestionformation/dto/UserAdminChangePasswordRequest.java` — créé
- `src/main/java/fr/eni/gestionformation/dto/UserAdminResponse.java` — créé
- `src/main/java/fr/eni/gestionformation/exception/UserNotFoundException.java` — créé
- `src/main/java/fr/eni/gestionformation/exception/UserAlreadyExistsException.java` — créé
- `src/main/java/fr/eni/gestionformation/exception/GlobalExceptionHandler.java` — ajout handlers UserNotFoundException (404) et UserAlreadyExistsException (409)
- `src/main/java/fr/eni/gestionformation/service/UserAdminService.java` — créé
- `src/main/java/fr/eni/gestionformation/controller/UserAdminController.java` — créé
- `src/main/java/fr/eni/gestionformation/security/SecurityConfig.java` — ajout règle `/api/admin/**` → ADMINISTRATEUR

## Evidence
```
> Task :test
BUILD SUCCESSFUL in 8s
4 actionable tasks: 3 executed, 1 up-to-date
```

## Decisions
- `UserRepository.findByEmail` déjà présent, pas de modification nécessaire.
- Le mapper User → UserAdminResponse est fait directement dans le controller (méthode privée `toResponse`), cohérent avec le pattern existant dans FiliereController.
- `@Builder.Default` sur `enabled = true` pour compatibilité avec le builder Lombok existant.

## Open Blockers
Aucun.

## Next Actions
Aucune.

## Recall Hints
- Pattern controller existant : mapper dans le controller via méthode privée, pas de mapper dédié
- Lombok @Builder.Default obligatoire pour les champs primitifs avec valeur par défaut quand @Builder est présent
