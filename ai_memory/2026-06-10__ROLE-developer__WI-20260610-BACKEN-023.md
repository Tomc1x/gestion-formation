# WI-20260610-BACKEN-023

**Work Item:** WI-20260610-BACKEN-023
**Role:** developer
**Status:** DONE

## Scope
Cleanup of a fixed list of IDE warnings to allow a clean commit, without changing functional behavior. Branch `feature/backend/admin-user`, tier simple.

## Files Touched
- `backend/src/main/java/fr/eni/gestionformation/repository/CursusCoursRepository.java`
- `backend/src/test/java/fr/eni/gestionformation/controller/CoursControllerTest.java`
- `backend/src/main/java/fr/eni/gestionformation/service/CursusService.java`
- `backend/src/main/java/fr/eni/gestionformation/service/FiliereService.java`
- `backend/src/main/java/fr/eni/gestionformation/exception/GlobalExceptionHandler.java`
- `frontend/src/app/layouts/main-layout/header/header.scss`
- `frontend/src/styles.scss`

Not touched (deliberate, see Decisions): `backend/src/test/java/fr/eni/gestionformation/service/CoursServiceTest.java`, `ai_memory/2026-06-10__ROLE-developer__WI-20260610-BACKEN-011.md`.

## Evidence
- `./gradlew test` → `BUILD SUCCESSFUL in 7s` (4 tasks, all tests pass).
- `cd frontend && npx ng build` → `Application bundle generation complete.` No new errors. Two pre-existing budget warnings remain (`register.scss`, `utilisateurs.scss`), unrelated to this WI's changes — confirmed these are pre-existing oversized component stylesheets, not introduced by this change.

## Decisions

