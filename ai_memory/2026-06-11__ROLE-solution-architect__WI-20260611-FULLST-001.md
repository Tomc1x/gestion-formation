# ROLE NOTE — solution-architect

Work Item: WI-20260611-FULLST-001
Role: solution-architect
Status: READY_FOR_REVIEW
Mode: DEEP
Files Touched: aucun (design only)

## Probleme analyse

Le modal create/edit Promotion (`frontend/src/app/features/administration/promotions/promotions.html`)
melange formulaire de metadonnees (nom, cursus, dateDebut, rythme) et une
checkbox-list exhaustive de TOUS les eleves (role ETUDIANT), sans recherche
ni pagination. Decision deja actee : separer ces deux responsabilites.

## Decisions

### 1. Modal create/edit Promotion -> metadonnees uniquement

`promotions.ts` / `promotions.html` :
- Supprimer `eleveIds: FormArray<FormControl<boolean>>` du `PromotionFormGroup`
  (champ `name`, `cursusId`, `dateDebut`, `rythmeActif`, `semainesCentre`,
  `semainesEntreprise` uniquement).
- Supprimer `resetEleveIds()`, le bloc `formArrayName="eleveIds"` dans les deux
  modales (create/edit), et l'injection de `BaseUserAdminAdapter` +
  signal `eleves` si plus utilises ailleurs dans ce composant.
