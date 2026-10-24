# Day 23 — 2026-11-15: Expose minimal operational health

**State:** A narrow Actuator response-projection seam and live endpoint test are installed. The learner-owned status projection and database-readiness indicator remain explicit TODOs. Instructor HTTP checks use a real disposable PostgreSQL probe and a temporary projection witness; they are infrastructure evidence, not learner completion.

**Course lesson:** Required owned-course 4993276, section “Getting Started with Spring Boot”: “Step 12 - Get Production Ready with Spring Boot - 4 - Actuator” (7:33). It is foundation only; the [Day 23 guide](../teaching/day-23-readiness.md#indicator-versus-projection) teaches the status-only response projection. See the [Days 1–30 map](../course-learning-map.md); exact catalog title metadata is not a claim of playback.

## Learner seam

`HealthResponseProjection.publicOptions()` controls component/group exposure while Boot preserves the health status and HTTP code. `DatabaseReadinessIndicator.health()` remains a separate learner-owned seam. Do not implement later-day behavior early.

## Acceptance invariants

Expose only `GET /actuator/health` and `/actuator/health/readiness`; response body contains only `status` (`UP` or `DOWN`), never DB details or secrets. Environment/config endpoints remain disabled (404). Disposable DB failure returns readiness `DOWN`.

## Required edge cases

Healthy, DB unavailable, unauthorized management detail, exact response allowlist.

## Prerequisite and isolation

Days 8 and 12; disposable DB lifecycle fixture. The instructor HTTP test checks actual PostgreSQL availability and uses a test-only projection witness; the tagged projection acceptance and existing indicator contract exercise the real learner TODOs. Do not count the witness as learner evidence. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**Worked prompt:** Separate readiness indicator truth, public projection, endpoint exposure, and real database availability. Why must the response preserve `status` and omit `groups`, components, and environment details?

## Concept (25 minutes)

**Assistant-provided capstone architecture:** [../assistant-architecture-notes-15-70.md#day-23](<../assistant-architecture-notes-15-70.md#day-23>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

Readiness truth, health response projection, endpoint exposure, and evidence boundaries. The projection acceptance requires exactly the public `status` field.

## Evidence (10 minutes)

Healthy/unavailable responses with exposure check. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only. Record the assistant-provided architecture note used; no capstone design proposal or retrospective justification is assigned.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day23ContractTest test`

Run `Day23ActuatorInfrastructureTest` for live Actuator HTTP with an actual disposable PostgreSQL probe and a test-only response-projection witness. It verifies exact status-only bodies, UP/DOWN status codes, and disabled environment/configuration endpoints. `Day23HealthProjectionContractTest` separately tests the learner-owned projection against the real HTTP surface and remains red at its named TODO. `Day23ContractTest` separately tests the learner-owned database readiness indicator and remains red at its existing TODO. Do not treat the instructor witness as learner completion.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
