# Day 1 — Synthetic quote selection invariant

## Work budget

Total scheduled learning is 180, 195, or 210 minutes according to the DSA tier: 60/75/90 minutes of Java DSA separately,
plus 120 engineering minutes. Engineering is 85 minutes implementation and
testing, 25 minutes concept/course, and 10 minutes evidence. Instructor-owned
scaffolding and setup are outside this budget. There is no extra mandatory
setup work. Record actual time; do not claim this allocation is validated until
a learner attempt has been measured.

## Guided DSA study — LeetCode 121

An earlier version of this guide called LC121 the planned first independent attempt. Preserve that historical description as a plan only: independent-solve credit remains zero, and no confirmed dated actual-attempt event is recorded here. The existing reports of three screenshots and seven assisted practice sessions remain assisted practice, not independent solves. Do not backfill dates or reclassify those sessions.

The corrected initial sequence uses LeetCode Premium's problem statement/editorial as guided learning for LC121 on study Day1. Attempt the problem before opening the editorial when possible; if editorial guidance is used, record the attempt as assisted even if accepted. For this single guided exercise, use the 60-minute tier. Derive D+3, D+7 and D+21 only from an actual first-attempt date. Earlier planned dates (23 Oct, 27 Oct, 10 Nov) are forecasts only and do not establish an attempt or review. See the [shared DSA policy](reboot/dsa/guided-transfer-policy.md) and [error-log template](reboot/dsa-error-log-template.md).


**Preserved learner record:** Advait reported the DSA course/learning portion complete; the independent solve and reviews remain unrecorded. The earlier planned first-attempt date of 20 October 2026 and forecast reviews of 23 October, 27 October, and 10 November remain forecasts only; actual attempt dates determine due work. The historical reports of three screenshots and seven assisted practice sessions remain assisted practice.

The learner method is `src/main/java/assignments/dsa/BestTimeToBuySellStock.java`
and its fixed acceptance tests are
`src/test/java/assignments/dsa/BestTimeToBuySellStockAcceptanceTest.java`.
Implement the maximum profit from one buy followed by one later sell. Return
zero for null, empty, one-day, or non-profitable input; do not mutate
the array. Prices follow LeetCode 121's inclusive range `0..10_000`. Expected
target: O(n) time and O(1) auxiliary space. Explain descending-price, repeated-
price, and minimum/maximum boundary cases. The current main revision contains
a learner-authored implementation. This documentation update did not run the
DSA acceptance selector; independent-solve credit remains unrecorded until the
learner confirms an unaided attempt.

Run the acceptance check separately and record its actual result; it is no
longer an expected TODO failure:

```sh
./mvnw -Plearner-tests -Dtest=BestTimeToBuySellStockAcceptanceTest test
```

## Day 1 engineering task: API bridge first, quote selection separately

The Day 1 learner status remains pending. First study and run the [Day 1 API bridge](reboot/day-01-api-bridge.md), then complete its separate tiny practice endpoint if the prerequisite is understood. The bridge uses an isolated test-runtime fixture; it does not add a production route or alter the quote-selection API.

The 25-minute study block uses the essential course lessons **Step 02 - Creating a Hello World REST API with Spring Boot** (6:21) and **Step 03 - Enhancing the Hello World REST API to return a Bean** (4:30), plus the worked guide and readiness check. Add Step 00's overview only if the request/API terms are new. Pause/replay time counts; carry unfinished viewing forward. The supplied fixture handles project setup, so Step 01 is optional. The course has a Spring Boot 3 title; this repository uses Java 21 / Spring Boot 4.1.1. Do not downgrade or copy old setup instructions.

The guide explains a real local request → Spring route → Java method → Java return value → HTTP response, with verified curl examples for plain text, JSON, 404, and 405. Practice modifies only `src/test/java/assignments/learning/day01/Day01ApiPractice.java`; the exact selector is `./mvnw -Plearner-tests -Dtest=Day01ApiPracticeAcceptanceTest test`. Instructor reference verification is `./mvnw -Dtest=Day01ApiBridgeInfrastructureTest test`. The teaching server is test-runtime-only and loopback-bound.

This API bridge does not implement quote selection. The original learner seam stays `src/main/java/assignments/insurance/QuoteSelectionPolicy.java#uniqueSelections`; its fixed acceptance suite and exact examples remain in [Day 01 contract](reboot/contracts/day-01.md). The original acceptance command is `./mvnw -Plearner-tests -Dtest=QuoteSelectionAcceptanceTest,QuoteSelectionApiAcceptanceTest test`. The current main application reaches the Day 2 request-validator TODO before Day 1 selection behavior; do not implement Day 2 or production wiring to make the Day 1 API lesson work. A completed practice endpoint is API-bridge evidence only. Keep quote selection pending until its own contract is independently met.

The original quote-selection behavior and edge cases are unchanged: preserve first-seen order for exact composite pairs; same quote version under different journeys is distinct; repeated exact pairs collapse; null list/entry or null, empty, or blank key parts are rejected without partial output; whitespace is not trimmed; and delimiter-containing fields do not collide. Empty input returns an empty list and inputs remain unchanged. The supplied API remains `POST /api/quote-selections` with `{"selections":[{"journeyId":"journey-B","quoteVersionId":"home-secure-v1"}]}`. After the learner policy is implemented, valid input returns HTTP 200 with the exact key array; malformed or invalid input returns HTTP 400. Before the TODO is implemented, the separate existing app may encounter Day 2 validation first, while the isolated original Day 1 acceptance test uses its own validator and reaches the Day 1 TODO.

| Fixed input | Expected behavior |
|---|---|
| `[(journey-B, home-secure-v1), (journey-A, home-secure-v1), (journey-B, home-secure-v1)]` | Return the first two exact pairs in that order; leave input unchanged. |
| `[]` | Return `[]`. |
| Same quote version under two journey IDs | Keep both distinct composite keys. |
| Null list, null entry, or null/empty/blank `journeyId` or `quoteVersionId` | Throw `IllegalArgumentException`; return no partial output. |
| `home-secure-v1` and ` home-secure-v1` | Keep as distinct exact strings; do not trim. |
| `("journey-A|journey-B", "home-secure-v1")` and `("journey-A", "journey-B|home-secure-v1")` | Keep as distinct pairs; composite identity must not collide on delimiters. |

Use the existing 85-minute practice/testing allocation for the reference and small practice variation; use the 10-minute evidence allocation for exact commands/results and one concise request-to-response explanation. If the bridge is unclear, record the specific gap and carry quote-selection implementation forward within the existing plan. Do not add hours or an architecture case study.

**AWS certification:** No AWS course, account, infrastructure, or cloud task is assigned on Day 1. The assigned concept is local backend API learning.
