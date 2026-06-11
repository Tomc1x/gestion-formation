import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { ReactiveFormsModule, FormGroup, FormControl, Validators, FormArray } from '@angular/forms';
import {
  LucideBookOpen,
  LucidePlus,
  LucidePencil,
  LucideTrash2,
  LucideX,
  LucideUsers,
} from '@lucide/angular';
import { BaseCoursAdapter } from '../../../core/adapters/cours.adapter';
import { BaseUserAdminAdapter } from '../../../core/adapters/user-admin.adapter';
import { Cours, CreateCoursRequest, FormateurInfo } from '../../../core/models/cours.model';
import { UserAdmin } from '../../../core/models/user.model';

@Component({
  selector: 'app-cours',
  imports: [
    ReactiveFormsModule,
    LucideBookOpen,
    LucidePlus,
    LucidePencil,
    LucideTrash2,
    LucideX,
    LucideUsers,
  ],
  templateUrl: './cours.html',
  styleUrl: './cours.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CoursComponent implements OnInit {
  private readonly coursAdapter = inject(BaseCoursAdapter);
  private readonly userAdminAdapter = inject(BaseUserAdminAdapter);

  protected readonly coursList = signal<Cours[]>([]);
  protected readonly formateurs = signal<FormateurInfo[]>([]);
  protected readonly loading = signal(true);
  protected readonly loadError = signal<string | null>(null);

  protected readonly submitting = signal(false);
  protected readonly formError = signal<string | null>(null);

  // ── Calcul "Requis par" ──────────────────────────────────────────────────────
  protected readonly requiredByMap = computed<Map<number, Cours[]>>(() => {
    const map = new Map<number, Cours[]>();
    for (const cours of this.coursList()) {
      for (const prereq of cours.prerequis) {
        const list = map.get(prereq.id) ?? [];
        list.push(cours);
        map.set(prereq.id, list);
      }
    }
    return map;
  });

  protected requiredBy(coursId: number): Cours[] {
    return this.requiredByMap().get(coursId) ?? [];
  }

  // ── Création ──────────────────────────────────────────────────────────────────
  protected readonly showCreateModal = signal(false);

  protected readonly createForm = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: Validators.required }),
    formateurIds: new FormArray<FormControl<boolean>>([]),
    prerequisIds: new FormArray<FormControl<boolean>>([]),
  });

  // ── Modification ─────────────────────────────────────────────────────────────
  protected readonly editingCours = signal<Cours | null>(null);

  protected readonly editForm = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: Validators.required }),
    formateurIds: new FormArray<FormControl<boolean>>([]),
    prerequisIds: new FormArray<FormControl<boolean>>([]),
  });

  /** Pour la modale d'édition : ids de cours désactivés (créeraient un cycle) */
  protected readonly disabledPrerequisIds = signal<Set<number>>(new Set());

  // ── Suppression ───────────────────────────────────────────────────────────────
  protected readonly deletingCours = signal<Cours | null>(null);

  ngOnInit(): void {
    this.loadAll();
  }

  private loadAll(): void {
    this.loading.set(true);
    this.loadError.set(null);

    this.coursAdapter.getAll().subscribe({
      next: cours => {
        this.coursList.set(cours);
        this.loading.set(false);
      },
      error: () => {
        this.loadError.set('Impossible de charger le catalogue de cours.');
        this.loading.set(false);
      },
    });

    this.userAdminAdapter.getAll().subscribe({
      next: users => {
        this.formateurs.set(
          users
            .filter((u: UserAdmin) => u.role === 'FORMATEUR')
            .map(u => ({ id: u.uid, firstName: u.firstName, lastName: u.lastName }))
        );
      },
    });
  }

  protected fullName(p: { firstName: string; lastName: string }): string {
    return `${p.firstName} ${p.lastName}`;
  }

  // ── Création ──────────────────────────────────────────────────────────────────
  protected openCreateModal(): void {
    this.formError.set(null);
    this.createForm.controls.name.setValue('');

    const formateurArray = this.createForm.controls.formateurIds;
    formateurArray.clear();
    this.formateurs().forEach(() => formateurArray.push(new FormControl(false, { nonNullable: true })));

    const prereqArray = this.createForm.controls.prerequisIds;
    prereqArray.clear();
    this.coursList().forEach(() => prereqArray.push(new FormControl(false, { nonNullable: true })));

    this.showCreateModal.set(true);
  }

  protected closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  protected submitCreate(): void {
    if (this.createForm.invalid || this.submitting()) return;

    const v = this.createForm.getRawValue();
    const formateurIds = this.formateurs()
      .filter((_, i) => v.formateurIds[i])
      .map(f => f.id);
    const prerequisIds = this.coursList()
      .filter((_, i) => v.prerequisIds[i])
      .map(c => c.id);

    const req: CreateCoursRequest = { name: v.name, formateurIds, prerequisIds };

    this.submitting.set(true);
    this.formError.set(null);

    this.coursAdapter.create(req).subscribe({
      next: created => {
        this.coursList.update(list => [...list, created]);
        this.submitting.set(false);
        this.closeCreateModal();
      },
      error: err => {
        this.formError.set(this.extractError(err, 'Une erreur est survenue lors de la création.'));
        this.submitting.set(false);
      },
    });
  }

  // ── Modification ─────────────────────────────────────────────────────────────
  protected openEditModal(cours: Cours): void {
    this.formError.set(null);
    this.editForm.controls.name.setValue(cours.name);

    const formateurArray = this.editForm.controls.formateurIds;
    formateurArray.clear();
    const formateurIds = new Set(cours.formateurs.map(f => f.id));
    this.formateurs().forEach(f => formateurArray.push(new FormControl(formateurIds.has(f.id), { nonNullable: true })));

    const prereqIds = new Set(cours.prerequis.map(p => p.id));
    const otherCours = this.coursList().filter(c => c.id !== cours.id);

    // Un cours C ne peut pas devenir prérequis de `cours` si `cours` est déjà
    // (transitivement) prérequis de C — sinon cycle.
    const disabled = new Set<number>();
    for (const candidate of otherCours) {
      if (this.isTransitivePrerequis(cours.id, candidate)) {
        disabled.add(candidate.id);
      }
    }
    this.disabledPrerequisIds.set(disabled);

    const prereqArray = this.editForm.controls.prerequisIds;
    prereqArray.clear();
    otherCours.forEach(c => prereqArray.push(new FormControl(prereqIds.has(c.id), { nonNullable: true })));

    this.editingCours.set(cours);
  }

  protected closeEditModal(): void {
    this.editingCours.set(null);
  }

  /** Vrai si `target` apparaît (directement ou transitivement) dans l'arbre `prerequis` de `candidate`. */
  private isTransitivePrerequis(targetId: number, candidate: Cours): boolean {
    for (const prereq of candidate.prerequis) {
      if (prereq.id === targetId) return true;
      if (this.isTransitivePrerequis(targetId, prereq)) return true;
    }
    return false;
  }

  protected editOtherCours(): Cours[] {
    const cours = this.editingCours();
    if (!cours) return [];
    return this.coursList().filter(c => c.id !== cours.id);
  }

  protected hasDisabledPrerequis(): boolean {
    return this.disabledPrerequisIds().size > 0;
  }

  protected submitEdit(): void {
    const cours = this.editingCours();
    if (!cours || this.editForm.invalid || this.submitting()) return;

    const v = this.editForm.getRawValue();
    const formateurIds = this.formateurs()
      .filter((_, i) => v.formateurIds[i])
      .map(f => f.id);
    const otherCours = this.editOtherCours();
    const prerequisIds = otherCours
      .filter((_, i) => v.prerequisIds[i])
      .map(c => c.id);

    this.submitting.set(true);
    this.formError.set(null);

    this.coursAdapter.setFormateurs(cours.id, formateurIds).subscribe({
      next: updatedAfterFormateurs => {
        this.replaceCours(updatedAfterFormateurs);

        this.coursAdapter.setPrerequis(cours.id, prerequisIds).subscribe({
          next: updated => {
            this.replaceCours(updated);
            this.submitting.set(false);
            this.closeEditModal();
          },
          error: err => {
            this.formError.set(this.extractError(err, 'Une erreur est survenue lors de la mise à jour des prérequis.'));
            this.submitting.set(false);
          },
        });
      },
      error: () => {
        this.formError.set('Une erreur est survenue lors de la mise à jour des formateurs.');
        this.submitting.set(false);
      },
    });
  }

  // ── Suppression ───────────────────────────────────────────────────────────────
  protected openDeleteModal(cours: Cours): void {
    this.formError.set(null);
    this.deletingCours.set(cours);
  }

  protected closeDeleteModal(): void {
    this.deletingCours.set(null);
  }

  protected confirmDelete(): void {
    const cours = this.deletingCours();
    if (!cours || this.submitting()) return;

    this.submitting.set(true);
    this.formError.set(null);

    this.coursAdapter.delete(cours.id).subscribe({
      next: () => {
        this.coursList.update(list =>
          list
            .filter(c => c.id !== cours.id)
            .map(c => ({ ...c, prerequis: c.prerequis.filter(p => p.id !== cours.id) }))
        );
        this.submitting.set(false);
        this.closeDeleteModal();
      },
      error: () => {
        this.formError.set('Impossible de supprimer ce cours.');
        this.submitting.set(false);
      },
    });
  }

  // ── Utilitaires ───────────────────────────────────────────────────────────────
  private replaceCours(updated: Cours): void {
    this.coursList.update(list => list.map(c => (c.id === updated.id ? updated : c)));
  }

  private extractError(err: unknown, fallback: string): string {
    const httpErr = err as { status?: number; error?: string };
    if (httpErr.status === 422 && typeof httpErr.error === 'string' && httpErr.error) {
      return httpErr.error;
    }
    return fallback;
  }
}
