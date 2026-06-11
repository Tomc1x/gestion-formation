import { Observable, of, throwError } from "rxjs";
import { Injectable } from "@angular/core";
import { Cours, CreateCoursRequest, FormateurInfo } from "../models/cours.model";
import { BaseCoursAdapter } from "./cours.adapter";

interface MockCours {
  id: number;
  name: string;
  formateurIds: number[];
  prerequisIds: number[];
}

const MOCK_FORMATEURS: FormateurInfo[] = [
  { id: 3, firstName: 'Claire', lastName: 'Lefevre' },
];

let MOCK_DATA: MockCours[] = [
  { id: 1, name: 'TypeScript Bases', formateurIds: [3], prerequisIds: [] },
  { id: 2, name: 'Angular Avancé', formateurIds: [3], prerequisIds: [1] },
  { id: 3, name: 'HTML/CSS Fondamentaux', formateurIds: [], prerequisIds: [] },
  { id: 4, name: 'Angular Bases', formateurIds: [3], prerequisIds: [1, 3] },
  { id: 5, name: 'Spring Boot Avancé', formateurIds: [], prerequisIds: [6] },
  { id: 6, name: 'Java Bases', formateurIds: [], prerequisIds: [] },
];

@Injectable({ providedIn: 'root' })
export class MockCoursAdapter extends BaseCoursAdapter {
  getAll(): Observable<Cours[]> {
    return of(MOCK_DATA.map(c => this.toResponse(c)));
  }

  create(req: CreateCoursRequest): Observable<Cours> {
    const created: MockCours = {
      id: Math.max(...MOCK_DATA.map(c => c.id), 0) + 1,
      name: req.name,
      formateurIds: req.formateurIds ?? [],
      prerequisIds: req.prerequisIds ?? [],
    };
    MOCK_DATA = [...MOCK_DATA, created];
    return of(this.toResponse(created));
  }

  delete(id: number): Observable<void> {
    MOCK_DATA = MOCK_DATA
      .filter(c => c.id !== id)
      .map(c => ({ ...c, prerequisIds: c.prerequisIds.filter(p => p !== id) }));
    return of(undefined);
  }

  setFormateurs(id: number, formateurIds: number[]): Observable<Cours> {
    const cours = this.findCours(id);
    const updated = { ...cours, formateurIds };
    this.replaceCours(updated);
    return of(this.toResponse(updated));
  }

  setPrerequis(id: number, prerequisIds: number[]): Observable<Cours> {
    const cours = this.findCours(id);

    for (const prereqId of prerequisIds) {
      if (this.wouldCreateCycle(id, prereqId)) {
        return throwError(() => ({
          status: 422,
          error: `Affecter le cours ${prereqId} comme prérequis de "${cours.name}" créerait un cycle.`,
        }));
      }
    }

    const updated = { ...cours, prerequisIds };
    this.replaceCours(updated);
    return of(this.toResponse(updated));
  }

  /**
   * Vérifie si rendre `prereqId` prérequis de `coursId` créerait un cycle,
   * c'est-à-dire si `coursId` est déjà (directement ou transitivement)
   * un prérequis de `prereqId`.
   */
  private wouldCreateCycle(coursId: number, prereqId: number): boolean {
    if (coursId === prereqId) return true;

    const visited = new Set<number>();
    const stack = [prereqId];

    while (stack.length > 0) {
      const current = stack.pop()!;
      if (current === coursId) return true;
      if (visited.has(current)) continue;
      visited.add(current);

      const currentCours = MOCK_DATA.find(c => c.id === current);
      if (currentCours) {
        stack.push(...currentCours.prerequisIds);
      }
    }

    return false;
  }

  private toResponse(cours: MockCours): Cours {
    return {
      id: cours.id,
      name: cours.name,
      formateurs: MOCK_FORMATEURS.filter(f => cours.formateurIds.includes(f.id)),
      prerequis: cours.prerequisIds.map(pid => {
        const prereq = MOCK_DATA.find(c => c.id === pid);
        return prereq ? this.toResponse(prereq) : null;
      }).filter((c): c is Cours => c !== null),
    };
  }

  private findCours(id: number): MockCours {
    const cours = MOCK_DATA.find(c => c.id === id);
    if (!cours) {
      throw new Error(`Cours ${id} not found`);
    }
    return cours;
  }

  private replaceCours(updated: MockCours): void {
    MOCK_DATA = MOCK_DATA.map(c => (c.id === updated.id ? updated : c));
  }
}
