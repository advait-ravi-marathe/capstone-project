# Day 15 — Join, ownership, and projection

**Purpose:** learn parent/child reads, optional values, empty lists, and response allowlists before implementing `JourneySummaryQuery`. This guide and its instructor test use a separate toy library schema; they do not supply the capstone query solution.

## Study (25 minutes)

Required owned-course references: in28Minutes, course 4993276, “Step 07 - Querying Data using Spring JDBC” (5:36) and “Step 07 - Understanding Optional class in Java” (7:14). These cover JDBC/Optional background, not the full join. Read PostgreSQL’s [joined tables](https://www.postgresql.org/docs/18/queries-table-expressions.html#QUERIES-JOIN) and compare `LEFT JOIN` with `INNER JOIN` (2 minutes). Spend 6 minutes walking the worked trace below, 2 minutes predicting the variation, and keep 2:08 for pauses. No new lecture is stacked.

## Worked trace

`Day15JoinProjectionReferenceTest` creates its own disposable PostgreSQL tables `teaching_shelf`, `teaching_spotlight`, and `teaching_hold`. The fixed seed has S1 owned by M1 with no optional rows, S2 owned by M1 with a spotlight and two holds, and S3 owned by M2. It never reads or changes the capstone schema.

The reference first selects only `s.id`, `p.isbn`, and `p.title` from the owner-scoped parent with a `LEFT JOIN` to its optional spotlight. If no parent row exists, the result is absent. If S1 exists, SQL returns its parent and NULL child columns; the projection is a present summary with `spotlight=null` and `holds=[]`. A second scoped child query orders holds by position. S2 yields B3 then B8; private note and owner columns never enter the response record. S9 and foreign-owned S3 both produce absence.

The code and executable assertions are in `src/test/java/assignments/learning/Day15JoinProjectionReferenceTest.java`:

```sh
./mvnw -Plearner-tests -Dtest=Day15JoinProjectionReferenceTest test
```

Observed instructor reference: **3 tests pass** on a disposable PostgreSQL 14.22 instance, which is closed after the class. This is a worked reference result, not learner completion.

## Practice and transfer

Use 15 minutes to run/read the reference; 10 minutes to predict and verify the S4 variation (owned parent, one hold, no spotlight); then use the remaining existing implementation time on the actual seam. Existing learner command remains `./mvnw -Plearner-tests -Dtest=Day15ContractTest test`.

The capstone uses `requestedJourneyId` and `quoteJourneyId`; the toy uses `shelfId` and `ownerId`. Transfer the ideas of owner-scoped parent selection, an optional child, and an empty collection. Do not copy toy table names, fields, SQL, or record into the capstone. Keep its exact `{journeyId, quote, selections}` allowlist and ordered selection contract.

Explain from the trace: why an inner join loses S1; why missing and foreign-owned parents both look absent; why SQL NULL becomes a null child while zero child rows become `[]`; and why selecting only response fields limits leakage. No extra architecture worksheet is assigned. The existing system-design prompt should be answered using the supplied three-box trace, inside the current study/evidence allowance.
