# Day 10 — 2026-11-02: Persist selection batch atomically

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

Database state after injected mid-batch failure. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only. Keep design reasoning in the learner-led proposal or review when assigned; no retrospective architecture or trade-off justification is required.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day10ContractTest test`

This selector runs the installed batch tests against a real embedded PostgreSQL server. The expected learner TODO failures are not a passing result. Infrastructure checks are separate.

## Interview-first system-design session (45 minutes inside the existing 85-minute practice block)

**Prompt:** [Q13](../interview/question-bank.md#q13) from the original question bank. Before any hint, reference answer, or AI, write your own initial proposal. Follow the exact 5/5/15/10/10 phases in [the mock rubric](../interview/mock-rubric.md): requirements; capacity and constraints; initial architecture and data model (save before help); bottlenecks, reliability and scaling; trade-offs, failures and follow-ups.

**Schedule:** 2026-11-02. This session replaces 45 minutes of implementation/test practice; the remaining 40 minutes stay for the day’s implementation and tests. Do not add time or mark a partial acceptance gate complete. Use the existing 10-minute evidence block for command, result, assistance, and blocker only. For the exact 40-minute EAI slice and deferred acceptance queue, follow [Sprint 1 session scope](../interview/session-scope.md); the original day stays pending until its full acceptance passes.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
