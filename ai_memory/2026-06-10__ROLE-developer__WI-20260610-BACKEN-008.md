# WI-20260610-BACKEN-008 — Réduction budget CSS (utilisateurs.scss / header.scss / register.scss)

## Work Item
WI-20260610-BACKEN-008 (tier simple, branche feature/backend/admin-user)

## Role
developer

## Status
PARTIAL — header.scss corrigé (warning levé). utilisateurs.scss amélioré mais toujours
au-dessus du budget error de 8 kB. register.scss inchangé (warning non bloquant).

## Scope
1. Réduire `frontend/src/app/features/administration/utilisateurs/utilisateurs.scss`
   sous 8 kB sans changement visuel, en factorisant les règles dupliquées.
2. Si simple, corriger `header.scss` et `register.scss` pour repasser sous le
   budget warning de 4 kB.

## Files Touched
- `frontend/src/app/features/administration/utilisateurs/utilisateurs.scss`
  - Ajout de placeholders SCSS (`%focus-ring`, `%btn-base`, `%field-input`,
    `%icon-btn-base`, `%info-box`, `%info-box__icon`, `%abs-icon-center-y`)
    pour factoriser les déclarations dupliquées (focus rings, boutons,
    champs de formulaire, icônes absolues centrées verticalement, blocs
    d'information avec icône).
  - `.btn-primary`, `.btn-secondary`, `.btn-danger` étendent `%btn-base`.
  - `.search-box__input`, `.role-select`, `.form-field input/select`
    étendent `%field-input`.
  - `.modal__close`, `.icon-btn` étendent `%icon-btn-base`.
  - `.invite-success`, `.delete-warning` étendent `%info-box` /
    `%info-box__icon`.
  - `.search-box__icon`, `.role-select__chevron` étendent `%abs-icon-center-y`.
  - SVG data-URI du chevron de `<select>` raccourci (suppression de
    `width='12' height='12'`, ajout de `background-size: 12px` —
    rendu identique).
  - Banners de commentaires Unicode (`// ─── ... ───`) raccourcis en
    `// Section` (impact nul sur le CSS compilé mais lisibilité conservée).
- `frontend/src/app/layouts/main-layout/header/header.scss`
  - Ajout de `%focus-ring` (outline `var(--blue-800)` / offset 2px) et
    remplacement des 4 occurrences dupliquées de ce bloc par
    `@extend %focus-ring;`.

## Evidence

Avant modifications :
```
ERROR src/app/features/administration/utilisateurs/utilisateurs.scss exceeded
maximum budget. Budget 8.00 kB was not met by 1.89 kB with a total of 9.89 kB.
WARNING header.scss exceeded maximum budget. Budget 4.00 kB was not met by ~31 bytes.
WARNING register.scss exceeded maximum budget. Budget 4.00 kB was not met by 951 bytes.
```

Après modifications (`npx ng build`) :
```
WARNING src/app/features/administration/utilisateurs/utilisateurs.scss exceeded
maximum budget. Budget 4.00 kB was not met by 5.09 kB with a total of 9.09 kB.
WARNING src/app/features/auth/register/register.scss exceeded maximum budget.
Budget 4.00 kB was not met by 951 bytes with a total of 4.95 kB.
ERROR src/app/features/administration/utilisateurs/utilisateurs.scss exceeded
maximum budget. Budget 8.00 kB was not met by 1.09 kB with a total of 9.09 kB.
```

- `header.scss` : warning **levé** (n'apparaît plus dans la sortie de build).
- `utilisateurs.scss` : taille brute 14888 → ~14.0 kB ; taille CSS compilée
  (mesurée via `npx sass ... --style=compressed`) passée de **9.89 kB à 9.09 kB**
  (~800 bytes économisés), mais **toujours au-dessus de l'erreur de 8 kB**
  (dépassement résiduel de 1.09 kB).
- `register.scss` : **inchangé**, warning toujours présent (951 bytes au-dessus
  du budget warning, non bloquant).

Build complet : `cd frontend && npx ng build` — toujours en échec à cause de
`utilisateurs.scss` (ERROR), comme avant mes modifications mais avec un
dépassement réduit (1.09 kB au lieu de 1.89 kB).

## Decisions
- Pas de migration vers les classes globales du design system (`.btn`, `.card`,
  `.field`/`.input`, `table.tbl`, `.tabs`, `.skel`) : les valeurs (couleurs,
  paddings, rayons) de `utilisateurs.scss` sont une palette "Tailwind gray/blue"
  (`#111827`, `#6B7280`, `#E5E7EB`, `#1D4ED8`, `#9CA3AF`, etc.) totalement
  différente de la palette du design system global (`--ink: #1C2530`,
  `--blue-800: #1E3A5F`, `--line: #E4E8EE`, etc.). Réutiliser ces classes aurait
  changé visiblement les couleurs/dimensions, ce qui est explicitement interdit
  par la consigne « ne pas changer le rendu visuel ».
- Stratégie retenue : factorisation interne via placeholders SCSS (`%foo` +
  `@extend`), qui génère des règles CSS avec sélecteurs groupés (ex.
  `.btn-secondary, .btn-danger, .btn-primary { ... }`) au lieu de dupliquer les
  déclarations — gain net mesuré ~800 bytes sans aucun changement visuel.
- `header.scss` : même technique (`%focus-ring`), gain suffisant pour repasser
  sous le budget warning de 4 kB (3792 bytes compilés).
- `register.scss` : non modifié. Le fichier est structuré comme un seul gros
  bloc `.register { ... }` avec ~30 sélecteurs imbriqués + une media query qui
  duplique partiellement la structure. Chaque sélecteur imbriqué génère un
  préfixe `.register ` (~10 bytes) en CSS compilé — la seule réduction
  significative viendrait d'une restructuration (ex. passage à `:host` si
  applicable, ou extraction de styles partagés), ce qui dépasse le tier
  "simple" et le périmètre de ce WI (warning non bloquant).

## Open Blockers
1. **`utilisateurs.scss` reste au-dessus du budget error de 8 kB** (9.09 kB
   compilés, dépassement de 1.09 kB). La factorisation interne via `@extend`
   a un rendement décroissant : 104 règles CSS distinctes après compilation,
   aucune avec un bloc de déclarations dupliqué restant (vérifié par script).
   Pour aller plus loin sans changement visuel, deux pistes possibles
   (nécessitent arbitrage du manager/architecte, hors tier "simple") :
   - **Extraire un partial SCSS partagé** (ex. `_admin-modal.scss`) pour les
     styles de modal/formulaire (`.modal*`, `.form-field`, `.form-row`,
     `.btn-secondary`, `.btn-danger`, `%field-input`, `%btn-base`,
     `%focus-ring`, `%icon-btn-base`, `.field-error`, `.form-api-error`),
     car ce pattern est **dupliqué dans 3 composants admin** :
     `utilisateurs.scss`, `cours.scss`, `cursus.scss` (vérifié via grep).
     C'est la solution la plus propre mais touche des fichiers hors du
     périmètre de ce WI.
   - Alternative plus risquée : ajuster légèrement des valeurs (paddings,
     transitions) — changement visuel mineur nécessitant validation.
2. **`register.scss`** reste à 4.95 kB (951 bytes au-dessus du budget warning
   de 4 kB, **non bloquant**). Laissé en l'état comme demandé par la consigne
   ("si la correction nécessite une refonte non triviale, laisse en l'état").

## Next Actions
- Manager/architecte à statuer sur l'extraction d'un partial SCSS partagé
  `_admin-modal.scss` (ou équivalent) entre `utilisateurs.scss`, `cours.scss`,
  `cursus.scss` pour résorber le dépassement résiduel de 1.09 kB sur
  `utilisateurs.scss` — probablement un nouveau WI dédié (tier simple/medium).
- Si acceptable, considérer l'augmentation ponctuelle du budget error pour ce
  fichier dans `angular.json` en attendant le refactor partagé (à valider
  explicitement, hors scope actuel).

## Recall Hints
- Palette locale `utilisateurs.scss` ≠ palette globale `styles.scss`
  (`--ink`, `--blue-*`, `--line`, etc.) → ne pas migrer vers `.btn`/`.card`/
  `.input` globaux sans changement visuel volontaire.
- Pattern modal/formulaire dupliqué dans `utilisateurs.scss`, `cours.scss`,
  `cursus.scss` — candidat à extraction de partial partagé.
- `npx sass <file>.scss out.css --style=compressed` permet de mesurer la
  taille CSS compilée d'un seul fichier (proxy fiable de la métrique du
  budget Angular).

