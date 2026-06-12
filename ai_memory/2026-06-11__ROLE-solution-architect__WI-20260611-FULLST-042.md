# Solution Architect Note — WI-20260611-FULLST-042

Work Item: WI-20260611-FULLST-042
Role: solution-architect
Status: ANALYSIS_DONE (no code written)
Mode: DEEP
Problème analysé: Permettre à une REFERENTE_ADMINISTRATIVE d'inscrire un élève à un `CoursPlanifie` individuel (hors promotion), bloquant par défaut les inscriptions "hors-ordre" par rapport au cursus, sauf si elle force explicitement (avec warning).

## Recommandation retenue

Validation backend stricte ("Option 1") : un cours est "hors-ordre" si au moins un cours de rang inférieur dans le `Cursus` de la promotion de l'élève n'a AUCUNE inscription (PROMOTION ou INDIVIDUEL) chez cet élève, quel que soit son statut. Référence d'ordre = `CursusCours.ordre` (ordre pédagogique du cursus), PAS `CoursPlanifie.ordre` (ordre de planning promotion, déjà utilisé par WI-027/029 pour un autre warning — ne pas mélanger).

## Options écartées

- **Option 2 (validation basée sur statut TERMINE des prérequis)** : rejetée — sur-ingénierie, faux positifs si un prérequis est `EN_COURS` mais bien planifié avant. Cahier des charges parle d'"ordre" (planification), pas de complétion réelle.
- **Option 3 (validation uniquement front, calcul JS)** : rejetée — règle métier doit être appliquée serveur, sinon contournable via appel API direct. Duplication logique front/back.

## Contrat API défini

### `InscriptionCoursRequest` (ajout)
```java
public class InscriptionCoursRequest {
    private Long eleveId;
    private boolean forcer; // default false
}
```

### Nouvelle exception `InscriptionHorsOrdreException`
```java
public class InscriptionHorsOrdreException extends RuntimeException {
    public InscriptionHorsOrdreException(Long eleveId, Long coursPlanifieId, List<String> coursManquants) {
        super("L'élève " + eleveId + " ... hors-ordre ... prérequis non couverts = " + coursManquants
            + ". Utilisez 'forcer=true' pour inscrire malgré tout.");
    }
}
```

### `GlobalExceptionHandler` (nouveau handler, suit le pattern existant — réponse texte)
```java
@ExceptionHandler(InscriptionHorsOrdreException.class)
public ResponseEntity<String> handleInscriptionHorsOrdre(InscriptionHorsOrdreException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage()); // 409
}
```

### `InscriptionCoursResponse` (ajout)
```java
private List<String> warnings; // vide si OK, sinon message(s) "hors-ordre forcé"
```

### Logique service `creerInscription` (pseudo)
```
1. Checks doublon existants -> INCHANGES (déjà corrects, NE PAS TOUCHER).
2. Si eleve.getPromotion() != null && promotion.getCursus() != null :
     prerequisManquants = calculerPrerequisManquants(eleve, coursPlanifieCible)
     si non vide :
       si !forcer -> throw InscriptionHorsOrdreException(...)
       si forcer  -> warnings.add("Inscription hors-ordre (forcée) : ...")
3. Si eleve sans promotion/cursus -> pas de validation hors-ordre (cf edge case ci-dessous).
4. Créer l'inscription comme avant, retourner (inscription, warnings).
```

