import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import {
  LucideUsers,
  LucidePlus,
  LucideTrash2,
  LucideX,
  LucideSearch,
  LucideGitBranch,
  LucideCalendar,
  LucideBookOpenCheck,
  LucideCalendarRange,
  LucideArrowRight,
  LucideLayers,
} from '@lucide/angular';
import { BasePromotionAdapter } from '../../../core/adapters/promotion.adapter';
import { BaseCursusAdapter } from '../../../core/adapters/cursus.adapter';
import { BaseFiliereAdapter } from '../../../core/adapters/filiere.adapter';
import { Promotion, PromotionRequest } from '../../../core/models/promotion.model';
import { Cursus, Filiere } from '../../../core/models/cursus.model';

/** Couleurs déterministes par filière (purement visuel, non persisté côté backend). */
const FILIERE_COLORS = [
  '#2563EB', '#7C3AED', '#DB2777', '#D97706',
  '#059669', '#0891B2', '#DC2626', '#4F46E5',
];

/** Statut dérivé d'une promotion (pas de champ `statut` en base — calculé depuis les dates). */
type PromotionStatut = 'a-venir' | 'en-cours' | 'terminee';

interface PromotionCard {
  promotion: Promotion;
  cursus: Cursus | null;
  filiere: Filiere | null;
  filiereColor: string;
  statut: PromotionStatut;
  dateFin: string | null;
}

type PromotionFormGroup = FormGroup<{
  name: FormControl<string>;
  cursusId: FormControl<number | null>;
  dateDebut: FormControl<string>;
  rythmeActif: FormControl<boolean>;
  semainesCentre: FormControl<number | null>;
  semainesEntreprise: FormControl<number | null>;
}>;

