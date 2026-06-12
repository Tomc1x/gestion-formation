# Work Item: WI-20260611-FULLST-002

## Role
developer

## Status
DONE

## Scope
Corriger les 2 issues DevTools "[issue]" (form field sans id/name, label non
associe) presentes lorsque le modal "Modifier la promotion" de
/app/admin/promotions est ouvert, et verifier/corriger le meme defaut sur le
modal Catalogue de cours (formArrayName formateurs/prerequis). Documenter
sans corriger l'issue similaire sur /app/admin/promotions/:id/planning si liee
a FullCalendar.

## Files Touched
- frontend/src/app/layouts/main-layout/header/header.html — ajout id/name +
  `<label class="sr-only" for="global-search">` sur l'input de recherche
  globale (root cause reelle des 2 issues, presente sur toutes les pages).
- frontend/src/app/layouts/main-layout/header/header.scss — ajout classe
  utilitaire `.sr-only`.
- frontend/src/app/features/administration/promotions/promotions.html —
  remplace les 2 `<label>Élèves</label>` (createForm + editForm, groupe
  formArrayName="eleveIds") par `<span class="label1" id="...">` +
  `role="group" aria-labelledby="..."` sur le `.checkbox-list`.
- frontend/src/app/features/administration/cours/cours.html — meme correction
  pour les 4 occurrences : `<label>Formateurs</label>` (create + edit) et
  `<label>Prérequis</label>` (create + edit).

## Evidence
- `npx ng build --configuration development` -> succes (5.78 MB initial,
  warnings habituels uniquement, aucun nouveau).
- Audit chrome devtools AVANT correction sur /app/admin/promotions, modal
  "Modifier" ouvert : `list_console_messages` types=["issue"] -> 2 issues
  (msgid 4 "id or name" sur uid=1_12 = searchbox header global ; msgid 5
  "no label associated").
- Apres correction du header (id/name + label sr-only) : reload + reouverture
  du modal "Modifier" -> 1 issue restante ("no label associated", sans node
  precis).
- Apres remplacement des `<label>Élèves</label>` par span+role=group : reload
  + reouverture modal "Modifier" -> `list_console_messages` types=["issue"]
  -> aucune issue.
- Modal "Modifier" Catalogue de cours (cours "Angular Avance", formateurs +
  prerequis remplis) -> apres correction equivalente, `list_console_messages`
  types=["issue"] -> aucune issue.
- /app/admin/promotions/3/planning (FullCalendar) -> `list_console_messages`
  types=["issue"] -> aucune issue constatee lors de cette verification (donc
  non corrigee, voir Decisions).

## Decisions
- La root cause reelle des 2 issues du modal Promotions n'etait PAS dans
  promotions.html : c'etait l'input de recherche globale du header
  (composant partage sur toutes les pages), qui n'avait qu'un `aria-label`
  sans `id`/`name` ni `<label>` associe. C'est lui qui remontait msgid=4
  (uid=1_12 pointe vers la searchbox du header) et probablement aussi msgid=5.
  Corrige avec un `<label class="sr-only" for="global-search">` + id/name sur
  l'input, en suivant le pattern d'accessibilite deja utilise dans le projet
  (classe `.sr-only` existante dans promotions.scss/cours.scss/utilisateurs.scss,
  reproduite localement dans header.scss faute de fichier de styles partage).
- En complement (defense en profondeur / coherence), les `<label>` autonomes
  servant de "legende de groupe" pour les listes de checkboxes
  (formArrayName) — qui n'enveloppent aucun input et n'ont pas de `for` —
  ont ete remplaces par `<span class="label1" id="...">` + le groupe de
  checkboxes recoit `role="group" aria-labelledby="..."`. Ce pattern est
  applique de maniere identique dans promotions.html et cours.html (4
  occurrences supplementaires dans cours.html : Formateurs create/edit,
  Prerequis create/edit) pour eviter tout "dangling label" similaire meme
  si non remonte explicitement par DevTools sur ces pages au moment de
  l'audit.
- L'issue planning/FullCalendar n'a pas ete reproduite lors de cette session
  (0 issue sur /app/admin/promotions/3/planning). Non traitee — sera de
  toute facon supprimee par WI-20260611-FULLST-005 (remplacement de
  FullCalendar). Pas d'action requise ici.

## Open Blockers
Aucun.

## Next Actions
- Aucune action de suite necessaire pour ce WI.
- Pour WI-20260611-FULLST-005 : si l'issue "[issue] id or name" reapparait sur
  /app/admin/promotions/:id/planning lors de l'implementation, verifier si
  elle vient du `<select>`/combobox "Sélectionner une promotion" (probablement
  hors FullCalendar, dans planning.html) et la corriger a cette occasion si
  triviale (id/name + label).

## Recall Hints
- header.html / header.scss : input recherche globale = `#global-search`,
  pattern `.sr-only` reutilisable pour d'autres labels caches.
- promotions.html / cours.html : pattern `formArrayName` + checkbox-list ->
  utiliser `<span class="label1" id="X-label">` + `role="group"
  aria-labelledby="X-label"` plutot que `<label>` nu.

## Proposed Rules
- TYPE: PITFALL
  Title: Audit DevTools "[issue]" sur une page peut pointer vers le layout partage, pas la page testee
  Scope: frontend/src/app/layouts/main-layout/**, tout audit chrome-devtools "[issue]" sur une page admin
  Rule: Avant de corriger un composant de page suite a une issue DevTools, verifier le `data.violatingNodeAttribute`/uid de l'issue — il peut pointer vers un element du header/sidebar partage (ex: input recherche globale) present sur toutes les pages.
  Why: Sur ce WI, les 2 issues remontees pour le modal Promotions venaient en realite de l'input de recherche globale du header (`frontend/src/app/layouts/main-layout/header/header.html`), pas du modal lui-meme.
  How to apply: Utiliser `list_console_messages` puis `get_console_message` pour lire `data.violatingNodeAttribute`/`uid`, et `take_snapshot` pour identifier l'element reel avant d'editer le composant de la page.
  Evidence: WI-20260611-FULLST-002, msgid=4 pointait vers uid=1_12 (searchbox header).
