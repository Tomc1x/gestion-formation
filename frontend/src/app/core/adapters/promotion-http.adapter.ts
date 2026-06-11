import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { PlanningCreateRequest, PlanningUpdateRequest, Promotion, PromotionCours, PromotionRequest } from "../models/promotion.model";
import { BasePromotionAdapter } from "./promotion.adapter";

@Injectable({ providedIn: 'root' })
export class HttpPromotionAdapter extends BasePromotionAdapter {
  private readonly http = inject(HttpClient);
  private readonly API = '/api/promotions';

  getAll(): Observable<Promotion[]> {
    return this.http.get<Promotion[]>(this.API);
  }

  getById(id: number): Observable<Promotion> {
    return this.http.get<Promotion>(`${this.API}/${id}`);
  }

  create(req: PromotionRequest): Observable<Promotion> {
    return this.http.post<Promotion>(this.API, req);
  }

  update(id: number, req: PromotionRequest): Observable<Promotion> {
    return this.http.put<Promotion>(`${this.API}/${id}`, req);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${id}`);
  }

  createPlanning(promotionId: number, req: PlanningCreateRequest): Observable<PromotionCours> {
    return this.http.post<PromotionCours>(`${this.API}/${promotionId}/planning`, req);
  }

  updatePlanning(promotionId: number, promotionCoursId: number, req: PlanningUpdateRequest): Observable<PromotionCours> {
    return this.http.put<PromotionCours>(`${this.API}/${promotionId}/planning/${promotionCoursId}`, req);
  }

  deletePlanning(promotionId: number, coursPlanifieId: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${promotionId}/planning/${coursPlanifieId}`);
  }

  addEleve(promotionId: number, eleveId: number): Observable<Promotion> {
    return this.http.post<Promotion>(`${this.API}/${promotionId}/eleves/${eleveId}`, {});
  }

  removeEleve(promotionId: number, eleveId: number): Observable<Promotion> {
    return this.http.delete<Promotion>(`${this.API}/${promotionId}/eleves/${eleveId}`);
  }
}
