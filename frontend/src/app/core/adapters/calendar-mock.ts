import { Observable,of } from "rxjs";
import { CalendarEvent } from "../models/calendar-event.model";
import { BaseCalendarAdapter } from "./calendar.adapter";
import { Injectable } from "@angular/core";

const MOCK_EVENTS: CalendarEvent[] = [
    { id: 1, startDate: new Date(2026, 5, 8, 9, 0), endDate: new Date(2026, 5, 8, 12, 0), userId: 1, cours: 'Angular Avancé', promotion: 'Promo 2026' },
    { id: 2, startDate: new Date(2026, 5, 9, 14, 0), endDate: new Date(2026, 5, 9, 17, 0), userId: 2, cours: 'Spring Boot', promotion: 'Promo 2026' },
    { id: 3, startDate: new Date(2026, 5, 15, 9, 0), endDate: new Date(2026, 5, 15, 11, 0), userId: 1, cours: 'TypeScript Bases' },
    { id: 4, startDate: new Date(2026, 5, 22, 10, 0), endDate: new Date(2026, 5, 22, 16, 0), userId: 3, cours: 'Docker', promotion: 'Promo 2025' },
    { id: 5, startDate: new Date(2026, 6, 1, 9, 0), endDate: new Date(2026, 6, 1, 12, 0), userId: 2, cours: 'CI/CD Pipeline' },
  ];


@Injectable({providedIn: 'root'})
export class MockCalendarAdapter extends BaseCalendarAdapter {
    getEvents(view: 'month' | 'week' | 'day' | 'year', referenceDate: Date): Observable<CalendarEvent[]> {
        const {start, end} = this.getDateRange(view, referenceDate);
        return of(MOCK_EVENTS.filter(event => event.startDate >= start && event.startDate < end));
    }
}