1. **CursusCoursRepository.existsByCoursId(Long)** — confirmed via grep across `backend/src` that the only occurrence was the declaration itself (no callers in services/controllers/tests). Removed the method. Note: `ai_rules/decisions.md` (DEC-001) references this method as "available for this purpose" for a future use case, but per current scope (no functional change beyond what's listed, remove truly-unused code) it was removed since it has zero current callers. If a future WI implements the "course used in N cursus" check described in DEC-001, the method will need to be re-added at that time.

2. **CoursServiceTest — `userRepository` and `cursusCoursRepository` mocks** — left unchanged. `CoursService` is annotated `@RequiredArgsConstructor` with three `private final` dependencies (`coursRepository`, `userRepository`, `cursusCoursRepository`). The test uses `@InjectMocks CoursService coursService` with constructor injection — Mockito's `@InjectMocks` resolves constructor parameters by type from the declared `@Mock` fields. Removing either mock would leave that constructor argument unsatisfied (Mockito would either fail to find a matching mock or pass `null`), which could break construction or cause NPEs if `CoursService` ever touches those fields in the tested methods. Kept as-is; the "unused field" warning is a false positive caused by the IDE not tracing `@InjectMocks` constructor wiring as a "use" of the mock.

3. **CoursControllerTest — `.get(0)` → `.getFirst()`** — applied directly (Java 21 `List.getFirst()`), both occurrences (lines ~44 and ~48). No behavior change.

4. **CursusService**:
   - **`deleteById(Long id, boolean cascade)` parameter `cascade`** — left unchanged (parameter kept, warning left in place). Verified `CursusController.delete` still declares `@RequestParam(defaultValue = "false") boolean cascade)` and passes it through to `cursusService.deleteById(id, cascade)`. Removing it would change the REST endpoint's query-parameter contract (`DELETE /api/cursus/{id}?cascade=...`), which is out of scope for a "no functional change" cleanup WI. Per DEC-001 in `ai_rules/decisions.md`, the parameter is intentionally retained for API compatibility even though it has no distinct effect inside the method. No `@SuppressWarnings` added (per instructions). The unused-parameter warning remains — this is an accepted/documented warning, not a regression.
   - **`addCours(...)` return value** — verified `CursusController.addCours` calls `cursusService.addCours(...)` and discards the return value, then separately calls `cursusService.findById(id)` to build the `CursusResponse`. The returned `CursusCours` was therefore genuinely unused. Changed `addCours` return type from `CursusCours` to `void` (now ends with `cursusCoursRepository.save(cursusCours);` as a statement). No controller change needed since the return value was never consumed. This is the simplest option consistent with the controller's existing pattern (re-fetching the aggregate via `findById` for the response).
   - **`finalOrdre` if/else → `Objects.requireNonNullElseGet`** — refactored:
     ```java
     int finalOrdre = Objects.requireNonNullElseGet(ordre, () -> cursusCoursRepository.findByCursusIdOrderByOrdre(cursusId).stream()
             .mapToInt(CursusCours::getOrdre)
             .max()
             .orElse(-1) + 1);
     ```
     Added `import java.util.Objects;`. Kept readable as a single statement; the supplier lambda mirrors the original else-branch logic exactly (lazy evaluation preserved, since `requireNonNullElseGet` only invokes the supplier when `ordre == null`).

5. **FiliereService — unused lambda parameter `f` in `.ifPresent(f -> {...})`** — replaced with Java 21 unnamed variable `_` (the `.filter(f -> !f.getId().equals(id))` lambda still legitimately uses `f` and was left unchanged). Result: `.ifPresent(_ -> { throw new FiliereAlreadyExistsException(name); });`.

6. **GlobalExceptionHandler — `HttpStatus.UNPROCESSABLE_ENTITY` deprecated** — verified by decompiling `spring-web-7.0.7.jar` (the actual version on this project's classpath, Spring Boot 4.0.6 / Spring Framework 7): `UNPROCESSABLE_ENTITY` is indeed annotated `@Deprecated`, with `UNPROCESSABLE_CONTENT` (also status 422, "Unprocessable Content") as its direct replacement in the same enum. Replaced `HttpStatus.UNPROCESSABLE_ENTITY` with `HttpStatus.UNPROCESSABLE_CONTENT` in `handleCycleDetected`. This is consistent with the existing `HttpStatus.XXX` style used by every other handler in the file — minimal, no new imports, no behavior change (same 422 status code and reason phrase family).

7. **SCSS font-family generic fallback** — `_variables.scss` already defines `$font-head: "Sora", system-ui, sans-serif;` and `$font-body: "Poppins", system-ui, sans-serif;`, both ending in `sans-serif`. However, the flagged lines use `var(--font-body)` / `var(--font-head)` (CSS custom properties), and the linter inspects the literal `font-family` declaration value, which is just `var(--...)` with no fallback family token of its own. Appended `, sans-serif` after the `var(...)` call on all 6 flagged declarations (2 in `header.scss`, 4 in `styles.scss`), without changing the primary font reference. This satisfies the "generic family fallback" rule at the declaration level while the custom property itself still resolves to `"Poppins"/"Sora", system-ui, sans-serif`.

8. **`ai_memory/2026-06-10__ROLE-developer__WI-20260610-BACKEN-011.md`** — not touched, per explicit instruction (IDE false positive on a fenced ```ts``` block being parsed as code by the markdown linter).

## Open Blockers
None.

## Next Actions
None — WI complete. Update `ai_memory/REPO_STATE.md` active_work_items entry for WI-20260610-BACKEN-023 to DONE (manager/rules-curator responsibility per process).

## Recall Hints
- "UNPROCESSABLE_ENTITY deprecated" → `HttpStatus.UNPROCESSABLE_CONTENT` (Spring Framework 7 / Boot 4.0.6).
- "existsByCoursId removed from CursusCoursRepository" → if DEC-001's "course used in N cursus" check is implemented later, re-add `boolean existsByCoursId(Long coursId)`.
- "CursusService.addCours now returns void" — controller already re-fetches via `findById`.
- "cascade param on CursusService.deleteById intentionally unused, kept for API compat" — see DEC-001.

## Proposed Rules

- TYPE: PITFALL
  Title: HttpStatus 422 constant renamed in Spring Framework 7
  Scope: backend/src/main/java/fr/eni/gestionformation/exception/ (and any new code using HttpStatus 422)
  Rule: Use `HttpStatus.UNPROCESSABLE_CONTENT` instead of the deprecated `HttpStatus.UNPROCESSABLE_ENTITY` for HTTP 422 responses.
  Why: Spring Boot 4.0.6 (Spring Framework 7.0.7) deprecated `UNPROCESSABLE_ENTITY` in favor of `UNPROCESSABLE_CONTENT` (same 422 code, renamed reason phrase "Unprocessable Content"). Several other `HttpStatus` constants were similarly deprecated/renamed in Spring 7 (`PROCESSING`, `PAYLOAD_TOO_LARGE`→`CONTENT_TOO_LARGE`, `I_AM_A_TEAPOT`, `BANDWIDTH_LIMIT_EXCEEDED`, `NOT_EXTENDED`).
  How to apply: When adding new `@ExceptionHandler` methods per CONV-003, check whether the chosen `HttpStatus` constant is deprecated in Spring 7 before using it; prefer the renamed equivalent.
  Evidence: WI-20260610-BACKEN-023, decompiled `spring-web-7.0.7.jar` `org/springframework/http/HttpStatus.class`.
