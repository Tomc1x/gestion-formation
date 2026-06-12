# Role Note — developer

Work Item: WI-20260611-FULLST-008
Role: developer
Status: DONE

## Scope interpretation

Implementer le modele decrit dans ai_doc/ANALYSIS__WI-20260611-FULLST-007__cours-planifie-inscription.md
(Option 1 retenue) : renommer PromotionCours -> CoursPlanifie (lien promotion
nullable), ajouter InscriptionCours (eleve <-> CoursPlanifie, UNIQUE), et
exposer les 3 endpoints d'agregation/inscription proposes par l'analyse,
en reutilisant PlanificationService et la detection de conflits formateurs
sans les dupliquer. Pas de Flyway/Liquibase (absent du projet) -> ddl-auto=update.

## Decisions

1. **Champ `formateur` explicite sur CoursPlanifie : NON ajoute.** Garde la
   deduction existante via `cours.getFormateurs()`. L'analyse mentionnait
   ce point comme "a trancher" mais "documenter comme dette si jugé trop
   large". Choisi de ne pas l'ajouter pour limiter le diff a ce WI ; si
   FULLST-009 a besoin d'afficher un formateur unique par session, il
   faudra soit ajouter ce champ (migration supplementaire), soit exposer
   `cours.formateurs` dans CoursPlanifieResponse.

2. **Renommage PromotionCoursStatut -> CoursPlanifieStatut** effectue
   (coherence de nommage), valeurs inchangees (PLANIFIE/EN_COURS/TERMINE).

3. **Migration de schema** : pas de Flyway/Liquibase. `ddl-auto=update`
   (profil local uniquement, cf application.properties). Le renommage de
   l'entite cree une nouvelle table `cours_planifie` au prochain demarrage
   Hibernate ; l'ancienne table `promotion_cours` (si elle existe deja en
   environnement de dev local) reste orpheline et n'est pas droppee
   automatiquement. Aucune donnee de production n'est concernee (projet en
   developpement). A nettoyer manuellement par qui gere sa BDD locale si
   besoin (`DROP TABLE promotion_cours;`).

