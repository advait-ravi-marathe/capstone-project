# CoverStory Day 1 starter — synthetic insurance quote selection

This starter reuses the verified Java 21, Maven, Spring MVC, and fixed-test
harness from the legacy capstone exercises. Day 1 begins a synthetic insurance
lifecycle with a small quote-selection domain rule. All products and quote IDs
are fixtures; no real insurer, customer, payment, or policy system is connected.
The learner implementation stays in one explicit TODO method.

## Run

With Java 21 and network access to Maven Central on first use:

```sh
./mvnw test
```

The official Maven Wrapper downloads Maven 3.10.0 and verifies its SHA-256
before use. The default run compiles the project and runs infrastructure smoke
checks. To run the visible learner acceptance tests (which are expected to
fail until the TODO is complete), use:

```sh
./mvnw -Plearner-tests test
```

## Day 1: preserve quote selection identity and order (120 engineering minutes)

Read [the Day 1 guide](docs/day-1.md). Implement `uniqueSelections` in
`src/main/java/assignments/insurance/QuoteSelectionPolicy.java`.

- A selection is identified by the exact pair `(journeyId, quoteVersionId)`.
- Keep only the first occurrence of each pair and preserve encounter order.
- Reject a null list, null entries, or blank key parts with
  `IllegalArgumentException`; never return partial output.
- Do not mutate the input, trim values, sort, persist, or infer authorization
  from a journey identifier.

The visible behavioral acceptance tests are fixed and expected to fail until
the TODO is implemented. Instructor infrastructure checks only establish that
Java/Maven compilation and the provided Spring MVC route are available; they
do not implement the policy. After the TODO is implemented, the controller
maps valid input to HTTP 200 and invalid input to HTTP 400. Before then, valid
requests reach the explicit TODO and return HTTP 500; a missing body returns
HTTP 400.

Run infrastructure checks:

```sh
./mvnw -Dtest=QuoteSelectionInfrastructureTest test
```

Run learner acceptance separately (expected TODO failures):

```sh
./mvnw -Plearner-tests -Dtest=QuoteSelectionAcceptanceTest,QuoteSelectionApiAcceptanceTest test
```

The separate 60-minute Java DSA assignment is LeetCode 121. See the first
section of [the Day 1 guide](docs/day-1.md), implement
`src/main/java/assignments/dsa/BestTimeToBuySellStock.java`, and run:

```sh
./mvnw -Plearner-tests -Dtest=BestTimeToBuySellStockAcceptanceTest test
```

## Legacy exercises retained

The following earlier contracts remain in place and unchanged.

### CAP-43: event policy (90 minutes)

Implement the two TODO methods in
`src/main/java/assignments/events/EventPolicy.java`.

- `classify` is pure: it returns a decision and never changes the map.
- `accept` stores a payload only when the decision is `ACCEPT`.
- Null or blank owner ID, event ID, or payload is `INVALID`.
- A previously unseen `(ownerId, eventId)` pair is `ACCEPT`.
- The same pair with the same exact payload is `DUPLICATE`.
- The same pair with a different exact payload is `CONFLICT`.
- Do not trim or normalize strings. Assume a non-null mutable map and sequential calls.

The composite key is provided as a Java record so the assignment can focus on
the decision rules. Acceptance tests cover purity, replay and conflict
behavior, owner isolation, composite-key collisions, invalid inputs, and exact
string handling. Add one edge-case test of your own. In your notes, explain
why the logic is correct, its expected time complexity, and why sequential map
behavior does not provide concurrency safety.

The starter also includes infrastructure smoke checks that run before learner
logic is implemented. They confirm project compilation, a valid JSON request
reaches the supplied Spring MVC endpoint, and the project-local PostgreSQL
server starts and accepts JDBC connections. The embedded database uses a
loopback port and does not require Docker or a system-wide PostgreSQL install.

## Spring validation seam (30 minutes)

The instructor-provided request shape and controller are in
`src/main/java/assignments/events/EventRequest.java` and
`EventsController.java`. Complete the controller's validation TODO so missing
bodies, missing required fields, blank fields, and malformed JSON return HTTP
400. The controller is exercised with standalone Spring `MockMvc`; no server
or database is started. The validation acceptance checks run with
`./mvnw -Plearner-tests test` alongside CAP-43.

## CAP-42: PostgreSQL aggregation query (60 minutes)

Complete `src/test/resources/assignments/cap42/learner-query.sql`. Bind the
owner ID, start time, and end time as JDBC parameters in that order. Return one
row containing `accepted_count` and `signed_total`, count accepted events, sum
signed amounts, and use the half-open UTC window `[from_utc, to_utc)`. An empty
window must still return a count and decimal total of zero.

The fixed fixtures use unique event IDs and one declared currency (USD). The
main window is `2026-10-13T00:00:00Z` through `2026-10-14T00:00:00Z`, with the
upper bound excluded. The harness starts real PostgreSQL 14.22 from
test-scoped native binaries using
`io.zonky.test:embedded-postgres:2.2.2`; Darwin ARM64 and Linux amd64 binaries
are available for Apple Silicon and cloud/Linux checkouts. It does not use H2
or Docker. PostgreSQL tests
are part of `./mvnw -Plearner-tests test` and intentionally fail until the SQL
TODO is completed.

## CAP-44: retrieval ranking and provenance (60 minutes)

Read [docs/assignments/CAP-44.md](docs/assignments/CAP-44.md). The standalone
Python starter is `ai/retrieval/ranking.py`; instructor types and tokenization
are provided, while `rank_runbooks` raises `NotImplementedError` for the
learner. The JSON corpus and acceptance tests cover exact owner authorization,
scores, source/version tie ordering, unchanged excerpts, and invalid or
duplicate provenance. No network access is needed.

Run the setup smoke checks with:

```sh
python3 -m unittest discover -s ai/tests -p test_infrastructure.py
```

Run the learner assertions (expected to fail until the TODO is complete) with:

```sh
python3 -m unittest discover -s ai/tests -p test_ranking.py
```

CAP-44's comparison with CAP-41 is pending: this checkout contains no CAP-41
baseline, and no comparison scores are claimed. CAP-44 itself is independently
runnable.

## CAP-45: SQL event reconciliation (60 minutes)

Read [docs/assignments/CAP-45.md](docs/assignments/CAP-45.md). The instructor
DDL and fixed fixture run through the same embedded PostgreSQL 14.22 and Java
Maven harness as CAP-42. The fixture contains exactly five events and three
delivery attempts. The SQL learner TODO is
`src/test/resources/assignments/cap45/learner-query.sql`; tests isolate each
mutation and check complete rows, attempt counts, no-attempt retention,
owner isolation, late attempts, row count, and the total amount.

Check the harness independently with
`./mvnw -Dtest=Cap45PostgresInfrastructureTest test`. Run CAP-45 acceptance
tests with
`./mvnw -Plearner-tests -Dtest=Cap45SqlTest test`; these are expected to fail
until the learner query is implemented. The combined learner suite is
`./mvnw -Plearner-tests test`.

## Study plan

The learner time budget is at most 150 minutes on each of the seven days.
Certification preparation runs alongside every stage and is integrated with
the related practical concepts inside that same daily budget. No exam purchases
are included. Project setup and instructor-provided fixtures, boilerplate, and
test harnesses are outside the learner budget. Repository implementation
belongs to the learner; the Scrum owner manages the roadmap only.
