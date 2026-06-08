# Memory Note

Work Item: WI-20260608-BACKEN-002
Role: developer
Status: DONE
Scope: Cursus + Cours couche complète (entity, repository, dto, exception, service, controller, security)

## Files Touched

### Modified
- `backend/src/main/java/fr/eni/gestionformation/entity/Filiere.java` — added `@OneToMany`, `@Builder.Default`, `@ToString(exclude)`, `@EqualsAndHashCode(exclude)`
- `backend/src/main/java/fr/eni/gestionformation/exception/GlobalExceptionHandler.java` — added handlers for `CursusNotFoundException` and `CoursNotFoundException`
- `backend/src/main/java/fr/eni/gestionformation/security/SecurityConfig.java` — added GET/write rules for `/api/cursus/**` and `/api/cours/**`

### Created
- `backend/src/main/java/fr/eni/gestionformation/entity/Cursus.java`
- `backend/src/main/java/fr/eni/gestionformation/entity/Cours.java`
- `backend/src/main/java/fr/eni/gestionformation/repository/CursusRepository.java`
- `backend/src/main/java/fr/eni/gestionformation/repository/CoursRepository.java`
- `backend/src/main/java/fr/eni/gestionformation/dto/CursusRequest.java`
- `backend/src/main/java/fr/eni/gestionformation/dto/CursusResponse.java`
- `backend/src/main/java/fr/eni/gestionformation/dto/CoursRequest.java`
- `backend/src/main/java/fr/eni/gestionformation/dto/CoursResponse.java`
- `backend/src/main/java/fr/eni/gestionformation/dto/FormateurInfo.java`
- `backend/src/main/java/fr/eni/gestionformation/exception/CursusNotFoundException.java`
- `backend/src/main/java/fr/eni/gestionformation/exception/CoursNotFoundException.java`
- `backend/src/main/java/fr/eni/gestionformation/service/CursusService.java`
- `backend/src/main/java/fr/eni/gestionformation/service/CoursService.java`
- `backend/src/main/java/fr/eni/gestionformation/controller/CursusController.java`
- `backend/src/main/java/fr/eni/gestionformation/controller/CoursController.java`

## Evidence

```
cd backend && ./gradlew test
BUILD SUCCESSFUL in 6s
4 actionable tasks: 2 executed, 2 up-to-date
```

Zero compiler warnings after `@Builder.Default` fix.

## Decisions

1. `@Builder.Default` on `@OneToMany` / `@ManyToMany` list fields — Lombok `@Builder` ignores field initializers without this annotation; adding it ensures `new ArrayList<>()` is honored when using the builder pattern.
2. `CursusService.deleteById` uses `@Transactional` — needed to ensure the null-set loop on Cours + cursus deletion is atomic.
3. `CoursController.create` handles formateurs inline — if `formateurIds` is provided in the creation request, it saves first then calls `assignFormateurs` to reuse the existing method (avoids duplicating logic).
4. `CursusService.save` uses `IllegalArgumentException` for duplicate name — as specified in the work item (different from Filiere pattern that uses a custom `FiliereAlreadyExistsException`; scope was explicit here).
5. Mapper in controller (no MapStruct) — consistent with existing `FiliereController` pattern.

## Open Blockers

None.

## Next Actions

None — layer is complete and tests pass.

## Recall Hints

- Cursus → GET `/api/cursus`, `/api/cursus/{id}`, `/api/cursus/filiere/{filiereId}`, POST `/api/cursus`, DELETE `/api/cursus/{id}?cascade=true/false`
- Cours → GET `/api/cours`, `/api/cours/{id}`, `/api/cours/cursus/{cursusId}`, POST `/api/cours`, DELETE `/api/cours/{id}`, PUT `/api/cours/{id}/formateurs`
- Security: GET endpoints require authentication; write endpoints require REFERENTE_ADMINISTRATIVE

## Proposed Rules

- TYPE: PITFALL
  Title: Lombok @Builder + list field initializer
  Scope: All JPA entities with Lombok @Builder and collection fields
  Rule: Always annotate collection fields with `@Builder.Default` when using `@Builder` + field initializer (e.g., `= new ArrayList<>()`).
  Why: Without `@Builder.Default`, Lombok ignores the initializer silently, causing NullPointerExceptions when code accesses the list on a builder-constructed instance.
  How to apply: Add `@Builder.Default` immediately before the field annotation (`@OneToMany`, `@ManyToMany`, etc.) whenever the field has an initializer.
  Evidence: WI-20260608-BACKEN-002 — Filiere.java, Cursus.java, Cours.java
