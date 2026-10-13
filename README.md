# Capstone starter

This clean starter begins with CAP-43, a small Java event-policy exercise. It
uses Java 21 and Maven. The assignment code and its fixed tests are supplied;
the classification and acceptance behavior remain learner work.

## Run

With Java 21 and either Maven 3.9+ installed or an existing Maven Wrapper
distribution cached for the current user:

```sh
./mvnw test
```

The repository-local `./mvnw` is a small portable helper, not the official Maven
Wrapper. It uses `mvn` from `PATH` when available, otherwise it searches the
current user's Maven Wrapper cache. It does not download Maven or install
anything. The default run compiles the project and runs infrastructure smoke
checks. To run the visible learner acceptance tests (which are expected to
fail until the TODOs are complete), use:

```sh
./mvnw -Plearner-tests test
```

## CAP-43: event policy (90 minutes)

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

The starter also includes JUnit smoke checks that run before the learner logic
is implemented. Those smoke checks confirm the project and assignment types
compile; they do not claim the assignment is complete.

## Deferred work

CAP-42 will cover parameterized PostgreSQL queries over a half-open UTC range,
accepted event counts, and signed decimal totals. Its fixtures will use a single
declared currency and unique event IDs, with a window from
`2026-10-13T00:00:00Z` to `2026-10-14T00:00:00Z` (upper bound exclusive).
That assignment is deferred until a real PostgreSQL acceptance harness is
available; this starter does not use H2 or claim PostgreSQL verification.

The remaining 30-minute slice is a Java review and explain-back of safe request
validation. HTTP/Spring wiring is deferred until its infrastructure is ready.

## Study plan

The learner time budget is 150 minutes per day. Project setup and instructor-
provided fixtures, boilerplate, and test harnesses are outside that budget.
Repository changes belong to the learner; the Scrum owner manages the roadmap
only.
