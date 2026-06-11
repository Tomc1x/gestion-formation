import { Observable, of, throwError } from "rxjs";
import { Injectable } from "@angular/core";
import { PlanningCreateRequest, PlanningUpdateRequest, Promotion, PromotionCours, PromotionRequest, Rythme } from "../models/promotion.model";
import { BasePromotionAdapter } from "./promotion.adapter";

interface MockEleve {
  id: number;
  firstName: string;
  lastName: string;
}

const MOCK_ELEVES: MockEleve[] = [
  { id: 10, firstName: 'Lucas', lastName: 'Martin' },
  { id: 11, firstName: 'Emma', lastName: 'Bernard' },
  { id: 12, firstName: 'Hugo', lastName: 'Petit' },
];

const MOCK_FORMATEURS: { id: number; nom: string }[] = [
  { id: 20, nom: 'Marie Curie' },
  { id: 21, nom: 'Albert Dupont' },
];

interface MockPromotion {
  id: number;
  name: string;
  cursusId: number | null;
  cursusNom: string | null;
  dateDebut: string;
  rythme: Rythme | null;
  eleveIds: number[];
  planning: PromotionCours[];
}

let MOCK_DATA: MockPromotion[] = [
  {
    id: 1,
    name: 'Promo Front-End 2026',
    cursusId: 1,
    cursusNom: 'Développeur Front-End',
    dateDebut: '2026-09-01',
    rythme: { semainesCentre: 3, semainesEntreprise: 1 },
    eleveIds: [10, 11],
    planning: [
      { id: 100, coursId: 3, coursNom: 'HTML/CSS Fondamentaux', dateDebut: '2026-09-01', dateFin: '2026-09-12', ordre: 1, statut: 'PLANIFIE', formateurId: null, formateurNom: null, salle: null, warnings: [] },
      { id: 101, coursId: 1, coursNom: 'TypeScript Bases', dateDebut: '2026-09-15', dateFin: '2026-09-26', ordre: 2, statut: 'PLANIFIE', formateurId: null, formateurNom: null, salle: null, warnings: [] },
    ],
  },
  {
    id: 2,
    name: 'Promo Back-End Java 2026',
    cursusId: 2,
    cursusNom: 'Développeur Back-End Java',
    dateDebut: '2026-10-01',
    rythme: null,
    eleveIds: [12],
    planning: [
      { id: 102, coursId: 6, coursNom: 'Java Bases', dateDebut: '2026-10-01', dateFin: '2026-10-15', ordre: 1, statut: 'PLANIFIE', formateurId: null, formateurNom: null, salle: null, warnings: [] },
    ],
  },
];

@Injectable({ providedIn: 'root' })
export class MockPromotionAdapter extends BasePromotionAdapter {
  getAll(): Observable<Promotion[]> {
    return of(MOCK_DATA.map(p => this.toResponse(p)));
  }

  getById(id: number): Observable<Promotion> {
    return of(this.toResponse(this.findPromotion(id)));
  }

  create(req: PromotionRequest): Observable<Promotion> {
    const created: MockPromotion = {
      id: Math.max(...MOCK_DATA.map(p => p.id), 0) + 1,
      name: req.name,
      cursusId: req.cursusId,
      cursusNom: null,
      dateDebut: req.dateDebut,
      rythme: req.rythme ?? null,
      eleveIds: req.eleveIds ?? [],
      planning: [],
    };
    MOCK_DATA = [...MOCK_DATA, created];
    return of(this.toResponse(created));
  }

  update(id: number, req: PromotionRequest): Observable<Promotion> {
    const promotion = this.findPromotion(id);
    const updated: MockPromotion = {
      ...promotion,
      name: req.name,
      cursusId: req.cursusId,
      dateDebut: req.dateDebut,
      rythme: req.rythme ?? null,
      eleveIds: req.eleveIds ?? [],
    };
    this.replace(updated);
    return of(this.toResponse(updated));
  }

