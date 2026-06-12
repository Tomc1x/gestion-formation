export type OrigineInscription = 'PROMOTION' | 'INDIVIDUEL';

export interface PlanningEleve {
  coursPlanifieId: number;
  coursId: number;
  coursNom: string;
  dateDebut: string;
  dateFin: string;
  ordre: number;
  statut: 'PLANIFIE' | 'EN_COURS' | 'TERMINE';
  origine: OrigineInscription;
  formateurId: number | null;
  formateurNom: string;
}

export interface PlanningFormateur {
  coursPlanifieId: number;
  coursId: number;
  coursNom: string;
  dateDebut: string;
  dateFin: string;
  ordre: number;
  statut: 'PLANIFIE' | 'EN_COURS' | 'TERMINE';
  salle: string | null;
  eleves: InscritCours[];
}

export interface InscritCours {
  eleveId: number;
  firstName: string;
  lastName: string;
  origine: OrigineInscription;
}

export interface CreerInscriptionRequest {
  eleveId: number;
  forcer?: boolean;
}

export interface InscriptionCours {
  id: number;
  eleveId: number;
  coursPlanifieId: number;
  dateInscription: string;
  warnings: string[];
}
