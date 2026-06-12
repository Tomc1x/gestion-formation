import { Component, ChangeDetectionStrategy, OnInit, inject, signal, computed } from '@angular/core';
import { Router } from '@angular/router';
import {
  LucideLayers,
  LucideUsers,
  LucideCalendarClock,
  LucideBookOpenCheck,
  LucideCalendarDays,
  LucideUser,
  LucideArrowRight,
  LucideChevronRight,
  LucideInfo,
  LucideCalendarCheck,
  LucideClock,
  LucideMapPin,
  LucideUserCog,
  LucideUserCheck,
  LucideUserX,
  LucideGraduationCap,
} from '@lucide/angular';
import { AuthService } from '../../core/services/auth.service';
import { BasePromotionAdapter } from '../../core/adapters/promotion.adapter';
import { BaseCursusAdapter } from '../../core/adapters/cursus.adapter';
import { BaseEleveAdapter } from '../../core/adapters/eleve.adapter';
import { BaseCalendarAdapter } from '../../core/adapters/calendar.adapter';
import { BaseUserAdminAdapter } from '../../core/adapters/user-admin.adapter';
import { Promotion, PromotionCours } from '../../core/models/promotion.model';
import { Cursus } from '../../core/models/cursus.model';
import { CalendarEvent } from '../../core/models/calendar-event.model';
import { BackendRole, BACKEND_ROLE_OPTIONS, UserAdmin, FrontendRole } from '../../core/models/user.model';

interface FeedItem {
  planning: PromotionCours;
  promotion: Promotion;
}

