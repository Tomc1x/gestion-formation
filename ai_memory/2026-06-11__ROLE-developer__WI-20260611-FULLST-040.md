# WI-20260611-FULLST-040 — Tableau de bord FORMATEUR

## Role
developer

## Status
DONE

## Scope
- Backend: nouvel endpoint `GET /api/formateurs/{id}/planning` exposant les `CoursPlanifie` assignes a un formateur, avec la liste des eleves inscrits par session.
- Frontend: adapter `BaseCalendarAdapter` role-aware (FORMATEUR -> nouvel adapter formateur, sinon adapter eleve existant) ; affichage de la liste des eleves dans une vue detail au clic sur l'evenement de `/app/calendrier`.

## Files Touched
- `backend/src/main/java/fr/eni/gestionformation/repository/CoursPlanifieRepository.java` (ajout `findByFormateurUidOrderByDateDebut`)
- `backend/src/main/java/fr/eni/gestionformation/dto/PlanningFormateurResponse.java` (nouveau DTO)
- `backend/src/main/java/fr/eni/gestionformation/service/InscriptionCoursService.java` (ajout `getPlanningFormateur`)
- `backend/src/main/java/fr/eni/gestionformation/controller/InscriptionCoursController.java` (nouvel endpoint `GET /api/formateurs/{id}/planning`, helper `hasRole`)
- `backend/src/main/java/fr/eni/gestionformation/security/SecurityConfig.java` (regle `GET /api/formateurs/*/planning` -> `authenticated()`)
- `frontend/src/app/core/adapters/formateur-planning-http.adapter.ts` (nouveau)
- `frontend/src/app/core/models/inscription.model.ts` (ajout `PlanningFormateur`)
- `frontend/src/app/core/models/calendar-event.model.ts` (ajout `salle`, `eleves`, `formateurNom`)
- `frontend/src/app/app.config.ts` (provider `BaseCalendarAdapter` devient une factory role-aware)
- `frontend/src/app/features/calendrier/mon-calendrier/mon-calendrier.ts` (signal `selectedEvent`, `selectEvent`/`closeDetails`)
- `frontend/src/app/features/calendrier/mon-calendrier/mon-calendrier.html` (click handlers sur les chips/cards + panneau de detail)
- `frontend/src/app/features/calendrier/mon-calendrier/mon-calendrier.scss` (styles overlay/detail)

## Evidence
- `./gradlew test` -> BUILD SUCCESSFUL (4 actionable tasks).
- `ng build --configuration=production` -> succes, seuls warnings CSS budget preexistants (mon-calendrier.scss desormais 4.77kB / 12kB error threshold, toujours sous la limite d'erreur).
- API directe (apres assignation formateur1 a `cours-planifie 29` et eleve1 a la promotion 12) :
  `GET /api/formateurs/10/planning` (token formateur1) ->
  `[{"coursPlanifieId":29,...,"eleves":[{"eleveId":11,"firstName":"Eleve","lastName":"Un","origine":"PROMOTION"}]}]`
- chrome-devtools : login `formateur1@eni.fr` -> `/app/calendrier` affiche la session "Algorithmique / Pseudo-Code" ; clic ouvre un panneau "Élèves inscrits (1) : Eleve Un".

## Decisions
- Endpoint ajoute dans `InscriptionCoursController` (pas de nouveau `CoursPlanifieController` — coherent avec le pattern existant qui regroupe les endpoints lies au planning/inscriptions).
- Securite : `GET /api/formateurs/*/planning` est `authenticated()`, avec verification IDOR manuelle dans le controller (cast `Authentication.getPrincipal()` vers `User`, cf PIT-019) — le formateur ne peut consulter que son propre planning, ADMIN/REF ont acces a tout id.
- Reutilisation de `InscriptionCoursService.getInscritsCombines` (deja utilise pour `/api/cours-planifies/{id}/inscrits`) pour la liste d'eleves par session, evite la duplication de logique PROMOTION/INDIVIDUEL.
- UI : pas de nouvelle page "Mes cours" — extension de `/app/calendrier` (mon-calendrier) avec un panneau de detail au clic, reutilisable pour eleve (formateur de la session, WI-041) et formateur (liste d'eleves).
- Etat des donnees laisse en place pour permettre la verification visuelle (cf section Decisions de WI-041 — meme changement de donnees couvre les deux WI).

## Open Blockers
Aucun.

## Next Actions
Aucune action complementaire requise pour cette WI. Voir WI-041 pour le second volet (affichage formateur cote eleve), traite dans la meme session avec les memes changements de donnees.

## Recall Hints
- `GET /api/formateurs/{id}/planning`, `PlanningFormateurResponse`, `HttpFormateurPlanningAdapter`, factory `BaseCalendarAdapter` dans `app.config.ts`.
- Donnees de test : formateur1 (uid=10) assigne a `cours-planifie id=29` (promotion 12), eleve1 (uid=11) inscrit dans promotion 12.

## Proposed Rules
- TYPE: CONVENTION
  Title: Provider Angular role-aware via useFactory + inject()
  Scope: frontend/src/app/app.config.ts, tout DI dont l'implementation depend du role utilisateur courant
  Rule: Pour fournir une implementation differente d'un token DI selon le role de l'utilisateur connecte, utiliser `useFactory: () => { const auth = inject(AuthService); return auth.currentRole() === 'X' ? inject(AdapterX) : inject(AdapterDefault); }` dans `app.config.ts`.
  Why: Pattern simple, sans NgModule ni structural directive, coherent avec l'usage existant de `inject()` ; evite de dupliquer la logique de selection de role dans chaque composant consommateur (ici `BaseCalendarAdapter` pour `/app/calendrier`).
  How to apply: Si un futur WI ajoute un role supplementaire avec un comportement de planning/donnees different, etendre la factory existante (`BaseCalendarAdapter`) plutot que de creer un nouveau token.
  Evidence: frontend/src/app/app.config.ts (provider `BaseCalendarAdapter`), WI-20260611-FULLST-040.
