# WI-20260612-FRONTE-003 — Multi-day events in week view

## Status
DONE

## Scope
frontend/src/app/features/calendrier/mon-calendrier/ (mon-calendrier.ts, .html, .scss) — week view only. Month view untouched (FRONTE-004).

## Files Touched
- frontend/src/app/features/calendrier/mon-calendrier/mon-calendrier.ts
- frontend/src/app/features/calendrier/mon-calendrier/mon-calendrier.html
- frontend/src/app/features/calendrier/mon-calendrier/mon-calendrier.scss

## Changes Summary
- Added `max`, `min`, `differenceInCalendarDays` imports from `date-fns`.
- Added a new computed signal `weekEventSegments()` that, for each event in `events()`, computes whether it intersects the displayed week (`weekDays()[0]`..`weekDays()[6]`). For intersecting events it computes:
  - `segmentStart = max(event.startDate, weekStart)`
  - `segmentEnd = min(event.endDate, weekEnd)`
  - `startCol = differenceInCalendarDays(segmentStart, weekStart) + 1` (1-7, Monday=1)
  - `span = differenceInCalendarDays(segmentEnd, segmentStart) + 1`
  Events with no overlap are excluded. Single-day events naturally get span=1.
- Template (week view): replaced the per-day event loop (`getEventsForDay(day)` inside each `grid-cell--week`) with:
  - An empty `week-body` grid of 7 day cells (kept for visual grid lines/structure).
  - A new sibling `.week-events` grid overlay (`position: absolute; inset: 0`, `grid-template-columns: repeat(7, 1fr)`) that renders ALL events for the week as `event-card` articles, each positioned via `[style.grid-column]="startCol + ' / span ' + span"` (native style binding, no ngStyle).
  - For `segment.span > 1`, the `event-card__time` block is omitted (no start/end time shown). For `span === 1`, the time block is shown as before.
  - `event-card--span` class added when span > 1 (for `justify-self: stretch` so the card fills its spanned columns).
  - `getEventsForDay()` is still used by the month view (untouched).
- SCSS: added `.calendrier__grid--week { position: relative; }`, `.week-events` overlay grid (absolute, `pointer-events: none` on container, `pointer-events: auto` on `.event-card` so day-cell clicks aren't blocked), and `.event-card--span { justify-self: stretch; }`.

## Decisions
- Chose the "single overlay grid" approach (option B from the brief) over keeping per-day rendering for single-day events, to avoid maintaining two separate code paths and avoid duplicate event-card layout logic. All week-view events (single- or multi-day) now render through `weekEventSegments()`.
- The `.week-body` day cells are kept empty (just for the visual column/row grid lines and `grid-cell--week` background/borders); events render in a separate absolutely-positioned overlay grid sharing the same `repeat(7, 1fr)` column template, so `grid-column: <start> / span <n>` aligns visually with the day columns underneath.
- `pointer-events: none` on `.week-events` container + `pointer-events: auto` on `.event-card` ensures the overlay doesn't block any future interactive elements added to day cells, while event cards remain clickable/focusable.

## Evidence
- `cd frontend && npm run build` — succeeded (exit 0). Output confirms `mon-calendrier` lazy chunk built (43.84 kB raw / 10.90 kB transfer).
- Pre-existing SCSS budget warning for `mon-calendrier.scss` (was 4.77 kB before this change, now 5.10 kB, budget 4.00 kB) — warning only, not a build error; same warning class exists for several other feature SCSS files in this project already (register, promotions, utilisateurs, cursus, dashboard). Not a regression introduced by this WI in terms of severity (warning existed before).

## Manual Test Procedure
1. Run `cd frontend && ng serve`, log in, navigate to "Mon calendrier".
2. Ensure there is at least one event (promotion or cours) whose `startDate`/`endDate` span multiple days and that the displayed week overlaps that range (e.g., create/observe a promotion course running Mon–Fri).
3. Switch to "Semaine" view (`view() === 'week'`) and navigate (prev/next) so the multi-day event's range overlaps the displayed week.
4. Verify:
   - The event renders as a single bar starting at the correct day column and visually spanning all the days it covers within that week (including partial weeks — e.g., event starts before the displayed week: bar starts at column 1; event ends after the displayed week: bar extends to column 7).
   - No start/end time is shown on this multi-day bar (`event-card__time` absent).
   - Course name, promotion (if any), and formateur (if any) are still shown.
   - Clicking the bar opens the same event-details overlay as before.
5. Verify a single-day event in the same week still shows in its own day column with `HH:mm – HH:mm` time, exactly as before.
6. Verify month view (`view() === 'month'`) is unchanged (still shows `event-chip` per day with start time only).

## Open Blockers
None.

## Next Actions
None for this WI. FRONTE-004 will address the month view multi-day rendering.

## Recall Hints
- "week view multi-day event span" / "weekEventSegments" / "grid-column span calendrier"

## Proposed Rules
None — change is local to one component and follows existing project conventions (signals, computed, native style binding, OnPush) already documented in frontend/.claude/CLAUDE.md.
