# Day 50 — Map known unavailable outcomes safely

In the membership example, no principal is401 and performs no lookup; missing/foreign membership has the same404; found EARLY, ENDED or STOPPED maps to the same409 with a fixed message; only an available reminder proceeds to a projection. `ResponseEntity` carries status and body but does not sanitize arbitrary data.

```java
ResponseEntity<?> unavailable() {
  return ResponseEntity.status(409).body(Map.of(
    "code", "REMINDER_UNAVAILABLE",
    "message", "Membership reminder is unavailable for this date or state."));
}
```

`RenewalTeachingReferenceTest#unavailableOutcomeIsNotSuccessfulContent` directly exercises the independent membership mapper and rejects its 200 mutant; it is not a route-level test. The project's actual GET route is separately covered by `PolicyRenewalControllerAcceptanceTest#day50FoundIneligibleGetMaps409WithoutCallingAvailablePreviewBuilder`: a found ineligible fixture must return the exact 409 body, and the preview builder must remain untouched while Day51 is still a TODO. A separate route test compares missing and foreign 404 bodies. The project route uses its own `RENEWAL_PREVIEW_UNAVAILABLE` code and fixed message. No503/500/timeout design is assigned here.

Study25: 7:49 focused404 lesson,3:00 ResponseEntity,10:11 worked response table,4:00 prediction. Practice85: 10 table,50 `unavailable`,25 route/negative cases. Evidence10. Optional generic exception lesson is deferred reference only. Official: [Spring MVC return values](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/return-types.html).

Commands: `./mvnw -Plearner-tests -Dtest=PolicyRenewalControllerAcceptanceTest#day50EndpointConcealsMissingAndForeignRowsAndUsesStableConflictCodes+day50FoundIneligibleGetMaps409WithoutCallingAvailablePreviewBuilder test`; `./mvnw -Dtest=RenewalTeachingReferenceTest#unavailableOutcomeIsNotSuccessfulContent test`.