@Component({
  selector: 'app-promotions',
  imports: [
    ReactiveFormsModule,
    LucideUsers,
    LucidePlus,
    LucideTrash2,
    LucideX,
    LucideSearch,
    LucideGitBranch,
    LucideCalendar,
    LucideBookOpenCheck,
    LucideCalendarRange,
    LucideArrowRight,
    LucideLayers,
  ],
  templateUrl: './promotions.html',
  styleUrl: './promotions.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PromotionsComponent implements OnInit {
  private readonly promotionAdapter = inject(BasePromotionAdapter);
  private readonly cursusAdapter = inject(BaseCursusAdapter);
  private readonly filiereAdapter = inject(BaseFiliereAdapter);
  private readonly router = inject(Router);

  protected readonly promotions = signal<Promotion[]>([]);
  protected readonly cursusList = signal<Cursus[]>([]);
  protected readonly filieres = signal<Filiere[]>([]);
  protected readonly loading = signal(true);
  protected readonly loadError = signal<string | null>(null);

  protected readonly submitting = signal(false);
  protected readonly formError = signal<string | null>(null);

  // ── Filtres ────────────────────────────────────────────────────────────────────
  protected readonly searchQuery = signal('');
  protected readonly filiereFilter = signal<string>('all');
  protected readonly yearFilter = signal<string>('all');

  /** Années disponibles (déduites des dates de début des promotions), triées décroissant. */
  protected readonly availableYears = computed<string[]>(() => {
    const years = new Set<string>();
    for (const p of this.promotions()) {
      const year = p.dateDebut?.slice(0, 4);
      if (year) years.add(year);
    }
    return [...years].sort((a, b) => b.localeCompare(a));
  });

  protected readonly cards = computed<PromotionCard[]>(() => {
    const cursusList = this.cursusList();
    const filieres = this.filieres();

    return this.promotions().map(promotion => {
      const cursus = cursusList.find(c => c.id === promotion.cursusId) ?? null;
      const filiere = cursus?.filiereId != null
        ? filieres.find(f => f.id === cursus.filiereId) ?? null
        : null;
      const filiereColor = filiere ? FILIERE_COLORS[filiere.id % FILIERE_COLORS.length] : 'var(--ink-3)';

      const dateFin = promotion.planning.length > 0
        ? promotion.planning.reduce((max, c) => (c.dateFin > max ? c.dateFin : max), promotion.planning[0].dateFin)
        : null;

      return { promotion, cursus, filiere, filiereColor, statut: this.computeStatut(promotion, dateFin), dateFin };
    });
  });

  protected readonly filteredCards = computed<PromotionCard[]>(() => {
    const q = this.searchQuery().trim().toLowerCase();
    const fil = this.filiereFilter();
    const year = this.yearFilter();

    return this.cards().filter(card => {
      if (fil !== 'all' && card.filiere?.id !== Number(fil)) return false;
      if (year !== 'all' && !card.promotion.dateDebut.startsWith(year)) return false;
      if (q) {
        const inName = card.promotion.name.toLowerCase().includes(q);
        const inCursus = (card.promotion.cursusNom ?? '').toLowerCase().includes(q);
        if (!inName && !inCursus) return false;
      }
      return true;
    });
  });

  /** Statut dérivé : pas de champ `statut` côté backend, calculé depuis dateDebut / dernière session planifiée. */
  private computeStatut(promotion: Promotion, dateFin: string | null): PromotionStatut {
    const today = new Date().toISOString().slice(0, 10);
    if (promotion.dateDebut > today) return 'a-venir';
    if (dateFin && dateFin < today) return 'terminee';
    return 'en-cours';
  }

  protected statutLabel(statut: PromotionStatut): string {
    switch (statut) {
      case 'a-venir': return 'À venir';
      case 'en-cours': return 'En cours';
      case 'terminee': return 'Terminée';
    }
  }

  protected fmtRange(debut: string, fin: string | null): string {
    const start = this.fmtDate(debut, { day: '2-digit', month: 'short' });
    if (!fin) return start;
    return `${start} → ${this.fmtDate(fin, { day: '2-digit', month: 'short', year: 'numeric' })}`;
  }

  private fmtDate(iso: string, options: Intl.DateTimeFormatOptions): string {
    const date = new Date(iso + 'T00:00:00');
    return new Intl.DateTimeFormat('fr-FR', options).format(date);
  }

  ngOnInit(): void {
    this.loadAll();
  }

  private loadAll(): void {
    this.loading.set(true);
    this.loadError.set(null);

    this.promotionAdapter.getAll().subscribe({
      next: promotions => {
        this.promotions.set(promotions);
        this.loading.set(false);
      },
      error: () => {
        this.loadError.set('Impossible de charger les promotions.');
        this.loading.set(false);
      },
    });

    this.cursusAdapter.getAll().subscribe({
      next: cursus => this.cursusList.set(cursus),
    });

    this.filiereAdapter.getAll().subscribe({
      next: filieres => this.filieres.set(filieres),
    });
  }

  protected openPromotion(promotion: Promotion): void {
    this.router.navigate(['/app/admin/promotions', promotion.id]);
  }

  private extractError(err: unknown, fallback: string): string {
    const httpErr = err as { status?: number; error?: string };
    if ((httpErr.status === 409 || httpErr.status === 422) && typeof httpErr.error === 'string' && httpErr.error) {
      return httpErr.error;
    }
    return fallback;
  }

  // ── Formulaire commun (création / édition) ───────────────────────────────────
  private buildForm(): PromotionFormGroup {
    return new FormGroup({
      name: new FormControl('', { nonNullable: true, validators: Validators.required }),
      cursusId: new FormControl<number | null>(null, { validators: Validators.required }),
      dateDebut: new FormControl('', { nonNullable: true, validators: Validators.required }),
      rythmeActif: new FormControl(false, { nonNullable: true }),
      semainesCentre: new FormControl<number | null>(null),
      semainesEntreprise: new FormControl<number | null>(null),
    });
  }

  private buildRequest(form: PromotionFormGroup): PromotionRequest {
    const v = form.getRawValue();

    return {
      name: v.name,
      cursusId: v.cursusId,
      dateDebut: v.dateDebut,
      rythme: v.rythmeActif
        ? { semainesCentre: v.semainesCentre ?? 0, semainesEntreprise: v.semainesEntreprise ?? 0 }
        : null,
    };
  }

  // ── Modale : Nouvelle promotion ───────────────────────────────────────────────
  protected readonly showCreateModal = signal(false);
  protected readonly createForm = this.buildForm();

  protected openCreateModal(): void {
    this.formError.set(null);
    this.createForm.reset({
      name: '',
      cursusId: null,
      dateDebut: '',
      rythmeActif: false,
      semainesCentre: null,
      semainesEntreprise: null,
    });
    this.showCreateModal.set(true);
  }

  protected closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  protected submitCreate(): void {
    if (this.createForm.invalid || this.submitting()) return;

    this.submitting.set(true);
    this.formError.set(null);

    this.promotionAdapter.create(this.buildRequest(this.createForm)).subscribe({
      next: created => {
        this.promotions.update(list => [...list, created]);
        this.submitting.set(false);
        this.closeCreateModal();
      },
      error: err => {
        this.formError.set(this.extractError(err, 'Une erreur est survenue lors de la création de la promotion.'));
        this.submitting.set(false);
      },
    });
  }

  // ── Modale : Supprimer une promotion ──────────────────────────────────────────
  protected readonly deletingPromotion = signal<Promotion | null>(null);

  protected openDeleteModal(promotion: Promotion): void {
    this.formError.set(null);
    this.deletingPromotion.set(promotion);
  }

  protected closeDeleteModal(): void {
    this.deletingPromotion.set(null);
  }

  protected confirmDelete(): void {
    const promotion = this.deletingPromotion();
    if (!promotion || this.submitting()) return;

    this.submitting.set(true);
    this.formError.set(null);

    this.promotionAdapter.delete(promotion.id).subscribe({
      next: () => {
        this.promotions.update(list => list.filter(p => p.id !== promotion.id));
        this.submitting.set(false);
        this.closeDeleteModal();
      },
      error: err => {
        this.formError.set(this.extractError(err, 'Impossible de supprimer cette promotion.'));
        this.submitting.set(false);
      },
    });
  }
}