4. **NPE audit** : tous les `.getPromotion()` sur CoursPlanifie dans
   PromotionService.updatePlanning sont desormais gardes par `!= null` :
   - verification d'appartenance `coursPlanifie.getPromotion() == null ||
     !coursPlanifie.getPromotion().getId().equals(promotionId)` -> leve
     CoursPlanifieNotFoundException si vrai (un CoursPlanifie sans
     promotion ne peut pas etre mis a jour via cet endpoint promo-scoped,
     ce qui est correct).
   - boucle de conflits formateurs : filtre
     `pc.getPromotion() != null && !pc.getPromotion().getId().equals(promotionId)`
     pour eviter le NPE sur les CoursPlanifie sans promotion retournes par
     `findOverlappingForFormateur`.

5. **Double-couverture promo+individuel** : `creerInscription` rejette
   (409 InscriptionAlreadyExistsException) si
   `coursPlanifie.getPromotion() != null && eleve.getPromotion() != null &&
   coursPlanifie.getPromotion().getId().equals(eleve.getPromotion().getId())`,
   AVANT meme de verifier l'existence d'une InscriptionCours -> empeche
   toute inscription individuelle redondante pour un eleve deja couvert par
   sa promotion sur cette session.

6. **SecurityConfig** : ajout de regles pour `/api/cours-planifies/**` et
   `/api/eleves/*/planning`. GET inscrits ouvert aux FORMATEUR en plus de
   ADMIN/REFERENT (besoin metier explicite). GET planning eleve laisse
   `authenticated()` (pas de verification d'ownership cote backend dans ce
   WI - a affiner si necessaire, ex: un ETUDIANT pourrait consulter le
   planning d'un autre eleve via son uid).

## Files Touched

Renommages (mv + edits) :
- backend/src/main/java/fr/eni/gestionformation/entity/PromotionCours.java -> CoursPlanifie.java
- backend/src/main/java/fr/eni/gestionformation/entity/PromotionCoursStatut.java -> CoursPlanifieStatut.java
- backend/src/main/java/fr/eni/gestionformation/repository/PromotionCoursRepository.java -> CoursPlanifieRepository.java
- backend/src/main/java/fr/eni/gestionformation/dto/PromotionCoursResponse.java -> CoursPlanifieResponse.java
- backend/src/main/java/fr/eni/gestionformation/exception/PromotionCoursNotFoundException.java -> CoursPlanifieNotFoundException.java

Modifies :
- backend/src/main/java/fr/eni/gestionformation/service/PromotionService.java (renommage cascade + null-guards)
- backend/src/main/java/fr/eni/gestionformation/service/PlanificationService.java (renommage cascade)
- backend/src/main/java/fr/eni/gestionformation/controller/PromotionController.java (renommage cascade)
- backend/src/main/java/fr/eni/gestionformation/dto/PromotionResponse.java (type planning -> CoursPlanifieResponse)
- backend/src/main/java/fr/eni/gestionformation/exception/GlobalExceptionHandler.java (handlers renommes + 2 nouveaux)
- backend/src/main/java/fr/eni/gestionformation/security/SecurityConfig.java (regles cours-planifies + eleves/planning)
- backend/src/test/java/fr/eni/gestionformation/service/PromotionServiceTest.java (renommage cascade via sed)
- backend/src/test/java/fr/eni/gestionformation/service/PlanificationServiceTest.java (renommage cascade via sed)

Nouveaux fichiers :
- backend/src/main/java/fr/eni/gestionformation/entity/InscriptionCours.java
- backend/src/main/java/fr/eni/gestionformation/repository/InscriptionCoursRepository.java
- backend/src/main/java/fr/eni/gestionformation/exception/InscriptionAlreadyExistsException.java
- backend/src/main/java/fr/eni/gestionformation/exception/InscriptionNotFoundException.java
- backend/src/main/java/fr/eni/gestionformation/dto/InscriptionCoursRequest.java
- backend/src/main/java/fr/eni/gestionformation/dto/InscriptionCoursResponse.java
- backend/src/main/java/fr/eni/gestionformation/dto/OrigineInscription.java
- backend/src/main/java/fr/eni/gestionformation/dto/InscritResponse.java
- backend/src/main/java/fr/eni/gestionformation/dto/PlanningEleveResponse.java
- backend/src/main/java/fr/eni/gestionformation/service/InscriptionCoursService.java
- backend/src/main/java/fr/eni/gestionformation/controller/InscriptionCoursController.java
- backend/src/test/java/fr/eni/gestionformation/service/InscriptionCoursServiceTest.java (7 tests)

## Endpoints (pour FULLST-009)

- `POST /api/cours-planifies/{id}/inscriptions` body `{ "eleveId": Long }`
  -> 201 `InscriptionCoursResponse { id, eleveId, coursPlanifieId, dateInscription }`
  -> 409 si deja couvert par promo ou doublon individuel
  -> Roles : ADMINISTRATEUR, REFERENTE_ADMINISTRATIVE
- `DELETE /api/cours-planifies/{id}/inscriptions/{eleveId}` -> 204 / 404
  -> Roles : ADMINISTRATEUR, REFERENTE_ADMINISTRATIVE
- `GET /api/cours-planifies/{id}/inscrits` -> 200 `List<InscritResponse>`
  `{ eleveId, firstName, lastName, origine: PROMOTION|INDIVIDUEL }`
  -> Roles : ADMINISTRATEUR, REFERENTE_ADMINISTRATIVE, FORMATEUR
- `GET /api/eleves/{id}/planning` -> 200 `List<PlanningEleveResponse>`
  `{ coursPlanifieId, coursId, coursNom, dateDebut, dateFin, ordre, statut, origine }`
  -> Roles : authenticated (pas de verif ownership)

## Evidence

- `cd backend && ./gradlew test --rerun --console=plain` -> BUILD SUCCESSFUL
- `build/test-results/test/TEST-fr.eni.gestionformation.service.InscriptionCoursServiceTest.xml`
  -> tests="7" failures="0" errors="0"
- Compilation full project (compileJava + compileTestJava) sans erreur
  apres renommage en cascade (verifie par grep -r "PromotionCours" -> 0 match
  dans backend/src).

## Open Blockers

Aucun.

## Next Actions

- FULLST-009 (developer) : consommer les 4 endpoints ci-dessus cote
  frontend (calendrier eleve agrege avec badge origine, vue formateur des
  inscrits, formulaire d'inscription individuelle). Verifier si le champ
  `formateur` par session est necessaire pour l'affichage (cf decision 1) -
  si oui, prevoir un sous-WI backend pour l'ajouter sur CoursPlanifie.
- Si l'environnement de dev local a deja une table `promotion_cours`
  peuplee, `DROP TABLE promotion_cours;` apres verification (ddl-auto=update
  ne la supprime pas).

## Recall Hints

- "Pourquoi PromotionCours s'appelle CoursPlanifie maintenant" -> ce WI +
  ai_doc/ANALYSIS__WI-20260611-FULLST-007__cours-planifie-inscription.md
- "Comment eviter une double inscription promo+individuel" ->
  InscriptionCoursService.creerInscription, verification
  coursPlanifie.getPromotion() == eleve.getPromotion()
- "Endpoints inscriptions/planning eleve" -> InscriptionCoursController

## Proposed Rules

- TYPE: CONVENTION
  Title: Renommage PromotionCours -> CoursPlanifie (entite pivot planning)
  Scope: backend/src/main/java/fr/eni/gestionformation/{entity,repository,dto,service,controller,exception} (planning de cours)
  Rule: L'entite anciennement nommee PromotionCours s'appelle desormais CoursPlanifie (statut: CoursPlanifieStatut, repo: CoursPlanifieRepository, DTO: CoursPlanifieResponse, exception: CoursPlanifieNotFoundException). Toute nouvelle fonctionnalite touchant le planning de cours doit utiliser ce nommage, et `promotion` sur CoursPlanifie est nullable (toujours null-checker).
  Why: Decouple "session de cours planifiee" de "appartenance a une promotion" pour supporter les cours a l'unite (WI-20260611-FULLST-007/008).
  How to apply: grep -r "PromotionCours" doit retourner 0 resultat dans backend/src ; tout nouveau code doit gerer coursPlanifie.getPromotion() == null comme cas valide.
  Evidence: WI-20260611-FULLST-008, ai_doc/ANALYSIS__WI-20260611-FULLST-007__cours-planifie-inscription.md

- TYPE: PITFALL
  Title: ddl-auto=update et renommage d'entite -> table orpheline
  Scope: backend (JPA/Hibernate, profil local, ddl-auto=update)
  Rule: Renommer une entite/table JPA sans Flyway/Liquibase (ddl-auto=update) cree une nouvelle table mais ne supprime pas l'ancienne ; elle reste orpheline en base locale.
  Why: Le projet n'a pas d'outillage de migration ; ce comportement peut surprendre (deux tables similaires coexistent : promotion_cours orpheline + cours_planifie nouvelle).
  How to apply: Apres un renommage d'entite, documenter dans la note de role le DROP TABLE manuel a executer en dev local ; envisager Flyway si ce type de renommage devient frequent.
  Evidence: WI-20260611-FULLST-008 (renommage PromotionCours -> CoursPlanifie)