`calculerPrerequisManquants` :
- `CursusCoursRepository.findByCursusIdOrderByOrdre(cursusId)` -> liste ordonnée.
- Trouver l'entrée du cours cible -> `ordreCible`. Si le cours cible n'appartient pas au cursus -> liste vide (pas de notion d'ordre applicable).
- Pour chaque `CursusCours` avec `ordre < ordreCible` : vérifier via `getPlanningEleve(eleveId)` (déjà existant) si l'élève a une inscription (PROMOTION ou INDIVIDUEL, tout statut) sur un `CoursPlanifie` de ce `Cours`. Si absent -> ajouter le nom du cours à la liste retournée.

## Flux UI retenu

Modale "Inscrire à l'unité — {coursNom}" ajoutée dans `frontend/src/app/features/promotions/promotion-detail/cours-planifies/cours-planifies-tab.{ts,html}`, sur chaque ligne de `CoursPlanifie` (bouton dédié à côté de "Modifier session"/"Retirer"). Réutilise `BaseEleveAdapter.getAll()` (existant, pas de nouvel endpoint de recherche). Champs : `<select>` élève + checkbox "Forcer l'inscription (hors-ordre cursus)" + bouton "Inscrire".

Comportement :
- 201 sans warnings -> succès, fermer modale.
- 409 `InscriptionAlreadyExistsException` -> message bloquant "déjà inscrit" (no regression scénario 9).
- 409 `InscriptionHorsOrdreException` -> afficher message, proposer de cocher "forcer" et resoumettre.
- 201 avec `warnings` non vide (forcer=true) -> encart `.warning-box` (pattern réutilisé de `sessionWarnings`).

## Plan d'implémentation pour le developer (résumé — détail complet ci-dessous)

### Backend
1. `dto/InscriptionCoursRequest.java` : ajouter `boolean forcer`.
2. `exception/InscriptionHorsOrdreException.java` (nouveau).
3. `exception/GlobalExceptionHandler.java` : ajouter handler 409 pour `InscriptionHorsOrdreException`, près de `handleInscriptionAlreadyExists`.
4. `dto/InscriptionCoursResponse.java` : ajouter `List<String> warnings`.
5. `service/InscriptionCoursService.java` :
   - `creerInscription(coursPlanifieId, eleveId, forcer)` -> retourne un record interne `InscriptionResult(InscriptionCours, List<String> warnings)`.
   - Nouvelle méthode privée `calculerPrerequisManquants(User eleve, CoursPlanifie cible)` (voir logique ci-dessus).
   - NE PAS toucher aux checks de doublon existants (lignes ~38-47 actuelles).
6. `controller/InscriptionCoursController.java` : passer `request.isForcer()`, adapter `toInscriptionResponse` pour inclure `warnings`.
7. Vérifier lazy-loading `CursusCours.cours` / noms de cours accessibles dans le contexte `@Transactional`.

### Frontend
8. `core/models/inscription.model.ts` : ajouter `forcer?: boolean` à la requête, `warnings: string[]` à la réponse.
9. `core/adapters/inscription.adapter.ts` : méthode abstraite `creerInscription(coursPlanifieId, {eleveId, forcer})`.
10. `core/adapters/inscription-http.adapter.ts` : implémenter `POST /api/cours-planifies/{id}/inscriptions`. Vérifier le pattern de gestion d'erreur 409 texte déjà utilisé ailleurs dans le projet (rester cohérent, ex. `FiliereAlreadyExistsException`).
11. `cours-planifies-tab.ts` : injecter `BaseEleveAdapter` + `BaseInscriptionAdapter`, ajouter signals (`inscriptionTarget`, `eleves`, `inscriptionForm`, `inscriptionError`, `inscriptionWarnings`, `inscriptionSubmitting`), méthodes `openInscriptionModal/closeInscriptionModal/submitInscription`.
12. `cours-planifies-tab.html` : bouton "Inscrire à l'unité" par ligne + nouvelle modale (select élève + checkbox forcer + `.warning-box` + `.form-api-error`).

### Anti-scope
- Ne pas modifier les checks de doublon existants dans `creerInscription`.
- Ne pas toucher aux warnings de planning existants (`updatePlanning`, `detecterConflitsFormateur`, warning "ordre chronologique du cursus non respecté" sur `CoursPlanifie.ordre`) — mécanisme distinct, ordre différent (planning promotion vs ordre cursus).
- Ne pas créer de nouvel endpoint de recherche élèves — réutiliser `GET /api/eleves`.
- Ne pas ajouter de section/données de démonstration (règle globale utilisateur).
- Respecter règles Angular du projet : `@if`/`@for`, signals, `OnPush`, pas de `ngClass`/`ngStyle`.

### Tests / validations attendus
- Tests unitaires `InscriptionCoursServiceTest` : hors-ordre sans forcer -> exception ; hors-ordre avec forcer -> succès + warnings ; en-ordre -> succès sans warnings ; non-régression doublon (scénario 9).
- Test API : `POST /api/cours-planifies/{id}/inscriptions` avec `forcer:false` -> 409 ; `forcer:true` -> 201 + warnings.
- Chrome-devtools : login REFERENTE_ADMINISTRATIVE, promotion avec cursus ≥2 cours ordonnés, tester inscription hors-ordre (refus, puis forcer -> succès + warning), retest doublon (scénario 9, no regression).

## Risques identifiés

1. **CRITIQUE** — Élève sans promotion (`eleve.getPromotion() == null`) : hypothèse retenue = inscription individuelle autorisée sans validation d'ordre (pas de cursus de référence). À CONFIRMER avec manager/PO avant implémentation finale — si le métier veut au contraire l'interdire, ça change le contrôle d'accès, pas juste la validation.
2. MOYEN — Cours cible hors du cursus de l'élève (cursus différent) -> autorisé sans warning par construction (cursus cible absent de `findByCursusIdOrderByOrdre`). À documenter dans le code.
3. MOYEN — Performance : `calculerPrerequisManquants` appelle `getPlanningEleve` (plusieurs requêtes) — acceptable pour une action ponctuelle, pas d'optimisation requise.
4. FAIBLE — Lazy-loading JPA sur `CursusCours.cours` / noms de cours pour messages — rester dans le contexte `@Transactional`.
5. FAIBLE — Pattern de gestion d'erreur 409 texte côté Angular à vérifier avant d'écrire le code d'affichage (cohérence avec le reste du front).

## Livrable produit

- Cette note : `ai_memory/2026-06-11__ROLE-solution-architect__WI-20260611-FULLST-042.md`
- WORK_ITEMS.md WI-20260611-FULLST-042 : Status OPEN -> READY_FOR_REVIEW

## Next Actions

- Manager : confirmer hypothèse risque #1 (élève sans promotion) avec le PO si possible, puis assigner au developer pour implémentation suivant le plan ci-dessus.
- Developer : implémenter backend puis frontend dans l'ordre indiqué, exécuter `./gradlew test` + `ng build` + chrome-devtools (scénarios 9 et 10).

## Proposed Rules

- TYPE: CONVENTION
  Title: Pattern "warning non bloquant" pour règles métier inscription/planning
  Scope: backend/src/main/java/fr/eni/gestionformation (DTOs *Response, GlobalExceptionHandler, services InscriptionCours/Promotion)
  Rule: Une règle métier "soft" (peut être contournée avec un flag explicite type `forcer`) doit suivre le pattern : exception dédiée -> 409 CONFLICT via GlobalExceptionHandler si flag absent/false ; si flag=true, ajouter un message dans `warnings: List<String>` du DTO de réponse plutôt que de bloquer.
  Why: Pattern déjà utilisé pour les warnings de planning (`CoursPlanifieResponse.warnings`, ordre chronologique, conflits formateur) et repris ici pour l'ordre cursus — garder l'homogénéité évite la divergence de formats d'erreur/avertissement entre modules.
  How to apply: Avant d'ajouter une nouvelle validation métier "souple", vérifier si un DTO de réponse existant porte déjà `warnings` ; sinon l'ajouter plutôt que créer un nouveau format.
  Evidence: ai_doc analysis WI-20260611-FULLST-042 ; backend/src/main/java/fr/eni/gestionformation/dto/CoursPlanifieResponse.java ; backend/src/main/java/fr/eni/gestionformation/service/PromotionService.java (detecterConflitsFormateur, updatePlanning)
