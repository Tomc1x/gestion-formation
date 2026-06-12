# WI-20260610-BACKEN-010

## Work Item
WI-20260610-BACKEN-010

## Role
developer

## Status
DONE

## Scope
Frontend uniquement — section CRUD "Filières" (modifier/supprimer) dans la page Cursus existante, branchée sur `PUT /api/filiere/{id}` et `DELETE /api/filiere/{id}` (WI-009).

## Files Touched
- `frontend/src/app/core/adapters/filiere.adapter.ts` — ajout `update()` et `delete()` abstraits
- `frontend/src/app/core/adapters/filiere-http.adapter.ts` — implémentation HTTP (`PUT`/`DELETE` sur `/api/filiere/{id}`)
- `frontend/src/app/core/adapters/filiere-mock.ts` — implémentation mock, `delete()` simule un 409 (`{status: 409, error: 'Cette filière est utilisée par X cursus.'}`) si des cursus mockés y sont rattachés
- `frontend/src/app/core/adapters/cursus-mock.ts` — ajout d'une fonction exportée `countCursusByFiliere(filiereId)` pour la cohérence cross-mock (lue par `filiere-mock.ts`)
- `frontend/src/app/features/administration/cursus/cursus.ts` — `cursusCount()`, modale d'édition de filière (`editingFiliere`, `editFiliereForm`, `submitEditFiliere`), modale de suppression (`deletingFiliere`, `confirmDeleteFiliere`), `extractError()` (statuts 409/422)
- `frontend/src/app/features/administration/cursus/cursus.html` — nouvelle section tableau "Filières" (`.tbl`, `.pill`, `.dot`, `.btn-icon`) + 2 nouvelles modales (édition/suppression), pattern recopié de `cours.html`

## Evidence
- `npx tsc --noEmit -p tsconfig.app.json` → exit 0, aucune erreur
- `npx ng build` → succès, chunk `cursus` généré (26.23 kB), aucune nouvelle erreur/warning. Warnings CSS budget pré-existants inchangés (`register.scss`, `utilisateurs.scss`), hors scope.

## Decisions
- Le tableau "Filières" est affiché en haut de page (avant les groupes de cursus), réutilisant `.tbl`, `.pill`, `.dot` (styles globaux `styles.scss`) et `.btn-icon`/`.btn-ghost` — pas de nouveau système de style.
- `extractError()` dans `cursus.ts` accepte 409 ET 422 (le 422 existant pour `reorder` côté cursus, le 409 pour la suppression de filière en conflit), pattern aligné sur `cours.ts` mais étendu pour couvrir les deux codes.
- Pour la cohérence du mock (filière utilisée par X cursus → 409), `cursus-mock.ts` expose désormais `countCursusByFiliere()` plutôt que dupliquer `MOCK_DATA` — minimal export, pas de nouvelle abstraction.
- Après update réussi de filière, `cursusList` est mis à jour localement (`filiereName` recalculé) pour rester cohérent avec `groupedByFiliere` sans recharger toute la page.
- Après delete réussi, simple filtrage local de `filieres()` (le bouton est de toute façon désactivé/absent si des cursus existent encore côté réel grâce au 409).

## Open Blockers
None.

## Next Actions
None — WI terminé, prêt pour revue.

## Recall Hints
- "filiere update delete frontend", "section filieres page cursus", "countCursusByFiliere mock", "modal modifier supprimer filière"

## Proposed Rules
None — patterns suivis sont déjà ceux établis par WI-006/WI-007/WI-009.
