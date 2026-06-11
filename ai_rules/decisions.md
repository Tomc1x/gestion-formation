# Decisions

Durable architectural/business decisions for this codebase.

---

### DEC-001 — Catalogue deletion silently removes CursusCours links

Scope: backend/src/main/java/fr/eni/gestionformation/service/CoursService.java (deleteById), entities Cours/CursusCours

Origin: WI-20260610-BACKEN-003

Added: 2026-06-10
Verified: 2026-06-10

**Rule / Decision / Pitfall:** Deleting a `Cours` from the global catalogue (`CoursService.deleteById`) silently removes all `CursusCours` link rows referencing it across every `Cursus`, without confirmation or blocking, before deleting the `Cours` itself.

**Why:** `Cours` is now an independent catalogue entity decoupled from any single `Cursus` (global reusable catalogue). Keeping `deleteById` simple avoids introducing orphan-link error states, but means a course can silently disappear from cursus listings it was part of.

**How to apply:** If a UX warning ("this course is used in N cursus") is needed before deletion, implement it on the frontend or add a check endpoint — `CursusCoursRepository.existsByCoursId` is already available for this purpose. Do not assume deletion is reversible or that cursus content remains stable after a catalogue course deletion.

**Counter-indications:** Does not apply to `CursusService.deleteById`, which only removes `CursusCours` rows for that cursus (never the underlying `Cours`).
