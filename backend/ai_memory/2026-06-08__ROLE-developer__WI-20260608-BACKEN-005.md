# Work Item: WI-20260608-BACKEN-005
- Role: developer
- Status: DONE
- Scope: Système d'invitation — InvitationToken entity, repository, DTOs, exception, service JavaMailSender, controller

## Files Touched
- `src/main/java/fr/eni/gestionformation/entity/InvitationToken.java` — créé
- `src/main/java/fr/eni/gestionformation/repository/InvitationTokenRepository.java` — créé
- `src/main/java/fr/eni/gestionformation/dto/InviteRequest.java` — créé
- `src/main/java/fr/eni/gestionformation/dto/RegisterWithTokenRequest.java` — créé
- `src/main/java/fr/eni/gestionformation/exception/InvalidInvitationTokenException.java` — créé
- `src/main/java/fr/eni/gestionformation/exception/GlobalExceptionHandler.java` — ajout handler InvalidInvitationTokenException → 400
- `src/main/java/fr/eni/gestionformation/service/InvitationService.java` — créé
- `src/main/java/fr/eni/gestionformation/controller/InvitationController.java` — créé
- `src/main/resources/application-local.properties` — ajout `app.invitation.base-url`
- `src/main/resources/application.properties` — ajout `app.invitation.base-url`

## Evidence
```
> Task :compileJava
> Task :processResources
> Task :classes
> Task :test

BUILD SUCCESSFUL in 6s
4 actionable tasks: 3 executed, 1 up-to-date
```

## Decisions
- Pattern controller identique à UserAdminController : méthode privée `toResponse(User)` dans le controller, pas de mapper dédié.
- `@Builder.Default` sur `used = false` dans InvitationToken, cohérent avec `enabled` dans User.java.
- SecurityConfig non modifiée : `/api/admin/**` → ADMINISTRATEUR et `/api/auth/**` → permitAll() déjà en place.
- InvitationController utilise deux `@RequestMapping`-level différents (pas de `@RequestMapping` classe), ce qui permet de mélanger `/api/admin/invite` et `/api/auth/register-invitation` dans le même controller sans conflit de route.

## Open Blockers
Aucun.

## Next Actions
Aucune.

## Recall Hints
- `app.invitation.base-url` configuré dans les deux fichiers properties (local + prod)
- Token UUID marqué `used=true` après usage, jamais supprimé
- Expiration 24h : `LocalDateTime.now().plusHours(24)`

## Proposed Rules
- TYPE: CONVENTION
  Title: Controller unique par domaine fonctionnel transversal
  Scope: Controllers qui croisent plusieurs patterns de routes (ex: /api/admin/* et /api/auth/*)
  Rule: Quand un controller doit exposer des endpoints sur deux préfixes de route distincts, éviter @RequestMapping au niveau classe et annoter chaque méthode individuellement avec le chemin complet.
  Why: Evite d'avoir deux controllers artificiellement séparés pour un seul service cohérent (InvitationService).
  How to apply: @PostMapping("/api/admin/invite") et @PostMapping("/api/auth/register-invitation") directement sur les méthodes.
  Evidence: InvitationController.java — WI-20260608-BACKEN-005
