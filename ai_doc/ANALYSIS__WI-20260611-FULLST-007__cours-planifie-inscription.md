# Analysis - Modele CoursPlanifie / InscriptionCours (inscriptions promo + a l'unite)

Date: 2026-06-11
Mode: DEEP
Work Item: WI-20260611-FULLST-007
Probleme: Le modele actuel `PromotionCours` fusionne deux notions distinctes - "session planifiee d'un cours" (dates, statut, ordre) et "lien obligatoire vers une Promotion". Il n'existe aucune entite pour representer l'inscription individuelle d'un eleve a une session de cours hors promotion ("cours a l'unite"). Le formateur n'a pas de vue agregee des eleves inscrits (promo + individuel) sur une session.

## Analyse du probleme

### Cause racine

`PromotionCours` porte une contrainte `@JoinColumn(name = "promotion_id", nullable = false)`. Cette contrainte FK NOT NULL empeche structurellement de representer une session de cours qui n'appartient a aucune promotion (le cas "cours a l'unite"). Le couplage fort entre "planification d'une session" et "appartenance a une promotion" est la cause racine : ce sont deux concepts orthogonaux qui doivent etre decouples.

Par ailleurs, l'inscription d'un eleve a un cours est aujourd'hui implicite et globale : `User.promotion` (un seul `@ManyToOne`) determine indirectement tout le planning de l'eleve via `PromotionCours.promotion`. Il n'y a pas de table de jointure eleve <-> session permettant une inscription ad-hoc, individuelle, sans toucher `User.promotion`.

### Contraintes reelles identifiees

- Stack : Spring Boot 3.3.5 / Java 21, JPA/Hibernate.
- PlanificationService (WI-20260610-BACKEN-016) genere automatiquement le planning d'une promotion en creant des `PromotionCours` a partir de `CursusCours` (ordre, duree). Il ne gere que le cas "promotion".
- Detection de conflits formateurs (WI-20260610-BACKEN-018, implementee dans `PromotionCoursRepository.findOverlappingForFormateur` + `PromotionService.updatePlanning`) interroge `PromotionCours` en supposant systematiquement une `promotion` non nulle (ligne `pc.getPromotion().getId()`).
- `PromotionCoursResponse` (DTO) et les controleurs lies exposent l'entite dans son role "planning de promotion" - ils devront continuer a fonctionner pour ce cas d'usage apres refonte.
- `Cours.formateurs` est un `List<User>` (ManyToMany) - une session planifiee (`PromotionCours` actuel) n'a pas de champ `formateur` dedie : le formateur est deduit de `cours.getFormateurs()`. Le besoin metier mentionne "CoursPlanifie : ... formateur ...", ce qui suggere d'introduire un champ `formateur` explicite - point a trancher (voir Risques).

## Contraintes

- Aucune regle dans `ai_rules/INDEX.md` ne couvre ce sujet (decisions existantes portent sur stack/downgrade Java, pas sur ce domaine metier).
- Le renommage de `PromotionCours` en `CoursPlanifie` est un breaking change de schema (renommage de table/colonnes, FK rendue nullable) : a cadrer comme migration explicite, pas comme simple refactor d'entite JPA. Le developer (FULLST-008) doit verifier la strategie de migration (Flyway/Liquibase ou ddl-auto) avant de renommer.

## Options

### Option 1 - Renommer/refondre PromotionCours en CoursPlanifie (lien promotion nullable) + nouvelle entite InscriptionCours

Renommer l'entite existante, rendre `promotion` optionnel (`@ManyToOne(optional = true)`, colonne nullable), ajouter une entite de jointure `InscriptionCours` (eleve_id, cours_planifie_id, date_inscription). Toute la logique de planification automatique et de detection de conflits formateurs continue de fonctionner sur `CoursPlanifie`, simplement avec `promotion` pouvant etre null.

Avantages :
- Un seul concept "session planifiee", coherent avec le besoin metier ("le cours a l'unite est en realite un cours planifie sans promotion").
- Reutilise directement PlanificationService et la detection de conflits (BACKEN-018) : ces services raisonnent deja sur des sessions avec dates+formateur, le filtre `promotion != null` devient juste une condition supplementaire.
- Une seule table a requeter pour le planning agrege d'un eleve (UNION simple entre "sessions de ma promotion" et "sessions ou il a une InscriptionCours").

