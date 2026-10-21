# Day 50 — Map known unavailable outcomes safely

In the membership example, no principal is401 and performs no lookup; missing/foreign membership has the same404; found EARLY, ENDED or STOPPED maps to the same409 with a fixed message; only an available reminder proceeds to a projection. `ResponseEntity` carries status and body but does not sanitize arbitrary data.

```java
ResponseEntity<?> unavailable() {
  return ResponseEntity.status(409).body(Map.of(
    "code", "REMINDER_UNAVAILABLE",
    "message", "Membership reminder is unavailable for this date or state."));
}
```

The complete route and negative 200 mutant check live in `RenewalTeachingReferenceTest#unavailableOutcomeIsNotSuccessfulContent`. The project route uses its own `RENEWAL_PREVIEW_UNAVAILABLE` code and fixed message. Check all three existing non-ready eligibility enums, then make real missing and foreign GET requests indistinguishable. No503/500/timeout design is assigned here.

Study25: 7:49 focused404 lesson,3:00 ResponseEntity,10:11 worked response table,4:00 prediction. Practice85: 10 table,50 `unavailable`,25 route/negative cases. Evidence10. Optional generic exception lesson is deferred reference only. Official: [Spring MVC return values](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/return-types.html).

Commands: `./mvnw -Plearner-tests -Dtest=PolicyRenewalControllerAcceptanceTest#day50EndpointConcealsMissingAndForeignRowsAndUsesStableConflictCodes test`; `./mvnw -Dtest=RenewalTeachingReferenceTest#unavailableOutcomeIsNotSuccessfulContent test`.
