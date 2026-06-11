# WI-20260610-BACKEN-003

## Work Item
WI-20260610-BACKEN-003 — Migration vers un catalogue global de cours (CursusCours liaison ordonnée)

## Role
developer

## Status
DONE

## Scope
Backend uniquement (entities, repositories, DTOs, services, controllers) sur le module Cours/Cursus.
Pas de modification au champ `prerequis` (réservé à WI-20260610-BACKEN-004).
Pas de migration SQL écrite (ddl-auto=update sur application.properties et application-local.properties).

## Files Touched
- backend/src/main/java/fr/eni/gestionformation/entity/Cours.java (suppression `cursus` ManyToOne)
- backend/src/main/java/fr/eni/gestionformation/entity/Cursus.java (remplacement `cours` par `cursusCours` OneToMany + @OrderBy)
- backend/src/main/java/fr/eni/gestionformation/entity/CursusCours.java (NOUVEAU)
- backend/src/main/java/fr/eni/gestionformation/repository/CoursRepository.java (suppression findByCursusId)
- backend/src/main/java/fr/eni/gestionformation/repository/CursusCoursRepository.java (NOUVEAU)
- backend/src/main/java/fr/eni/gestionformation/dto/CoursRequest.java (suppression cursusId)
- backend/src/main/java/fr/eni/gestionformation/dto/CoursResponse.java (suppression cursusId/cursusName)
- backend/src/main/java/fr/eni/gestionformation/dto/CoursInCursusResponse.java (NOUVEAU)
- backend/src/main/java/fr/eni/gestionformation/dto/AddCoursToCursusRequest.java (NOUVEAU)
- backend/src/main/java/fr/eni/gestionformation/dto/ReorderCoursRequest.java (NOUVEAU)
- backend/src/main/java/fr/eni/gestionformation/dto/CursusResponse.java (ajout `List<CoursInCursusResponse> cours`)
- backend/src/main/java/fr/eni/gestionformation/service/CoursService.java (suppression findByCursusId, deleteById nettoie CursusCours)
- backend/src/main/java/fr/eni/gestionformation/service/CursusService.java (deleteById adapté, addCours/removeCours/reorderCours/getCoursOrdonnes ajoutés)
- backend/src/main/java/fr/eni/gestionformation/controller/CoursController.java (suppression GET /cursus/{id}, POST sans cursusId)
- backend/src/main/java/fr/eni/gestionformation/controller/CursusController.java (toResponse construit la liste ordonnée, 3 nouveaux endpoints)

## Evidence
```
cd backend && ./gradlew compileJava -q   -> BUILD OK (2 warnings résiduels corrigés ensuite)
cd backend && ./gradlew test             -> BUILD SUCCESSFUL, 4 actionable tasks: 4 up-to-date, aucun test en échec
```
Aucun test existant ne référençait `Cours.cursus`, `findByCursusId`, ou les anciens champs des DTOs (vérifié par grep) — aucune adaptation de test nécessaire.

## Decisions
1. **`CoursService.deleteById(id)`** : supprime d'abord toutes les `CursusCours` référençant ce cours (`findByCoursId` + `deleteAll`), puis supprime le `Cours`. Un cours catalogue peut donc être retiré du catalogue même s'il est utilisé dans un ou plusieurs cursus — il est simplement retiré de toutes les listes ordonnées concernées. Choix documenté car potentiellement surprenant côté UX (perte silencieuse de la présence du cours dans des cursus) ; à signaler au PO si un avertissement de confirmation est souhaité côté frontend.

2. **`CursusService.deleteById(id, cascade)`** : comme les `Cours` sont désormais indépendants du `Cursus` (catalogue global), il n'y a plus de notion de "cours orphelins" à gérer différemment selon `cascade`. Choix retenu : dans les deux cas (`cascade` true ou false), on supprime uniquement les lignes `CursusCours` du cursus (jamais les `Cours` eux-mêmes), puis le `Cursus`. Le paramètre `cascade` est conservé dans la signature pour compatibilité d'API (le contrôleur et le frontend l'envoient toujours) mais n'a plus d'effet différenciant. Si le PO souhaite un comportement distinct (ex: `cascade=false` doit échouer si le cursus contient encore des cours), il faudra le spécifier dans un WI ultérieur.

3. **`@OrderBy("ordre ASC")`** ajouté sur `Cursus.cursusCours` pour garantir l'ordre de la collection JPA, en complément du tri explicite fait dans `CursusService.getCoursOrdonnes` (Comparator) et de la requête `findByCursusIdOrderByOrdre`.

4. **`reorderCours`** valide stricte égalité d'ensemble (et de taille) entre les coursId actuellement liés et la liste fournie ; lève `IllegalArgumentException` sinon (conforme à la consigne).

5. Pas de `@PreAuthorize` ajouté sur les nouveaux endpoints car aucune annotation de ce type n'existait sur `CoursController`/`CursusController` avant cette modification (vérifié par lecture des deux fichiers) — la politique de sécurité existante (absence d'annotation au niveau contrôleur) a été reproduite à l'identique pour rester cohérent. Si une politique de sécurité globale est appliquée ailleurs (filtre/config Spring Security), elle s'applique automatiquement aux nouveaux endpoints sans modification nécessaire ici.

## Open Blockers
Aucun.

## Next Actions
- WI-20260610-BACKEN-004 : ajout du champ `prerequis` sur `Cours` (même entité, scope distinct).
- Le frontend (utilisé par `frontend/src/app/features/.../cours` et `cursus`) devra être adapté pour : ne plus envoyer `cursusId` dans `CoursRequest`, consommer `CursusResponse.cours` (liste ordonnée) au lieu de `GET /api/cours/cursus/{id}`, et utiliser les 3 nouveaux endpoints (`POST/DELETE/PUT reorder` sur `/api/cursus/{id}/cours`).

## Recall Hints
- Entité de liaison: `CursusCours` (cursus_id, cours_id, ordre), unique (cursus_id, cours_id)
- Nouveaux endpoints: `POST /api/cursus/{id}/cours`, `DELETE /api/cursus/{id}/cours/{coursId}`, `PUT /api/cursus/{id}/cours/reorder`
- Endpoint supprimé: `GET /api/cours/cursus/{cursusId}`
- `CursusService.getCoursOrdonnes(cursusId)` retourne `List<CursusCours>` triée par ordre, utilisé par `CursusController.toResponse`

## Proposed Rules
- TYPE: DECISION
  Title: Suppression de cours catalogue avec liaisons existantes
  Scope: backend/.../service/CoursService.java, entité Cours/CursusCours
  Rule: La suppression d'un `Cours` du catalogue retire silencieusement toutes ses liaisons `CursusCours` dans tous les cursus, sans confirmation ni blocage.
  Why: Permet de garder `Cours` totalement indépendant des `Cursus` (catalogue global réutilisable) tout en gardant `deleteById` simple.
  How to apply: Si un avertissement UX "ce cours est utilisé dans N cursus" est requis avant suppression, l'ajouter côté frontend ou ajouter un endpoint de vérification (`existsByCoursId` est déjà disponible dans `CursusCoursRepository`).
  Evidence: backend/src/main/java/fr/eni/gestionformation/service/CoursService.java (deleteById)
