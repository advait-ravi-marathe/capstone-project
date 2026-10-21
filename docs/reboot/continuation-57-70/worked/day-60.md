# Day 60 — Map an owner-scoped row

**Study: 25 minutes.** Retrieve the existing raw-JDBC binding/resource guide. Review Java `ResultSet.getDate(String)` and `getBigDecimal(String)`, then trace the complete nullable loan example before changing the native seam.

## Worked example

The synthetic `toy_loan` example in `RenewalJdbcTeachingReferenceTest` maps `loan-a/reader-a` to revision 1, deposit 12.50, due date 2026-12-20 and null returned date; `loan-b/reader-b` returns its different complete values and non-null return date. Foreign, absent and SQL-looking ID values return empty. Both ID and owner are bound parameters. Each read compares the complete table rows before and after, not just a count.

The expected mapping is an immutable `LoanView`; SQL exceptions remain failures rather than being converted to absence. A same-count UPDATE changes the fee and fails the full snapshot assertion.

## Transfer

Implement the actual `JdbcPolicyReadStore.findByPolicyIdAndOwnerId` seam. Keep the existing renewal-read assertion as a prerequisite, but call the policy read store directly for the issued seeded row and a distinct fixture. Verify every current-row field and nullable cancellation date; these issued rows have no endorsement history, so do not claim aggregation. Compare all three lifecycle tables after each read.

**Practice 85:** 10 map prediction, 50 adapter, 25 tests. **Selector:** `./mvnw -Plocal-app,learner-tests -Dtest=LocalApplicationAcceptanceTest#day60OwnerScopedPreviewReadDoesNotWrite test`.
