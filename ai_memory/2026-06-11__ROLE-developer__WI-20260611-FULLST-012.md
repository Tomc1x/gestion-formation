# WI-20260611-FULLST-012 — Frontend: Page "Gestion de la promotion" (tabs Cours planifiés / Stagiaires + modale Planifier un cours)

## Work Item
WI-20260611-FULLST-012

## Role
developer

## Status
READY_FOR_REVIEW

## Scope
Implémenter la page `/app/admin/promotions/:id` selon le design Option 1 de
`ai_doc/ANALYSIS__WI-20260611-FULLST-012__gestion-promotion.md` : conteneur
`promotion-detail` étendu (header + Meta + tabs) + `cours-planifies-tab` +
`stagiaires-tab` + `plan-course-modal`. Retrait de la route/page `planning`
(SUPERSEDED par FULLST-010/012).

## Files Touched
- `frontend/src/app/core/models/promotion.model.ts` — `PromotionCours` étendu
  (`formateurId`, `formateurNom`, `salle`), `PlanningUpdateRequest` étendu
  (`formateurId?`, `salle?`), conformément au contrat FULLST-011.
- `frontend/src/app/core/adapters/promotion-mock.ts` — données mock enrichies
  (`formateurId/formateurNom/salle: null`), `updatePlanning` gère désormais
  `formateurId`/`salle` (lookup `MOCK_FORMATEURS`).
- `frontend/src/app/features/promotions/promotion-detail/promotion-detail.ts` —
  conteneur étendu : `activeTab` (signal), Meta blocks (Cursus/Période/Stagiaires/
  Cours planifiés X/Y), chargement `coursDisponibles` (catalogue complet via
  `BaseCoursAdapter.getAll()`, filtré par les ids du cursus — voir Décision
  Risque #4), chargement `formateurs` (via `BaseUserAdminAdapter`, rôle
  FORMATEUR), boutons "Planifier un cours" / "Inscrire un stagiaire" (hook vide
  `// FULLST-015`), wiring modale `plan-course-modal`.
- `frontend/src/app/features/promotions/promotion-detail/promotion-detail.html` —
  header avec Meta grid, tabs, rendu conditionnel des deux tabs, modale
  conditionnelle. Bouton "Voir le planning" supprimé.
- `frontend/src/app/features/promotions/promotion-detail/promotion-detail.scss` —
  styles `.meta-grid`, `.tabs`/`.tab`, `.page__actions`, `.detail-header` en
  colonne.
- **Nouveau** `frontend/src/app/features/promotions/promotion-detail/cours-planifies/`
  (`cours-planifies-tab.ts/.html/.scss`) — tableau Cours/Période/Formateur/Salle/
  Inscrits/Actions, EmptyState, modale d'édition de session reprenant
  `editForm`/`sessionWarnings`/`submitSession`/`closeSessionModal` de
  l'ancien `planning.ts`, étendue avec `formateurId` (select chargé via
  `BaseUserAdminAdapter`, rôle FORMATEUR) et `salle` (texte). Exporte
  `FormateurOption` (réutilisé par le conteneur et la modale).
