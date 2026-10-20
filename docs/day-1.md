# Day 1 — Synthetic quote selection invariant

## Work budget

Total scheduled learning is 180 minutes: 60 minutes of Java DSA separately,
plus 120 engineering minutes. Engineering is 85 minutes implementation and
testing, 25 minutes concept/course, and 10 minutes evidence. Instructor-owned
scaffolding and setup are outside this budget. There is no extra mandatory
setup work. Record actual time; do not claim this allocation is validated until
a learner attempt has been measured.

## Separate Java DSA block — LeetCode 121 (60 minutes)

This is Advait's first independent attempt. Prior completion is zero; earlier
assisted practice records remain historical and do not count as solved. The
learner method is `src/main/java/assignments/dsa/BestTimeToBuySellStock.java`
and its fixed acceptance tests are
`src/test/java/assignments/dsa/BestTimeToBuySellStockAcceptanceTest.java`.
Implement the maximum profit from one buy followed by one later sell. Return
zero for empty, one-day, or non-profitable input; reject `null`; do not mutate
the array. Expected target: O(n) time and O(1) auxiliary space. Explain the
descending-price, repeated-price, and minimum/maximum boundary cases. The
solution is intentionally left as a TODO.

Run the acceptance check separately; it is expected to fail at the explicit
TODO until implemented:

```sh
./mvnw -Plearner-tests -Dtest=BestTimeToBuySellStockAcceptanceTest test
```

## Feature and learning goal

CoverStory starts as an enterprise insurance lifecycle simulation. Day 1 uses
the single synthetic product fixture at `fixtures/day-1/home-secure.json`.
The small slice establishes that a quote selection is identified
by both journey and quote version, duplicates are removed in first-seen order,
and invalid input is rejected before a result is returned. No catalog database,
authentication, payment, policy activation, Kafka, or frontend is needed for
this exercise.

The learner implementation belongs only in
`src/main/java/assignments/insurance/QuoteSelectionPolicy.java`.

## Behavioral cases

The fixed acceptance suite is
`src/test/java/assignments/insurance/QuoteSelectionAcceptanceTest.java`.

| Input | Expected result |
| --- | --- |
| `[(journey-B, home-secure-v1), (journey-A, home-secure-v1), (journey-B, home-secure-v1)]` | First two exact pairs, same order; input unchanged |
| `[]` | `[]` |
| Same quote version under two journey IDs | Two distinct keys |
| Null list, null entry, or blank key part | `IllegalArgumentException`; no partial result |
| `home-secure-v1` and ` home-secure-v1` | Distinct exact values; no trimming |

The runnable Spring Boot app starts with `./mvnw spring-boot:run`. The supplied
API is `POST /api/quote-selections`, with JSON
`{"selections":[{"journeyId":"journey-B","quoteVersionId":"home-secure-v1"}]}`.
After the learner TODO is implemented, valid input returns HTTP 200 and a JSON
array of exact composite keys. Missing request body, malformed JSON, or invalid
selections return HTTP 400. Before implementation, valid selection requests
reach the explicit `UnsupportedOperationException` TODO and return HTTP 500;
that is the expected red learner state. The API adapter is instructor-owned;
the domain invariant is learner-owned.

## Run commands

From the repository root, instructor infrastructure is separate and should
pass before touching the learner TODO:

```sh
./mvnw -Dtest=QuoteSelectionInfrastructureTest test
```

The learner acceptance suite is intentionally excluded by default. Before the
TODO is implemented, this should fail with the explicit
`Day 1 learner TODO: implement uniqueSelections` exception:

```sh
./mvnw -Plearner-tests -Dtest=QuoteSelectionAcceptanceTest,QuoteSelectionApiAcceptanceTest test
```

The regular documented project command remains:

```sh
./mvnw test
```

## Concept prompt — AWS DVA-C02 Domain 1, Task 1, Skill 1.1.6

Read the [official Developer Associate Domain 1 objective](https://docs.aws.amazon.com/aws-certification/latest/developer-associate-02/developer-associate-02-domain1.html),
Task 1, Skill 1.1.6, “Create, extend, and maintain APIs.” The 25 minutes is our
planner allocation, not an AWS-published lesson duration. Use this local Spring
endpoint to define request validation, response shape, and HTTP success/error
codes; no AWS account, infrastructure, or cloud deployment is part of Day 1.
Also explain why a composite key is needed, how encounter-order deduplication
can be implemented in expected O(n) time, and why an in-memory selection is
neither durable state nor authorization.

## Evidence

Use the 10-minute evidence allocation to record exact commands, exit codes,
test counts, and any first TODO stack trace in the learner's own notes. Do not
mark the learner task complete from infrastructure checks alone.
