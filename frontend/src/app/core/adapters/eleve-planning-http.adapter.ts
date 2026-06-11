import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable, map } from "rxjs";
import { CalendarEvent } from "../models/calendar-event.model";
import { PlanningEleve } from "../models/inscription.model";
import { BaseCalendarAdapter } from "./calendar.adapter";
import { AuthService } from "../services/auth.service";

@Injectable({ providedIn: 'root' })
export class HttpElevePlanningAdapter extends BaseCalendarAdapter {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);

  getEvents(view: 'month' | 'week' | 'day' | 'year', referenceDate: Date): Observable<CalendarEvent[]> {
    const eleveId = this.authService.currentUserId();
    if (eleveId == null) {
      return new Observable<CalendarEvent[]>(subscriber => {
        subscriber.next([]);
        subscriber.complete();
      });
    }

    const { start, end } = this.getDateRange(view, referenceDate);

    return this.http.get<PlanningEleve[]>(`/api/eleves/${eleveId}/planning`).pipe(
      map(sessions => sessions
        .map(session => this.toCalendarEvent(session, eleveId))
        .filter(event => event.startDate >= start && event.startDate < end)
      )
    );
  }

  private toCalendarEvent(session: PlanningEleve, eleveId: number): CalendarEvent {
    return {
      id: session.coursPlanifieId,
      startDate: new Date(session.dateDebut),
      endDate: new Date(session.dateFin),
      userId: eleveId,
      cours: session.coursNom,
      promotion: session.origine === 'PROMOTION' ? session.coursNom : undefined,
      origine: session.origine,
    };
  }
}
