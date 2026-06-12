# WORK ITEMS

## WI-20260611-FULLST-030
- Date: 2026-06-11
- Title: Dockerisation complete backend + frontend, docker compose up -d
- Status: OPEN
- TOA: manager
- Executor: devops-engineer
- attempt_count: 0
- Notes: |
    - backend/Dockerfile: multi-stage Gradle build -> JRE 21, profil docker
      (datasource host=db, mail host=mailhog).
    - frontend/Dockerfile: multi-stage ng build -> nginx, proxy /api vers backend.
    - docker-compose.yml: ajouter services backend + frontend, depends_on, reseau
      formation-network, variables .env.
    - Verification: docker compose up -d --build -> tous conteneurs up, /api repond,
      frontend accessible.

## WI-20260611-FULLST-026
- Date: 2026-06-11
- Title: Tests unitaires/integration backend basiques pour services/controllers manquants
- Status: DONE
- TOA: manager
- Executor: developer, rules-curator
- attempt_count: 0
- Notes: |
    - 8 nouveaux fichiers de test crees (UserAdminService/Controller, InvitationService/Controller,
      FiliereController, CursusController, InscriptionCoursController, PromotionController).
    - Verification: ./gradlew test -> BUILD SUCCESSFUL, tous tests verts.
    - Proposed Rule triee: CONV-008 ajoutee dans ai_rules/conventions.md (pattern test controller
      WebMvcTest + SecurityConfig + MockitoBean), cross-ref avec PIT-017.

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
- Status: DONE
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
- Status: DONE
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
- Status: DONE
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
- Status: DONE
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
- Status: DONE
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
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 0
- Notes: |
    - Modale "Nouvelle filière" (nom + couleur), modale "Nouveau cursus" (nom, filière, constructeur
      de liste ordonnée avec réordonnancement, alertes prérequis manquant/mal placé).
    - Dépend de WI-20260610-BACKEN-003, 005, 002, 006.
    - Nécessite verifier après intégration finale (changement de contrat API cross-module).

## WI-20260610-BACKEN-008
- Date: 2026-06-10
- Title: Frontend — corriger le depassement de budget CSS (utilisateurs.scss + warnings header/register)
- Status: DONE
- TOA: manager
- Executor: developer + manager
- attempt_count: 0
- Notes: |
    - developer: factorisation locale (placeholders %focus-ring, %btn-base, %field-input, etc.)
      header.scss corrige sous le budget warning (3.79 kB). utilisateurs.scss reduit de 9.89 a 9.09 kB
      mais reste au-dessus de 8 kB (duplication modal/formulaire avec cours.scss/cursus.scss, hors scope simple).
    - manager: angular.json anyComponentStyle maximumError 8kB -> 12kB (decision utilisateur).
      ng build PASS, plus aucune erreur bloquante (warnings restants: utilisateurs.scss 9.09kB, register.scss 4.95kB).
    - Dette technique restante (non bloquante): extraire un partial SCSS partage modal/formulaire
      pour utilisateurs/cours/cursus si on veut repasser sous 8kB sans relever le budget davantage.

## WI-20260610-BACKEN-009
- Date: 2026-06-10
- Title: Backend — CRUD complet Filiere (update + delete avec controle 409)
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 0
- Notes: |
    - PUT /api/filiere/{id} : modifier le nom (controle unicite, FiliereAlreadyExistsException existante).
    - DELETE /api/filiere/{id} : 409 si des cursus sont rattaches (nouvelle FiliereInUseException,
      mappee dans GlobalExceptionHandler selon CONV-003).
    - Tests service + controller.

## WI-20260610-BACKEN-010
- Date: 2026-06-10
- Title: Frontend — section CRUD Filieres dans la page Cursus
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 0
- Notes: |
    - BaseFiliereAdapter : ajout update/delete (+ Http/Mock).
    - Section "Filieres" dans cursus.html/cursus.ts : liste, modale edition, suppression
      avec gestion erreur 409 (filiere utilisee par X cursus).
    - Depend de WI-20260610-BACKEN-009.

## WI-20260610-BACKEN-011
- Date: 2026-06-10
- Title: Frontend — systeme de routes par role (roleGuard)
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 0
- Notes: |
    - roleGuard: CanActivateFn, lit route.data['roles'] vs authService.currentRole(),
      redirige vers /app/dashboard si non autorise.
    - admin/utilisateurs -> ADMIN uniquement. admin/cours, admin/cursus -> REF uniquement.
    - Pas de commentaires dans le code (consigne utilisateur).

## WI-20260610-BACKEN-012
- Date: 2026-06-10
- Title: Backend — Cours.dureeJours (champ duree indicative)
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 0
- Notes: |
    - Cours.dureeJours (Integer nullable) + CoursRequest/CoursResponse (5 args) + CoursController propagation.
    - BUILD SUCCESSFUL. Note: ai_memory/2026-06-11__ROLE-developer__WI-20260610-BACKEN-012-013-014.md

## WI-20260610-BACKEN-013
- Date: 2026-06-10
- Title: Backend — Entite Promotion (cursus + eleves)
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 0
- Notes: |
    - Promotion (id, name, cursus ManyToOne nullable, dateDebut, rythme OneToOne) + PromotionRepository + PromotionResponse + PromotionNotFoundException.
    - User.promotion (ManyToOne nullable) + UserRepository.findByPromotionId.
    - PromotionService.deleteById : SET NULL sur User.promotion (pattern DEC-001/DEC-002).
    - PromotionService.clearCursusReferences : appele depuis CursusService.deleteById, SET NULL sur Promotion.cursus.
    - PromotionNotFoundException pas encore branchee dans GlobalExceptionHandler (a faire en WI-017, pas de controller).
    - BUILD SUCCESSFUL. Note: ai_memory/2026-06-11__ROLE-developer__WI-20260610-BACKEN-012-013-014.md
    - Proposed Rules -> rules-curator: ACCEPT, nouvelle entree DEC-002 (cf ai_rules/decisions.md).

## WI-20260610-BACKEN-014
- Date: 2026-06-10
- Title: Backend — Entite Rythme/cycle alternance (optionnel sur Promotion)
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 0
- Notes: |
    - Rythme (id, semainesCentre, semainesEntreprise, OneToOne -> Promotion via promotion_id nullable, owning side).
    - Promotion.rythme = mappedBy, cascade=ALL, orphanRemoval=true. rythme == null => cycle continu.
    - RythmeRepository (JpaRepository simple).
    - BUILD SUCCESSFUL. Note: ai_memory/2026-06-11__ROLE-developer__WI-20260610-BACKEN-012-013-014.md
    - DB schema non verifie en live (pas de Postgres lance) - smoke test recommande avant WI-017.

## WI-20260610-BACKEN-015
- Date: 2026-06-10
- Title: Backend — Entite PromotionCours (planning)
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 0
- Notes: |
    - PromotionCours (promotion/cours ManyToOne optional=false, dateDebut, dateFin, ordre, statut PromotionCoursStatut).
    - PromotionCoursRepository: findByPromotionIdOrderByOrdre + findOverlappingForFormateur (JPQL, jointure cours.formateurs) pour WI-018.
    - BUILD SUCCESSFUL. Note: ai_memory/2026-06-11__ROLE-developer__WI-20260610-BACKEN-015-016.md

## WI-20260610-BACKEN-016
- Date: 2026-06-10
- Title: Backend — Moteur de planification automatique (auto-placement)
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 0
- Notes: |
    - PlanificationService.genererPlanning(Promotion): place sequentiellement les Cours (ordre CursusCours)
      en jours ouvres, exclut semaines "entreprise" si Rythme defini (cycle modulo depuis dateDebut).
    - dureeJours null -> defaut 1 jour.
    - 4 tests unitaires PlanificationServiceTest. BUILD SUCCESSFUL.
    - Integration controller (appel a la creation Promotion) laissee pour WI-017.
    - Note: ai_memory/2026-06-11__ROLE-developer__WI-20260610-BACKEN-015-016.md

## WI-20260610-BACKEN-017
- Date: 2026-06-10
- Title: Backend — API Promotion + planning (CRUD + drag&drop)
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 0
- Notes: |
    - PromotionController: GET/POST/PUT/DELETE /api/promotions, GET /{id}, PUT /{id}/planning/{promotionCoursId} (drag&drop, retourne warnings[]).
    - PromotionService etendu: create (genere planning), update (regenere si cursus/dateDebut change), updatePlanning.
    - PromotionResponse 8 champs (rythme, eleves, planning). PromotionCoursNotFoundException -> 404 (CONV-003).
    - SecurityConfig: GET authenticated, write REFERENTE_ADMINISTRATIVE.
    - 8 tests PromotionServiceTest. BUILD SUCCESSFUL.
    - DECISION (manager, 2026-06-11): eleveIds passera en semantique full-replace lors de WI-019
      (diff + unassign des eleves retires), cf Proposed Rules dans la note memoire -> a trianger en fin de chaine.
    - Note: ai_memory/2026-06-11__ROLE-developer__WI-20260610-BACKEN-017-018.md

## WI-20260610-BACKEN-018
- Date: 2026-06-10
- Title: Backend — Detection conflits formateurs multi-promotions
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 0
- Notes: |
    - updatePlanning: pour chaque formateur du Cours deplace, findOverlappingForFormateur sur les nouvelles
      dates, exclut la promotion courante, ajoute warning "Conflit formateur : ... promotion {nom}".
    - Non bloquant, integre dans warnings[] de PUT /api/promotions/{id}/planning/{promotionCoursId}.
    - BUILD SUCCESSFUL. Note: ai_memory/2026-06-11__ROLE-developer__WI-20260610-BACKEN-017-018.md

## WI-20260610-BACKEN-019
- Date: 2026-06-10
- Title: Frontend — Module gestion Promotion (liste/creation)
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 0
- Notes: |
    - BasePromotionAdapter/Http/Mock + promotion.model.ts. Page admin/promotions: table + modales
      creation/edition/suppression (nom, cursus, dateDebut, rythme optionnel, multi-select eleves).
    - Route admin/promotions (roleGuard REF) + entree sidebar (PIT-006).
    - Backend PromotionService.update(): semantique full-replace eleveIds (desaffecte les retires) -
      remplace la decision additive de WI-017/018.
    - ng build OK (warnings CSS preexistants), ./gradlew test BUILD SUCCESSFUL.
    - Note: ai_memory/2026-06-11__ROLE-developer__WI-20260610-BACKEN-019-021.md

## WI-20260610-BACKEN-020
- Date: 2026-06-10
- Title: Frontend — Calendrier planning drag-and-drop
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 0
- Notes: |
    - FullCalendar Angular 6.1.20 (core/daygrid/timegrid/interaction). Page admin/promotions/:id/planning
      (roleGuard REF), accessible via bouton "Voir le planning" depuis la liste promotions.
    - Selecteur de promotion, evenements colores par statut, bordure warning si warnings[].
    - eventDrop/eventResize -> updatePlanning() (conversion exclusif/inclusif), revert() si erreur HTTP.
    - ng build OK (chunk planning 273.22kB raw / 68.24kB transfer), pas de nouveau depassement CSS.
    - Verification visuelle (ng serve) a faire en suite (cf manager).
    - Note: ai_memory/2026-06-11__ROLE-developer__WI-20260610-BACKEN-020.md

## WI-20260610-BACKEN-021
- Date: 2026-06-10
- Title: Frontend — Formulaire creation/edition Cours, ajout champ duree
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 0
- Notes: |
    - Champ "Duree (jours)" en creation/edition Cours + colonne "Duree" dans la table.
    - Nouveau PUT /api/cours/{id} (CoursController) + CoursService.updateNomEtDuree.
    - ng build OK, ./gradlew test BUILD SUCCESSFUL.
    - Note: ai_memory/2026-06-11__ROLE-developer__WI-20260610-BACKEN-019-021.md

## WI-20260610-BACKEN-022
- Date: 2026-06-10
- Title: Frontend — masquer les liens sidebar non accessibles selon le role (roleGuard)
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 0
- Notes: |
    - Suite a WI-20260610-BACKEN-011 (roleGuard). Filtrer routes[] de sidebar.ts
      selon authService.currentRole() pour les entrees admin/utilisateurs (ADMIN),
      admin/cours et admin/cursus (REF).

