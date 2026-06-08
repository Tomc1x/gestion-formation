import { Observable } from "rxjs";
import { sub,add } from "date-fns";
import { CalendarEvent } from "../models/calendar-event.model";
export abstract class BaseCalendarAdapter {
    abstract getEvents(view: 'month' | 'week' | 'day' | 'year', referenceDate: Date): Observable<CalendarEvent[]>;

    getDateRange(view: 'month' | 'week' | 'day' | 'year', referenceDate: Date): { start: Date; end: Date } {

        switch (view) {
            case 'week':
                const startOfWeek = sub(referenceDate, { days: (referenceDate.getDay() + 6 ) % 7 });
                const endOfWeek = add(startOfWeek, { days: 6, hours: 23, minutes: 59, seconds: 59 });
                return { start: startOfWeek, end: endOfWeek };
            case 'month':
                const startOfMonth = new Date(referenceDate.getFullYear(), referenceDate.getMonth(), 1);
                const endOfMonth = add(startOfMonth, { months: 1, days: -1, hours: 23, minutes: 59, seconds: 59 });
                return { start: startOfMonth, end: endOfMonth };
            case 'day':
                const startOfDay = new Date(referenceDate.getFullYear(), referenceDate.getMonth(), referenceDate.getDate());
                const endOfDay = add(startOfDay, { hours: 23, minutes: 59, seconds: 59 });
                return { start: startOfDay, end: endOfDay };
            case 'year':
                const startOfYear = new Date(referenceDate.getFullYear(), 0, 1);
                const endOfYear = add(startOfYear, { months: 11, days: 30, hours: 23, minutes: 59, seconds: 59 });
                return { start: startOfYear, end: endOfYear };
            default:
                return { start: new Date(), end: new Date() };
        }

    }
}
