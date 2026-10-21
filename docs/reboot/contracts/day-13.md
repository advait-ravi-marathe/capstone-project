# Day 13 — 2026-11-01: Add honest repeatable CI

**State:** Instructor-owned fail-closed GitHub Actions workflow and its local infrastructure check are installed. The learner-owned readiness evaluator remains a TODO; no learner attempt is recorded. Remote Actions success is unverified until a run is observed.

## Learner seam

`CiReadinessPolicy` for the evidence-checking part of CI readiness. The instructor supplies `.github/workflows/coverstory-starter.yml`, which runs the Java 21 infrastructure suite, frontend install/build/instructor tests, and frontend audit with failures propagated. Learner work is the evidence-based readiness evaluation and recording truthful local/remote results; a workflow file alone does not establish a successful remote run.

## Acceptance invariants

Java 21 compile, infrastructure tests and explicitly selected verified contracts run from clean checkout; failures propagate nonzero; unfinished contracts remain pending, not green. No deploy credentials.

## Required edge cases

Clean-checkout commands; deliberate test failure proves nonzero propagation; manifest not calendar-derived.

## Prerequisite and isolation

Days 1–12 status manifest and fixture smoke. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Show which evidence inputs make readiness blocked or ready; calendar date is not an input. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

Test pyramid, reproducibility, CI failure signals [I].

## Evidence (10 minutes)

Actual local/CI-equivalent output; do not claim remote run if absent. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Dtest=Day13WorkflowInfrastructureTest test`

`./mvnw -Plearner-tests -Dtest=Day13ContractTest test`

The first selector checks the supplied workflow and fail-closed commands; it passes locally. The second runs the learner readiness-policy acceptance tests and is expected red at the explicit TODO. The remote Actions result is not claimed until observed; local evidence and remote CI are separate.

## Interview transfer (inside the current evidence block)

**Question:** How can a green build lie? What proves learner logic rather than fixture wiring?

**Observable evidence:** Distinguish infrastructure, isolated contract and integrated tests; show that an intentional contract failure exits nonzero and unfinished work stays visible.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [Official expectations](../interview-evidence-overlay.md#official-expectations) (official). These references guide practice; they do not establish question frequency or predict interview content.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 13` first. This command uses the actual Asia/Kolkata local study date; contract dates are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../dsa/guided-transfer-policy.md](../dsa/guided-transfer-policy.md) and [the DSA error log](../dsa-error-log-template.md).
