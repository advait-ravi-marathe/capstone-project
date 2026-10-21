# Day 23 — 2026-11-11: Expose minimal operational health

**State:** The Actuator starter and live endpoint test are installed with a test-only health contributor. Database-readiness policy remains an explicit TODO; no real database readiness integration is claimed. Spring Boot 4.1.1 adds a public `groups` field to health responses when readiness/liveness groups are enabled; the requested status-only body therefore remains a known contract gap requiring a custom response boundary.

**Course lesson:** Required owned-course 4993276, section “Getting Started with Spring Boot”: “Step 12 - Get Production Ready with Spring Boot - 4 - Actuator” (7:33). It is foundation only; the [Day 23 guide](../teaching/day-23-readiness.md#indicator-versus-projection) teaches response projection and documents the current `groups` mismatch. See the [Days 1–30 map](../course-learning-map.md); exact catalog title metadata is not a claim of playback.

## Learner seam

Actuator config / health test. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Expose only `GET /actuator/health` and `/actuator/health/readiness`; response body contains only `status` (`UP` or `DOWN`), never DB details or secrets. Environment/config endpoints remain disabled (404). Disposable DB failure returns readiness `DOWN`.

## Required edge cases

Healthy, DB unavailable, unauthorized management detail, exact response allowlist.

## Prerequisite and isolation

Days 8 and 12; disposable DB lifecycle fixture. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**Worked prompt:** Separate readiness indicator truth, public projection, endpoint exposure, and real database availability. What is the observed `groups` limitation?

## Concept (25 minutes)

Readiness truth, health response projection, endpoint exposure, and evidence boundaries. The overall endpoint currently includes `groups` as well as `status`.

## Evidence (10 minutes)

Healthy/unavailable responses with exposure check. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day23ContractTest test`

Run `Day23ActuatorInfrastructureTest` for the live Actuator path and exposure check with a canned contributor, then `Day23ContractTest` for the learner-owned DB readiness policy TODO. The surface test confirms Boot 4.1.1 emits `groups` alongside `status`; this does not meet the strict status-only contract. The surface check is not evidence of DB availability. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**New pattern anchor:** LeetCode 155 Min Stack — augmented stack. Learn the pattern for 25 minutes, solve independently for 20 minutes, and spend 15 minutes on same-day D recall plus the error log. Record the actual first-attempt date; schedule D+3, D+7 and D+21 reviews from that date. Planned dates in the manifest are forecasts only. A failed prerequisite/review displaces the next new anchor; do not add overtime. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** What should happen when the database is unavailable? How do liveness and readiness differ?

**Observable evidence:** Show actual health under controlled failure; explain management-endpoint exposure and why health does not prove every business flow works.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [Official expectations](../interview-evidence-overlay.md#official-expectations) (official); [A7](../interview-evidence-overlay.md#a7) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.
