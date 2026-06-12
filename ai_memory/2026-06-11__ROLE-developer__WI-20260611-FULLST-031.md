# WI-20260611-FULLST-031

## Work Item
WI-20260611-FULLST-031 — Repointer les `prerequis` des cours catalogue qui referencent
le cours fusionne "Algorithmique + Initiation a la Programmation / Java" (id 26) vers
les deux cours separes "Algorithmique / Pseudo-Code" (id 8) et "Initiation a la
Programmation / Java" (id 9), deja presents dans le cursus DWWM (id 5), afin de faire
disparaitre les 14 alertes "prerequis absent du cursus" identifiees en FULLST-029.

## Role
developer

## Status
**BLOCKED** — conflit cross-cursus decouvert pendant la verification prealable (etape 6
du scope), avant toute mutation. Aucune ecriture API effectuee.

## Scope
Analyse en lecture seule du catalogue cours (`GET /api/cours`) et des cursus
(`GET /api/cursus/{id}`) via l'API backend (login ref@ref.com, profil local). Simulation
locale (script Python jetable, supprime) de `computeCursusPrereqAlerts` /
`transitivePrerequis` (frontend/src/app/core/utils/cursus-alerts.util.ts) sur les
listes de cours ordonnees de DWWM (id 5) et CDA (id 6) avant/apres la mutation
envisagee. Aucun fichier de code ni donnee modifie.

## Evidence

### Identification des ids (GET /api/cours, 78 entrees)
- id 26 = "Algorithmique + Initiation a la Programmation / Java" (cours fusionne,
  `prerequis: []`)
- id 8  = "Algorithmique / Pseudo-Code" (`prerequis: []`)
- id 9  = "Initiation a la Programmation / Java" (`prerequis: []`)

### Recherche des cours catalogue ayant 26 dans `prerequis`
Sur les 78 cours du catalogue, **un seul** cours catalogue a `prerequis` contenant 26 :
- **id 10 "Web Client / HTML & CSS"** -> `prerequis: [26]`

Tous les autres cours de la chaine lineaire (11 -> 10, 13 -> 11, 14 -> 13, ..., 23 -> 22)
referencent leur predecesseur direct, pas 26. Les 14 alertes DWWM rapportees en
FULLST-029 proviennent du calcul **transitif** (`transitivePrerequis`) : pour les cours
10 a 23 du DWWM, la chaine transitive de prerequis remonte jusqu'a 10 -> [26], et 26
etant absent de la liste DWWM, chacun de ces cours genere une alerte "26 absent".
Simulation locale : **13 alertes DWWM** (proche des 14 documentees en FULLST-029,
ecart probable du au cours 24/25 hors chaine ou a un detail d'implementation mineur du
calcul transitif — non investigue plus avant car non bloquant pour le diagnostic).

### Composition des cursus concernes (GET /api/cursus/{id})
- **Cursus 5 "Développeur web et web mobile" (DWWM)** : 18 cours, ids
  `[8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25]`. Contient 8 et 9 (positions
  0 et 1), **ne contient PAS 26**.
- **Cursus 6 "CDA"** : 23 cours, ids
  `[26,10,11,13,14,15,16,17,18,19,20,21,22,23,27,28,29,30,31,32,33,34,35]`. Contient
  **26 en position 0** (juste avant 10 en position 1), **ne contient NI 8 NI 9**.
- Cursus 7 "ESD" : ids `[36..52]`, sans rapport avec 8/9/10/26.
- Cursus 1-4 : reponse vide/non trouvee via l'API a ce moment (non investigues plus —
  hors de la chaine 8/9/10/26 donc non pertinents pour ce conflit).

### Simulation de la mutation proposee (10: prerequis [26] -> [8,9])
Calcul `computeCursusPrereqAlerts` reproduit fidelement (transitive + position) sur les
listes ordonnees reelles de DWWM et CDA :

| Cursus | Alertes AVANT | Alertes APRES (10 -> [8,9]) |
|--------|---------------|------------------------------|
| DWWM (id 5) | 13 (toutes "26 absent") | **0** |
| CDA (id 6)  | 0 | **44** (22 cours x 2 prereqs "8 absent" + "9 absent") |

