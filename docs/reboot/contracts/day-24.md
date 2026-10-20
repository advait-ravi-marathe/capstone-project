# Day 24 — 2026-11-12: Explain and improve one real query

**State:** Real-PostgreSQL fixture and plan/result acceptance test are installed. The index migration is an explicit learner TODO; no query-plan improvement is claimed.

## Learner seam

history query index migration / query note. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Choose Day 16 query; seed representative synthetic data; capture plan before/after suitable index; preserve output/order; avoid brittle time thresholds and forced-index claims.

## Required edge cases

Before/after EXPLAIN, result equivalence, index write/storage trade-off.

## Prerequisite and isolation

Days 8 and 16; deterministic larger SQL seed. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Record a baseline query plan and measurement before changing one query/index. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

B-tree indexes, selectivity, EXPLAIN vs EXPLAIN ANALYZE [O].

## Evidence (10 minutes)

Both plans and concise trade-off note. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day24ContractTest test`

This selector starts real embedded PostgreSQL and captures EXPLAIN plans plus ordered result equivalence. It fails at the explicit index migration TODO; no time-threshold or planner-choice assertion is used. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**Consolidation day — no new mandatory problem.** Use 25 minutes to refresh due patterns, 20 minutes to cold-solve the weakest due anchor, and 15 minutes for same-day/other due active recall and the error log. Planned review items: LC 1 (D+21 from Day 3), LC 3 (D+7 from Day 17), LC 20 (D+3 from Day 21). Recompute from actual first-attempt dates and record completion only after the review. If no review is actually due, cold-recall the weakest earlier anchor; failed recall displaces the next new anchor. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).
## Understanding check (within the existing 10-minute evidence block)

Choose one or two prompts and keep the response brief. This check uses the existing 10 evidence minutes; it is not extra work.

1. Explain what Day 24 (Explain and improve one real query) does, why its data structure, module boundary, or transaction choice fits, one alternative, and one failure case.
2. Without AI assistance, make or debug one small change to this feature and explain the observed result.

Record behavior (fixed-test result), explanation (invariants and trade-offs), and independent adaptation as separate outcomes. A scaffold or passing tests alone earns no explanation or adaptation credit.
