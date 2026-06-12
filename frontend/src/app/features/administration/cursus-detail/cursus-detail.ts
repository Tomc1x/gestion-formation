import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import {
  LucideArrowLeft,
  LucidePlus,
  LucideX,
  LucideArrowUp,
  LucideArrowDown,
  LucideUsers,
  LucideGripVertical,
  LucideListTree,
} from '@lucide/angular';
import { BaseCursusAdapter } from '../../../core/adapters/cursus.adapter';
import { BaseCoursAdapter } from '../../../core/adapters/cours.adapter';
import { Cursus } from '../../../core/models/cursus.model';
import { Cours } from '../../../core/models/cours.model';
import { computeCursusPrereqAlerts, CursusPrereqAlert } from '../../../core/utils/cursus-alerts.util';

@Component({
  selector: 'app-cursus-detail',
  imports: [
    RouterLink,
    LucideArrowLeft,
    LucidePlus,
    LucideX,
    LucideArrowUp,
    LucideArrowDown,
    LucideUsers,
    LucideGripVertical,
    LucideListTree,
  ],
  templateUrl: './cursus-detail.html',
  styleUrl: './cursus-detail.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CursusDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly cursusAdapter = inject(BaseCursusAdapter);
  private readonly coursAdapter = inject(BaseCoursAdapter);

  protected readonly cursus = signal<Cursus | null>(null);
  protected readonly catalogue = signal<Cours[]>([]);
  protected readonly loading = signal(true);
  protected readonly loadError = signal<string | null>(null);
  protected readonly formError = signal<string | null>(null);

  /** Liste des cours du cursus, avec leurs `prerequis: Cours[]` résolus depuis le catalogue. */
  protected readonly coursWithPrereqs = computed<Cours[]>(() => {
    const cursus = this.cursus();
    if (!cursus) return [];
    const catalogue = this.catalogue();
    const byId = new Map(catalogue.map(c => [c.id, c]));

    return cursus.cours.map(c => {
      const catalogueCours = byId.get(c.id);
      return {
        id: c.id,
        name: c.name,
        dureeJours: catalogueCours?.dureeJours ?? null,
        formateurs: c.formateurs,
        prerequis: catalogueCours?.prerequis ?? [],
      };
    });
  });

  protected readonly prereqAlerts = computed<CursusPrereqAlert[]>(() =>
    computeCursusPrereqAlerts(this.coursWithPrereqs())
  );

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const id = idParam ? Number(idParam) : null;
    if (id === null) {
      this.loadError.set('Cursus introuvable.');
      this.loading.set(false);
      return;
    }
    this.loadCursus(id);

    this.coursAdapter.getAll().subscribe({
      next: cours => this.catalogue.set(cours),
    });
  }

  private loadCursus(id: number): void {
    this.loading.set(true);
    this.loadError.set(null);

    this.cursusAdapter.getAll().subscribe({
      next: cursusList => {
        const found = cursusList.find(c => c.id === id);
        if (!found) {
          this.loadError.set('Cursus introuvable.');
        } else {
          this.cursus.set(found);
        }
        this.loading.set(false);
      },
      error: () => {
        this.loadError.set('Impossible de charger le cursus.');
        this.loading.set(false);
      },
    });
  }

  protected fullName(p: { firstName: string; lastName: string }): string {
    return `${p.firstName} ${p.lastName}`;
  }

  // ── Réordonnancement des cours (drag&drop + boutons monter/descendre) ────────
  protected readonly draggedCoursId = signal<number | null>(null);

  protected onDragStart(coursId: number): void {
    this.draggedCoursId.set(coursId);
  }

  protected onDragEnd(): void {
    this.draggedCoursId.set(null);
  }

  protected onDropOnCours(targetCoursId: number): void {
    const cursus = this.cursus();
    const dragged = this.draggedCoursId();
    if (!cursus || dragged === null || dragged === targetCoursId) return;

    const ids = cursus.cours.map(c => c.id);
    const fromIndex = ids.indexOf(dragged);
    const toIndex = ids.indexOf(targetCoursId);
    if (fromIndex === -1 || toIndex === -1) return;

    const reordered = [...ids];
    reordered.splice(fromIndex, 1);
    reordered.splice(toIndex, 0, dragged);
    this.draggedCoursId.set(null);
    this.persistReorder(cursus.id, reordered);
  }

  /** Alternative clavier au drag&drop : déplace un cours vers le haut/bas dans le cursus. */
  protected moveCoursInCursus(coursId: number, direction: -1 | 1): void {
    const cursus = this.cursus();
    if (!cursus) return;

    const ids = cursus.cours.map(c => c.id);
    const index = ids.indexOf(coursId);
    const target = index + direction;
    if (index === -1 || target < 0 || target >= ids.length) return;

    const reordered = [...ids];
    [reordered[index], reordered[target]] = [reordered[target], reordered[index]];
    this.persistReorder(cursus.id, reordered);
  }

  private persistReorder(cursusId: number, coursIds: number[]): void {
    this.cursusAdapter.reorder(cursusId, coursIds).subscribe({
      next: updated => this.cursus.set(updated),
      error: () => {
        this.formError.set('Une erreur est survenue lors du réordonnancement du cursus.');
      },
    });
  }

  /**
   * Corrige une alerte de prérequis mal ordonné : replace le prérequis `prereqId`
   * juste avant le cours `beforeCoursId` dans l'ordre du cursus.
   */
  protected fixOrder(prereqId: number, beforeCoursId: number): void {
    const cursus = this.cursus();
    if (!cursus) return;

    const ids = cursus.cours.map(c => c.id);
    const prereqIndex = ids.indexOf(prereqId);
    if (prereqIndex === -1) return;

    const withoutPrereq = ids.filter(id => id !== prereqId);
    const insertAt = withoutPrereq.indexOf(beforeCoursId);
    const reordered = [...withoutPrereq];
    reordered.splice(Math.max(insertAt, 0), 0, prereqId);

    this.persistReorder(cursus.id, reordered);
  }

  // ── Ajouter / retirer un cours ────────────────────────────────────────────────
  protected availableCours(): Cours[] {
    const cursus = this.cursus();
    if (!cursus) return [];
    const ids = new Set(cursus.cours.map(c => c.id));
    return this.catalogue().filter(c => !ids.has(c.id));
  }

  protected readonly showAddCoursModal = signal(false);
  protected readonly coursSearchQuery = signal<string>('');

  protected readonly filteredAvailableCours = computed(() => {
    const query = this.coursSearchQuery().toLowerCase().trim();
    const baseList = this.availableCours();
    if (!query) return baseList;
    return baseList.filter(cours => cours.name.toLowerCase().includes(query));
  });

  protected openAddCoursModal(): void {
    this.formError.set(null);
    this.coursSearchQuery.set('');
    this.showAddCoursModal.set(true);
  }

  protected closeAddCoursModal(): void {
    this.showAddCoursModal.set(false);
  }

  protected onSearchQueryChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.coursSearchQuery.set(input.value);
  }

  protected addCoursToCursus(coursId: number): void {
    const cursus = this.cursus();
    if (!cursus || !coursId) return;

    this.cursusAdapter.addCours(cursus.id, coursId).subscribe({
      next: updated => this.cursus.set(updated),
      error: () => {
        this.formError.set('Une erreur est survenue lors de l\'ajout du cours au cursus.');
      },
    });
  }

  protected removeCoursFromCursus(coursId: number): void {
    const cursus = this.cursus();
    if (!cursus) return;

    this.cursusAdapter.removeCours(cursus.id, coursId).subscribe({
      next: updated => this.cursus.set(updated),
      error: () => {
        this.formError.set('Une erreur est survenue lors du retrait du cours.');
      },
    });
  }
}
