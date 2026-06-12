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
}

export interface InscritCours {
  eleveId: number;
  firstName: string;
  lastName: string;
  origine: OrigineInscription;
}
