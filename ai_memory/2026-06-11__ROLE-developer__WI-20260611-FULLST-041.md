# WI-20260611-FULLST-041 — Exposer le formateur dans le planning ELEVE

## Role
developer

## Status
DONE

## Scope
- Backend : ajouter `formateurId`/`formateurNom` a `PlanningEleveResponse`, mappes depuis `CoursPlanifie.getFormateur()` (null -> "Non assigné").
- Frontend : repercuter dans `PlanningEleve` (inscription.model.ts) et l'affichage calendrier (panneau de detail de session ajoute dans WI-040).

## Files Touched
- `backend/src/main/java/fr/eni/gestionformation/dto/PlanningEleveResponse.java` (ajout `formateurId: Long`, `formateurNom: String`)
- `backend/src/main/java/fr/eni/gestionformation/controller/InscriptionCoursController.java` (`getPlanningEleve` : mapping `cp.getFormateur()` -> `formateurId`/`formateurNom`, "Non assigné" si null)
- `frontend/src/app/core/models/inscription.model.ts` (ajout `formateurId`/`formateurNom` a `PlanningEleve`)
- `frontend/src/app/core/adapters/eleve-planning-http.adapter.ts` (`toCalendarEvent` transmet `formateurNom`)
- `frontend/src/app/core/models/calendar-event.model.ts` (ajout `formateurNom?: string`)
- `frontend/src/app/features/calendrier/mon-calendrier/mon-calendrier.html` (panneau de detail affiche "Formateur : ..." si present, + `event-card__formateur` en vue semaine)
- `frontend/src/app/features/calendrier/mon-calendrier/mon-calendrier.scss` (style `.event-card__formateur`)

## Evidence
- `./gradlew test` -> BUILD SUCCESSFUL (execute conjointement avec WI-040, mêmes fichiers backend).
- `ng build --configuration=production` -> succes (cf WI-040).
- Donnees de test : eleve1 (uid=11) ajoute a la promotion 12 "TEST CDA 2" via `POST /api/promotions/12/eleves/11` (REF token) ; formateur1 (uid=10) assigne au `cours-planifie id=29` via `PUT /api/promotions/12/planning/29` avec `{"dateDebut":"2026-06-15","dateFin":"2026-06-19","formateurId":10}`.
- API directe : `GET /api/eleves/11/planning` (token eleve1) -> premiere session id=29 contient `"formateurId":10,"formateurNom":"Form Ateur"`, les sessions sans formateur retournent `"formateurId":null,"formateurNom":"Non assigné"`.
- chrome-devtools : login `eleve1@eni.fr` -> `/app/calendrier` affiche 3 sessions de la promotion 12 (juin 2026) ; clic sur "Algorithmique / Pseudo-Code" ouvre le panneau de detail montrant "Promotion : Algorithmique / Pseudo-Code" et "Formateur : Form Ateur".

## Decisions
- `formateurNom` est calcule cote backend (`firstName + " " + lastName`, ou "Non assigné" si `cp.getFormateur() == null`) plutot que de renvoyer un objet imbrique `User` complet — coherent avec `InscritResponse` (deja `firstName`/`lastName` separes) et evite d'exposer des champs sensibles de `User`.
- **Etat des donnees laisse en place (non revert)** : eleve1 (uid=11) reste inscrit dans la promotion 12 "TEST CDA 2", et `cours-planifie id=29` (Algorithmique / Pseudo-Code, promotion 12) reste assigne a formateur1 (uid=10). Ce choix permet de garder un jeu de donnees demontrable pour les deux WI (040/041) et pour de futures verifications visuelles. Si un etat "propre" est prefere :
  - `DELETE /api/promotions/12/eleves/11` (retire eleve1 de la promotion 12)
  - `PUT /api/promotions/12/planning/29` avec `{"dateDebut":"2026-06-15","dateFin":"2026-06-19","formateurId":null}` (retire l'assignation formateur)

## Open Blockers
Aucun.

## Next Actions
Aucune. Si l'etat de donnees ci-dessus doit etre nettoye avant une demo/prod, executer les 2 appels listes dans Decisions.

## Recall Hints
- `PlanningEleveResponse.formateurId/formateurNom`, "Non assigné" pour formateur null.
- Promotion 12 "TEST CDA 2" : eleve1 inscrit, cours-planifie 29 assigne a formateur1 — etat partage avec WI-040.

## Proposed Rules
Aucune regle additionnelle (cf WI-040 pour la regle CONVENTION sur le provider role-aware, qui couvre les deux WI).
