# Day 16 — 2026-11-08: Bound quote-history reads

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

## Concept (25 minutes)

**Assistant-provided capstone architecture:** [../assistant-architecture-notes-15-70.md#day-16](<../assistant-architecture-notes-15-70.md#day-16>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

Sorting, pagination cost and query complexity.

## Evidence (10 minutes)

Adjacent page fixture assertions. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only. Record the assistant-provided architecture note used; no capstone design proposal or retrospective justification is assigned.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day16ContractTest test`

This selector starts a real embedded PostgreSQL instance with timestamp ties and multiple pages. Expected TODO failures are not a pagination or infrastructure pass.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
