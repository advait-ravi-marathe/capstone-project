# Day 24 — 2026-11-12: Explain and improve one real query

**State:** Real-PostgreSQL fixture and plan/result acceptance test are installed. The index migration is an explicit learner TODO; no query-plan improvement is claimed.

**Course lesson:** Conditional, access-unconfirmed reference to course 1341538: “Step 82 - Performance Tuning - Measure before Tuning” (1:56) and “Step 83 - Performance Tuning - Indexes” (1:44), total 3:40. If unavailable, replace that time with the named PostgreSQL reading; do not purchase/enroll. The [Day 24 guide](../teaching/day-24-explain.md#observed-postgresql-run) contains the runnable plan/output example. See the [Days 1–30 map](../course-learning-map.md); exact catalog title metadata is not a claim of playback.

## Learner seam

history query index migration / query note. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Choose Day 16 query; seed representative synthetic data; capture plan before/after suitable index; preserve output/order; avoid brittle time thresholds and forced-index claims.

## Required edge cases

Before/after EXPLAIN, result equivalence, index write/storage trade-off.

## Prerequisite and isolation

Days 8 and 16; deterministic larger SQL seed. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**Worked prompt:** Identify filter, sort, and limit in the before plan; state the observed after-plan change and one storage/write cost.

## Concept (25 minutes)

Read the observed EXPLAIN shape and compare the exact output before/after an index; no timing claim.

## Evidence (10 minutes)

Both plans and concise trade-off note. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day24ContractTest test`

This selector starts real embedded PostgreSQL and captures EXPLAIN plans plus ordered result equivalence. It fails at the explicit index migration TODO; no time-threshold or planner-choice assertion is used. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**Consolidation day — no new mandatory problem.** Use 25 minutes to refresh due patterns, 20 minutes to cold-solve the weakest due anchor, and 15 minutes for same-day/other due active recall and the error log. Planned review items: LC 1 (D+21 from Day 3), LC 3 (D+7 from Day 17), LC 20 (D+3 from Day 21). Recompute from actual first-attempt dates and record completion only after the review. If no review is actually due, cold-recall the weakest earlier anchor; failed recall displaces the next new anchor. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** Why did this index change the plan? When could a sequential scan still be sensible?

**Observable evidence:** Compare real plans, data size, selectivity, ordering and write/storage cost; distinguish measurement from guaranteed speedup.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A5](../interview-evidence-overlay.md#a5) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.
