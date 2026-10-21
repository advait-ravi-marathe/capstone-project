# Day 30 — 2026-11-18: Defend and assess the slice

**State:** Machine-checkable evidence checklist and instructor integrity test are installed; learner status remains evidence-driven and may evolve truthfully from open/blocked to passed when local evidence exists.

## Learner seam

`docs/reboot/acceptance-checklist.md` plus `Day30ContractTest` in `src/test/java/assignments/reboot/Day30ContractTest.java`. The test verifies evidence links and manifest statuses, leaves absent/unverified records open, and accepts valid evidence-backed completion; it does not mark learner work complete. The architecture note records the explanation and ranked backlog. Do not implement later-day behavior early.

## Acceptance invariants

Rerun appropriate gate; independently explain dedup, money, transactions, authorization and UI race choices; list TODOs and evidence links; choose next scope only from demonstrated gaps; StreamForge remains deferred.

## Required edge cases

Machine-check checklist against actual manifest and referenced evidence; missing/unverified entries remain open, never auto-pass. The day-specific command checks this artifact:

## Prerequisite and isolation

Days 28–29; no manufactured integrated success. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Choose one view to explain the implemented slice and one verified trade-off. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

SDE II trade-off discussion and technical communication.

## Evidence (10 minutes)

5-minute demo script, current gate, prerequisite-ranked backlog. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day30ContractTest test`

This instructor integrity selector is expected to pass against fixed status/evidence fixtures and the live evolving checklist. It rejects unsupported statuses and passed rows without a repository-local evidence file; it does not require the live checklist to stay open. Infrastructure checks are separate.

## Interview transfer (inside the current evidence block)

**Question:** What did you own, what failed, what changed, and what evidence supports the outcome? What breaks first at 10× load?

**Observable evidence:** 20-minute mock replaces implementation: 5 minutes architecture, 5 dedup/money/transaction/security choice, 5 measured bottleneck versus hypothesis, 5 honest gap/behavior story. Do not invent users, impact or production incidents.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [Official expectations](../interview-evidence-overlay.md#official-expectations) (official); [A3](../interview-evidence-overlay.md#a3) (anecdotal); [A5](../interview-evidence-overlay.md#a5) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.

**Milestone timebox:** Use 20 minutes from the existing 85-minute implementation block (65 build/test + 20 mock); do not add hours. If the integration gate is blocked, explain the actual failing boundary and next diagnostic step, without presenting a passing demo.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 30` first. This command uses the actual Asia/Kolkata local study date; contract dates are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../dsa/guided-transfer-policy.md](../dsa/guided-transfer-policy.md) and [the DSA error log](../dsa-error-log-template.md).
