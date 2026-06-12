# ROLE NOTE — solution-architect

Work Item: WI-20260611-FULLST-005
Role: solution-architect
Status: READY_FOR_REVIEW
Mode: DEEP
Files Touched: aucun (design only)

## Probleme analyse

`frontend/src/app/features/administration/promotions/planning/planning.ts`
utilise FullCalendar Angular avec drag&drop (`eventDrop`/`eventResize` ->
`updatePlanning`). Decision utilisateur : remplacer par un composant base sur
`mon-calendrier` (vues mois/semaine, FR, date-fns), en lecture + clic — sans
drag&drop. L'API backend (`PlanificationService`, `updatePlanning`,
`findOverlappingForFormateur`) ne change pas.

## Analyse de compatibilite backend (confirmee)

- `PromotionController.updatePlanning` (`PUT
  /api/promotions/{id}/planning/{promotionCoursId}`, body
  `PlanningUpdateRequest{dateDebut, dateFin}`) reste utilise tel quel — il
  sert maintenant a la sauvegarde depuis un formulaire d'edition au lieu d'un
  drop.
- `PromotionService.updatePlanning` (lignes 153-186) calcule deux types de
  warnings :
  - "ordre chronologique du cursus non respecte" (chevauchement avec
    voisins ordre-1/ordre+1)
  - "Conflit formateur : ... promotion {nom}" (via
    `findOverlappingForFormateur`, WI-018)
  Ces warnings sont retournes dans `PromotionCoursResponse.warnings: string[]`
  par le PUT planning. Aucune modification necessaire — le nouveau composant
  doit juste les afficher differemment (pas de bordure orange sur un
  evenement draggable, mais un encart d'alerte dans le formulaire).
- `PlanificationService.genererPlanning` (WI-016) reste appelee uniquement a
  la creation/maj de promotion (cursus/dateDebut change) — aucun lien direct
  avec l'UI calendrier, pas d'impact.
- Pas de nouvel endpoint necessaire pour "Planifier un cours" (creation d'une
  nouvelle entree de planning) SAUF si le besoin est de creer une
  `PromotionCours` ad-hoc hors planning genere automatiquement — a clarifier
  (voir Open Blockers). Si confirme necessaire : `POST
  /api/promotions/{id}/planning` avec body `{coursId, dateDebut, dateFin,
  formateurId?}` — actuellement AUCUN endpoint de creation de
  `PromotionCours` n'existe (seul `updatePlanning` modifie une entree
  existante). Le moteur (WI-016) cree toutes les `PromotionCours` initiales a
  la creation de la promotion ; il n'y a pas de "trous" a remplir par
  l'utilisateur dans le flux actuel.

## Decisions

### 1. Reutilisation de mon-calendrier — composition vs duplication

**Recommandation** : NE PAS reutiliser directement
`MonCalendrierComponent` tel quel (il est concu pour le calendrier
eleve/formateur agrege avec `CalendarEvent` issu de `BaseCalendarAdapter`,
modele different de `PromotionCours`). A la place :

- Creer `frontend/src/app/features/administration/promotions/planning/planning.ts`
  (remplace le contenu actuel) en **portant la logique de grille** de
  `mon-calendrier.ts`/`.html`/`.scss` (monthGrid/weekDays/eventsByDay,
  navigation prev/next, navTitle FR via date-fns) directement dans
  `PlanningComponent`, adaptee a `PromotionCours` au lieu de `CalendarEvent`.
- Alternative (effort plus eleve, mais DRY) : extraire la grille pure de
  `mon-calendrier` dans un composant partage
  `frontend/src/app/shared/components/month-week-grid/` qui prend en
  `input()` une `Map<string, T[]>` (eventsByDay generique) + un
  `template`/`ng-content` pour le rendu de chaque event, et un
  `output()` `dayClick`/`eventClick`. `MonCalendrierComponent` et
  `PlanningComponent` deviennent tous deux des consommateurs de cette grille.

**Choix retenu** : Alternative (composant partage `month-week-grid`).
Justification : le portage direct (option 1) duplique ~150 lignes de logique
date-fns/grille entre deux composants, ce qui est exactement le type de
duplication que ce refactoring est cense eviter (esprit de la decision
"remplacer FullCalendar par notre composant"). L'effort supplementaire est
modere car la grille de `mon-calendrier` est deja bien isolee
(computed `monthGrid`/`weekDays`/`eventsByDay`, template generique par
cellule). Le cout de migration de `mon-calendrier.ts` vers le composant
partage est faible (signals/computed deplaces tels quels).

