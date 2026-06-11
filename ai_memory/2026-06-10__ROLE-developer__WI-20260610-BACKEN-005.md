# WI-20260610-BACKEN-005

## Work Item
WI-20260610-BACKEN-005 (tier complex, branch feature/backend/admin-user)

## Role
developer

## Status
DONE

## Scope
Replace `CoursResponse.prerequisIds: List<Long>` with a recursive `prerequis: List<CoursResponse>` structure, where each prerequisite is itself a full `CoursResponse` recursively containing its own prerequisites.

## Files Touched
- `backend/src/main/java/fr/eni/gestionformation/dto/CoursResponse.java` — replaced `prerequisIds: List<Long>` with `prerequis: List<CoursResponse>`.
- `backend/src/main/java/fr/eni/gestionformation/controller/CoursController.java` — `toResponse(Cours)` now delegates to a new private recursive `toResponse(Cours cours, Set<Long> visited)`. Adds `cours.getId()` to `visited` before recursing into prerequisites. If a prerequisite's id is already in `visited` (cycle safety net), returns a `CoursResponse` for that node with `prerequis = List.of()` instead of recursing or omitting it.
- `backend/src/test/java/fr/eni/gestionformation/controller/CoursControllerTest.java` — new test file, verifies a 2-level recursive chain A -> B -> C via `getById`.

## Files Checked, Not Modified
- `backend/src/main/java/fr/eni/gestionformation/entity/Cours.java` — no change needed, `prerequis: Set<Cours>` already in place from WI-004.
- `backend/src/main/java/fr/eni/gestionformation/dto/CoursInCursusResponse.java` — does not contain `prerequisIds`/`prerequis`, no change needed.
- `backend/src/main/java/fr/eni/gestionformation/controller/CursusController.java` — builds `CoursInCursusResponse` (a separate, flat DTO), never `CoursResponse`. No coupling to fix; recursion change is isolated to `CoursController`.
- `backend/src/test/java/fr/eni/gestionformation/service/CoursServiceTest.java` — does not reference `CoursResponse`/`prerequisIds`, no change needed.

## Evidence
- `./gradlew compileJava` — success, no errors.
- `./gradlew test --tests "*CoursControllerTest*" --tests "*CoursServiceTest*"` — pass.
- `./gradlew test` (full suite) — pass.

## Decisions
- **Final shape of `CoursResponse`** (for WI-006 frontend Catalogue de cours):
  ```java
  class CoursResponse {
      Long id;
      String name;
      List<FormateurInfo> formateurs;
      List<CoursResponse> prerequis; // recursive, same shape
  }
  ```
  Each entry in `prerequis` is itself a full `CoursResponse`, recursively expanded until a course has no prerequisites (`prerequis = []`).
- `toResponse(Cours cours)` (public-facing, used by `getAll`/`getById`/`create`/`assignFormateurs`/`setPrerequis`) now calls `toResponse(cours, new HashSet<>())`. For `GET /api/cours` (list), each top-level course starts with its own fresh empty `visited` set — not shared across the list — which is correct and simple per WI scope.
- Cycle safety net: a `Set<Long> visited` is threaded through the recursion. Before recursing into a prerequisite, if its id is already in `visited`, the recursion stops for that branch and a `CoursResponse` is still returned for that node (with its own `formateurs` populated but `prerequis = List.of()`), rather than silently omitting it. This keeps the frontend aware the node exists without infinite recursion / StackOverflowError.

## Open Blockers
None for this WI. Documented risk only: termination relies on the anti-cycle guarantee enforced at write-time by `CoursService.setPrerequis`/`wouldCreateCycle` (WI-004). If a cycle ever existed in the DB despite that guard (future bug, manual DB edit, etc.), the `visited` set in `toResponse` prevents infinite recursion/StackOverflow — each node is visited at most once per recursive path, cycle is cut and surfaced as an empty-prerequis leaf instead of crashing.

## Next Actions
None — WI complete. WI-006 (frontend Catalogue de cours) can consume `CoursResponse.prerequis` as a recursive tree directly.

## Recall Hints
- `CoursController.toResponse` private recursive overload signature: `private CoursResponse toResponse(Cours cours, Set<Long> visited)`.
- `CoursResponse.prerequis` is `List<CoursResponse>`, NOT `List<Long>` anymore (renamed from `prerequisIds`).
- New test: `backend/src/test/java/fr/eni/gestionformation/controller/CoursControllerTest.java`.

## Proposed Rules
None — pattern (recursive DTO mapping with a `visited` Set as a cheap cycle safety net) is specific to this self-referencing entity (`Cours.prerequis`) and not yet a recurring pattern elsewhere in the codebase.
