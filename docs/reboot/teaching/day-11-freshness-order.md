# Day 11 — Apply freshness checks in the declared order

No exact verified preferred-course lecture teaches this pure metadata guard. Required supplement: Java 21 [`Instant.isBefore`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Instant.html) review (5 minutes), then this worked trace (16 minutes) and readiness check (4 minutes): 25 minutes total. The 19-minute isolation-level video bundle is not required today; it does not teach this predicate.

The library reservation is a toy analogy, not a new capstone domain object. Its `requestedMember`/`recordMember` comparison teaches the ownership/scope check. In the actual capstone, the corresponding fields are `requestedJourneyId` and `quoteJourneyId`; do not add member or edition fields to `QuoteFreshnessState`. Its `requestedEdition`/`currentEdition` comparison illustrates the same current-version check using the capstone's requested/current quote-version fields. Both use the same ordering: conceal wrong owner/journey; reject expiry at equality; reject a superseded version; otherwise allow.

```java
record ReservationState(String requestedMember, String recordMember,
        String requestedEdition, String currentEdition, Instant expiresAt) { }

static String reservationDecision(ReservationState state, Instant now) {
    if (!state.requestedMember().equals(state.recordMember())) return "HIDDEN";
    if (!now.isBefore(state.expiresAt())) return "EXPIRED";
    if (!state.requestedEdition().equals(state.currentEdition())) return "SUPERSEDED";
    return "ALLOW";
}
```

With `now = 10:00:00Z`, the fixed cases are:

| Requested/stored member | Requested/current edition | Expiry | Outcome | Write callback |
|---|---|---|---|---:|
| a/a | e2/e2 | 10:00:30Z | ALLOW | 1 |
| a/a | e2/e2 | 10:00:00Z | EXPIRED | 0 |
| a/a | e1/e2 | 10:00:30Z | SUPERSEDED | 0 |
| b/a | e1/e2 | 10:00:00Z | HIDDEN | 0 |
| a/a | e1/e2 | 10:00:00Z | EXPIRED | 0 |

`!now.isAfter(expiresAt)` would incorrectly accept equality; `now.isBefore(expiresAt)` is false exactly at expiry. The caller's write callback is invoked only for `ALLOW`. A zero callback count proves this guard did not call that callback in this test; it does not prove an atomic database check/write. If stored current version changes from e2 to e3 after a caller received e2, this pure check cannot prevent the race.

```sh
./mvnw -Dtest=Day11FreshnessReferenceTest test
```

Keep the existing 85-minute practice block: 15 minutes run/inspect the reference, 10 minutes predict before/equal/after expiry, 45 minutes on the existing `QuoteFreshnessPolicy` seam, and 15 minutes for its acceptance selector and diagnosis. Evidence remains 10 minutes.

**Readiness question:** What is checked first, and why? What does a zero callback prove—and not prove? Then use the existing capstone exceptions and precedence; do not rewrite immutable metadata or design SQL CAS/locks ahead of its scheduled instruction.

Existing learner selector: `./mvnw -Plearner-tests -Dtest=Day11ContractTest test`. This small reference returns toy strings and is not the capstone exception implementation.
