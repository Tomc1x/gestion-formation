# WI-20260611-FULLST-028

## Role
developer

## Status
READY_FOR_REVIEW

## Scope
Simplification de la page liste des cursus `admin/cursus` (`cursus.ts/html/scss`) :
remplacement du contenu detaille de chaque carte cursus (liste ordonnee, drag&drop,
monter/descendre, retirer, "Ajouter un cours") par un resume (nom, nombre de cours,
badge alerte si prerequis mal ordonnes), carte cliquable vers `admin/cursus/:id`.

## Files Touched
- `frontend/src/app/features/administration/cursus/cursus.ts`
  - Ajout `Router` (inject) et `openCursus(cursus)` -> `router.navigate(['/app/admin/cursus', cursus.id])`
    (meme convention que `promotions.ts` -> `/app/admin/promotions/:id`).
  - Ajout `cursusAlertCount(cursus): number` qui croise `cursus.cours` avec `catalogue()`
    (meme pattern que `coursWithPrereqs` dans `cursus-detail.ts`) puis appelle
    `computeCursusPrereqAlerts(...)` de `core/utils/cursus-alerts.util.ts` et renvoie
    `.length`.
  - Import `computeCursusPrereqAlerts` depuis `core/utils/cursus-alerts.util.ts`.
  - Suppression des methodes/signaux devenus inutiles sur cette page :
    `draggedCours`, `onDragStart`, `onDragEnd`, `onDropOnCours`, `moveCoursInCursus`,
    `persistReorder`, `availableCoursForCursus`, `addingCoursToCursus`,
    `openAddCoursModal`/`closeAddCoursModal`, `addCoursToCursus(cursus, coursId)`,
    `removeCoursFromCursus`, `coursSearchQuery`, `filteredAvailableCoursForCursus`,
    `onSearchQueryChange`, `fullName`.
  - Suppression des imports lucide devenus inutiles : `LucideUsers`, `LucideGripVertical`.
    `LucideX`, `LucideArrowUp`, `LucideArrowDown` conserves (toujours utilises dans la
    modale "Nouveau cursus" / builder, non touchee).
  - La modale builder (`BuilderRow`, `transitivePrerequis` prive, `misorderedPrereqs`,
    `hasMisorderedPrereqs`, `fixOrder`, etc.) est INCHANGEE, conformement au scope.
- `frontend/src/app/features/administration/cursus/cursus.html`
  - Pour chaque carte cursus (groupes par filiere ET "Sans filiere") : remplace le bloc
    `<ol class="cours-ordered-list">` + bouton "Ajouter un cours" par rien — la carte
    ne montre plus que titre + `"{{ cursus.cours.length }} cours"` + badge optionnel
    `⚠ N alerte(s)` (`class="badge badge-warning"`, pluriel gere via ternaire inline).
  - Carte entiere : `role="button"`, `tabindex="0"`, `(click)/(keydown.enter)/(keydown.space)`
    -> `openCursus(cursus)`, classe `cursus-card--clickable` (meme pattern accessibilite
    que `promo-card` dans `promotions.html`).
  - Boutons Modifier/Supprimer cursus : ajout `$event.stopPropagation();` avant l'appel
    existant pour ne pas declencher la navigation de la carte.
  - Suppression complete de la modale "Ajouter un cours à {{ ... }}" (dependait des
    methodes/signaux supprimes).
  - Modales filiere (Nouvelle/Modifier/Supprimer), modale "Nouveau cursus" (builder avec
    ghosts/misorderedPrereqs), modales Modifier/Supprimer cursus : INCHANGEES.
- `frontend/src/app/features/administration/cursus/cursus.scss`
  - Ajout `.cursus-card--clickable` (cursor: pointer, hover/focus-visible box-shadow,
    meme pattern que `.promo-card` dans `promotions.scss`).
  - Ajout `.cursus-card__header .badge-warning { margin-top: 0.375rem; }` pour espacer
    le badge sous le sous-titre "N cours".

## Evidence
- `cd frontend && ng build` -> PASS (5.6s). Warning budget preexistant sur
  `cursus.scss` (4.31 kB, depasse le budget de 4.00 kB de 311 bytes) — meme type de
  warning deja present sur `utilisateurs.scss`, `promotions.scss`, `register.scss`
  avant ce WI ; non bloquant.
  Chunk lazy `cursus` toujours genere (35.62 kB, en baisse vs avant car logique
  retiree).

## Decisions
- `cursusAlertCount` recalcule `computeCursusPrereqAlerts` a chaque appel (pas de
  `computed()` car la fonction prend `cursus` en parametre par carte, pas un signal
  global). Meme approche que `cursusForFiliere`/`cursusCount` deja presents (methodes
  non-computed appelees depuis le template, OnPush + signals en amont garantissent la
  fraicheur).
- Le texte "· ordre pedagogique" du sous-titre a ete retire avec la liste ordonnee
  (n'a plus de sens sur une carte qui ne montre plus l'ordre) ; le scope demandait de
  garder "{{ cursus.cours.length }} cours" exactement, ce qui est fait.
- Badge pluriel gere inline via `{{ cursusAlertCount(cursus) > 1 ? 's' : '' }}` —
  pas de pipe i18n existant dans ce composant, coherent avec le style du reste du
  fichier (pas de nouvelle abstraction).
- N'ai PAS touche `cursus-detail.ts/html/scss` (hors scope, deja livre par WI-027).

## Open Blockers
Aucun.

## Next Actions
- Test manuel recommande (non execute, pas de session chrome-devtools lancee) :
  1. Naviguer vers `/app/admin/cursus`.
  2. Cliquer sur une carte cursus (zone hors boutons) -> doit naviguer vers
     `/app/admin/cursus/:id` (CursusDetailComponent, route ajoutee en WI-027).
  3. Cliquer sur "Modifier" ou "Supprimer" (icones) sur une carte -> ne doit PAS
     naviguer (stopPropagation), doit ouvrir la modale correspondante.
  4. Verifier qu'un cursus dont les cours ont des prerequis mal ordonnes affiche le
     badge "⚠ N alerte(s)" (N = `computeCursusPrereqAlerts(...).length`), et qu'un
     cursus sans probleme n'affiche aucun badge.
  5. Verifier que la modale "Nouveau cursus" (builder, ghosts, "Corriger") fonctionne
     toujours comme avant (non modifiee).

## Recall Hints
- cursus.ts : `openCursus`, `cursusAlertCount` (nouveaux), modale builder inchangee
  (`BuilderRow`, `recomputeGhosts`, `transitivePrerequis` prive).
- cursus.html : cartes cursus simplifiees, `cursus-card--clickable`.
- cursus-alerts.util.ts (WI-027) : `computeCursusPrereqAlerts`, reutilise ici sans
  modification.

## Proposed Rules
Aucune nouvelle regle proposee (le pattern "logique partagee -> core/utils/*.util.ts"
a deja ete propose et est en cours de triage via WI-20260611-FULLST-026/027).