Inconvenients / risques :
- Renommage de table = migration de schema (rename table + colonnes potentiellement, ou nouvelle table + copie). Si pas de Flyway/Liquibase en place, risque de divergence dev/prod selon ddl-auto.
- Tout le code existant referencant `PromotionCours` (entite, repository, DTO, exception, service, controleur, tests) doit etre renomme - ratissage large mais mecanique.
- `findOverlappingForFormateur` et `updatePlanning` doivent etre audites ligne par ligne pour gerer `promotion == null` (ex: `pc.getPromotion().getId().equals(promotionId)` levera un NPE si `promotion` est null).

Effort : moyen (renommage mecanique + quelques branches conditionnelles `promotion != null`).
Compatibilite stack : totale - pur JPA/Hibernate, pas de nouvelle dependance.

### Option 2 - Garder PromotionCours tel quel (promo obligatoire) + nouvelle entite CoursPlanifieIndividuel separee pour les sessions hors promotion

Creer une entite distincte `CoursPlanifieIndividuel` (memes champs que `PromotionCours` mais sans `promotion`), plus `InscriptionCours` qui reference soit l'un soit l'autre.

Avantages :
- Aucun renommage, aucune migration sur la table existante `PromotionCours` - moindre risque de regression sur BACKEN-016/018.

Inconvenients / risques :
- Duplication de structure (deux entites quasi identiques : dates, statut, ordre, lien cours).
- Toute logique transverse (conflits formateurs, calendrier eleve agrege, vue formateur "tous les inscrits") doit interroger deux tables et faire l'UNION en memoire ou en SQL avec des types polymorphes - complexite et duplication dans PlanificationService, PromotionCoursRepository, et le futur InscriptionCoursService.
- Contredit directement la formulation du besoin metier ("ce cours a l'unite est en realite un cours planifie") - introduit artificiellement deux concepts la ou il n'y en a qu'un.

Effort : moyen a eleve (nouvelle entite + repository + toute la logique de conflits/agregation dupliquee ou polymorphe).
Compatibilite stack : totale, mais dette technique immediate (duplication).

## Recommandation

Option 1 : renommer/refondre `PromotionCours` en `CoursPlanifie` avec `promotion` rendu optionnel (`nullable = true`), et ajouter `InscriptionCours` comme nouvelle entite de jointure eleve <-> CoursPlanifie.

Justification :
- C'est l'option qui colle exactement a la formulation du besoin metier ("un cours a l'unite est en realite un cours planifie").
- Elle evite la duplication de la logique de planification automatique et de detection de conflits formateurs (BACKEN-016/018), investissements recents et non triviaux (chevauchements de dates, ordre chronologique, conflits formateurs cross-promotion).
- Le cout de migration (renommage) est ponctuel et mecanique, alors que l'Option 2 introduirait une dette structurelle permanente (deux tables a maintenir en parallele).

