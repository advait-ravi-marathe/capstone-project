# Day 10 — 2026-10-29: Persist selection batch atomically

**State:** Instructor scaffold installed; learner implementation pending. Transaction behavior is checked with real PostgreSQL and a fixed schema/quote fixture that bypasses earlier service rules.

## Learner seam

`SelectionService`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Persist pre-normalized batch in one PostgreSQL transaction; failure on any row leaves none of the batch. Successful batch assigns contiguous zero-based `selection_order` in input order. Isolated test never calls unfinished Day 1 policy.

## Required edge cases

Empty batch, two valid rows, induced second-row failure, pre-existing rows remain.

## Prerequisite and isolation

Days 8–9; canned normalized pairs and forced repository failure. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**PostgreSQL design prompt:** Identify primary/composite keys, normalization, constraints, and transaction/isolation boundary relevant to this day's schema/write; do not add infrastructure beyond the contract. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

Transactions, atomicity, rollback [G].

## Evidence (10 minutes)

Database state after injected mid-batch failure. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day10ContractTest test`

This selector runs the installed batch tests against a real embedded PostgreSQL server. The expected learner TODO failures are not a passing result. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**Consolidation day — no new mandatory problem.** Use 25 minutes to refresh due patterns, 20 minutes to cold-solve the weakest due anchor, and 15 minutes for same-day/other due active recall and the error log. Planned review items: LC 1 (D+7 from Day 3), LC 49 (D+3 from Day 7). Recompute from actual first-attempt dates and record completion only after the review. If no review is actually due, cold-recall the weakest earlier anchor; failed recall displaces the next new anchor. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).
## Understanding check (within the existing 10-minute evidence block)

Choose one or two prompts and keep the response brief. This check uses the existing 10 evidence minutes; it is not extra work.

1. Explain what Day 10 (Persist selection batch atomically) does, why its data structure, module boundary, or transaction choice fits, one alternative, and one failure case.
2. Without AI assistance, make or debug one small change to this feature and explain the observed result.

Record behavior (fixed-test result), explanation (invariants and trade-offs), and independent adaptation as separate outcomes. A scaffold or passing tests alone earns no explanation or adaptation credit.
