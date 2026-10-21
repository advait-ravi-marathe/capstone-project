# Day 47 — Copy current facts without making a new offer

Independent library example: stored membership m7 is version4, plan `library-plan-v1`, current fee80.00EUR, next anniversary2024-07-01. A later catalog revision is95.00EUR. The original informational notice remains version4 and80.00; a distinct later source value produces version5 and95.00. It never prices or commits a renewal.

```java
record MembershipFacts(String id, long version, String plan, java.math.BigDecimal fee,
                       String currency, java.time.LocalDate anniversary) {}
record MembershipNotice(String id, long sourceVersion, String plan, java.math.BigDecimal referenceFee,
                        String currency, java.time.LocalDate anniversary, String notice) {}
static MembershipNotice notice(MembershipFacts f) {
  return new MembershipNotice(f.id(), f.version(), f.plan(), f.fee(), f.currency(), f.anniversary(),
      "Reference information only; not a renewal offer.");
}
```

The trace is executable in `RenewalTeachingReferenceTest#membershipNoticeCopiesSourceWithoutPricing`. The decision note compares copying stored facts with consulting today's catalog and implying a new price. The cost of copying is that this screen cannot answer what a future renewal costs; that is a separate workflow.

Transfer: `buildPreview` copies policyId, source version, product/rate versions, current annual premium, currency and the approved notice. Only the next-term date is derived. Use the second distinct fixture in the existing acceptance test, and preserve input facts. The premium125.00 is not the separate training rebuild amount; do not invent a new quote.

Study25: 5:00 focused Record/ADR sections,16:00 worked projection and decision,4:00 prediction. Practice85: 10 decision variation,55 `buildPreview`,20 tests. Evidence10. Official: [Java Record](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Record.html), [ADR structure](https://docs.aws.amazon.com/prescriptive-guidance/latest/architectural-decision-records/appendix.html). The two Records videos are optional prerequisite review only.

Commands: `./mvnw -Plearner-tests -Dtest=PolicyRenewalAcceptanceTest#day47PreviewCopiesCurrentSnapshotAndMakesNoRenewalPriceOrCommitment test`; `./mvnw -Dtest=RenewalTeachingReferenceTest#membershipNoticeCopiesSourceWithoutPricing test`.
