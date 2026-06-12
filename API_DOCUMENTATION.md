# Documentation API - Gestion Formation

Stack attendue : Java Spring Boot, Spring Security, JWT (`io.jsonwebtoken:jjwt-api:0.12.6` / `jjwt-impl` / `jjwt-jackson`).

Toutes les routes (sauf `/api/auth/**`) nécessitent un header `Authorization: Bearer <token>`.

Rôles existants (enum `Role`) : `ETUDIANT`, `REFERENTE_ADMINISTRATIVE`, `ADMINISTRATEUR`, `FORMATEUR`.

---

## Authentification - `/api/auth`

### POST `/api/auth/login`
Accès : public.

Requête :
```json
{
  "email": "",
  "password": ""
}
```

Réponse `200 OK` :
```json
{
  "token": "",
  "uid": 0,
  "firstName": "",
  "lastName": "",
  "email": "",
  "role": ""
}
```

Réponse `401 Unauthorized` si identifiants invalides (corps vide).

### POST `/api/auth/register`
Accès : `ADMINISTRATEUR`, `REFERENTE_ADMINISTRATIVE`.

Requête :
```json
{
  "firstName": "",
  "lastName": "",
  "email": "",
  "password": "",
  "role": "ETUDIANT | REFERENTE_ADMINISTRATIVE | ADMINISTRATEUR | FORMATEUR"
}
```

Réponse `200 OK` : même format que `AuthResponse` (login).

### GET `/api/auth/invite/preview?token=`
Accès : public.

Réponse `200 OK` :
```json
{
  "email": "",
  "role": ""
}
```

### POST `/api/auth/register-invitation`
Accès : public.

Requête :
```json
{
  "token": "",
  "password": "",
  "firstName": "",
  "lastName": ""
}
```

Réponse `201 Created` : voir `UserAdminResponse` ci-dessous.

---

## Administration utilisateurs - `/api/admin`

Accès : `ADMINISTRATEUR` (sauf `GET /api/admin/users` qui est aussi réservé `ADMINISTRATEUR`).

### Format `UserAdminResponse`
```json
{
  "uid": 0,
  "firstName": "",
  "lastName": "",
  "email": "",
  "role": "ETUDIANT | REFERENTE_ADMINISTRATIVE | ADMINISTRATEUR | FORMATEUR",
  "enabled": true
}
```

### GET `/api/admin/users`
Réponse `200 OK` : `UserAdminResponse[]`

### GET `/api/admin/users/{id}`
Réponse `200 OK` : `UserAdminResponse`

### POST `/api/admin/users`
Requête :
```json
{
  "firstName": "",
  "lastName": "",
  "email": "",
  "password": "",
  "role": ""
}
```
Réponse `201 Created` : `UserAdminResponse`

### PUT `/api/admin/users/{id}`
Requête :
```json
{
  "firstName": "",
  "lastName": "",
  "email": ""
}
```
Réponse `200 OK` : `UserAdminResponse`

### PUT `/api/admin/users/{id}/enable`
Pas de corps. Réponse `200 OK` : `UserAdminResponse`

### PUT `/api/admin/users/{id}/disable`
Pas de corps. Réponse `200 OK` : `UserAdminResponse`

### PUT `/api/admin/users/{id}/password`
Requête :
```json
{
  "newPassword": ""
}
```
Réponse `200 OK` : `UserAdminResponse`

### PUT `/api/admin/users/{id}/role`
Requête :
```json
{
  "role": ""
}
```
Réponse `200 OK` : `UserAdminResponse`

### DELETE `/api/admin/users/{id}`
Réponse `204 No Content`

