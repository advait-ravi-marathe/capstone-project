# Short worked guides for Days 1–30

These are instructor-provided bridges for topics the verified course titles only partly cover or do not cover. Use the relevant guide inside that day's existing 25-minute course/concept allocation. They are not a replacement for the learner TODO, acceptance test, or independent explanation. If course viewing plus pauses runs over 25 minutes, stop and carry unfinished material forward; reduce implementation scope. Code fragments illustrate a concept and are not a completed assignment solution.

## Day 1 — First API bridge

Use the complete runnable lesson in [Day 1 API bridge](day-01-api-bridge.md). It maps the verified Step 02 Hello World and Step 03 returned-bean course lessons to actual loopback HTTP, a complete `@RestController` example, status/header/body observations, and a distinct learner practice TODO. Step 00 is an optional overview when request/API terms are unfamiliar; Step 01 is optional setup because the supplied test-runtime fixture handles setup. The guide includes the required 25/85/10-minute boundaries and does not solve the quote-selection contract.

**Primary references:** [Spring `@ResponseBody` and `@RestController`](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/responsebody.html), [Spring MVC request mappings](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html), and [Spring Boot Maven `test-run`](https://docs.spring.io/spring-boot/maven-plugin/run.html#run.test-run). These support the worked guide; the learner should run and observe the fixture rather than treat links as completion.

## Day 4 — Immutable catalog values

A Java `record Product(String code, String displayName)` is a compact value carrier. `List.copyOf(input)` prevents callers from changing the collection through an alias; it does not validate duplicate codes or blank fields. Inspect one supplied catalog fixture and identify which checks belong at construction versus request validation. Keep those invariants as explicit learner work.


**Concrete example:** Example: `record Product(String code, String name) {}` and `List<Product> snapshot = List.copyOf(input);`. This freezes collection membership; a separate check still has to reject duplicate/blank codes.

**Primary reference:** [Java records](https://docs.oracle.com/en/java/javase/21/language/records.html)

## Day 5 — Pure eligibility decision

Treat eligibility as a function of an input and fixed rules: same input, same decision, no database writes or clock reads. Make a two-column decision table from the contract's allowed and denied examples, then identify the first rule that rejects each denied case. Implement only the assigned policy seam; do not add an unrequested rule.


**Concrete example:** Example shape: `Eligibility decide(QuoteInput input, Rules rules)` returns a decision and performs no repository call. Write each fixed rule as one decision-table row, then map one allowed and one denied fixture to it.

**Primary reference:** [Java functional interfaces and lambdas](https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html)

## Day 6 — Decimal calculation and rounding

`new BigDecimal("12.30")` preserves decimal intent; `new BigDecimal(12.30)` starts from a binary floating-point approximation. Multiplication does not itself choose the business rounding rule. Apply the contract's scale/mode only at the named premium boundary, then compare the supplied boundary examples. The formula and policy are fixed by the fixture; do not invent pricing rules.


**Concrete example:** Example: `new BigDecimal("12.30").multiply(rate).setScale(2, RoundingMode.HALF_UP)`. The scale/mode shown is illustrative only; apply the capstone contract’s exact rounding rule instead.

**Primary reference:** [Java 21 `BigDecimal`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/math/BigDecimal.html)

## Day 7 — Deterministic time

`Clock` supplies wall-clock instants to code that needs business time. A test can use `Clock.fixed(instant, ZoneOffset.UTC)` so expiry behavior is repeatable. Wall time answers “what time is it?”; elapsed-time measurement answers “how long did this take?” and uses a monotonic source such as `System.nanoTime()`. Inject the provided clock seam; do not use sleeps or change the expiry rule.


**Concrete example:** Example: `Clock clock = Clock.fixed(Instant.parse("2026-10-20T12:00:00Z"), ZoneOffset.UTC);`. The business policy compares instants from the injected clock; do not sleep in tests or substitute a monotonic timer for expiry.

**Primary reference:** [Java 21 `Clock`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Clock.html)

## Day 8 — PostgreSQL schema and constraints

A primary key identifies a row; a unique constraint enforces a business uniqueness rule even under concurrent requests; a foreign key enforces a relationship. The supplied PostgreSQL migration is authoritative. Read its column types and constraints, then trace one invalid insert to the database constraint. The course's H2 example is introductory only; do not switch databases or add ORM setup.


**Concrete example:** Illustrative invariant: `UNIQUE (journey_id, quote_version_id)` makes the composite rule hold even when requests race. A `FOREIGN KEY` can enforce the related journey; the repository migration has the actual names and is authoritative.

**Primary reference:** [PostgreSQL constraints](https://www.postgresql.org/docs/18/ddl-constraints.html)

## Day 10 — Atomic write and rollback

A transaction groups related SQL writes so they commit together or roll back together. For the supplied two-write example, ask what remains if the second write fails: the expected answer is neither partial write survives. Follow the current Spring/JDBC transaction boundary and its fixed test; do not replace it with JPA or add manual commits.


**Concrete example:** Pattern only: `transactionTemplate.execute(status -> { insertSelection(); insertAudit(); return null; });`. If either statement fails, the transaction boundary must roll back both; follow the existing JDBC flow and acceptance test.

**Primary reference:** [Spring programmatic transaction management](https://docs.spring.io/spring-framework/reference/data-access/transaction/programmatic.html)

## Day 11 — Version check versus lost update

Two clients can read version 4. If client A writes version 5 first, client B's update must require `WHERE id = ? AND version = 4`; zero affected rows means the earlier view is stale. Isolation-level lessons explain transaction visibility but do not implement this optimistic check. Use the contract's version conflict example and preserve its exact conflict response.


**Concrete example:** Illustrative SQL: `UPDATE quote SET value = ?, version = version + 1 WHERE id = ? AND version = ?`. Check the affected-row count: zero means the expected version was stale. Use the contract’s response mapping.

**Primary reference:** [PostgreSQL `UPDATE`](https://www.postgresql.org/docs/18/sql-update.html)

## Day 12 — Object ownership

Authentication answers who is making the request; authorization answers whether that identity may access this object. Compare the authenticated principal's owner key with the journey's owner key before returning or mutating the journey. A guessed object ID is not proof of ownership. Follow the fixed anonymous/foreign-owner tests; do not reveal whether another user's object exists.


**Concrete example:** Guard shape: load the target, compare `principal.ownerId()` with `target.ownerId()`, and only then return or mutate it. Keep nonexistent and foreign-owner response behavior exactly as the contract specifies.

**Primary reference:** [Spring Security method authorization](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)

## Day 13 — CI concept to this repository

A CI workflow checks out a commit, installs the declared runtime, restores dependencies, and runs the repository's documented checks. In `.github/workflows/coverstory-starter.yml`, trace each step to its command and identify which failure would stop later steps. The in28Minutes lecture is conceptual; use the existing GitHub Actions workflow as the concrete example. Do not configure Azure DevOps, Jenkins, or deployment.


**Concrete example:** Trace the existing workflow from checkout → Java setup → Maven test → npm ci → frontend build/test. A failing step returns nonzero and stops the job; do not add deploy credentials or deploy stages.

**Primary reference:** [GitHub Actions workflow syntax](https://docs.github.com/en/actions/writing-workflows/workflow-syntax-for-github-actions)

## Day 14 — Request/data ownership trace

Draw one request through controller → policy/service → repository → PostgreSQL and back. At each arrow name the input/output type and the layer that owns validation, authorization, and transaction scope. Use the existing fixed test to confirm the trace; this is a review exercise, not a new architecture task.


**Concrete example:** Concrete trace: `POST` JSON → controller request object → validator → service/policy → JDBC adapter → transaction result → response DTO. At each step, point to the actual class and fixed test; do not invent a new layer.

**Primary reference:** [Spring MVC controller return values](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/responsebody.html)

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
