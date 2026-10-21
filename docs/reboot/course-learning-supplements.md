# Short worked guides for Days 1–30

These are instructor-provided bridges for topics the verified course titles only partly cover or do not cover. Use the relevant guide inside that day's existing 25-minute course/concept allocation. They are not a replacement for the learner TODO, acceptance test, or independent explanation. If course viewing plus pauses runs over 25 minutes, stop and carry unfinished material forward; reduce implementation scope. Code fragments illustrate a concept and are not a completed assignment solution.

## Day 1 — First API bridge

Use the complete runnable lesson in [Day 1 API bridge](day-01-api-bridge.md). It maps the verified Step 02 Hello World and Step 03 returned-bean course lessons to actual loopback HTTP, a complete `@RestController` example, status/header/body observations, and a distinct learner practice TODO. Step 00 is an optional overview when request/API terms are unfamiliar; Step 01 is optional setup because the supplied test-runtime fixture handles setup. The guide includes the required 25/85/10-minute boundaries and does not solve the quote-selection contract.

**Primary references:** [Spring `@ResponseBody` and `@RestController`](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/responsebody.html), [Spring MVC request mappings](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html), and [Spring Boot Maven `test-run`](https://docs.spring.io/spring-boot/maven-plugin/run.html#run.test-run). These support the worked guide; the learner should run and observe the fixture rather than treat links as completion.

## Day 2 — Request-body conversion and validation order

Use [the complete request-boundary guide](teaching/day-02-request-boundary.md) after required Step 09, “Implementing POST Method to create User Resource” (8:36). The optional validation lecture is a later reference, not today’s required material. The guide includes a beginner constructor/test primer, an executable in-process reference with valid, blank, malformed, absent-body, and distinct-value cases, a recording collaborator, and the exact limits of MockMvc evidence. Transfer only after the example: the capstone empty selection list is valid and must not be rejected as a generic “empty request.”

**Primary references:** [Spring `@RequestBody`](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/requestbody.html), [constructor injection](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html), [MockMvc overview](https://docs.spring.io/spring-framework/reference/testing/mockmvc/overview.html), and [JUnit assertions](https://docs.junit.org/6.1.3/writing-tests/assertions.html).

## Day 3 — Safe exception translation

Use [the complete safe-errors guide](teaching/day-03-safe-errors.md) after required Step 12, “Implementing Generic Exception Handling for all Resources” (11:29). The Day 11 404 lecture is optional later, not stacked into today’s block. The guide shows a path-variable request, a known 409 failure, an unexpected 500 with fixed redacted body, and a separate new-ID variation. It preserves the capstone’s own error codes and messages and does not require the later 401/403 topic.

**Primary reference:** Spring MVC [exception handling and `@ExceptionHandler`](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-exceptionhandler.html), initial handler example and exception mapping.

## Day 4 — Immutable catalog values

Use the [complete collection-ownership guide](teaching/day-04-immutable-values.md) after required “Step 13 - Exploring Java Bean vs POJO vs Spring Bean” (5:15; item 35020094). The bean lesson does not teach collection ownership; the guide works through constructor copying, caller mutation, an unmodifiable returned list, and its limits. “Step 09 - Creating More Java Spring Beans in Spring Java Configuration File” (6:41; item 35020124) is optional later, not stacked today. Total: 5:15 lesson + 15:45 worked guide + 4:00 readiness = 25 minutes.

**Primary reference:** [Java 21 `List.copyOf` and unmodifiable lists](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/List.html).

## Day 5 — Pure eligibility decision

Use the [complete decision-table guide](teaching/day-05-decision-table.md) after required “Core concepts of Functional Programming” (6:43; item 49578631). The optional streams lesson (5:30) is not required. The guide teaches ordered rule precedence with a separate parcel example, then asks you to map the existing insurance table without copying toy values. Total: 6:43 lesson + 14:17 guide + 4:00 readiness = 25 minutes.

**Course scope:** the video is supporting context; the instructor guide supplies the concrete table and worked outcomes.

## Day 6 — Decimal calculation and rounding

Use the [complete decimal rounding guide](teaching/day-06-decimal-rounding.md) with conditional “Step 05 - Introduction to BigDecimal Java Class” (6:58; item 9565516; access unconfirmed). The other BigDecimal videos are optional, not stacked. If inaccessible, use the named Java 21 documentation sections instead. The guide works exact versus premature rounding before the existing premium formula. Total: 6:58 + 14:02 + 4:00, or 7:00 official reading + 14:00 + 4:00; both fit 25 minutes.

**Primary reference:** [Java 21 `BigDecimal`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/math/BigDecimal.html). No purchase or enrollment.

## Day 7 — Deterministic time

No exact verified course lecture covers `Clock` injection. Use the [complete deterministic-time guide](teaching/day-07-deterministic-time.md) with official Java 21 `Clock.fixed`, `Instant.plusSeconds`, and `Instant.isBefore` sections. The guide works the exact-before/equal/after expiry rule and retained snapshot value before the quote-version task. Total: 6 minutes reading + 15 minutes worked example + 4 minutes readiness = 25 minutes.

**Primary references:** [Java 21 `Clock`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Clock.html) and [Java 21 `Instant`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Instant.html).

## Day 8 — PostgreSQL schema and constraints

Use the [complete composite-constraint guide](teaching/day-08-composite-constraints.md) after the required “Step 03 - Launching up H2 Console and Creating Course Table in H2” (7:24; item 35019878). H2 is only table-concept background; the guide runs a separate disposable PostgreSQL schema and shows actual SQLSTATEs for pair mismatch, null, and duplicate rows. “Step 04 - Getting Started with Spring JDBC” (3:58; item 35019874) is optional later. Total: 7:24 + 13:36 worked guide + 4:00 readiness = 25 minutes. The reference does not apply or solve the learner migration.

**Primary reference:** PostgreSQL [constraints](https://www.postgresql.org/docs/current/ddl-constraints.html), not-null, primary-key, and foreign-key sections.

## Day 9 — Parameterized JDBC, resource ownership, and row mapping

Use the [complete raw-JDBC guide](teaching/day-09-jdbc-boundary.md) after “Step 07 - Querying Data using Spring JDBC” (5:36; item 35019858). It is higher-level background; the guide teaches the actual raw `DataSource`/`PreparedStatement` shape using a separate table, fresh PostgreSQL connections, bound parameters, explicit absence, and SQL-failure handling. “Step 06 - Inserting and Deleting Data using Spring JDBC” (9:01; item 35019864) is optional later. Total: 5:36 + 15:24 guide + 4:00 readiness = 25 minutes. Do not switch the adapter to JPA or `JdbcTemplate`.

**Primary references:** pgJDBC [query and result processing](https://jdbc.postgresql.org/documentation/query/), Java [`PreparedStatement`](https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/java/sql/PreparedStatement.html), and short [try-with-resources](https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html) lifetime example.

## Day 10 — Atomic write and rollback on one connection

Use the [complete JDBC transaction guide](teaching/day-10-jdbc-transaction.md) after “Step 58 - Introduction to Transaction Management” (4:43; item 7907610) and “Step 59 - Transaction Management - ACID Properties” (5:49; item 7907612), if accessible. Access is unconfirmed; no purchase or enrollment. “Step 63 - Implementing Transaction Management - 3 Things to Decide” (4:38) is optional later. If the course is unavailable, the guide names Java 21 `Connection` methods for the same 10:32 study budget. Total: 10:32 foundation + 10:28 worked trace + 4:00 readiness = 25 minutes. This repository seam owns one raw JDBC connection; don't import Spring `TransactionTemplate`, JPA, audit code, or proxy questions into today's task.

**Primary reference:** Java [`Connection`](https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/java/sql/Connection.html): `setAutoCommit`, `commit`, `rollback`, `close`.

## Day 11 — Immutable freshness metadata and rejection precedence

Use the [complete freshness-order guide](teaching/day-11-freshness-order.md). There is no exact preferred-course lesson for this pure guard; read Java 21 `Instant.isBefore` for 5 minutes, the fixed owner/expiry/version decision example for 16 minutes, and readiness for 4 minutes. Do not stack the earlier 19:16 isolation-level video bundle: it neither teaches this predicate nor supports the present implementation choice. The guide shows equality expiry, ordered rejection, callback counts, and the check/write race limitation before the policy task.

**Primary reference:** Java 21 [`Instant.isBefore`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Instant.html).

## Day 12 — Object ownership beside the real security test fixture

Required course remains “Step 21 - Understanding Spring Security Authentication” (4:17; item 35017628) and “Step 22 - Exploring Spring Security Authorization” (9:47; item 35017626), total 14:04. Use the remaining 6:56 to trace the actual harness and 4:00 for readiness; total 25 minutes. The instructor harness authenticates by POSTing the in-memory test username/password to `/login` with a CSRF token, then retains the resulting session. It uses form login and session cookies, not HTTP Basic. The filter chain, users, and controller exist only in the test context.

Run `./mvnw -Dtest=Day12SecurityHarnessTest test` for the four untagged instructor checks: anonymous protected-harness identity returns 401, a valid form-login session identifies `owner-a`, CSRF-less/invalid writes return 403, and the tokenized test write succeeds. The actual quote-selection route is checked only for CSRF behavior; do not infer production authentication from that test. The owner/other journey read is separately tagged learner acceptance; run `./mvnw -Plearner-tests -Dtest=Day12SecurityHarnessTest#ownerScopedJourneyHttpReadUsesLearnerPolicy test` for the owner-policy TODO, which remains expected red. Its 200/404 outcomes are target acceptance behavior, not achieved production security. The harness is not production authentication and creates no accounts or credentials.

**Readiness question:** Who vouches for identity in the form-login test session, and why is a request-body `ownerId` not proof? Distinguish the instructor filter-chain checks from the tagged learner owner-policy case.

## Day 13 — Trace real workflow logs and readiness evidence

Conditional course reference: *Learn DevOps: Docker, Kubernetes, Terraform and Azure DevOps* (in28Minutes Official; access unconfirmed), “Getting Started with Continuous Integration, Deployment and Delivery” (4:50; item 18086493). If unavailable, use the official [GitHub Actions workflow-run logs](https://docs.github.com/en/actions/how-tos/monitor-workflows/use-workflow-run-logs) guide for the same 4:50. Trace the existing workflow and the example below for 16:10, then use 4:00 for readiness; total 25 minutes. Do not install Azure DevOps or Jenkins.

**Hypothetical log trace, not an observed run:** the workflow step named “Verify Java infrastructure and instructor harness” runs `./mvnw -B -ntp test`. If a test in that step fails, Maven exits 1; GitHub marks that named step failed and skips the later frontend-install step. The following excerpt is illustrative only:

```text
Verify Java infrastructure and instructor harness
$ ./mvnw -B -ntp test
[ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0
Error: Process completed with exit code 1.
```

No failure or remote run is claimed here. To record real evidence, inspect that exact step in the actual run log and record its job URL, step name, exit result and timestamp. A local reproduction is separate evidence; a future date or passing infrastructure selector cannot establish remote Actions success. The learner-owned Day 13 policy is tested separately with `./mvnw -Plearner-tests -Dtest=Day13ContractTest test` and remains TODO-red until implemented.

The existing evidence tuple `[true,false,false,true,false,true]` means compile observed, infrastructure failed, selected acceptance failed, clean command documented, failure propagation unproven, calendar independent. Its expected result is `ready=false` with blockers `infrastructure_tests`, `selected_acceptance_tests`, `failure_propagation`. The variation changes only clean-command evidence to false; predict its blocker before running the reference.

**Readiness question:** What does a green infrastructure suite fail to prove about learner logic and remote CI? Name one actual step/output or say that no hosted run was observed.

## Day 14 — Trace and diagnose the real backend slice

This is review/integration, not another course. If useful, revisit only unfamiliar parts of owned course 4993276: “Step 04 - What's happening in the background? Spring Boot Starters & Autoconfig” (9:06; item 35018830) or “Step 13 - Understanding Spring Boot vs Spring vs Spring MVC” (3:55; item 35019898), within the same 25 minutes. They are optional refreshers, not required new theory.

Before the existing acceptance command, read this annotated path. Arrows mean a method call or value handoff; `[DB]` marks a database operation; `STOP` means later stages are unobserved if that step fails:

```text
fixture DDL + journey row [DB]
  → catalog.findByCode("homesecure")
  → eligibility check + premium calculation
  → QuoteVersionService creates immutable quote snapshot
  → QuoteRepositoryAdapter.insert [DB]
  → JourneyAccessPolicy checks the test principal against owner
  → QuoteFreshnessPolicy checks the fixed snapshot at current time
  → SelectionService.persistBatch [DB]
  → fresh repository read + direct SQL selection readback [DB]
```

In `Day14BackendSliceIT`, the first handoff uses the existing learner-owned `ProductCatalogService.findByCode` seam. If its explicit Day 4 TODO throws, mark `STOP` there: eligibility, premium, quote creation, persistence, owner check, freshness, selection and readback have not been observed. The table describes the test's intended wiring, not a successful learner run. Once the learner's actual implementation is ready, run `./mvnw -Plearner-tests -Dtest=Day14BackendSliceIT test` and record the first observed result. The existing practice budget remains 65 minutes build/test + 20 minutes mock; if blocked, use the mock to explain the observed stop and the next diagnostic, not to stage a pretend success.

**Readiness question:** Trace one observed positive path or identify its actual first failing boundary. Name a previously taught transaction or value-ownership guarantee. No extra architecture assignment or Day 15 dependency.

## Day 15 — Join, ownership, and projection

Use the [Day 15 worked guide](teaching/day-15-join-projection.md#worked-trace) and executable PostgreSQL reference. It teaches parent/child cardinality, absent versus empty, and an allowlisted projection before the learner query task.
## Day 16 — Stable pagination

Use the [Day 16 worked guide](teaching/day-16-stable-pagination.md#worked-trace) and executable PostgreSQL reference. It uses the contract order `created_at DESC, quote_version_id ASC`; do not use the older illustrative `id DESC` order.
## Day 17 — Accessible typed UI states

Use the [Day 17 worked guide](teaching/day-17-render-typed-state.md#worked-trace) and rendered React tests. The toy renders loading/error/empty/ready props and labelled controls; it performs no fetch.
## Day 18 — Await the form Promise

Use the [Day 18 worked guide](teaching/day-18-await-form-promise.md#worked-trace) and controlled-promise React tests. It teaches the `Promise<void>` boundary, duplicate-submit guard, and cleanup on both outcomes; it does not change the capstone validation contract.
## Day 19 — Latest-request-wins refresh

Use the [Day 19 worked guide](teaching/day-19-latest-refresh.md#worked-trace) and deferred-promise tests. The existing seam is an event callback; TanStack Query is not required, and cancellation is not claimed.
## Day 20 — Truthful mutation outcomes

Use the [Day 20 worked guide](teaching/day-20-truthful-mutation.md#worked-trace) and executable React tests. The injected selection function returns `Promise<void>`; do not read a status from its resolved value or retry blindly.
## Day 21 — CSRF boundary

Use the [complete Day 21 CSRF guide](teaching/day-21-csrf-boundaries.md): required owned-course Step 08 (11:08), then the server-issued cookie/session/header trace in the existing 25-minute block. Instructor HTTP check: `./mvnw -Dtest=Day21SecurityInfrastructureTest test`. Learner acceptance: `./mvnw -Plearner-tests -Dtest=Day21ContractTest test`; it calls the actual `JourneyAccessPolicy` and remains blocked at its Day 12 TODO. The instructor harness is not production security evidence.

**Primary reference:** [Spring Security CSRF protection](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)

## Day 22 — Correlation and privacy

Use the [complete Day 22 correlation guide](teaching/day-22-correlation.md) and run `./mvnw -Dtest=Day22CorrelationReferenceTest test`. The executable toy checks ID selection before the handler, response/header agreement, audit only for errors, and unsafe-ID replacement. Apply the actual capstone 64-character grammar only after the toy is understood.

**Primary reference:** [Spring Boot logging](https://docs.spring.io/spring-boot/reference/features/logging.html)

## Day 23 — Liveness, readiness, and exposure

Use the [complete Day 23 readiness guide](teaching/day-23-readiness.md). `./mvnw -Dtest=Day23ActuatorInfrastructureTest test` exercises a real Actuator server and disposable PostgreSQL probe with a test-only projection witness. It verifies exact status-only health/readiness bodies, UP/DOWN HTTP codes, and disabled `/env` and `/configprops`. The learner-tagged `Day23HealthProjectionContractTest` remains red at `HealthResponseProjection.publicOptions()` until implemented; `Day23ContractTest` separately covers the database readiness indicator TODO.

**Primary reference:** [Spring Boot Actuator endpoints](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html)

## Day 24 — Read a PostgreSQL plan

Use the [complete Day 24 plan guide](teaching/day-24-explain.md), the two conditional index lectures (3:40 total; access unconfirmed), and the 25-minute cap. Run `./mvnw -Dtest=Day24ExplainReferenceTest test` for a disposable PostgreSQL example with actual rows and plans. Transfer the method, not the toy index, to the existing query.

**Primary reference:** [PostgreSQL using EXPLAIN](https://www.postgresql.org/docs/18/using-explain.html)

## Day 25 — Concurrent idempotent selection

Use the [complete Day 25 transaction guide](teaching/day-25-unique-race.md) and run `./mvnw -Dtest=Day25TwoTransactionReferenceTest test`. The real PostgreSQL toy checks the held winner, SQLSTATE 23505, loser rollback, fresh-connection read, and one final row. The learner acceptance stays separate.

**Primary reference:** [PostgreSQL unique constraints](https://www.postgresql.org/docs/18/ddl-constraints.html)

## Day 26 — Atomic audit and minimization

Use the [complete Day 26 audit guide](teaching/day-26-audit.md) and run `./mvnw -Dtest=Day26AuditReferenceTest test`. The real disposable PostgreSQL reference verifies first insert, repeat without another event, and trigger failure rolling back both rows; it checks exact fields and row counts.

**Primary reference:** [Spring programmatic transaction management](https://docs.spring.io/spring-framework/reference/data-access/transaction/programmatic.html)

## Day 27 — Regression hypothesis and scope

Use the [worked boundary example](teaching/day-27-regression-hypothesis.md). It shows the exact expected failure at 1,000 g, distinguishes a comparison bug from an input-unit bug, and reruns both neighboring boundaries. Then inspect the saved capstone reproducer; its expected TODO failure is not evidence of a production regression. Course access is unconfirmed; Good Unit Tests (5:34) and Response Matchers (5:07) are conditional partial support, while Mockito Verify (6:40) is optional only. Total study cap: 25 minutes.

**Primary reference:** [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)

## Day 28 — Browser/API/database review

Use the [integrated-gate review](teaching/day-28-integrated-review.md) to trace only actual observations through the installed harness. This is review-only and adds no lecture. The existing milestone uses 65 minutes for build/test and 20 minutes for the mock inside the same 85-minute practice block.

**Primary reference:** [Spring Security CSRF protection](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)

## Day 29 — Safe disposable reset

Use the [reset-guard walkthrough](teaching/day-29-reset-safety.md). The required Profiles lesson is 7:05; ConfigurationProperties and Embedded Servers are optional later references, not stacked into today. The only supplied executable uses its own disposable embedded PostgreSQL database. Do not use external URI mode for this exercise.

**Primary reference:** [Repository reset script](../../scripts/reset-synthetic-homesecure.sh)

## Day 30 — Evidence-based review

Use the [evidence-state walkthrough](teaching/day-30-evidence-review.md) during the existing review/milestone block. It distinguishes component evidence, blocked integration, and actual end-to-end evidence. No new architecture or SDE II task is assigned. The 20-minute mock is inside practice (65 build/test +20 mock).

**Primary reference:** [Day 30 checklist](acceptance-checklist.md)
