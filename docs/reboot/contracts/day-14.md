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

## Interview transfer (inside the current evidence block)

**Question:** Trace quote creation and selection end to end. Defend one transaction boundary and one rejected abstraction.

**Observable evidence:** 20-minute mock replaces implementation: 5 minutes flow, 5 failure demo, 5 alternative/cost, 5 ownership reflection. Use actual gate output; do not claim a demo with fake components is integration.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [Amazon official lifecycle expectations; A3](../interview-evidence-overlay.md#amazon official lifecycle expectations; a3) (anecdotal); [A5](../interview-evidence-overlay.md#a5) (anecdotal). These sources motivate practice only; they do not establish question frequency or predict an interview.

**Milestone timebox:** Use 20 minutes from the existing 85-minute implementation block (65 build/test + 20 mock); do not add hours. If the integration gate is blocked, explain the actual failing boundary and next diagnostic step, without presenting a passing demo.