### 2. Composant partage `month-week-grid`

**Emplacement** : `frontend/src/app/shared/components/month-week-grid/`
(`MonthWeekGridComponent`, `app-month-week-grid`)

- `input()` :
  - `view: input<'month' | 'week'>()`
  - `referenceDate: input<Date>()`
  - `eventsByDay: input<Map<string, T[]>>()` (T generique via composant
    generique TypeScript, ou `unknown[]` + projection par le parent)
- `output()` :
  - `dayClick: output<Date>()`
  - (le rendu de chaque event est fourni par le parent via `ng-content`/
    `ContentChild` template, ou plus simplement : le composant emet
    `eventsByDay` deja calcule et le parent boucle lui-meme dans son propre
    template — **plus simple en Angular** : garder le grid layout +
    navigation dans le composant partage, mais laisser le rendu de cellule
    au parent via un `<ng-template let-events let-day>` projete)
- Expose aussi : `monthGrid`, `weekDays`, `navTitle`, `weekDayLabels`,
  `navigatePrev()`, `navigateNext()`, `setView()`, `isCurrentMonth()` —
  identiques a `mon-calendrier.ts` actuel (computed purs, pas de dependance a
  `BaseCalendarAdapter`).

`MonCalendrierComponent` et `PlanningComponent` injectent chacun leur propre
adapter de donnees (`BaseCalendarAdapter` / `BasePromotionAdapter`), calculent
leur propre `eventsByDay` (Map cle `yyyy-MM-dd`), et passent au composant
partage. Le rendu visuel de chaque event (chip eleve vs event promotion)
reste dans le template de chaque feature, via projection de contenu.

**Si l'effort de refactoring de `mon-calendrier` est juge hors scope par le
developer** (FULLST-005 ne mentionne pas de modifier `mon-calendrier`), repli
accepte : porter une copie adaptee de la logique grille directement dans
`planning.ts` (option 1), et noter la duplication comme dette (PIT a
proposer) pour une factorisation ulterieure. Le manager tranche selon le
budget du WI.

### 3. Interaction : lecture + clic, formulaire de planification/edition

- **Ouverture initiale** : `referenceDate` initialise a la date de
  `promotion.dateDebut` (parsee en `Date`) de la promotion selectionnee — pas
  `new Date()`. Recalculer `referenceDate` a chaque changement de promotion
  selectionnee (`onPromotionChange`).
