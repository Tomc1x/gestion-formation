import { Component, ChangeDetectionStrategy, inject, input, output, signal, computed, effect } from '@angular/core';
import { LucideTrash2, LucideUserPlus, LucideX } from '@lucide/angular';
import { BasePromotionAdapter } from '../../../../core/adapters/promotion.adapter';
import { BaseUserAdminAdapter } from '../../../../core/adapters/user-admin.adapter';
import { Promotion } from '../../../../core/models/promotion.model';
import { EntitySelectorComponent, SelectableEntity } from '../../../../shared/components/entity-selector/entity-selector';

@Component({
  selector: 'app-stagiaires-tab',
  imports: [LucideTrash2, LucideUserPlus, LucideX, EntitySelectorComponent],
  templateUrl: './stagiaires-tab.html',
  styleUrl: './stagiaires-tab.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StagiairesTabComponent {
  private readonly promotionAdapter = inject(BasePromotionAdapter);
  private readonly userAdminAdapter = inject(BaseUserAdminAdapter);

  readonly promotion = input.required<Promotion>();
  /** Incrémenté par le parent (bouton "Inscrire un stagiaire") pour ouvrir la modale d'ajout. */
  readonly openEnrollmentTrigger = input<number>(0);
  readonly promotionUpdated = output<Promotion>();

  protected readonly elevesDisponibles = signal<SelectableEntity[]>([]);
  protected readonly showAddEleveModal = signal(false);
  protected readonly addEleveError = signal<string | null>(null);
  protected readonly removingEleveId = signal<number | null>(null);

  protected readonly currentEleveIds = computed(() => new Set(this.promotion().eleves.map(e => e.id)));

  private lastEnrollmentTrigger = 0;

  constructor() {
    effect(() => {
      const trigger = this.openEnrollmentTrigger();
      if (trigger > 0 && trigger !== this.lastEnrollmentTrigger) {
        this.lastEnrollmentTrigger = trigger;
        this.openAddEleveModal();
      }
    });
  }

  protected fullName(p: { firstName: string; lastName: string }): string {
    return `${p.firstName} ${p.lastName}`;
  }

  // ── Ajout d'élève ──────────────────────────────────────────────────────────────
  protected openAddEleveModal(): void {
    this.addEleveError.set(null);
    const currentIds = this.currentEleveIds();

    this.userAdminAdapter.getAll().subscribe({
      next: users => {
        this.elevesDisponibles.set(
          users
            .filter(u => u.role === 'ETUDIANT' && !currentIds.has(u.uid))
            .map(u => ({ id: u.uid, label: this.fullName(u), sublabel: u.email }))
        );
      },
      error: () => {
        this.addEleveError.set('Impossible de charger la liste des élèves.');
      },
    });

    this.showAddEleveModal.set(true);
  }

  protected closeAddEleveModal(): void {
    this.showAddEleveModal.set(false);
  }

  protected onAddEleve(eleveId: number): void {
    const promotion = this.promotion();
    this.addEleveError.set(null);

    this.promotionAdapter.addEleve(promotion.id, eleveId).subscribe({
      next: updated => {
        this.promotionUpdated.emit(updated);
        this.elevesDisponibles.update(list => list.filter(item => item.id !== eleveId));
      },
      error: () => {
        this.addEleveError.set("Impossible d'ajouter cet élève.");
      },
    });
  }

  // ── Retrait d'élève ────────────────────────────────────────────────────────────
  protected removeEleve(eleveId: number): void {
    const promotion = this.promotion();
    if (this.removingEleveId() !== null) return;

    this.removingEleveId.set(eleveId);

    this.promotionAdapter.removeEleve(promotion.id, eleveId).subscribe({
      next: updated => {
        this.promotionUpdated.emit(updated);
        this.removingEleveId.set(null);
      },
      error: () => {
        this.removingEleveId.set(null);
      },
    });
  }
}
