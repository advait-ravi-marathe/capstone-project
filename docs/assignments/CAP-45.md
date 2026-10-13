# CAP-45 — Data Engineering: SQL joins for event reconciliation

**Date:** 14 Oct
**Timebox:** 60 minutes
**Runnable status:** real PostgreSQL fixture and adapter smoke-tested; learner
query remains unimplemented.

## Goal

Reconcile accepted events with delivery attempts without losing undelivered
events, mixing owners, or multiplying event amounts.

## Learner task

Implement the parameterized PostgreSQL query in
`src/test/resources/assignments/cap45/learner-query.sql`. JDBC parameters are
`owner_id`, `start_utc`, and `end_utc`, in that order. Return columns
`owner_id TEXT`, `event_id TEXT`, `amount NUMERIC`, `attempt_count BIGINT`,
`last_attempt_at TIMESTAMPTZ` (nullable), and `has_attempt BOOLEAN`, with exactly
one row per qualifying event and rows ordered by `event_id` ascending.

The tables are `events(owner_id,event_id,status,amount,occurred_at)` with
primary key `(owner_id,event_id)` and
`delivery_attempts(owner_id,event_id,attempt_id,attempted_at)` with primary key
`(owner_id,event_id,attempt_id)`. Select accepted events for the requested
owner in the half-open event interval `[start_utc,end_utc)`. Match attempts on
both `owner_id` and `event_id`. Keep events with no attempts; they have count
zero, null last-attempt time, and `has_attempt=false`. Count every matching
attempt regardless of attempt timestamp. Amount contributes once per event.

## Fixed fixture and expected results

The fixture has **five events plus three attempts, eight rows total**. The event
window is `2026-10-13T00:00:00Z` inclusive through `2026-10-14T00:00:00Z`
exclusive.

| owner | event | status | amount | occurred at |
| --- | --- | --- | ---: | --- |
| A | e1 | accepted | 100 | 13 Oct 00:00Z |
| A | e2 | accepted | -20 | 13 Oct 12:00Z |
| A | e3 | rejected | 999 | 13 Oct 13:00Z |
| A | e4 | accepted | 50 | 14 Oct 00:00Z |
| B | e1 | accepted | 500 | 13 Oct 10:00Z |

Initial attempts: A/e1/t1 at 10:00Z, A/e1/t2 at 11:00Z, and B/e1/t3 at
12:00Z on 13 Oct. For owner A, return exactly e1 (amount 100, count 2, last
attempt 11:00Z, true) and e2 (amount -20, count 0, null, false). Sum of
returned amounts is 80; row count is 2.

Each test resets the fixture before its isolated mutation:

- Add B/e2/t9: A/e2 stays unmatched; the shared event ID cannot cross owners.
- Add A/e1/t4 at 12:30Z: e1 count becomes 3, its last time is 12:30Z, while
  output stays two rows and sum stays 80.
- Query owner C: return no rows.
- Add A/e2/t5 at 15 Oct 09:00Z: A/e2 remains in the event window and reports
  count 1, last attempt 15 Oct 09:00Z, and true.

## Run and verify

The test-scoped embedded PostgreSQL 14.22 harness is used; there is no SQLite
substitute. The independent fixture/parameter-binding smoke command is:

```sh
./mvnw -Dtest=Cap45PostgresInfrastructureTest test
```

Run CAP-45 learner assertions with:

```sh
./mvnw -Plearner-tests -Dtest=Cap45SqlTest test
```

These acceptance tests currently fail against the deliberate placeholder.
They compare complete expected rows and explicitly check amount totals and
attempt-count fanout.

## Explain back

Show why A/e2 survives a `LEFT JOIN`, why both owner and event IDs belong in
the join condition, and how your one-row-per-event output keeps an extra attempt
from multiplying the event amount.

## References

- [PostgreSQL table expressions — joined tables and `LEFT JOIN`](https://www.postgresql.org/docs/current/queries-table-expressions.html)
- [PostgreSQL aggregate functions — `count`, `sum`, and empty-input behavior](https://www.postgresql.org/docs/current/functions-aggregate.html)
