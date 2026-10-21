# Day 7 — Use fixed business time to test expiry

No exact verified course lecture teaches `Clock` injection or deterministic business time. The required focused supplement is the official Java 21 [`Clock`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Clock.html) section `fixed(Instant, ZoneId)` and [`Instant`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Instant.html) sections `plusSeconds` and `isBefore`. Use six minutes for those sections, 15 minutes for the worked example below, and four minutes for readiness: total 25 minutes. This replaces vague “time concept” reading with a concrete trace.

## Worked example

```java
var created = Instant.parse("2026-01-01T10:00:00Z");
var clock = Clock.fixed(created, ZoneOffset.UTC);
var observed = clock.instant();
var expires = observed.plusSeconds(30);
assertEquals(Instant.parse("2026-01-01T10:00:30Z"), expires);
assertTrue(expires.minusNanos(1).isBefore(expires));
assertFalse(expires.isBefore(expires));
assertFalse(expires.plusNanos(1).isBefore(expires));
boolean validAt(Instant now, Instant expiresAt) {
    return now.isBefore(expiresAt);
}
assertTrue(validAt(expires.minusNanos(1), expires));
assertFalse(validAt(expires, expires));
assertFalse(validAt(expires.plusNanos(1), expires));
record Reservation(String rateVersion, String capturedPrice, Instant expiresAt) { }
var saved = new Reservation("rate-A", "10.00", expires);
String currentCatalogPrice = "11.00";
assertEquals("10.00", saved.capturedPrice());
assertEquals("11.00", currentCatalogPrice);
```

The fixed clock always returns the creation instant. Under the strictly-before rule, one nanosecond before expiry is still valid, while equality and later are expired. The saved reservation retains its captured price even after a separate current catalog value changes. The string price is only a retention illustration; the application uses `BigDecimal` for premium values.

The reference test runs without sleeping:

```sh
./mvnw -Dtest=Day07TimeReferenceTest test
```

## Practice and transfer

Keep the existing 85-minute practice block: up to 15 minutes reproduce/observe the fixed-clock reference, 10 minutes predict and run the date-boundary variation, 45 minutes on the original quote-version seam, and 15 minutes for its selector and diagnosis. The existing 10-minute evidence block records the observed boundary, explanation, and assistance. If the bridge takes longer, reduce implementation scope and carry it forward.

Predict the variation: `2026-01-01T23:59:50Z` plus 30 seconds is `2026-01-02T00:00:20Z`; one tick before is valid, equality is expired. Then work the existing 15-minute quote snapshot using its fixed identity/rate data and injected collaborators. Do not create a mutable catalog or depend on wall-clock time.

**Readiness question:** Why inject time rather than wait? Which captured value stays fixed when the catalog changes? `Clock` represents business wall time; monotonic elapsed-time benchmarking is outside this task.

Existing learner check: `./mvnw -Plearner-tests -Dtest=Day07ContractTest test`. The reference shows the declared strict comparison; it does not satisfy the quote-version contract.
