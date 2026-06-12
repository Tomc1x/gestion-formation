import { Observable, of, throwError } from "rxjs";
import { Injectable } from "@angular/core";
import { CoursInCursus, CreateCursusRequest, Cursus } from "../models/cursus.model";
import { BaseCursusAdapter } from "./cursus.adapter";
import { FormateurInfo } from "../models/cours.model";

interface MockCoursRef {
  id: number;
  name: string;
  formateurs: FormateurInfo[];
}

interface MockCursus {
  id: number;
  name: string;
  filiereId: number | null;
  filiereName: string | null;
  cours: { coursId: number; ordre: number }[];
}

const MOCK_FILIERES = [
  { id: 1, name: 'Développement Web' },
  { id: 2, name: 'Infrastructure & Réseaux' },
  { id: 3, name: 'Data & IA' },
];

const FORMATEUR_CLAIRE: FormateurInfo = { id: 3, firstName: 'Claire', lastName: 'Lefevre' };

const MOCK_COURS: MockCoursRef[] = [
  { id: 1, name: 'TypeScript Bases', formateurs: [FORMATEUR_CLAIRE] },
  { id: 2, name: 'Angular Avancé', formateurs: [FORMATEUR_CLAIRE] },
  { id: 3, name: 'HTML/CSS Fondamentaux', formateurs: [] },
  { id: 4, name: 'Angular Bases', formateurs: [FORMATEUR_CLAIRE] },
  { id: 5, name: 'Spring Boot Avancé', formateurs: [] },
  { id: 6, name: 'Java Bases', formateurs: [] },
];

let MOCK_DATA: MockCursus[] = [
  {
    id: 1,
    name: 'Développeur Front-End',
    filiereId: 1,
    filiereName: 'Développement Web',
    cours: [
      { coursId: 3, ordre: 1 },
      { coursId: 1, ordre: 2 },
      { coursId: 4, ordre: 3 },
      { coursId: 2, ordre: 4 },
    ],
  },
  {
    id: 2,
    name: 'Développeur Back-End Java',
    filiereId: 1,
    filiereName: 'Développement Web',
    cours: [
      { coursId: 6, ordre: 1 },
      { coursId: 5, ordre: 2 },
    ],
  },
  {
    id: 3,
    name: 'Cursus Data',
    filiereId: 3,
    filiereName: 'Data & IA',
    cours: [],
  },
];

export function countCursusByFiliere(filiereId: number): number {
  return MOCK_DATA.filter(c => c.filiereId === filiereId).length;
}

@Injectable({ providedIn: 'root' })
export class MockCursusAdapter extends BaseCursusAdapter {
  getAll(): Observable<Cursus[]> {
    return of(MOCK_DATA.map(c => this.toResponse(c)));
  }

  create(req: CreateCursusRequest): Observable<Cursus> {
    const created: MockCursus = {
      id: Math.max(...MOCK_DATA.map(c => c.id), 0) + 1,
      name: req.name,
      filiereId: req.filiereId,
      filiereName: null,
      cours: [],
    };
    MOCK_DATA = [...MOCK_DATA, created];
    return of(this.toResponse(created));
  }

  update(cursusId: number, req: CreateCursusRequest): Observable<Cursus> {
    const cursus = this.findCursus(cursusId);
    const dup = MOCK_DATA.find(c => c.id !== cursusId && c.name === req.name);
    if (dup) {
      return throwError(() => ({ status: 422, error: `Un cursus avec le nom '${req.name}' existe déjà.` }));
    }
    const filiereName = MOCK_FILIERES.find(f => f.id === req.filiereId)?.name ?? null;
    const updated: MockCursus = {
      ...cursus,
      name: req.name,
      filiereId: req.filiereId ?? null,
      filiereName,
    };
    this.replace(updated);
    return of(this.toResponse(updated));
  }

  delete(cursusId: number): Observable<void> {
    this.findCursus(cursusId);
    MOCK_DATA = MOCK_DATA.filter(c => c.id !== cursusId);
    return of(undefined);
  }

  addCours(cursusId: number, coursId: number, ordre?: number): Observable<Cursus> {
    const cursus = this.findCursus(cursusId);
    const finalOrdre = ordre ?? cursus.cours.length + 1;
    const updated: MockCursus = {
      ...cursus,
      cours: [...cursus.cours.filter(c => c.coursId !== coursId), { coursId, ordre: finalOrdre }]
        .sort((a, b) => a.ordre - b.ordre),
    };
    this.replace(updated);
    return of(this.toResponse(updated));
  }

  removeCours(cursusId: number, coursId: number): Observable<Cursus> {
    const cursus = this.findCursus(cursusId);
    const updated: MockCursus = {
      ...cursus,
      cours: cursus.cours
        .filter(c => c.coursId !== coursId)
        .map((c, i) => ({ coursId: c.coursId, ordre: i + 1 })),
    };
    this.replace(updated);
    return of(this.toResponse(updated));
  }

  reorder(cursusId: number, coursIds: number[]): Observable<Cursus> {
    const cursus = this.findCursus(cursusId);
    if (coursIds.length !== cursus.cours.length) {
      return throwError(() => ({ status: 422, error: 'La liste de réordonnancement doit contenir tous les cours du cursus.' }));
    }
    const updated: MockCursus = {
      ...cursus,
      cours: coursIds.map((coursId, i) => ({ coursId, ordre: i + 1 })),
    };
    this.replace(updated);
    return of(this.toResponse(updated));
  }

  private toResponse(cursus: MockCursus): Cursus {
    const cours: CoursInCursus[] = [...cursus.cours]
      .sort((a, b) => a.ordre - b.ordre)
      .map(c => {
        const ref = MOCK_COURS.find(m => m.id === c.coursId);
        return {
          id: c.coursId,
          name: ref?.name ?? `Cours ${c.coursId}`,
          ordre: c.ordre,
          formateurs: ref?.formateurs ?? [],
        };
      });
    return {
      id: cursus.id,
      name: cursus.name,
      filiereId: cursus.filiereId,
      filiereName: cursus.filiereName,
      cours,
    };
  }

  private findCursus(id: number): MockCursus {
    const cursus = MOCK_DATA.find(c => c.id === id);
    if (!cursus) {
      throw new Error(`Cursus ${id} not found`);
    }
    return cursus;
  }

  private replace(updated: MockCursus): void {
    MOCK_DATA = MOCK_DATA.map(c => (c.id === updated.id ? updated : c));
  }
}
