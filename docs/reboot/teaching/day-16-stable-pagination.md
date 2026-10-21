# Day 16 — Stable pages over tied timestamps

## Study (25 minutes)

Required reference in owned course 4993276: “Step 33 - Exploring JPA and Hibernate Queries for REST API” (6:36). It gives query context; this project still uses JDBC. Do not stack the Spring Data JPA lecture. Read PostgreSQL’s [LIMIT and OFFSET](https://www.postgresql.org/docs/18/queries-limit.html) uniqueness warning and large-offset note (3 minutes); work the five-row trace (8 minutes); predict the changed-data case (4 minutes); use 3:24 as pause buffer. No JPA conversion is assigned.

## Worked trace

`Day16PaginationReferenceTest` seeds its own disposable PostgreSQL `teaching_notice` table. Sort rows by `created_at DESC, id ASC`. Equal-time rows therefore sort N1 before N2 and N3 before N4. With page size 2, offsets 0, 2, 4, and 6 return `[N1,N2]`, `[N3,N4]`, `[N5]`, and `[]`; total remains 5. Page below zero, size zero, and size above 20 are rejected. The ID tie-break direction matters: descending IDs would be deterministic but wrong for this order.

Run:

```sh
./mvnw -Plearner-tests -Dtest=Day16PaginationReferenceTest test
```

Observed instructor reference: **3 tests pass** using a disposable PostgreSQL 14.22 instance, closed after the class. A separate test inserts N0 between page requests and shows OFFSET can repeat N2 across changing snapshots. Deterministic ordering for one unchanged result set does not promise stable paging while rows are added.

## Practice and transfer

Spend 15 minutes inspecting the reference and 10 minutes on the changed-data prediction; then work the existing JDBC seam using the remaining implementation time. Learner command: `./mvnw -Plearner-tests -Dtest=Day16ContractTest test`.

Transfer the fixed capstone order `created_at DESC, quote_version_id ASC`, request defaults, maximum size, and count/page contract from its test. The toy has no owner predicate because the existing `QuoteHistoryQuery.page` has no owner parameter. Do not add cursor pagination or a new API. Explain why the tie-breaker is needed, how offset is calculated, one deep-offset cost, and the difference between a fixed dataset and changes between requests.
