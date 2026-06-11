Work Item: WI-20260611-FULLST-012
Role: solution-architect
Status: ANALYSIS_DONE — pret pour developer
Mode: DEEP

Probleme analyse: Page "Gestion de la promotion" (/app/admin/promotions/:id) -
fusion promotion-detail (WI-001) + planning (WI-005/010, SUPERSEDED) en tabs
"Cours planifies"/"Stagiaires" + modale "Planifier un cours" avec bandeau
prerequis (design React PromoDetail/PlanCourseModal/Meta fourni par l'utilisateur).

Recommandation retenue: Option 1 - conteneur promotion-detail.ts (route/selecteur
inchanges) + 2 composants tab standalone (cours-planifies-tab, stagiaires-tab)
+ 1 composant modale standalone (plan-course-modal), reutilisant
editForm/sessionWarnings/submitSession de planning.ts (sans le code de grille
calendrier, jete) et EntitySelectorComponent existant.

Options ecartees:
- Option 2 (monolithique dans promotion-detail.ts) : viole single
  responsibility, .html ingerable.
- Option 3 (sous-routes par tab) : sur-ingenierie pour 2 tabs, complexifie
  le partage d'etat promotion().

Risques identifies (voir ANALYSIS pour details/mitigations):
1. BLOQUANT POTENTIEL - endpoint POST creation CoursPlanifie pour une
   promotion non identifie dans FULLST-008/011. Le developer doit verifier
   et remonter au Manager si confirme absent (mode edition seul livrable
   sinon).
2. Ambiguite badge Promotion/Unite sur tab Stagiaires (Promotion.eleves vs
   InscriptionCours individuelles) - a defaut, badge "Promotion" uniquement.
3. FULLST-011 livree (READY_FOR_REVIEW) : contrat reel documente section 5 de
   l'ANALYSIS (formateurId/formateurNom/salle sur CoursPlanifieResponse,
   PUT /api/promotions/{id}/planning/{coursPlanifieId} etendu).
4. Volumetrie/source de Cours.prerequis recursif a verifier au codage.
5. Comportement "Planifier malgre tout" (force) - hypothese non bloquante a
   documenter, payload extensible.

Livrable: ai_doc/ANALYSIS__WI-20260611-FULLST-012__gestion-promotion.md
(arborescence finale, mapping design->Angular, contrat API, plan de retrait
de planning/, anti-patterns).

Proposed Rules:
- TYPE: CONVENTION
  Title: Tabs de page detail = composants standalone enfants avec
  input(promotion)/output(promotionUpdated)
  Scope: frontend/src/app/features/**/*-detail/** (pages detail avec tabs)
  Rule: Chaque tab d'une page detail a onglets est un composant standalone
  separe recevant l'entite parente via input() et emettant ses mutations
  via output(), jamais gere par des @if dans le composant page.
  Why: Evite les composants monolithiques (.html trop longs), coherent avec
  CLAUDE.md "single responsibility" et le pattern retenu pour FULLST-012.
  How to apply: Voir structure cours-planifies-tab/stagiaires-tab dans
  ai_doc/ANALYSIS__WI-20260611-FULLST-012__gestion-promotion.md.
  Evidence: ai_doc/ANALYSIS__WI-20260611-FULLST-012__gestion-promotion.md
