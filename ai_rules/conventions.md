# Conventions

Durable conventions for this codebase.

---

### CONV-001 — Adapter pattern for feature data access (frontend)

Scope: frontend/src/app/core/adapters/, frontend/src/app/features/**
Origin: WI-20260610-BACKEN-001
Added: 2026-06-10
Verified: 2026-06-10

**Rule / Decision / Pitfall:** New feature data-access layers must be implemented as an abstract adapter class (`Base<Feature>Adapter`) plus a concrete real-backend implementation (`Http<Feature>Adapter` or `<Feature>ApiAdapter`) and a `Mock<Feature>Adapter`, all `@Injectable({providedIn:'root'})`, wired via a provider in `app.config.ts`; components must inject only the abstract base class, never a concrete implementation.

**Why:** Established for calendar (`BaseCalendarAdapter` / `CalendarApiAdapter` / `MockCalendarAdapter`) and now replicated identically for user-admin (`UserAdminAdapter` / `UserAdminHttpAdapter` / `UserAdminMockAdapter`). Keeps components testable/swappable between mock and real backend without touching component code.

**How to apply:** Mirror `frontend/src/app/core/adapters/calendar.adapter.ts` and `calendar-mock.ts` structure: abstract class as DI token, no constructors, use `inject()`, provide the chosen implementation in `app.config.ts`. The naming suffix for the real implementation may be `Http<Feature>Adapter` or `<Feature>ApiAdapter` (both seen in the codebase) — pick one consistent with the nearest sibling feature.

**Counter-indications:** Does not apply to one-off utility services with no mock/real backend distinction (e.g. pure UI state services).