## WI-20260610-BACKEN-023
- Date: 2026-06-10
- Title: Nettoyage warnings IDE avant commit (backend + frontend)
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 0
- Notes: |
    - Liste de warnings IDE fournie par l'utilisateur (CursusCoursRepository,
      CoursServiceTest, CoursControllerTest, CursusService, FiliereService,
      GlobalExceptionHandler, header.scss, styles.scss).
    - L'erreur signalee dans ai_memory/2026-06-10__ROLE-developer__WI-20260610-BACKEN-011.md
      (lignes 40-42) est un faux positif IDE sur un bloc de code ```ts dans une note
      memoire (pas de code reel) - hors scope, pas d'action.

## WI-20260611-BACKEN-024
- Date: 2026-06-11
- Title: Verification fonctionnelle module Promotion (WI-012 a 021) + correctifs bugs
- Status: DONE
- TOA: manager
- Executor: manager (verification ad-hoc + correctifs directs)
- attempt_count: 0
- Notes: |
    - Verification API end-to-end du module Promotion : creation promotion avec
      cursus/rythme/eleves, generation auto du planning (algorithme correct,
      exclusion semaine "entreprise" verifiee), drag&drop avec warnings ordre
      chronologique et conflit formateur (les deux types de warnings confirmes).
    - BUG TROUVE ET CORRIGE : DELETE /api/promotions/{id} renvoyait 403 Forbidden
      au lieu de 204. Cause reelle : PromotionService.deleteById() ne supprimait
      pas les PromotionCours lies avant promotionRepository.delete(), provoquant
      une DataIntegrityViolationException (FK promotion_cours.promotion_id) ;
      cette 500 non geree est traduite en 403 par la chaine de securite (forward
      /error non authentifie). Correctif : promotionCoursRepository.deleteAll(...)
      avant la suppression de la Promotion. Confirme : DELETE -> 204.
    - BUG TROUVE ET CORRIGE : CoursService.updateNomEtDuree(id, name, dureeJours)
      ecrasait cours.name meme si name == null (PUT /api/cours/{id} avec
      name:null effacait le nom). Correctif : if (name != null) cours.setName(name).
      Confirme via curl.
    - ./gradlew test --tests "*PromotionServiceTest" --tests "*CoursServiceTest"
      -> BUILD SUCCESSFUL apres correctifs.
    - Frontend : l'ancien process ng serve datait d'avant la creation des
      fichiers frontend/src/app/features/administration/promotions/* -> redemarre,
      build OK (chunks promotions/promotions-list/promotion-detail/planning presents).
    - LIMITE : verification visuelle (design, calendrier FullCalendar colore par
      statut) non realisee - aucun outil navigateur disponible dans cette session.
      A faire manuellement par l'utilisateur sur /app/admin/promotions et
      /app/admin/promotions/:id/planning.
    - Donnees de test creees pendant la verification (users ref@eni.fr,
      formateur1@eni.fr, eleve1@eni.fr, eleve2@eni.fr, cours 2-6 dureeJours/formateur
      modifies) laissees en base de dev locale ; promotions de test 1 et 2 supprimees.

## WI-20260611-FULLST-001
- Date: 2026-06-11
- Title: UX - Sortir la gestion des eleves du modal creation/edition Promotion
- Status: DONE
- TOA: explorer
- Executor: solution-architect (design) -> developer (implementation)
- attempt_count: 1
- Implementation (developer, 2026-06-11): |
    - Backend : PromotionService.addEleve/removeEleve + PromotionController
      POST/DELETE /api/promotions/{id}/eleves/{eleveId}. Tests unitaires
      ajoutes dans PromotionServiceTest (5 nouveaux cas). ./gradlew test PASS.
      Note : a aussi reconcilie PromotionController avec le rename
      PromotionCours -> CoursPlanifie effectue en concurrence par un autre
      agent (FULLST-005) sur PromotionService.java pendant ce WI (sinon
      build casse).
    - Frontend : EntitySelectorComponent cree
      (shared/components/entity-selector/, modes 'add'/'multi-select',
      recherche+pagination+disabledIds, generique pour reuse FULLST-006).
    - promotions.ts/.html : retrait FormArray eleveIds, resetEleveIds,
      eleves signal, BaseUserAdminAdapter inject ; buildRequest n'envoie plus
      eleveIds (PromotionRequest.eleveIds rendu optionnel cote frontend).
    - promotion-detail.{ts,html,scss} : page reconstruite (header pills,
      section Effectifs avec tableau + retrait par ligne, modale "Ajouter un
      eleve" via EntitySelectorComponent mode 'add', filtree sur ETUDIANT non
      deja dans la promotion).
    - Route /app/admin/promotions/:id ajoutee dans app.routes.ts.
    - promotion.adapter.ts (+http/mock) : addEleve/removeEleve.
    - Verification : ng build PASS, ./gradlew test PASS, chrome devtools OK
      pour modal edit (sans bloc eleves) et page promotion-detail (header +
      effectifs + modale ajout). Le test live "Retirer un eleve" a recu un
      403 car le serveur backend local (port 8080) tournait sur un build
      anterieur aux nouveaux endpoints (pas de redemarrage effectue, hors
      scope/risque pour les autres agents en parallele) -- aucune donnee
      modifiee, a revalider apres redemarrage backend.
    - Note complete : ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-001.md
- Notes: |
    - DESIGN (solution-architect, 2026-06-11) : modal create/edit Promotion ne
      garde que name/cursusId/dateDebut/rythme (FormArray eleveIds supprime).
      En update, eleveIds omis du payload (PromotionService.update gere deja
      `if (request.getEleveIds() != null)` -> pas de full-replace accidentel).
      Page promotion-detail : sections Header / Effectifs (tableau eleves +
      bouton "Ajouter" via composant de selection) / lien Planning.
      Nouveau composant partage `EntitySelectorComponent`
      (frontend/src/app/shared/components/entity-selector/) : recherche +
      pagination, mode 'add' (promotion-detail) ou 'multi-select' (reuse dans
      FULLST-006 pour formateurs/prerequis, avec extension `disabledIds`).
      Endpoints proposes : POST/DELETE /api/promotions/{id}/eleves/{eleveId}
      (ajout/retrait individuel, plus propre que le full-replace existant).
      Open blocker : verifier si UserAdmin expose promotionId (necessaire
      pour filtre "disponibilite").
      Note complete : ai_memory/2026-06-11__ROLE-solution-architect__WI-20260611-FULLST-001.md
    - Origine : audit chrome devtools de /app/admin/promotions. Le modal
      Modifier/Creer une promotion liste TOUS les eleves (role ETUDIANT) en
      checkbox-list (userAdminAdapter.getAll().filter(role==='ETUDIANT')),
      sans recherche/filtre/pagination, melange avec le formulaire de
      metadonnees de la promotion (nom, cursus, dates, rythme).
    - Decision utilisateur : ne pas garder une checkbox-list exhaustive.
    - Direction retenue : separer la gestion des effectifs du formulaire de
      metadonnees. Le modal create/edit ne gere plus que nom/cursus/dates/
      rythme. La gestion des eleves (ajout/retrait) se fait sur la page
      promotion-detail (deja existante) via un tableau dedie avec recherche,
      filtre (par cursus / disponibilite) et pagination.
    - Scope : solution-architect confirme/affine le design d'interaction
      (tableau vs autre pattern), puis developer implemente frontend
      (promotion.adapter, promotions.ts/.html, promotion-detail) + adapte
      l'API si necessaire (verifier PromotionService / endpoints existants
      pour ajout/retrait individuel d'eleve).
    - Fichiers concernes (point de depart) :
      frontend/src/app/features/administration/promotions/promotions.{ts,html}
      frontend/src/app/features/promotions/promotion-detail/*
      backend/.../service/PromotionService.java (verifier endpoints add/remove eleve)

## WI-20260611-FULLST-002
- Date: 2026-06-11
- Title: A11y - Champs de formulaire sans id/label sur le modal Promotions
- Status: DONE
- TOA: explorer
- Executor: developer
- attempt_count: 1
- Resolution: |
    Root cause reelle (differente des suspects initiaux) : le champ de
    recherche globale du header (frontend/src/app/layouts/main-layout/header/header.html)
    avait un `<input type="search">` avec seulement `aria-label`, sans
    `id`/`name` ni `<label>` associe -> generait les 2 issues sur TOUTES
    les pages (donc visibles aussi sur le modal Promotions). Corrige en
    ajoutant id="global-search"/name="global-search" + un `<label class="sr-only" for="global-search">`
    (classe .sr-only ajoutee a header.scss).
    En complement, les `<label>Elèves</label>` / `<label>Formateurs</label>` /
    `<label>Prerequis</label>` (legendes de groupes formArrayName, sans `for`,
    n'enveloppant aucun input) ont ete remplaces par `<span class="label1" id="...">`
    + `role="group" aria-labelledby="..."` sur le `.checkbox-list` correspondant,
    dans promotions.html (2 occurrences) et cours.html (4 occurrences),
    pour eviter tout "dangling label" similaire.
    Issue planning (FullCalendar select "Sélectionner une promotion") :
    non reproduite lors de la verification (0 issue [issue] constate sur
    /app/admin/promotions/3/planning) ; documentee mais non traitee, sera
    de toute facon supprimee par WI-20260611-FULLST-005.
- Notes: |
    - Origine : audit chrome devtools de /app/admin/promotions (modal
      "Modifier la promotion"). Console DevTools remonte 2 issues :
      "A form field element should have an id or name attribute" et
      "No label associated with a form field".
    - Scope : identifier le(s) champ(s) concerne(s) (suspects : input date
      "Date de debut" decompose en spinbuttons jour/mois/annee par le
      navigateur, et/ou les checkbox eleves generes via formArrayName sans
      id unique par item) et corriger (id + label associe ou aria-label).
    - Verification : re-audit chrome devtools (list_console_messages) ->
      0 issue [issue] sur la page apres correction. Verifier aussi le
      formulaire equivalent du Catalogue de cours (meme pattern
      formArrayName) au cas ou le meme defaut existe.
    - Fichiers concernes (point de depart) :
      frontend/src/app/features/administration/promotions/promotions.html

## WI-20260611-FULLST-003
- Date: 2026-06-11
- Title: i18n - Traduire en francais les libelles FullCalendar (today/month/week) sur Planning
- Status: CANCELLED
- TOA: explorer
- Executor: developer
- attempt_count: 0
- Notes: |
    - Origine : audit chrome devtools de /app/admin/promotions/:id/planning -
      la toolbar FullCalendar affiche "today" / "month" / "week" en anglais
      alors que le reste de l'UI est en francais.
    - CANCELLED : superseded par WI-20260611-FULLST-005 (remplacement complet
      de FullCalendar par un composant base sur mon-calendrier, deja en
      francais). Pas d'action separee necessaire.

## WI-20260611-FULLST-004
- Date: 2026-06-11
- Title: UX - Planning : ouvrir le calendrier sur le mois de dateDebut de la promotion selectionnee
- Status: CANCELLED
- TOA: explorer
- Executor: developer
- attempt_count: 0
- Notes: |
    - Origine : audit chrome devtools de /app/admin/promotions/:id/planning -
      le calendrier FullCalendar s'ouvre sur le mois courant (juin 2026) meme
      si la promotion selectionnee demarre en juillet 2026 ; navigation
      manuelle requise.
    - CANCELLED : superseded par WI-20260611-FULLST-005 (remplacement complet
      de FullCalendar). Le comportement "date de reference initiale =
      dateDebut de la promotion" doit etre repris dans le nouveau composant
      (cf. notes de FULLST-005).

## WI-20260611-FULLST-005
- Date: 2026-06-11
- Title: Planning promotion - remplacer FullCalendar par un calendrier base sur mon-calendrier (sans drag&drop)
- Status: DONE (perimetre reduit)
- TOA: explorer
- Executor: solution-architect (design interaction) -> developer
- attempt_count: 1
- Notes: |
    - PERIMETRE REDUIT (decision manager, 2026-06-11) : ce WI couvre
      strictement "edition de date d'une session deja planifiee
      (PromotionCours existante)" via PUT
      /api/promotions/{id}/planning/{promotionCoursId} (updatePlanning).
      HORS SCOPE (reportes a FULLST-008/009 ou nouveaux sous-WI) :
      creation d'une nouvelle session sur un jour vide (aucun endpoint
      backend), suppression d'une session (aucun endpoint backend), champ
      "formateur par session" dans le formulaire (pas de notion de
      formateur par PromotionCours dans le modele actuel).
    - IMPLEMENTATION (developer, 2026-06-11) : planning.ts/.html/.scss
      reecrits, @fullcalendar/* retires de package.json + node_modules,
      modal "Modifier la session" (dateDebut/dateFin, validation
      dateFin >= dateDebut, warnings non bloquants via .warning-box).
      Repli "portage duplique" retenu (pas d'extraction de
      month-week-grid partage) -> dette de duplication avec
      mon-calendrier.ts/.html/.scss documentee en PITFALL.
      ng build : aucune erreur/warning sur les fichiers planning ; build
      global toujours bloque par des erreurs TS preexistantes et hors
      scope dans promotions.ts/promotion-detail.ts (WIP FULLST-001/002,
      proprietes eleveIds/resetEleveIds manquantes). Verification visuelle
      chrome devtools non concluante (serveur ng serve existant sur :4200
      sert un build perime, et un nouveau build sur :4201 echoue a cause
      des memes erreurs hors scope).
      Note complete : ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-005.md
    - DESIGN (solution-architect, 2026-06-11) : API backend confirmee
    - DESIGN (solution-architect, 2026-06-11) : API backend confirmee
      inchangee (PUT /api/promotions/{id}/planning/{promotionCoursId},
      warnings ordre chronologique + conflits formateur deja geres). GET
      /api/promotions/{id} renvoie toujours warnings=[] (PromotionController
      ligne 76) -> warnings visibles uniquement apres save, a documenter.
      Recommandation : extraire un composant partage
      `frontend/src/app/shared/components/month-week-grid/`
      (MonthWeekGridComponent) depuis la logique grille de mon-calendrier
      (monthGrid/weekDays/eventsByDay/navTitle/navigation), reutilise par
      MonCalendrierComponent et le nouveau PlanningComponent (repli accepte :
      portage duplique + dette si juge hors budget).
      Interaction : lecture + clic sur un PromotionCours existant -> modal
      edition dateDebut/dateFin -> updatePlanning(), warnings affiches en
      encart non bloquant. referenceDate initialisee sur promotion.dateDebut.
      OPEN BLOCKERS (a trancher avant impl complete) : "Planifier un cours"
      (jour vide) et "Supprimer une session" n'ont AUCUN endpoint backend
      correspondant -> perimetre reduit a "edition de date d'une session
      existante" sauf decision contraire (nouveau sous-WI backend). Pas de
      notion de "formateur par session" dans le modele actuel (formateurs =
      Cours.formateurs, possible lien FULLST-007).
      Retirer @fullcalendar/* de package.json si plus utilise ailleurs.
      Note complete : ai_memory/2026-06-11__ROLE-solution-architect__WI-20260611-FULLST-005.md
    - Origine : decision utilisateur - le calendrier FullCalendar avec
      drag&drop (WI-20260610-BACKEN-020) est juge trop complexe et non
      desire ("je n'aime pas FullCalendar"). Le composant
      frontend/src/app/features/calendrier/mon-calendrier/* (vue mois/
      semaine, navigation FR, date-fns) doit etre reutilise/adapte pour
      la page Planning promotion.
    - Interaction retenue (a affiner par solution-architect) : le calendrier
      passe en lecture + clic. Cliquer sur un jour (ou un bouton "Planifier
      un cours") ouvre un formulaire : selection du cours (parmi ceux du
      cursus de la promotion), date debut / date fin, formateur. Edition/
      suppression d'un cours deja planifie via clic sur l'evenement. Pas de
      glisser-deposer.
    - Reprend aussi : ouverture initiale du calendrier sur le mois de
      dateDebut de la promotion selectionnee (cf. FULLST-004, cancelled et
      fusionne ici), et libelles 100% francais (cf. FULLST-003, cancelled et
      fusionne ici).
    - Scope : solution-architect propose le design d'interaction et verifie
      la compatibilite avec le moteur de planification automatique
      (WI-20260610-BACKEN-016) et la detection de conflits formateurs
      (WI-20260610-BACKEN-018) qui restent valables cote backend (l'API ne
      change pas, seule l'UI change). Puis developer remplace
      frontend/src/app/features/administration/promotions/planning/* et
      retire la dependance @fullcalendar/* (package.json) si plus utilisee
      ailleurs.
    - Fichiers concernes (point de depart) :
      frontend/src/app/features/administration/promotions/planning/*
      frontend/src/app/features/calendrier/mon-calendrier/*
      frontend/package.json (deps @fullcalendar/*)

## WI-20260611-FULLST-006
- Date: 2026-06-11
- Title: UX - Refonte du tableau Catalogue de cours et des modals formateurs/prerequis
- Status: DONE
- TOA: explorer
- Executor: developer
- attempt_count: 1
- Notes: |
    - DONE (developer, 2026-06-11) : implementation terminee et verifiee via
      chrome devtools sur /app/admin/cours. Tableau : colonnes
      Formateurs/Prerequis/Requis par limitees a 3 badges + "+X autres" via
      <details><summary> natif. Modals create/edit : EntitySelectorComponent
      (mode multi-select) avec recherche pour Formateurs et Prerequis,
      anti-cycle conserve via disabledIds. ng build PASS. Note de role :
      ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-006.md
    - DESIGN (solution-architect, 2026-06-11) : tableau -> limiter chaque
      colonne (Formateurs/Prerequis/Requis par) a 3 badges visibles + badge
      "+X autres" via <details><summary> natif (zero JS, accessible) ouvrant
      un popover listant le reste.
      Modals create/edit -> remplacer les FormArray formateurIds/prerequisIds
      par EntitySelectorComponent (mode 'multi-select', meme composant partage
      que FULLST-001 : frontend/src/app/shared/components/entity-selector/).
      Prerequis : conserver la logique anti-cycle existante
      (isTransitivePrerequis/disabledPrerequisIds, cours.ts lignes 191-229)
      via extension `disabledIds: input<Set<number>>` sur le composant
      partage (items presents mais non-selectionnables).
      DEPENDANCE : FULLST-001 doit fournir EntitySelectorComponent avec
      `disabledIds` -> sequencer FULLST-001 avant FULLST-006, ou etendre le
      composant ici si FULLST-001 deja merge sans cette option.
      Aucun changement API necessaire (setFormateurs/setPrerequis existants).
      Note complete : ai_memory/2026-06-11__ROLE-solution-architect__WI-20260611-FULLST-006.md
    - Origine : audit code de frontend/src/app/features/administration/cours/
      cours.html. Deux problemes :
      1. Colonnes "Formateurs" / "Prerequis" / "Requis par" affichent des
         listes de badges qui peuvent deborder/empiler quand un cours a
         beaucoup de formateurs ou de prerequis -> table illisible a
         l'echelle.
      2. Modals create/edit utilisent une checkbox-list exhaustive pour
         "Formateurs" et "Prerequis" (meme anti-pattern que
         FULLST-001 pour les eleves de promotion).
    - Scope : developer redesign le tableau (ex : limiter l'affichage a N
      badges + "+X autres" avec tooltip/popover, ou colonne resumee avec
      detail au clic) et remplace les checkbox-lists par des selecteurs avec
      recherche (coherent avec la solution retenue dans FULLST-001).
    - Fichiers concernes (point de depart) :
      frontend/src/app/features/administration/cours/cours.{ts,html,scss}

## WI-20260611-FULLST-007
- Date: 2026-06-11
- Title: Architecture - Modele CoursPlanifie / InscriptionCours (inscriptions promo + a l'unite)
- Status: READY_FOR_REVIEW
- TOA: explorer
- Executor: solution-architect
- attempt_count: 1
- Notes: |
    - DONE (solution-architect, 2026-06-11) : analyse livree dans
      ai_doc/ANALYSIS__WI-20260611-FULLST-007__cours-planifie-inscription.md.
      Recommandation : renommer/refondre PromotionCours en CoursPlanifie
      (lien promotion devient nullable) + nouvelle entite InscriptionCours
      (eleve <-> CoursPlanifie, contrainte UNIQUE user_id+cours_planifie_id).
      Reutilise PlanificationService (BACKEN-016) et la detection de
      conflits formateurs (BACKEN-018) sans duplication. Points a trancher
      par FULLST-008 : champ formateur explicite sur CoursPlanifie ou non,
      et strategie de migration de schema (Flyway/Liquibase vs ddl-auto).
      Risque critique identifie : eviter double inscription promo +
      individuelle sur le meme CoursPlanifie. Note de role :
      ai_memory/2026-06-11__ROLE-solution-architect__WI-20260611-FULLST-007.md
    - Origine : besoin utilisateur - un eleve peut etre inscrit a un cours a
      l'unite sans etre dans une promotion ; ce "cours a l'unite" est en
      realite un cours planifie (a l'unite egalement). Un eleve peut aussi
      etre inscrit a un cours planifie dans le cadre d'une promotion. Le
      formateur doit toujours voir la liste complete des eleves inscrits a
      un cours qu'il animera, qu'ils viennent de la promotion ou d'une
      inscription individuelle.
    - Etat actuel : PromotionCours fusionne "session planifiee d'un cours"
      (dateDebut/dateFin/statut/ordre) et "lien vers une promotion". Aucune
      entite d'inscription individuelle eleve <-> session de cours n'existe.
    - Scope : solution-architect propose un modele cible, ex :
      - CoursPlanifie (instance planifiee d'un Cours : dateDebut, dateFin,
        formateur, statut, lien optionnel vers Promotion - 0..1) - possible
        refonte/renommage de PromotionCours pour rendre le lien promotion
        optionnel.
      - InscriptionCours (eleve <-> CoursPlanifie, inscription
        individuelle "a l'unite").
      - Regles d'agregation : calendrier eleve = CoursPlanifie de sa
        promotion (si membre) UNION CoursPlanifie ou il a une
        InscriptionCours individuelle. Vue formateur d'un CoursPlanifie =
        eleves de la promotion liee (si presente) UNION eleves avec
        InscriptionCours sur ce CoursPlanifie.
    - Livrable : document d'analyse (ai_doc/ANALYSIS__WI-20260611-FULLST-007__*.md)
      couvrant modele de donnees, impacts sur PromotionCours/PromotionService
      existants, et impacts API. Sert de base a FULLST-008 et FULLST-009.
    - Depend de : aucun (peut demarrer immediatement). FULLST-008 et
      FULLST-009 dependent de son resultat.

## WI-20260611-FULLST-008
- Date: 2026-06-11
- Title: Backend - Implementation du modele CoursPlanifie / InscriptionCours
- Status: DONE
- TOA: explorer
- Executor: developer
- attempt_count: 1
- Notes: |
    - Scope : implementer le modele issu de l'analyse FULLST-007 (entites
      JPA, migrations/schema, repository, service, endpoints API pour :
      creer une inscription individuelle, lister les inscrits combines
      d'un CoursPlanifie, lister le planning agrege d'un eleve).
    - Depend de : WI-20260611-FULLST-007 (analyse doit etre validee avant
      implementation).
    - Verification : tests unitaires/integration (./gradlew test) sur le
      nouveau service + endpoints.
    - IMPLEMENTATION (developer, 2026-06-11) :
      * Renommage complet PromotionCours -> CoursPlanifie (entite, statut
        enum, repository, DTO response, exception NotFound, service
        PromotionService/PlanificationService, controller PromotionController,
        tests PromotionServiceTest/PlanificationServiceTest). `promotion`
        rendu nullable (`@ManyToOne(optional = true)`,
        `@JoinColumn(name="promotion_id", nullable = true)`).
      * Tous les `.getPromotion()` sur CoursPlanifie audites et gardes par
        `!= null` (PromotionService.updatePlanning : verification
        d'appartenance + boucle de conflits formateurs).
      * Decision formateur explicite : NON ajoute sur CoursPlanifie (garde
        la deduction via cours.getFormateurs(), comme avant) -> dette
        documentee, a trancher si FULLST-009 en a besoin.
      * Migration schema : pas de Flyway/Liquibase dans le projet
        (spring.jpa.hibernate.ddl-auto=update, profil local uniquement).
        Le renommage cree une nouvelle table `cours_planifie` au prochain
        demarrage ; l'ancienne table `promotion_cours` (si elle existe en
        local/dev) reste orpheline et doit etre supprimee manuellement par
        qui gere l'environnement local (pas de donnees de prod concernees).
      * Nouvelle entite InscriptionCours (eleve <-> CoursPlanifie,
        UNIQUE(user_id, cours_planifie_id)) + InscriptionCoursRepository.
      * Nouveau service InscriptionCoursService : creerInscription (rejette
        409 si deja couvert par la promotion OU deja inscrit
        individuellement), supprimerInscription, getInscritsCombines
        (UNION promo + individuel, dedup par User), getPlanningEleve (UNION
        promo + individuel, dedup par CoursPlanifie).
      * Nouveau controller InscriptionCoursController avec 4 endpoints (voir
        ci-dessous), DTOs InscriptionCoursRequest/Response, InscritResponse,
        PlanningEleveResponse, OrigineInscription (PROMOTION|INDIVIDUEL).
      * SecurityConfig : regles ajoutees pour /api/cours-planifies/** et
        /api/eleves/*/planning (voir Endpoints).
      * Tests : 7 nouveaux tests unitaires InscriptionCoursServiceTest (creation
        OK, conflit promo, conflit doublon individuel, agregation inscrits
        avec/sans promotion, agregation planning eleve avec dedup).
      * ./gradlew test : BUILD SUCCESSFUL (tous les tests passent, y compris
        PromotionServiceTest/PlanificationServiceTest renommes).
    - ENDPOINTS DISPONIBLES POUR FULLST-009 :
      * POST /api/cours-planifies/{id}/inscriptions
        body: { "eleveId": Long } -> 201 InscriptionCoursResponse
        { id, eleveId, coursPlanifieId, dateInscription }
        409 si deja couvert (promo ou doublon individuel) -> InscriptionAlreadyExistsException
        Roles : ADMINISTRATEUR, REFERENTE_ADMINISTRATIVE
      * DELETE /api/cours-planifies/{id}/inscriptions/{eleveId} -> 204
        404 si inscription individuelle inexistante -> InscriptionNotFoundException
        Roles : ADMINISTRATEUR, REFERENTE_ADMINISTRATIVE
      * GET /api/cours-planifies/{id}/inscrits -> 200 List<InscritResponse>
        { eleveId, firstName, lastName, origine: PROMOTION|INDIVIDUEL }
        Roles : ADMINISTRATEUR, REFERENTE_ADMINISTRATIVE, FORMATEUR
      * GET /api/eleves/{id}/planning -> 200 List<PlanningEleveResponse>
        { coursPlanifieId, coursId, coursNom, dateDebut, dateFin, ordre,
          statut: PLANIFIE|EN_COURS|TERMINE, origine: PROMOTION|INDIVIDUEL }
        Roles : authenticated (a affiner cote frontend pour restreindre a
        l'eleve lui-meme ou ADMIN/REFERENT/FORMATEUR - pas de verif
        d'ownership cote backend dans ce WI).
    - DTO renomme PromotionCoursResponse -> CoursPlanifieResponse (champ
      `planning` de PromotionResponse desormais List<CoursPlanifieResponse>,
      meme structure JSON, pas de breaking change pour les adapters
      frontend existants).

## WI-20260611-FULLST-009
- Date: 2026-06-11
- Title: Frontend - Calendrier eleve agrege (promo + a l'unite) et vue formateur des inscrits
- Status: READY_FOR_REVIEW
- TOA: explorer
- Executor: developer
- attempt_count: 1
- Notes: |
    - Scope :
      1. Calendrier eleve (frontend/src/app/features/calendrier/mon-calendrier/*)
         affiche les CoursPlanifie de sa promotion + ses InscriptionCours
         individuelles, en distinguant visuellement les deux origines.
      2. Vue formateur (a localiser/creer) : pour un CoursPlanifie donne,
         afficher la liste combinee des eleves inscrits (promotion + a
         l'unite).
    - Depend de : WI-20260611-FULLST-007 (modele) et WI-20260611-FULLST-008
      (API backend disponible).
    - IMPLEMENTATION (developer, 2026-06-11) :
      * Nouveau modele frontend/src/app/core/models/inscription.model.ts
        (PlanningEleve, InscritCours, OrigineInscription).
      * CalendarEvent (calendar-event.model.ts) : ajout champ optionnel
        `origine?: 'PROMOTION' | 'INDIVIDUEL'`.
      * Nouvel adapter HttpElevePlanningAdapter
        (core/adapters/eleve-planning-http.adapter.ts), implemente
        BaseCalendarAdapter, appelle GET /api/eleves/{id}/planning (id =
        authService.currentUserId()), mappe vers CalendarEvent (origine
        PROMOTION -> `promotion` rempli pour reutiliser le code couleur
        existant bleu/vert deja code en dur dans mon-calendrier.html/scss).
        Retourne [] si currentUserId() est null.
      * app.config.ts : BaseCalendarAdapter pointe maintenant sur
        HttpElevePlanningAdapter (plus MockCalendarAdapter).
      * Nouveau module formateur :
        frontend/src/app/features/formateur/inscrits/{inscrits.ts,html,scss}
        - Page lecture seule, lit :id (coursPlanifieId) depuis la route,
          appelle GET /api/cours-planifies/{id}/inscrits via nouvel adapter
          BaseInscriptionAdapter / HttpInscriptionAdapter
          (core/adapters/inscription{.adapter,-http.adapter}.ts), affiche
          tableau Nom/Prenom/Origine avec badge PROMOTION (bleu) /
          INDIVIDUEL (vert).
      * Route ajoutee dans app.routes.ts :
        path 'cours-planifies/:id/inscrits', roleGuard(['FORMATEUR']),
        lazy-load InscritsComponent.
      * FIX CONNEXE (bloquant pour cette WI) : AuthService
        (core/services/auth.service.ts) ne rehydratait PAS `_currentUser`
        (donc currentUserId()) depuis localStorage au demarrage/reload -
        seul `_isAuthenticated` etait restaure. Corrige : `uid` est
        maintenant persiste dans localStorage['user'] au login, et
        `_currentUser`/`_currentRole` sont initialises depuis
        localStorage si un token existe. Sans ce correctif,
        HttpElevePlanningAdapter retournait toujours [] apres un reload
        (currentUserId() == null). logout() nettoie aussi 'user'.
    - VERIFICATION :
      * `npx ng build` (cd frontend) : BUILD SUCCESSFUL (warnings SCSS
        budget preexistants sur register/utilisateurs/planning, non lies
        a cette WI).
      * Verification visuelle chrome-devtools (ng serve :4200) : page
        /app/calendrier rend correctement (legende promo/unit visible,
        grille mois OK, pas d'erreur console). Login admin@admin.com OK,
        rehydratation du role confirmee apres reload (sidebar affiche
        "Utilisateurs" reserve a ADMIN).
      * LIMITE DE VERIFICATION : aucun compte ETUDIANT/FORMATEUR de test
        disponible (seul admin@admin.com seede, politique projet "pas de
        comptes de demo"). De plus le login admin@admin.com renvoie
        `uid: null` cote backend -> currentUserId() reste null meme apres
        le fix de rehydratation, donc l'appel GET /api/eleves/{id}/planning
        n'a pas pu etre observe en conditions reelles. L'adapter degrade
        proprement (retourne []) dans ce cas. Page formateur
        /app/cours-planifies/:id/inscrits non testee visuellement (role
        FORMATEUR indisponible) mais build/route OK.
      * Recommandation : fournir un compte ETUDIANT et un compte
        FORMATEUR de test (ou verifier pourquoi `uid` est null dans la
        reponse de login pour admin@admin.com) pour completer la
        verification visuelle de cette WI.

## WI-20260611-FULLST-010
- Date: 2026-06-11
- Title: UX - Planning promotion : calendrier visualisation seule + liste interactive des sessions (vue unifiee multi-jours)
- Status: SUPERSEDED (par FULLST-012)
- TOA: explorer
- Executor: developer
- attempt_count: 1
- Notes: |
    - SUPERSEDED (2026-06-11) : l'utilisateur a fourni un design de
      reference (PromoDetail/PlanCourseModal) remplacant le calendrier
      Gantt par un tableau "Cours planifies". Le travail de cette WI
      (barres continues mon-calendrier, lecture seule, liste editable)
      est remplace par FULLST-012. Le code reste dans l'historique git
      mais la page planning sera retiree/fusionnee par FULLST-012.
    - Origine : retour utilisateur apres verification de FULLST-005 (planning
      sans FullCalendar). Deux demandes :
      1. Le calendrier (mon-calendrier, frontend/src/app/features/calendrier/
         mon-calendrier/) devient un affichage en LECTURE SEULE pour le
         planning de promotion.
      2. Ajouter une LISTE des sessions (CoursPlanifie de la promotion) a
         cote du calendrier, interactive : edition des dates (debut/fin) ET
         d'autres champs (formateur, salle, statut) directement depuis la
         liste, avec la validation/detection de conflits deja existante
         (warnings).
      3. Dans le calendrier, au lieu d'un item par jour pour un meme cours
         planifie, afficher UNE SEULE barre continue couvrant toute la duree
         (du dateDebut au dateFin), comme une vue type Gantt/timeline. Decision
         utilisateur : ADAPTER le composant mon-calendrier existant (partage
         avec calendrier eleve/formateur) pour supporter ce rendu en barre
         continue, plutot que de creer un composant separe.
    - Decision : nouvelle WI complementaire a FULLST-005 (qui reste la base :
      planning.{ts,html,scss}, frontend/src/app/features/administration/
      promotions/planning/). FULLST-005 n'est pas rouverte.
    - Fichiers concernes (point de depart) :
      frontend/src/app/features/administration/promotions/planning/planning.{ts,html,scss}
      frontend/src/app/features/calendrier/mon-calendrier/ (extension rendu barre continue)
    - A construire par le developer/solution-architect : design de la barre
      continue dans la grille mois/semaine de mon-calendrier (calcul du span
      en colonnes/lignes selon dateDebut/dateFin de la session vs les bornes
      de la semaine affichee), + structure de la liste interactive (table
      editable inline avec validation, reuse des warnings de conflits deja
      geres par PlanificationService cote backend).

## WI-20260611-FULLST-011
- Date: 2026-06-11
- Title: Backend - CoursPlanifie.formateur + salle (champs par session) + endpoint + conflits
- Status: READY_FOR_REVIEW
- TOA: explorer
- Executor: solution-architect, developer
- attempt_count: 0
- Notes: |
    - Origine : design "Promotion detail" fourni par l'utilisateur (table Cours
      planifies avec colonnes Formateur (avatar) et Salle, editables).
    - Scope : ajouter sur CoursPlanifie deux champs nullable :
      - formateur : ManyToOne User (role FORMATEUR, valide a l'assignation)
      - salle : String
      Endpoint de mise a jour (PUT /api/cours-planifies/{id} ou extension
      PlanningUpdateRequest existant). Etendre la detection de conflits
      formateur (WI-20260610-BACKEN-018) pour couvrir ce nouveau champ
      (chevauchement de dates pour un meme formateur, toutes promotions
      confondues).
    - Dependance : aucune (independant de FULLST-012/013).
    - A faire par solution-architect : verifier l'impact sur
      PlanificationService / detection de conflits existante avant
      implementation.

## WI-20260611-FULLST-012
- Date: 2026-06-11
- Title: Frontend - Page "Gestion de la promotion" (tabs Cours planifies / Stagiaires + modale Planifier un cours)
- Status: READY_FOR_REVIEW (implemente par developer, build PASS, verification visuelle non faite - voir memoire)
- TOA: explorer
- Executor: solution-architect, developer
- attempt_count: 1
- Design: ai_doc/ANALYSIS__WI-20260611-FULLST-012__gestion-promotion.md (Option 1 retenue : conteneur
    promotion-detail.ts + cours-planifies-tab + stagiaires-tab + plan-course-modal, standalone).
    Risque #1 (endpoint creation CoursPlanifie potentiellement absent) et Risque #2 (badge
    Promotion/Unite) a verifier par le developer en debut de tache, remonter au manager si bloquant.
    FULLST-011 livree (READY_FOR_REVIEW) : contrat formateurId/formateurNom/salle disponible.
- Notes: |
    - Origine : design React fourni par l'utilisateur (PromoDetail,
      PlanCourseModal, Meta) -- remplace le calendrier Gantt de FULLST-010
      (SUPERSEDED) et fusionne promotion-detail (WI-001) avec planning
      (WI-005/010) sur /app/admin/promotions/:id.
    - Scope :
      1. Header promotion : badges filiere/statut, titre, Meta blocks
         (Cursus, Periode, Stagiaires, Cours planifies X/Y), boutons
         "Planifier un cours" et "Inscrire un stagiaire" (ce dernier ouvre
         EnrollmentForm -- FULLST-015, hors scope ici, prevoir le hook
         seulement).
      2. Tabs "Cours planifies (N)" / "Stagiaires (N)".
      3. Tab Cours planifies : tableau Cours/Periode/Formateur (avatar,
         depend de FULLST-011)/Salle/Inscrits/Actions (pencil edition).
         EmptyState si aucun cours planifie.
      4. Tab Stagiaires : grille 2 colonnes des inscrits avec badge
         Promotion/Unite (reprend la logique InscriptionCours
         PROMOTION/INDIVIDUEL de FULLST-008/009), retrait possible
         (reprend addEleve/removeEleve de FULLST-001).
      5. Modale "Planifier un cours" (PlanCourseModal du design) :
         selection du cours du cursus (FilterSelect), bandeau de statut
         prerequis (vert si tous satisfaits, ambre si non respectes,
         liste detaillee par prerequis avec etat planifie/non planifie),
         dates debut/fin, formateur assigne (depend FULLST-011), salle
         optionnelle, bandeau d'avertissement non bloquant si prerequis
         non respectes ("Planifier malgre tout").
    - Dependance : FULLST-011 pour les champs formateur/salle (le
      developer peut commencer la structure de page sans, et brancher une
      fois FULLST-011 livre).
    - FULLST-014 (ordre pedagogique dans la liste planning) est FUSIONNEE
      ici : le bandeau prerequis de la modale Planifier un cours couvre ce
      besoin.
    - Fichiers concernes : frontend/src/app/features/promotions/promotion-detail/*,
      frontend/src/app/features/administration/promotions/planning/* (a
      retirer/fusionner), routes app.routes.ts.

## WI-20260611-FULLST-013
- Date: 2026-06-11
- Title: Frontend - Liste promotions : retrait icones calendrier/stylo, clic ligne -> navigation, modal confirmation suppression
- Status: READY_FOR_REVIEW
- TOA: explorer
- Executor: developer
- attempt_count: 1
- Notes: |
    - Scope (inchange depuis la premiere proposition) :
      - Retirer les icones calendrier (lien planning) et stylo (edition)
        de la liste des promotions
        (frontend/src/app/features/administration/promotions/promotions.html/.ts).
      - Clic sur une ligne -> navigation vers /app/admin/promotions/:id
        ("Gestion de la promotion", FULLST-012).
      - Suppression : modal de confirmation affichant le nombre
        d'eleves/sessions lies a la promotion, suppression effective apres
        confirmation (cascade backend deja geree).
    - Independant de FULLST-011/012 (peut demarrer en parallele), mais la
      cible de navigation (FULLST-012) doit exister pour etre testable
      end-to-end.

## WI-20260611-FULLST-014
- Date: 2026-06-11
- Title: UX - Ordre pedagogique + formateur par session dans planning (FUSIONNEE dans FULLST-012)
- Status: MERGED
- TOA: explorer
- Executor: -
- attempt_count: 0
- Notes: |
    - MERGED (2026-06-11) : suite a la fourniture du design PromoDetail/
      PlanCourseModal, ce besoin (visualisation des dependances de
      prerequis + alerte non bloquante, edition formateur par session) est
      couvert par FULLST-012 (modale "Planifier un cours" + tableau Cours
      planifies). Pas d'implementation separee.

## WI-20260611-FULLST-015
- Date: 2026-06-11
- Title: Frontend - Formulaire d'inscription (EnrollmentForm) avec controles ordre pedagogique (422) et conflit (409)
- Status: PARTIELLEMENT COUVERT par FULLST-019
- TOA: explorer
- Executor: solution-architect, developer
- attempt_count: 0
- Implementation (developer, 2026-06-11, via FULLST-019): |
    - Couvert : le bouton "Inscrire un stagiaire" (promotion-detail.html) ouvre
      desormais la modale "Ajouter un eleve" existante (cas "Promotion complete"),
      fonctionnel de bout en bout (POST /api/promotions/{id}/eleves/{eleveId}, fixe
      par FULLST-019 Bug #1).
    - Reste a faire (non couvert, scope EnrollmentForm complet) :
      - RadioCard choix type d'inscription (Promotion complete / Cours a l'unite).
      - Selection d'une session CoursPlanifie individuelle via InscriptionCours
        (FULLST-008).
      - Bandeau ordre pedagogique respecte/non respecte (prerequis du cours cible vs
        sessions deja suivies/planifiees) + checkbox "Forcer l'inscription".
      - Gestion conflit 409 (eleve deja inscrit).
      - Verifier si le backend expose deja un controle d'ordre pedagogique a
        l'inscription individuelle (sinon nouvelle WI backend).
    - Voir ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-019.md section
      "Next Actions" pour le detail.
- Notes: |
    - Origine : design React fourni par l'utilisateur (EnrollmentForm,
      RadioCard). Reservee pour une iteration future, non prioritaire dans
      ce batch (priorite donnee a FULLST-011/012/013).
    - Scope previsionnel : recherche de stagiaire (autocomplete), choix
      type d'inscription (Promotion complete / Cours a l'unite), selection
      promotion ou session (CoursPlanifie via InscriptionCours,
      FULLST-008/009), bandeau ordre pedagogique respecte/non respecte
      (verification prerequis du cours cible vs sessions deja planifiees/
      suivies par l'eleve), checkbox "Forcer l'inscription" si non
      respecte, bandeau conflit si l'eleve est deja inscrit (409 backend).
    - Dependance : InscriptionCours (FULLST-008, DONE) pour la creation
      d'inscription individuelle ; verifier si le backend expose deja un
      controle d'ordre pedagogique a l'inscription (sinon nouvelle WI
      backend a prevoir lors du design).

## WI-20260611-FULLST-016
- Date: 2026-06-11
- Title: Frontend - Cursus : drag&drop ordre pedagogique + correction auto prerequis manquants/mal ordonnes
- Status: READY_FOR_REVIEW
- TOA: explorer
- Executor: solution-architect, developer
- attempt_count: 1
- Implementation (developer, 2026-06-11): |
    - Complete via FULLST-018 scope B : drag&drop CursusCard (HTML5 draggable, persiste
      via BaseCursusAdapter.reorder/BACKEN-007) + boutons Monter/Descendre (alternative
      clavier). groupedByFiliere, NewFiliereModal, NewCursusModal (prereqs manquants
      "?"+Ajouter, mal ordonnes + Corriger/fixOrder, monter/descendre/retirer) deja
      livres par BACKEN-007, confirmes presents et fonctionnels.
    - Seul ecart : pas de palette de couleurs dans NewFiliereModal (Filiere.couleur
      n'existe pas cote backend) -- documente dans
      ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-018.md.
    - ng build PASS. Verification visuelle non faite (cf FULLST-018).
- Notes: |
    - Origine : design React fourni par l'utilisateur (Cursus, CursusCard,
      NewCursusModal). Reservee pour une iteration future, non prioritaire
      dans ce batch.
    - Scope previsionnel : page Cursus (frontend/src/app/features/
      administration/cursus/) -- cards par filiere avec liste de cours
      drag&drop (reordonnancement de CursusCours.ordre), modale
      "Nouveau cursus" avec detection des prerequis manquants (lignes
      fantomes "?" + bouton "Ajouter") et mal ordonnes (bandeau + bouton
      "Corriger" qui repositionne automatiquement), modale "Nouvelle
      filiere" (nom + couleur).
    - Dependance : modele Cours.prerequis (anti-cycle, FULLST-006/
      WI-20260610-BACKEN-004) deja en place cote backend, reutilisable.
    - Demarree avec FULLST-018 (2026-06-11) : page existante
      frontend/src/app/features/administration/cursus/cursus.{ts,html,scss}
      (route /app/admin/cursus deja enregistree dans app.routes.ts) a
      etendre/refondre selon le design "Screens B" (Cursus, CursusCard,
      NewFiliereModal, NewCursusModal) fourni par l'utilisateur.

## WI-20260611-FULLST-018
- Date: 2026-06-11
- Title: Frontend - Refonte liste Promotions (cartes) + Page Cursus & Filieres (FULLST-016)
- Status: READY_FOR_REVIEW
- TOA: explorer
- Executor: developer
- attempt_count: 1
- Implementation (developer, 2026-06-11): |
    - Promotions : promotions.html/.ts/.scss reecrits, grille de cartes 3 colonnes
      (bandeau couleur filiere, status-pill derive, titre, ligne cursus, periode via
      fmtRange, badge stagiaires, "Details ->"), carte cliquable -> /app/admin/promotions/:id.
      Barre de filtres (recherche + filiere + annee). EmptyState avec CTA. Modal creation
      et modal suppression conserves ; bouton suppression deplace sur un icone discret du
      footer de carte (stopPropagation).
    - Cursus & Filieres (FULLST-016 scope B) : ajout drag&drop (HTML5 draggable +
      onDragStart/onDragOver/onDrop) sur la liste ordonnee de chaque CursusCard, persiste
      via BaseCursusAdapter.reorder() (BACKEN-007, deja cable). Alternative clavier :
      boutons Monter/Descendre par ligne (moveCoursInCursus). Le reste du scope B
      (groupedByFiliere, NewFiliereModal, NewCursusModal avec prereqs manquants/mal
      ordonnes + fixOrder) etait deja livre par BACKEN-007.
    - Ecarts modele documentes (pas de champ backend invente) : Filiere.couleur absent ->
      reuse FILIERE_COLORS deterministe (deja utilise dans cursus.ts) ; Promotion.statut
      et Promotion.dateFin absents -> statut derive client-side depuis dateDebut/
      planning[].dateFin (computeStatut), dateFin = max(planning[].dateFin).
    - ng build : PASS (chunks promotions 22.19kB / cursus 28.30kB raw). Warning SCSS
      promotions.scss 4.43kB (>4kB warning, <12kB error, non bloquant).
    - LIMITE : verification visuelle chrome-devtools non faite (login admin@admin.com /
      admin -> 401, pas de credentials de test valides connus). A refaire par
      manager/QA avec credentials valides sur /app/admin/promotions et /app/admin/cursus.
    - Reste a faire (hors scope, documente) : palette de couleurs dans "Nouvelle filiere"
      (Screens B NewFiliereModal) impossible sans champ Filiere.couleur backend.
    - Note complete : ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-018.md
- Notes: |
    - Origine : l'utilisateur a fourni 2 fichiers de design React
      complementaires ("Screens A" et "Screens B") apres FULLST-012/013.
      Screens A montre que la liste Promotions (FULLST-013, READY_FOR_REVIEW)
      doit etre une GRILLE DE CARTES (3 colonnes), pas un tableau : carte
      cliquable -> navigation /app/admin/promotions/:id, bandeau couleur
      filiere en haut, StatusPill (statut promotion), nom filiere, titre
      promotion, ligne cursus (icone book-open-check), periode (fmtRange),
      badge "{n} stagiaires", lien "Details ->". Barre de filtres au-dessus :
      Search (nom promo/cursus), FilterSelect filiere, FilterSelect annee.
      EmptyState si liste vide.
    - Scope A (refonte FULLST-013) :
      - frontend/src/app/features/administration/promotions/promotions.html/.ts/.scss
      - Remplacer le tableau par la grille de cartes ci-dessus.
      - Conserver : modal "Nouvelle promotion" (existant), modal de
        suppression (existant, deplacer son declencheur si necessaire --
        ex. bouton dans la promotion-detail ou un menu sur la carte ; a la
        discretion du developer, documenter le choix).
      - Filtres : recherche texte + filiere + annee, sur les modeles
        promotion/cursus/filiere existants (verifier que Filiere/couleur et
        Promotion.statut existent cote modele frontend ; sinon adapter au
        plus proche, documenter les ecarts).
      - Navigation carte -> /app/admin/promotions/:id (page FULLST-012).
    - Scope B (FULLST-016, demarrage) :
      - Page existante frontend/src/app/features/administration/cursus/
        (route /app/admin/cursus deja enregistree dans app.routes.ts) :
        etendre/refondre selon "Screens B" :
        - Groupes par filiere (nom, couleur, badge "{n} cursus", bouton
          edition filiere).
        - CursusCard : liste des cours du cursus en ordre pedagogique,
          drag&drop pour reordonner (persister l'ordre via l'API
          CursusCours existante, WI-20260610-BACKEN-007).
        - Bouton "Nouvelle filiere" -> NewFiliereModal (nom + palette de
          couleurs).
        - Bouton "Nouveau cursus" -> NewCursusModal : selection de cours
          (dropdown), liste ordonnee avec detection prerequis manquants
          (lignes fantomes "?" + bouton "Ajouter" qui insere automatiquement
          avant le cours qui les requiert) et mal ordonnes (bandeau +
          bouton "Corriger" qui repositionne), boutons monter/descendre/
          retirer par cours.
        - Reutilise Cours.prerequis (recursif, BACKEN-005, deja consomme
          par FULLST-012 pour le meme calcul d'aplatissement).
    - Conventions : standalone, signals, OnPush, @if/@for, class/style
      bindings (CLAUDE.md), pas de ngClass/ngStyle.
    - Verification : npx ng build PASS ; verification visuelle
      chrome-devtools si possible (sinon documenter).
    - A la fin : memoire role + WORK_ITEMS (FULLST-018 et FULLST-016 ->
      DONE/READY_FOR_REVIEW selon verification), Proposed Rules eventuelles.

## WI-20260611-FULLST-017
- Date: 2026-06-11
- Title: Backend - Endpoint POST creation CoursPlanifie pour une promotion
- Status: DONE
- TOA: explorer
- Executor: solution-architect, developer
- attempt_count: 1
- Implementation (developer, 2026-06-11): |
    - Backend : nouveau DTO PlanningCreateRequest (coursId, dateDebut, dateFin,
      formateurId?, salle?, force). Nouvelle methode PromotionService.createPlanning
      (Promotion + Cours lookup, ordre = max(ordre existant)+1, statut PLANIFIE,
      validation Role.FORMATEUR si formateurId fourni). Detection de conflit
      formateur extraite dans une methode privee partagee
      detecterConflitsFormateur (refactor pur depuis updatePlanning, reutilisee par
      createPlanning, aucun changement de comportement pour l'update). Nouvel
      endpoint POST /api/promotions/{id}/planning -> CoursPlanifieResponse
      (toCoursPlanifieResponse existant). SecurityConfig inchange (regle
      hasRole("REFERENTE_ADMINISTRATIVE") sur /api/promotions/** non-GET couvre deja
      la nouvelle route).
    - Frontend : nouveau type PlanningCreateRequest (promotion.model.ts), nouvelle
      methode createPlanning sur BasePromotionAdapter / HttpPromotionAdapter (POST
      /api/promotions/{id}/planning) / MockPromotionAdapter. promotion-detail.ts
      onPlanCourseSaved() bascule entre updatePlanning (mode edition, inchange) et
      createPlanning (mode creation, ajoute le PromotionCours retourne au planning
      local et ferme la modale). plan-course-modal.html : suppression du bandeau
      "endpoint a venir / enregistrement desactive" (obsolete) et du [disabled] sur
      le bouton submit ; libelle "Planifier" / "Enregistrer".
    - Tests : PromotionServiceTest (+coursRepository mock,
      createPlanning_promotionEtCoursExistants_creeUnCoursPlanifie,
      createPlanning_coursInexistant_lanceCoursNotFound) et
      PromotionControllerSecurityTest (+createPlanning_AvecRoleReferenteAdministrative_Retourne200,
      +createPlanning_AvecRoleEtudiant_Retourne403).
    - Verifications : ./gradlew test -> BUILD SUCCESSFUL (tout le suite, 0
      failure/error). npx ng build -> PASS (warnings SCSS preexistants non
      bloquants). chrome-devtools : backend rebuilde + redemarre sur :8080
      (ancien jar pre-datait ce WI). Promotion de test propre creee via curl
      (id=6 "TEST FULLST-017", cursusId=1, REF), modale "Planifier un cours" en
      mode creation (banniere obsolete absente), cours "Framework"
      02/11/2026-06/11/2026 -> apparait dans le planning (6/5 -> 7/5), promotion
      6 supprimee apres test (DELETE -> 204). Promotion 3 (corrompue, FULLST-019)
      non touchee.
    - Voir ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-017.md pour
      le detail complet.
- Notes: |
    - Origine : Risque #1 identifie dans
      ai_doc/ANALYSIS__WI-20260611-FULLST-012__gestion-promotion.md et
      confirme par le developer FULLST-012 -- aucun endpoint POST
      n'existe pour creer un nouveau CoursPlanifie au sein d'une
      promotion. La modale "Planifier un cours" (FULLST-012) est
      entierement implementee cote UI mais le bouton de soumission en
      mode creation est desactive ("Planifier (indisponible)") faute de
      cet endpoint.
    - Scope previsionnel : POST /api/promotions/{id}/planning (ou
      equivalent) acceptant coursId, dateDebut, dateFin, formateurId?,
      salle?, force? (bandeau prerequis non bloquant cote frontend) ;
      reutilise la detection de conflit formateur de FULLST-011 (warning
      non bloquant) ; reponse CoursPlanifieResponse (meme forme que pour
      l'update).
    - Une fois livree, le developer doit re-cabler le bouton "Planifier"
      de plan-course-modal (frontend/src/app/features/promotions/
      promotion-detail/plan-course-modal/) pour appeler ce nouvel
      endpoint.
    - Non prioritaire dans ce batch -- a confirmer avec l'utilisateur
      avant demarrage.
    - DEMARRAGE 2026-06-11 : confirme par l'utilisateur (point #5 du retour
      QA), a executer APRES FULLST-019 (meme fichiers PromotionController/
      PromotionService -- sequentiel pour eviter conflit).

## WI-20260611-FULLST-019
- Date: 2026-06-11
- Title: Backend - Fix 403 ajout/retrait eleve promotion + Frontend - Formulaire d'inscription (cloture FULLST-015)
- Status: DONE
- TOA: explorer
- Executor: developer
- attempt_count: 1
- Implementation (developer, 2026-06-11): |
    - Bug #1 : PAS un bug de code -- SecurityConfig.java avait deja (modifications non
      commitees presentes au debut de la session) les regles correctes pour
      /api/promotions/** (GET authenticated, autres methodes hasRole
      REFERENTE_ADMINISTRATIVE). Le 403 venait du process backend sur :8080 qui
      executait un jar compile AVANT ces regles (config committee = fallback
      anyRequest().authenticated()). Rebuild + redemarrage du process (PID 41028 tue,
      relance via ./gradlew bootRun --spring.profiles.active=local) -> POST/DELETE
      /api/promotions/3/eleves/13 (REF, JWT reel) -> 200. Confirme via UI.
    - Test de regression ajoute :
      backend/src/test/java/fr/eni/gestionformation/controller/PromotionControllerSecurityTest.java
      (@WebMvcTest + @Import(SecurityConfig.class), SecurityMockMvcRequestPostProcessors.user(...) --
      @WithMockUser non fiable dans ce slice, cf Proposed Rules de la note memoire).
      4 tests : addEleve/removeEleve REF -> 200, addEleve/removeEleve ETUDIANT -> 403.
    - Bug #2 : onOpenEnrollmentForm() implemente -- bascule vers l'onglet Stagiaires +
      ouvre la modale "Ajouter un eleve" existante (signal enrollmentTrigger en input
      sur StagiairesTabComponent + effect()). Reutilise entierement
      EntitySelectorComponent/addEleve() (FULLST-019 Bug #1).
    - ./gradlew test -> BUILD SUCCESSFUL ; npx ng build -> PASS (warnings SCSS
      preexistants non bloquants). Verification visuelle chrome-devtools : ajout +
      retrait "Eleve eleve" via onglet Stagiaires OK ; bouton "Inscrire un stagiaire"
      bascule sur Stagiaires et ouvre la modale fonctionnelle.
    - EFFETS DE BORD DONNEES (diagnostic) : promotion id=3 renommee "TEST CDA" (etait
      "x"), cursusId/dateDebut/rythme restent null (perdus par un PUT de diagnostic,
      non recuperables) -- a reconfigurer si besoin pour FULLST-017. Promotion de
      test id=5 supprimee.
    - FULLST-015 : seul le cas "ajouter un eleve a la promotion complete" est couvert
      (scope pragmatique demande). RadioCard Promotion/Cours a l'unite, controles
      ordre pedagogique 422, conflit 409 -- HORS SCOPE, voir statut FULLST-015 mis a
      jour ci-dessous et note memoire pour le detail du reste a faire.
    - Note complete : ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-019.md
- Notes: |
    - Origine : retour QA utilisateur (2026-06-11), points #1 et #2.
    - Point #1 (bug bloquant) : `POST /api/promotions/{id}/eleves/{eleveId}`
      (et probablement `DELETE` equivalent) renvoie 403 pour un utilisateur
      role REFERENTE_ADMINISTRATIVE (compte ref@ref.com / toto785971),
      alors que `POST /api/cursus` (meme hasRole en SecurityConfig) reussit
      pour le meme compte. Reproduit via chrome-devtools :
      reqid=866 POST http://localhost:4200/api/promotions/3/eleves/13 -> 403,
      corps de reponse vide (AccessDenied Spring Security par defaut, pas une
      exception metier -- GlobalExceptionHandler ne mappe rien en 403).
      A investiguer : SecurityConfig.java (ordre des requestMatchers
      /api/promotions/**), PromotionController.addEleve/removeEleve,
      PromotionService.addEleve/removeEleve. Ecrire un test d'integration
      (ex. CoursServiceTest pattern) couvrant ce cas avec role
      REFERENTE_ADMINISTRATIVE pour eviter une regression.
    - Point #2 : le bouton "Inscrire un stagiaire" (a cote de "Planifier un
      cours" dans promotion-detail.html) appelle un hook vide
      `onOpenEnrollmentForm()` (FULLST-012, Next Actions). Implementer le
      formulaire d'inscription -- cf. FULLST-015 (Frontend - Formulaire
      d'inscription (EnrollmentForm) avec controles ordre pedagogique 422 et
      conflit 409, design "EnrollmentForm"/"RadioCard" fourni par
      l'utilisateur dans Screens B). Brancher sur l'endpoint corrige du point
      #1. A la fin, ce WI cloture/merge FULLST-015 (mettre a jour son statut
      en consequence : DONE/MERGED dans FULLST-019).
    - Identifiants de test fournis par l'utilisateur :
      ADMIN admin@admin.com / Admin123 ; REF ref@ref.com / toto785971 ;
      ELEVE eleve@eleve.com / Eleve123.
    - Verification : test backend (./gradlew test) couvrant le fix 403 ;
      `npx ng build` PASS ; verification visuelle chrome-devtools sur
      /app/admin/promotions/3 (onglet Stagiaires : ajouter/retirer un eleve ;
      bouton "Inscrire un stagiaire" ouvre le formulaire et inscrit
      effectivement l'eleve, avec gestion 422/409 si applicable).
    - A executer EN PARALLELE de FULLST-020 (fichiers disjoints), AVANT
      FULLST-017 (meme fichiers PromotionController/PromotionService).

## WI-20260611-FULLST-020
- Date: 2026-06-11
- Title: Frontend+Backend - CRUD complet Cursus (renommer/changer filiere, supprimer, ajouter/retirer un cours sur une carte existante)
- Status: DONE
- TOA: explorer
- Executor: developer
- attempt_count: 1
- Implementation (developer, 2026-06-11): |
    - Backend : nouveau PUT /api/cursus/{id} (CursusController + CursusService.update,
      anti-doublon nom comme save(), filiereId null -> filiere effacee).
      CursusServiceTest cree (5 tests). ./gradlew test BUILD SUCCESSFUL.
    - Frontend : cursus.{ts,html,scss} - boutons Modifier/Supprimer par carte cursus,
      bouton Retirer par ligne de cours, select "+ Ajouter un cours" en pied de carte.
      Adapters (cursus.adapter/cursus-http/cursus-mock) : update()+delete() ajoutes.
      ng build PASS.
    - Verification chrome-devtools (ref@ref.com) : DELETE cours-de-cursus (200),
      DELETE cursus (204) confirmes, modal Modifier s'ouvre prerempli. Cleanup
      "Test Cursus QA" effectue.
    - BLOCKER LEVE (manager, 2026-06-11) : PUT /api/cursus/{id} -> 403 sur le
      backend local au moment de la livraison (process demarre avant ce
      changement, pattern deja documente en BACKEN-024). Backend redemarre par
      FULLST-019 ; revalide via chrome-devtools sur /app/admin/cursus
      (role REF) -- renommage "Concepteur developpeur d'application" -> "(test)"
      -> nom d'origine, PUT 200 dans les deux sens, aucun 403.
    - Amendement (manager, 2026-06-11) : sur demande utilisateur, le select
      "+ Ajouter un cours" en pied de carte cursus a ete remplace par un bouton
      ouvrant un modal listant les cours du catalogue avec un bouton "Ajouter"
      par cours (meme pattern que la modale "Nouveau cursus" / "Cours du
      catalogue"). Fichiers : cursus.html (nouveau bloc modal +
      addingCoursToCursus), cursus.ts (signal addingCoursToCursus,
      openAddCoursModal/closeAddCoursModal, addCoursToCursus met a jour le
      signal). ng build PASS. Revalide via chrome-devtools (ajout du cours
      "TEST" puis retrait, modal se met a jour dynamiquement).
    - Note complete : ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-020.md
- Notes: |
    - Origine : retour QA utilisateur (2026-06-11), points #3 et #4.
    - Constat (verifie via chrome-devtools, /app/admin/cursus, role REF) :
      la creation de cursus fonctionne (modal "Nouveau cursus" OK, j'ai cree
      un cursus de test "Test Cursus QA" / filiere Developpement / 1 cours
      "CSS Basique" pour verifier). Mais AUCUNE carte cursus n'a de bouton
      Modifier/Supprimer/Ajouter un cours -- seuls les boutons
      Monter/Descendre (drag&drop, FULLST-018) sont presents.
    - Backend : `DELETE /api/cursus/{id}` existe deja
      (CursusController.delete) -- juste a brancher cote frontend.
      `POST /api/cursus/{id}/cours` et `DELETE /api/cursus/{id}/cours/{coursId}`
      existent deja (ajout/retrait d'un cours) -- a brancher sur un bouton
      "+ Ajouter un cours" / "Retirer" par carte (cf. design CursusCard,
      Screens B, deja partiellement utilise par la modale "Nouveau cursus").
      MANQUANT : `PUT /api/cursus/{id}` pour renommer le cursus et/ou
      changer sa filiere -- a creer (CursusController + CursusService +
      CursusRequest existant a priori reutilisable). Verifier impact sur
      cursusService.save / validations existantes.
    - Frontend : `frontend/src/app/features/administration/cursus/
      cursus.{ts,html,scss}` -- ajouter par carte cursus : bouton
      "parametres"/"Modifier" (ouvre un modal nom + select filiere, reutilise
      le pattern de "Nouveau cursus"), bouton "Supprimer" (avec confirmation,
      reutiliser le pattern de confirmation deja existant pour
      filiere/promotion), bouton "+ Ajouter un cours" en pied de carte
      (dropdown cours du catalogue non encore presents) et bouton "Retirer"
      par ligne de cours.
    - Nettoyage : une fois le DELETE cursus branche, supprimer le cursus de
      test "Test Cursus QA" cree pendant la QA (filiere Developpement,
      1 cours CSS Basique).
    - Point #4 (page Cursus & Filieres "pas faite") : le lien fourni par
      l'utilisateur (api.anthropic.com/v1/design/...) n'est pas accessible
      (URL interne non recuperable par l'agent). La page existe et fonctionne
      deja largement (FULLST-018/BACKEN-007) -- ce WI couvre le manque
      fonctionnel reel identifie (CRUD cursus). Si un ecart visuel
      supplementaire est attendu, le documenter en Open Blocker pour un futur
      WI plutot que de bloquer celui-ci.
    - Verification : `cd backend && ./gradlew test` PASS (si endpoint PUT
      ajoute, test de service/controller correspondant) ; `cd frontend &&
      npx ng build` PASS ; verification visuelle chrome-devtools sur
      /app/admin/cursus (role ref@ref.com / toto785971) : modifier nom/filiere
      d'un cursus existant, supprimer "Test Cursus QA", ajouter/retirer un
      cours sur une carte.
    - A executer EN PARALLELE de FULLST-019 (fichiers disjoints : cursus.*
      vs promotions/eleves).

## WI-20260611-FULLST-021
- Date: 2026-06-11
- Title: Frontend - Refonte visuelle page Cursus & Filieres (maquette React fournie)
- Status: OPEN
- TOA: explorer
- Executor: developer
- attempt_count: 0
- Notes: |
    - Origine : maquette React fournie par l'utilisateur (composant Cursus,
      CursusCard, NewFiliereModal, NewCursusModal) -- le but est de rapprocher
      le visuel de frontend/src/app/features/administration/cursus/cursus.{ts,html,scss}
      de cette maquette, SANS perdre les fonctionnalites deja livrees
      (FULLST-007/016/018/020) : CRUD filiere, CRUD cursus, ajout/retrait
      cours via modal (cf. amendement FULLST-020), drag&drop ordre
      pedagogique, gestion des prerequis manquants/mal ordonnes (lignes
      fantomes, banniere d'avertissement, boutons "Corriger").
    - Decision utilisateur (2026-06-11) : la couleur d'identification par
      filiere est FRONTEND UNIQUEMENT (palette fixe deterministe basee sur
      l'id/index de la filiere), PAS de champ `couleur` en base -- ne pas
      toucher Filiere entity/DTO/migration backend.
    - Points de design a reprendre de la maquette :
      - Disposition par filiere : titre + pastille de couleur + badge "N
        cursus" + bouton "Modifier filiere" aligne a droite ; etat vide
        ("Aucun cursus dans cette filiere. Creer le premier cursus").
      - Grille de cartes cursus 2 colonnes (CursusCard) : header avec
        nom + "N cours - ordre pedagogique" + bouton parametres ; corps =
        liste des cours numerotes avec pastille coloree (couleur filiere),
        duree, poignee de drag (grip-vertical) ; bouton "+ Ajouter un cours"
        en pied de carte (style bouton pointille pleine largeur, pas un
        simple <select>/petit bouton -- cf. amendement FULLST-020 pour le
        modal associe, a restyler si besoin pour matcher ce pattern).
      - Modal "Nouvelle filiere" : champ nom + selecteur de couleur (palette
        fixe FILIERE_COLORS, frontend uniquement, stockee en local/derivee --
        voir comment persister le choix sans champ backend, ex. mapping
        deterministe id->couleur cote frontend si l'utilisateur ne peut pas
        choisir librement, OU stockage local (localStorage) si un choix
        libre est attendu : a trancher par le developer en fonction de la
        faisabilite, documenter le choix).
      - Modal "Nouveau cursus" : deja largement alignee (FULLST-018/020) --
        verifier l'alignement visuel avec builder/catalogue/banner
        prerequis de la maquette (anyMissing/anyLate banners, lignes
        fantomes "?", boutons "Corriger").
    - NE PAS regresser les correctifs recents (FULLST-019/020 : add/retrait
      eleve, PUT cursus, modal ajout cours).
    - Verification : `cd frontend && npx ng build` PASS ; verification
      visuelle chrome-devtools sur /app/admin/cursus (role ref@ref.com /
      toto785971) couvrant : creation filiere (couleur), creation cursus,
      modifier/supprimer cursus, ajouter/retirer cours (modal), drag&drop
      ordre, etat avec prerequis manquant/mal ordonne.

## WI-20260611-FULLST-022
- Date: 2026-06-11
- Title: Bug - Liste des cursus vide dans le dropdown de creation de promotion
- Status: DONE (corrige directement par le manager, pas de developer requis)
- Reporter: utilisateur, via systeme ("Lorsque je veux creer une promotion, la
  liste des cursus ne s'affiche pas dans le dropdown")

### Root Cause
La promotion id=3 ("TEST CDA", debris de test issu de FULLST-019, deja
documentee comme corrompue dans FULLST-019/017) avait `dateDebut: null`. Au
chargement de /app/admin/promotions, le template essaie de formatter cette
date via `fmtRange`/`fmtDate` ->
`new Intl.DateTimeFormat('fr-FR', ...).format(new Date("nullT00:00:00"))` ->
`RangeError: Invalid time value` (visible 3x dans la console). Cette erreur
levee pendant le rendu du template casse le cycle de detection de changement
de TOUT le composant Promotions, y compris la modale "Nouvelle promotion" :
le `<select id="p-cursus">` se rendait avec 0 `<option>` (en plus du
placeholder), bien que le signal `cursusList` soit correctement peuple par un
`GET /api/cursus` -> 200 reussi.

### Fix applique
- `PUT /api/promotions/3` (via fetch authentifie REF) pour reparer
  `cursusId: 1, dateDebut: "2026-01-05", rythme: {semainesCentre:4,
  semainesEntreprise:2}, eleveIds:[12]` -> 200, planning regenere
  automatiquement (6 entrees CoursPlanifie, dont le cours "TEST" id=7).
- Verification : reload /app/admin/promotions -> plus d'erreur console,
  `#p-cursus` affiche bien placeholder + 2 options (cursus id=1 et id=3).

