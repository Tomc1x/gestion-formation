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

## WI-20260605-WRAPPE-002
- Date: 2026-06-05
- Title: Header Angular — port du Topbar React (sans démo switcher)
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 1
- Notes: |
    - auth.constants.ts : types Role/RoleMeta/UserProfile + ROLE_META + USER_FOR_ROLE (4 rôles)
    - AuthService : currentRole signal, setRole(), currentUser+currentRoleMeta computed, logout reset role
    - AvatarComponent : shared/components/avatar/ — initiales calculées, taille/couleurs via input()
    - RoleBadgeComponent : shared/components/role-badge/ — badge coloré, sizes sm/md
    - HeaderComponent : template complet (search, bell+dot, séparateur, user menu dropdown, hamburger mobile)

## WI-20260605-WRAPPE-001
- Date: 2026-06-05
- Title: Sidebar mobile — drawer hamburger ≤ 900px
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 1
- Notes: |
    - SidebarService (signal isOpen, toggle, close) — providedIn root
    - SidebarComponent : [class.open] binding, backdrop fade, close on nav-item click
    - HeaderComponent : bouton hamburger LucideMenu, aria-expanded, visible ≤ 900px
    - sidebar.scss : bug media query corrigé + drawer fixed + transition translateX
    - Proposed Rules acceptées : PIT-001 (Lucide imports[]), PIT-002 (SCSS @media nesting)

## WI-20260608-BACKEN-001
- Date: 2026-06-08
- Title: Filiere — corrections post-revue (ControllerAdvice + REST + visibilité repo)
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 1
- Notes: |
    - FiliereNotFoundException + FiliereAlreadyExistsException créées dans exception/
    - GlobalExceptionHandler @RestControllerAdvice (404 + 409)
    - FiliereService mis à jour avec les exceptions personnalisées
    - FiliereRepository et URLs Controller déjà corrigés par l'utilisateur
    - Tests : BUILD SUCCESSFUL 8/8

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
