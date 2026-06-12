import {
  Component,
  ChangeDetectionStrategy,
  signal,
  computed,
  inject,
} from '@angular/core';
import { toSignal, toObservable } from '@angular/core/rxjs-interop';
import { combineLatest, switchMap } from 'rxjs';
import {
  format,
  startOfMonth,
  endOfMonth,
  startOfWeek,
  endOfWeek,
  addDays,
  addMonths,
  subMonths,
  addWeeks,
  subWeeks,
  isSameMonth,
} from 'date-fns';
import { fr } from 'date-fns/locale';
import { DatePipe } from '@angular/common';
import { BaseCalendarAdapter } from '../../../core/adapters/calendar.adapter';
import { CalendarEvent } from '../../../core/models/calendar-event.model';

@Component({
  selector: 'app-mon-calendrier',
  imports: [DatePipe],
  templateUrl: './mon-calendrier.html',
  styleUrl: './mon-calendrier.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MonCalendrierComponent {
  private readonly adapter = inject(BaseCalendarAdapter);

  readonly view = signal<'month' | 'week'>('month');
  readonly referenceDate = signal<Date>(new Date());

  // Load events reactively when referenceDate or view changes
  private readonly events$ = combineLatest([
    toObservable(this.referenceDate),
    toObservable(this.view),
  ]).pipe(
    switchMap(([date, view]) => this.adapter.getEvents(view, date))
  );

  readonly events = toSignal(this.events$, { initialValue: [] as CalendarEvent[] });

  // Month grid: 6 rows × 7 cols starting from Monday
  readonly monthGrid = computed<Date[][]>(() => {
    const ref = this.referenceDate();
    const firstDay = startOfMonth(ref);
    const lastDay = endOfMonth(ref);
    const gridStart = startOfWeek(firstDay, { weekStartsOn: 1 });
    const gridEnd = endOfWeek(lastDay, { weekStartsOn: 1 });

    const weeks: Date[][] = [];
    let current = gridStart;
    while (current <= gridEnd) {
      const week: Date[] = [];
      for (let i = 0; i < 7; i++) {
        week.push(current);
        current = addDays(current, 1);
      }
      weeks.push(week);
    }
    return weeks;
  });

  // Week view: 7 days of the current week (Mon–Sun)
  readonly weekDays = computed<Date[]>(() => {
    const ref = this.referenceDate();
    const start = startOfWeek(ref, { weekStartsOn: 1 });
    return Array.from({ length: 7 }, (_, i) => addDays(start, i));
  });

  // Map 'YYYY-MM-DD' -> CalendarEvent[]
  readonly eventsByDay = computed<Map<string, CalendarEvent[]>>(() => {
    const map = new Map<string, CalendarEvent[]>();
    for (const event of this.events()) {
      const key = format(event.startDate, 'yyyy-MM-dd');
      const existing = map.get(key) ?? [];
      map.set(key, [...existing, event]);
    }
    return map;
  });

  // Navigation title
  readonly navTitle = computed<string>(() => {
    const ref = this.referenceDate();
    if (this.view() === 'month') {
      return format(ref, 'MMMM yyyy', { locale: fr });
    }
    const days = this.weekDays();
    const start = days[0];
    const end = days[6];
    if (start.getMonth() === end.getMonth()) {
      return `${format(start, 'd')} – ${format(end, 'd MMMM yyyy', { locale: fr })}`;
    }
    return `${format(start, 'd MMM', { locale: fr })} – ${format(end, 'd MMM yyyy', { locale: fr })}`;
  });

  readonly weekDayLabels = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];

  readonly selectedEvent = signal<CalendarEvent | null>(null);

  selectEvent(event: CalendarEvent): void {
    this.selectedEvent.set(event);
  }

  closeDetails(): void {
    this.selectedEvent.set(null);
  }

  isCurrentMonth(date: Date): boolean {
    return isSameMonth(date, this.referenceDate());
  }

  getEventsForDay(date: Date): CalendarEvent[] {
    const key = format(date, 'yyyy-MM-dd');
    return this.eventsByDay().get(key) ?? [];
  }

  formatEventTime(date: Date): string {
    return format(date, 'HH:mm');
  }

  formatDayHeader(date: Date): string {
    return format(date, 'EEE d', { locale: fr });
  }

  navigatePrev(): void {
    if (this.view() === 'month') {
      this.referenceDate.set(subMonths(this.referenceDate(), 1));
    } else {
      this.referenceDate.set(subWeeks(this.referenceDate(), 1));
    }
  }

  navigateNext(): void {
    if (this.view() === 'month') {
      this.referenceDate.set(addMonths(this.referenceDate(), 1));
    } else {
      this.referenceDate.set(addWeeks(this.referenceDate(), 1));
    }
  }

  setView(v: 'month' | 'week'): void {
    this.view.set(v);
  }
}