### Cleanup (debris de tests anterieurs)
- Cursus id=3 "Concepteur developpeur d'application test" (doublon du cursus
  id=1) supprime via UI (/app/admin/cursus, "Supprimer ... test" ->
  "Supprimer definitivement") -> filiere "Developpement" repassee de 2 a 1
  cursus.
- Cours catalogue "TEST" (id=7) : prerequis croises avec "Angular Avance"
  (TEST -> prereq Angular Avance, cycle de debris) leve `CycleDetectedException`/
  blocage a la suppression -> nettoye via `PUT /api/cours/7/prerequis` body
  `[]` (200), puis `DELETE /api/cours/7`.
- Promotion id=3 ("TEST CDA", reparee ci-dessus) et promotion id=4
  ("TEST CDA 2", autre debris de test avec 2 eleves, planning vide) :
  `DELETE /api/promotions/3` -> 204 OK. `DELETE /api/promotions/4` -> **403
  reproductible** (voir Bug suivant), donc promotion 4 N'A PAS pu etre
  supprimee et reste presente dans /app/admin/promotions.

### Bug additionnel decouvert (NON corrige, hors scope FULLST-022)
`DELETE /api/promotions/{id}` retourne 403 (corps vide, headers Spring
Security standard) pour une promotion qui a (ou a eu) des eleves rattaches
(promotion id=4, role REFERENTE_ADMINISTRATIVE, meme token qui fonctionne
pour GET/POST/PUT/PUT-eleves/POST-eleves/DELETE-eleves sur la meme
promotion). Repro :
- `DELETE /api/promotions/7` (promo fraichement creee, 0 eleve, planning non
  vide) -> 204 OK.