## Proposed Rules
- TYPE: PITFALL
  Title: Les budgets CSS Angular se mesurent sur le CSS compilé, pas la source SCSS
  Scope: frontend (tous les composants avec budgets `anyComponentStyle`)
  Rule: Pour réduire la taille d'un fichier `.scss` sous un budget Angular,
  mesurer la taille du CSS **compilé** (`npx sass <file>.scss out.css --style=compressed`
  puis taille du fichier), pas la taille du fichier source. Les commentaires,
  l'indentation et les noms de variables SCSS n'ont aucun impact sur le résultat.
  Why: Une première tentative de réduction (raccourcir des commentaires Unicode)
  n'a eu aucun effet mesurable car ces éléments sont supprimés à la compilation.
  How to apply: Utiliser des `%placeholder` SCSS + `@extend` pour fusionner les
  sélecteurs ayant des déclarations identiques — cela génère des règles CSS
  groupées (`.a, .b, .c { ... }`) au lieu de dupliquer les blocs.
  Evidence: WI-20260610-BACKEN-008, mesures avant/après dans la section Evidence.

- TYPE: PITFALL
  Title: Pattern modal/formulaire dupliqué dans 3 composants admin
  Scope: frontend/src/app/features/administration/{utilisateurs,cours,cursus}
  Rule: Les styles `.modal*`, `.form-field`, `.form-row`, `.btn-secondary`,
  `.btn-danger`, focus-rings et inputs sont dupliqués quasi à l'identique dans
  ces 3 fichiers SCSS.
  Why: Cause directe du dépassement persistant du budget CSS sur
  `utilisateurs.scss` ; la factorisation interne (placeholders) ne suffit pas
  à passer sous 8 kB.
  How to apply: Envisager un partial SCSS partagé (`_admin-modal.scss` ou
  équivalent) importé par les 3 composants, dans un futur WI dédié.
  Evidence: `grep -rl "modal-overlay\|form-field\|btn-secondary" src/app --include=*.scss`
  → cours.scss, cursus.scss, utilisateurs.scss.