  delete(id: number): Observable<void> {
    if (!MOCK_DATA.some(p => p.id === id)) {
      return throwError(() => ({ status: 404, error: 'Promotion introuvable.' }));
    }
    MOCK_DATA = MOCK_DATA.filter(p => p.id !== id);
    return of(undefined);
  }

  createPlanning(promotionId: number, req: PlanningCreateRequest): Observable<PromotionCours> {
    const promotion = this.findPromotion(promotionId);
    const ordre = Math.max(0, ...promotion.planning.map(pc => pc.ordre)) + 1;
    const created: PromotionCours = {
      id: Math.max(0, ...MOCK_DATA.flatMap(p => p.planning.map(pc => pc.id))) + 1,
      coursId: req.coursId,
      coursNom: `Cours #${req.coursId}`,
      dateDebut: req.dateDebut,
      dateFin: req.dateFin,
      ordre,
      statut: 'PLANIFIE',
      formateurId: req.formateurId ?? null,
      formateurNom: req.formateurId ? (MOCK_FORMATEURS.find(f => f.id === req.formateurId)?.nom ?? null) : null,
      salle: req.salle ?? null,
      warnings: [],
    };
    this.replace({ ...promotion, planning: [...promotion.planning, created] });
    return of(created);
  }

  updatePlanning(promotionId: number, promotionCoursId: number, req: PlanningUpdateRequest): Observable<PromotionCours> {
    const promotion = this.findPromotion(promotionId);
    const planning = promotion.planning.map(pc =>
      pc.id === promotionCoursId
        ? {
            ...pc,
            dateDebut: req.dateDebut,
            dateFin: req.dateFin,
            formateurId: req.formateurId ?? null,
            formateurNom: req.formateurId ? (MOCK_FORMATEURS.find(f => f.id === req.formateurId)?.nom ?? null) : null,
            salle: req.salle ?? null,
          }
        : pc
    );
    this.replace({ ...promotion, planning });
    const updated = planning.find(pc => pc.id === promotionCoursId);
    if (!updated) {
      return throwError(() => ({ status: 404, error: 'Cours de promotion introuvable.' }));
    }
    return of(updated);
  }

  deletePlanning(promotionId: number, coursPlanifieId: number): Observable<void> {
    const promotion = this.findPromotion(promotionId);
    if (!promotion.planning.some(pc => pc.id === coursPlanifieId)) {
      return throwError(() => ({ status: 404, error: 'Cours planifié introuvable.' }));
    }
    this.replace({ ...promotion, planning: promotion.planning.filter(pc => pc.id !== coursPlanifieId) });
    return of(undefined);
  }

  addEleve(promotionId: number, eleveId: number): Observable<Promotion> {
    const promotion = this.findPromotion(promotionId);
    if (!promotion.eleveIds.includes(eleveId)) {
      this.replace({ ...promotion, eleveIds: [...promotion.eleveIds, eleveId] });
    }
    return of(this.toResponse(this.findPromotion(promotionId)));
  }

  removeEleve(promotionId: number, eleveId: number): Observable<Promotion> {
    const promotion = this.findPromotion(promotionId);
    this.replace({ ...promotion, eleveIds: promotion.eleveIds.filter(id => id !== eleveId) });
    return of(this.toResponse(this.findPromotion(promotionId)));
  }

  private toResponse(promotion: MockPromotion): Promotion {
    return {
      id: promotion.id,
      name: promotion.name,
      cursusId: promotion.cursusId,
      cursusNom: promotion.cursusNom,
      dateDebut: promotion.dateDebut,
      rythme: promotion.rythme,
      eleves: MOCK_ELEVES.filter(e => promotion.eleveIds.includes(e.id)),
      planning: promotion.planning,
    };
  }

  private findPromotion(id: number): MockPromotion {
    const promotion = MOCK_DATA.find(p => p.id === id);
    if (!promotion) {
      throw new Error(`Promotion ${id} not found`);
    }
    return promotion;
  }

  private replace(updated: MockPromotion): void {
    MOCK_DATA = MOCK_DATA.map(p => (p.id === updated.id ? updated : p));
  }
}
