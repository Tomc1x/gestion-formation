import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { Cours, CreateCoursRequest } from "../models/cours.model";
import { BaseCoursAdapter } from "./cours.adapter";

@Injectable({ providedIn: 'root' })
export class HttpCoursAdapter extends BaseCoursAdapter {
  private readonly http = inject(HttpClient);
  private readonly API = '/api/cours';

  getAll(): Observable<Cours[]> {
    return this.http.get<Cours[]>(this.API);
  }

  create(req: CreateCoursRequest): Observable<Cours> {
    return this.http.post<Cours>(this.API, req);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${id}`);
  }

  setFormateurs(id: number, formateurIds: number[]): Observable<Cours> {
    return this.http.put<Cours>(`${this.API}/${id}/formateurs`, formateurIds);
  }

  setPrerequis(id: number, prerequisIds: number[]): Observable<Cours> {
    return this.http.put<Cours>(`${this.API}/${id}/prerequis`, prerequisIds);
  }
}
