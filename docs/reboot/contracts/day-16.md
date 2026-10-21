# Day 16 — 2026-11-04: Bound quote-history reads

**State:** Real-PostgreSQL pagination fixture and learner query seam installed. The focused tests currently stop at the explicit pagination TODO.

**Coursework before implementation:** Required owned reference: in28Minutes course 4993276, “Step 33 - Exploring JPA and Hibernate Queries for REST API” (6:36), query context only. Do not stack the Spring Data JPA lesson. Use the [Day 16 executable pagination guide](../teaching/day-16-stable-pagination.md#worked-trace) for the exact tie-break and page outputs. Study remains 25 minutes; this project stays JDBC.

## Learner seam

`QuoteHistoryQuery`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

`GET /api/journeys/{journeyId}/quotes` uses zero-based `page` (default 0), `size` (default 10, maximum 20), newest first by `createdAt` descending, then `quoteVersionId` ascending for ties. Empty/final pages are valid. Negative page, size below 1 or above 20 returns 400 `INVALID_REQUEST`.

## Required edge cases

Timestamp ties, page size 0/over 20, empty/final page, adjacent pages have no duplicate.

## Prerequisite and isolation

Days 9 and 12; seeded history. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**Teach-first design question (within the current concept block):** Why does equal-time ordering need the unique ascending ID tie-breaker? What does OFFSET select, and why can a new row between requests repeat an item? Use the concrete guided example; no blank system-design worksheet is assigned.

## Concept (25 minutes)

Sorting, pagination cost and query complexity.

## Evidence (10 minutes)

Adjacent page fixture assertions. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day16ContractTest test`

This selector starts a real embedded PostgreSQL instance with timestamp ties and multiple pages. Expected TODO failures are not a pagination or infrastructure pass.

## Separate Java DSA block (60 minutes)

**Consolidation day — no new mandatory problem.** Use 25 minutes to refresh due patterns, 20 minutes to cold-solve the weakest due anchor, and 15 minutes for same-day/other due active recall and the error log. Planned review items: LC 238 (D+7 from Day 9), LC 167 (D+3 from Day 13). Recompute from actual first-attempt dates and record completion only after the review. If no review is actually due, cold-recall the weakest earlier anchor; failed recall displaces the next new anchor. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** Why does sorting only by timestamp fail? What happens when rows are inserted between offset-page requests?

**Observable evidence:** Demonstrate stable fixture ties; explain the bounded OFFSET tradeoff and why intervening inserts can change later pages. Cursor pagination is outside this assignment.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A5](../interview-evidence-overlay.md#a5) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.