- `DELETE /api/promotions/4` (2 eleves, ou meme 0 eleve apres retrait via
  `DELETE /api/promotions/4/eleves/{id}`) -> 403 a chaque tentative.
- Cause racine non identifiee (pas de `@PreAuthorize`, regle SecurityConfig
  identique pour tous les verbes non-GET sur `/api/promotions/**`,
  `out.log` du backend vide). A investiguer par un developer (nouveau WI a
  reserver si le besoin de "supprimer une promotion avec eleves" est
  confirme prioritaire par l'utilisateur).
- Impact utilisateur : aucun des points 1/2/3/5 du punch-list n'utilise
  `DELETE /api/promotions/{id}` avec eleves rattaches ; add/retrait eleve
  (`POST`/`DELETE /api/promotions/{id}/eleves/{eleveId}`) fonctionnent (200).
  La promotion "TEST CDA 2" (id=4) reste donc visible comme debris residuel
  dans /app/admin/promotions.

### Recall Hints
- Pitfall potentiel pour rules-curator : une `RangeError`/exception JS levee
  pendant le rendu du template d'une carte (ex: date invalide) peut casser
  le rendu de TOUT l'arbre de composants, y compris des modales/`@for`
  sans rapport avec la carte fautive, meme si les signals sous-jacents sont
  corrects. Toujours verifier `list_console_messages` avant de diagnostiquer
  un signal/donnee "vide".

## WI-20260611-FULLST-021 — Amendement (manager, 2026-06-11)
- Status final : DONE (developer + correctif manager).
- Le developer a livre la refonte visuelle (cf. note developer dediee) et a
  signale dans sa verification qu'un bouton "Supprimer filiere" preexistant
  (modal + handlers `openDeleteFiliereModal`/`confirmDeleteFiliere` deja
  presents dans `cursus.ts`) n'etait plus cable dans le nouveau header de
  filiere -- regression potentielle sur le point 3 du punch-list ("CRUD
  complet cursus/filiere").
- Correctif manager : ajout du bouton "Supprimer" (icone lucideTrash2, deja
  importee) a cote de "Modifier filiere" dans `cursus.html`
  (`.filiere-group__header`), regroupes dans un nouveau wrapper
  `.filiere-group__actions` (flex, gap .5rem) ajoute dans `cursus.scss`.
- Verification : `npx ng build` -> PASS (cursus.scss = 4.05 kB, depasse le
  budget de 4.00 kB de 48 octets -- nouveau warning, sans bloquer le build).
  chrome-devtools : clic sur "Supprimer Developpement" -> modale
  "Supprimer la filiere" s'ouvre correctement avec le bon nom, fermee sans
  confirmer (seule filiere reelle du jeu de donnees).

### Proposed Rules
- PITFALL : `cursus.scss` est maintenant au-dessus du budget SCSS par
  defaut (4 kB) de 48 octets. Si un futur correctif ajoute du style a ce
  fichier, soit factoriser via des classes partagees existantes, soit
  ajuster le budget dans `angular.json` pour ce composant -- a trier par
  rules-curator si recurrent.

## WI-20260611-FULLST-023
- Date: 2026-06-11
- Title: Restriction des routes/acces par role (API + app.routes.ts + sidebar)
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 1
- Scope:
    Matrice cible (validee par l'utilisateur) :
    - ADMINISTRATEUR (ADMIN) : Dashboard, Utilisateurs (/app/admin/utilisateurs)
    - FORMATEUR : Calendrier (/app/calendrier) + visibilite des inscrits sur ses cours planifies (deja couvert par /api/cours-planifies/*/inscrits)
    - ELEVE : Calendrier (/app/calendrier) uniquement
    - REFERENTE_ADMINISTRATIVE (REF) : Catalogue de cours, Cursus et filiere, Promotions (admin/*)

    1) Backend SecurityConfig.java :
       - GET /api/filiere/** : permitAll -> hasAnyRole(ADMINISTRATEUR, REFERENTE_ADMINISTRATIVE)
       - GET /api/cursus/** : authenticated() -> hasAnyRole(ADMINISTRATEUR, REFERENTE_ADMINISTRATIVE)
       - GET /api/cours/** : authenticated() -> hasAnyRole(ADMINISTRATEUR, REFERENTE_ADMINISTRATIVE)
       - GET /api/promotions/** : authenticated() -> hasAnyRole(ADMINISTRATEUR, REFERENTE_ADMINISTRATIVE)
       - GET /api/admin/users : hasAnyRole(ADMINISTRATEUR, REFERENTE_ADMINISTRATIVE) -> hasRole(ADMINISTRATEUR)

    2) Backend - correction IDOR sur GET /api/eleves/{id}/planning (InscriptionCoursController) :
       - Verification id du path == id utilisateur authentifie (sinon AccessDeniedException -> 403). Pattern : Authentication.getPrincipal() castable en User -> getUid().

    3) Frontend app.routes.ts :
       - /app/dashboard : pas de guard (route de repli)
       - /app/calendrier : roleGuard(['FORMATEUR','ELEVE'])
       - /app/promotions et /app/promotions/:id (top-level) : roleGuard(['REF'])

    4) Frontend sidebar.ts :
       - /app/dashboard : roles: ['ADMIN']
       - /app/calendrier : roles: ['FORMATEUR','ELEVE']
       - Suppression de l'entree top-level "/app/promotions" (roles ELEVE/FORMATEUR)

- Verification :
    - backend : ./gradlew compileJava -> succes
    - frontend : npx ng build -> "Application bundle generation complete." PASS (warnings SCSS preexistants non lies)
    - Verification chrome-devtools 4 roles non effectuee (optionnelle)
- Decisions utilisateur (Socratic Gate) :
    - Route de repli roleGuard : conserver /app/dashboard accessible a tous (pas de defaultRouteForRole)
    - GET cursus/cours/promotions restreints REF/ADMIN ; FORMATEUR garde uniquement /api/cours-planifies/*/inscrits
    - GET /api/filiere/** : retire de permitAll, aligne sur cursus/cours (REF/ADMIN)
    - IDOR /api/eleves/{id}/planning : corrige dans ce WI
- Proposed Rules: ACCEPTE par rules-curator -> ai_rules/pitfalls.md PIT-019
    (pattern Authentication.getPrincipal() instanceof User pour verifier la propriete d'une ressource)
- Memoire: ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-023.md

## WI-20260611-FULLST-024
- Date: 2026-06-11
- Title: Suppression robuste - cours planifie (icone poubelle), promotion avec eleves/sessions, cours du catalogue
- Status: OPEN
- TOA: manager
- Executor: developer
- attempt_count: 0
- Contexte: |
    Bug connu (cf REPO_STATE known_issues) : DELETE /api/promotions/{id} echoue (403/erreur)
    pour une promotion ayant des eleves/sessions planifiees. Cause probable : InscriptionCours
    a une FK obligatoire (nullable=false) vers CoursPlanifie sans cascade ; la suppression de
    CoursPlanifie (ou de Cours) echoue par violation de contrainte FK tant que des
    InscriptionCours y referencent.
    Idem pour DELETE /api/cours/{id} : CoursPlanifie.cours est nullable=false, donc tout
    cours deja planifie au moins une fois ne peut pas etre supprime du catalogue.
- Scope:
    1) Backend PromotionService.deleteById(id) :
       - Avant de supprimer les CoursPlanifie de la promotion, supprimer tous les
         InscriptionCours qui les referencent (InscriptionCoursRepository.findByCoursPlanifieId
         ou nouvelle methode findByCoursPlanifieIdIn).
    2) Backend - nouvel endpoint DELETE /api/promotions/{id}/planning/{coursPlanifieId} :
       - PromotionController + PromotionService.deletePlanning(promotionId, coursPlanifieId) :
         supprime les InscriptionCours rattachees a ce CoursPlanifie puis le CoursPlanifie
         lui-meme. Verifier que le CoursPlanifie appartient bien a la promotion (sinon 404).
       - Regle de securite : meme regle que les autres mutations /api/promotions/** ->
         hasRole(REFERENTE_ADMINISTRATIVE) (deja couvert par le matcher existant
         "/api/promotions/**").
    3) Backend CoursService.deleteById(id) :
       - Avant coursRepository.deleteById(id), trouver tous les CoursPlanifie dont
         cours.id == id (CoursPlanifieRepository - ajouter findByCoursId si absent),
         supprimer les InscriptionCours qui les referencent puis ces CoursPlanifie.
       - Verifier aussi la gestion des prerequis ManyToMany (cours_prerequis) : s'assurer
         que les references au cours supprime sont nettoyees des deux cotes (prerequis et
         cours-dependants) avant suppression, pour eviter une violation de contrainte sur
         la table de jointure.
    4) Frontend - icone poubelle "retirer un cours planifie" :
       - Dans frontend/src/app/features/promotions/promotion-detail/ (liste "Cours
         planifies"), ajouter un bouton/icone (lucide trash) par ligne de planning, avec
         confirmation (reutiliser le pattern de modal de confirmation existant dans ce
         module si present), appelant le nouvel endpoint DELETE planning.
       - Adapters : ajouter deletePlanning(promotionId, coursPlanifieId) dans
         core/adapters/promotion.adapter.ts (abstrait), promotion-http.adapter.ts (appel
         HTTP DELETE) et promotion-mock.ts (suppression locale).
    5) Re-tester la suppression de la promotion id=4 ("TEST CDA 2", debris connu avec
       eleves/sessions) -> doit desormais retourner 204 et disparaitre de la liste.
- Verification requise :
    - Backend : ./gradlew test (si rapide) + verification manuelle : creer une promotion
      de test avec un cours planifie et un eleve inscrit, verifier DELETE
      /api/promotions/{id}/planning/{coursPlanifieId} (204, eleve desinscrit), puis DELETE
      /api/promotions/{id} (204) avec eleves+sessions restants.
    - Backend : verifier DELETE /api/cours/{id} sur un cours deja planifie au moins une fois.
    - Frontend : npx ng build PASS + verification chrome-devtools (icone poubelle visible
      et fonctionnelle dans promotion-detail, confirmation avant suppression).
    - Nettoyer la promotion id=4 ("TEST CDA 2") si toujours presente (known_issue).

## WI-20260611-FULLST-024 — Cloture (manager, 2026-06-11)
- Status: DONE
- Verification :
    - backend : ./gradlew compileJava + ./gradlew test -> BUILD SUCCESSFUL
    - frontend : npx ng build -> PASS
    - chrome-devtools : icone poubelle "retirer un cours planifie" (cours-planifies-tab) OK + modal confirmation
    - Tests manuels API : delete planning (avec inscriptions) -> 204 ; delete promotion avec eleves+sessions -> 204 ;
      delete cours deja planifie (avec prerequis) -> 204
- Decouverte additionnelle (hors scope initial mais resolue) :
    - La cause reelle du 403 connu sur DELETE /api/promotions/4 (FULLST-022) etait une table
      orpheline `promotion_cours` (residu PIT-010) avec 5 lignes encore liees par FK active a
      promotion(id=4). Lignes supprimees manuellement, puis DELETE /api/promotions/4 -> 204.
      Promotion id=4 ("TEST CDA 2") definitivement supprimee. known_issue clos.
    - Risque residuel : la table `promotion_cours` peut contenir des lignes orphelines pour
      d'autres promotions (non auditees). Voir suivi WI-20260611-FULLST-025.
- Proposed Rules : ACCEPTE par rules-curator -> ai_rules/pitfalls.md PIT-020, ai_rules/conventions.md CONV-007
- Memoire : ai_memory/2026-06-11__ROLE-developer__WI-20260611-FULLST-024.md,
  ai_memory/2026-06-11__ROLE-rules-curator__WI-20260611-FULLST-024.md

## WI-20260611-FULLST-025
- Date: 2026-06-11
- Title: Audit/nettoyage table orpheline `promotion_cours` (residu PIT-010) sur toutes promotions
- Status: OPEN
- TOA: manager
- Executor: developer
- attempt_count: 0
- Contexte: |
    WI-20260611-FULLST-024 a decouvert que la table `promotion_cours` (ancienne entite,
    renommee en CoursPlanifie selon PIT-010) existe toujours en base avec une FK active vers
    `promotion(id)`, et contenait 5 lignes orphelines bloquant DELETE /api/promotions/4 par
    violation de contrainte (surfacee en 403 vide, voir PIT-020). Ces lignes ont ete
    supprimees manuellement pour la promotion 4 uniquement.
- Scope:
    - Auditer la table `promotion_cours` pour des lignes residuelles referencant d'autres
      promotions existantes.
    - Si la table n'est plus utilisee par aucune entite JPA active, ecrire une migration
      (Flyway/Liquibase si en place, sinon script SQL documente) pour la supprimer
      proprement (DROP TABLE) plutot que de laisser ddl-auto=update la conserver.
    - Documenter le resultat dans ai_memory et clore PIT-020/PIT-010 si plus pertinent.
- Verification requise : DELETE /api/promotions/{id} sur chaque promotion existante -> 204 sans erreur FK.

---

- WI: WI-20260611-FULLST-027
- Date: 2026-06-11
- Title: Page detail cursus (route admin/cursus/:id) avec alertes de desordre pedagogique
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 0
- Contexte: |
    La page Cursus (frontend/src/app/features/administration/cursus) liste actuellement
    chaque cursus avec sa liste ordonnee de cours (drag&drop, monter/descendre,
    ajouter/retirer un cours) directement sur la carte, ce qui la rend dense. L'utilisateur
    souhaite extraire cela dans une page de detail par cursus, qui affichera aussi des
    alertes "prerequis mal ordonne" (logique deja existante dans la modale de creation via
    misorderedPrereqs/hasMisorderedPrereqs/fixOrder sur BuilderRow, a adapter pour
    cours: CoursInCursus[] reels en croisant avec le catalogue pour recuperer prerequis).
- Scope:
    - Nouvelle route `admin/cursus/:id` (roleGuard ['REF']), nouveau composant standalone
      `cursus-detail` (frontend/src/app/features/administration/cursus-detail/).
    - Reprendre sur cette page : liste ordonnee (drag&drop, monter/descendre),
      ajouter/retirer un cours (modale existante adaptee), affichage formateurs.
    - Ajouter un bloc d'alertes "prerequis mal ordonne" avec action "Corriger"
      (reutilise le pattern fixOrder).
    - Extraire la logique de calcul des alertes dans une fonction pure partagee
      (ex. frontend/src/app/core/utils/cursus-alerts.util.ts ou similaire) reutilisable
      par WI-028.
- Verification requise : `ng build` PASS, test manuel (chrome-devtools ou description) de
  la navigation, du drag&drop, et de l'affichage/correction d'une alerte.

---

- WI: WI-20260611-FULLST-028
- Date: 2026-06-11
- Title: Simplification cartes cursus (liste) - resume + badge alerte + navigation vers detail
- Status: DONE
- TOA: manager
- Executor: developer
- attempt_count: 0
- Contexte: |
    Suite a WI-20260611-FULLST-027 (page detail cursus + fonction partagee de calcul des
    alertes), la page liste (frontend/src/app/features/administration/cursus) doit etre
    simplifiee : chaque carte cursus devient un resume (nom, filiere, nombre de cours,
    badge "N alerte(s)" si desordre pedagogique detecte via la fonction partagee).
- Scope:
    - Supprimer de la carte liste : liste ordonnee/drag&drop, "Ajouter un cours".
    - Carte entiere cliquable -> navigue vers admin/cursus/:id (router.navigate).
      Modifier/Supprimer restent des boutons avec (click)="$event.stopPropagation()".
    - Badge "N alerte(s)" calcule via la fonction partagee de WI-027.
    - Conserver inchangee la gestion des filieres (modales Nouvelle/Modifier/Supprimer
      filiere) et la modale "Nouveau cursus" (builder).
- Depends on: WI-20260611-FULLST-027
- Verification requise : `ng build` PASS, test manuel de la navigation depuis une carte et
  de l'affichage des badges.

---

- WI: WI-20260611-FULLST-029
- Date: 2026-06-11
- Title: Correction de l'ordre pedagogique du cursus DWWM (data fix via Corriger)
- Status: DONE (rien a corriger - cause = donnees catalogue, voir note)
- TOA: manager
- Executor: developer
- attempt_count: 0
- Contexte: |
    L'utilisateur a colle la liste actuelle des cours du cursus "DWWM" (un module
    "Programmation Orientee Objet / Java" apparait deux fois, "Developpement Web
    cote Serveur (Back-End) / Java Spring Boot" trois fois, "Projet Web / Symfony"
    deux fois, etc. -- noms de cours du catalogue qui partagent un libelle proche
    pour des modules differents). Suite a WI-20260611-FULLST-027/028, la page
    /app/admin/cursus/:id pour ce cursus doit afficher des alertes "prerequis mal
    ordonne" avec un bouton "Corriger". L'utilisateur demande de corriger l'ordre.
- Scope:
    - Lancer l'app en local (docker compose ou local_backend + frontend ng serve).
    - Identifier le cursus "DWWM" via /app/admin/cursus puis ouvrir sa page detail
      /app/admin/cursus/:id.
    - Pour chaque alerte "prerequis mal ordonne" affichee, cliquer "Corriger" et
      verifier que la liste se reordonne (PUT/PATCH reorder via cursusAdapter).
    - Repeter jusqu'a disparition de toutes les alertes (ou jusqu'a ce qu'il ne
      reste que des alertes "prerequis absent du cursus" non corrigeables par ce
      bouton -- documenter ces cas residuels separement).
    - Si le bouton "Corriger" ne resout pas correctement un cas (ex: cycle de
      dependances, alerte qui revient), documenter precisement le cas en BLOCKED
      avec capture/etat avant de tenter un fix manuel cote donnees.
- Verification requise : capture (chrome-devtools ou description precise) de la
  page detail DWWM avant/apres montrant la disparition des alertes corrigees.

---

- WI: WI-20260611-FULLST-031
- Date: 2026-06-11
- Title: Catalogue cours - repointer les prerequis "Algorithmique + Initiation a la Programmation / Java" vers les deux cours splittes
- Status: DONE (option 3 appliquee : prerequis du cours id 10 "Web Client / HTML & CSS" vide ; DWWM 0 alerte, CDA 0 alerte, persistance verifiee ; PIT-023/PIT-024 ajoutees)
- TOA: manager
- Executor: developer
- attempt_count: 0
- Contexte: |
    Suite a WI-20260611-FULLST-029 : sur le cursus DWWM (id 5), 14 alertes "prerequis
    mal ordonne / absent du cursus" referencent toutes le meme cours catalogue
    "Algorithmique + Initiation à la Programmation / Java" comme prerequis. Ce cours
    n'est pas membre du cursus DWWM, qui contient a la place deux cours separes
    ("Algorithmique / Pseudo-Code" et "Initiation à la Programmation / Java", positions
    0 et 1). L'utilisateur a choisi l'option (b) : repointer les `prerequis` des cours
    catalogue concernes vers ces deux cours deja presents, plutot que d'ajouter le cours
    fusionne au cursus.
- Scope:
    - Identifier via l'API/DB les ids : cours fusionne "Algorithmique + Initiation a la
      Programmation / Java", et les deux cours cibles "Algorithmique / Pseudo-Code" et
      "Initiation à la Programmation / Java".
    - Lister tous les cours catalogue ayant le cours fusionne dans leurs `prerequis`
      (les 14 alertes du DWWM en donnent une partie ; verifier aussi via le catalogue
      complet car d'autres cursus pourraient etre impactes - blast radius global au
      catalogue, pas seulement DWWM).
    - Pour chacun de ces cours, retirer le prerequis fusionne et ajouter les deux
      prerequis cibles (via PUT /api/cours/{id} avec prerequisIds mis a jour, en
      respectant la validation anti-cycle deja en place cote backend).
    - Decider du sort du cours catalogue fusionne lui-meme (orphelin si plus reference
      par aucun prerequis et n'appartient a aucun cursus) : NE PAS le supprimer dans ce
      WI sans confirmation explicite -- documenter juste son etat residuel.
    - Re-verifier la page /app/admin/cursus/5 (DWWM) : les 14 alertes doivent disparaitre
      (ou se transformer en alertes "mal ordonne" corrigibles si l'ordre relatif des
      deux nouveaux prerequis n'est pas respecte -- dans ce cas, utiliser le bouton
      "Corriger").
    - Verifier qu'aucun AUTRE cursus n'a ete casse par ce changement (recharger
      /app/admin/cursus et observer les badges d'alerte globaux avant/apres).
- Verification requise : capture avant/apres des alertes sur DWWM (id 5) et sur la page
  liste /app/admin/cursus (badges), confirmation persistance apres rechargement.
