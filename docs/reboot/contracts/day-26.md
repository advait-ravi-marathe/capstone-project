# Day 26 — 2026-11-18: Record minimal selection audit

**State:** Real PostgreSQL scaffold and fixed learner tests are installed; the JDBC transaction/audit behavior remains a learner TODO.

**Course/concept bridge:** No exact verified audit lecture. Read the named PostgreSQL transactions and `INSERT ... RETURNING` sections, then run the [Day 26 guide](../teaching/day-26-audit.md#first-change-repeat-and-failure) within the existing 25-minute block. See the [Days 1–30 map](../course-learning-map.md); exact catalog title metadata is not a claim of playback.

## Learner seam

audit migration/write service. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

First successful selection appends exactly `{action:"SELECTION_CREATED", resourceId, actorId, occurredAt}` in the same transaction; rollback removes both selection and audit; a repeated request appends no creation event. Store no request or quote payload.

## Required edge cases

Success, repeat, rollback; exact event count/fields; no payload column.

## Prerequisite and isolation

Days 10, 12 and 25; fixed actor/time and seeded input. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**Worked prompt:** Compare first, repeated, and failed-audit paths. Why is a business creation event not the same as a request log?

## Concept (25 minutes)

First-change versus repeat semantics, one transaction, minimal event fields, and rollback.

## Evidence (10 minutes)

Success/repeat/rollback database counts. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day26ContractTest test`

This selector runs the fixed cases against real embedded PostgreSQL. The tests are expected to fail at the learner-owned transaction/audit TODO. Infrastructure checks are separate.

## Interview transfer (inside the current evidence block)

**Question:** Can the selection commit while its audit event is missing? What happens on repeat?

**Observable evidence:** Explain shared transaction and minimum audit fields; demonstrate first/repeat/rollback counts; distinguish this local atomic record from external event-delivery guarantees.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [Official expectations](../interview-evidence-overlay.md#official-expectations) (official); [A4](../interview-evidence-overlay.md#a4) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 26` first. This command uses the actual Asia/Kolkata local study date; contract dates are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../dsa/guided-transfer-policy.md](../dsa/guided-transfer-policy.md) and [the DSA error log](../dsa-error-log-template.md).
