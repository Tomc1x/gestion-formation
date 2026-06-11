import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import {
  LucideListTree,
  LucidePlus,
  LucideX,
  LucideArrowUp,
  LucideArrowDown,
  LucideTrash2,
  LucideUsers,
  LucidePencil,
} from '@lucide/angular';
import { BaseCursusAdapter } from '../../../core/adapters/cursus.adapter';
import { BaseFiliereAdapter } from '../../../core/adapters/filiere.adapter';
import { BaseCoursAdapter } from '../../../core/adapters/cours.adapter';
import { Cursus, Filiere, CreateCursusRequest } from '../../../core/models/cursus.model';
import { Cours } from '../../../core/models/cours.model';

/** Une ligne de la liste construite dans la modale "Nouveau cursus". */
interface BuilderRow {
  cours: Cours;
  /** true si cette ligne représente un prérequis manquant non encore ajouté par l'utilisateur. */
  ghost: boolean;
}

/** Couleurs déterministes pour les cards de filière (purement visuel, non persisté). */
const FILIERE_COLORS = [
  '#2563EB', '#7C3AED', '#DB2777', '#D97706',
  '#059669', '#0891B2', '#DC2626', '#4F46E5',
];

@Component({
  selector: 'app-cursus',
  imports: [
    ReactiveFormsModule,
    LucideListTree,
    LucidePlus,
    LucideX,
    LucideArrowUp,
    LucideArrowDown,
    LucideTrash2,
    LucideUsers,
    LucidePencil,
  ],
  templateUrl: './cursus.html',
  styleUrl: './cursus.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CursusComponent implements OnInit {
  private readonly cursusAdapter = inject(BaseCursusAdapter);
  private readonly filiereAdapter = inject(BaseFiliereAdapter);
  private readonly coursAdapter = inject(BaseCoursAdapter);

  protected readonly cursusList = signal<Cursus[]>([]);
  protected readonly filieres = signal<Filiere[]>([]);
  protected readonly catalogue = signal<Cours[]>([]);
  protected readonly loading = signal(true);
  protected readonly loadError = signal<string | null>(null);

  protected readonly submitting = signal(false);
  protected readonly formError = signal<string | null>(null);

  // ── Regroupement par filière ─────────────────────────────────────────────────
  protected readonly groupedByFiliere = computed<{ filiere: Filiere | null; cursus: Cursus[] }[]>(() => {
    const groups = new Map<number | null, Cursus[]>();
    for (const cursus of this.cursusList()) {
      const list = groups.get(cursus.filiereId) ?? [];
      list.push(cursus);
      groups.set(cursus.filiereId, list);
    }
    const result: { filiere: Filiere | null; cursus: Cursus[] }[] = [];
    for (const filiere of this.filieres()) {
      const cursus = groups.get(filiere.id) ?? [];
      if (cursus.length > 0) {
        result.push({ filiere, cursus });
      }
    }
    const sansFiliere = groups.get(null) ?? [];
    if (sansFiliere.length > 0) {
      result.push({ filiere: null, cursus: sansFiliere });
    }
    return result;
  });

  protected filiereColor(filiereId: number): string {
    return FILIERE_COLORS[filiereId % FILIERE_COLORS.length];
  }

  protected cursusCount(filiereId: number): number {
    return this.cursusList().filter(c => c.filiereId === filiereId).length;
  }

  ngOnInit(): void {
    this.loadAll();
  }

  private loadAll(): void {
    this.loading.set(true);
    this.loadError.set(null);

    this.cursusAdapter.getAll().subscribe({
      next: cursus => {
        this.cursusList.set(cursus);
        this.loading.set(false);
      },
      error: () => {
        this.loadError.set('Impossible de charger les cursus.');
        this.loading.set(false);
      },
    });

    this.filiereAdapter.getAll().subscribe({
      next: filieres => this.filieres.set(filieres),
    });

    this.coursAdapter.getAll().subscribe({
      next: cours => this.catalogue.set(cours),
    });
  }

  protected fullName(p: { firstName: string; lastName: string }): string {
    return `${p.firstName} ${p.lastName}`;
  }

  // ── Modale : Nouvelle filière ─────────────────────────────────────────────────
  protected readonly showFiliereModal = signal(false);

  protected readonly filiereForm = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: Validators.required }),
  });

  protected openFiliereModal(): void {
    this.formError.set(null);
    this.filiereForm.controls.name.setValue('');
    this.showFiliereModal.set(true);
  }

  protected closeFiliereModal(): void {
    this.showFiliereModal.set(false);
  }

  protected submitFiliere(): void {
    if (this.filiereForm.invalid || this.submitting()) return;

    this.submitting.set(true);
    this.formError.set(null);

    this.filiereAdapter.create({ name: this.filiereForm.controls.name.value }).subscribe({
      next: created => {
        this.filieres.update(list => [...list, created]);
        this.submitting.set(false);
        this.closeFiliereModal();
      },
      error: () => {
        this.formError.set('Une erreur est survenue lors de la création de la filière.');
        this.submitting.set(false);
      },
    });
  }

  // ── Modale : Modifier une filière ─────────────────────────────────────────────
  protected readonly editingFiliere = signal<Filiere | null>(null);

  protected readonly editFiliereForm = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: Validators.required }),
  });

  protected openEditFiliereModal(filiere: Filiere): void {
    this.formError.set(null);
    this.editFiliereForm.controls.name.setValue(filiere.name);
    this.editingFiliere.set(filiere);
  }

  protected closeEditFiliereModal(): void {
    this.editingFiliere.set(null);
  }

  protected submitEditFiliere(): void {
    const filiere = this.editingFiliere();
    if (!filiere || this.editFiliereForm.invalid || this.submitting()) return;

    this.submitting.set(true);
    this.formError.set(null);

    this.filiereAdapter.update(filiere.id, { name: this.editFiliereForm.controls.name.value }).subscribe({
      next: updated => {
        this.filieres.update(list => list.map(f => (f.id === updated.id ? updated : f)));
        this.cursusList.update(list =>
          list.map(c => (c.filiereId === updated.id ? { ...c, filiereName: updated.name } : c))
        );
        this.submitting.set(false);
        this.closeEditFiliereModal();
      },
      error: err => {
        this.formError.set(this.extractError(err, 'Une erreur est survenue lors de la modification de la filière.'));
        this.submitting.set(false);
      },
    });
  }

  // ── Modale : Supprimer une filière ────────────────────────────────────────────
  protected readonly deletingFiliere = signal<Filiere | null>(null);

  protected openDeleteFiliereModal(filiere: Filiere): void {
    this.formError.set(null);
    this.deletingFiliere.set(filiere);
  }

  protected closeDeleteFiliereModal(): void {
    this.deletingFiliere.set(null);
  }

  protected confirmDeleteFiliere(): void {
    const filiere = this.deletingFiliere();
    if (!filiere || this.submitting()) return;

    this.submitting.set(true);
    this.formError.set(null);

    this.filiereAdapter.delete(filiere.id).subscribe({
      next: () => {
        this.filieres.update(list => list.filter(f => f.id !== filiere.id));
        this.submitting.set(false);
        this.closeDeleteFiliereModal();
      },
      error: err => {
        this.formError.set(this.extractError(err, 'Impossible de supprimer cette filière.'));
        this.submitting.set(false);
      },
    });
  }

  private extractError(err: unknown, fallback: string): string {
    const httpErr = err as { status?: number; error?: string };
    if ((httpErr.status === 409 || httpErr.status === 422) && typeof httpErr.error === 'string' && httpErr.error) {
      return httpErr.error;
    }
    return fallback;
  }

  // ── Modale : Nouveau cursus ───────────────────────────────────────────────────
  protected readonly showCursusModal = signal(false);

  protected readonly cursusForm = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: Validators.required }),
    filiereId: new FormControl<number | null>(null, { validators: Validators.required }),
  });

  /** Liste construite (ordonnée), inclut les lignes "fantômes" pour les prérequis manquants. */
  protected readonly builderRows = signal<BuilderRow[]>([]);

  protected openCursusModal(): void {
    this.formError.set(null);
    this.cursusForm.reset({ name: '', filiereId: null });
    this.builderRows.set([]);
    this.showCursusModal.set(true);
  }

  protected closeCursusModal(): void {
    this.showCursusModal.set(false);
  }

  /** Cours du catalogue pas encore ajoutés à la liste construite (hors lignes fantômes). */
  protected availableCours(): Cours[] {
    const ids = new Set(this.builderRows().filter(r => !r.ghost).map(r => r.cours.id));
    return this.catalogue().filter(c => !ids.has(c.id));
  }

  protected addCoursToBuilder(cours: Cours): void {
    this.builderRows.update(rows => {
      // Si une ligne fantôme existe déjà pour ce cours, on la "matérialise" sur place.
      const ghostIndex = rows.findIndex(r => r.ghost && r.cours.id === cours.id);
      if (ghostIndex !== -1) {
        const next = [...rows];
        next[ghostIndex] = { cours, ghost: false };
        return this.recomputeGhosts(next);
      }
      return this.recomputeGhosts([...rows, { cours, ghost: false }]);
    });
  }

  protected removeCoursFromBuilder(coursId: number): void {
    this.builderRows.update(rows => this.recomputeGhosts(rows.filter(r => !(r.cours.id === coursId && !r.ghost))));
  }

  protected moveUp(index: number): void {
    if (index <= 0) return;
    this.builderRows.update(rows => {
      const next = [...rows];
      [next[index - 1], next[index]] = [next[index], next[index - 1]];
      return this.recomputeGhosts(next);
    });
  }

  protected moveDown(index: number): void {
    this.builderRows.update(rows => {
      if (index >= rows.length - 1) return rows;
      const next = [...rows];
      [next[index], next[index + 1]] = [next[index + 1], next[index]];
      return this.recomputeGhosts(next);
    });
  }

  /** Insère le prérequis fantôme `prereq` juste avant le cours qui le requiert. */
  protected addGhostPrerequis(ghostRow: BuilderRow, beforeCoursId: number): void {
    this.builderRows.update(rows => {
      const targetIndex = rows.findIndex(r => r.cours.id === beforeCoursId && !r.ghost);
      const filtered = rows.filter(r => !(r.ghost && r.cours.id === ghostRow.cours.id));
      const insertAt = filtered.findIndex(r => r.cours.id === beforeCoursId && !r.ghost);
      const idx = insertAt === -1 ? targetIndex : insertAt;
      const next = [...filtered];
      next.splice(idx, 0, { cours: ghostRow.cours, ghost: false });
      return this.recomputeGhosts(next);
    });
  }

  /**
   * Déplace le prérequis `prereqId` juste avant le cours `beforeCoursId`
   * (utilisé par le bouton "Corriger" quand un prérequis est mal placé).
   */
  protected fixOrder(prereqId: number, beforeCoursId: number): void {
    this.builderRows.update(rows => {
      const prereqRow = rows.find(r => r.cours.id === prereqId && !r.ghost);
      if (!prereqRow) return rows;
      const withoutPrereq = rows.filter(r => !(r.cours.id === prereqId && !r.ghost));
      const insertAt = withoutPrereq.findIndex(r => r.cours.id === beforeCoursId && !r.ghost);
      const next = [...withoutPrereq];
      next.splice(Math.max(insertAt, 0), 0, prereqRow);
      return this.recomputeGhosts(next);
    });
  }

  /**
   * Recalcule les lignes "fantômes" (prérequis manquants) après chaque modification :
   * pour chaque cours réel, si un prérequis (direct ou transitif) n'apparaît pas
   * AVANT lui dans la liste, on insère une ligne fantôme juste après ce cours.
   */
  private recomputeGhosts(rows: BuilderRow[]): BuilderRow[] {
    const realRows = rows.filter(r => !r.ghost);
    const result: BuilderRow[] = [];

    for (let i = 0; i < realRows.length; i++) {
      const row = realRows[i];
      result.push(row);

      const before = realRows.slice(0, i).map(r => r.cours.id);
      const allPrereqs = this.transitivePrerequis(row.cours);

      for (const prereq of allPrereqs) {
        if (!before.includes(prereq.id) && !realRows.some(r => r.cours.id === prereq.id)) {
          result.push({ cours: prereq, ghost: true });
        }
      }
    }

    return result;
  }

  /** Renvoie tous les prérequis (directs et transitifs) d'un cours, sans doublons. */
  private transitivePrerequis(cours: Cours, seen = new Set<number>()): Cours[] {
    const result: Cours[] = [];
    for (const prereq of cours.prerequis) {
      if (seen.has(prereq.id)) continue;
      seen.add(prereq.id);
      result.push(prereq);
      result.push(...this.transitivePrerequis(prereq, seen));
    }
    return result;
  }

  /**
   * Pour un cours réel donné (par index dans `builderRows`), renvoie la liste des
   * prérequis présents dans la liste mais positionnés APRÈS lui (mal ordonnés),
   * avec leur position actuelle (1-indexée).
   */
  protected misorderedPrereqs(index: number): { prereq: Cours; position: number }[] {
    const rows = this.builderRows();
    const row = rows[index];
    if (row.ghost) return [];

    const allPrereqs = this.transitivePrerequis(row.cours);
    const result: { prereq: Cours; position: number }[] = [];

    for (const prereq of allPrereqs) {
      const prereqIndex = rows.findIndex(r => r.cours.id === prereq.id && !r.ghost);
      if (prereqIndex > index) {
        result.push({ prereq, position: prereqIndex + 1 });
      }
    }

    return result;
  }

  /** Vrai si ce cours (réel) a au moins un prérequis mal ordonné. */
  protected hasMisorderedPrereqs(index: number): boolean {
    return this.misorderedPrereqs(index).length > 0;
  }

  protected submitCursus(): void {
    if (this.cursusForm.invalid || this.submitting()) return;

    const v = this.cursusForm.getRawValue();
    const req: CreateCursusRequest = { name: v.name, filiereId: v.filiereId! };
    const coursIds = this.builderRows().filter(r => !r.ghost).map(r => r.cours.id);

    this.submitting.set(true);
    this.formError.set(null);

    this.cursusAdapter.create(req).subscribe({
      next: created => {
        this.addCoursSequentially(created.id, coursIds, 0, created);
      },
      error: () => {
        this.formError.set('Une erreur est survenue lors de la création du cursus.');
        this.submitting.set(false);
      },
    });
  }

  /**
   * Ajoute les cours un par un dans l'ordre choisi (sans `ordre` explicite : chaque
   * appel ajoute en fin de liste, ce qui préserve l'ordre voulu). Approche choisie
   * pour sa simplicité par rapport à un appel `reorder` après ajouts en vrac.
   */
  private addCoursSequentially(cursusId: number, coursIds: number[], index: number, last: Cursus): void {
    if (index >= coursIds.length) {
      this.cursusList.update(list => [...list, last]);
      this.submitting.set(false);
      this.closeCursusModal();
      return;
    }

    this.cursusAdapter.addCours(cursusId, coursIds[index]).subscribe({
      next: updated => this.addCoursSequentially(cursusId, coursIds, index + 1, updated),
      error: () => {
        this.formError.set('Le cursus a été créé mais l\'ajout des cours a échoué.');
        this.submitting.set(false);
        // Le cursus existe déjà côté serveur : on le reflète tel quel dans la liste.
        this.cursusList.update(list => [...list, last]);
        this.closeCursusModal();
      },
    });
  }
}
