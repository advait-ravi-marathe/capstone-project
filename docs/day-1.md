# Day 1 — Synthetic quote selection invariant

## Work budget

Total scheduled engineering time is 120 minutes: 85 minutes implementation and
testing, 25 minutes concept/course, and 10 minutes evidence. Instructor-owned
scaffolding and setup are outside this budget. There is no extra mandatory
setup work. Record actual time; do not claim this allocation is validated until
a learner attempt has been measured.

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

## Separate DSA track

All DSA assignments, code, tests, schedules, and attempt history are maintained only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). The Day 1 engineering contract here remains unchanged.
