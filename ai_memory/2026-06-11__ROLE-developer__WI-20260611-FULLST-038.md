# WI-20260611-FULLST-038 — Fix positionnement CSS modale "Ajouter un eleve"

## Work Item
WI-20260611-FULLST-038

## Role
developer

## Status
DONE (aucun fix necessaire — positionnement deja correct)

## Scope
Diagnostiquer et corriger, si necessaire, le positionnement visuel de la modale
"Ajouter un élève" (`stagiaires-tab.html`, `.modal-overlay` / `.modal` dans
`stagiaires-tab.scss`).

## Files Touched
Aucun fichier modifie pour ce WI. (Le seul changement CSS pertinent avait deja ete
applique en FULLST-034 dans `frontend/src/styles.scss`.)

## Evidence
Verification chrome-devtools, frontend `ng serve` (localhost:4200) + backend
`./gradlew bootRun --args='--spring.profiles.active=local'` (localhost:8080) :
1. Login `ref@ref.com` / `toto785971` (REFERENTE_ADMINISTRATIVE).
2. Navigation `/app/admin/promotions/12` (promotion "TEST CDA 2", seule promotion
   existante dans le seed actuel).
3. Onglet "Stagiaires (0)", clic "Ajouter un élève" -> modale ouverte avec la liste
   des 3 eleves disponibles (Eleve Un / Deux / eleve), grace au fix FULLST-037.
4. Screenshot viewport 929x917 (taille par defaut de la page) : modale centree
   horizontalement et verticalement, overlay sombre couvre toute la fenetre,
   `max-height: 90vh` respecte, pas de depassement.
5. Screenshot apres `resize_page` a 1280x800 : modale toujours centree, meme rendu.
6. Test fonctionnel complet : ajout d'un eleve (compteur Stagiaires 0->1, ligne
   ajoutee au tableau, eleve retire de la liste disponible), puis retrait (retour a 0).
   Aucune anomalie de positionnement observee a aucune etape (ouverture, scroll
   interne de la liste `app-entity-selector`, fermeture).

## Decisions
- Aucun changement CSS effectue. Le CSS de `stagiaires-tab.scss` (`.modal-overlay`
  `position: fixed; inset: 0; display: flex; align-items: center; justify-content: center`
  + `.modal { max-width: 520px; max-height: 90vh; overflow-y: auto }`) suit deja le
  pattern harmonise par FULLST-034 (`@keyframes fadeUp` sans `transform` residuel sur
  `.page`, qui cassait `position: fixed` des modales avant ce fix). Avec le backend
  qui renvoie maintenant une liste peuplee (FULLST-037), la modale s'affiche
  correctement centree dans le viewport, sans element qui depasse.
- Hypothese initiale de l'enonce (conflit avec le scroll confine de FULLST-036 dans
  `main-layout.scss`, ou dropdown `entity-selector` qui depasse) non confirmee :
  aucun de ces symptomes n'a ete observe lors des tests visuels.

## Open Blockers
Aucun.

## Next Actions
Aucune action de suivi necessaire pour ce WI. Si un probleme de positionnement est
signale a nouveau par l'utilisateur, demander une capture d'ecran precise (taille de
fenetre, navigateur, etat avant/apres) car le rendu observe ici (chrome-devtools,
ng serve local) est correct.

## Recall Hints
- Comptes de test : `ref@ref.com` / `toto785971` (REFERENTE_ADMINISTRATIVE).
- Promotion de test disponible : `/app/admin/promotions/12` ("TEST CDA 2").
- Pattern modal CSS harmonise : voir FULLST-034
  (`ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-034.md`) — root cause
  historique = `@keyframes fadeUp` avec `transform: translateY(0)` + `forwards` sur
  `.page` qui creait un containing block et cassait `position: fixed` des modales,
  corrige par 1 ligne dans `frontend/src/styles.scss`.

## Proposed Rules
Aucune nouvelle regle proposee (le pattern modal etait deja documente via FULLST-034).
