
## WI-20260611-FULLST-023
- Date: 2026-06-11
- Title: Restriction des routes/acces par role (API + app.routes.ts + sidebar)
- Status: OPEN
- TOA: manager
- Executor: developer
- attempt_count: 0
- Scope:
    Matrice cible (validee par l'utilisateur) :
    - ADMINISTRATEUR (ADMIN) : Dashboard, Utilisateurs (/app/admin/utilisateurs)
    - FORMATEUR : Calendrier (/app/calendrier) + visibilite des inscrits sur ses cours planifies (deja couvert par /api/cours-planifies/*/inscrits)
    - ELEVE : Calendrier (/app/calendrier) uniquement
    - REFERENTE_ADMINISTRATIVE (REF) : Catalogue de cours, Cursus et filiere, Promotions (admin/*)

    1) Backend SecurityConfig.java :
       - GET /api/filiere/** : permitAll -> hasAnyRole(ADMINISTRATEUR, REFERENTE_ADMINISTRATIVE) (etait ouvert sans auth)
       - GET /api/cursus/** : authenticated() -> hasAnyRole(ADMINISTRATEUR, REFERENTE_ADMINISTRATIVE)
       - GET /api/cours/** : authenticated() -> hasAnyRole(ADMINISTRATEUR, REFERENTE_ADMINISTRATIVE)
       - GET /api/promotions/** : authenticated() -> hasAnyRole(ADMINISTRATEUR, REFERENTE_ADMINISTRATIVE)
       - GET /api/admin/users : hasAnyRole(ADMINISTRATEUR, REFERENTE_ADMINISTRATIVE) -> hasRole(ADMINISTRATEUR) (REF n'a plus "Utilisateurs")
       - /api/eleves/{id}/planning, /api/cours-planifies/**, /api/cours-planifies/*/inscrits : regles existantes inchangees

    2) Backend - correction IDOR sur GET /api/eleves/{id}/planning (InscriptionCoursController + service) :
       - Ajouter une verification : id du path == id de l'utilisateur authentifie (sinon 403). Endpoint reserve a la consultation de son propre planning (ELEVE/FORMATEUR).

    3) Frontend app.routes.ts :
       - /app/dashboard : pas de guard (reste accessible a tous, sert de route de repli du roleGuard - decision utilisateur)
       - /app/calendrier : ajouter canActivate roleGuard(['FORMATEUR','ELEVE'])
       - /app/promotions et /app/promotions/:id (top-level) : ajouter canActivate roleGuard(['REF']) (route desormais redondante avec /app/admin/promotions, mais conservee sans suppression de composant)
       - /app/admin/utilisateurs, /app/admin/cours, /app/admin/cursus, /app/admin/promotions(/:id), /app/cours-planifies/:id/inscrits : guards existants inchanges

    4) Frontend sidebar.ts :
       - /app/dashboard : ajouter roles: ['ADMIN']
       - /app/calendrier : ajouter roles: ['FORMATEUR','ELEVE']
       - Supprimer l'entree top-level "/app/promotions" (roles ELEVE/FORMATEUR) - hors perimetre matrice cible
       - Entrees admin/* inchangees (deja correctement filtrees REF/ADMIN)

- Verification requise :
    - Backend : ./gradlew test (ou tests cibles SecurityConfig/InscriptionCours), verification manuelle 403/200 par role pour les endpoints modifies
    - Frontend : npx ng build (PASS), verification chrome-devtools : connexion avec chacun des 4 roles (ADMIN/REF/FORMATEUR/ELEVE), controle de la sidebar visible et des redirections roleGuard sur acces direct via URL interdite
- Decisions utilisateur (Socratic Gate) :
    - Route de repli roleGuard : conserver /app/dashboard accessible a tous (pas de defaultRouteForRole)
    - GET cursus/cours/promotions restreints REF/ADMIN ; FORMATEUR garde uniquement /api/cours-planifies/*/inscrits pour voir ses inscrits
    - GET /api/filiere/** : retire de permitAll, aligne sur cursus/cours (REF/ADMIN)
    - IDOR /api/eleves/{id}/planning : a corriger dans ce WI
- Proposed Rules: (a evaluer en cloture)
    - PITFALL: tout nouvel endpoint GET expose des donnees pedagogiques (cursus/cours/promotions/filiere) doit explicitement restreindre aux roles REF/ADMIN, ne jamais laisser authenticated()/permitAll() par defaut.
