# Analysis — Frontend Page "Gestion de la promotion" (tabs Cours planifies / Stagiaires + modale Planifier un cours)

Date: 2026-06-11
Mode: DEEP
Work Item: WI-20260611-FULLST-012
Probleme: Concevoir la page `/app/admin/promotions/:id` qui fusionne `promotion-detail` (header + effectifs) et `planning` (calendrier Gantt, SUPERSEDED) en une page unique avec tabs "Cours planifies" / "Stagiaires" et une modale "Planifier un cours" avec bandeau de prerequis, en s'inspirant du design React fourni (PromoDetail / PlanCourseModal / Meta).

## Analyse du probleme

Le design React introduit 3 changements structurels :

1. **Fusion de deux pages** : `promotion-detail` (header + tableau "Effectifs") et `planning` (calendrier Gantt) deviennent une page unique avec tabs `Cours planifies (N)` / `Stagiaires (N)`.
2. **Remplacement du calendrier Gantt par un tableau** "Cours planifies" : colonnes Cours / Periode / Formateur (avatar) / Salle / Inscrits / Actions (crayon). La logique d'edition de session (`editForm`, `sessionWarnings`, `submitSession`) de `planning.ts` reste reutilisable car independante de la grille.
3. **Nouvelle modale "Planifier un cours"** (creation) avec bandeau de statut de prerequis calcule cote frontend a partir de `Cours.prerequis` (deja recursif dans `CoursResponse`, BACKEN-005) compare aux cours deja planifies dans la promotion.

Cause racine du redesign : l'ancienne vue calendrier n'exposait pas de maniere actionnable les notions metier cles (formateur par session, salle, ordre pedagogique/prerequis, inscriptions individuelles vs promo) — le nouveau design les rend visibles et editables dans un tableau.

## Contraintes

- Stack Angular 20 standalone, signals, `OnPush`, Reactive Forms, `@if`/`@for`, `inject()`, pas de `ngClass`/`ngStyle` — patterns deja respectes dans `promotion-detail.ts`/`planning.ts`, a reproduire.
- `EntitySelectorComponent` (CONV-004) a reutiliser pour le tab "Stagiaires" (deja utilise dans `promotion-detail.html`).
- CONV-005 (`<details>/<summary>`) potentiellement applicable pour la liste detaillee des prerequis dans le bandeau ambre.
- DEC-003 : `CoursPlanifie.promotion` nullable, inscriptions individuelles via `InscriptionCours`. Le tab "Stagiaires" (`Promotion.eleves`) reste distinct des "Inscrits" d'un `CoursPlanifie` (`InscritCours[]`, origine `PROMOTION`/`INDIVIDUEL`, FULLST-008/009 DONE) — ne pas confondre.
- **FULLST-011 (formateur+salle sur CoursPlanifie + endpoint update) est maintenant READY_FOR_REVIEW (livree).** Contrat reel a verifier en section 5 contre `ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-011.md`.
- `Promotion.planning: PromotionCours[]` (frontend `promotion.model.ts`) est l'API actuelle exposee par `BasePromotionAdapter.getById`. A etendre cote frontend avec `formateurId`/`formateurNom`/`salle` (FULLST-011 cote backend deja livre).
- Bouton "Inscrire un stagiaire" (EnrollmentForm, FULLST-015) hors scope : prevoir uniquement le hook (bouton + handler vide).
- Regle memoire utilisateur : pas de comptes/section demo (non pertinent ici, mais a garder en tete).

## Options

### Option 1 — Page conteneur + 2 composants tab standalone + 1 composant modale standalone

`PromotionDetailComponent` (conteneur, conserve route/selecteur existants) porte le header/Meta + un signal `activeTab: 'cours' | 'stagiaires'`. Chaque tab est un sous-composant standalone (`cours-planifies-tab.ts` / `stagiaires-tab.ts`) recevant `promotion()` en `input()` et emettant des `output()`. La modale "Planifier un cours" (`plan-course-modal.ts`) est ouverte depuis le conteneur.

- Avantages : respecte "single responsibility" (CLAUDE.md), modale isolee et testable (logique prerequis non triviale), reutilisable.
- Inconvenients : plus de fichiers, plomberie inputs/outputs.
- Effort : moyen.
- Compatibilite stack : totale.

### Option 2 — Page monolithique (tout dans `promotion-detail.ts`/`.html`)

Tabs et modale ajoutes comme blocs `@if` supplementaires dans le composant existant.

- Avantages : pas de nouveaux fichiers, acces direct au contexte.
- Inconvenients : `.html` tres long (header + 2 tabs + 2 modales), viole "Keep components small", logique prerequis melangee avec logique effectifs.
- Effort : faible court terme, dette a moyen terme.
- Compatibilite : compatible mais contraire a la convention.

