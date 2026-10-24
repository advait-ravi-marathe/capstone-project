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

**Teach-first design question (within the current concept block):** Why does S1 remain present with a null optional child? Why do missing and foreign-owned parents both yield absence? Explain why the projected response excludes private columns. Use the concrete guided example; no blank system-design worksheet is assigned.

## Concept (25 minutes)

Use the [executable Day 15 guide](../teaching/day-15-join-projection.md#worked-trace) before implementation. Work through the separate shelf/spotlight/hold example and then transfer only ownership, optional-child, empty-list, and field-allowlist ideas to the existing journey contract. No new schema or capstone SQL is supplied.

## Interview-first proposal (5 minutes inside practice)

Before implementation hints or AI, write a brief task-specific proposal: the user-visible result, data/UI boundary, one expected case, and one failure case. Save it, then continue the original acceptance in the remaining 80 minutes of the existing practice block. Do not add a post-hoc design essay. The 10-minute evidence block records actual command, result, assistance, changed paths, and blocker; unfinished acceptance stays pending in the normal recovery queue.

## Evidence (10 minutes)

Empty/populated JSON fixtures. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only. Keep design reasoning in the learner-led proposal or review when assigned; no retrospective architecture or trade-off justification is required.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day15ContractTest test`

This selector starts a real embedded PostgreSQL instance with fixed owner/quote/selection rows and exercises the query TODO. Its expected TODO failures are not an infrastructure pass.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
