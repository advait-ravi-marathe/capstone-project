# System design pattern library

Use this as a retrieval index after recording an initial design. Each pattern
is a question to test against the current requirement, not a default answer.

1. **Requirement slice:** separate user-visible outcome, must-not-happen
   cases, and measurable target.
2. **Invariant-first modeling:** state the condition the domain and database
   must preserve before choosing tables or classes.
3. **Ownership at the boundary:** derive identity from the authenticated
   principal; do not accept client authority as data.
4. **Immutable snapshot:** preserve the exact facts promised at the time a
   quote or issue was created.
5. **Unique constraint as concurrency guard:** encode durable uniqueness in
   PostgreSQL; handle the conflict at the service boundary.
6. **Idempotent intent:** bind a retry key to the request's owner and exact
   intent; replay the existing result or report a conflict.
7. **Atomic aggregate write:** keep related state, event, and receipt writes
   in one transaction when the contract requires all-or-nothing behavior.
8. **Optimistic version check:** reject stale state rather than silently
   overwriting a concurrent update.
9. **Stable pagination:** use a deterministic sort key and cursor that match
   the query's uniqueness and ordering.
10. **Typed UI states:** model loading, ready, expired, conflict, and unavailable
    separately; clear old-owner data before a new request.
11. **Evidence at the boundary:** compare the exact API value, rendered value,
    and persisted state using independent expectations.
12. **Measured complexity:** add caching, async work, a service boundary, or
    AI only after a measured constraint and an explicit failure mode justify
    the added operational cost.

When a pattern suggests a new feature or abstraction, stop and check the daily
contract before implementing it. StreamForge and model-based features remain
gated by the current roadmap.
