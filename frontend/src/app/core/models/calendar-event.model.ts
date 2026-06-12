export interface CalendarEvent {
    id: number;
    startDate: Date;
    endDate: Date;
    userId: number;
    cours: string;
    promotion?: string;
    origine?: 'PROMOTION' | 'INDIVIDUEL';
  }
