# WI-20260611-FULLST-026

## Work Item
WI-20260611-FULLST-026

## Role
developer

## Status
DONE

## Scope
Add basic, idiomatic unit/MockMvc tests (no integration/Testcontainers) for backend services and controllers that were missing test coverage, following the exact style of CursusServiceTest, CoursControllerTest, FiliereServiceTest, InscriptionCoursServiceTest, AuthControllerTest, and PromotionControllerSecurityTest.

## Files Touched
- backend/src/test/java/fr/eni/gestionformation/service/UserAdminServiceTest.java (new)
- backend/src/test/java/fr/eni/gestionformation/service/InvitationServiceTest.java (new)
- backend/src/test/java/fr/eni/gestionformation/controller/UserAdminControllerTest.java (new)
- backend/src/test/java/fr/eni/gestionformation/controller/InvitationControllerTest.java (new)
- backend/src/test/java/fr/eni/gestionformation/controller/FiliereControllerTest.java (new)
- backend/src/test/java/fr/eni/gestionformation/controller/CursusControllerTest.java (new)
- backend/src/test/java/fr/eni/gestionformation/controller/InscriptionCoursControllerTest.java (new)
- backend/src/test/java/fr/eni/gestionformation/controller/PromotionControllerTest.java (new)

## Evidence
Command: `.\gradlew.bat test --console=plain` (PowerShell, backend/)
Result: `BUILD SUCCESSFUL in 17s`, 4 actionable tasks (2 executed, 2 up-to-date).
All 8 new test classes produced result XML files under `backend/build/test-results/test/`:
- TEST-fr.eni.gestionformation.service.UserAdminServiceTest.xml
- TEST-fr.eni.gestionformation.service.InvitationServiceTest.xml
- TEST-fr.eni.gestionformation.controller.UserAdminControllerTest.xml
- TEST-fr.eni.gestionformation.controller.InvitationControllerTest.xml
- TEST-fr.eni.gestionformation.controller.FiliereControllerTest.xml
- TEST-fr.eni.gestionformation.controller.CursusControllerTest.xml
- TEST-fr.eni.gestionformation.controller.InscriptionCoursControllerTest.xml
- TEST-fr.eni.gestionformation.controller.PromotionControllerTest.xml
No existing test was modified or broken.

## Decisions
- UserAdminServiceTest / InvitationServiceTest: pure Mockito unit tests (`@ExtendWith(MockitoExtension.class)`, `@Mock`/`@InjectMocks`), mirroring CursusServiceTest/FiliereServiceTest/InscriptionCoursServiceTest style. 3-4 tests each covering nominal success, "not found", and "already exists"/expired-token cases.
- InvitationServiceTest uses `ReflectionTestUtils.setField` to inject the `@Value("${app.invitation.base-url}")` private field `baseUrl`, since it's not a constructor-injected dependency picked up by `@InjectMocks`.
- All controller tests use `@WebMvcTest(XController.class)` + `@Import(SecurityConfig.class)` + `@MockitoBean` for the service/JwtService/UserDetailsServiceImpl, and `SecurityMockMvcRequestPostProcessors.user(...)` to simulate authenticated users with roles — same pattern as PromotionControllerSecurityTest/AuthControllerTest.
- For PromotionController: created a new `PromotionControllerTest.java` (separate file) for nominal CRUD (GET all, GET by id, GET 404), since the existing `PromotionControllerSecurityTest` is scoped purely to role/security regression (FULLST-019) and adding nominal cases there would mix concerns. Kept it minimal (3 tests) since security/roles are already covered.
- For CursusController and FiliereController, controller tests cover: GET list (200, ADMINISTRATEUR), GET by id (200), GET by id unknown (404 via exception->GlobalExceptionHandler), and GET with ETUDIANT role (403) — matching the security rules in SecurityConfig (`/api/cursus/**` and `/api/filiere/**` GET allow ADMINISTRATEUR/REFERENTE_ADMINISTRATIVE only).
- For CursusControllerTest, mocked `cursusService.getCoursOrdonnes(id)` to return an empty list since `toResponse()` always calls it.
- For InscriptionCoursControllerTest, focused only on `POST /api/cours-planifies/{id}/inscriptions` (creerInscription) with 3 cases: nominal 201, 404 via CoursPlanifieNotFoundException, and 403 for ETUDIANT — kept minimal per scope; did not add tests for getInscrits/getPlanningEleve to respect the "2-3 tests max" guidance.
- Used `LocalDate` (not `LocalDateTime`) for `InscriptionCours.dateInscription` after checking the entity field type.

## Open Blockers
None.

## Next Actions
None — work item complete. Manager may want to run the full `./gradlew test` once more as part of WI closure to confirm no regressions across the whole suite (already done here, all green).

## Recall Hints
- Controller test pattern: `@WebMvcTest(X.class)` + `@Import(SecurityConfig.class)` + `@MockitoBean` for service + JwtService + UserDetailsServiceImpl, `user(User.builder()...build())` from SecurityMockMvcRequestPostProcessors.
- SecurityConfig role rules referenced: `/api/admin/**` -> ADMINISTRATEUR; `/api/filiere/**`, `/api/cursus/**`, `/api/promotions/**` GET -> ADMINISTRATEUR or REFERENTE_ADMINISTRATIVE; `/api/cours-planifies/**` -> ADMINISTRATEUR/REFERENTE_ADMINISTRATIVE (GET inscrits also FORMATEUR); `/api/auth/**` permitAll (except /api/auth/register).

## Proposed Rules
- TYPE: CONVENTION
  Title: Controller test pattern for WebMvcTest with security
  Scope: backend/src/test/java/fr/eni/gestionformation/controller/**
  Rule: New controller tests should use `@WebMvcTest(XController.class)` + `@Import(SecurityConfig.class)`, mock the service + `JwtService` + `UserDetailsServiceImpl` with `@MockitoBean`, and authenticate requests via `SecurityMockMvcRequestPostProcessors.user(User.builder()...build())` rather than disabling security.
  Why: Ensures controller tests also implicitly verify role-based access rules declared in SecurityConfig, consistent with PromotionControllerSecurityTest/AuthControllerTest.
  How to apply: Copy the @MockitoBean list (service, JwtService, UserDetailsServiceImpl) and the userWithRole() helper from any of the new controller tests added in WI-20260611-FULLST-026.
  Evidence: backend/src/test/java/fr/eni/gestionformation/controller/{UserAdminControllerTest,FiliereControllerTest,CursusControllerTest,InvitationControllerTest,InscriptionCoursControllerTest,PromotionControllerTest}.java
