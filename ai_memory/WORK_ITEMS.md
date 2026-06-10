# WORK ITEMS

## WI-20260608-FRONTE-003
- Date: 2026-06-08
- Title: Écran gestion utilisateurs — Screen 10
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 1
- Notes: |
    - core/models/user.model.ts : BackendRole + UserAdmin interface + BACKEND_TO_FRONTEND_ROLE + ROLE_FILTER_LABELS
    - core/services/user-admin.service.ts : getAll(), enable(uid), disable(uid) → /api/admin/users
    - shared/components/avatar/ : initiales calculées, couleur déterministe par hash du nom
    - features/administration/utilisateurs/ : table recherche + filtre rôle + toggle actif/inactif
    - Route lazy /app/admin/utilisateurs ajoutée dans app.routes.ts
    - Build : PASS (chunk utilisateurs 34.88 kB)

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

## WI-20260608-FRONTE-002
- Date: 2026-06-08
- Title: MonCalendrierComponent — vues mois et semaine
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 1
- Notes: |
    - Signals : view, referenceDate, events (toSignal), computed monthGrid/weekDays/eventsByDay/navTitle
    - Chargement réactif via combineLatest + switchMap
    - Navigation prev/next par mois ou semaine selon la vue
    - Bleu #1D4ED8 pour promotion, vert #16A34A pour cours à l'unité
    - ARIA complet (grid/row/gridcell)
    - Build : PASS

## WI-20260608-BACKEN-006
- Date: 2026-06-08
- Title: Postman — collection + environnement + scripts JWT
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 1
- Notes: |
    - docs/postman/gestion-formation.collection.json (v2.1) — 6 dossiers, 27 requêtes
    - docs/postman/gestion-formation.environment.json — baseUrl, token, userId, filiereId, cursusId, coursId
    - Scripts auto : login → token, create user → userId, create filiere/cursus/cours → ids

## WI-20260608-BACKEN-005
- Date: 2026-06-08
- Title: Système d'invitation — InvitationToken + JavaMailSender + endpoints
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 1
- Notes: |
    - InvitationToken : token UUID, email, role, expirationDate (now+24h), used
    - InvitationService : sendInvitation (invalide ancien token + envoie mail) + registerWithToken
    - POST /api/admin/invite (ADMIN) + POST /api/auth/register-invitation (public)
    - InvalidInvitationTokenException → 400
    - app.invitation.base-url configurable dans properties
    - Tests : BUILD SUCCESSFUL

