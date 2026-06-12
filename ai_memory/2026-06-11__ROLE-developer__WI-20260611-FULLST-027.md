# WI-20260611-FULLST-027

## Role
developer

## Status
READY_FOR_REVIEW

## Scope
Extraction de la gestion des cours d'un cursus (liste ordonnee, drag&drop, monter/descendre,
ajouter/retirer) depuis la page liste `admin/cursus` vers une nouvelle page de detail
`admin/cursus/:id`, avec ajout d'un bloc d'alertes "prerequis mal ordonne" et bouton "Corriger".
La page liste `cursus.ts/html` n'a pas ete modifiee (pas de regression).

## Files Touched
- `frontend/src/app/core/utils/cursus-alerts.util.ts` (NOUVEAU) — fonction pure
  `computeCursusPrereqAlerts(cours: Cours[]): CursusPrereqAlert[]` + helper exporte
  `transitivePrerequis`. Pas de dependance Angular/signals : reutilisable par
  WI-20260611-FULLST-028 pour un badge "N alerte(s)" sur la page liste.
- `frontend/src/app/features/administration/cursus-detail/cursus-detail.ts` (NOUVEAU)
- `frontend/src/app/features/administration/cursus-detail/cursus-detail.html` (NOUVEAU)
- `frontend/src/app/features/administration/cursus-detail/cursus-detail.scss` (NOUVEAU)
- `frontend/src/app/app.routes.ts` — ajout route `admin/cursus/:id` (`CursusDetailComponent`,
  `roleGuard(['REF'])`), juste apres `admin/cursus`.

## Evidence
- `cd frontend && ng build` -> PASS (warnings preexistants sur d'autres fichiers .scss,
  non lies a ce WI ; `cursus-detail.scss` n'apparait pas dans les warnings de budget).
  Chunk lazy `cursus-detail` genere (15.56 kB).

## Decisions
- `CursusDetailComponent` charge la liste complete via `cursusAdapter.getAll()` et trouve
  le cursus par `id` (meme pattern simple que la page liste ; pas d'endpoint GET /cursus/{id}
  dedie cote backend a ce jour).
- `coursWithPrereqs` (computed) croise `cursus().cours: CoursInCursus[]` avec
  `catalogue: Cours[]` (charge via `coursAdapter.getAll()`) pour reconstruire des `Cours`
  complets avec `prerequis: Cours[]` resolus, en entree de `computeCursusPrereqAlerts`.
- La logique `transitivePrerequis` etait dupliquee en prive dans `cursus.ts` (operant sur
  `BuilderRow[]` de la modale de creation). Je l'ai EXTRAITE et exportee dans
  `cursus-alerts.util.ts` SANS toucher a `cursus.ts` : la copie privee dans `cursus.ts`
  reste en place et fonctionne telle quelle (zero regression sur la page liste, qui sera
  refactoree dans WI-028 pour reutiliser l'util commun).
- `computeCursusPrereqAlerts` retourne une liste plate d'alertes `{ cours, coursPosition,
  prereq, prereqPosition }`. `prereqPosition === 0` signifie "prerequis absent du cursus"
  (pas de bouton Corriger dans ce cas, car rien a reordonner) ; sinon `prereqPosition > coursPosition`
  signifie "mal ordonne" -> bouton "Corriger" qui appelle `fixOrder(prereqId, beforeCoursId)`,
  lequel reordonne via `cursusAdapter.reorder`.
- Style : classes reprises de `cursus.scss` (`.cours-ordered-list`, `.pill-formateur`,
  `.warning-box`, `.badge-num`, `.modal*`, `.builder*`) + structure `page__title-group` /
  `detail-header` de `promotion-detail.scss` pour le pattern bouton retour + header.
  `.pill` / `.pill--muted` viennent de `styles.scss` global (deja utilises ainsi dans
  promotion-detail.html).

## Open Blockers
Aucun.

## Next Actions
- WI-20260611-FULLST-028 : sur la page liste `cursus.ts/html`, afficher un badge "N alerte(s)"
  par cursus en appelant `computeCursusPrereqAlerts` (necessite de croiser `cursus.cours`
  avec le catalogue, meme pattern que `coursWithPrereqs` ici) ; envisager de refactorer
  `cursus.ts` pour utiliser `transitivePrerequis` exporte au lieu de sa copie privee.
- Verification manuelle suggeree (non executee, chrome-devtools non lance dans cette session) :
  - Naviguer vers `/app/admin/cursus`, cliquer sur un cursus (necessite d'ajouter un lien
    vers `/app/admin/cursus/:id` sur la page liste — PAS fait ici, hors scope ; pour tester
    en l'etat, naviguer directement via l'URL `/app/admin/cursus/<id>`).
  - Verifier l'affichage nom + filiere + liste ordonnee + formateurs.
  - Tester drag&drop et boutons monter/descendre -> persistance via PATCH reorder.
  - Verifier qu'un cursus avec prerequis mal ordonnes affiche le bloc d'alertes et que
    "Corriger" reordonne correctement.

## Recall Hints
- cursus-alerts.util.ts : `computeCursusPrereqAlerts`, `transitivePrerequis`, `CursusPrereqAlert`
- cursus-detail/ : nouveau composant standalone, route `admin/cursus/:id`

## Proposed Rules
- TYPE: CONVENTION
  Title: Logique de calcul partagee Angular -> core/utils/*.util.ts en TS pur
  Scope: frontend/src/app/core/utils/, composants administration
  Rule: Quand une logique de calcul (alertes, transformations de donnees) doit etre
    reutilisee par plusieurs composants, l'extraire dans `core/utils/<domaine>.util.ts`
    en fonctions pures (pas de signals, pas d'injection Angular), prenant des modeles
    `core/models/*` en entree/sortie.
  Why: Permet la reutilisation testable independamment du cycle de vie Angular (ex:
    badge sur page liste + bloc d'alertes sur page detail calculent la meme chose).
  How to apply: Creer le fichier dans core/utils/, exporter des types + fonctions pures,
    les appeler depuis des `computed()` dans les composants.
  Evidence: frontend/src/app/core/utils/cursus-alerts.util.ts (WI-20260611-FULLST-027)
