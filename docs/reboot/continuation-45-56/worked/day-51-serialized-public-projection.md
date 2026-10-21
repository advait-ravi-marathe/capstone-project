# Day 51 — Inspect the actual JSON projection

The parcel reference starts from a private stored row, explicitly constructs a five-field public `ParcelView`, then performs a real standalone MockMvc GET and examines serialized JSON. It asserts exact keys/values and that weight is a JSON number; the private email never appears. Runtime inspection shows the configured serializer writes BigDecimal `2.50` as numeric JSON `2.5`. JSON carries the value/type, not the display scale; the UI adapter adds two decimal places for presentation. The executable selector is `ParcelProjectionReferenceTest#serializedViewHasOnlyTheFivePublicFieldsAndANumericWeight`.

```java
record ParcelView(String ref, int revision, BigDecimal weightKg, String measuredOn, String notice) {}
return ResponseEntity.ok(new ParcelView(stored.ref(), stored.revision(), stored.weightKg(),
    stored.measuredOn(), "Recorded measurement only; not a delivery promise."));
```

`ResponseEntity` does not choose or filter fields; the projection record does. Returning the private record, adding ownerEmail, or changing numeric weight to a string must break the assertions.

Transfer: keep the existing `PolicyRenewalPreview` including current annual premium. Add a small MVC test adapter that invokes the real learner `available` seam, and assert exactly the eight approved keys, values and types using a second fixture. Do not serialize private `PolicyRenewalFacts` or add mutation/offer fields.

Study25: 4:30 Bean response foundation,16:30 exact JSON trace,4:00 prediction. Practice85: 10 public-field reasoning,50 `available`,25 serialized tests. Evidence10. Official: [Spring REST resource representation](https://spring.io/guides/gs/rest-service/) and [ResponseEntity](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/return-types.html). Exact reference command: `./mvnw -Dtest=ParcelProjectionReferenceTest test`.