- `buildRequest()` : `eleveIds` n'est plus construit depuis le formulaire.
  - Pour la **creation** : `PromotionRequest.eleveIds` peut rester `[]` (une
    promotion vient d'etre creee, aucun eleve assigne pour l'instant — on les
    ajoute ensuite via promotion-detail).
  - Pour la **modification** (`submitEdit`) : NE PAS envoyer `eleveIds:
    undefined` au risque de declencher la semantique full-replace cote
    backend (`PromotionService.update`, lignes 107-117) qui desaffecterait
    TOUS les eleves actuels. Deux options :
    - (a) preferee : rendre `eleveIds` optionnel dans `PromotionRequest`
      (deja `eleveIds: number[]` obligatoire actuellement — voir Backend
      ci-dessous) et ne pas l'inclure dans le payload PUT depuis ce modal ;
      `PromotionService.update` ne touche aux eleves QUE si
      `request.getEleveIds() != null` (deja le cas, ligne 107 :
      `if (request.getEleveIds() != null)`). Donc cote frontend il suffit de
      ne pas serialiser le champ (ou l'envoyer `null`/omis) — verifier que
      l'adapter HTTP n'impose pas un defaut `[]`.
    - (b) repli si (a) trop intrusif : continuer a envoyer la liste actuelle
      des eleves de la promotion (`promotion.eleves.map(e => e.id)`) inchangee
      depuis ce formulaire, pour preserver l'effectif tel quel. Moins propre
      (couplage cache) mais zero risque de regression silencieuse.
  - **Recommandation** : option (a). C'est la plus propre et le backend la
    supporte deja (le `if (request.getEleveIds() != null)` existe). Le
    `PromotionRequest` DTO backend (`Long cursusId`, etc.) doit juste accepter
    `eleveIds` absent/null en JSON (Jackson le fait nativement pour un champ
    `List<Long>` sans annotation `@NotNull`).
- Modal create : conserve la generation auto du planning
  (`PlanificationService.genererPlanning`), inchangee — aucune dependance aux
  eleves dans cette generation.

### 2. Page promotion-detail — structure des sections

Fichier `frontend/src/app/features/promotions/promotion-detail/*` (actuellement
un placeholder vide). Route a verifier/ajouter dans `app.routes.ts` —
probablement `/app/admin/promotions/:id` (a distinguer de
`/app/admin/promotions/:id/planning` qui existe deja pour FULLST-005).

Structure proposee (page avec `OnInit`, charge `Promotion` via
`BasePromotionAdapter.getById(id)`) :

1. **Header** : nom de la promotion, cursus, date de debut, rythme (résumé en
   pills), boutons "Modifier" (ouvre le modal metadonnees, reutilisable depuis
   `promotions.ts` ou duplique localement — voir Open Blockers) et "Voir le
   planning" (lien vers la route planning existante).
2. **Section "Effectifs"** (coeur du WI) :
   - Tableau des eleves actuellement dans la promotion (`promotion.eleves`),
     colonnes : nom, prenom, (email si disponible via `UserAdmin`), action
     "Retirer" par ligne.
   - Bouton "Ajouter des eleves" qui ouvre le composant de selection partage
     (cf. section 3) en mode "ajout" : recherche + filtre + pagination sur la
     liste des eleves NON encore dans la promotion (role ETUDIANT, et
     idealement filtrables par "disponibilite" = sans promotion actuelle, et
     par cursus si pertinent — voir filtres ci-dessous).
   - Le retrait d'un eleve et l'ajout se font via mise a jour incrementale
     (pas en passant par le full-replace `PUT /api/promotions/{id}` avec
     `eleveIds` complet, pour eviter tout risque sur le planning) — voir
     section 4 (endpoints).
3. **Section "Planning"** (optionnelle ici, ou simple lien vers la page
   planning existante — eviter de dupliquer FULLST-005).

### 3. Composant de selection d'eleve reutilisable

**Nom propose** : `EleveSelectorComponent` (generique au-dela des eleves —
voir reuse FULLST-006).

**Emplacement propose** :
`frontend/src/app/shared/components/entity-selector/entity-selector.{ts,html,scss}`

Conception generique pour etre reutilise par FULLST-001 (eleves) ET FULLST-006
(formateurs/prerequis) :

- Selector : `app-entity-selector`
- `input()` :
  - `items: input.required<{ id: number; label: string; sublabel?: string }[]>`
    (le composant appelant mappe ses entites — eleves/formateurs/cours — vers
    cette forme generique avant de les passer)
  - `selectedIds: input<Set<number>>` (ids deja selectionnes/affectes,
    affiches differemment ou exclus selon `mode`)
  - `mode: input<'add' | 'multi-select'>` — `'add'` = liste d'items
    disponibles avec bouton "Ajouter" par ligne (cas promotion-detail,
    ajout incremental) ; `'multi-select'` = checkboxes pour selection en bloc
    (cas formulaire Cours de FULLST-006, ou la selection est soumise au submit
    du formulaire parent).
  - `pageSize: input<number>` (defaut 10)
  - `placeholder: input<string>` (texte de recherche, ex. "Rechercher un
    eleve...")
- `output()` :
  - `add: output<number>()` (mode 'add' : id de l'item ajoute — le parent
    appelle l'API et met a jour son etat)
  - `selectionChange: output<Set<number>>()` (mode 'multi-select' : etat de
    selection courant, le parent le lit au submit)
- Comportement interne :
  - signal `searchTerm`, `currentPage`
  - `computed` : items filtres par `searchTerm` (sur `label`, insensible a la
    casse/accents) puis pagines (`pageSize`)
  - template : input recherche + liste/table paginée + controles pagination
    (prev/next + indicateur "page X / Y")
- Filtres additionnels (cursus / disponibilite pour FULLST-001) : NE PAS les
  integrer dans le composant generique — ce sont des filtres metier
  specifiques aux eleves. Le composant parent (`promotion-detail.ts`) calcule
  la liste `items` deja filtree (par cursus de l'eleve courant / disponibilite
  = `promotionId == null`) AVANT de la passer au selecteur generique. Le
  selecteur generique ne gere que recherche texte + pagination, pas les
  filtres metier (separation claire des responsabilites, reutilisable sans
  porter de logique "eleve" ou "cours").
- Filtres metier dans `promotion-detail.ts` (au-dessus du selecteur) :
  - Toggle/select "Disponibilite" : Tous / Sans promotion uniquement (filtre
    sur `UserAdmin` etendu — voir Open Blockers, le modele `UserAdmin` actuel
    n'expose pas `promotionId`)
  - Select "Cursus" : si l'eleve a un cursus rattache via sa promotion
    actuelle (information potentiellement absente du DTO `UserAdmin` —
    a verifier, voir Open Blockers).

### 4. Endpoints API necessaires

Etat actuel (`PromotionController` / `PromotionService`) :
- `PUT /api/promotions/{id}` avec `PromotionRequest.eleveIds` = full-replace
  (toute la liste). Existe deja, fonctionne, mais granularite "tout ou rien" —
  pas ideal pour ajout/retrait unitaire depuis promotion-detail (race
  conditions si deux onglets, et UX moins reactive).

Deux options :

**Option A — reutiliser le full-replace existant (effort faible)**
`promotion-detail.ts` maintient `promotion.eleves` en local (signal), et a
chaque ajout/retrait appelle `PUT /api/promotions/{id}` avec
`PromotionRequest` complet (metadonnees actuelles + nouvelle liste
`eleveIds`). Inconvenient : il faut reconstruire tout le `PromotionRequest`
(cursusId, dateDebut, rythme) depuis la `Promotion` chargee — facile vu que
`PromotionResponse` contient deja tout. Le risque de
`cursusChange || dateDebutChange` regenerant le planning (lignes 119-123 de
`PromotionService.update`) est nul tant que cursusId/dateDebut renvoyes sont
identiques a l'existant.

**Option B — endpoints dedies add/remove eleve (effort moyen)**
- `POST /api/promotions/{id}/eleves/{eleveId}` -> ajoute un eleve (set
  `User.promotion`), retourne `PromotionResponse` ou `EleveInfo`.
- `DELETE /api/promotions/{id}/eleves/{eleveId}` -> retire (set
  `User.promotion = null`).
Plus propre semantiquement, evite tout risque de toucher au planning, et
exprime l'intention exacte. Necessite nouvelles methodes
`PromotionService.addEleve(promotionId, eleveId)` /
`removeEleve(promotionId, eleveId)` + entrees controller + verif role
ETUDIANT (meme garde que `assignFormateurs` pour `CoursService`, cf.
WI-20260608-BACKEN-003).

**Recommandation** : Option B. Le cout est faible (deux methodes service +
deux endpoints, suivant exactement le pattern deja en place dans
`PromotionService`/`PromotionController`), et elle elimine toute ambiguite sur
le full-replace + le risque de regenerer le planning par effet de bord. Elle
documente aussi explicitement l'intention "ajout/retrait individuel" dans
l'API, utile si un futur écran (ex. vue eleve) a besoin du meme contrat.

Pour la liste "eleves disponibles" (non affectes a une promotion), deux
sous-options :
- Reutiliser `BaseUserAdminAdapter.getAll()` (deja utilise par `promotions.ts`)
  et filtrer cote front sur `role === 'ETUDIANT'` + `promotionId == null` —
  necessite que `UserAdmin` expose `promotionId` (a verifier/ajouter dans
  `UserAdminResponse` backend si absent).
- Ou nouvel endpoint `GET /api/admin/users?role=ETUDIANT&disponible=true` —
  plus efficace si la liste des eleves devient grande, mais effort plus eleve
  et hors scope immediat (pagination cote serveur n'est pas demandee
  explicitement). Pour ce WI, filtrage cote front sur `getAll()` est
  suffisant (le volume d'eleves reste raisonnable pour un institut de
  formation) — a revisiter si performance devient un probleme (cf. risque
  ci-dessous).

## Plan d'action pour le developer

1. Verifier/ajouter le champ `promotionId: number | null` dans
   `UserAdmin`/`UserAdminResponse` (backend `UserAdminController`/DTO +
   frontend `user.model.ts`) si absent — necessaire pour le filtre
   "disponibilite".
2. Backend : ajouter `PromotionService.addEleve(promotionId, eleveId)` /
   `removeEleve(promotionId, eleveId)` (verifier role ETUDIANT, lever
   `UserNotFoundException`/`PromotionNotFoundException` si besoin) +
   `PromotionController` : `POST /api/promotions/{id}/eleves/{eleveId}` et
   `DELETE /api/promotions/{id}/eleves/{eleveId}`. SecurityConfig :
   `REFERENTE_ADMINISTRATIVE` (meme regle que les autres writes promotions).
3. Backend : verifier que `PromotionRequest.eleveIds` peut etre `null`/absent
   en JSON sans casser la validation (pas de `@NotNull` sur ce champ) — sinon
   le rendre nullable.
4. Frontend `core/adapters/promotion.adapter.ts` : ajouter
   `addEleve(promotionId, eleveId)` / `removeEleve(promotionId, eleveId)` dans
   `BasePromotionAdapter` + implementations Http/Mock.
5. Frontend : creer `frontend/src/app/shared/components/entity-selector/`
   (composant generique decrit en section 3).
6. Frontend `promotions.ts`/`.html` : retirer le bloc eleves du
   `PromotionFormGroup` et des deux modales (create/edit), ajuster
   `buildRequest()` pour omettre `eleveIds` en update.
7. Frontend `promotion-detail.ts`/`.html` : implementer la page (sections
   header / effectifs / lien planning), integrer `EntitySelectorComponent` en
   mode `'add'` pour l'ajout d'eleves, tableau + bouton "Retirer" pour la
   liste actuelle.
8. Ajouter/verifier la route `/app/admin/promotions/:id` dans
   `app.routes.ts` (roleGuard REFERENTE_ADMINISTRATIVE, coherent avec
   PIT-006 — sync sidebar si entree de menu ajoutee).
9. Tests : `./gradlew test` (nouveaux tests `PromotionServiceTest` pour
   addEleve/removeEleve), `ng build` (verifier budgets CSS, cf PIT-004/PIT-005
   — le nouveau composant entity-selector aura son propre SCSS, attention
   au budget si import partage).

## Anti-scope

- Ne pas toucher a `PlanificationService` ni a la generation du planning.
- Ne pas modifier la page `planning/*` (sujet de FULLST-005, en parallele).
- Ne pas implementer ici le composant generique de maniere couplee aux
  formateurs/prerequis de FULLST-006 — le concevoir generique des le depart
  (section 3) mais l'integration FULLST-006 reste un WI separe.

## Risques et points d'attention

1. **(Critique)** Si l'option (a) pour `eleveIds` omis n'est pas geree
   correctement cote Jackson/validation backend, un PUT sans `eleveIds`
   pourrait soit echouer (400 si `@NotNull`), soit (pire) etre interprete
   comme `[]` par un defaut Lombok/Jackson et desaffecter tous les eleves.
   Mitigation : test backend explicite "PUT promotion sans champ eleveIds ->
   eleves inchanges".
2. **(Moyen)** `UserAdmin`/`UserAdminResponse` n'expose peut-etre pas
   `promotionId` — bloquant pour le filtre "disponibilite". Verifier en
   premier (etape 1 du plan) avant de concevoir l'UI des filtres.
3. **(Moyen)** Volume d'eleves : filtrage cote front via `getAll()` peut
   devenir lent si la base d'eleves grossit fortement. Acceptable pour le
   contexte ENI actuel, a documenter comme dette si pagination serveur
   devient necessaire.
4. **(Faible)** Reutilisation du modal "Modifier metadonnees" entre
   `promotions.ts` (liste) et `promotion-detail.ts` (detail) : eviter la
   duplication du `PromotionFormGroup` — envisager un composant modal
   partage si les deux ecrans doivent l'ouvrir, sinon dupliquer
   ponctuellement (PIT-005 deja documente sur la duplication SCSS modal/
   formulaire — ne pas l'aggraver).
5. **(Faible)** Respecter CONV-001 (adapter pattern) pour les nouvelles
   methodes `addEleve`/`removeEleve`.

## Recall Hints

- Composant partage : `frontend/src/app/shared/components/entity-selector/`
  (`EntitySelectorComponent`, `app-entity-selector`) — utilise aussi par
  WI-20260611-FULLST-006.
- Endpoints proposes : `POST`/`DELETE /api/promotions/{id}/eleves/{eleveId}`.
- Verifier `UserAdmin.promotionId` avant de concevoir les filtres
  "disponibilite".

## Proposed Rules

- TYPE: CONVENTION
  Title: Composant generique de selection avec recherche/pagination
  Scope: frontend/src/app/shared/components/entity-selector/*, tout formulaire
    remplaçant une checkbox-list exhaustive (eleves, formateurs, prerequis)
  Rule: Toute nouvelle UI de selection multiple sur une liste potentiellement
    longue (> ~15 items) doit utiliser `EntitySelectorComponent`
    (recherche texte + pagination), pas une checkbox-list exhaustive.
  Why: Anti-pattern recurrent identifie sur promotions (FULLST-001) et
    catalogue de cours (FULLST-006) — checkbox-lists exhaustives non
    filtrables, illisibles a l'echelle.
  How to apply: Mapper les entites metier vers `{ id, label, sublabel? }`,
    choisir `mode: 'add' | 'multi-select'` selon que la selection declenche un
    appel API immediat ou est soumise au submit d'un formulaire parent.
  Evidence: ai_memory/2026-06-11__ROLE-solution-architect__WI-20260611-FULLST-001.md,
    ai_memory/2026-06-11__ROLE-solution-architect__WI-20260611-FULLST-006.md