## WI-20260608-BACKEN-004
- Date: 2026-06-08
- Title: API Admin utilisateurs — CRUD + enabled + sécurité
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 1
- Notes: |
    - User.enabled + isEnabled() surchargé
    - UserAdminService : findAll, findById, createUser, enable/disable, changePassword, deleteUser, changeRole
    - UserAdminController : 8 endpoints sur /api/admin/users
    - UserNotFoundException (404) + UserAlreadyExistsException (409)
    - SecurityConfig : /api/admin/** → ADMINISTRATEUR
    - Tests : BUILD SUCCESSFUL

## WI-20260608-BACKEN-003
- Date: 2026-06-08
- Title: CoursService — validation rôle FORMATEUR dans assignFormateurs
- Status: DONE
- TOA: manager
- Executor: manager (inline)
- attempt_count: 1
- Notes: |
    - Ajout vérification Role.FORMATEUR sur chaque User avant assignation
    - IllegalArgumentException avec id fautif si rôle incorrect
    - Tests : BUILD SUCCESSFUL

## WI-20260608-BACKEN-002
- Date: 2026-06-08
- Title: Cursus + Cours — entités, repos, services, controllers, sécurité
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 1
- Notes: |
    - Cursus (ManyToOne filiere nullable, OneToMany cours) + Cours (ManyToOne cursus, ManyToMany formateurs)
    - Table jointure cours_formateurs
    - CursusService : deleteById(id, cascade) — cascade supprime les cours, sinon met cursus à null
    - CoursService : assignFormateurs(coursId, List<Long>)
    - GlobalExceptionHandler étendu : CursusNotFoundException + CoursNotFoundException → 404
    - SecurityConfig : GET authenticated, write REFERENTE_ADMINISTRATIVE pour /api/cursus/** et /api/cours/**
    - @Builder.Default sur toutes les listes pour éviter NPE avec Lombok @Builder
    - Tests : BUILD SUCCESSFUL

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

## WI-20260610-BACKEN-001
- Date: 2026-06-10
- Title: Adapter pattern pour la gestion utilisateurs (front) — BaseUserAdminAdapter / Http / Mock
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 0
- Notes: |
    - Réplique le pattern calendrier (BaseCalendarAdapter / MockCalendarAdapter).
    - Créer frontend/src/app/core/adapters/user-admin.adapter.ts (BaseUserAdminAdapter, abstrait, 9 méthodes de UserAdminService).
    - Créer user-admin-http.adapter.ts (HttpUserAdminAdapter, fusionne les appels HTTP de UserAdminService).
    - Créer user-admin-mock.ts (MockUserAdminAdapter, CRUD en mémoire).
    - app.config.ts : provide BaseUserAdminAdapter -> HttpUserAdminAdapter.
    - utilisateurs.ts injecte BaseUserAdminAdapter au lieu de UserAdminService.
    - Supprimer UserAdminService.

## WI-20260610-BACKEN-002
- Date: 2026-06-10
- Title: Frontend — intégration design system "GestionFormation" (index.html Claude Design)
- Status: OPEN
- TOA: manager
- Executor: developer
- attempt_count: 0
- Notes: |
    - Source: bundle Claude Design (projet-full-stack/project/index.html, screenshots, screens-*.jsx).
    - Tokens CSS (couleurs --blue-*, --green/--red/--amber, fonts Poppins/Sora, radius, ombres e1/e2/e3).
    - Composants: .btn (primary/ghost/outline/green/sm/icon), .card, .field/.input, .pill/.badge/.dot,
      table.tbl, .tabs/.tab, .avatar, .switch, skeleton .skel, animations fadeUp/scaleIn.
    - Intégrer dans frontend/src/styles.scss (ou partials importés) + variables SCSS/CSS custom properties.
    - Indépendant des WI backend — peut démarrer en parallèle.

## WI-20260610-BACKEN-003
- Date: 2026-06-10
- Title: Backend — refonte modèle Cours/Cursus (catalogue global + table de liaison ordonnée CursusCours)
- Status: OPEN
- TOA: manager
- Executor: developer
- attempt_count: 0
- Notes: |
    - Décision validée: catalogue global. Cours n'a plus de cursus_id direct (ManyToOne supprimé).
    - Nouvelle entité CursusCours (cursus_id, cours_id, ordre) - table de liaison ordonnée.
    - Met à jour CoursRepository/CursusRepository, CoursService/CursusService, DTOs (CoursResponse,
      CursusResponse: liste ordonnée de cours), controllers, exceptions.
    - Risque: casse l'API existante consommée par le front actuel (pages Cursus/Cours déjà livrées
      en WI-20260608-BACKEN-002/003) -> coordination avec WI-006/WI-007.
    - DDL: vérifier ddl-auto Hibernate vs migration Flyway (pas de dossier db/migration peuplé).

## WI-20260610-BACKEN-004
- Date: 2026-06-10
- Title: Backend — prérequis Cours ManyToMany auto-référencé + validation anti-cycle
- Status: OPEN
- TOA: manager
- Executor: developer
- attempt_count: 0
- Notes: |
    - Décision validée: Cours.prerequis: Set<Cours> (ManyToMany auto-référencé, table cours_prerequis).
    - Validation à l'écriture: refuser l'ajout d'un prérequis créant un cycle (DFS/BFS sur le graphe).
    - Dépend de WI-20260610-BACKEN-003 (même entité Cours).

## WI-20260610-BACKEN-005
- Date: 2026-06-10
- Title: Backend — CoursResponse récursif (prérequis imbriqués jusqu'à la racine)
- Status: OPEN
- TOA: manager
- Executor: developer
- attempt_count: 0
- Notes: |
    - Décision validée: récursion complète (CoursResponse.prerequis: List<CoursResponse>),
      terminaison garantie par l'anti-cycle de WI-20260610-BACKEN-004.
    - Dépend de WI-20260610-BACKEN-004.

## WI-20260610-BACKEN-006
- Date: 2026-06-10
- Title: Frontend — page Catalogue de cours (CRUD + gestion prérequis, adapter pattern)
- Status: OPEN
- TOA: manager
- Executor: developer
- attempt_count: 0
- Notes: |
    - Adapter pattern (cf. WI-20260610-BACKEN-001 / CONV-001 ai_rules/conventions.md).
    - Table CRUD cours, modale édition avec checkboxes prérequis (anti-cycle UI), badges
      "prérequis de" / "dépend de".
    - Dépend de WI-20260610-BACKEN-005 (API) et WI-20260610-BACKEN-002 (design system).

## WI-20260610-BACKEN-007
- Date: 2026-06-10
- Title: Frontend — page Cursus (création cursus + filière, liste ordonnée, alertes ordre pédagogique)
- Status: OPEN
- TOA: manager
- Executor: developer
- attempt_count: 0
- Notes: |
    - Modale "Nouvelle filière" (nom + couleur), modale "Nouveau cursus" (nom, filière, constructeur
      de liste ordonnée avec réordonnancement, alertes prérequis manquant/mal placé).
    - Dépend de WI-20260610-BACKEN-003, 005, 002, 006.
    - Nécessite verifier après intégration finale (changement de contrat API cross-module).
