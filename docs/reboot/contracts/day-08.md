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

## Separate Java DSA block (60 minutes)

**Consolidation day — no new mandatory problem.** Use 25 minutes to refresh due patterns, 20 minutes to cold-solve the weakest due anchor, and 15 minutes for same-day/other due active recall and the error log. Planned review items: LC 121 (D+7 from Day 1), LC 383 (D+3 from Day 5). Recompute from actual first-attempt dates and record completion only after the review. If no review is actually due, cold-recall the weakest earlier anchor; failed recall displaces the next new anchor. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).
## Understanding check (within the existing 10-minute evidence block)

Choose one or two prompts and keep the response brief. This check uses the existing 10 evidence minutes; it is not extra work.

1. Explain what Day 8 (Define PostgreSQL quote schema) does, why its data structure, module boundary, or transaction choice fits, one alternative, and one failure case.
2. Without AI assistance, make or debug one small change to this feature and explain the observed result.

Record behavior (fixed-test result), explanation (invariants and trade-offs), and independent adaptation as separate outcomes. A scaffold or passing tests alone earns no explanation or adaptation credit.