- **Nouveau** `frontend/src/app/features/promotions/promotion-detail/stagiaires/`
  (`stagiaires-tab.ts/.html/.scss`) — reprend `addEleve`/`removeEleve`/
  `elevesDisponibles`/`showAddEleveModal`/`EntitySelectorComponent` de l'ancien
  `promotion-detail.ts`, en `input(promotion)`/`output(promotionUpdated)`.
  Badge "Promotion" uniquement sur chaque ligne (Risque #2, voir Décisions).
- **Nouveau** `frontend/src/app/features/promotions/promotion-detail/plan-course-modal/`
  (`plan-course-modal.ts/.html/.scss`) — formulaire réactif (coursId, dateDebut,
  dateFin, formateurId, salle, force), bandeau prérequis vert/ambre via
  `computed()` (aplatissement récursif dédupliqué de `Cours.prerequis` vs
  `promotion.planning.map(pc => pc.coursId)`), `<details>/<summary>` (CONV-005)
  pour le détail des prérequis non satisfaits, checkbox "Planifier malgré tout".
  Bouton de soumission désactivé en mode création (Risque #1, voir Décisions).
- `frontend/src/app/app.routes.ts` — route `admin/promotions/:id/planning`
  supprimée (vérifié : aucun autre lien `routerLink`/`navigate` ne pointe vers
  `/planning` en dehors de l'ancienne page elle-même).
- **Supprimé** `frontend/src/app/features/administration/promotions/planning/`
  (planning.ts/.html/.scss) — code de grille calendrier (`monthGrid`, `weekDays`,
  `getBarsForWeek`, `navTitle`, navigation, imports `date-fns`) jeté comme prévu ;
  `editForm`/`sessionWarnings`/`submitSession`/`closeSessionModal` extraits dans
  `cours-planifies-tab.ts`.

## Evidence
- `cd frontend && npx ng build` → **PASS** (4.9s). Chunk `promotion-detail`
  33.12 kB / 7.31 kB transfer. Seuls warnings restants (pré-existants, hors
  scope) : `register.scss` (4.95 kB / budget 4 kB) et `utilisateurs.scss`
  (9.09 kB / budget 4 kB).
- Vérification visuelle chrome-devtools : **non réalisée**. `ng serve` lancé
  sur :4200, mais `app.config.ts` fournit les adapters HTTP réels (pas mock)
  pour `BasePromotionAdapter`/`BaseCoursAdapter`/etc., et la route `/app/...`
  est protégée par `authGuard`. Aucun compte de démonstration disponible
  (règle mémoire utilisateur "pas de comptes/section démo") et pas
  d'identifiants fournis pour ce WI → impossible de naviguer authentifié vers
  `/app/admin/promotions/:id` dans cette session. Recommandation : vérification
  visuelle à faire par l'utilisateur ou via un compte REF existant.

## Decisions

### Risque #1 — Endpoint de création de CoursPlanifie
Confirmé absent : `PromotionController` n'expose que
`GET/POST/PUT/DELETE /api/promotions[...]`, `POST/DELETE /{id}/eleves/{eleveId}`
et `PUT /{id}/planning/{coursPlanifieId}` — aucun `POST` de création de
`CoursPlanifie`. Décision (conforme à la mitigation de l'ANALYSIS) :
- `plan-course-modal` est entièrement implémenté côté UI (formulaire réactif,
  bandeau prérequis, sélection cours/formateur/salle/dates, checkbox "force"),
  ouvert par le bouton "Planifier un cours" du conteneur.
- Le bouton de soumission est **désactivé** (`[disabled]="!editTarget()"`) et
  affiche "Planifier (indisponible)" tant qu'aucun `editTarget` n'est fourni
  (création). Un message d'information explique pourquoi.
- `onPlanCourseSaved()` dans le conteneur ne fait rien si `editTarget()` est
  `null` (no-op documenté en commentaire).
- Le mode édition de session existante est livré séparément et fonctionnel via
  l'modale inline de `cours-planifies-tab` (pencil icon → édition dates +
  formateur + salle via `PUT /planning/{id}`, pattern repris de `planning.ts`).
  `plan-course-modal` n'est donc actuellement utilisé qu'en mode création
  (bloqué) — pas de doublon d'UI d'édition.
- **Action de suivi recommandée (à ne pas créer par moi)** : nouvelle WI
  backend "POST /api/promotions/{id}/planning — création d'un CoursPlanifie
  pour une promotion" (extension de `PromotionService`/`PromotionController`,
  réutilisant la validation prérequis/conflits déjà en place sur `updatePlanning`).
  Une fois livrée, brancher `plan-course-modal` (mode création) sur ce nouvel
  endpoint et activer le bouton de soumission.

### Risque #2 — Badge Promotion/Unité (tab Stagiaires)
Appliqué le fallback documenté dans l'ANALYSIS : chaque ligne de
`stagiaires-tab` affiche un badge `<span class="pill">Promotion</span>` fixe
(tous les éléments de `Promotion.eleves` sont par construction `origine
PROMOTION`). Pas de badge "Unité" — nécessiterait un endpoint d'agrégation
hors scope (FULLST-008/009).

### Risque #4 — Source de `Cours.prerequis` récursif
`CoursInCursus` (modèle `cursus.model.ts`, retourné par
`BaseCursusAdapter.getAll()`) ne contient que `{id, name, ordre, formateurs}`,
sans `prerequis`. Décision : charger le catalogue complet via
`BaseCoursAdapter.getAll()` (qui retourne `Cours[]` avec `prerequis` récursif,
BACKEN-005) et filtrer par les ids présents dans `cursus.cours`. Un seul appel
supplémentaire au chargement de la page (volumétrie jugée acceptable, catalogue
global).

### Formateurs disponibles
`FormateurOption` (id + nom complet) chargé via `BaseUserAdminAdapter.getAll()`
filtré sur `role === 'FORMATEUR'`, dans `cours-planifies-tab` (pour son select
inline) et dans le conteneur `promotion-detail` (passé à `plan-course-modal`).
Petite duplication d'appel HTTP acceptée (pattern simple, cohérent avec le
reste du code qui n'a pas de cache partagé pour les listes d'utilisateurs).

## Open Blockers
Aucun blocage pour la livraison de ce WI. Le mode création de
`plan-course-modal` reste fonctionnellement désactivé jusqu'à la WI backend de
suivi (voir Risque #1 ci-dessus).

## Next Actions
- Manager : ouvrir une WI backend de suivi pour
  `POST /api/promotions/{id}/planning` (création CoursPlanifie), cf. Risque #1.
- Vérification visuelle (chrome-devtools ou manuelle) à faire avec un compte
  REF authentifié sur `/app/admin/promotions/:id`.
- FULLST-015 (EnrollmentForm) viendra remplacer le hook vide
  `onOpenEnrollmentForm()`.

## Recall Hints
- `cours-planifies-tab.ts` exporte `FormateurOption` — réutilisé par
  `plan-course-modal.ts` et `promotion-detail.ts` pour éviter une duplication
  d'interface.
- `plan-course-modal` est actif uniquement en mode édition pour le bouton
  submit ; en mode création le formulaire est navigable mais non soumissible
  (cf. Risque #1).
- Page `planning` (route + dossier `features/administration/promotions/planning/`)
  entièrement supprimée — toute référence future doit passer par
  `promotion-detail` + `cours-planifies-tab`.

## Proposed Rules
- TYPE: DECISION
  Title: Catalogue Cours complet comme source de vérité pour Cours.prerequis récursif
  Scope: frontend/src/app/features/promotions/promotion-detail/, tout composant ayant besoin de Cours.prerequis pour un cursus donné
  Rule: Quand un composant a besoin de `Cours.prerequis` (récursif, BACKEN-005) pour les cours d'un cursus, charger `BaseCoursAdapter.getAll()` (catalogue complet) et filtrer par les ids de `Cursus.cours` plutôt que d'étendre `CoursInCursus`.
  Why: `CoursInCursus` (cursus.model.ts) ne porte pas `prerequis` ; étendre ce DTO casserait potentiellement d'autres consommateurs (page Cursus, BACKEN-007). Le catalogue global est déjà la source de vérité (DEC du WI BACKEN-003).
  How to apply: `cursusAdapter.getAll()` → trouver le cursus → `coursAdapter.getAll()` → `.filter(c => coursIds.has(c.id))`.
  Evidence: frontend/src/app/features/promotions/promotion-detail/promotion-detail.ts (loadCursusCours), ai_doc/ANALYSIS__WI-20260611-FULLST-012__gestion-promotion.md Risque #4.
