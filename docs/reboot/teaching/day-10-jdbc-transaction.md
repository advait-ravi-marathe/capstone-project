# Day 10 — One JDBC connection owns one batch transaction

**Conditional course reference:** in28Minutes Official, *Master Hibernate and JPA with Spring Boot in 100 Steps*, section “Transaction Management”: “Step 58 - Introduction to Transaction Management” (4:43; item 7907610) and “Step 59 - Transaction Management - ACID Properties” (5:49; item 7907612), 10:32 total. Course access is unconfirmed; no purchase or enrollment. “Step 63 - Implementing Transaction Management - 3 Things to Decide” (4:38; item 7907620) is optional later, not stacked today. If the course is unavailable, use the named Java 21 `Connection` methods for the same 10:32 budget.

Study budget: 10:32 foundation + 10:28 worked trace + 4:00 readiness = 25 minutes. Spring-managed transactions and this example's manually owned JDBC transaction are different contexts; the learner seam deliberately owns one connection. Do not transfer this pattern to a connection already managed elsewhere.

## Independent teaching table and code

```sql
CREATE TABLE teaching_names (name TEXT PRIMARY KEY);
INSERT INTO teaching_names VALUES ('existing');
```

The reference opens its own connection from a fresh disposable PostgreSQL `DataSource` for every call. Both row inserts use the same transaction-owning connection:

```java
static void storeNames(DataSource ds, List<String> names) throws SQLException {
    try (Connection c = ds.getConnection()) {
        c.setAutoCommit(false);
        try {
            try (PreparedStatement p = c.prepareStatement(
                    "INSERT INTO teaching_names(name) VALUES (?)")) {
                for (String name : names) {
                    p.setString(1, name);
                    p.executeUpdate();
                }
            }
            c.commit();
        } catch (SQLException | RuntimeException original) {
            try { c.rollback(); }
            catch (SQLException rollbackFailure) { original.addSuppressed(rollbackFailure); }
            throw original;
        }
    }
}
```

An empty input commits no changes. For `storeNames([first, existing])`, `first` is inserted but not yet committed; `existing` then violates the primary key with SQLSTATE `23505`. Rollback removes `first`; a fresh connection reads only `[existing]`. For `[first, second]`, one commit makes both visible to a fresh read. An earlier committed row remains because rollback applies only to the current uncommitted transaction.

The handled failure is a known statement error. If rollback itself fails, the code attaches that failure as suppressed and rethrows the original. This beginner example makes no promise about a network interruption near commit, where the final outcome can be uncertain. Closing a connection alone is not the explicit commit/rollback decision taught here.

```sh
./mvnw -Dtest=Day10AtomicityReferenceTest test
```

## Practice and transfer

Keep the 85-minute practice allocation: 15 minutes run/inspect the failure and success traces; 10 minutes predict a third duplicate; 45 minutes implement the existing pre-normalized selection-batch seam; 15 minutes run its original selector and diagnose. Evidence remains 10 minutes. If prerequisite work is incomplete, carry it forward rather than inventing a normalized-input solution.

**Readiness question:** Draw the one connection spanning both inserts and the commit/rollback branch. Why does rollback leave a row committed before this method? Distinguish atomicity in this example from concurrent isolation. No audit insert, `TransactionTemplate`, proxy, self-invocation, or selection SQL is assigned by this teaching example.

Existing learner selector: `./mvnw -Plearner-tests -Dtest=Day10ContractTest test`. A green teaching reference is not a learner batch completion.

**Primary reference:** Java 21 [`Connection`](https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/java/sql/Connection.html): `setAutoCommit`, `commit`, `rollback`, and `close`.
