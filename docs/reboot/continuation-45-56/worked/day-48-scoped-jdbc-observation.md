# Day 48 — Bind both keys and compare complete rows

An independent membership example queries `WHERE id = ? AND owner_id = ?`, binding the resource id and trusted owner separately. A found row returns its actual id/version/fee/currency; foreign, absent, and SQL-looking input return empty. Connection, statement, and result set close with try-with-resources. The executable implementation and disposable PostgreSQL fixtures are in `RenewalTeachingReferenceTest#scopedReadBindsValuesAndPreservesCompleteRows`.

```sql
SELECT id, version, fee, currency
FROM toy_membership
WHERE id = ? AND owner_id = ?;
```

The reference records full ordered member/event/receipt rows before and after its reads. It then demonstrates why a count is insufficient: `UPDATE toy_membership SET fee=81.00 WHERE id='m7'` leaves the number of rows at2 but changes the complete snapshot. The reference detects this deliberately bad mutation only in its disposable toy database.

Transfer: query the existing lifecycle table with both policy and owner keys. Preserve actual fixtures, add a changed second owner/value and a malicious-looking literal id, and compare every ordered field of all three existing tables after each read. The instructor helper supplies row snapshots; no schema/framework change is part of this day. A scoped query is not a claim that production authentication is enabled.

Study25: 5:36 verified JDBC foundation,14:24 worked raw-JDBC/resource/snapshot trace,5:00 prediction. Practice85: 10 bound-parameter trace,50 existing read seam,25 tests. Evidence10. Optional Spring JDBC introduction is recovery-only. Official: [PreparedStatement](https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/java/sql/PreparedStatement.html).

Commands: `./mvnw -Plearner-tests -Dtest=PolicyRenewalRepositoryAcceptanceTest#day48ReadIsOwnerScopedAndReturnsOnlyRenewalInputsWithoutWrites test`; `./mvnw -Dtest=RenewalTeachingReferenceTest#scopedReadBindsValuesAndPreservesCompleteRows test`.
