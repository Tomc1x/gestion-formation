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

**Counter-indications:** Does not apply to `CursusService.deleteById`, which only removes `CursusCours` rows for that cursus (never the underlying `Cursus`).

**See also:** DEC-002 generalizes the cross-service cascade-cleanup call pattern used here.

---

### DEC-002 — Cross-entity cascade-cleanup via direct sibling-service calls

Scope: backend/src/main/java/fr/eni/gestionformation/service/*.java (cascade cleanup between unrelated entities, e.g. PromotionService/CursusService)

Origin: WI-20260610-BACKEN-013

Added: 2026-06-11
Verified: 2026-06-11

**Rule / Decision / Pitfall:** When deleting an entity X must null out a reference that another entity Y holds to X (e.g. `Cursus` deletion must null `Promotion.cursus`), implement this by injecting Y's service directly into X's service and calling an explicit `clearXReferences(xId)` method on it from `X...Service.deleteById`, before the final delete. Do not use an event bus, JPA-level cascade across unrelated aggregates, or database `ON DELETE` triggers for this.

**Why:** Keeps cascade-cleanup logic consistent, traceable, and testable in plain Java, matching the existing direct-call style (see DEC-001's `CoursService`/`CursusCoursRepository` cleanup). Avoids hidden cross-aggregate side effects from DB-level cascades or async event handlers.

**How to apply:** For a new "X deletion nulls Y.x reference" rule: (1) add `clearXReferences(xId)` to Y's service — load dependents via a repository finder, null the FK field, save; (2) constructor-inject Y's service into X's service via `@RequiredArgsConstructor`; (3) call `clearXReferences(id)` from `X...Service.deleteById` before `xRepository.delete(x)`. Verify there is no circular dependency between the two services before adding the injection.

**Counter-indications:** Does not apply to cleanup confined within a single entity's own owned/cascaded children (e.g. `Promotion.rythme` with `cascade=ALL, orphanRemoval=true`, or `User.promotion` reset within `PromotionService.deleteById` itself) — those are handled by JPA cascade or local repository calls without cross-service injection.

---

### DEC-003 — CoursPlanifie decoupled from Promotion (optional link)

Scope: backend/src/main/java/fr/eni/gestionformation/entity/ (CoursPlanifie, InscriptionCours, Promotion, User)
Origin: WI-20260611-FULLST-007, WI-20260611-FULLST-008, ai_doc/ANALYSIS__WI-20260611-FULLST-007__cours-planifie-inscription.md
Added: 2026-06-11
Verified: 2026-06-11

**Rule / Decision / Pitfall:** A planned course session (`CoursPlanifie`) can exist without a linked `Promotion` (`promotion_id` is nullable). An individual student's enrollment to a session is represented by a separate `InscriptionCours` entity, never by forcing a fake/placeholder `Promotion`.

**Why:** Allows "cours a l'unite" (standalone course sessions) to be modeled as a special case of a planned session rather than duplicating the whole planning model (see also CONV-006 for the PromotionCours -> CoursPlanifie rename that implements this).

**How to apply:** Any new code touching `CoursPlanifie` must treat `coursPlanifie.getPromotion() == null` as a valid case, not an error. Any aggregation query (student planning, session roster) must UNION `Promotion`-based enrollment with `InscriptionCours`.

**Counter-indications:** None — applies to all planning-related queries and code touching `CoursPlanifie`.

**See also:** PIT-009 (InscriptionCours unique constraint), PIT-010 (orphan table risk from the PromotionCours -> CoursPlanifie rename under ddl-auto=update).
