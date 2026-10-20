# Day 14 — 2026-11-02: Milestone 1 backend vertical slice

**State:** Readiness-only infrastructure check installed. It reads current manifest evidence and verifies that the Day 14 integration gate remains blocked while any Day 1–13 prerequisite is unverified; it does not run the full backend slice.

## Learner seam

`BackendSliceIT`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Real product → eligible quote → persist → owner selection → readback. Duplicate semantics remain Day 1’s; stale, expired, invalid and unauthorized cases cannot write. Real PostgreSQL and completed production policies.

## Required edge cases

Full success readback; duplicate; stale/expired; invalid; unauthorized with no side effect.

## Prerequisite and isolation

Days 1–13 are required; the integration gate contains no business-policy fakes. The installed readiness check reads current manifest evidence only and reports the gate as blocked until every prerequisite is genuinely complete.

**System-design prompt (within the 25-minute concept block):** Draw the full product-to-database path and list missing prerequisites; keep the gate blocked until real components pass. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

Trace request lifecycle, data ownership, transaction boundary.

## Evidence (10 minutes)

Integration report, short demo, prerequisite gaps. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Dtest=Day14ContractTest test`

This readiness check passes only by confirming the gate is blocked. It is not a Day 14 integration pass. `BackendSliceIT` remains uninstalled until every prerequisite is genuinely complete.

## Separate Java DSA block (60 minutes)

**Consolidation day — no new mandatory problem.** Use 25 minutes to refresh due patterns, 20 minutes to cold-solve the weakest due anchor, and 15 minutes for same-day/other due active recall and the error log. Planned review items: LC 49 (D+7 from Day 7), LC 125 (D+3 from Day 11). Recompute from actual first-attempt dates and record completion only after the review. If no review is actually due, cold-recall the weakest earlier anchor; failed recall displaces the next new anchor. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).
## Understanding check (within the existing 10-minute evidence block)

Choose one or two prompts and keep the response brief. This check uses the existing 10 evidence minutes; it is not extra work.

1. Explain what Day 14 (Milestone 1 backend vertical slice) does, why its data structure, module boundary, or transaction choice fits, one alternative, and one failure case.
2. Without AI assistance, make or debug one small change to this feature and explain the observed result.

Record behavior (fixed-test result), explanation (invariants and trade-offs), and independent adaptation as separate outcomes. A scaffold or passing tests alone earns no explanation or adaptation credit.
