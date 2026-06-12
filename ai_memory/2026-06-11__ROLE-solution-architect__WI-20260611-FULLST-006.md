# ROLE NOTE — solution-architect

Work Item: WI-20260611-FULLST-006
Role: solution-architect
Status: READY_FOR_REVIEW
Mode: DEEP
Files Touched: aucun (design only)

## Probleme analyse

`frontend/src/app/features/administration/cours/cours.html` :
1. Colonnes "Formateurs" / "Prerequis" / "Requis par" affichent
   `.badge-list` de badges sans limite — deborde/empile pour un cours avec
   beaucoup de formateurs/prerequis.
2. Modals create/edit utilisent des `formArrayName="formateurIds"` /
   `"prerequisIds"` en checkbox-list exhaustive sur TOUS les formateurs / TOUS
   les autres cours du catalogue (meme anti-pattern que FULLST-001).

## Decisions

### 1. Redesign du tableau — badges tronques + popover

Pour les 3 colonnes concernees (`Formateurs`, `Prerequis`, `Requis par`) :

- Limiter l'affichage a **N = 3** badges visibles (constante locale
  `MAX_BADGES_VISIBLE = 3` dans `cours.ts`).
- Si `liste.length > 3` : afficher les 3 premiers + un badge
  `+{{ liste.length - 3 }} autres` (style `.badge` neutre, ex. `.badge-more`).
- Au clic/hover sur le badge `+X autres` : afficher un popover/tooltip listant
  les elements restants. Implementation simple sans nouvelle dependance :
  - Un `<details>`/`<summary>` natif stylise (accessible par defaut, pas de
    JS supplementaire), OU
  - Un signal local `openPopoverFor: signal<{coursId: number, column: string}
    | null>` + positionnement CSS `position: absolute` relatif a la cellule,
    fermeture au clic exterieur (`(document:click)` via `host` listener —
    attention regle CLAUDE.md : pas de `@HostListener`, utiliser `host: {
    '(document:click)': ... }` dans le decorateur, ou un `cdkOverlay` si
    Angular CDK est deja une dependance — **a verifier** : si CDK n'est pas
    deja installe, ne pas l'ajouter pour ce seul besoin, preferer
    `<details>`).
  - **Recommandation** : `<details><summary>+X autres</summary><div
    class="popover">...</div></details>` — zero JS, accessible
    (clavier + lecteur d'ecran via semantique native), stylable en CSS pur
    (`details[open] summary ~ .popover { display: block; position:
    absolute; }`). Coherent avec la contrainte AXE/WCAG AA du projet
    (CLAUDE.md frontend).
- Tri des badges visibles : garder l'ordre existant (formateurs : ordre de
  retour API ; prerequis/requis-par : idem) — pas de tri supplementaire
  demande.

### 2. Remplacement des checkbox-lists par le composant de selection partage

Reutilisation directe de `EntitySelectorComponent` propose dans
WI-20260611-FULLST-001
(`frontend/src/app/shared/components/entity-selector/entity-selector.{ts,html,scss}`,
selector `app-entity-selector`).

