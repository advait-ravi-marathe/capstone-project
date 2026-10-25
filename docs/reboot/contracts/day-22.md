# Day 22 — 2026-11-16: Add safe request correlation

**State:** Fixed filter scaffold and learner acceptance tests are installed; the filter TODO remains unimplemented.

**Course lesson:** Required owned-course 4993276, section “APPENDIX - Build Java Web Application with Spring, Spring Boot and Hibernate”: “Step 09 - Quick Overview - Importance of Logging with Spring Boot” (9:53). “Step 25 - Monitoring APIs with Spring Boot Actuator” (6:54) is optional later only. Use the [Day 22 guide](../teaching/day-22-correlation.md#worked-run) within the same study block. See the [Days 1–30 map](../course-learning-map.md); exact catalog title metadata is not a claim of playback.

## Learner seam

request filter / error mapping. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Every response has `X-Request-Id` matching `[A-Za-z0-9._-]{1,64}`. Missing IDs are generated; malformed supplied IDs replaced. One error log references the selected ID, never applicant body/token/cookie. Request IDs are not metric labels.

## Required edge cases

Absent, valid, malformed ID; captured-log redaction assertions.

## Prerequisite and isolation

Day 3; fixed request fixtures and fake logger. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**Worked prompt:** Trace one 400 and one 200 through ID selection, handler execution, and audit. Why validate before echoing or recording?

## Concept (25 minutes)

**Assistant-provided capstone architecture:** [../assistant-architecture-notes-15-70.md#day-22](<../assistant-architecture-notes-15-70.md#day-22>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

Request-ID selection, validation, response propagation, and error-only audit; never log request data.

## Evidence (10 minutes)

Captured log assertions. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only. Record the assistant-provided architecture note used; no capstone design proposal or retrospective justification is assigned.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day22ContractTest test`

The fixed request-filter tests run through the named selector and currently fail at the explicit correlation TODO. Infrastructure checks are separate.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
