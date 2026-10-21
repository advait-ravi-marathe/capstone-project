# Day 26 — Audit only a committed first change

No exact audit lecture is assigned. The 25-minute block is 4:00 focused PostgreSQL reading, 13:00 worked toy, 4:00 prediction, and 4:00 pause buffer.

Use the focused [PostgreSQL transaction tutorial](https://www.postgresql.org/docs/18/tutorial-transactions.html) for atomic commit/rollback and [INSERT](https://www.postgresql.org/docs/18/sql-insert.html) for `RETURNING`, which reports rows actually inserted or updated. These are the relevant JDBC/database semantics; a general Spring transaction lecture does not teach this raw-connection seam.

## First change, repeat, and failure

```sh
./mvnw -Dtest=Day26AuditReferenceTest test
```

The disposable PostgreSQL reference checks three outcomes: first activation commits one card and one minimal event and returns true; repeat creates no second event and returns false; an injected audit-trigger failure rolls back the new card and audit together. A fresh read sees only the original committed C7 after the C8 failure. Event fields are action, resource ID, actor ID, and supplied time; there is no payload column. The boolean alone is insufficient evidence, so the test checks exact fields and both row counts.

Transfer only the transaction reasoning to the existing `SELECTION_CREATED` contract. Keep the same connection and commit boundary. Do not copy toy table names or build a second audit system. This is an instructor reference, not learner completion.

Prediction variation: a genuinely new C8 after C7 should produce two card rows and two minimal events. If C8 is inserted but its audit step fails, the earlier committed C7 remains while C8 contributes neither row. Explain this with transaction boundaries; do not claim the reference test generated a C8 receipt.

**Readiness prompt:** Contrast first, repeat, and failed-audit paths; explain why request logs and business creation events are different. Engineering remains 25/85/10.
