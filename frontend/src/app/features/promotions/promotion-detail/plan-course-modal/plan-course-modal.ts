import { Component, ChangeDetectionStrategy, input, output, computed, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { LucideX } from '@lucide/angular';
import { Promotion, PromotionCours } from '../../../../core/models/promotion.model';
import { Cours } from '../../../../core/models/cours.model';
import { FormateurOption } from '../cours-planifies/cours-planifies-tab';

export interface PlanCourseSaved {
  coursId: number;
  dateDebut: string;
  dateFin: string;
  formateurId: number | null;
  salle: string | null;
  force: boolean;
}

interface PrerequisStatus {
  id: number;
  name: string;
  satisfait: boolean;
}

@Component({
  selector: 'app-plan-course-modal',
  imports: [ReactiveFormsModule, LucideX],
  templateUrl: './plan-course-modal.html',
  styleUrl: './plan-course-modal.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PlanCourseModalComponent implements OnInit {
  readonly promotion = input.required<Promotion>();
  readonly coursDisponibles = input<Cours[]>([]);
  readonly formateurs = input<FormateurOption[]>([]);
  readonly editTarget = input<PromotionCours | null>(null);

  readonly closed = output<void>();
  readonly saved = output<PlanCourseSaved>();

  protected readonly form = new FormGroup({
    coursId: new FormControl<number | null>(null, { validators: Validators.required }),
    dateDebut: new FormControl('', { nonNullable: true, validators: Validators.required }),
    dateFin: new FormControl('', { nonNullable: true, validators: Validators.required }),
    formateurId: new FormControl<number | null>(null),
    salle: new FormControl<string | null>(null),
    // Hypothèse FULLST-012 (section 4 ANALYSIS) : "force" est purement informatif côté
    // frontend, aucun contrôle de prérequis bloquant n'existe côté backend pour
    // CoursPlanifie (FULLST-007/008/011). Le payload reste extensible si un futur
    // endpoint l'exploite.
    force: new FormControl(false, { nonNullable: true }),
  });

  /** Cours sélectionné dans le formulaire, recherché dans coursDisponibles(). */
  protected readonly selectedCours = computed<Cours | null>(() => {
    const coursId = this.form.controls.coursId.value;
    if (coursId === null) return null;
    return this.coursDisponibles().find(c => c.id === coursId) ?? null;
  });

  /** Identifiants des cours déjà planifiés dans cette promotion. */
  private readonly coursPlanifiesIds = computed(() => new Set(this.promotion().planning.map(pc => pc.coursId)));

  /** Liste aplatie (dédupliquée) des prérequis du cours sélectionné, avec leur statut. */
  protected readonly prerequisRequis = computed<PrerequisStatus[]>(() => {
    const cours = this.selectedCours();
    if (!cours) return [];

    const planifiesIds = this.coursPlanifiesIds();
    const seen = new Set<number>();
    const result: PrerequisStatus[] = [];

    const collect = (c: Cours): void => {
      for (const p of c.prerequis) {
        if (seen.has(p.id)) continue;
        seen.add(p.id);
        result.push({ id: p.id, name: p.name, satisfait: planifiesIds.has(p.id) });
        collect(p);
      }
    };
    collect(cours);

    return result;
  });

  protected readonly tousSatisfaits = computed(() => this.prerequisRequis().every(p => p.satisfait));

  ngOnInit(): void {
    const target = this.editTarget();
    if (target) {
      this.form.patchValue({
        coursId: target.coursId,
        dateDebut: target.dateDebut,
        dateFin: target.dateFin,
        formateurId: target.formateurId,
        salle: target.salle,
      });
      this.form.controls.coursId.disable();
    }
  }

  protected close(): void {
    this.closed.emit();
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { coursId, dateDebut, dateFin, formateurId, salle, force } = this.form.getRawValue();
    if (dateFin < dateDebut) {
      return;
    }
    if (coursId === null) return;

    this.saved.emit({ coursId, dateDebut, dateFin, formateurId, salle, force });
  }
}
