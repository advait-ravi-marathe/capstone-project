# Day 24 — Compare the plan and the rows

Required conditional course references: “Measure before Tuning” (1:56) and “Indexes” (1:44), access unconfirmed. They are context only. The 25-minute allocation is 3:40 viewing (or replacement), 5:00 official reading, 10:00 worked run, 3:00 prediction, and 3:20 pause buffer; do not purchase access or claim a timing improvement.

## Observed PostgreSQL run

The runnable reference uses its own temporary PostgreSQL database and `teaching_readings` table:

```sh
./mvnw -Dtest=Day24ExplainReferenceTest test
```

It filters 10,000 generated readings to station 7, orders by `measured_at DESC, reading_id ASC`, and returns the same rows before and after the candidate index: `9007@2026-01-01T00:09:00Z`, `9107@2026-01-01T00:09:00Z`. The observed PostgreSQL 14.22 plan before the index was `Limit → Sort → Seq Scan` with filter `station_id = 7`; after creating `(station_id, measured_at DESC, reading_id ASC)` it was `Limit → Index Only Scan` with that index condition. These are observed plans from this deterministic teaching fixture, not a promise that another database or dataset chooses the same plan, nor a wall-clock benchmark.

Then inspect the existing contract's representative query and derive the candidate index yourself. Keep the exact ordered output. An index also costs storage and writes. `EXPLAIN ANALYZE` executes the query; it is not part of this read-only plan exercise.

**Readiness prompt:** Identify filter, sort, and limit in the baseline plan, then state the observed change and one write/storage cost. Engineering remains 25/85/10.
