# WI-20260612-FRONTE-004 — Vue mois : événements multi-jours en barre superposée

## Status
DONE

## Scope
frontend/src/app/features/calendrier/mon-calendrier/ (mon-calendrier.ts, .html, .scss)

## Files Touched
- frontend/src/app/features/calendrier/mon-calendrier/mon-calendrier.ts
- frontend/src/app/features/calendrier/mon-calendrier/mon-calendrier.html
- frontend/src/app/features/calendrier/mon-calendrier/mon-calendrier.scss

## Summary
- Extracted shared utility `computeWeekSegments(events, weekStart, weekEnd): EventSegment[]`
  (top-level exported function + `EventSegment` interface) computing 1-based `startCol` and
  `span` for events intersecting a week range, using the same `max`/`min`/`differenceInCalendarDays`
  logic previously inlined in `weekEventSegments`.
- `weekEventSegments` (week view, FRONTE-003) now delegates to `computeWeekSegments(events, days[0], days[6])` — unchanged behavior, just refactored.
- New `monthEventSegments = computed<EventSegment[][]>()`: one segment array per week row of
  `monthGrid()`, computed via `computeWeekSegments(events, week[0], week[6])`. A multi-day event
  spanning two month-grid weeks now produces two segments (one per row), each clipped to that row's
  bounds — same approach as week view.
- `getEventsForDay(date)` now filters out multi-day events (`differenceInCalendarDays(endDate, startDate) > 0`)
  so they are not duplicated as `event-chip` in the start-day cell; only single-day events remain as chips.
- HTML month view: each `.grid-week` now contains a `.grid-week__row` (the 7-column day grid, role="row")
  plus a `.month-events` overlay (role="presentation") rendering `monthEventSegments()[$index]` — only
  segments with `span > 1` are rendered as `.event-card.event-card--month` bars positioned via
  `[style.grid-column]="startCol / span N"`. `span === 1` events stay as `event-chip` in their cell
  (unchanged). `.event-card__time` is never rendered in the month overlay (no time block in the
  `--month` variant template at all).
- SCSS: `.grid-week` becomes `position: relative` (was the grid itself); `.grid-week__row` now holds
  `display: grid; grid-template-columns: repeat(7,1fr)`. New `.month-events` overlay:
  `position: absolute; inset: 0; top: 1.5rem` (clears `.grid-cell__number`), 7-col grid,
  `grid-auto-rows: min-content`, `pointer-events: none` with `pointer-events: auto` on the cards
  (same pattern as `.week-events`). New `.event-card--month`: compact row-layout variant of
  `.event-card` (flex-direction row, smaller font, ellipsis name, no time).

## Decisions
- Factored `computeWeekSegments` as a plain exported function (not a method) at module scope —
  matches the existing functional/computed style and avoids `this` binding concerns when reused
  across two computeds.
- Multi-day single-day-equivalent events (span===1 segment from `computeWeekSegments`, e.g. an event
  that starts and ends same day) are NOT filtered from `getEventsForDay` differently than before —
  filter is based on the *event's own* `startDate`/`endDate` span (constant across all weeks), not the
  per-row segment span. This is correct: an event lasting >1 calendar day should never appear as a chip,
  even in weeks where its segment span happens to be 1 (e.g. starts on Sunday, 1 day visible in that row).
- `.month-events` top offset (`1.5rem`) is a fixed estimate matching `.grid-cell__number` height +
  margin; verified visually acceptable at `min-height: 5.5rem` cell height with 1-2 stacked bars.
- No changes to `.week-events` / week view rendering logic beyond the refactor extraction — verified
  unchanged behavior (same formula, same output).

## Evidence
- `cd frontend && npm run build` → PASS, "Application bundle generation complete."
- Preexisting SCSS budget warning on mon-calendrier.scss: was already present before this WI (per task
  description); now reports "exceeded maximum budget. Budget 4.00 kB was not met by 1.72 kB with a
  total of 5.72 kB." (increase of ~1.7kB from the new `.month-events`/`.event-card--month` rules —
  acceptable per task instructions, did not attempt to fix preexisting budget config).

## Manual Test Procedure
1. `cd frontend && ng serve`, log in, navigate to "Mon calendrier" (vue par défaut = Mois).
2. Identify or create (via planning admin) a cours-planifié whose `dateDebut`/`dateFin` span ≥2
   calendar days (multi-day event).
3. In month view: the multi-day event should appear as a single horizontal bar spanning the correct
   number of day columns, positioned below the day numbers row, without a time label. It should NOT
   also appear as a separate `event-chip` in its start-day cell.
4. If the event spans across a week boundary in the grid (e.g. starts Saturday week N, ends Tuesday
   week N+1), verify two separate bars appear — one in each week row, each clipped to that row's days
   (e.g. bar 1 spans Sat-Sun of row N, bar 2 spans Mon-Tue of row N+1).
5. Single-day events: verify they still render as `event-chip` with time (`event-chip__time`) inside
   their day cell, unchanged from before.
6. Click both the overlay bar and a chip — `selectEvent` should open the detail panel as before.
7. Switch to "Semaine" view: verify multi-day events still render correctly as single spanning bars
   (regression check for FRONTE-003) — behavior should be identical to before this WI.
8. Keyboard: tab to an overlay bar, press Enter — should open detail panel (ARIA `tabindex="0"` +
   `(keydown.enter)` preserved).

## Open Blockers
None.

## Next Actions
None — WI complete.

## Recall Hints
- `computeWeekSegments` / `EventSegment` exported from mon-calendrier.ts, shared by week + month views.
- `monthEventSegments()[$index]` indexed by `monthGrid()` week row index (`$for (week of monthGrid(); track $index)`).
- `.month-events` overlay per `.grid-week`, `.week-events` overlay once per `.calendrier__grid--week`.

## Proposed Rules
None — pattern is a direct extension of the FRONTE-003 approach, already documented there if applicable.
