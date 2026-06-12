export interface Rythme {
  semainesCentre: number;
  semainesEntreprise: number;
}

export interface EleveInfo {
  id: number;
  firstName: string;
  lastName: string;
}

export interface PromotionCours {
  id: number;
  coursId: number;
  coursNom: string;
  dateDebut: string;
  dateFin: string;
  ordre: number;
  statut: string;
  formateurId: number | null;
  formateurNom: string | null;
  salle: string | null;
  warnings: string[];
}

export interface Promotion {
  id: number;
  name: string;
  cursusId: number | null;
  cursusNom: string | null;
  dateDebut: string;
  rythme: Rythme | null;
  eleves: EleveInfo[];
  planning: PromotionCours[];
}

export interface PromotionRequest {
  name: string;
  cursusId: number | null;
  dateDebut: string;
  rythme?: Rythme | null;
  eleveIds?: number[];
}

export interface PlanningUpdateRequest {
  dateDebut: string;
  dateFin: string;
  formateurId?: number | null;
  salle?: string | null;
}

export interface PlanningCreateRequest {
  coursId: number;
  dateDebut: string;
  dateFin: string;
  formateurId?: number | null;
  salle?: string | null;
  force?: boolean;
}
