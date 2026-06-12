import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { CreateCursusRequest, Cursus } from "../models/cursus.model";
import { BaseCursusAdapter } from "./cursus.adapter";

@Injectable({ providedIn: 'root' })
export class HttpCursusAdapter extends BaseCursusAdapter {
  private readonly http = inject(HttpClient);
  private readonly API = '/api/cursus';

  getAll(): Observable<Cursus[]> {
    return this.http.get<Cursus[]>(this.API);
  }

  create(req: CreateCursusRequest): Observable<Cursus> {
    return this.http.post<Cursus>(this.API, req);
  }

  update(cursusId: number, req: CreateCursusRequest): Observable<Cursus> {
    return this.http.put<Cursus>(`${this.API}/${cursusId}`, req);
  }

  delete(cursusId: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${cursusId}`);
  }

  addCours(cursusId: number, coursId: number, ordre?: number): Observable<Cursus> {
    return this.http.post<Cursus>(`${this.API}/${cursusId}/cours`, { coursId, ordre });
  }

  removeCours(cursusId: number, coursId: number): Observable<Cursus> {
    return this.http.delete<Cursus>(`${this.API}/${cursusId}/cours/${coursId}`);
  }

  reorder(cursusId: number, coursIds: number[]): Observable<Cursus> {
    return this.http.put<Cursus>(`${this.API}/${cursusId}/cours/reorder`, { coursIds });
  }
}
