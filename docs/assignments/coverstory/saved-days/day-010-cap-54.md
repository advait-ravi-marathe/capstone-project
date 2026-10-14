# Day 010 · 23 Oct 2026 — Authorize UUID journey and homepage resume (150m)

**Date:** 2026-10-23 · **Phase:** 1 · **Primary ID:** `CAP-54` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `dcaf268278089aa3d3c48fff60a37b808ec96ec81ddac2d493e175b5d309ef2d`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Enforce authenticated ownership for UUID journeys and minimal homepage resume while public catalog discovery remains anonymous.

## Explicit repository paths in the saved contract

- `src/main/java/capstone/coverstory/application/api/CAP54Seam.java` (planned in source; existence/readiness is not implied)
- `src/test/java/capstone/coverstory/application/CAP54Test.java` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-54/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-54.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-54/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-54/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-54` · primary · Day 010 · 23 Oct 2026 — Authorize UUID journey and homepage resume (150m)

Description SHA-256: `dcaf268278089aa3d3c48fff60a37b808ec96ec81ddac2d493e175b5d309ef2d`

**Practical task:** Enforce authenticated ownership for UUID journeys and minimal homepage resume while public catalog discovery remains anonymous.

Exact source description: [saved curriculum record `CAP-54`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- src/main/java/capstone/coverstory/application/api/CAP54Seam.java
- src/test/java/capstone/coverstory/application/CAP54Test.java
- fixtures/coverstory/CAP-54/synthetic-v1.json
- docs/assignments/coverstory/CAP-54.md
- docs/evidence/coverstory/CAP-54/harness-self-check.json
- docs/evidence/coverstory/CAP-54/result.json

Optional<JourneySummary> get(AuthenticatedBuyer principal, UUID journeyId); Page<JourneySummary> listActive(principal, limit, cursor). Complete the owner-scoped query/adapter tests only.

#### Concrete inputs and outputs

GET /api/v1/journeys/{id} and GET /api/v1/me/journeys?status=active&limit=20 use an instructor-supplied trusted principal, repository and current-domain navigation validator. Limit 1–50; default 20. Summary fields are journey UUID, category, safe status/current step, version, updatedAt and allowed next action. It references owned shortlist/application/revision/quote IDs but returns no decrypted consumer answers. Unknown and foreign UUIDs are the same 404. Owner overrides in query/body are rejected before repository access. Anonymous selections stay device-local; no anonymous private journey or sensitive draft is created. Encryption adapter readiness is required before any later private write.

#### Acceptance cases · expected, NOT_RUN

- CAP-54-CS-01: Input: A owns journey jA linked to application-A revision 2; reload under a new authenticated A session.. Expected: 200 with the same journey UUID and recomputed safe resume action; no decrypted fields in the summary..
- CAP-54-CS-02: Input: B requests jA, or A requests an absent journey UUID.. Expected: Identical safe 404; no foreign serialization; decrypt/provider call counts 0..
- CAP-54-CS-03: Input: A supplies owner_user_id=B or a browser current_step=ISSUED override.. Expected: 400 for the owner override; browser step cannot advance state; no unauthorized lookup or mutation..
- CAP-54-CS-04: Input: Anonymous visitor browses catalog and requests /me/journeys.. Expected: Public catalog remains 200; private journey request 401; device-local selections preserved..
- CAP-54-CS-05: Input: A uses an owner-bound cursor with limit 50, then limit 51 or a cursor issued for B.. Expected: 50 accepted; invalid limit/cursor rejected safely; no foreign summaries..

#### Architecture/interview evidence delta

At most 5 minutes inside the existing docs/test-evidence allocation; at most 3 changed facts and either 6 nodes or 10 sequence messages. Do not draw a new full system or add implementation work. Use the instructor-supplied Authorization/resume sequence template and today’s existing test/receipt in docs/evidence/coverstory/CAP-54/result.json. Annotate the supplied session → owner-scoped journey lookup → current-domain resume validator → minimal summary sequence. Record only these changed facts: The UUID identifies the journey but does not authorize it.; Foreign and absent journeys have the same safe 404 response.; Ownership is checked before any protected-payload access or decrypt call..

Alternative: Global UUID lookup with client-side filtering versus an owner-scoped server query before serialization. Interview question: Why can the homepage return a safe resume action without decrypting consumer answers, and how does a stale navigation projection fail closed? Use at most 3 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task.
