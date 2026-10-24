# Day 22 — 2026-11-14: Add safe request correlation

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

Request-ID selection, validation, response propagation, and error-only audit; never log request data.

## Interview-first proposal (5 minutes inside practice)

Before implementation hints or AI, write a brief task-specific proposal: the user-visible result, data/UI boundary, one expected case, and one failure case. Save it, then continue the original acceptance in the remaining 80 minutes of the existing practice block. Do not add a post-hoc design essay. The 10-minute evidence block records actual command, result, assistance, changed paths, and blocker; unfinished acceptance stays pending in the normal recovery queue.

## Evidence (10 minutes)

Captured log assertions. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only. Keep design reasoning in the learner-led proposal or review when assigned; no retrospective architecture or trade-off justification is required.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day22ContractTest test`

The fixed request-filter tests run through the named selector and currently fail at the explicit correlation TODO. Infrastructure checks are separate.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
