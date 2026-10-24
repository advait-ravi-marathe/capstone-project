# Day 24 — 2026-11-16: Explain and improve one real query

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

## Interview-first system-design mock (45 minutes inside practice)

**Prompt:** [Q10](../interview/question-bank.md#q10) — Notification retries and durable intent. Write and save your own initial proposal before hints, reference answers, or AI. Use the exact 5/5/15/10/10 phases and 100-point rubric in [the mock rubric](../interview/mock-rubric.md).

**Timebox:** 45 minutes of the existing 85-minute practice block for the mock, then 40 minutes for this EAI slice: Implement the single query index, preserve ordered results, and capture before/after EXPLAIN using the existing selector. No overtime. The unchanged full acceptance selector remains the completion gate.

**Deferred acceptance:** `S2-D24-1` is in the [Sprint 2 session scope](../interview/sprint2-session-scope.md). Preserve the actual result and keep this day pending until the named completion check and all original acceptance criteria pass.

## Evidence (10 minutes)

Both plans and ordered result-equivalence evidence. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only. Keep design reasoning in the learner-led proposal or review when assigned; no retrospective architecture or trade-off justification is required.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day24ContractTest test`

This selector starts real embedded PostgreSQL and captures EXPLAIN plans plus ordered result equivalence. It fails at the explicit index migration TODO; no time-threshold or planner-choice assertion is used. Infrastructure checks are separate.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
