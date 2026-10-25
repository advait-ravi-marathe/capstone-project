# Sprint 3 interview sessions and EAI slices

Sprint 3 covers existing Days 29–42 (23 November–6 December 2026). The four
45-minute mocks are Days 31, 35, 38, and 42: two in each seven-day practice
week. Each occupies the existing 85-minute practice block with a 45-minute mock
and a 40-minute CoverStory slice (5 minutes preconditions/prediction, 25
minutes implementation, 10 minutes selector and blocker evidence). The remaining
EAI acceptance is preserved in a named queue;
the mock or partial slice never marks the day complete. Daily study/evidence
remains 25/10 minutes, for 120 engineering minutes total.

For every mock, save the initial proposal during the 15-minute architecture
phase before hints, reference answers, or AI. Use the existing
[100-point rubric](mock-rubric.md) and its 5/5/15/10/10 phases. Keep evidence
to the saved proposal, actual command/results or first blocker, changed paths,
assistance, and relevant snapshots. Do not create a post-hoc architecture
essay. Foundations and current local acceptance remain primary; no new cloud,
broker, NoSQL service, or StreamForge work is assigned.

| Day/date | Design prompt and supported bridge | 40-minute EAI slice and existing selector | Named queue / preserved acceptance |
|---|---|---|---|
| 31 · 2026-11-25 | [Q05 — CoverStory domain workflow](question-bank.md#q05). Existing owned foundation from *Master Spring Boot 3 & Spring Framework 6 with Java* (in28Minutes Official): “Step 21 - Understanding Spring Security Authentication” (4:17), used only for authenticated identity; continue with the already assigned [GOV.UK acceptance criteria](https://www.gov.uk/service-manual/agile-delivery/writing-user-stories) and [Spring MVC method arguments](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/arguments.html). | Implement the supplied-facts happy path and one explicit quote rejection in `PolicyLifecycleService.validateIssue`; run `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day31RejectsForeignUnselectedStaleOrIneligibleQuoteWithoutIssue test` and record the first unmet case. | **S3-D31-1:** Complete valid, missing, foreign-owner, unselected, stale, ineligible, foreign-journey, and foreign-version cases, with no policy write on rejection; rerun the same selector. |
| 35 · 2026-11-29 | [Q13 — Booking: reserve one seat atomically](question-bank.md#q13). The conditional *Master Hibernate and JPA with Spring Boot in 100 Steps* lesson “Step 63 - Implementing Transaction Management - 3 Things to Decide” (4:38; access unconfirmed) is optional replacement-only. Use the already assigned [PostgreSQL transaction tutorial](https://www.postgresql.org/docs/18/tutorial-transactions.html) and [AWS ADR template](https://docs.aws.amazon.com/prescriptive-guidance/latest/architectural-decision-records/appendix.html) if unavailable. | Implement the successful one-connection policy/event/receipt write and verify fresh rows using `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day35AtomicPostgresWriteHasOnePolicyAndOneReceipt test`. | **S3-D35-1:** Complete injected receipt-conflict rollback and prove no policy, event, or receipt partial write and no in-memory replay publication; run both existing Day 35 selectors. |
| 38 · 2026-12-02 | [Q16 — Messaging: ordering and replay](question-bank.md#q16). No additional lesson: use existing [PostgreSQL constraints](https://www.postgresql.org/docs/18/ddl-constraints.html), [Java Record documentation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Record.html), and the Day 38 history guide. Treat event ordering as a comparison; CoverStory's current local relational history remains the implementation. | Append one immutable endorsement version and preserve prior snapshots; run `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day38EndorsementAppendsAndPreservesPriorHistoryInEffectiveOrder test`. | **S3-D38-1:** Complete the original effective-order, exactly-one-version, input-immutability, and history-preservation acceptance in that selector. Do not create a broker, outbox, or StreamForge dependency. |
| 42 · 2026-12-06 | [Q11 — Scheduler: run one job](question-bank.md#q11). Keep the existing *Java Programming for Complete Beginners* (in28Minutes Official) enum lesson as state-vocabulary foundation only (access unconfirmed); use already assigned [Java `Clock`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Clock.html) and [AWS runbook](https://docs.aws.amazon.com/wellarchitected/latest/operational-excellence-pillar/ops_ready_to_support_use_runbooks.html) readings for time and recovery. | Implement only `permitsServicingWrite` guard and run `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day42CancelledPolicyRejectsWritesButRemainsReadable test`. That selector asserts a boolean and synthetic owner read only. | **S3-D42-1:** Complete the pure guard/synthetic-read selector. **S3-D42-2 (OPEN):** after Days 38/39 actual endorsement writes and Day 41 persisted transition exist, add actual endorsement and reschedule write/retry checks with complete history snapshots; assert 409 only for a real mapped endpoint. The current selector cannot close this integration item. No scheduler implementation is added to Day 42. |

## Capstone implementation guidance on non-mock days

For Days 29–30, 32–34, 36–37, and 39–41, use the assistant-provided decision and rationale in the daily concept block, then implement and test the existing task within its 85-minute practice allocation. No capstone design proposal or post-hoc architectural justification is assigned. The learner saves a proposal only for a scheduled question-bank mock. Keep any unfinished acceptance open under its existing day ID.

## Prerequisites and boundaries

Day 31 depends on the actual prior quote, identity, and eligibility seams.
Day 35 depends on the earlier issuance decisions and schema; Day 38 depends on
issue history and Day 37 validation; Day 42 depends on Day 41's due-cancellation
transition. A failure at a prerequisite stays a blocker, never a reason to
copy a later solution or substitute a mock result. The existing `fde-coursework-v2`
selectors remain unchanged. StreamForge remains prerequisite-gated.
