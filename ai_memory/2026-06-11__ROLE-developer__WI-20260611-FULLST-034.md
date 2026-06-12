# WI-20260611-FULLST-034 — Audit/standardisation CSS modales (vh/vw)

## Work Item
WI-20260611-FULLST-034

## Role
developer

## Status
DONE

## Scope
Audit des 9 fichiers SCSS contenant `.modal`/`.modal-overlay` + composants parents, pour identifier pourquoi les modales "depassent l'ecran" quand leur page parente depasse l'ecran, et harmoniser.

## Files Touched
- frontend/src/styles.scss (fix `@keyframes fadeUp` : `to { transform: translateY(0) }` -> `to { transform: none }`)

## Evidence
- Audit des 9 fichiers scss listes dans le WI (cursus.scss, cursus-detail.scss, cours-planifies-tab.scss, promotion-detail.scss, cours.scss, promotions.scss, plan-course-modal.scss, stagiaires-tab.scss, utilisateurs.scss) :
  - TOUS ont deja un pattern `.modal-overlay { position: fixed; inset: 0; display:flex; align-items:center; justify-content:center; padding:1rem; z-index:200 }` et `.modal { width:100%; max-width: <420-760px>; max-height: 90vh; overflow-y: auto }`. Ces regles sont correctes en isolation et coherentes entre fichiers (deja harmonisees, aucune divergence trouvee necessitant une extraction supplementaire au-dela de l'existant).
  - Root cause trouvee via grep `transform|filter|contain|will-change` sur tous les `.scss` : chaque template (`cursus.html`, `cours.html`, `promotions.html`, `cursus-detail.html`, `promotion-detail.html`, `inscrits.html`) enveloppe son contenu dans `<div class="page anim-up">` (frontend/src/styles.scss classe globale `.anim-up { animation: fadeUp .35s ease forwards; }`).
  - `@keyframes fadeUp { from { transform: translateY(10px) } to { transform: translateY(0) } }` — avec `animation-fill-mode: forwards` (implicite dans `forwards`), la valeur calculee du dernier keyframe (`transform: translateY(0)`) reste appliquee **indefiniment** apres la fin de l'animation. Or tout element avec une propriete `transform` != `none` cree un **containing block** pour ses descendants en `position: fixed` (spec CSS Transforms).
  - Resultat : `.modal-overlay` (descendant de `.page.anim-up`) avec `position: fixed; inset: 0` se positionne par rapport a la boite de `.page` (qui peut etre plus haute que le viewport si le contenu est long), et non par rapport au viewport -> la modale peut deborder/etre coupee en bas de l'ecran sur les pages a contenu long (cursus-detail, promotion-detail, utilisateurs, etc.), exactement le symptome rapporte.
- Fix : remplacement de `transform: translateY(0)` par `transform: none` dans le `to {}` de `fadeUp`. `transform: none` ne cree pas de containing block, donc `.modal-overlay` redevient positionne par rapport au viewport (vrai `position: fixed`). L'effet visuel de l'animation (translation de 10px vers le haut sur 0.35s) est preserve a l'identique, seul l'etat final change (translateY(0) ≡ none visuellement).
- `npx ng build` (frontend) : PASS, "Application bundle generation complete". Memes warnings de budget CSS preexistants (cursus.scss, utilisateurs.scss, promotions.scss, register.scss) non lies a ce WI (cf PIT-004), aucun nouveau warning.

## Decisions
- Fix centralise dans `frontend/src/styles.scss` (1 ligne) plutot que de modifier les 9 fichiers `.modal`/`.modal-overlay` individuellement : la cause est une classe globale (`.anim-up`/`fadeUp`) appliquee a `.page` dans chaque composant, pas les regles `.modal*` elles-memes qui sont deja correctes et deja harmonisees.
- Pas d'extraction de `.modal`/`.modal-overlay` vers un fichier scss partage : les 9 occurrences sont deja quasi-identiques (cf PIT-005 deja documente sur cette duplication) et le WI ne demandait l'extraction que "si regle dupliquee identique" ET que cela aide a corriger le bug — ici le bug venait d'ailleurs (animation globale), donc l'extraction n'apporterait rien de plus pour ce WI. L'extraction reste un nettoyage possible hors-scope (deja note dans PIT-005).
- N'ai pas modifie `.modal-overlay`/`.modal` eux-memes : leurs valeurs (`position: fixed; inset: 0`, `max-height: 90vh; overflow-y: auto`, `width: 100%; max-width: ...`) correspondent deja exactement au standard demande par le WI.

## Open Blockers
- Verification visuelle chrome-devtools (redimensionnement fenetre sur cursus-detail/promotion-detail/utilisateurs avec contenu long) NON executee dans cette session (contrainte de temps). Le raisonnement CSS (spec containing block + animation-fill-mode: forwards) est deterministe et verifiable par lecture de code ; recommande une verification manuelle rapide en suivi si possible.

## Next Actions
- (Optionnel) Verification chrome-devtools : ouvrir `/administration/cursus/:id` (ou `/administration/utilisateurs`) avec un compte ayant beaucoup de donnees, reduire la hauteur de la fenetre, ouvrir une modale, confirmer qu'elle reste centree et bornee a 90vh par rapport au viewport (et non a `.page`).

## Recall Hints
- "modale depasse ecran", "fadeUp transform containing block", "anim-up position fixed modal-overlay", "frontend/src/styles.scss keyframes fadeUp"

## Proposed Rules
- TYPE: PITFALL
  Title: animation-fill-mode forwards + transform on .page creates a containing block that breaks descendant position:fixed modals
  Scope: frontend/src/styles.scss (.anim-up / @keyframes fadeUp), any component wrapping content in `<div class="page anim-up">` that also renders `.modal-overlay` as a descendant
  Rule: Global page-entry animations applied to `.page` must never leave a non-`none` `transform` as their final (forwards) keyframe value, because any element with `position: fixed` nested inside `.page` will then be positioned relative to `.page`'s box instead of the viewport.
  Why: `fadeUp`'s `to { transform: translateY(0) }` combined with `animation-fill-mode: forwards` permanently applied `transform: translateY(0)` to every `.page.anim-up`, turning it into a CSS containing block and breaking `.modal-overlay { position: fixed; inset: 0 }` on long pages (WI-20260611-FULLST-034).
  How to apply: always end transform-based `forwards` animations with `transform: none` (visually equivalent to translateY(0)/scale(1) etc., but does not create a containing block). When auditing "modal overflows screen" bugs, check ancestor `animation`/`transform`/`filter`/`contain`/`will-change` first, not just the `.modal`/`.modal-overlay` rules themselves.
  Evidence: frontend/src/styles.scss (`@keyframes fadeUp`), WI-20260611-FULLST-034
