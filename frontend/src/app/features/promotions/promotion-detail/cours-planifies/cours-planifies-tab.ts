import { Component, ChangeDetectionStrategy, inject, input, output, signal, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { LucidePencil } from '@lucide/angular';
import { BasePromotionAdapter } from '../../../../core/adapters/promotion.adapter';
import { BaseUserAdminAdapter } from '../../../../core/adapters/user-admin.adapter';
import { Promotion, PromotionCours } from '../../../../core/models/promotion.model';

export interface FormateurOption {
  id: number;
  nom: string;
}

@Component({
  selector: 'app-cours-planifies-tab',
  imports: [DatePipe, ReactiveFormsModule, LucidePencil],
  templateUrl: './cours-planifies-tab.html',
  styleUrl: './cours-planifies-tab.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CoursPlanifiesTabComponent implements OnInit {
  private readonly promotionAdapter = inject(BasePromotionAdapter);
  private readonly userAdminAdapter = inject(BaseUserAdminAdapter);

  readonly promotion = input.required<Promotion>();
  readonly promotionUpdated = output<Promotion>();

  protected readonly editingSession = signal<PromotionCours | null>(null);
  protected readonly submitting = signal(false);
  protected readonly formError = signal<string | null>(null);
  protected readonly sessionWarnings = signal<string[]>([]);
  protected readonly formateurs = signal<FormateurOption[]>([]);

  ngOnInit(): void {
    this.userAdminAdapter.getAll().subscribe({
      next: users => {
        this.formateurs.set(
          users
            .filter(u => u.role === 'FORMATEUR')
            .map(u => ({ id: u.uid, nom: `${u.firstName} ${u.lastName}` }))
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
}