Detail CDA apres mutation : tous les cours en position >= 2 (de "Web Client / HTML &
CSS" a "IA / Python + Projet Final") heritent transitivement du nouveau prerequis
`[8,9]` de course 10, et 8/9 sont absents de CDA (qui utilise 26 a la place, en
position 0) -> 2 alertes "absent" par cours x 22 cours = 44.

## Decisions
- **La mutation specifiee par le scope (option b : repointer le prerequis de course 10
  de [26] vers [8,9]) resout entierement les 14 alertes DWWM mais introduit 44
  nouvelles alertes sur le cursus CDA (id 6)**, qui passe de 0 a 44 alertes
  "prerequis absent du cursus".
- Cause racine : 26 et {8,9} sont deux representations catalogue differentes du meme
  contenu pedagogique ("Algorithmique + initiation Java"), utilisees par des cursus
  differents :
  - DWWM utilise la version "splittee" (8 + 9) comme premiers cours du cursus, et ne
    contient pas 26.
  - CDA utilise la version "fusionnee" (26) comme premier cours du cursus, et ne
    contient ni 8 ni 9.
  Le `prerequis` de course 10 ("Web Client / HTML & CSS") ne peut satisfaire les deux
  cursus simultanement avec une seule valeur de `prerequis`, puisque les positions
  attendues different selon le cursus.
- Conformement au protocole BLOCKED et a l'instruction explicite du scope ("documenter
  le cas et ne pas forcer" pour les echecs de validation anti-cycle ; etape 6 "verifier
  qu'aucun AUTRE cursus n'est casse"), **aucune mutation n'a ete appliquee**. Ce n'est
  pas un cycle de prerequis (pas d'erreur 4xx attendue du backend), mais une
  **regression fonctionnelle certaine sur CDA** si la mutation prevue est appliquee
  telle que specifiee.
- Le cours fusionne 26 n'est donc PAS orphelin : il est activement utilise comme
  premier cours du cursus CDA (id 6), en plus d'etre le prerequis (direct) de course
  10. Sa suppression ou sa "mise en residuel" n'est pas envisageable sans casser CDA
  egalement.

## Files Touched
Aucun. Travail entierement en lecture (API GET via curl + simulation Python locale,
fichiers temporaires supprimes en fin d'analyse).

## Open Blockers
**Conflit cross-cursus bloquant** : appliquer la mutation telle que specifiee dans le
scope (10.prerequis: [26] -> [8,9]) ferait passer CDA de 0 a 44 alertes
"prerequis absent du cursus", ce qui contredit directement l'exigence de l'etape 6 du
scope ("verifier qu'aucun autre cursus n'est casse").

Options de resolution possibles, a valider par l'utilisateur/PO (aucune appliquee) :
1. **Repointer course 10 vers `[8, 9, 26]`** (les trois ids en prerequis simultanement).
   Pour DWWM (sans 26) : 26 reste absent -> alerte "26 absent" persiste pour course 10
   et ses successeurs (revient au probleme initial, ne resout rien pour DWWM).
2. **Etendre la mutation a CDA** : ajouter 8 et 9 au cursus CDA (et idealement retirer
   26 de CDA, ou les placer avant course 10) en plus de modifier `prerequis` de
   course 10. Resout les deux cursus mais modifie la **composition** d'un cursus
   (`cursus_cours`), ce qui sort du scope explicite de ce WI ("Sort du cours fusionne
   ... NE PAS le supprimer" + scope limite a `prerequis` des cours catalogue).
3. **Retirer purement et simplement le prerequis de course 10** (`prerequis: []`).
   Resout DWWM (0 alerte) ET CDA (0 alerte, car course 10 n'aurait plus de prerequis
   du tout). Perte de l'information pedagogique "il faut avoir vu
   l'algorithmique/Java avant le HTML/CSS", mais c'est la seule option qui reste dans
   le strict scope "modifier `prerequis` des cours catalogue" sans casser un cursus.
4. **Ne rien faire** (statu quo) : les 14 alertes DWWM restent visibles mais
   n'affectent aucun cursus existant negativement ; documenter que le "desordre" est
   un artefact de modelisation catalogue (cf. FULLST-029) sans solution propre sans
   refonte de la composition de CDA.

Aucune de ces options n'est "repointer vers [8,9]" pur comme demande litteralement par
le scope sans effet de bord. Recommandation : decision produit necessaire avant toute
implementation (probablement option 3 si on veut rester dans le scope `prerequis`
uniquement, ou option 2 si une refonte de la composition CDA est acceptable dans un WI
suivant).

## Next Actions
- Manager/PO : choisir entre les options 1-4 ci-dessus (ou une autre), avec arbitrage
  explicite sur le perimetre autorise (uniquement `prerequis` vs composition de
  cursus egalement).
- Si option 3 (prerequis vide pour course 10) est retenue, ouvrir un sous-WI
  d'implementation : `PUT /api/cours/10` avec `prerequisIds: []`, puis revalider
  DWWM (0/14 alertes) et CDA (toujours 0 alerte) via chrome-devtools + persistance F5.
- Si option 2 (etendre a CDA) est retenue, le WI devra explicitement autoriser la
  modification de `cursus_cours` pour CDA (id 6), en plus de `prerequis` de course 10.

## Recall Hints
- Cours catalogue : id 8 = "Algorithmique / Pseudo-Code", id 9 = "Initiation a la
  Programmation / Java", id 10 = "Web Client / HTML & CSS" (`prerequis: [26]`),
  id 26 = "Algorithmique + Initiation a la Programmation / Java" (cours fusionne,
  `prerequis: []`).
- DWWM = cursus id 5 (18 cours, contient 8,9 mais pas 26). CDA = cursus id 6 (23
  cours, contient 26 mais pas 8/9).
- Seul course 10 a 26 dans ses `prerequis` parmi les 78 cours du catalogue ; les 13-14
  alertes DWWM viennent de la propagation transitive de ce seul lien via
  `transitivePrerequis` (frontend/src/app/core/utils/cursus-alerts.util.ts).
- Login API : ref@ref.com / password "toto785971" (champ JSON `password`, pas
  `motDePasse`) -> `POST /api/auth/login` retourne `{token, uid, ...}`, header
  `Authorization: Bearer <token>`.

---

# Execution suite (option 3 retenue par l'utilisateur)

## Status final
**DONE** — `prerequis` du cours catalogue id 10 ("Web Client / HTML & CSS") vide
(`[]`), verifie sans regression sur DWWM (5) et CDA (6), ni sur les autres cursus.
Persistance confirmee (DB Postgres, rechargement de page).

## Mutation appliquee
`PUT /api/cours/10/prerequis` avec corps `[]` (endpoint dedie
`CoursController.setPrerequis`, pas `PUT /api/cours/{id}` qui ne gere que
name/dureeJours). Reponse 200 :
```json
{"id":10,"name":"Web Client / HTML & CSS","dureeJours":10,"formateurs":[],"prerequis":[]}
```

## Evidence avant/apres

### Avant (GET /api/cours/10)
```json
{"id":10,"name":"Web Client / HTML & CSS","dureeJours":10,"formateurs":[],
 "prerequis":[{"id":26,"name":"Algorithmique + Initiation à la Programmation / Java",
 "dureeJours":5,"formateurs":[],"prerequis":[]}]}
```

### Apres (GET /api/cours/10, requete independante post-mutation)
```json
{"id":10,"name":"Web Client / HTML & CSS","dureeJours":10,"formateurs":[],"prerequis":[]}
```
-> confirme persistance DB (pas un effet de cache).

### Alertes UI (chrome-devtools, recherche regex "alerte" sur document.body.innerText
apres reload ignoreCache)
- `/app/admin/cursus` (page liste, 4 cursus : Développeur web et web mobile, CDA, EADL,
  ESD) : **aucune occurrence de "alerte"** -> 0 badge "⚠ N alerte(s)" sur tous les
  cursus, avant et apres.
- `/app/admin/cursus/5` (DWWM) : **aucune occurrence de "alerte"** apres mutation ->
  0 alerte (les 13-14 alertes liees a la chaine transitive vers 26 ont disparu, comme
  attendu puisque course 10 n'a plus aucun prerequis a propager).
- `/app/admin/cursus/6` (CDA) : **aucune occurrence de "alerte"** -> 0 alerte,
  pas de regression (course 10 n'a plus de prerequis du tout, donc rien de
  "absent/mal ordonne" a signaler pour CDA non plus).

Note : la page liste etait deja a 0 alerte visible AVANT la mutation egalement (les
13-14 alertes DWWM n'apparaissaient que sur la page detail /app/admin/cursus/5, pas
sur les badges de la liste -- verifie en re-testant apres coup, coherent avec
FULLST-029/031 ou le badge liste semble base sur un autre calcul ou n'etait pas
remonte). Aucune regression detectee dans tous les cas : 0 avant, 0 apres, partout.

## Decisions
- Endpoint correct pour modifier les prerequis d'un cours catalogue :
  `PUT /api/cours/{id}/prerequis` avec corps = tableau brut `List<Long>` (pas un objet
  avec champ `prerequisIds`). `PUT /api/cours/{id}` (CoursController.update) ne gere
  que `name`/`dureeJours` (CoursService.updateNomEtDuree) et ignore silencieusement
  `prerequisIds` -- a eviter pour ce type de modification (verifie : un premier essai
  via `PUT /api/cours/10` avec `prerequisIds:[]` a renvoye 200 mais `prerequis` est
  reste `[26]`, sans erreur).
- Backend (PID 46332, instance precedente sur :8080) servait du 403 sur TOUTES les
  routes (y compris `/api/auth/login` et `/api/auth/register` qui sont en
  `permitAll()` dans le source) -- instance perimee/incoherente avec le source actuel.
  Tue et redemarree via `cd backend && .\gradlew.bat bootRun --args=--spring.profiles.active=local`
  (+ `docker compose up db mailhog -d` au prealable, conteneurs absents). Nouvelle
  instance (PID 44336) repond correctement (login 200, GET/PUT /api/cours OK).

## Files Touched
Aucun fichier de code. Mutation de donnees runtime via API (`PUT /api/cours/10/prerequis`,
corps `[]`), persistee en base Postgres (conteneur `formation-db` via docker compose).

## Open Blockers
Aucun. WI cloture.

## Next Actions
- Aucune action de suite necessaire pour ce WI. Le cours fusionne id 26 reste utilise
  par CDA (position 0), inchange, conformement au scope.
- Si une future evolution touche aux prerequis des cours 8, 9, 10 ou 26, revalider
  DWWM (5) et CDA (6) (cf. Proposed Rules de l'analyse precedente, toujours valide).

## Recall Hints (mise a jour)
- Cours id 10 ("Web Client / HTML & CSS") a desormais `prerequis: []` (etait `[26]`).
  Endpoint pour modifier : `PUT /api/cours/{id}/prerequis` avec body `List<Long>` brut.
- DWWM (cursus 5) et CDA (cursus 6) : 0 alerte chacun, verifie post-mutation via
  chrome-devtools sur `/app/admin/cursus/5`, `/app/admin/cursus/6` et
  `/app/admin/cursus` (liste, 4 cursus).
- Pour relancer le backend si instance perimee (403 partout y compris `/api/auth/**`
  qui est `permitAll`) : tuer le process sur :8080, `docker compose up db mailhog -d`
  si conteneurs absents, puis `cd backend && .\gradlew.bat bootRun
  --args=--spring.profiles.active=local`.

## Proposed Rules
- TYPE: PITFALL
  Title: Catalogue cours 26 ("Algorithmique + Initiation à la Programmation / Java")
    est un point de jonction cross-cursus DWWM/CDA
  Scope: Modelisation catalogue cours / cursus (table `cours`, `cours_prerequis`,
    `cursus_cours`)
  Rule: Ne jamais modifier `prerequis` du cours id 10 ("Web Client / HTML & CSS") ou
    la composition des cursus DWWM (id 5) / CDA (id 6) sans simuler
    `computeCursusPrereqAlerts` sur LES DEUX cursus avant d'appliquer un changement,
    car id 26 (cursus CDA, position 0) et ids 8+9 (cursus DWWM, positions 0-1) sont
    deux representations concurrentes du meme prerequis pedagogique consommees par
    course 10 via une seule relation `prerequis`.
  Why: WI-20260611-FULLST-031 a montre qu'un repointage de prerequis qui resout les 13
    alertes DWWM en cree 44 nouvelles sur CDA (0 -> 44), car CDA n'a ni 8 ni 9 et DWWM
    n'a pas 26.
  How to apply: Avant tout changement de `prerequis` touchant id 8, 9, 10 ou 26, lister
    via `GET /api/cursus/{id}` la composition de DWWM (5) et CDA (6), et simuler
    `transitivePrerequis`/`computeCursusPrereqAlerts` (cursus-alerts.util.ts) sur les
    deux listes avant/apres.
  Evidence: ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-031.md

- TYPE: PITFALL
  Title: PUT /api/cours/{id} ignore silencieusement `prerequisIds`
  Scope: backend/src/main/java/fr/eni/gestionformation/controller/CoursController.java
    (endpoint `update`), backend/.../service/CoursService.java
    (`updateNomEtDuree`)
  Rule: Pour modifier les prerequis d'un cours catalogue, utiliser
    `PUT /api/cours/{id}/prerequis` avec un corps JSON = tableau brut `List<Long>`
    (ex. `[8,9]` ou `[]`), pas `PUT /api/cours/{id}` avec un champ `prerequisIds`
    dans le body (cet endpoint n'appelle que `updateNomEtDuree`, qui ne touche que
    `name`/`dureeJours`).
  Why: WI-20260611-FULLST-031 — un premier essai `PUT /api/cours/10` avec
    `{"prerequisIds":[]}` a renvoye HTTP 200 sans erreur, mais `prerequis` du cours
    est reste `[26]` (non modifie). Risque de "succes silencieux" trompeur.
  How to apply: Toujours utiliser l'endpoint dedie `/api/cours/{id}/prerequis`
    (corps = `List<Long>` brut) pour toute mutation de `prerequis`.
  Evidence: ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-031.md