### Option 3 — Conteneur + sous-routes par tab (lazy-loaded)

- Avantages : lazy loading natif, URL par tab.
- Inconvenients : sur-ingenierie pour 2 tabs legers, complexifie le partage d'etat `promotion()`, change la navigation actuelle.
- Effort : eleve.
- Compatibilite : compatible mais disproportionne.

## Recommandation

**Option 1.** Justification : conforme aux conventions CLAUDE.md (petits composants, single responsibility), isole la logique prerequis non triviale dans son propre composant testable, reutilise directement `editForm`/`sessionWarnings`/`submitSession` de `planning.ts` sans heriter du code de grille calendrier (jete). Aucun changement de routing.

Option 2 ecartee : violerait "single responsibility", `.html` ingerable.
Option 3 ecartee : complexite de routing/etat disproportionnee pour 2 tabs.

**Conditions d'invalidation** : si un futur besoin necessite un deep-link direct vers un tab (ex. email -> "Stagiaires"), des query params (`?tab=stagiaires`) suffisent — la recommandation tient quand meme.

## Plan d'action pour le developer

### 1. Arborescence finale

```
frontend/src/app/features/promotions/promotion-detail/
  promotion-detail.ts          (conteneur, existant - etendu)
  promotion-detail.html         (etendu)
  promotion-detail.scss         (etendu)
  cours-planifies/
    cours-planifies-tab.ts
    cours-planifies-tab.html
    cours-planifies-tab.scss
  stagiaires/
    stagiaires-tab.ts
    stagiaires-tab.html
    stagiaires-tab.scss
  plan-course-modal/
    plan-course-modal.ts
    plan-course-modal.html
    plan-course-modal.scss
```

Selecteurs : `app-promotion-detail` (inchange), `app-cours-planifies-tab`, `app-stagiaires-tab`, `app-plan-course-modal`.

### 2. `promotion-detail.ts` (conteneur)

- Conserve `loadPromotion`, `promotion`, `loading`, `loadError`.
- Ajoute :
  - `activeTab = signal<'cours' | 'stagiaires'>('cours')`.
  - `cursusCours = signal<CoursInCursus[] | null>(null)` charge via `BaseCursusAdapter.getById(promotion.cursusId)` pour le `FilterSelect` de la modale (uniquement si `cursusId !== null`).
  - Source de `Cours.prerequis` recursif : si `BaseCoursAdapter.getAll()` retourne deja `prerequis` recursif pour chaque cours, un seul appel au chargement de page suffit ; sinon charger via `getById(coursId)` a l'ouverture de la modale (lazy).
  - `showPlanCourseModal = signal(false)`, `planCourseEditTarget = signal<PromotionCours | null>(null)` (null = creation, sinon edition via crayon).
  - Hook "Inscrire un stagiaire" : `onOpenEnrollmentForm()` vide, commente `// FULLST-015`.
- Mapping Meta :
  - "Cursus" -> `p.cursusNom` (deja en `pill`).
  - "Periode" -> `p.dateDebut` (+ date fin si dispo).
  - "Stagiaires" -> `p.eleves.length`.
  - "Cours planifies X/Y" -> X = `p.planning.length`, Y = `cursusCours()?.length ?? '?'`.
- Tabs : boutons avec `[class.active]` + `(click) activeTab.set(...)`, libelles `Cours planifies ({{p.planning.length}})` / `Stagiaires ({{p.eleves.length}})`.
- Template : `@if (activeTab()==='cours') { <app-cours-planifies-tab .../> } @else { <app-stagiaires-tab .../> }`.
- **Recommandation** : deplacer entierement `addEleve`/`removeEleve`/`elevesDisponibles`/`showAddEleveModal` dans `stagiaires-tab.ts` (input `promotion`, output `promotionUpdated`, injecte ses propres adapters).

### 3. Mapping design React -> Angular

