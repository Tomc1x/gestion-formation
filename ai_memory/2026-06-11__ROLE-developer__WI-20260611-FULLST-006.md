# Role note - developer - WI-20260611-FULLST-006

## Status: DONE

## Summary
Refonte du tableau Catalogue de cours (frontend/src/app/features/administration/cours/cours.{ts,html,scss}) :
- Colonnes Formateurs / Prerequis / Requis par limitees a 3 badges visibles + badge "+X autres" via `<details><summary>` natif (accessible, zero JS supplementaire).
- Modals create/edit : remplacement des checkbox-lists exhaustives par `EntitySelectorComponent` (mode `multi-select`, frontend/src/app/shared/components/entity-selector/) avec recherche, pour Formateurs et Prerequis.
- Logique anti-cycle conservee via `disabledIds` passe a EntitySelectorComponent.

## Verification
- `npx ng build` : PASS (chunk cours 23.43 kB).
- chrome devtools sur http://localhost:4200/app/admin/cours :
  - Tableau affiche "+1 autres" (DisclosureTriangle) sur la ligne Angular Avance (Formateurs/Prerequis/Requis par).
  - Modal "Creer un cours" : champs Formateurs et Prerequis sont des EntitySelectorComponent avec searchbox + checkbox-list filtrable.

## Proposed Rules
- CONVENTION : pour toute liste de badges potentiellement longue dans un tableau, utiliser `<details><summary>+X autres</summary>...</details>` natif plutot qu'un tooltip JS (accessible, pas de dependance).
- CONVENTION : EntitySelectorComponent (frontend/src/app/shared/components/entity-selector/) est le composant standard pour remplacer les checkbox-lists exhaustives (modes 'add' et 'multi-select', support `disabledIds`).
