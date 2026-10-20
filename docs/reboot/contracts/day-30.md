# Day 30 — 2026-11-18: Defend and assess the slice

**State:** Machine-checkable open-evidence checklist and instructor integrity test are installed; no learner completion is recorded.

## Learner seam

`docs/reboot/acceptance-checklist.md` plus `Day30ContractTest` in `src/test/java/assignments/reboot/Day30ContractTest.java`. The test verifies evidence links and manifest statuses, leaving absent/unverified records open; it does not mark learner work complete. The architecture note records the explanation and ranked backlog. Do not implement later-day behavior early.

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

This instructor integrity selector is expected to pass while checking that the checklist remains open and does not auto-complete learner status. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**Consolidation day — no new mandatory problem.** Use 25 minutes to refresh due patterns, 20 minutes to cold-solve the weakest due anchor, and 15 minutes for same-day/other due active recall and the error log. Planned review items: LC 238 (D+21 from Day 9), LC 155 (D+7 from Day 23), LC 35 (D+3 from Day 27). Recompute from actual first-attempt dates and record completion only after the review. If no review is actually due, cold-recall the weakest earlier anchor; failed recall displaces the next new anchor. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).
## Understanding check (within the existing 10-minute evidence block)

Choose one or two prompts and keep the response brief. This check uses the existing 10 evidence minutes; it is not extra work.

1. Explain what Day 30 (Defend and assess the slice) does, why its data structure, module boundary, or transaction choice fits, one alternative, and one failure case.
2. Without AI assistance, make or debug one small change to this feature and explain the observed result.

Record behavior (fixed-test result), explanation (invariants and trade-offs), and independent adaptation as separate outcomes. A scaffold or passing tests alone earns no explanation or adaptation credit.
