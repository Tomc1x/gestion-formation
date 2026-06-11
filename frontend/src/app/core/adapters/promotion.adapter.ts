import { Observable } from "rxjs";
import { PlanningCreateRequest, PlanningUpdateRequest, Promotion, PromotionCours, PromotionRequest } from "../models/promotion.model";

export abstract class BasePromotionAdapter {
  abstract getAll(): Observable<Promotion[]>;

  abstract getById(id: number): Observable<Promotion>;

  abstract create(req: PromotionRequest): Observable<Promotion>;

  abstract update(id: number, req: PromotionRequest): Observable<Promotion>;

  abstract delete(id: number): Observable<void>;

  abstract createPlanning(promotionId: number, req: PlanningCreateRequest): Observable<PromotionCours>;

  abstract updatePlanning(promotionId: number, promotionCoursId: number, req: PlanningUpdateRequest): Observable<PromotionCours>;

  abstract addEleve(promotionId: number, eleveId: number): Observable<Promotion>;

  abstract removeEleve(promotionId: number, eleveId: number): Observable<Promotion>;
}
