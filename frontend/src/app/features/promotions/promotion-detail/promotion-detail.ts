import { Component, ChangeDetectionStrategy, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { LucideArrowLeft, LucideCalendarPlus, LucideUserPlus } from '@lucide/angular';
import { BasePromotionAdapter } from '../../../core/adapters/promotion.adapter';
import { BaseCursusAdapter } from '../../../core/adapters/cursus.adapter';
import { BaseCoursAdapter } from '../../../core/adapters/cours.adapter';
import { BaseUserAdminAdapter } from '../../../core/adapters/user-admin.adapter';
import { Promotion, PromotionCours } from '../../../core/models/promotion.model';
import { Cours } from '../../../core/models/cours.model';
import { CoursPlanifiesTabComponent, FormateurOption } from './cours-planifies/cours-planifies-tab';
import { StagiairesTabComponent } from './stagiaires/stagiaires-tab';
import { PlanCourseModalComponent, PlanCourseSaved } from './plan-course-modal/plan-course-modal';

type Tab = 'cours' | 'stagiaires';

@Component({
  selector: 'app-promotion-detail',
  imports: [
    RouterLink,
    LucideArrowLeft,
    LucideCalendarPlus,
    LucideUserPlus,
    CoursPlanifiesTabComponent,
    StagiairesTabComponent,
    PlanCourseModalComponent,
  ],
  templateUrl: './promotion-detail.html',
  styleUrl: './promotion-detail.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PromotionDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly promotionAdapter = inject(BasePromotionAdapter);
  private readonly cursusAdapter = inject(BaseCursusAdapter);
  private readonly coursAdapter = inject(BaseCoursAdapter);
  private readonly userAdminAdapter = inject(BaseUserAdminAdapter);

  protected readonly promotion = signal<Promotion | null>(null);
  protected readonly loading = signal(true);
  protected readonly loadError = signal<string | null>(null);

  protected readonly activeTab = signal<Tab>('cours');

  protected readonly cursusCoursCount = signal<number | null>(null);
  protected readonly coursDisponibles = signal<Cours[]>([]);
  protected readonly formateurs = signal<FormateurOption[]>([]);

  protected readonly showPlanCourseModal = signal(false);
  protected readonly planCourseEditTarget = signal<PromotionCours | null>(null);

  /** Incrémenté par "Inscrire un stagiaire" pour déclencher la modale d'ajout d'élève (stagiaires-tab). */
  protected readonly enrollmentTrigger = signal(0);

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const id = idParam ? Number(idParam) : null;
    if (id === null) {
      this.loadError.set('Promotion introuvable.');
      this.loading.set(false);
      return;
    }
    this.loadPromotion(id);

    this.userAdminAdapter.getAll().subscribe({
      next: users => {
        this.formateurs.set(
          users
            .filter(u => u.role === 'FORMATEUR')
            .map(u => ({ id: u.uid, nom: `${u.firstName} ${u.lastName}` }))
        );
      },
      error: () => {
        // Non bloquant.
      },
    });
  }

  private loadPromotion(id: number): void {
    this.loading.set(true);
    this.loadError.set(null);

    this.promotionAdapter.getById(id).subscribe({
      next: promotion => {
        this.promotion.set(promotion);
        this.loading.set(false);
        this.loadCursusCours(promotion);
      },
      error: () => {
        this.loadError.set('Impossible de charger cette promotion.');
        this.loading.set(false);
      },
    });
  }

  private loadCursusCours(promotion: Promotion): void {
    if (promotion.cursusId === null) {
      this.cursusCoursCount.set(null);
      this.coursDisponibles.set([]);
      return;
    }

    this.cursusAdapter.getAll().subscribe({
      next: cursusList => {
        const cursus = cursusList.find(c => c.id === promotion.cursusId);
        const coursIds = new Set((cursus?.cours ?? []).map(cc => cc.id));
        this.cursusCoursCount.set(coursIds.size);

        // Charge le catalogue complet pour disposer de Cours.prerequis (récursif,
        // BACKEN-005), absent de CoursInCursus (Risque #4 ANALYSIS).
        this.coursAdapter.getAll().subscribe({
          next: allCours => {
            this.coursDisponibles.set(allCours.filter(c => coursIds.has(c.id)));
          },
          error: () => {
            this.coursDisponibles.set([]);
          },
        });
      },
      error: () => {
        this.cursusCoursCount.set(null);
      },
    });
  }

  protected setActiveTab(tab: Tab): void {
    this.activeTab.set(tab);
  }

  protected onPromotionUpdated(updated: Promotion): void {
    this.promotion.set(updated);
  }

  // ── Modale "Planifier un cours" ─────────────────────────────────────────────────
  protected openPlanCourseModal(target: PromotionCours | null = null): void {
    this.planCourseEditTarget.set(target);
    this.showPlanCourseModal.set(true);
  }

  protected closePlanCourseModal(): void {
    this.showPlanCourseModal.set(false);
    this.planCourseEditTarget.set(null);
  }

  protected onPlanCourseSaved(saved: PlanCourseSaved): void {
    const promotion = this.promotion();
    if (!promotion) return;

    const target = this.planCourseEditTarget();

    if (target) {
      this.promotionAdapter
        .updatePlanning(promotion.id, target.id, {
          dateDebut: saved.dateDebut,
          dateFin: saved.dateFin,
          formateurId: saved.formateurId,
          salle: saved.salle,
        })
        .subscribe({
          next: updated => {
            this.promotion.update(promo => {
              if (!promo) return promo;
              return {
                ...promo,
                planning: promo.planning.map(pc => pc.id === updated.id ? { ...pc, ...updated } : pc),
              };
            });
            this.closePlanCourseModal();
          },
          error: () => {
            this.loadError.set("Impossible d'enregistrer cette modification.");
          },
        });
      return;
    }

    this.promotionAdapter
      .createPlanning(promotion.id, {
        coursId: saved.coursId,
        dateDebut: saved.dateDebut,
        dateFin: saved.dateFin,
        formateurId: saved.formateurId,
        salle: saved.salle,
        force: saved.force,
      })
      .subscribe({
        next: created => {
          this.promotion.update(promo => {
            if (!promo) return promo;
            return {
              ...promo,
              planning: [...promo.planning, created],
            };
          });
          this.closePlanCourseModal();
        },
        error: () => {
          this.loadError.set("Impossible de planifier ce cours.");
        },
      });
  }

  // ── "Inscrire un stagiaire" (FULLST-015/019) ────────────────────────────────────
  // Ouvre la modale "Ajouter un élève" du tab Stagiaires (réutilisation FULLST-019,
  // cf. mémoire pour le scope complet de FULLST-015 non couvert).
  protected onOpenEnrollmentForm(): void {
    this.activeTab.set('stagiaires');
    this.enrollmentTrigger.update(v => v + 1);
  }
}
