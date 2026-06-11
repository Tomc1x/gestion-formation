import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { CreateFiliereRequest, Filiere } from "../models/cursus.model";
import { BaseFiliereAdapter } from "./filiere.adapter";

@Injectable({ providedIn: 'root' })
export class HttpFiliereAdapter extends BaseFiliereAdapter {
  private readonly http = inject(HttpClient);
  private readonly API = '/api/filiere';

  getAll(): Observable<Filiere[]> {
    return this.http.get<Filiere[]>(this.API);
  }

  create(req: CreateFiliereRequest): Observable<Filiere> {
    return this.http.post<Filiere>(this.API, req);
  }

  update(id: number, req: CreateFiliereRequest): Observable<Filiere> {
    return this.http.put<Filiere>(`${this.API}/${id}`, req);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${id}`);
  }
}
