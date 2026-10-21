# Day 23 — 2026-11-11: Expose minimal operational health

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

Readiness truth, health response projection, endpoint exposure, and evidence boundaries. The projection acceptance requires exactly the public `status` field.

## Evidence (10 minutes)

Healthy/unavailable responses with exposure check. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day23ContractTest test`

Run `Day23ActuatorInfrastructureTest` for live Actuator HTTP with an actual disposable PostgreSQL probe and a test-only response-projection witness. It verifies exact status-only bodies, UP/DOWN status codes, and disabled environment/configuration endpoints. `Day23HealthProjectionContractTest` separately tests the learner-owned projection against the real HTTP surface and remains red at its named TODO. `Day23ContractTest` separately tests the learner-owned database readiness indicator and remains red at its existing TODO. Do not treat the instructor witness as learner completion.

## Interview transfer (inside the current evidence block)

**Question:** What should happen when the database is unavailable? How do liveness and readiness differ?

**Observable evidence:** Show actual health under controlled failure; explain management-endpoint exposure and why health does not prove every business flow works.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [Official expectations](../interview-evidence-overlay.md#official-expectations) (official); [A7](../interview-evidence-overlay.md#a7) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 23` first. This command uses the actual Asia/Kolkata local study date; contract dates are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../dsa/guided-transfer-policy.md](../dsa/guided-transfer-policy.md) and [the DSA error log](../dsa-error-log-template.md).
