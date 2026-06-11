# WI-20260610-BACKEN-002 — Design system global (frontend)

## Work Item
WI-20260610-BACKEN-002 (tier complex, branche feature/backend/admin-user)

## Role
developer

## Status
DONE

## Scope
Intégrer les fondations CSS du "Claude Design" (variables, composants utilitaires globaux,
polices, scrollbars, animations) dans `frontend/src/styles.scss`, sans créer de nouvelles
pages/composants et sans casser les styles existants (login, sidebar, header, page utilisateurs).

## Files Touched
- `frontend/src/styles.scss`
  - Ajout des variables CSS `--e1`, `--e2`, `--e3` dans `:root` (mappées sur `$e1/$e2/$e3`
    déjà définies dans `_variables.scss` mais non encore exposées en custom properties).
  - Extension du bloc `.btn` : ajout `.btn-outline`, `.btn-green`, `.btn-icon`, `&:disabled`.
  - Extension de `.input` : `&::placeholder`, `&:focus` (anneau de focus bleu).
  - Ajout en fin de fichier : `.lic`/`.lic svg`, scrollbars custom (`::-webkit-scrollbar*`),
    `.card`, `.pill`, `.dot`, `.badge`, `.table-wrap` + `table.tbl` (th/td/tbody/hover),
    `.tabs`/`.tab`/`.tab.active`, animations `fadeUp`/`fadeIn`/`scaleIn` + `.anim-up`/`.anim-in`
    sous `@media (prefers-reduced-motion: no-preference)`, `.skel` + `@keyframes sh`.

## Files NOT touched (and why)
- `frontend/src/styles/_variables.scss` : déjà complet (palette bleue, statuts, neutrals,
  radius, ombres `$e1/$e2/$e3`, layout, fonts) — réutilisé tel quel, aucune nouvelle variable
  SCSS introduite.
- `frontend/src/index.html` : import Google Fonts Poppins/Sora déjà présent dans
  `styles.scss` via `@import url(...)` (lignes 1-3) — pas de doublon ajouté.
- Pas de classe globale `.avatar` ajoutée (voir Decisions).

## Evidence
- `cd frontend && npx tsc --noEmit -p tsconfig.app.json` → exit 0, aucune sortie.
- `npx sass --no-source-map src/styles.scss /tmp/out.css` → compile sans erreur (vars SCSS
  `$e1/$e2/$e3` résolues correctement dans `:root`).

## Decisions
1. **Tokens déjà en place** : la quasi-totalité des design tokens demandés (palette bleue,
   statuts, neutrals, radius, polices, layout) existaient déjà dans
   `frontend/src/styles/_variables.scss` et `styles.scss` (probablement portés lors d'un WI
   précédent — cf. commit 85a8ff8). Seules `--e1/--e2/--e3` (ombres) manquaient en custom
   properties CSS bien que les variables SCSS `$e1/$e2/$e3` existaient déjà : ajoutées par
   simple mapping, aucun renommage.
2. **`.avatar` global non ajouté** : le projet a déjà un composant partagé
   `frontend/src/app/shared/components/avatar/avatar.ts` avec classes BEM
   `.avatar`, `.avatar--sm`, `.avatar--md`, `.avatar--lg` et styles encapsulés
   (`avatar.scss`) sans `width`/`height` sur `.avatar` de base (les tailles sont sur les
   modificateurs). Ajouter un `.avatar` global avec les règles génériques du fichier source
   (`display:inline-flex; border-radius:50%; background:var(--blue-100); ...`) sans
   width/height casserait ou entrerait en conflit conceptuel avec ce composant existant.
   Décision : ne pas dupliquer — le composant partagé `app-avatar` reste la source de vérité
   pour les avatars. Si WI-006/007 ont besoin d'un avatar "brut" en CSS pur (hors composant),
   réévaluer à ce moment.
3. **`.lic` ajouté mais non actif** : le projet utilise `@lucide/angular` avec des directives
   `<svg lucideX>` directement dimensionnées par le CSS du composant (pas de wrapper `.lic`
   actuellement). La classe `.lic`/`.lic svg` a été ajoutée en globale (conforme au scope du
   bundle) mais n'est utilisée nulle part pour l'instant — disponible pour WI-006/007 si
   besoin d'icônes Lucide en taille `1em` héritée du `font-size` parent (pattern utilisé dans
   `.btn .lic`, `.input-icon .lic` du fichier source, non porté car ces blocs `.btn`/`.input-icon`
   utilisent déjà `<svg>` directement dans ce projet).
