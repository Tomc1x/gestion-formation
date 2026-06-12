import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { BaseEleveAdapter, EleveInfo } from "./eleve.adapter";

@Injectable({ providedIn: 'root' })
export class HttpEleveAdapter extends BaseEleveAdapter {
  private readonly http = inject(HttpClient);
  private readonly API = '/api/eleves';

  getAll(): Observable<EleveInfo[]> {
    return this.http.get<EleveInfo[]>(this.API);
  }
}
