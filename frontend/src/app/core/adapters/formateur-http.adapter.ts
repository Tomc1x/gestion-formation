import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { BaseFormateurAdapter, FormateurInfo } from "./formateur.adapter";

@Injectable({ providedIn: 'root' })
export class HttpFormateurAdapter extends BaseFormateurAdapter {
  private readonly http = inject(HttpClient);
  private readonly API = '/api/formateurs';

  getAll(): Observable<FormateurInfo[]> {
    return this.http.get<FormateurInfo[]>(this.API);
  }
}
