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

## Evidence (10 minutes)

Captured log assertions. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day22ContractTest test`

The fixed request-filter tests run through the named selector and currently fail at the explicit correlation TODO. Infrastructure checks are separate.

## Interview transfer (inside the current evidence block)

**Question:** A user reports an error. How can you follow the request without logging its body or token?

**Observable evidence:** Find matching correlation ID; show unsafe-ID replacement and log-redaction assertions; explain why IDs are poor metric labels.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A1](../interview-evidence-overlay.md#a1) (anecdotal); [A7](../interview-evidence-overlay.md#a7) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 22` first. This command uses the actual Asia/Kolkata local study date; contract dates are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../dsa/guided-transfer-policy.md](../dsa/guided-transfer-policy.md) and [the DSA error log](../dsa-error-log-template.md).