### POST `/api/admin/invite`
Requête :
```json
{
  "email": "",
  "role": ""
}
```
Réponse `200 OK` : `string` (lien d'inscription, en `text/plain` ou JSON string)

---

## Filières - `/api/filiere`

Accès lecture (`GET`) : `ADMINISTRATEUR`, `REFERENTE_ADMINISTRATIVE`. Accès écriture : `REFERENTE_ADMINISTRATIVE`.

### Format `FiliereResponse`
```json
{
  "id": 0,
  "name": ""
}
```

### POST `/api/filiere`
Requête :
```json
{
  "name": ""
}
```
Réponse `200 OK` : `FiliereResponse`

### GET `/api/filiere`
Réponse `200 OK` : `FiliereResponse[]`

### GET `/api/filiere/search?name=`
Réponse `200 OK` : `FiliereResponse[]`

### GET `/api/filiere/exists?name=`
Réponse `200 OK` : `boolean`

### GET `/api/filiere/{id}`
Réponse `200 OK` : `FiliereResponse`

### PUT `/api/filiere/{id}`
Requête :
```json
{
  "name": ""
}
```
Réponse `200 OK` : `FiliereResponse`

### DELETE `/api/filiere/{id}`
Réponse `204 No Content`

---

## Cursus - `/api/cursus`

Accès lecture (`GET`) : `ADMINISTRATEUR`, `REFERENTE_ADMINISTRATIVE`. Accès écriture : `REFERENTE_ADMINISTRATIVE`.

### Format `CursusResponse`
```json
{
  "id": 0,
  "name": "",
  "filiereId": 0,
  "filiereName": "",
  "cours": [
    {
      "id": 0,
      "name": "",
      "ordre": 0,
      "formateurs": [
        { "id": 0, "firstName": "", "lastName": "" }
      ]
    }
  ]
}
```

### GET `/api/cursus`
Réponse `200 OK` : `CursusResponse[]`

### GET `/api/cursus/{id}`
Réponse `200 OK` : `CursusResponse`

### GET `/api/cursus/filiere/{filiereId}`
Réponse `200 OK` : `CursusResponse[]`

### POST `/api/cursus`
Requête :
```json
{
  "name": "",
  "filiereId": 0
}
```
Réponse `200 OK` : `CursusResponse`

### PUT `/api/cursus/{id}`
Requête : identique à la création.

Réponse `200 OK` : `CursusResponse`

### DELETE `/api/cursus/{id}`
Réponse `204 No Content`

### POST `/api/cursus/{id}/cours`
Requête :
```json
{
  "coursId": 0,
  "ordre": 0
}
```
Réponse `200 OK` : `CursusResponse`

### DELETE `/api/cursus/{id}/cours/{coursId}`
Réponse `200 OK` : `CursusResponse`

### PUT `/api/cursus/{id}/cours/reorder`
Requête :
```json
{
  "coursIds": [0, 0, 0]
}
```
Réponse `200 OK` : `CursusResponse`

---

## Cours - `/api/cours`

Accès lecture (`GET`) : `ADMINISTRATEUR`, `REFERENTE_ADMINISTRATIVE`. Accès écriture : `REFERENTE_ADMINISTRATIVE`.

### Format `CoursResponse`
```json
{
  "id": 0,
  "name": "",
  "dureeJours": 0,
  "formateurs": [
    { "id": 0, "firstName": "", "lastName": "" }
  ],
  "prerequis": [
    { "id": 0, "name": "", "dureeJours": 0, "formateurs": [], "prerequis": [] }
  ]
}
```
Note : `prerequis` est récursif (les prérequis d'un cours déjà visité sont renvoyés sans leurs propres prérequis pour éviter les boucles infinies).

### GET `/api/cours`
Réponse `200 OK` : `CoursResponse[]`

### GET `/api/cours/{id}`
Réponse `200 OK` : `CoursResponse`

### POST `/api/cours`
Requête :
```json
{
  "name": "",
  "dureeJours": 0,
  "formateurIds": [0],
  "prerequisIds": [0]
}
```
Réponse `200 OK` : `CoursResponse`

### PUT `/api/cours/{id}`
Requête :
```json
{
  "name": "",
  "dureeJours": 0
}
```
Réponse `200 OK` : `CoursResponse`

### DELETE `/api/cours/{id}`
Réponse `204 No Content`

### PUT `/api/cours/{id}/formateurs`
Requête :
```json
[0, 0, 0]
```
(liste d'IDs de formateurs)

Réponse `200 OK` : `CoursResponse`

### PUT `/api/cours/{id}/prerequis`
Requête :
```json
[0, 0, 0]
```
(liste d'IDs de cours prérequis)

Réponse `200 OK` : `CoursResponse`

---

## Promotions - `/api/promotions`

Accès lecture (`GET`) : `ADMINISTRATEUR`, `REFERENTE_ADMINISTRATIVE`. Accès écriture : `REFERENTE_ADMINISTRATIVE`. Les routes `/api/cours-planifies/**` sont également couvertes par les mêmes règles que `/api/promotions/**` (sauf l'exception ci-dessous pour `inscrits`).

### Format `PromotionResponse`
```json
{
  "id": 0,
  "name": "",
  "cursusId": 0,
  "cursusNom": "",
  "dateDebut": "AAAA-MM-JJ",
  "rythme": { "semainesCentre": 0, "semainesEntreprise": 0 },
  "eleves": [
    { "id": 0, "firstName": "", "lastName": "" }
  ],
  "planning": [
    {
      "id": 0,
      "coursId": 0,
      "coursNom": "",
      "dateDebut": "AAAA-MM-JJ",
      "dateFin": "AAAA-MM-JJ",
      "ordre": 0,
      "statut": "PLANIFIE | EN_COURS | TERMINE",
      "formateurId": 0,
      "formateurNom": "",
      "salle": "",
      "warnings": []
    }
  ]
}
```

### GET `/api/promotions`
Réponse `200 OK` : `PromotionResponse[]`

### GET `/api/promotions/{id}`
Réponse `200 OK` : `PromotionResponse`

### POST `/api/promotions`
Requête :
```json
{
  "name": "",
  "cursusId": 0,
  "dateDebut": "AAAA-MM-JJ",
  "rythme": { "semainesCentre": 0, "semainesEntreprise": 0 },
  "eleveIds": [0]
}
```
Réponse `200 OK` : `PromotionResponse`

### PUT `/api/promotions/{id}`
Requête : identique à la création.

Réponse `200 OK` : `PromotionResponse`

### DELETE `/api/promotions/{id}`
Réponse `204 No Content`

### POST `/api/promotions/{id}/eleves/{eleveId}`
Pas de corps. Ajoute un élève à la promotion.

Réponse `200 OK` : `PromotionResponse`

### DELETE `/api/promotions/{id}/eleves/{eleveId}`
Retire un élève de la promotion.

Réponse `200 OK` : `PromotionResponse`

### POST `/api/promotions/{id}/planning`
Crée une session de cours planifiée.

Requête :
```json
{
  "coursId": 0,
  "dateDebut": "AAAA-MM-JJ",
  "dateFin": "AAAA-MM-JJ",
  "formateurId": 0,
  "salle": "",
  "force": false
}
```
`force` permet de passer outre certains avertissements (conflits de planning).

Réponse `200 OK` :
```json
{
  "id": 0,
  "coursId": 0,
  "coursNom": "",
  "dateDebut": "AAAA-MM-JJ",
  "dateFin": "AAAA-MM-JJ",
  "ordre": 0,
  "statut": "PLANIFIE | EN_COURS | TERMINE",
  "formateurId": 0,
  "formateurNom": "",
  "salle": "",
  "warnings": ["..."]
}
```

### PUT `/api/promotions/{id}/planning/{coursPlanifieId}`
Requête :
```json
{
  "dateDebut": "AAAA-MM-JJ",
  "dateFin": "AAAA-MM-JJ",
  "formateurId": 0,
  "salle": ""
}
```
Réponse `200 OK` : même format que la création de planning (avec `warnings`).

### DELETE `/api/promotions/{id}/planning/{coursPlanifieId}`
Réponse `204 No Content`

---

## Inscriptions aux cours - `/api/cours-planifies` et `/api/eleves`

### POST `/api/cours-planifies/{id}/inscriptions`
Accès : `ADMINISTRATEUR`, `REFERENTE_ADMINISTRATIVE`.

Requête :
```json
{
  "eleveId": 0
}
```
Réponse `201 Created` :
```json
{
  "id": 0,
  "eleveId": 0,
  "coursPlanifieId": 0,
  "dateInscription": "AAAA-MM-JJ"
}
```

### DELETE `/api/cours-planifies/{id}/inscriptions/{eleveId}`
Accès : `ADMINISTRATEUR`, `REFERENTE_ADMINISTRATIVE`.

Réponse `204 No Content`

### GET `/api/cours-planifies/{id}/inscrits`
Accès : `ADMINISTRATEUR`, `REFERENTE_ADMINISTRATIVE`, `FORMATEUR`.

Réponse `200 OK` :
```json
[
  {
    "eleveId": 0,
    "firstName": "",
    "lastName": "",
    "origine": "PROMOTION | INDIVIDUEL"
  }
]
```

### GET `/api/eleves/{id}/planning`
Accès : utilisateur authentifié, uniquement pour son propre `id` (sinon `403 Forbidden`).

Réponse `200 OK` :
```json
[
  {
    "coursPlanifieId": 0,
    "coursId": 0,
    "coursNom": "",
    "dateDebut": "AAAA-MM-JJ",
    "dateFin": "AAAA-MM-JJ",
    "ordre": 0,
    "statut": "PLANIFIE | EN_COURS | TERMINE",
    "origine": "PROMOTION | INDIVIDUEL"
  }
]
```

---

## Tests

Pour chaque endpoint, prévoir :
- Tests unitaires de service (logique métier, règles de validation, conflits de planning).
- Tests d'intégration de contrôleur (`@WebMvcTest` ou `@SpringBootTest` + `MockMvc`) couvrant :
  - cas nominal (200/201/204 attendus + corps de réponse),
  - accès refusé selon le rôle (403),
  - non authentifié (401),
  - ressource introuvable (404),
  - validation des entrées invalides (400 si applicable).
