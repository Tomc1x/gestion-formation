# Role Note — solution-architect

Work Item: WI-20260611-FULLST-007
Role: solution-architect
Status: DONE
Mode: DEEP

## Probleme analyse

Le modele actuel `PromotionCours` fusionne deux concepts : "session planifiee d'un cours" (dates, statut, ordre) et "lien obligatoire vers une Promotion" (`promotion_id` NOT NULL). Aucune entite ne permet l'inscription individuelle d'un eleve a une session "a l'unite" (hors promotion). Besoin : permettre des sessions sans promotion, une inscription individuelle eleve <-> session, et des vues agregees (calendrier eleve, liste des inscrits pour le formateur).

## Recommandation retenue

Option 1 : renommer/refondre `PromotionCours` en `CoursPlanifie`, rendre `promotion` nullable (`@ManyToOne(optional = true)`), et ajouter une nouvelle entite `InscriptionCours` (eleve <-> CoursPlanifie, contrainte unique `(user_id, cours_planifie_id)`). Reutilise directement `PlanificationService` (BACKEN-016) et la detection de conflits formateurs (BACKEN-018) plutot que de les dupliquer.

## Options ecartees

- Option 2 : garder `PromotionCours` intact + nouvelle entite `CoursPlanifieIndividuel` separee pour les sessions hors promotion. Ecartee car elle duplique la structure et toute la logique de conflits/agregation devrait interroger deux tables (UNION polymorphe), introduisant une dette structurelle permanente, et contredit la formulation du besoin metier ("le cours a l'unite est en realite un cours planifie").

## Risques identifies

1. [CRITIQUE] Double inscription promo + individuelle sur le meme CoursPlanifie -> a bloquer dans `creerInscription` (verifier `coursPlanifie.getPromotion() == eleve.getPromotion()`).
2. [ELEVE] NPE potentiel sur `.getPromotion().getId()` une fois le champ nullable (PromotionService.updatePlanning, boucle conflits formateurs) -> audit complet requis.
3. [ELEVE] Migration de schema sur table existante (renommage `promotion_cours` -> `cours_planifie`, FK rendue nullable) -> verifier strategie de migration (Flyway/Liquibase ou ddl-auto) avant implementation.
4. [MOYEN] Decision a trancher : ajouter un champ `formateur` explicite sur `CoursPlanifie` ou garder la deduction via `cours.getFormateurs()`. Le besoin metier mentionne "formateur" comme attribut de la session.
5. [MOYEN] Renommage en cascade incomplet (entite, repository, DTO, exception, statut, service, controleurs, tests) -> verifier par grep + build complet.

## Livrable produit

- ai_doc/ANALYSIS__WI-20260611-FULLST-007__cours-planifie-inscription.md (modele de donnees, schema texte, regles d'agregation, options, plan d'action FULLST-008, risques)

## Files Touched

- ai_doc/ANALYSIS__WI-20260611-FULLST-007__cours-planifie-inscription.md (nouveau)
- ai_memory/INDEX.md (ligne ANALYSIS ajoutee)
- ai_memory/WORK_ITEMS.md (statut WI-20260611-FULLST-007 -> READY_FOR_REVIEW)

## Evidence

- backend/src/main/java/fr/eni/gestionformation/entity/PromotionCours.java (FK promotion_id NOT NULL, ligne 21-23)
- backend/src/main/java/fr/eni/gestionformation/service/PromotionService.java (updatePlanning, conflits formateurs lignes 153-190)
- backend/src/main/java/fr/eni/gestionformation/repository/PromotionCoursRepository.java (findOverlappingForFormateur)
- backend/src/main/java/fr/eni/gestionformation/entity/User.java (User.promotion @ManyToOne nullable)

## Open Blockers

Aucun. L'analyse peut servir de base immediate a FULLST-008 (implementation) et FULLST-009 (frontend).

## Next Actions

- FULLST-008 (developer) : implementer le modele selon le plan d'action de l'analyse, en commencant par verifier la strategie de migration de schema et trancher la question du champ `formateur` explicite sur `CoursPlanifie`.
- FULLST-009 (developer, depend de FULLST-008) : calendrier eleve agrege + vue formateur des inscrits.

## Recall Hints

- Si on cherche "pourquoi PromotionCours a ete renomme en CoursPlanifie" -> voir cette analyse, section Recommandation.
- Si on cherche la regle d'agregation calendrier eleve / inscrits formateur -> section "Regles d'agregation" du document ANALYSIS.

## Proposed Rules

- TYPE: DECISION
  Title: CoursPlanifie decouple de Promotion (lien optionnel)
  Scope: backend/src/main/java/fr/eni/gestionformation/entity/ (CoursPlanifie, InscriptionCours, Promotion, User)
  Rule: Une session de cours planifiee (CoursPlanifie) peut exister sans Promotion liee (promotion_id nullable) ; l'inscription individuelle d'un eleve a une session se fait via une entite InscriptionCours separee, jamais en forcant une Promotion factice.
  Why: Permet de representer les "cours a l'unite" comme un cas particulier de session planifiee plutot que de dupliquer le modele de planification (BACKEN-016/018).
  How to apply: Tout nouveau code touchant CoursPlanifie doit gerer promotion == null comme un cas valide (pas une erreur), et toute requete d'agregation (planning eleve, inscrits d'une session) doit faire l'UNION promotion + InscriptionCours.
  Evidence: ai_doc/ANALYSIS__WI-20260611-FULLST-007__cours-planifie-inscription.md

- TYPE: PITFALL
  Title: Contrainte unique obligatoire sur InscriptionCours
  Scope: backend/src/main/java/fr/eni/gestionformation/entity/InscriptionCours.java
  Rule: La table InscriptionCours doit porter une contrainte UNIQUE(user_id, cours_planifie_id) en base, pas seulement une verification applicative.
  Why: Evite les doublons d'inscription et les inscriptions redondantes promo+individuel sur la meme session, qui fausseraient les comptages d'effectifs.
  How to apply: Declarer @Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "cours_planifie_id"})) des la creation de l'entite.
  Evidence: ai_doc/ANALYSIS__WI-20260611-FULLST-007__cours-planifie-inscription.md, section Risques point 1
