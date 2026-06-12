- Work Item: WI-20260608-BACKEN-001
- Role: developer
- Status: DONE
- Scope: Corrections post-revue couche Filiere (exceptions personnalisées, GlobalExceptionHandler, URLs RESTful)
- Files Touched:
  - backend/src/main/java/fr/eni/gestionformation/exception/FiliereNotFoundException.java (created)
  - backend/src/main/java/fr/eni/gestionformation/exception/FiliereAlreadyExistsException.java (created)
  - backend/src/main/java/fr/eni/gestionformation/exception/GlobalExceptionHandler.java (created)
  - backend/src/main/java/fr/eni/gestionformation/service/FiliereService.java (modified)
- Evidence: ./gradlew.bat test — BUILD SUCCESSFUL in 34s — 8 tests (3 AuthControllerTest, 3 JwtServiceTest, 2 UserDetailsServiceImplTest), 0 failures, 0 errors
- Decisions:
  - Correction 1 (FiliereRepository public): déjà présente dans le fichier — skip.
  - Correction 6 (URLs RESTful): le contrôleur utilisait déjà @PostMapping("/") et @GetMapping("/") — pas de /save ni /all — skip.
  - FiliereService: imports ajoutés pour FiliereNotFoundException et FiliereAlreadyExistsException; IllegalArgumentException remplacé dans findById() et save().
  - GlobalExceptionHandler: @RestControllerAdvice retournant 404 pour FiliereNotFoundException et 409 pour FiliereAlreadyExistsException.
- Open Blockers: ~
- Next Actions: ~
- Recall Hints: Le package exception/ a été créé ex nihilo — aucun GlobalExceptionHandler n'existait avant ce WI.

Proposed Rules:
- TYPE: CONVENTION
  Title: Exceptions métier dans package dédié avec handler global
  Scope: Tous les modules Spring Boot du projet
  Rule: Chaque domaine métier doit avoir ses propres RuntimeException dans le package exception/ et un @RestControllerAdvice centralisé (GlobalExceptionHandler) qui les mappe vers les codes HTTP appropriés.
  Why: FiliereService utilisait IllegalArgumentException générique, ce qui rendait impossible la distinction 404/409 au niveau HTTP sans dupliquer la logique dans chaque contrôleur.
  How to apply: Créer <Domain>NotFoundException (-> 404) et <Domain>AlreadyExistsException (-> 409) dans exception/; ajouter @ExceptionHandler dans GlobalExceptionHandler; ne jamais lancer IllegalArgumentException depuis un @Service.
  Evidence: WI-20260608-BACKEN-001 — FiliereService.java, GlobalExceptionHandler.java