| Element design | Composant/section Angular | Reutilisation |
|---|---|---|
| `PromoDetail` header (titre, badges) | `promotion-detail.html` `.detail-header` etendu | existant lignes 25-39 |
| `Meta` (Cursus/Periode/Stagiaires/Cours X/Y) | nouveau `.meta-grid` sous le header | nouveau, donnees deja dans `promotion()` |
| Bouton "Planifier un cours" | header conteneur -> `app-plan-course-modal` | nouveau |
| Bouton "Inscrire un stagiaire" | header conteneur, hook vide (FULLST-015) | nouveau |
| Tabs | `.tabs` + signal `activeTab` | nouveau |
| Tableau "Cours planifies" | `cours-planifies-tab.html` `<table class="tbl">` | structure pattern existant lignes 50-82 ; donnees = `PromotionCours[]` etendu (FULLST-011, livre) |
| EmptyState | `@if (planning.length===0) {...}` | pattern existant lignes 60-62 |
| Edition session (crayon) | reprend `editingSession`/`editForm`/`sessionWarnings`/`submitSession`/`closeSessionModal` de `planning.ts` (lignes 128-137, 216-274), etendre avec `formateurId`/`salle` (FULLST-011) | `planning.ts` |
| Tab Stagiaires (grille + badges) | `stagiaires-tab.html` reprend table eleves existante ; voir Risque #2 sur badge Promotion/Unite | `promotion-detail.html` lignes 50-82 |
| Retrait/Ajout stagiaire | conserve tel quel dans `stagiaires-tab.ts` | `promotion-detail.ts` lignes 112-128 + logique addEleve |
| `PlanCourseModal` | `plan-course-modal.ts/.html` (section 4) | nouveau |

### 4. Modale "Planifier un cours"

Inputs/outputs :
- `promotion: input.required<Promotion>()`
- `coursDisponibles: input<Cours[]>()` (cours du cursus, avec `prerequis` recursif)
- `editTarget: input<PromotionCours | null>()`
- `closed: output<void>()`
- `saved: output<{coursId, dateDebut, dateFin, formateurId?, salle?, force: boolean}>()`

Formulaire reactif :
```ts
coursId: FormControl<number | null>     // requis, FilterSelect
dateDebut: FormControl<string>           // requis
dateFin: FormControl<string>             // requis, dateFin >= dateDebut (cf planning.ts:240)
formateurId: FormControl<number | null>  // optionnel, FULLST-011
salle: FormControl<string | null>        // optionnel, FULLST-011
force: FormControl<boolean>              // defaut false, "Planifier malgre tout"
```

