# WI-20260610-BACKEN-007 — Page "Cursus" (frontend)

## Work Item
WI-20260610-BACKEN-007 (tier complex, branche feature/backend/admin-user) — dernier WI de la série.

## Role
developer

## Status
DONE

## Scope
Créer la page Angular "Cursus" (gestion des filières + cursus, constructeur de liste
ordonnée de cours avec alertes d'ordre pédagogique basées sur les prérequis), avec
adapter pattern (CONV-001), réutilisant le design system global et le catalogue de
cours (WI-20260610-BACKEN-006).

## Files Touched
- `frontend/src/app/core/models/cursus.model.ts` (nouveau) : `Filiere`,
  `CreateFiliereRequest`, `CoursInCursus`, `Cursus`, `CreateCursusRequest`.
- `frontend/src/app/core/adapters/filiere.adapter.ts` (nouveau) : `BaseFiliereAdapter`
  abstrait — `getAll`, `create`.
- `frontend/src/app/core/adapters/filiere-http.adapter.ts` (nouveau) : `HttpFiliereAdapter`.
- `frontend/src/app/core/adapters/filiere-mock.ts` (nouveau) : `MockFiliereAdapter`,
  3 filières mockées (Développement Web, Infrastructure & Réseaux, Data & IA).
- `frontend/src/app/core/adapters/cursus.adapter.ts` (nouveau) : `BaseCursusAdapter`
  abstrait — `getAll`, `create`, `addCours`, `removeCours`, `reorder`.
- `frontend/src/app/core/adapters/cursus-http.adapter.ts` (nouveau) : `HttpCursusAdapter`.
- `frontend/src/app/core/adapters/cursus-mock.ts` (nouveau) : `MockCursusAdapter`,
  3 cursus mockés réutilisant les ids des cours de `cours-mock.ts` (1=TS Bases,
  2=Angular Avancé, 3=HTML/CSS, 4=Angular Bases, 5=Spring Boot Avancé, 6=Java Bases).
- `frontend/src/app/app.config.ts` : ajout providers
  `{provide: BaseFiliereAdapter, useClass: HttpFiliereAdapter}` et
  `{provide: BaseCursusAdapter, useClass: HttpCursusAdapter}`.
- `frontend/src/app/features/administration/cursus/cursus.ts` (nouveau) : composant
  standalone, OnPush, signals — `cursusList`, `filieres`, `catalogue`, `loading`,
  `loadError`, `submitting`, `formError`, `groupedByFiliere` (computed), `builderRows`
  (signal de la liste construite, avec lignes "fantômes" pour prérequis manquants).
- `frontend/src/app/features/administration/cursus/cursus.html` (nouveau) : template
  externe — cards de cursus groupées par filière (`.card`, `.badge`, `.pill`), modale
  "Nouvelle filière" (champ nom), modale "Nouveau cursus" (nom + select filière +
  constructeur de liste à deux colonnes : catalogue disponible / liste ordonnée avec
  ↑/↓/retirer, lignes fantômes grisées avec badge `?`, alertes `.warning-box` pour
  prérequis mal ordonnés avec bouton "Corriger").
- `frontend/src/app/features/administration/cursus/cursus.scss` (nouveau) : mise en
  page spécifique (grilles de cards, badges colorés, constructeur 2 colonnes,
  warning-box) — réutilise `.btn`, `.card`, `.field`, `.input`, `.pill`, `.badge`,
  `.checkbox-list`, `.modal*`, animations `.anim-up`/`.anim-in`.
- `frontend/src/app/app.routes.ts` : route `admin/cursus` (titre "Cursus"),
  `loadComponent` lazy vers `CursusComponent`.
- `frontend/src/app/layouts/main-layout/sidebar/sidebar.ts` : entrée sidebar
  `/app/admin/cursus` "Cursus", icône `LucideListTree` (importée pour le typage
  `LucideIconInput`, pattern identique à `LucideBookOpen`/WI-006 — pas dans le tableau
  `imports` du composant car `LucideDynamicIcon` rend dynamiquement).

## Evidence
- `cd frontend && npx tsc --noEmit -p tsconfig.app.json` → exit 0, aucune sortie.
- `npx ng build` → chunk `cursus` généré (chunk-C2EZMRXI.js, 20.39 kB / 5.27 kB
  transfer), aucune nouvelle erreur/warning. Le seul échec de build (`ERROR ...
  utilisateurs.scss exceeded maximum budget 8kB`) est pré-existant et documenté dans
  la mémoire WI-20260610-BACKEN-006 — confirmé hors scope par l'énoncé du WI.

## Decisions
1. **Endpoint Filière réel = `/api/filiere` (singulier)**, et non `/api/filieres`
   comme indiqué dans l'énoncé du WI — vérifié dans
   `backend/.../controller/FiliereController.java` (`@RequestMapping("/api/filiere")`).
   `HttpFiliereAdapter` utilise donc `/api/filiere`.
2. **Couleur de filière purement front, déterministe, non persistée** : tableau
   `FILIERE_COLORS` (8 couleurs hex) indexé par `filiereId % 8`, utilisé uniquement
   pour le `.dot` du titre de groupe — aucun champ couleur ajouté côté modèle/backend,
   conformément à la contrainte du WI.
3. **Création de cursus = `create()` puis `addCours()` séquentiels (un appel par
   cours, sans `ordre` explicite)** plutôt que `reorder()` après ajouts en vrac :
   chaque `POST .../cours` sans `ordre` ajoute en fin de liste côté backend
   (`AddCoursToCursusRequest.ordre` optionnel = ajout en fin, cf. `CursusController`),
   donc enchaîner les appels dans l'ordre voulu produit directement le bon
   ordonnancement, sans appel `reorder` supplémentaire. Approche choisie pour sa
   simplicité/lisibilité (récursion `addCoursSequentially`).
4. **Lignes "fantômes" (prérequis manquants) recalculées à chaque mutation** via
   `recomputeGhosts()` : pour chaque cours réel de la liste (dans l'ordre), on calcule
   ses prérequis transitifs (`transitivePrerequis`, réutilise `Cours.prerequis`
   récursif du catalogue) et on insère une ligne fantôme juste après le cours pour
   chaque prérequis absent de la liste. Le bouton "+ Ajouter" d'une ligne fantôme
   (`addGhostPrerequis`) matérialise ce cours juste avant le cours qui le requiert.
5. **Prérequis présent mais mal positionné** : `misorderedPrereqs(index)` détecte,
   pour un cours réel à l'index `i`, tout prérequis transitif présent dans la liste
   mais à une position `> i`. Affiche un `.warning-box` avec le message
   `⚠ Requiert « X » — actuellement en position N` et un bouton "Corriger"
   (`fixOrder`) qui déplace le prérequis juste avant le cours concerné. Le badge
   numéroté du cours passe en orange (`.badge-warning`, `--amber`/`--amber-bg`
   existants dans `_variables.scss`) tant qu'un prérequis est mal ordonné.
6. **Regroupement par filière (`groupedByFiliere`)** : calculé via `computed()`,
   parcourt `filieres()` dans l'ordre pour préserver un ordre stable, puis ajoute un
   groupe "Sans filière" (`filiereId === null`) si nécessaire en fin de liste.
7. **Mock cursus** : 3 cursus, dont 2 réutilisant les ids 1-6 du catalogue mocké de
   `cours-mock.ts` (cohérence cross-mock pour tester les prérequis/formateurs).
   `MockCursusAdapter.reorder` rejette (422) si `coursIds.length` ne correspond pas au
   nombre de cours actuels du cursus, simulant une validation backend basique.

## Open Blockers
Aucun.

## Next Actions
Aucune (dernier WI de la série). Le manager lancera le `verifier` pour la
vérification globale cross-module (changement de contrat API).

## Recall Hints
- Adapters Filière : `frontend/src/app/core/adapters/{filiere.adapter.ts,
  filiere-http.adapter.ts, filiere-mock.ts}` — endpoint réel `/api/filiere` (singulier).
- Adapters Cursus : `frontend/src/app/core/adapters/{cursus.adapter.ts,
  cursus-http.adapter.ts, cursus-mock.ts}`, providers dans `app.config.ts`
  (actuellement `Http*Adapter`).
- Page : `frontend/src/app/features/administration/cursus/{cursus.ts, cursus.html,
  cursus.scss}`, route `/app/admin/cursus`.
- Modèles : `frontend/src/app/core/models/cursus.model.ts` (réutilise `FormateurInfo`
  de `cours.model.ts`).

## Proposed Rules
- TYPE: PITFALL
  Title: Endpoint Filière au singulier `/api/filiere`, pas `/api/filieres`
  Scope: backend FiliereController / frontend adapters Filière
  Rule: Toute référence future à l'API Filière doit utiliser `/api/filiere`
    (singulier), conformément à `@RequestMapping("/api/filiere")` dans
    `FiliereController.java` — les specs de WI peuvent contenir une coquille au
    pluriel.
  Why: L'énoncé de WI-20260610-BACKEN-007 indiquait `GET /api/filieres`
    (pluriel), ce qui aurait causé un 404 si suivi sans vérification.
  How to apply: Toujours confirmer le `@RequestMapping` du contrôleur réel avant
    d'écrire un `Http*Adapter`.
  Evidence: `backend/src/main/java/fr/eni/gestionformation/controller/FiliereController.java:14`
