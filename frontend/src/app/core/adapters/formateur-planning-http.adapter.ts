import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable, map } from "rxjs";
import { CalendarEvent } from "../models/calendar-event.model";
import { PlanningFormateur } from "../models/inscription.model";
import { BaseCalendarAdapter } from "./calendar.adapter";
import { AuthService } from "../services/auth.service";

@Injectable({ providedIn: 'root' })
export class HttpFormateurPlanningAdapter extends BaseCalendarAdapter {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);

  getEvents(view: 'month' | 'week' | 'day' | 'year', referenceDate: Date): Observable<CalendarEvent[]> {
    const formateurId = this.authService.currentUserId();
    if (formateurId == null) {
      return new Observable<CalendarEvent[]>(subscriber => {
        subscriber.next([]);
        subscriber.complete();
      });
    }

    const { start, end } = this.getDateRange(view, referenceDate);

    return this.http.get<PlanningFormateur[]>(`/api/formateurs/${formateurId}/planning`).pipe(
      map(sessions => sessions
        .map(session => this.toCalendarEvent(session, formateurId))
        .filter(event => event.startDate >= start && event.startDate < end)
      )
    );
  }

  private toCalendarEvent(session: PlanningFormateur, formateurId: number): CalendarEvent {
    return {
      id: session.coursPlanifieId,
      startDate: new Date(session.dateDebut),
      endDate: new Date(session.dateFin),
      userId: formateurId,
      cours: session.coursNom,
      salle: session.salle,
      eleves: session.eleves.map(e => ({ eleveId: e.eleveId, firstName: e.firstName, lastName: e.lastName })),
    };
  }
}
