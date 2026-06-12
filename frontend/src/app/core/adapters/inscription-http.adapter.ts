import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { CreerInscriptionRequest, InscriptionCours, InscritCours } from "../models/inscription.model";
import { BaseInscriptionAdapter } from "./inscription.adapter";

@Injectable({ providedIn: 'root' })
export class HttpInscriptionAdapter extends BaseInscriptionAdapter {
  private readonly http = inject(HttpClient);
  private readonly API = '/api/cours-planifies';

  getInscrits(coursPlanifieId: number): Observable<InscritCours[]> {
    return this.http.get<InscritCours[]>(`${this.API}/${coursPlanifieId}/inscrits`);
  }

  creerInscription(coursPlanifieId: number, request: CreerInscriptionRequest): Observable<InscriptionCours> {
    return this.http.post<InscriptionCours>(`${this.API}/${coursPlanifieId}/inscriptions`, request);
  }
}
