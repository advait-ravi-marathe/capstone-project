# Day 15 — 2026-11-07: Read journey summary

**State:** Real-PostgreSQL test fixture and learner query seam installed. The acceptance tests currently stop at the explicit query TODO; no learner implementation is included.

**Coursework before implementation:** Required owned references are in28Minutes course 4993276: “Step 07 - Querying Data using Spring JDBC” (5:36) and “Step 07 - Understanding Optional class in Java” (7:14). They do not teach the full join, so first use the [Day 15 executable join/projection guide](../teaching/day-15-join-projection.md#worked-trace), which supplies the complete PostgreSQL toy and output. Study remains 25 minutes; no extra lecture.

## Learner seam

`JourneySummaryQuery`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Return exactly `{journeyId, quote, selections}`. `quote` is null or the Day 7 snapshot projection; `selections` contains `{quoteVersionId, selectionOrder}` ordered by `selectionOrder` ascending. Empty selections are valid. Unknown/other-owned journey uses the same non-disclosing 404. No entity or owner field may escape.

## Required edge cases

Empty, populated, unknown, other-owned, and exact JSON field allowlist.

## Prerequisite and isolation

Days 9 and 12; seeded read model. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

## Concept (25 minutes)

**Assistant-provided capstone architecture:** [../assistant-architecture-notes-15-70.md#day-15](<../assistant-architecture-notes-15-70.md#day-15>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

Use the [executable Day 15 guide](../teaching/day-15-join-projection.md#worked-trace) before implementation. Work through the separate shelf/spotlight/hold example and then transfer only ownership, optional-child, empty-list, and field-allowlist ideas to the existing journey contract. No new schema or capstone SQL is supplied.

## Evidence (10 minutes)

Empty/populated JSON fixtures. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only. Record the assistant-provided architecture note used; no capstone design proposal or retrospective justification is assigned.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day15ContractTest test`

This selector starts a real embedded PostgreSQL instance with fixed owner/quote/selection rows and exercises the query TODO. Its expected TODO failures are not an infrastructure pass.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