- **Mode** : `'multi-select'` (la selection est soumise au `submit` du
  formulaire Cours, comme actuellement avec les `FormArray<FormControl<boolean>>`
  — pas d'appel API immediat par item).
- **Champ "Formateurs"** (create + edit) :
  - `items` = `formateurs().map(f => ({ id: f.id, label: fullName(f) }))`
  - `selectedIds` = ids des formateurs deja assignes (edit) / vide (create)
  - `selectionChange` -> mettre a jour un signal local
    `selectedFormateurIds: signal<Set<number>>`, lu au submit pour construire
    `formateurIds: number[]`.
- **Champ "Prerequis"** (create + edit) :
  - `items` = `coursList().filter(c => c.id !== editingCours()?.id).map(c =>
    ({ id: c.id, label: c.name }))`
  - Les cours desactives pour cause de cycle (`disabledPrerequisIds`,
    logique `isTransitivePrerequis` deja existante dans `cours.ts`, lignes
    191-219) doivent rester desactives dans le selecteur. Cela necessite que
    `EntitySelectorComponent` accepte un `input<Set<number>>` optionnel
    `disabledIds` — items presents dans la liste mais non selectionnables
    (ligne grisee, checkbox/bouton disabled). **Extension au composant
    generique propose dans FULLST-001** (a ajouter des la premiere
    implementation si FULLST-006 est planifie peu apres, pour eviter un
    second passage sur le composant partage) :
    - `disabledIds: input<Set<number>>` (defaut vide)
    - items avec `disabledIds.has(item.id)` : rendu non interactif + style
      attenue, tooltip optionnel via `title` attribute (ex. "creerait un
      cycle de dependances").
  - `selectionChange` -> `selectedPrerequisIds: signal<Set<number>>`, lu au
    submit -> `coursAdapter.setPrerequis(cours.id, [...ids])`.
- Conserver le message d'aide existant ("Certains cours sont grises car..." —
  `cours.html` ligne 213-216) au-dessus du selecteur si
  `disabledPrerequisIds().size > 0`.
- La logique de calcul (`isTransitivePrerequis`, `disabledPrerequisIds`,
  `editOtherCours`) dans `cours.ts` reste INCHANGEE — seule la presentation
  (checkbox-list -> entity-selector) change.

### 3. Migration des FormArray vers signals

Avec `EntitySelectorComponent` en mode `'multi-select'`, les
`FormArray<FormControl<boolean>>` (`formateurIds`, `prerequisIds`) dans
`createForm`/`editForm` deviennent superflus. Remplacer par deux paires de
signals (`selectedFormateurIds`/`selectedPrerequisIds` pour create, et
equivalents pour edit, ou un seul jeu de signals reset a l'ouverture de
chaque modal). `submitCreate()`/`submitEdit()` lisent ces signals au lieu de
`v.formateurIds`/`v.prerequisIds`. Le reste du `FormGroup` (`name`,
`dureeJours`) reste en Reactive Forms (`ReactiveFormsModule` deja importe,
inchange).

## Plan d'action pour le developer

**Prerequis** : ce WI suppose que `EntitySelectorComponent`
(WI-20260611-FULLST-001) existe deja avec le support `disabledIds` (mode
`'multi-select'`). Si FULLST-001 n'est pas encore implemente/merge au moment
de ce WI, soit attendre, soit implementer le composant partage ici en
premier (coordination manager).

1. `frontend/src/app/shared/components/entity-selector/` : si pas deja fait
   par FULLST-001, ajouter le support `disabledIds: input<Set<number>>` en
   mode `'multi-select'` (sinon, verifier qu'il est deja present).
2. `cours.ts` :
   - Retirer `formateurIds`/`prerequisIds` des `FormGroup` create/edit.
   - Ajouter signals `selectedFormateurIds`/`selectedPrerequisIds` (et
     versions edit si formulaires separes), reset dans
     `openCreateModal()`/`openEditModal()`.
   - Ajouter constante `MAX_BADGES_VISIBLE = 3` + methode utilitaire
     `visibleBadges<T>(list: T[]): T[]` et `hiddenCount<T>(list: T[]): number`
     pour le tableau.
   - `submitCreate`/`submitEdit` : lire les signals au lieu des `FormArray`.
3. `cours.html` :
   - Tableau : remplacer les boucles `@for` des 3 colonnes par
     `visibleBadges(...)` + badge `+X autres` dans un `<details>`.
   - Modals create/edit : remplacer les blocs `formArrayName="formateurIds"`
     et `"prerequisIds"` par `<app-entity-selector mode="multi-select"
     [items]="..." [selectedIds]="..." [disabledIds]="..."
     (selectionChange)="..." />`.
4. `cours.scss` : styles pour `.badge-more`, `details`/`.popover`
   (positionnement absolu, z-index, ombre `--shadow-e2` du design system) —
   attention au budget CSS (PIT-004/PIT-005, fichier deja proche des limites
   selon WI-20260610-BACKEN-008 — verifier la taille compilee apres ajout).
5. `ng build` : verifier budgets CSS et absence de regression sur les chunks
   `cours`.
6. Verification manuelle (`ng serve`) : tableau avec un cours ayant > 3
   formateurs/prerequis -> popover fonctionnel au clavier (focus sur
   `<summary>`, Entree pour ouvrir) ; modals create/edit -> recherche dans le
   selecteur, cours desactives toujours grises avec message d'aide.

## Anti-scope

- Ne pas modifier `CoursService`/`CoursController`/API backend — aucun
  changement de contrat necessaire (`setFormateurs`, `setPrerequis` deja
  existants et suffisants).
- Ne pas toucher a la logique `isTransitivePrerequis`/anti-cycle — seule la
  presentation change.
- Ne pas modifier `promotions.*` ni `promotion-detail.*` (FULLST-001, WI
  separe — mais le composant partage `entity-selector` est une dependance
  commune, a coordonner).

## Risques et points d'attention

1. **(Critique)** Dependance directe sur `EntitySelectorComponent` issu de
   FULLST-001 avec extension `disabledIds` non prevue dans la conception
   initiale de FULLST-001 telle que redigee ici — si FULLST-001 est
   implemente sans `disabledIds`, FULLST-006 devra l'ajouter (modification du
   composant partage). A signaler au manager pour sequencer (FULLST-001
   d'abord, avec `disabledIds` inclus des le depart si possible, ou
   FULLST-006 etend le composant).
2. **(Moyen)** Le popover via `<details>` natif peut avoir un style par
   defaut (triangle `::marker`) a neutraliser en CSS (`summary::-webkit-
   details-marker { display: none }` + `summary { list-style: none }`) pour
   matcher le design system — attention coherence visuelle avec `.badge`
   existant.
3. **(Moyen)** Budget CSS `cours.scss` : deja un point d'attention historique
   sur d'autres fichiers admin (PIT-004/PIT-005). L'ajout de styles popover +
   entity-selector (si son SCSS n'est pas correctement scope) pourrait
   declencher un nouveau warning/erreur de budget. Verifier `ng build` avant
   de considerer le WI termine.
4. **(Faible)** `MAX_BADGES_VISIBLE = 3` est un choix arbitraire — si le
   design final demande un autre nombre, c'est un changement local trivial
   (une constante).
5. **(Faible)** S'assurer que le popover ne deborde pas du tableau /
   viewport sur petits ecrans (`overflow: visible` sur `.table-wrap` peut
   etre necessaire localement pour la cellule active, sans casser le scroll
   horizontal du tableau).

## Recall Hints

- Reutilise `EntitySelectorComponent`
  (`frontend/src/app/shared/components/entity-selector/`) de FULLST-001, mode
  `'multi-select'`, avec extension `disabledIds: input<Set<number>>`.
- Logique anti-cycle existante (`isTransitivePrerequis`,
  `disabledPrerequisIds`, `editOtherCours`) dans `cours.ts` lignes 191-229 —
  a conserver telle quelle.
- Popover recommande : `<details><summary>` natif (zero JS, accessible),
  pas de nouvelle dependance CDK.

## Proposed Rules

- TYPE: CONVENTION
  Title: Troncature de listes de badges dans les tableaux admin
  Scope: frontend/src/app/features/administration/**/*.html (colonnes
    affichant des listes d'entites liees : formateurs, prerequis, eleves, etc.)
  Rule: Toute colonne de tableau affichant une liste d'entites liees limite
    l'affichage a 3 badges + un badge "+X autres" ouvrant un `<details>`
    natif listant le reste, plutot que d'afficher la liste complete en
    `.badge-list`.
  Why: Les listes de badges non bornees debordent/empilent et rendent le
    tableau illisible des qu'une entite a plus de quelques relations
    (constate sur Catalogue de cours, FULLST-006).
  How to apply: Extraire une fonction utilitaire partagee
    `visibleBadges(list, max=3)` / `hiddenCount(list, max=3)` (envisager
    `frontend/src/app/shared/utils/badge-list.ts` si reutilisee dans un
    3e ecran) + pattern `<details><summary>+X autres</summary>...`.
  Evidence: ai_memory/2026-06-11__ROLE-solution-architect__WI-20260611-FULLST-006.md,
    frontend/src/app/features/administration/cours/cours.html