Cette recommandation serait invalidee si :
- Il existe deja une volumetrie de donnees de production significative sur `promotion_cours` rendant le renommage risque sans outillage de migration (Flyway/Liquibase) - dans ce cas, prevoir une migration en deux etapes (nouvelle table + bascule progressive) plutot qu'un renommage in-place.
- Le besoin metier evolue pour exiger des champs structurellement differents entre "session de promo" et "session a l'unite" (peu probable au vu de l'enonce).

## Plan d'action pour le developer (FULLST-008)

1. Verifier la strategie de migration de schema du projet (chercher flyway/liquibase dans build.gradle/application.yml, sinon noter que ddl-auto gere le renommage - documenter le choix dans la note de role).

2. Renommer l'entite `PromotionCours` en `CoursPlanifie` (`backend/src/main/java/fr/eni/gestionformation/entity/PromotionCours.java`) :
   - Renommer la classe et le fichier.
   - Rendre `promotion` optionnel : `@ManyToOne(optional = true)` + `@JoinColumn(name = "promotion_id", nullable = true)`.
   - Conserver `cours`, `dateDebut`, `dateFin`, `ordre`, `statut` (renommer `PromotionCoursStatut` en `CoursPlanifieStatut` si coherence souhaitee - optionnel, a arbitrer par coherence de nommage).
   - Decision a prendre par le developer : ajouter ou non un champ `formateur` (`@ManyToOne User`) explicite sur `CoursPlanifie`. Recommandation : l'ajouter et le rendre nullable, peuple automatiquement par PlanificationService (ex: premier formateur de cours.getFormateurs() ou choix manuel) - cela simplifie findOverlappingForFormateur (filtrer directement sur coursPlanifie.formateur au lieu de parcourir cours.formateurs). Si juge trop large pour ce WI, documenter comme dette et garder le comportement actuel.

3. Renommer en cascade : `PromotionCoursRepository` -> `CoursPlanifieRepository`, `PromotionCoursResponse` -> `CoursPlanifieResponse`, `PromotionCoursNotFoundException` -> `CoursPlanifieNotFoundException`, `PromotionCoursStatut` (si renomme). Mettre a jour tous les usages dans PromotionService, PlanificationService, controleurs, tests.

4. Adapter PromotionService et PlanificationService :
   - getPlanning(Long promotionId), updatePlanning(...) : continuent de filtrer par promotion.id - ajouter une garde `promotion != null` partout ou .getPromotion() est derefence sans verification (ligne `promotionCours.getPromotion().getId().equals(promotionId)` dans updatePlanning, et le filtre `pc.getPromotion().getId().equals(promotionId)` dans la boucle de conflits formateurs).
   - findOverlappingForFormateur : la requete actuelle joint pc.cours.formateurs - verifier qu'elle continue de fonctionner pour des CoursPlanifie sans promotion (elle le devrait, car elle ne filtre pas sur promotion).
   - Ajouter une methode repository findByCoursAndPromotionIsNull(...) ou equivalent pour lister/creer les sessions "a l'unite".

5. Creer l'entite InscriptionCours (backend/src/main/java/fr/eni/gestionformation/entity/InscriptionCours.java) :
   - Champs : id, eleve (@ManyToOne User, FK user_id not null), coursPlanifie (@ManyToOne CoursPlanifie, FK not null), dateInscription (LocalDate, defaut now).
   - Contrainte d'unicite : @Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "cours_planifie_id"})) pour empecher une double inscription du meme eleve sur la meme session.
   - Repository InscriptionCoursRepository avec au minimum findByEleveId, findByCoursPlanifieId, existsByEleveIdAndCoursPlanifieId.

6. Service InscriptionCoursService (nouveau) :
   - creerInscription(eleveId, coursPlanifieId) : valider que l'eleve n'est pas deja inscrit (table InscriptionCours) ET verifier qu'il n'est pas deja couvert par sa promotion sur ce meme CoursPlanifie (cf. Risques - eviter double couverture).
   - getPlanningEleve(eleveId) : UNION des CoursPlanifie ou coursPlanifie.promotion == eleve.getPromotion() (si non null) ET des CoursPlanifie referencees par les InscriptionCours de l'eleve. Dedupliquer par id.
   - getInscritsCombines(coursPlanifieId) : UNION des eleves de coursPlanifie.getPromotion() (via userRepository.findByPromotionId) ET des eleves ayant une InscriptionCours sur ce coursPlanifie. Dedupliquer par uid.

7. Endpoints API (nouveau controleur InscriptionCoursController, ou extension de CoursController) :
   - POST /api/cours-planifies/{id}/inscriptions (body: { eleveId }) - creer une inscription individuelle. Reponse 201 + DTO inscription, ou 409 si deja inscrit (promo ou individuel).
   - GET /api/cours-planifies/{id}/inscrits - liste combinee des eleves inscrits (promo + individuel), avec un champ origine: PROMOTION | INDIVIDUEL par eleve pour permettre l'affichage distinct cote frontend (FULLST-009).
   - GET /api/eleves/{id}/planning (ou /api/me/planning si base sur l'utilisateur authentifie) - planning agrege de l'eleve (promo + individuel), avec le meme champ origine par session.
   - DELETE /api/cours-planifies/{id}/inscriptions/{eleveId} - desinscription individuelle (a prevoir pour coherence CRUD, meme si non explicitement demande).

8. Tests : tests unitaires InscriptionCoursService (creation, doublon promo+individuel rejete, agregation planning eleve, agregation inscrits formateur). Adapter CoursServiceTest et tout test existant referencant PromotionCours* au nouveau nommage.

### Anti-scope explicite

- Ne pas toucher a Cursus, CursusCours, Cours (catalogue) au-dela des renommages de references imposes par le renommage de PromotionCours.
- Ne pas modifier User.promotion (le lien eleve-promotion reste tel quel ; l'inscription individuelle est un mecanisme additif, pas un remplacement).
- Ne pas implementer le frontend (FULLST-009) - hors scope FULLST-008.
- Ne pas introduire Flyway/Liquibase si absent du projet, sauf si explicitement decide en amont avec le manager.

## Schema cible (texte)

```
Cours (catalogue)
  id, name, dureeJours
  formateurs (M:N -> User)
  prerequis (M:N -> Cours)

CoursPlanifie  [ex-PromotionCours, renomme]
  id
  cours_id        -> Cours        (NOT NULL)
  promotion_id    -> Promotion    (NULLABLE)   <-- changement cle
  formateur_id    -> User         (NULLABLE, optionnel - a trancher)
  dateDebut, dateFin, ordre, statut

InscriptionCours [nouvelle entite]
  id
  user_id           -> User          (NOT NULL)
  cours_planifie_id -> CoursPlanifie (NOT NULL)
  dateInscription
  UNIQUE(user_id, cours_planifie_id)

User
  ... promotion_id -> Promotion (NULLABLE, inchange)
```

### Regles d'agregation

- Calendrier eleve (eleveId) =
  CoursPlanifie WHERE promotion_id = eleve.promotion_id (si eleve.promotion_id != null)
  UNION
  CoursPlanifie JOIN InscriptionCours WHERE InscriptionCours.user_id = eleveId
  (dedupliquer par CoursPlanifie.id)

- Inscrits d'un CoursPlanifie (coursPlanifieId) =
  User WHERE User.promotion_id = coursPlanifie.promotion_id (si coursPlanifie.promotion_id != null)
  UNION
  User JOIN InscriptionCours WHERE InscriptionCours.cours_planifie_id = coursPlanifieId
  (dedupliquer par User.uid)

## Risques et points d'attention

1. [CRITIQUE] Double inscription promo + individuelle sur le meme CoursPlanifie - Signal : un eleve membre de la promotion liee a un CoursPlanifie cree en plus une InscriptionCours sur ce meme CoursPlanifie (redondant, fausse les comptages). Mitigation : creerInscription doit verifier coursPlanifie.getPromotion() != null && coursPlanifie.getPromotion().equals(eleve.getPromotion()) et rejeter (409) si vrai - l'eleve est deja couvert.

2. [ELEVE] NPE sur getPromotion() apres rendre le champ nullable - Signal : PromotionService.updatePlanning et la boucle de conflits formateurs derefencent pc.getPromotion().getId() sans null-check. Mitigation : auditer chaque usage de .getPromotion() sur CoursPlanifie dans le code existant et ajouter les gardes necessaires (cf. Plan d'action etape 4).

3. [ELEVE] Migration de schema sur table existante - Signal : si la base contient deja des lignes promotion_cours avec des donnees reelles (environnements de dev/demo partages), un renommage de table/colonne sans script de migration peut provoquer une perte de donnees ou un echec au demarrage selon ddl-auto. Mitigation : le developer (FULLST-008) doit verifier la config JPA (spring.jpa.hibernate.ddl-auto) et la presence d'outils de migration avant de renommer ; si ddl-auto=update/create-drop en dev seulement, le risque est limite mais a documenter.

4. [MOYEN] Champ formateur sur CoursPlanifie - decision de scope - Signal : le besoin metier mentionne explicitement "formateur" comme attribut de CoursPlanifie, mais l'entite actuelle ne le porte pas (deduit via cours.getFormateurs(), potentiellement plusieurs). Mitigation : trancher explicitement (ajouter le champ vs. garder la deduction) et documenter le choix dans la note de role FULLST-008 - ne pas laisser une ambiguite implicite qui complexifierait FULLST-009 (affichage du formateur par session).

5. [MOYEN] Renommage en cascade incomplet - Signal : des references residuelles a PromotionCours/PromotionCoursStatut/PromotionCoursResponse/PromotionCoursNotFoundException subsistent apres renommage (compilation cassee ou code mort qui compile mais n'est jamais appele). Mitigation : recherche globale grep -r "PromotionCours" apres renommage, ./gradlew build pour valider la compilation complete.

## Rappels pour le developer (anti-patterns a eviter)

- Ne pas dupliquer la logique de detection de chevauchement de dates (chevauchement(...) dans PromotionService) - la reutiliser/extraire si elle doit servir aussi pour les sessions a l'unite.
- Ne pas calculer le planning agrege eleve / inscrits combines en chargeant toutes les entites en memoire pour un grand volume - privilegier des requetes JPQL avec UNION ou deux requetes ciblees + dedup en memoire (volumetrie attendue faible ici, mais eviter les findAll() suivis de filtres Java sur de grandes listes).
- Ne pas oublier la contrainte d'unicite (user_id, cours_planifie_id) sur InscriptionCours - c'est le garde-fou principal contre les doublons, pas seulement une verification applicative.
- Ne pas casser la compatibilite du DTO PromotionCoursResponse/CoursPlanifieResponse cote frontend existant (frontend/src/app/core/adapters/cours-*.ts, page cours.ts) sans coordination - si le renommage change la forme du JSON expose, verifier les adapters frontend concernes (hors scope direct de FULLST-008 mais impact a signaler).
