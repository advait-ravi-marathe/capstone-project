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

Required course remains “Step 21 - Understanding Spring Security Authentication” (4:17; item 35017628) and “Step 22 - Exploring Spring Security Authorization” (9:47; item 35017626), total 14:04. Then spend 6:56 tracing the already installed test plus 4:00 readiness: 25 minutes total. The test configuration supplies two in-memory test users (`owner-a`, `owner-b`) and a real Spring Security filter chain. A trusted principal for `owner-a` reads its seeded `journey-a`; `owner-b` receives concealed 404. A forged `ownerId` request value does not replace `Principal`. Anonymous identity is rejected at the HTTP filter boundary; a unit policy call alone is not proof of HTTP authentication. A write without a CSRF token is 403; with the test token it reaches the test adapter. This harness does not install production security or create credentials/accounts.

**Readiness question:** Who vouches for identity, and why is a request-body owner value not proof? Use existing tests; do not add JWT, accounts, or security configuration changes.

## Day 13 — Trace real workflow logs and readiness evidence

Conditional course reference: *Learn DevOps: Docker, Kubernetes, Terraform and Azure DevOps* (in28Minutes Official; access unconfirmed), “Getting Started with Continuous Integration, Deployment and Delivery” (4:50; item 18086493). If unavailable, use the official [GitHub Actions workflow-run logs](https://docs.github.com/en/actions/how-tos/monitor-workflows/use-workflow-run-logs) topic for the same 4:50. Spend 16:10 tracing the checked-in workflow and frozen evidence example, then 4:00 on readiness: 25 minutes total. No Azure/Jenkins setup.

The worked evidence record `[true,false,false,true,false,true]` means: compile observed, infrastructure failed, selected acceptance failed, clean command documented, failure propagation unproven, and calendar independent. The result is `ready=false`, with ordered blockers `infrastructure_tests`, `selected_acceptance_tests`, `failure_propagation`. An actual workflow step with nonzero exit stops the job; identify it in the run log separately from local reproduction. A green local test is not a remote Actions result, and a future date is not evidence. The existing practice variation changes only clean-command evidence to false and predicts its blocker.

**Readiness question:** What could a green infrastructure build still fail to establish? Existing commands remain `./mvnw -Dtest=Day13WorkflowInfrastructureTest test` and `./mvnw -Plearner-tests -Dtest=Day13ContractTest test`; the latter is the learner policy task.

## Day 14 — Trace and diagnose the real backend slice

This is review/integration, not another course. If useful, revisit only unfamiliar parts of owned course 4993276: “Step 04 - What's happening in the background? Spring Boot Starters & Autoconfig” (9:06; item 35018830) or “Step 13 - Understanding Spring Boot vs Spring vs Spring MVC” (3:55; item 35019898), within the same 25 minutes. No new theory or rejected-abstraction essay is assigned.

Before the existing integration attempt, annotate the actual test with a small legend: call arrow, value handoff, database write, failure stop. The path is catalog → eligibility/premium → quote snapshot → repository → selection guard/persistence → direct PostgreSQL readback. If a learner component such as product lookup is still its named TODO, downstream success is unobserved: record the earliest failing boundary, rerun its focused selector after actual repair, then rerun `./mvnw -Plearner-tests -Dtest=Day14BackendSliceIT test`. This diagnosis is a worked hypothetical until you observe current output. The existing 85-minute practice remains 65 build/test + 20 mock; use the mock to explain an actual blocker, not to pretend the path passed.

**Readiness question:** Trace one real positive or blocked path and name one prior-taught transaction boundary. No fake integration, added feature, or Day 15 dependency.

## Day 15 — Join and projection

A SQL join combines related rows by their key; a projection selects only fields needed by the response. For the supplied journey/quote fixture, trace which rows match and which response columns are selected. Distinguish no matching rows (empty collection) from a missing required parent (not found) using the contract. Do not widen the query to return unrelated or sensitive columns.


**Concrete example:** Illustrative read: `SELECT j.id, q.version_id FROM journey j JOIN quote q ON q.journey_id = j.id WHERE j.id = ?`. Select only response fields and use the fixture to distinguish an empty match from a missing parent.

**Primary reference:** [PostgreSQL table expressions and joins](https://www.postgresql.org/docs/18/queries-table-expressions.html)

## Day 16 — Stable pagination

A page needs a deterministic order. Ordering only by a non-unique timestamp can move equal-time rows between pages; add the contract's stable unique tie-breaker. `LIMIT`/`OFFSET` choose a slice after ordering. Use the supplied page examples to check first, middle, and empty pages; do not claim the JPA lecture teaches PostgreSQL plan costs.


**Concrete example:** Illustrative order: `ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?`. The unique tie-breaker makes equal timestamps deterministic; use the contract’s exact sort and page defaults.

**Primary reference:** [PostgreSQL `LIMIT` and `OFFSET`](https://www.postgresql.org/docs/18/queries-limit.html)

## Day 17 — Typed props and UI state

A typed prop documents what a child component may receive. A discriminated union can represent mutually exclusive UI states, for example `{status: 'loading'}` or `{status: 'ready', items: Item[]}`; it avoids combinations such as “loading and failed” unless explicitly allowed. The course lesson supports typed props, not this project's complete state model. Adapt the supplied state table without implementing extra screens.


**Concrete example:** Illustrative state: `type LoadState = {status:"loading"} | {status:"ready"; items: Item[]} | {status:"error"; message: string}`. Render each case explicitly; this example does not implement the assigned component.

**Primary reference:** [React TypeScript guide](https://react.dev/learn/typescript)

## Day 18 — Input and server errors

Keep the field value as the source of truth while editing. On submit, map a server validation response to the relevant field or form-level message; do not assume client checks replace server validation. The fixed contract states which errors are field-specific. Use one valid and one invalid example and preserve the entered value when showing an error.


**Concrete example:** Example response mapping: `{field:"rebuildAmount", code:"OUT_OF_RANGE"}` becomes a message next to that field; keep the typed value so the learner can correct it. The server remains authoritative.

**Primary reference:** [React input reference](https://react.dev/reference/react-dom/components/input)

## Day 19 — Async ownership and TanStack Query

The preferred course has a matching module, “React Query / Tanstack Query: Handling HTTP Requests With Ease”; Day 19 assigns “Dynamic Query Functions & Query Keys” and “The Query Configuration Object & Aborting Requests” (18:32 total). The fixed Day 19 acceptance contract uses controlled promises to prove latest-request-wins and does not currently install TanStack Query. Treat the course as the concept reference; do not add a runtime dependency to this scaffold solely to match a video. In the remaining study time, connect query-key identity, cache ownership, and abort signal to the fixed behavior. Preserve any later task that explicitly requires TanStack Query rather than silently replacing it with `useEffect`.


**Concrete example:** Pattern: `useQuery({queryKey:["quote", journeyId], queryFn: ({signal}) => loadQuote(journeyId, signal)})`. Keep server data in the query cache; use the supplied mutation/invalidation behavior after a successful write. Do not add automatic write retries.

**Primary reference:** [TanStack Query queries](https://tanstack.com/query/latest/docs/framework/react/guides/queries) and [query cancellation](https://tanstack.com/query/latest/docs/framework/react/guides/query-cancellation)

## Day 20 — Honest mutation states and retry

A write UI should distinguish pending, confirmed success, and failure. After an ambiguous network failure, the server may already have committed; retry only when the endpoint's documented idempotency behavior makes it safe. The backend POST lessons cover status semantics, not frontend retry safety. Use the fixed scenarios and do not add automatic retries.


**Concrete example:** A minimal UI state mapping is pending → disable duplicate click, success → show server-confirmed result, error → show safe message and an explicit retry option only when the API contract permits retry. Never infer success from a click.

**Primary reference:** [TanStack Query mutations](https://tanstack.com/query/latest/docs/framework/react/guides/mutations)

## Day 21 — CSRF boundary

A browser automatically attaches a session cookie, which is why a state-changing request needs the contract's CSRF token/header. CORS controls which origins may read/use browser responses; it does not replace CSRF protection. Verify the existing cookie-session flow and keep the assigned CSRF protections enabled.


**Concrete example:** For the cookie session, a state-changing request includes the server-issued CSRF value in the configured header. Missing/invalid token is rejected; CORS configuration is a separate browser-origin rule.

**Primary reference:** [Spring Security CSRF protection](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)

## Day 22 — Correlation and privacy

A request ID is diagnostic metadata, not identity or authorization. Generate/validate it at the boundary, return the same value in the response, and include it in logs without logging quote payloads, credentials, or personal data. Follow the fixed format and test that separate requests get distinct IDs; do not expose the full request body.


**Concrete example:** Boundary sketch: accept a valid request ID or generate one; place it in response header and logging context; clear context after the request. Do not put customer or quote payloads into logs.

**Primary reference:** [Spring Boot logging](https://docs.spring.io/spring-boot/reference/features/logging.html)

## Day 23 — Liveness, readiness, and exposure

Liveness asks whether the process should be restarted; readiness asks whether it can serve traffic now. A dependency failure may make the app unready without proving the process is irrecoverably dead. Expose only the contract-approved health information; do not copy a tutorial's broad actuator exposure setting. Confirm the expected response fields in the existing test.


**Concrete example:** Check only the configured health groups: liveness represents process viability; readiness includes whether the app can accept work. Do not expose environment, beans, or all actuator endpoints as part of this task.

**Primary reference:** [Spring Boot Actuator endpoints](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html)

## Day 24 — Read a PostgreSQL plan

`EXPLAIN` shows a planned access path without executing the query; compare estimated rows and scan choices for the supplied selective lookup. An index can help reads but adds storage and write cost. Do not infer actual speed from one plan or run `EXPLAIN ANALYZE` on a mutating query. The course index lesson is conceptual; use PostgreSQL's official `EXPLAIN` documentation and the repository's fixed query.


**Concrete example:** Example command: `EXPLAIN SELECT id FROM quote WHERE journey_id = 42 ORDER BY created_at DESC;`. Inspect estimated rows and chosen scan; this is not a promise of latency, and plain EXPLAIN does not execute the query.

**Primary reference:** [PostgreSQL using EXPLAIN](https://www.postgresql.org/docs/18/using-explain.html)

## Day 25 — Concurrent idempotent selection

Two requests can both observe “not selected” before either writes. A database unique constraint is the final race-safe guard; the losing request must map the duplicate outcome to the contract's response without creating a second row. Trace the supplied two-request test and count committed rows. A Java synchronized block cannot protect multiple application instances.


**Concrete example:** The final database guard is an illustrative `UNIQUE (journey_id, quote_version_id)`. If requests A and B race, one insert wins; the other observes the mapped duplicate outcome; committed row count remains one.

**Primary reference:** [PostgreSQL unique constraints](https://www.postgresql.org/docs/18/ddl-constraints.html)

## Day 26 — Atomic audit and minimization

Write the business change and its audit record in the same transaction so neither can commit alone. Record only contract-approved identifiers/action/time; never copy the full request, credentials, or unrelated personal fields. Use the supplied accepted/rejected snapshots to check both atomicity and data minimization.


**Concrete example:** Illustrative transaction: `insert business row; insert audit(action, subject_id, occurred_at); commit`. A failure of the audit insert must roll back the business change too; omit payloads and credentials.

**Primary reference:** [Spring programmatic transaction management](https://docs.spring.io/spring-framework/reference/data-access/transaction/programmatic.html)

## Day 27 — Test compatibility

Read the test's arrange/act/assert boundaries and verify only behavior owned by the collaborator. The selected course is a conditional in28Minutes reference; its JUnit/Spring versions may differ from this repository. Follow this project's imports, dependency BOM, and existing test idioms rather than upgrading dependencies to match a lesson. A mock verification should prove an observable interaction, not mirror every implementation line.


**Concrete example:** Use the repository’s existing test stack: arrange fixture → call one public behavior → assert response and observable collaborator behavior. Do not copy version-specific imports from a course until they match the project BOM.

**Primary reference:** [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)

## Day 28 — Browser/API/database trace

Use the installed harness to follow one browser action through the session cookie and CSRF header, API response, and PostgreSQL snapshot. Check both the visible result and the no-write/one-write evidence. Do not treat a bearer-token demo as equivalent to this app's cookie-session contract. This is review of existing behavior; no new lecture bundle is required.


**Concrete example:** Take one browser action and record request method/path, cookie and CSRF presence, status/body, then compare the database before/after snapshot. Expected ownership and row-count assertions come from the installed test.

**Primary reference:** [Spring Security CSRF protection](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)

## Day 29 — Safe fixture reset

Only use the repository's guarded synthetic fixture reset command against its disposable embedded test database. Read the script's guard before running it; never point it at a personal, shared, or production database. Run it twice and compare the exact synthetic rows to establish repeatability. Profiles/configuration lessons do not authorize destructive resets or cloud resources.


**Concrete example:** Use exactly `./scripts/reset-synthetic-homesecure.sh --embedded-test --confirm-disposable`; first inspect its guard and confirm the embedded test database. Never substitute a live URI or remove the explicit guard.

**Primary reference:** [Repository reset script](../../scripts/reset-synthetic-homesecure.sh)

## Day 30 — Explain observed behavior

Choose one earlier fixed test and explain its input, expected result, and the evidence that proves it. If the explanation exposes a gap, revisit that day's named lesson or worked guide within the existing allocation. This is a review and communication day, not an extra architecture assignment.


**Concrete example:** Choose one already-installed test, write its given/when/then in three lines, and point to the assertion that proves the result. This checks explanation against observed behavior, not architecture vocabulary.

**Primary reference:** [Day 30 checklist](acceptance-checklist.md)
