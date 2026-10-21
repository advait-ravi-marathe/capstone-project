# Day 23 — 2026-11-11: Expose minimal operational health

**State:** The Actuator starter and live endpoint test are installed with a test-only health contributor. Database-readiness policy remains an explicit TODO; no real database readiness integration is claimed. Spring Boot 4.1.1 adds a public `groups` field to health responses when readiness/liveness groups are enabled; the requested status-only body therefore remains a known contract gap requiring a custom response boundary.

## Learner seam

Actuator config / health test. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Expose only `GET /actuator/health` and `/actuator/health/readiness`; response body contains only `status` (`UP` or `DOWN`), never DB details or secrets. Environment/config endpoints remain disabled (404). Disposable DB failure returns readiness `DOWN`.

## Required edge cases

Healthy, DB unavailable, unauthorized management detail, exact response allowlist.

## Prerequisite and isolation

Days 8 and 12; disposable DB lifecycle fixture. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** State which dependencies health represents and which it deliberately omits. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

Liveness/readiness and failure detection [N].

## Evidence (10 minutes)

Healthy/unavailable responses with exposure check. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day23ContractTest test`

Run `Day23ActuatorInfrastructureTest` for the live Actuator path and exposure check with a canned contributor, then `Day23ContractTest` for the learner-owned DB readiness policy TODO. The surface test confirms Boot 4.1.1 emits `groups` alongside `status`; this does not meet the strict status-only contract. The surface check is not evidence of DB availability. Infrastructure checks are separate.

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
