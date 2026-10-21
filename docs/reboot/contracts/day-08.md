# Day 08 — 2026-10-27: Define PostgreSQL quote schema

**State:** Instructor scaffold installed; learner implementation pending. PostgreSQL acceptance cases are fixed and currently fail because the scaffold migration omits the required constraints.

## Learner seam

Versioned `V...__homesecure_quotes.sql` migration. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Use `journeys`, `quote_versions`, and `selections`. `quote_versions` primary key is `(journey_id, quote_version_id)`; `selections` references that exact key and has unique `(journey_id, quote_version_id)` plus required `selection_order`. This prevents cross-journey selection at the database boundary and permits distinct versions in one journey.

## Required edge cases

Valid insert; null required field, duplicate pair, orphan quote reference, cross-journey reference rejected; two versions for one journey allowed. Test SQL constraints directly, not services.

## Prerequisite and isolation

Days 1 and 7; SQL seeds bypass services. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**PostgreSQL design prompt:** Identify primary/composite keys, normalization, constraints, and transaction/isolation boundary relevant to this day's schema/write; do not add infrastructure beyond the contract. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

Relational keys, normalization, integrity [E].

## Evidence (10 minutes)

Real PostgreSQL insert/rejection matrix. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day08ContractTest test`

This selector runs the installed tests against a real embedded PostgreSQL server. It should fail until the learner completes the explicit constraints in `src/main/resources/db/migration/V001__homesecure_quotes.sql`. Such failures are not an infrastructure pass. Infrastructure checks are separate.

## Interview transfer (inside the current evidence block)

**Question:** Which invalid state is impossible even if Java is bypassed? Why does pair uniqueness not itself prove same-journey ownership?

**Observable evidence:** Draw keys and references; explain cross-journey protection; execute valid and rejected direct SQL writes.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A4](../interview-evidence-overlay.md#a4) (anecdotal); [A5](../interview-evidence-overlay.md#a5) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 8` first. This command uses the actual Asia/Kolkata local study date; contract dates are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../dsa/guided-transfer-policy.md](../dsa/guided-transfer-policy.md) and [the DSA error log](../dsa-error-log-template.md).
