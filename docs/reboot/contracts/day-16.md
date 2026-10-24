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

**Teach-first design question (within the current concept block):** Why does equal-time ordering need the unique ascending ID tie-breaker? What does OFFSET select, and why can a new row between requests repeat an item? Use the concrete guided example; no blank system-design worksheet is assigned.

## Concept (25 minutes)

Sorting, pagination cost and query complexity.

## Interview-first proposal (5 minutes inside practice)

Before implementation hints or AI, write a brief task-specific proposal: the user-visible result, data/UI boundary, one expected case, and one failure case. Save it, then continue the original acceptance in the remaining 80 minutes of the existing practice block. Do not add a post-hoc design essay. The 10-minute evidence block records actual command, result, assistance, changed paths, and blocker; unfinished acceptance stays pending in the normal recovery queue.

## Evidence (10 minutes)

Adjacent page fixture assertions. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only. Keep design reasoning in the learner-led proposal or review when assigned; no retrospective architecture or trade-off justification is required.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day16ContractTest test`

This selector starts a real embedded PostgreSQL instance with timestamp ties and multiple pages. Expected TODO failures are not a pagination or infrastructure pass.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