- **Affichage des `PromotionCours`** : chaque jour du planning affiche un
  "chip" (style proche de `event-chip`/`event-card` de mon-calendrier),
  colore par `statut` (reprendre `STATUT_COLORS` de `planning.ts` actuel :
  PLANIFIE bleu, EN_COURS vert, TERMINE gris), avec un indicateur visuel
  (icone ou bordure orange) si `pc.warnings.length > 0` — mais les warnings
  ne sont connus qu'APRES un appel `updatePlanning` (le `GET
  /api/promotions/{id}` actuel renvoie `warnings: []` toujours, cf.
  `PromotionController.toResponse` ligne 76 : `.map(pc ->
  toPromotionCoursResponse(pc, List.of()))`). Donc en lecture initiale, AUCUN
  evenement n'a de warning affichable — les warnings n'apparaissent qu'au
  moment de la sauvegarde dans le formulaire (point 4 ci-dessous). Pas de
  changement necessaire ici, juste a documenter pour eviter une fausse
  attente UI.
- **Clic sur un jour vide** : ouvre un formulaire "Planifier un cours" — MAIS
  voir Open Blocker : aucun endpoint de creation de `PromotionCours`
  n'existe actuellement. Si le scope se limite a "deplacer/editer les cours
  deja generes par `genererPlanning`", retirer cette action ou la transformer
  en information ("aucune action sur un jour vide tant qu'aucun endpoint de
  creation n'existe").
- **Clic sur un evenement existant (`PromotionCours`)** : ouvre un modal
  "Modifier la session" :
  - Champs : cours (lecture seule — `pc.coursNom`, le cours est fixe par le
    planning genere, pas reassignable depuis l'UI), date debut, date fin
    (inputs `type="date"`), formateur (select — MAIS `PromotionCours` /
    `PlanningUpdateRequest` n'ont pas de notion de "formateur assigne a cette
    session" ; les formateurs sont sur `Cours.formateurs` (ManyToMany), pas
    par session. Si le besoin est d'assigner un formateur PAR SESSION
    planifiee, c'est un changement de modele hors scope FULLST-005 — a
    signaler comme Open Blocker / possible lien avec FULLST-007).
    Pour ce WI, le formulaire edite uniquement `dateDebut`/`dateFin`
    (= perimetre exact de `PlanningUpdateRequest` actuel).
  - Bouton "Enregistrer" -> `updatePlanning(promotionId, pc.id, {dateDebut,
    dateFin})`. En reponse :
    - Si `response.warnings.length > 0` : afficher un encart d'alerte dans le
      formulaire (liste des warnings, style `.warning-box` deja utilise dans
      `cours.html`), NE PAS bloquer la sauvegarde (warnings non bloquants,
      coherent avec le comportement actuel ou `eventDrop` n'annule que sur
      erreur HTTP, pas sur warning).
    - Si erreur HTTP : afficher message d'erreur, ne pas fermer le modal
      (equivalent du `arg.revert()` actuel, mais sans drag a annuler).
  - Bouton "Supprimer" : AUCUN endpoint de suppression d'une `PromotionCours`
    individuelle n'existe (`PromotionService` n'a pas de
    `deletePromotionCours`). Si "suppression" est requise par ce WI, c'est un
    nouvel endpoint backend — a clarifier (voir Open Blockers). Sinon, retirer
    cette action du modal pour ce WI.
- **Validation des dates** : `dateFin >= dateDebut` cote formulaire avant
  envoi (validation HTML5 `min` dynamique ou validator Angular custom).

### 4. Suppression de la dependance FullCalendar

- `frontend/package.json` : retirer `@fullcalendar/angular`,
  `@fullcalendar/core`, `@fullcalendar/daygrid`, `@fullcalendar/timegrid`,
  `@fullcalendar/interaction` SI aucun autre composant ne les utilise (grep
  rapide sur `@fullcalendar` dans `frontend/src` apres migration).
- `app.config.ts` : verifier s'il y a un provider/registration FullCalendar a
  retirer (generalement non, FullCalendar Angular ne necessite pas de
  provider global, juste l'import du module dans le composant — confirmer).

## Plan d'action pour le developer

1. (Optionnel mais recommande) Creer
   `frontend/src/app/shared/components/month-week-grid/` en extrayant la
   logique grille de `mon-calendrier.ts`/`.html`/`.scss` (computed
   `monthGrid`, `weekDays`, `eventsByDay` generique, `navTitle`, navigation,
   `weekDayLabels`). Refactorer `MonCalendrierComponent` pour consommer ce
   composant partage (verifier non-regression visuelle de
   `/app/calendrier`).
2. Reecrire `planning.ts`/`.html`/`.scss` :
   - Retirer tous les imports/usages `@fullcalendar/*`.
   - Charger `promotion.planning: PromotionCours[]`, construire
     `eventsByDay: Map<string, PromotionCours[]>` (cle `pc.dateDebut` —
     attention aux sessions multi-jours : afficher l'event sur chaque jour de
     `dateDebut` a `dateFin` inclus, ou seulement sur `dateDebut` avec une
     indication de duree — choix UX a trancher par le developer, mon-calendrier
     actuel n'affiche que sur `startDate`).
   - `referenceDate` initialise sur `promotion.dateDebut` (parse ISO date).
   - Implementer le modal "Modifier la session" (champs dateDebut/dateFin,
     bouton Enregistrer -> `updatePlanning`, affichage warnings non
     bloquants).
   - Conserver le selecteur de promotion existant
     (`onPromotionChange`/`selectedPromotionId`).
3. `frontend/package.json` : retirer les deps `@fullcalendar/*` si plus
   utilisees ailleurs (grep prealable).
4. `ng build` : verifier l'absence de chunk FullCalendar residuel et les
   budgets CSS (PIT-004).
5. Tests/verification : aucun test backend a modifier (API inchangee).
   Verification visuelle manuelle (`ng serve`) sur
   `/app/admin/promotions/:id/planning` — navigation mois/semaine FR, clic
   evenement -> modal -> sauvegarde -> warnings affiches si conflit (creer un
   scenario de conflit formateur en local pour le verifier, cf. methode de
   verification de WI-20260611-BACKEN-024).

## Anti-scope

- Ne pas modifier `PlanificationService`, `PromotionService.updatePlanning`,
  ni les entites backend — l'API ne change pas (confirme ci-dessus).
- Ne pas implementer de creation/suppression de `PromotionCours` tant que les
  endpoints correspondants n'existent pas — signaler comme Open Blocker plutot
  que d'improviser un comportement.
- Ne pas toucher a la page `promotion-detail` (FULLST-001, en parallele).

## Risques et points d'attention

1. **(Critique)** "Planifier un cours" (clic jour vide) et "Supprimer une
   session" n'ont pas d'endpoint backend correspondant. Si ces actions sont
   jugees indispensables par l'utilisateur final, c'est un sous-WI backend
   supplementaire (hors scope FULLST-005 tel que redige). Mitigation :
   confirmer avec le manager/utilisateur avant implementation — pour ce WI,
   se limiter a "edition de date d'une session existante" (perimetre exact de
   `updatePlanning`).
2. **(Moyen)** Affichage des sessions multi-jours dans une grille
   jour-par-jour (mon-calendrier n'a pas ete concu pour des "barres" multi-
   jours comme FullCalendar dayGrid). Decision UX a trancher par le developer
   (afficher sur le jour de debut uniquement + badge duree, vs. dupliquer
   l'event sur chaque jour couvert).
3. **(Moyen)** Le refactoring "composant partage month-week-grid" touche
   `mon-calendrier` (hors perimetre de fichiers explicitement liste dans le
   WI). Si jugee trop risque, replier sur portage duplique (option 1) +
   PITFALL a proposer pour dette de duplication.
4. **(Faible)** Les warnings de conflit ne sont visibles qu'apres une
   tentative de sauvegarde (le GET initial renvoie toujours
   `warnings: []`) — comportement backend existant, a documenter dans l'UI
   (ex: texte d'aide "les conflits sont detectes a l'enregistrement").
5. **(Faible)** Verifier qu'aucun autre module frontend n'importe
   `@fullcalendar/*` avant de le retirer de `package.json`.

## Open Blockers (a trancher par manager/utilisateur avant impl complete)

- "Planifier un cours" sur jour vide : endpoint de creation manquant — scope
  reduit ou nouveau sous-WI backend ?
- "Suppression d'une session planifiee" : endpoint manquant — meme question.
- "Formateur" dans le formulaire d'edition : pas de notion de formateur par
  session dans le modele actuel (formateurs sont lies au `Cours`, pas a la
  `PromotionCours`). Possible chevauchement avec FULLST-007
  (CoursPlanifie/InscriptionCours) — a clarifier si l'edition de formateur par
  session est requise maintenant ou differee.

## Recall Hints

- Composant grille partage propose :
  `frontend/src/app/shared/components/month-week-grid/`
  (`MonthWeekGridComponent`).
- `PromotionController.toResponse` renvoie toujours `warnings: []` au GET —
  warnings uniquement via `updatePlanning`.
- `STATUT_COLORS` (PLANIFIE/EN_COURS/TERMINE) a reprendre tel quel depuis
  l'ancien `planning.ts`.

## Proposed Rules

- TYPE: PITFALL
  Title: PromotionController.toResponse renvoie toujours warnings=[] au GET
  Scope: backend/.../controller/PromotionController.java,
    frontend planning UI
  Rule: Ne pas s'attendre a recevoir des `warnings` non-vides via
    `GET /api/promotions/{id}` — ils ne sont calcules que par
    `PUT .../planning/{id}` (updatePlanning). Toute UI affichant des
    indicateurs de conflit en lecture doit soit accepter qu'ils soient
    absents au chargement initial, soit (evolution future) demander un
    endpoint dedie de "verification de conflits" en lecture seule.
  Why: Source de confusion potentielle lors de l'implementation du
    calendrier en lecture seule (FULLST-005) — un developer pourrait
    chercher a afficher des warnings au chargement et ne rien trouver.
  How to apply: Documenter dans le composant planning que les warnings
    n'apparaissent qu'apres une action de sauvegarde.
  Evidence: backend/src/main/java/fr/eni/gestionformation/controller/PromotionController.java:76,
    ai_memory/2026-06-11__ROLE-solution-architect__WI-20260611-FULLST-005.md
