import { Observable, of, throwError } from "rxjs";
import { Injectable } from "@angular/core";
import { CreateFiliereRequest, Filiere } from "../models/cursus.model";
import { BaseFiliereAdapter } from "./filiere.adapter";
import { countCursusByFiliere } from "./cursus-mock";

let MOCK_DATA: Filiere[] = [
  { id: 1, name: 'Développement Web' },
  { id: 2, name: 'Infrastructure & Réseaux' },
  { id: 3, name: 'Data & IA' },
];

@Injectable({ providedIn: 'root' })
export class MockFiliereAdapter extends BaseFiliereAdapter {
  getAll(): Observable<Filiere[]> {
    return of(MOCK_DATA);
  }

  create(req: CreateFiliereRequest): Observable<Filiere> {
    const created: Filiere = {
      id: Math.max(...MOCK_DATA.map(f => f.id), 0) + 1,
      name: req.name,
    };
    MOCK_DATA = [...MOCK_DATA, created];
    return of(created);
  }

  update(id: number, req: CreateFiliereRequest): Observable<Filiere> {
    const updated: Filiere = { id, name: req.name };
    MOCK_DATA = MOCK_DATA.map(f => (f.id === id ? updated : f));
    return of(updated);
  }

  delete(id: number): Observable<void> {
    const count = countCursusByFiliere(id);
    if (count > 0) {
      return throwError(() => ({ status: 409, error: `Cette filière est utilisée par ${count} cursus.` }));
    }
    MOCK_DATA = MOCK_DATA.filter(f => f.id !== id);
    return of(undefined);
  }
}