Calcul du bandeau prerequis (`computed()`) :
1. Recuperer le `Cours` selectionne (lookup dans `coursDisponibles()` via `coursId()`).
2. `coursPlanifiesIds = new Set(promotion.planning.map(pc => pc.coursId))` — hypothese : "planifie" (peu importe statut) suffit pour considerer un prerequis satisfait dans cette UI (different d'un controle de progression individuel, qui releve plutot de FULLST-015). **A confirmer avec PO.**
3. Aplatir recursivement `cours.prerequis` (dedupliquer par id) -> `prerequisRequis: Cours[]`.
4. Pour chaque prerequis : `satisfait = coursPlanifiesIds.has(p.id)`.
5. `tousSatisfaits = computed(() => prerequisRequis().every(p => p.satisfait))`.

Rendu :
- Vide ou `tousSatisfaits()` -> bandeau **vert** "Tous les prerequis sont satisfaits" (ou "Aucun prerequis").
- Sinon -> bandeau **ambre** "Prerequis non respectes", liste detaillee (icone check vert/croix ambre par prerequis, etat Planifie/Non planifie), pattern `<details>/<summary>` (CONV-005) si liste longue, + checkbox "Planifier malgre tout" (`force`).
- **Recommandation** : ne pas bloquer la soumission cote frontend (aucun controle de prerequis backend identifie pour `CoursPlanifie` dans FULLST-007/008/011) — bandeau purement informatif, `force` documente en commentaire comme hypothese, payload extensible si un futur endpoint l'exploite.

Soumission :
- Creation : nouvel endpoint POST de creation de `CoursPlanifie` pour la promotion — **non identifie dans le contrat actuel, voir Risque #1 (BLOQUANT POTENTIEL)**.
- Edition : reprend `submitSession`/`updatePlanning` (etendu FULLST-011).

### 5. Contrat API attendu / livre par FULLST-011

FULLST-011 livree (READY_FOR_REVIEW). Contrat reel d'apres `ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-011.md` :

- `PUT /api/promotions/{id}/planning/{coursPlanifieId}` accepte `formateurId` (Long, nullable) et `salle` (String, nullable) en plus des dates — semantique full-overwrite.
- `CoursPlanifieResponse` expose `formateurId`, `formateurNom` ("Prenom Nom"), `salle`.
- Detection de conflit formateur par session : warning non bloquant (coherent avec PIT-008).

A faire cote frontend (FULLST-012) :
```ts
// promotion.model.ts — PromotionCours etendu
formateurId: number | null;
formateurNom: string | null;
salle: string | null;

// PlanningUpdateRequest etendu (adapter HTTP)
export interface PlanningUpdateRequest {
  dateDebut: string;
  dateFin: string;
  formateurId?: number | null;
  salle?: string | null;
}
```

`BasePromotionAdapter.updatePlanning(promotionId, promotionCoursId, req)` reste la methode utilisee.

**Endpoint de creation de `CoursPlanifie` pour une promotion non identifie** dans FULLST-008/011 — blocage potentiel pour le mode creation de la modale (voir Risque #1).

### 6. Plan de retrait/fusion de la page planning (FULLST-005/010)

- Route `admin/promotions/:id/planning` (app.routes.ts lignes 88-90, `PlanningComponent`) : **a supprimer**. Verifier (`grep '/planning'`) qu'aucun lien externe n'y pointe avant suppression definitive ; sinon `redirectTo` vers `/app/admin/promotions/:id`.
- Bouton "Voir le planning" (`promotion-detail.html` lignes 12-17) : **a supprimer**.
- `frontend/src/app/features/administration/promotions/planning/*` : **a supprimer** apres extraction :
  - `editForm`, `sessionWarnings`, `submitSession`, `closeSessionModal`, `openSession` (renomme) -> `cours-planifies-tab.ts` / `plan-course-modal.ts`.
  - Code de grille calendrier (`monthGrid`, `weekDays`, `getBarsForWeek`, `navTitle`, `navigatePrev/Next`, `view`, `referenceDate`, imports `date-fns`) : **jete**, ne pas migrer.
  - `STATUT_COLORS` : potentiellement reutilisable pour badges de statut dans le tableau.
- `PromotionCours` (modele) : conserve, etendu par FULLST-011 sans renommage.

## Risques et points d'attention

1. **[BLOQUANT POTENTIEL] Endpoint de creation de `CoursPlanifie` pour une promotion absent du contrat actuel.** Signal : aucune methode `BasePromotionAdapter`/endpoint backend pour `POST` une nouvelle session identifiee dans FULLST-008/011. Mitigation : si confirme absent, le developer remonte au Manager pour ouvrir une WI backend complementaire (extension FULLST-011 ou nouvelle WI) ; le mode edition (sessions existantes) peut etre livre independamment.

2. **Ambiguite badge "Promotion/Unite" sur le tab Stagiaires.** Signal : `Promotion.eleves` ne contient que des eleves `origine PROMOTION` par construction ; le design suggere un badge Promotion/Unite. Mitigation : clarifier avec Manager/PO si le tab doit aussi lister des eleves inscrits individuellement (`InscriptionCours`) a des `CoursPlanifie` de cette promotion sans etre membres (necessiterait un nouvel endpoint d'agregation, hors scope FULLST-008). A defaut, tous les elements de `Promotion.eleves` portent le badge "Promotion", pas de badge "Unite" dans ce tab.

3. **Dependance FULLST-011 livree, integration frontend a faire.** Signal : `formateurId`/`salle`/avatar absents du modele frontend actuel (`promotion.model.ts`). Mitigation : etendre le modele et l'adapter HTTP/mock selon le contrat de la section 5 ; tableau "Cours planifies" affiche placeholders ("Non assigne"/`—`) si `null`.

4. **Source de `Cours.prerequis` recursif et volumetrie `getAll()`.** Signal : a verifier au codage si `BaseCoursAdapter.getAll()`/`getById()` retourne bien `prerequis` peuple recursivement (BACKEN-005) en environnement reel. Mitigation : si volumetrie trop grande pour `getAll()` au chargement de page, charger via `getById(coursId)` a l'ouverture de la modale.

5. **Comportement "Planifier malgre tout" non bloquant — hypothese a valider.** Signal : si un retour utilisateur impose un blocage sans cocher la case, ou un flag `force` cote backend. Mitigation : documenter l'hypothese en commentaire dans `plan-course-modal.ts`, garder `force: FormControl<boolean>` extensible dans le payload.

## Rappels pour le developer (anti-patterns a eviter)

- Ne pas migrer le code de grille calendrier (`monthGrid`, `weekDays`, `getBarsForWeek`, imports `date-fns`) — jete avec la suppression de la page planning.
- Ne pas dupliquer/reecrire `addEleve`/`removeEleve`/`EntitySelectorComponent` — les deplacer telles quelles dans `stagiaires-tab.ts`.
- Ne pas utiliser `ngClass`/`ngStyle` pour badges/statuts — `class`/`style` bindings (CLAUDE.md).
- Ne pas confondre `Promotion.eleves` (effectif promotion) avec `InscritCours[]` (inscrits combines d'un `CoursPlanifie`, modele distinct `inscription.model.ts`).
- Ne pas implementer le bouton "Inscrire un stagiaire" — hook vide uniquement (FULLST-015).
