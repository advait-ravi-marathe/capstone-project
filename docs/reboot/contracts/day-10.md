# Day 10 — 2026-10-29: Persist selection batch atomically

**State:** Instructor scaffold installed; learner implementation pending. Transaction behavior is checked with real PostgreSQL and a fixed schema/quote fixture that bypasses earlier service rules.

**Conditional course reference:** [Master Hibernate and JPA with Spring Boot in 100 Steps — in28Minutes Official](https://www.udemy.com/course/hibernate-jpa-tutorial-for-beginners-in-100-steps/), “Transaction Management”: “Step 58 - Introduction to Transaction Management” (4:43; item 7907610) and “Step 59 - Transaction Management - ACID Properties” (5:49; item 7907612), 10:32 total. Access is unconfirmed; no purchase or enrollment. “Step 63 - Implementing Transaction Management - 3 Things to Decide” (4:38; item 7907620) is optional later. The [worked JDBC transaction guide](../teaching/day-10-jdbc-transaction.md) teaches this connection-owning seam; it does not teach Spring proxy semantics or audit wiring. Total: 10:32 + 10:28 + 4:00 readiness = 25 minutes.

## Learner seam

`SelectionService`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Persist pre-normalized batch in one PostgreSQL transaction; failure on any row leaves none of the batch. Successful batch assigns contiguous zero-based `selection_order` in input order. Isolated test never calls unfinished Day 1 policy.

## Required edge cases

Empty batch, two valid rows, induced second-row failure, pre-existing rows remain.

## Prerequisite and isolation

Days 8–9; canned normalized pairs and forced repository failure. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**Readiness question (inside the existing evidence slot):** Draw one connection spanning both inserts and the commit/rollback branch. Why does rollback preserve a row committed earlier? Distinguish this atomicity example from concurrency isolation.

## Concept (25 minutes)

One raw JDBC connection, auto-commit off, explicit commit after all rows, and rollback/rethrow on a statement failure. The course is conditional; the worked connection-owning example precedes the batch task. It does not promise uncertain commit outcome handling.

## Evidence (10 minutes)

Database state after injected mid-batch failure. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day10ContractTest test`

This selector runs the installed batch tests against a real embedded PostgreSQL server. The expected learner TODO failures are not a passing result. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**Consolidation day — no new mandatory problem.** Use 25 minutes to refresh due patterns, 20 minutes to cold-solve the weakest due anchor, and 15 minutes for same-day/other due active recall and the error log. Planned review items: LC 1 (D+7 from Day 3), LC 49 (D+3 from Day 7). Recompute from actual first-attempt dates and record completion only after the review. If no review is actually due, cold-recall the weakest earlier anchor; failed recall displaces the next new anchor. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** If the second insert fails, what can a fresh connection read and why?

**Observable evidence:** Run the positive and controlled-duplicate cases; show post-failure readback; distinguish batch atomicity from concurrency isolation.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A4](../interview-evidence-overlay.md#a4) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.
