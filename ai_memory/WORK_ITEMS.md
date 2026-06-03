# WORK ITEMS

## WI-20260602-APIAUT-001
- Date: 2026-06-02
- Title: Gitignore + init structure ai_memory
- Status: DONE
- TOA: manager
- Executor: manager (inline)
- attempt_count: 1
- Notes: Ajout ai_memory/, ai_doc/, ai_rules/, AGENT.md, STACK_SPEC.md au .gitignore racine

## WI-20260602-APIAUT-002
- Date: 2026-06-02
- Title: Fix erreurs backend + profil local sans Docker
- Status: DONE
- TOA: manager
- Executor: manager (inline)
- attempt_count: 1
- Notes: |
    - @Table(name="user") → "users" (mot réservé PostgreSQL)
    - Spring Boot 4.0.6 → 3.3.5 (Java 21 incompatible avec SB4+Java25)
    - Java toolchain 25 → 21
    - Dockerfile JDK/JRE 25 → 21
    - Création application-local.properties (datasource sur localhost)

## WI-20260603-FRONTE-002
- Date: 2026-06-03
- Title: Page de login Angular — fidèle au design React fourni
- Status: DONE
- TOA: manager
- Executor: manager (inline)
- attempt_count: 1
- Notes: |
    - login.ts : ReactiveFormsModule, signals (selectedRole), demoAccounts, onSubmit → AuthService.login() + navigate /app
    - login.html : deux colonnes (marque gauche / formulaire droit), @for pour stats et demoAccounts, ARIA complet
    - login.scss : styles complets — panneau gauche gradient, cercles déco, logo, CTA + stats, panneau droit, formulaire, comptes démo avec état actif

## WI-20260603-FRONTE-001
- Date: 2026-06-03
- Title: Restructuration frontend Angular — arborescence + routing auth/app
- Status: DONE
- TOA: manager
- Executor: manager (inline)
- attempt_count: 1
- Notes: |
    - Suppression layouts/, modules/, shared/ (ancienne structure NgModule)
    - Création core/ (AuthService, AuthGuard, AuthInterceptor)
    - Création layouts/ (AuthLayout, MainLayout + Header + Sidebar)
    - Création shared/components/ (Button, Modal, StatCard)
    - Création features/ (auth/login, dashboard, promotions/list+detail, calendrier)
    - app.routes.ts : routing complet avec lazy loading et authGuard
    - app.config.ts : provideHttpClient + authInterceptor enregistré
    - AuthService : mock login via localStorage, signal isAuthenticated
