# Short worked guides for Days 1–30

These are instructor-provided bridges for topics the verified course titles only partly cover or do not cover. Use the relevant guide inside that day's existing 25-minute course/concept allocation. They are not a replacement for the learner TODO, acceptance test, or independent explanation. If course viewing plus pauses runs over 25 minutes, stop and carry unfinished material forward; reduce implementation scope. Code fragments illustrate a concept and are not a completed assignment solution.

## Day 1 — First API bridge

A request such as `GET /hello` is matched by Spring MVC to a controller method. The method returns a Java value (for example, a small record); Spring's HTTP message converter serializes that value as JSON. First reproduce the instructor's Hello World endpoint and returned bean. Only after that works, change one harmless field or path and predict the response. Explain aloud: request → route/controller method → Java result → JSON response. The quote-selection endpoint remains a later adaptation; this bridge does not satisfy its acceptance cases.

## Day 4 — Immutable catalog values

A Java `record Product(String code, String displayName)` is a compact value carrier. `List.copyOf(input)` prevents callers from changing the collection through an alias; it does not validate duplicate codes or blank fields. Inspect one supplied catalog fixture and identify which checks belong at construction versus request validation. Keep those invariants as explicit learner work.

## Day 5 — Pure eligibility decision

Treat eligibility as a function of an input and fixed rules: same input, same decision, no database writes or clock reads. Make a two-column decision table from the contract's allowed and denied examples, then identify the first rule that rejects each denied case. Implement only the assigned policy seam; do not add an unrequested rule.

## Day 6 — Decimal calculation and rounding

`new BigDecimal("12.30")` preserves decimal intent; `new BigDecimal(12.30)` starts from a binary floating-point approximation. Multiplication does not itself choose the business rounding rule. Apply the contract's scale/mode only at the named premium boundary, then compare the supplied boundary examples. The formula and policy are fixed by the fixture; do not invent pricing rules.

## Day 7 — Deterministic time

`Clock` supplies wall-clock instants to code that needs business time. A test can use `Clock.fixed(instant, ZoneOffset.UTC)` so expiry behavior is repeatable. Wall time answers “what time is it?”; elapsed-time measurement answers “how long did this take?” and uses a monotonic source such as `System.nanoTime()`. Inject the provided clock seam; do not use sleeps or change the expiry rule.

## Day 8 — PostgreSQL schema and constraints

A primary key identifies a row; a unique constraint enforces a business uniqueness rule even under concurrent requests; a foreign key enforces a relationship. The supplied PostgreSQL migration is authoritative. Read its column types and constraints, then trace one invalid insert to the database constraint. The course's H2 example is introductory only; do not switch databases or add ORM setup.

## Day 10 — Atomic write and rollback

A transaction groups related SQL writes so they commit together or roll back together. For the supplied two-write example, ask what remains if the second write fails: the expected answer is neither partial write survives. Follow the current Spring/JDBC transaction boundary and its fixed test; do not replace it with JPA or add manual commits.

## Day 11 — Version check versus lost update

Two clients can read version 4. If client A writes version 5 first, client B's update must require `WHERE id = ? AND version = 4`; zero affected rows means the earlier view is stale. Isolation-level lessons explain transaction visibility but do not implement this optimistic check. Use the contract's version conflict example and preserve its exact conflict response.

## Day 12 — Object ownership

Authentication answers who is making the request; authorization answers whether that identity may access this object. Compare the authenticated principal's owner key with the journey's owner key before returning or mutating the journey. A guessed object ID is not proof of ownership. Follow the fixed anonymous/foreign-owner tests; do not reveal whether another user's object exists.

## Day 13 — CI concept to this repository

A CI workflow checks out a commit, installs the declared runtime, restores dependencies, and runs the repository's documented checks. In `.github/workflows/coverstory-starter.yml`, trace each step to its command and identify which failure would stop later steps. The in28Minutes lecture is conceptual; use the existing GitHub Actions workflow as the concrete example. Do not configure Azure DevOps, Jenkins, or deployment.

## Day 14 — Request/data ownership trace

Draw one request through controller → policy/service → repository → PostgreSQL and back. At each arrow name the input/output type and the layer that owns validation, authorization, and transaction scope. Use the existing fixed test to confirm the trace; this is a review exercise, not a new architecture task.

## Day 15 — Join and projection

A SQL join combines related rows by their key; a projection selects only fields needed by the response. For the supplied journey/quote fixture, trace which rows match and which response columns are selected. Distinguish no matching rows (empty collection) from a missing required parent (not found) using the contract. Do not widen the query to return unrelated or sensitive columns.

## Day 16 — Stable pagination

A page needs a deterministic order. Ordering only by a non-unique timestamp can move equal-time rows between pages; add the contract's stable unique tie-breaker. `LIMIT`/`OFFSET` choose a slice after ordering. Use the supplied page examples to check first, middle, and empty pages; do not claim the JPA lecture teaches PostgreSQL plan costs.