@Component({
  selector: 'app-dashboard',
  imports: [
    LucideLayers,
    LucideUsers,
    LucideCalendarClock,
    LucideBookOpenCheck,
    LucideCalendarDays,
    LucideUser,
    LucideArrowRight,
    LucideChevronRight,
    LucideInfo,
    LucideCalendarCheck,
    LucideClock,
    LucideMapPin,
    LucideUserCog,
    LucideUserCheck,
    LucideUserX,
    LucideGraduationCap,
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly promotionAdapter = inject(BasePromotionAdapter);
  private readonly cursusAdapter = inject(BaseCursusAdapter);
  private readonly eleveAdapter = inject(BaseEleveAdapter);
  private readonly calendarAdapter = inject(BaseCalendarAdapter);
  private readonly userAdminAdapter = inject(BaseUserAdminAdapter);

  protected readonly role: FrontendRole = this.auth.currentRole();

  protected readonly loading = signal(true);

  protected readonly today = new Date().toISOString().slice(0, 10);

  protected readonly subtitle = computed(() => {
    switch (this.role) {
      case 'ADMIN':
        return 'Vue d\'ensemble des comptes utilisateurs.';
      case 'FORMATEUR':
        return 'Vue d\'ensemble de votre activité pédagogique.';
      case 'ELEVE':
        return 'Vue d\'ensemble de votre emploi du temps.';
      default:
        return 'Vue d\'ensemble de l\'activité pédagogique du centre.';
    }
  });

  // ── Données REF ──────────────────────────────────────────────────────────────

  protected readonly promotions = signal<Promotion[]>([]);
  protected readonly cursusList = signal<Cursus[]>([]);
  protected readonly elevesCount = signal(0);

  protected readonly promotionsActives = computed(() =>
    this.promotions().filter(p => this.statutEnCours(p)).length
  );

  protected readonly coursCetteSemaine = computed(() => {
    const { start, end } = this.weekRange();
    return this.allPlanning().filter(item =>
      item.planning.dateDebut >= start && item.planning.dateDebut <= end
    ).length;
  });

  protected readonly elevesSansPromotion = computed(() => {
    const inscrits = new Set<number>();
    for (const p of this.promotions()) {
      for (const e of p.eleves) inscrits.add(e.id);
    }
    return Math.max(this.elevesCount() - inscrits.size, 0);
  });

  protected readonly prochainsCours = computed<FeedItem[]>(() => {
    const today = this.today;
    return this.allPlanning()
      .filter(item => item.planning.dateDebut >= today)
      .sort((a, b) => a.planning.dateDebut.localeCompare(b.planning.dateDebut))
      .slice(0, 5);
  });

  private allPlanning(): FeedItem[] {
    return this.promotions().flatMap(promotion =>
      promotion.planning.map(planning => ({ planning, promotion }))
    );
  }

  private statutEnCours(promotion: Promotion): boolean {
    const today = this.today;
    if (promotion.dateDebut > today) return false;
    const dateFin = promotion.planning.reduce<string | null>(
      (max, c) => (!max || c.dateFin > max ? c.dateFin : max),
      null
    );
    return !dateFin || dateFin >= today;
  }

  private weekRange(): { start: string; end: string } {
    const { start, end } = this.weekRangeDates();
    return { start: start.toISOString().slice(0, 10), end: end.toISOString().slice(0, 10) };
  }

  // ── Données FORMATEUR / ELEVE ────────────────────────────────────────────────

  protected readonly calendarEvents = signal<CalendarEvent[]>([]);

  protected readonly coursAujourdHui = computed(() => {
    const now = new Date();
    return this.calendarEvents().filter(e => this.isSameDay(new Date(e.startDate), now)).length;
  });

  protected readonly coursCetteSemaineCal = computed(() => {
    const { start, end } = this.weekRangeDates();
    return this.calendarEvents().filter(e => {
      const d = new Date(e.startDate);
      return d >= start && d <= end;
    }).length;
  });

  protected readonly coursCeMois = computed(() => this.calendarEvents().length);

  protected readonly prochainsEvents = computed(() => {
    const now = new Date();
    return [...this.calendarEvents()]
      .filter(e => new Date(e.startDate) >= now)
      .sort((a, b) => new Date(a.startDate).getTime() - new Date(b.startDate).getTime())
      .slice(0, 5);
  });

  protected readonly prochainEvent = computed(() => this.prochainsEvents()[0] ?? null);

  protected readonly prochainEventInscritsLink = computed(() => {
    const event = this.prochainEvent();
    return event ? `/app/cours-planifies/${event.id}/inscrits` : null;
  });

  protected readonly elevesAEncadrerSemaine = computed(() => {
    const { start, end } = this.weekRangeDates();
    const ids = new Set<number>();
    for (const e of this.calendarEvents()) {
      const d = new Date(e.startDate);
      if (d >= start && d <= end) {
        for (const el of e.eleves ?? []) ids.add(el.eleveId);
      }
    }
    return ids.size;
  });

  protected readonly formateursSemaine = computed(() => {
    const { start, end } = this.weekRangeDates();
    const noms = new Set<string>();
    for (const e of this.calendarEvents()) {
      const d = new Date(e.startDate);
      if (d >= start && d <= end && e.formateurNom) noms.add(e.formateurNom);
    }
    return noms.size;
  });

  private weekRangeDates(): { start: Date; end: Date } {
    return this.calendarAdapter.getDateRange('week', new Date());
  }

  private isSameDay(a: Date, b: Date): boolean {
    return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();
  }

  // ── Données ADMIN ────────────────────────────────────────────────────────────

  protected readonly users = signal<UserAdmin[]>([]);

  protected readonly totalUsers = computed(() => this.users().length);
  protected readonly totalFormateurs = computed(() => this.users().filter(u => u.role === 'FORMATEUR').length);
  protected readonly totalEleves = computed(() => this.users().filter(u => u.role === 'ETUDIANT').length);
  protected readonly comptesDesactives = computed(() => this.users().filter(u => !u.enabled).length);

  protected readonly derniersUtilisateurs = computed(() =>
    [...this.users()].sort((a, b) => b.uid - a.uid).slice(0, 5)
  );

  protected roleLabel(role: BackendRole): string {
    return BACKEND_ROLE_OPTIONS.find(o => o.value === role)?.label ?? role;
  }

  // ── Chargement ───────────────────────────────────────────────────────────────

  ngOnInit(): void {
    switch (this.role) {
      case 'REF':
        this.loadRef();
        break;
      case 'ADMIN':
        this.loadAdmin();
        break;
      case 'FORMATEUR':
      case 'ELEVE':
        this.loadCalendar();
        break;
    }
  }

  private loadRef(): void {
    this.loading.set(true);

    this.promotionAdapter.getAll().subscribe({
      next: promotions => {
        this.promotions.set(promotions);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });

    this.cursusAdapter.getAll().subscribe({
      next: cursus => this.cursusList.set(cursus),
    });

    this.eleveAdapter.getAll().subscribe({
      next: eleves => this.elevesCount.set(eleves.length),
    });
  }

  private loadCalendar(): void {
    this.loading.set(true);
    this.calendarAdapter.getEvents('month', new Date()).subscribe({
      next: events => {
        this.calendarEvents.set(events);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  private loadAdmin(): void {
    this.loading.set(true);
    this.userAdminAdapter.getAll().subscribe({
      next: users => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  // ── Formatage ────────────────────────────────────────────────────────────────

  protected fmtDay(iso: string): string {
    return new Intl.DateTimeFormat('fr-FR', { day: '2-digit' }).format(new Date(iso + 'T00:00:00'));
  }

  protected fmtMonth(iso: string): string {
    return new Intl.DateTimeFormat('fr-FR', { month: 'short' }).format(new Date(iso + 'T00:00:00'));
  }

  protected fmtDayD(date: Date): string {
    return new Intl.DateTimeFormat('fr-FR', { day: '2-digit' }).format(new Date(date));
  }

  protected fmtMonthD(date: Date): string {
    return new Intl.DateTimeFormat('fr-FR', { month: 'short' }).format(new Date(date));
  }

  protected fmtTime(date: Date): string {
    return new Intl.DateTimeFormat('fr-FR', { hour: '2-digit', minute: '2-digit' }).format(new Date(date));
  }

  protected goTo(path: string): void {
    this.router.navigate([path]);
  }
}
