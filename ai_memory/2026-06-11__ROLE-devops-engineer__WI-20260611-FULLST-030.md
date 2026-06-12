# Work Item: WI-20260611-FULLST-030

## Role
devops-engineer

## Status
DONE

## Scope
Dockerisation complete du backend (Spring Boot) et du frontend (Angular/nginx),
ajout des services `backend` et `frontend` a `docker-compose.yml` pour permettre
`docker compose up -d --build` depuis la racine (backend + frontend + db + mailhog).

## Files Touched
- `backend/Dockerfile` (cree) — build multi-stage Gradle -> jar, runtime JRE
- `backend/.dockerignore` (cree)
- `backend/src/main/resources/application-docker.properties` (cree) — jwt.secret/jwt.expiration lus depuis env JWT_SECRET/JWT_EXPIRATION avec defaut dev
- `frontend/Dockerfile` (cree) — build multi-stage Node -> nginx
- `frontend/.dockerignore` (cree)
- `frontend/nginx.conf` (cree) — sert le build Angular + fallback SPA + reverse-proxy `/api/` -> `http://backend:8080/api/`
- `docker-compose.yml` (modifie) — ajout services `backend` (build ./backend, port `${SPRING_PORT}`, profil docker, env DB/SMTP/JWT) et `frontend` (build ./frontend, port `${ANGULAR_PORT}:80`, depends_on backend)
- `.env` (modifie) — ajout `JWT_SECRET` (valeur dev existante, dollar echappe en `$$` pour interpolation docker-compose) et `JWT_EXPIRATION=86400000`
- `.env.example` (modifie) — ajout `JWT_SECRET`/`JWT_EXPIRATION` documentes avec placeholder

## Evidence
- `docker compose build backend` -> BUILD SUCCESSFUL (Gradle 9.5.1, Java 25), image `gestion-formation-backend` creee.
- `docker compose build frontend` -> `ng build` OK (avec warnings de budget SCSS preexistants, non bloquants), image `gestion-formation-frontend` creee.
- Verification isolee (car le port 8080 hote est occupe par un processus java local de dev, PID 46332, non arrete par moi) :
  - `docker compose run --rm -d -p 18080:8080 --name verify-backend backend` -> demarre avec profil "docker", connexion DB OK (`HikariPool-1` cree, dialect PostgreSQL 15.18), `Started BackendApplication in 5.481 seconds`.
  - `curl -X POST http://localhost:18080/api/auth/login -d '{}'` -> `401` (comportement attendu, prouve que l'API + securite repondent).
  - Service `formation-frontend` (cree par le `docker compose up -d` initial) demarre sur nginx, alias reseau `backend` ajoute au conteneur verify-backend pour simuler le nom de service compose :
    - `curl http://localhost:4200/` -> `200` (HTML servi)
    - `curl -X POST http://localhost:4200/api/auth/login -d '{}'` -> `401` (proxy `/api` -> backend fonctionnel)
  - Tous les conteneurs de verification supprimes, `docker compose down` execute. Aucun conteneur ne reste UP a la fin (sauf le processus java local de dev de l'utilisateur, non touche).

## Decisions
- **Java 25, pas 21** : `backend/build.gradle` pin `java.toolchain.languageVersion = JavaLanguageVersion.of(25)`. Le brief de depart indiquait Java 21 (info STACK_SPEC obsolete) ; j'ai utilise `eclipse-temurin:25-jdk-alpine` / `25-jre-alpine` pour matcher build.gradle. Les deux images existent et fonctionnent.
- **`npm install` au lieu de `npm ci`** dans `frontend/Dockerfile` : `package-lock.json` est desynchronise de `package.json` (deps optionnelles manquantes type `@emnapi/core`), `npm ci` echoue en `EUSAGE`. `npm install` est tolerant et a permis un build complet. Recommande de regenerer `package-lock.json` via `npm install` en local et de commit le resultat pour pouvoir revenir a `npm ci` (plus reproductible) - hors scope infra, a faire par un developer/frontend.
- **profil `docker`** : nouveau fichier `application-docker.properties` ajoute uniquement `jwt.secret`/`jwt.expiration` (absents de `application.properties`), lus depuis `JWT_SECRET`/`JWT_EXPIRATION` avec defaut = la meme valeur dev que `application-local.properties` (non sensible, deja en clair dans le repo). `application-local.properties` non touche.
- **`JWT_SECRET` dans `.env`** : la valeur contient des `$` qui sont interpretes par l'interpolation de variables de docker-compose (`${VAR}` syntax). Echappes en `$$` pour que la valeur passee au conteneur soit correcte (verifie via `docker compose config`).
- **nginx proxy** : `frontend/nginx.conf` route `/api/` vers `http://backend:8080/api/` (nom de service compose, port interne fixe a 8080 car c'est le port d'ecoute interne du conteneur backend independamment de `SPRING_PORT` host-mapping — `server.port=${SPRING_PORT:8080}` donc tant que `SPRING_PORT=8080` dans `.env` ceci est coherent ; si `SPRING_PORT` est change, `nginx.conf` devra etre mis a jour ou templatise).

## Open Blockers
Aucun. Le port 8080 hote etait occupe par le process de dev local de l'utilisateur (PID 46332,
`liberica-full-25.0.1\bin\java.exe`) pendant ma verification — je n'ai pas arrete ce process
(hors de mon scope / pas d'autorisation). La verification a ete faite via un mapping de port
alternatif (18080) pour le backend, ce qui prouve que l'image et la config sont correctes.
Pour un `docker compose up -d` reel sans conflit, l'utilisateur doit arreter son backend local
(`./gradlew bootRun`) avant de lancer Docker, ou inversement.

## Next Actions
- (Optionnel, hors scope devops) Regenerer `frontend/package-lock.json` (`npm install` en local + commit) pour pouvoir repasser a `npm ci` dans le Dockerfile (build plus reproductible/rapide).
- Si `SPRING_PORT` est amene a changer dans `.env`, mettre a jour `frontend/nginx.conf` (`proxy_pass http://backend:8080/...` est en dur).
- A noter pour rules-curator (cf. Proposed Rules) : ecart Java 21 (doc/brief) vs Java 25 (build.gradle reel).

## Recall Hints
docker, dockerfile, docker-compose, backend dockerfile, frontend dockerfile, nginx.conf,
application-docker.properties, JWT_SECRET, npm ci EUSAGE, package-lock desync, Java 25 toolchain,
eclipse-temurin 25, formation-network, docker compose up -d --build

## Proposed Rules
1. **PITFALL candidate** : "build.gradle pins Java 25 toolchain (`JavaLanguageVersion.of(25)`), pas Java 21 comme documente dans certains briefs/STACK_SPEC. Utiliser `eclipse-temurin:25-*-alpine` pour toute image Docker du backend, et verifier le JDK local installe (liberica-full-25.0.1 trouve sur la machine de dev)." Scope: backend/, Dockerfile, CI.
2. **PITFALL candidate** : "frontend/package-lock.json desynchronise de package.json -> `npm ci` echoue (EUSAGE, deps `@emnapi/*` manquantes). `npm install` fonctionne en attendant. A corriger en regenerant le lock file." Scope: frontend/, Dockerfile, CI.
3. **CONVENTION candidate** : "Variables contenant `$` dans `.env` (ex: JWT_SECRET) doivent etre echappees en `$$` pour eviter une interpolation partielle par docker-compose — verifier avec `docker compose config`." Scope: .env, docker-compose.yml.