## Day 17 — Typed props and UI state

A typed prop documents what a child component may receive. A discriminated union can represent mutually exclusive UI states, for example `{status: 'loading'}` or `{status: 'ready', items: Item[]}`; it avoids combinations such as “loading and failed” unless explicitly allowed. The course lesson supports typed props, not this project's complete state model. Adapt the supplied state table without implementing extra screens.

## Day 18 — Input and server errors

Keep the field value as the source of truth while editing. On submit, map a server validation response to the relevant field or form-level message; do not assume client checks replace server validation. The fixed contract states which errors are field-specific. Use one valid and one invalid example and preserve the entered value when showing an error.

## Day 19 — Async ownership and TanStack Query

For the request task, identify who owns cached server data, when it becomes stale, and what invalidates/refetches it. Where the contract calls for TanStack Query, use the repository's supplied query-client pattern and fixed query key; generic `useEffect` fetching is only background, not a substitute. A request may finish after a component is no longer interested: use the supplied cancellation/stale-result behavior and explain which result is allowed to update the view. No exact verified TanStack Query lecture was identified in the supplied catalog, so this project guide is the bridge.

## Day 20 — Honest mutation states and retry

A write UI should distinguish pending, confirmed success, and failure. After an ambiguous network failure, the server may already have committed; retry only when the endpoint's documented idempotency behavior makes it safe. The backend POST lessons cover status semantics, not frontend retry safety. Use the fixed scenarios and do not add automatic retries.

## Day 21 — CSRF boundary

A browser automatically attaches a session cookie, which is why a state-changing request needs the contract's CSRF token/header. CORS controls which origins may read/use browser responses; it does not replace CSRF protection. Verify the existing cookie-session flow and keep the assigned CSRF protections enabled.

## Day 22 — Correlation and privacy

A request ID is diagnostic metadata, not identity or authorization. Generate/validate it at the boundary, return the same value in the response, and include it in logs without logging quote payloads, credentials, or personal data. Follow the fixed format and test that separate requests get distinct IDs; do not expose the full request body.

## Day 23 — Liveness, readiness, and exposure

Liveness asks whether the process should be restarted; readiness asks whether it can serve traffic now. A dependency failure may make the app unready without proving the process is irrecoverably dead. Expose only the contract-approved health information; do not copy a tutorial's broad actuator exposure setting. Confirm the expected response fields in the existing test.

## Day 24 — Read a PostgreSQL plan

`EXPLAIN` shows a planned access path without executing the query; compare estimated rows and scan choices for the supplied selective lookup. An index can help reads but adds storage and write cost. Do not infer actual speed from one plan or run `EXPLAIN ANALYZE` on a mutating query. The course index lesson is conceptual; use PostgreSQL's official `EXPLAIN` documentation and the repository's fixed query.

## Day 25 — Concurrent idempotent selection

Two requests can both observe “not selected” before either writes. A database unique constraint is the final race-safe guard; the losing request must map the duplicate outcome to the contract's response without creating a second row. Trace the supplied two-request test and count committed rows. A Java synchronized block cannot protect multiple application instances.

## Day 26 — Atomic audit and minimization

Write the business change and its audit record in the same transaction so neither can commit alone. Record only contract-approved identifiers/action/time; never copy the full request, credentials, or unrelated personal fields. Use the supplied accepted/rejected snapshots to check both atomicity and data minimization.

## Day 27 — Test compatibility

Read the test's arrange/act/assert boundaries and verify only behavior owned by the collaborator. The selected course is a conditional in28Minutes reference; its JUnit/Spring versions may differ from this repository. Follow this project's imports, dependency BOM, and existing test idioms rather than upgrading dependencies to match a lesson. A mock verification should prove an observable interaction, not mirror every implementation line.

## Day 28 — Browser/API/database trace

Use the installed harness to follow one browser action through the session cookie and CSRF header, API response, and PostgreSQL snapshot. Check both the visible result and the no-write/one-write evidence. Do not treat a bearer-token demo as equivalent to this app's cookie-session contract. This is review of existing behavior; no new lecture bundle is required.

## Day 29 — Safe fixture reset

Only use the repository's guarded synthetic fixture reset command against its disposable embedded test database. Read the script's guard before running it; never point it at a personal, shared, or production database. Run it twice and compare the exact synthetic rows to establish repeatability. Profiles/configuration lessons do not authorize destructive resets or cloud resources.

## Day 30 — Explain observed behavior

Choose one earlier fixed test and explain its input, expected result, and the evidence that proves it. If the explanation exposes a gap, revisit that day's named lesson or worked guide within the existing allocation. This is a review and communication day, not an extra architecture assignment.
