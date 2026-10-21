# Day 25 — Recover a losing transaction

No exact course lesson is assigned. The existing 25-minute study block is 5:00 focused PostgreSQL reading, 13:00 worked trace, 4:00 prediction, and 3:00 pause buffer.

Read only the relevant primary-source sections: [PostgreSQL transactions](https://www.postgresql.org/docs/18/tutorial-transactions.html) (BEGIN/COMMIT/ROLLBACK and failed-transaction recovery), [Read Committed](https://www.postgresql.org/docs/18/transaction-iso.html) (each new statement obtains a fresh committed snapshot), and [INSERT](https://www.postgresql.org/docs/18/sql-insert.html) (`ON CONFLICT` as an alternative). These readings support the raw-JDBC transaction trace below; they do not require switching to Spring-managed transactions or copying the alternative into the learner method.

## Two-transaction trace

The independent real-PostgreSQL teaching test is:

```sh
./mvnw -Dtest=Day25TwoTransactionReferenceTest test
```

Connection A inserts `(R2,S9)` without committing. Connection B attempts the same composite key. After A commits, B receives SQLSTATE `23505`; that transaction is aborted, so it rolls back and reads the committed winner using a fresh connection. The observed final row count is one. The synchronized executor and bounded wait avoid relying on sleeps. If A rolled back, B could insert. Another slot is an independent key. Only the specific uniqueness conflict is eligible for recovery; connection failures do not show that another row exists.

Transfer the transaction timeline to the learner selector without copying the example implementation. The learner method has no owner argument and assumes authorization occurred earlier; a process-local lock cannot protect multiple application instances. Existing selector proves a service/database seam, not HTTP response mapping or production authorization.

Prediction variations: if A rolls back instead of committing, B's waiting insert can succeed and the final row count stays one. If B requests a different slot key, both inserts can commit and the final count is two. These are transaction predictions, not additional claims from the single runnable reference test.

**Readiness prompt:** Draw both transaction lanes and explain why the losing transaction must recover before reading. Engineering remains 25/85/10.
