import { Component, ChangeDetectionStrategy, inject, input, output, signal, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { LucidePencil, LucideTrash2, LucideUserPlus } from '@lucide/angular';
import { BasePromotionAdapter } from '../../../../core/adapters/promotion.adapter';
import { BaseFormateurAdapter } from '../../../../core/adapters/formateur.adapter';
import { BaseEleveAdapter, EleveInfo } from '../../../../core/adapters/eleve.adapter';
import { BaseInscriptionAdapter } from '../../../../core/adapters/inscription.adapter';
import { Promotion, PromotionCours } from '../../../../core/models/promotion.model';

export interface FormateurOption {
  id: number;
  nom: string;
}

@Component({
  selector: 'app-cours-planifies-tab',
  imports: [DatePipe, ReactiveFormsModule, LucidePencil, LucideTrash2, LucideUserPlus],
  templateUrl: './cours-planifies-tab.html',
  styleUrl: './cours-planifies-tab.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CoursPlanifiesTabComponent implements OnInit {
  private readonly promotionAdapter = inject(BasePromotionAdapter);
  private readonly formateurAdapter = inject(BaseFormateurAdapter);
  private readonly eleveAdapter = inject(BaseEleveAdapter);
  private readonly inscriptionAdapter = inject(BaseInscriptionAdapter);

  readonly promotion = input.required<Promotion>();
  readonly promotionUpdated = output<Promotion>();

  protected readonly editingSession = signal<PromotionCours | null>(null);
  protected readonly submitting = signal(false);
  protected readonly formError = signal<string | null>(null);
  protected readonly sessionWarnings = signal<string[]>([]);
  protected readonly formateurs = signal<FormateurOption[]>([]);

  protected readonly deleteTarget = signal<PromotionCours | null>(null);
  protected readonly deleting = signal(false);
  protected readonly deleteError = signal<string | null>(null);

  // ── Inscription à l'unité ────────────────────────────────────────────────────
  protected readonly inscriptionTarget = signal<PromotionCours | null>(null);
  protected readonly eleves = signal<EleveInfo[]>([]);
  protected readonly inscriptionError = signal<string | null>(null);
  protected readonly inscriptionWarnings = signal<string[]>([]);
  protected readonly inscriptionSubmitting = signal(false);

  protected readonly inscriptionForm = new FormGroup({
    eleveId: new FormControl<number | null>(null, { validators: Validators.required }),
    forcer: new FormControl(false, { nonNullable: true }),
  });

  ngOnInit(): void {
    this.formateurAdapter.getAll().subscribe({
      next: formateurs => {
        this.formateurs.set(
          formateurs.map(f => ({ id: f.id, nom: `${f.firstName} ${f.lastName}` }))
        );
      },
      error: () => {
        // Non bloquant : la liste des formateurs reste vide, le champ devient optionnel.
      },
    });
  }

  protected readonly editForm = new FormGroup({
    dateDebut: new FormControl('', { nonNullable: true, validators: Validators.required }),
    dateFin: new FormControl('', { nonNullable: true, validators: Validators.required }),
    formateurId: new FormControl<number | null>(null),
    salle: new FormControl<string | null>(null),
  });

  protected openSession(pc: PromotionCours): void {
    this.formError.set(null);
    this.sessionWarnings.set([]);
    this.editForm.setValue({
      dateDebut: pc.dateDebut,
      dateFin: pc.dateFin,
      formateurId: pc.formateurId,
      salle: pc.salle,
    });
    this.editingSession.set(pc);
  }

  protected closeSessionModal(): void {
    this.editingSession.set(null);
    this.formError.set(null);
    this.sessionWarnings.set([]);
  }

  protected submitSession(): void {
    const promotion = this.promotion();
    const session = this.editingSession();
    if (!session) return;

    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      return;
    }

    const { dateDebut, dateFin, formateurId, salle } = this.editForm.getRawValue();
    if (dateFin < dateDebut) {
      this.formError.set('La date de fin doit être postérieure ou égale à la date de début.');
      return;
    }

    this.submitting.set(true);
    this.formError.set(null);
    this.sessionWarnings.set([]);

    this.promotionAdapter.updatePlanning(promotion.id, session.id, { dateDebut, dateFin, formateurId, salle }).subscribe({
      next: updated => {
        this.submitting.set(false);
        this.sessionWarnings.set(updated.warnings);

        const updatedPromotion: Promotion = {
          ...promotion,
          planning: promotion.planning.map(pc => pc.id === updated.id ? { ...pc, ...updated } : pc),
        };
        this.promotionUpdated.emit(updatedPromotion);

        if (updated.warnings.length === 0) {
          this.closeSessionModal();
        } else {
          this.editingSession.set({ ...session, ...updated });
        }
      },
      error: () => {
        this.submitting.set(false);
        this.formError.set("Impossible d'enregistrer cette modification.");
      },
    });
  }

  protected openDeleteConfirm(pc: PromotionCours): void {
    this.deleteError.set(null);
    this.deleteTarget.set(pc);
  }

  protected closeDeleteConfirm(): void {
    this.deleteTarget.set(null);
    this.deleteError.set(null);
  }

  protected confirmDelete(): void {
    const promotion = this.promotion();
    const target = this.deleteTarget();
    if (!target) return;

    this.deleting.set(true);
    this.deleteError.set(null);

    this.promotionAdapter.deletePlanning(promotion.id, target.id).subscribe({
      next: () => {
        this.deleting.set(false);
        const updatedPromotion: Promotion = {
          ...promotion,
          planning: promotion.planning.filter(pc => pc.id !== target.id),
        };
        this.promotionUpdated.emit(updatedPromotion);
        this.closeDeleteConfirm();
      },
      error: () => {
        this.deleting.set(false);
        this.deleteError.set('Impossible de retirer ce cours planifié.');
      },
    });
  }

  // ── Inscription à l'unité ────────────────────────────────────────────────────
  protected openInscriptionModal(pc: PromotionCours): void {
    this.inscriptionError.set(null);
    this.inscriptionWarnings.set([]);
    this.inscriptionForm.reset({ eleveId: null, forcer: false });

    this.eleveAdapter.getAll().subscribe({
      next: eleves => this.eleves.set(eleves),
      error: () => this.inscriptionError.set('Impossible de charger la liste des élèves.'),
    });

    this.inscriptionTarget.set(pc);
  }

  protected closeInscriptionModal(): void {
    this.inscriptionTarget.set(null);
    this.inscriptionError.set(null);
    this.inscriptionWarnings.set([]);
  }

  protected submitInscription(): void {
    const target = this.inscriptionTarget();
    if (!target) return;

    if (this.inscriptionForm.invalid) {
      this.inscriptionForm.markAllAsTouched();
      return;
    }

    const { eleveId, forcer } = this.inscriptionForm.getRawValue();
    if (eleveId == null) return;

    this.inscriptionSubmitting.set(true);
    this.inscriptionError.set(null);
    this.inscriptionWarnings.set([]);

    this.inscriptionAdapter.creerInscription(target.id, { eleveId, forcer }).subscribe({
      next: result => {
        this.inscriptionSubmitting.set(false);
        if (result.warnings.length === 0) {
          this.closeInscriptionModal();
        } else {
          this.inscriptionWarnings.set(result.warnings);
        }
      },
      error: (err: HttpErrorResponse) => {
        this.inscriptionSubmitting.set(false);
        if (err.status === 409) {
          this.inscriptionError.set(typeof err.error === 'string' ? err.error : "Cette inscription n'est pas possible.");
        } else {
          this.inscriptionError.set("Impossible d'inscrire cet élève.");
        }
      },
    });
  }
}