4. **`.btn-icon` (global, pour `<button class="btn btn-icon">`) coexiste sans conflit avec le
   `.btn-icon` local de `utilisateurs.scss`** (qui style un `<svg class="btn-icon">` à
   l'intérieur d'un composant à encapsulation de vues Angular — portée différente, sélecteur
   différent, pas de collision réelle au runtime).
5. **`.switch`** : le `.switch`/`.switch.on` du fichier source (40x22px, après 18x18px) diffère
   légèrement de l'implémentation existante dans `styles.scss` (44x24px, après 18x18px,
   translateX). L'implémentation existante était déjà fonctionnelle et utilisée — non modifiée
   pour respecter "ne pas casser l'existant" (changement de dimensions visuelles non demandé
   explicitement comme correction).
6. **Polices** : aucun changement nécessaire, `@import` Poppins/Sora déjà en place dans
   `styles.scss` ligne 3, cohérent avec le fichier source (mêmes familles, poids suffisants
   pour les classes ajoutées : 400-700).

## Open Blockers
Aucun.

## Next Actions
- WI-20260610-BACKEN-006/007 (catalogue Cours/Cursus) peuvent réutiliser directement :
  `.card`, `.table-wrap`/`table.tbl`, `.tabs`/`.tab`/`.tab.active`, `.pill`/`.dot`/`.badge`,
  `.btn-outline`/`.btn-green`/`.btn-icon`, `.skel`, `.anim-up`/`.anim-in`, variables
  `--e1/--e2/--e3`.
- Si besoin d'un avatar "inline" sans le composant partagé, réévaluer point 2 ci-dessus.

## Recall Hints
Classes/variables disponibles globalement (frontend/src/styles.scss) :
- Variables : `--blue-900..050`, `--green`/`--green-bg`, `--red`/`--red-bg`,
  `--amber`/`--amber-bg`, `--ink`/`--ink-2`/`--ink-3`, `--line`/`--line-2`, `--card`, `--bg`,
  `--grey-card`, `--r-sm`/`--r`/`--r-lg`/`--r-xl`, `--e1`/`--e2`/`--e3`, `--sidebar-w`,
  `--topbar-h`, `--font-head`, `--font-body`.
- Boutons : `.btn`, `.btn-primary`, `.btn-ghost`, `.btn-outline`, `.btn-green`, `.btn-sm`,
  `.btn-icon`, `:disabled`.
- `.card`, `.field`, `.input` (focus/placeholder), `.input-icon`.
- `.pill`, `.dot`, `.badge`.
- `.table-wrap`, `table.tbl` (th/td/tbody hover).
- `.tabs`, `.tab`, `.tab.active`.
- `.switch`, `.switch.on` (dimensions existantes 44x24, non alignées sur le bundle 40x22).
- `.skel` + `@keyframes sh`.
- `@keyframes fadeUp/fadeIn/scaleIn` + `.anim-up`/`.anim-in` (reduced-motion aware).
- `.lic`/`.lic svg` (disponible, non utilisé activement — projet utilise `<svg lucideX>` direct).
- Scrollbars custom `::-webkit-scrollbar*`.
- Composant partagé avatar : `app-avatar` (`frontend/src/app/shared/components/avatar/`),
  classes `.avatar`, `.avatar--sm/md/lg` — pas de `.avatar` générique global.

## Proposed Rules
- TYPE: CONVENTION
  Title: Source de vérité unique pour les design tokens
  Scope: frontend/src/styles/_variables.scss + frontend/src/styles.scss (:root)
  Rule: Toute nouvelle variable de design (couleur, ombre, radius, etc.) doit être ajoutée
    d'abord en variable SCSS dans `_variables.scss`, puis exposée en custom property CSS
    dans le bloc `:root` de `styles.scss` via `--nom: #{$nom}` — ne jamais définir une
    valeur brute directement dans `:root` ou dans un composant.
  Why: Évite la duplication/désynchronisation constatée pour `--e1/--e2/--e3` (variables SCSS
    existaient mais n'étaient pas exposées en CSS custom properties, donc inutilisables
    via `var(--e1)` dans le SCSS global ou les composants).
  How to apply: Avant d'ajouter une variable CSS, vérifier si son équivalent SCSS existe déjà
    dans `_variables.scss` ; sinon créer les deux en même temps.
  Evidence: frontend/src/styles.scss (ajout `--e1/--e2/--e3`), frontend/src/styles/_variables.scss
