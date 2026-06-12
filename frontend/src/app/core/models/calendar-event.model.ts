export interface CalendarEvent {
    id: number;
    startDate: Date;
    endDate: Date;
    userId: number;
    cours: string;
    promotion?: string;
    origine?: 'PROMOTION' | 'INDIVIDUEL';
    formateurNom?: string;
    salle?: string | null;
    eleves?: { eleveId: number; firstName: string; lastName: string }[];
  }
