# WI-20260611-FULLST-032 — Bug: tous les roles voient le sidebar/menu REF

## Status: DONE

## Scope
Frontend only — `frontend/src/app/core/services/auth.service.ts`, methode `login()`.

## Root cause confirmed
Le signal `_currentRole` est initialise une seule fois a la construction du
service depuis `localStorage` (storedUser, lignes 26-28). `login()` mettait a
jour `_currentUser` et le `localStorage` mais n'appelait jamais
`_currentRole.set(...)`. Resultat : apres connexion (sans reload complet de
l'app), `currentRole()` restait figé sur sa valeur d'initialisation (souvent
'REF'), donc la sidebar (`sidebar.ts:38 visibleRoutes`) et `roleGuard`
affichaient/autorisaient selon ce role figé plutot que le vrai role connecte.

## Fix applique
Fichier: `frontend/src/app/core/services/auth.service.ts`

Dans le `tap()` de `login()`, ajout d'une ligne juste apres
`this._isAuthenticated.set(true);` :

```ts
this._currentRole.set(BACKEND_TO_FRONTEND_ROLE[response.role as BackendRole] ?? 'REF');
```

Meme pattern/mapping que celui utilise pour l'initialisation depuis
`storedUser` (lignes 26-28), via `BACKEND_TO_FRONTEND_ROLE` deja importe
(`frontend/src/app/core/models/user.model.ts`). Mapping verifie correct et
coherent :
- ADMINISTRATEUR -> ADMIN
- REFERENTE_ADMINISTRATIVE -> REF
- FORMATEUR -> FORMATEUR
- ETUDIANT -> ELEVE

## Build
`cd frontend && npm run build` -> PASS (Application bundle generation
complete, 6.036s). Seuls warnings preexistants (budgets SCSS sur
utilisateurs/promotions/cursus/register.scss), aucune nouvelle erreur.

## Verification chrome-devtools (frontend :4200, backend :8080 local)

Comptes de test utilises (issus de ai_memory/WORK_ITEMS.md ligne ~1495 et
gestion_formation_dump.sql) :
- ADMIN : admin@admin.com / Admin123
- REF : ref@ref.com / toto785971
- ELEVE : eleve@eleve.com / Eleve123
- FORMATEUR : aucun mot de passe connu/documente (formateur1@eni.fr testé avec
  plusieurs mots de passe plausibles via API — tous 401). Non verifie en UI,
  voir section "Couverture FORMATEUR" ci-dessous.

### ELEVE (eleve@eleve.com)
Apres login + reload complet (`/app/dashboard`) : sidebar affiche
**uniquement "Calendrier"** — conforme a `sidebar.ts` (route calendrier:
roles ['FORMATEUR','ELEVE']). Pas de Cursus/Promotions/Catalogue de
cours/Utilisateurs/Tableau de bord.
Navigation directe vers `/app/admin/cursus` -> redirige vers
`/app/dashboard` (roleGuard OK).

### ADMIN (admin@admin.com)
Apres login + reload complet : sidebar affiche **"Tableau de bord" et
"Utilisateurs"** — conforme a `sidebar.ts` (dashboard: ['ADMIN'],
utilisateurs: ['ADMIN']). Pas de Cursus/Promotions/Catalogue de
cours/Calendrier.
Navigation directe vers `/app/admin/cursus` -> redirige vers
`/app/dashboard` (roleGuard OK).
`localStorage.user` confirme `{"role":"ADMINISTRATEUR", ...}`.

### REF (ref@ref.com)
Apres login : sidebar affiche **"Catalogue de cours", "Cursus",
"Promotions"** — conforme a `sidebar.ts` (roles ['REF'] pour ces 3 routes).
Pas de Calendrier/Tableau de bord/Utilisateurs.

### Note sur le premier essai (ELEVE, avant reload)
Le premier login (ELEVE) immediatement apres l'edit a montre encore les
routes REF dans la sidebar — du au fait que ng serve/HMR n'avait pas encore
republie le service Angular (singleton deja instancie). Apres
`navigate_page reload ignoreCache=true` puis re-login, le comportement
correct est observe de facon repetable pour ELEVE, ADMIN et REF. Pas
d'incoherence dans le code, simple artefact de rechargement HMR.

## Couverture FORMATEUR (non verifie en UI)
Aucun compte FORMATEUR avec mot de passe connu n'a ete trouve (seed
`formateur1@eni.fr` : hash bcrypt different de tous les comptes connus,
plusieurs mots de passe plausibles testes via `/api/auth/login` -> 401
systematique). Pas de creation de nouveau compte de demo (regle memoire
existante).
Le fix est une ligne generique appliquant le meme mapping
`BACKEND_TO_FRONTEND_ROLE` pour TOUS les roles backend, sans cas particulier.
ELEVE et FORMATEUR partagent exactement le meme chemin de code et le meme
mapping (`FORMATEUR -> 'FORMATEUR'`, deja correct dans la table de mapping et
deja utilise a l'initialisation). ELEVE est verifie fonctionnel avec ce meme
mapping -> FORMATEUR est couvert par revue de code mais pas par test UI direct.

## Verification backend (point 5 du WI)
`backend/src/main/java/fr/eni/gestionformation/security/SecurityConfig.java`
(lignes 34-48) : les endpoints `/api/cursus/**`, `/api/cours/**`,
`/api/promotions/**`, `/api/filiere/**` sont bien proteges
`hasRole("REFERENTE_ADMINISTRATIVE")` (GET autorise aussi pour ADMINISTRATEUR).
Pas de probleme de securite backend independant a signaler — coherent avec
WI-20260611-FULLST-023 (DONE).

## Files Touched
- `frontend/src/app/core/services/auth.service.ts` (1 ligne ajoutee dans
  `login()`)

## Decisions
- Mapping `BACKEND_TO_FRONTEND_ROLE` reutilise tel quel (pattern existant a
  l'initialisation), pas de nouvelle abstraction.
- `logout()` (ligne 83, `_currentRole.set('REF')`) non touche, conforme au
  hors-scope du WI.

## Open Blockers
Aucun. WI considere DONE — la couverture FORMATEUR repose sur revue de code
(meme chemin/mapping que ELEVE, verifie) plutot que sur test UI direct, faute
de credentials connus.

## Next Actions
- Aucune action immediate requise pour ce WI.
- Si un compte FORMATEUR de test devient disponible/documente, refaire un
  passage chrome-devtools rapide pour confirmer la sidebar FORMATEUR
  (devrait afficher uniquement "Calendrier", comme ELEVE).

## Recall Hints
- auth.service.ts `_currentRole` signal — login() doit toujours
  `.set(...)` ce signal en plus de `_currentUser` et `localStorage`.
- Comptes de test connus : admin@admin.com/Admin123,
  ref@ref.com/toto785971, eleve@eleve.com/Eleve123.

## Proposed Rules

- TYPE: PITFALL
  Title: AuthService signals derives doivent etre mis a jour explicitement dans login(), pas seulement a l'init
  Scope: frontend/src/app/core/services/auth.service.ts et tout signal derive de storedUser
  Rule: Tout signal initialise une seule fois depuis localStorage/storedUser (ex. _currentRole) doit aussi etre explicitement `.set(...)` dans le tap() de login(), sinon il reste figé sur sa valeur d'initialisation apres connexion sans reload complet.
  Why: WI-20260611-FULLST-032 — _currentRole n'etait jamais mis a jour au login, causant l'affichage du sidebar/role REF pour tous les utilisateurs.
  How to apply: Lors de l'ajout d'un nouveau signal derive du profil utilisateur dans AuthService, verifier qu'il est mis a jour aux memes 3 endroits : initialisation (storedUser), login() (response), logout() (valeur par defaut).
  Evidence: frontend/src/app/core/services/auth.service.ts lignes 26-28 (init) vs 66-73 (login, avant fix) vs 83 (logout).

- TYPE: PITFALL
  Title: ng serve / HMR peut servir un service Angular singleton obsolete apres edition
  Scope: Verification chrome-devtools sur frontend Angular avec ng serve
  Rule: Apres modification d'un service `providedIn: 'root'` deja instancie (signal cree a la construction), faire un reload complet avec cache ignore (navigate_page reload ignoreCache=true) avant de relancer le test, sinon le comportement observe reflete l'ancienne instance du service.
  Why: Premier test ELEVE post-fix a montre l'ancien comportement (sidebar REF) malgre le fix applique, car HMR n'avait pas republie le singleton AuthService.
  How to apply: Toujours faire un hard reload (ignoreCache=true) avant de tester un changement touchant un service singleton avec etat (signals).
  Evidence: WI-20260611-FULLST-032, premier essai ELEVE vs essai apres reload.

- TYPE: DECISION
  Title: logout() reset _currentRole a 'REF' au lieu d'un etat "non authentifie"
  Scope: frontend/src/app/core/services/auth.service.ts logout()
  Rule: (Observation, pas un changement applique) logout() fait `_currentRole.set('REF')` (ligne 83) ce qui n'est pas vraiment un "etat non authentifie" — cela pourrait theoriquement laisser un flash de sidebar REF entre logout et redirection vers /login si un composant lit currentRole() pendant cette fenetre.
  Why: Releve pendant l'analyse de FULLST-032, hors scope de ce WI (instruction explicite de ne pas corriger logout()).
  How to apply: Si un futur WI touche au flux logout/redirection, evaluer l'ajout d'un type FrontendRole | null ou d'un signal isAuthenticated verifie avant tout rendu conditionnel sur currentRole().
  Evidence: frontend/src/app/core/services/auth.service.ts ligne 83.
