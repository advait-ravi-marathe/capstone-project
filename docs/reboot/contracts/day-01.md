# Day 01 — 2026-10-20: Quote selection identity/order

**State:** CONTRACT SCAFFOLD INSTALLED; learner implementation pending. Learner work is still pending.

Existing feature and acceptance paths are authoritative: `assignments.insurance.QuoteSelectionPolicy` and `POST /api/quote-selections` (existing).

## Fixed acceptance contract

**Prerequisite bridge:** Complete the studied reference and separate practice described in [`docs/reboot/day-01-api-bridge.md`](../day-01-api-bridge.md) before beginning quote-selection implementation. The essential course sequence is Step 02 (6:21) then Step 03 (4:30) inside the same 25-minute study block; use Step 00's overview only if API/request terms are unfamiliar. The guide supplies a runnable loopback-only test fixture, actual HTTP observations, and an independent practice TODO. It does not change the production app or solve this domain contract.

**Learner-owned domain behavior remains unchanged:** keep the first occurrence of each exact `(journeyId, quoteVersionId)` pair in encounter order. Reject null list/entries and null, empty or blank key parts before returning any partial output. Do not trim or normalize exact identifiers. Do not mutate input. The original acceptance suite remains authoritative for duplicates, interleaving, same version across journeys, distinct versions within a journey, delimiters, blank values and empty input.

**Repository boundary:** The request route is `POST /api/quote-selections`; the teaching fixture is a separate test-runtime app. The main application currently reaches the Day 2 request-validator TODO before the Day 1 selection TODO. Do not change production wiring or implement the Day 2 seam as part of this prerequisite. Existing isolated Day 1 API tests provide their own validator.

**Course/concept (25 minutes):** See the linked bridge for verified lesson titles, access status, source sections, the worked request→method→Java value→HTTP response trace, and timing. Catalog verification is not proof of viewing. Carry unfinished material forward within the same budget.

**Practice/test (within 85 minutes):** Start with `./mvnw -Dtest=Day01ApiBridgeInfrastructureTest test`; then make the one-field variation only in `src/test/java/assignments/learning/day01/Day01ApiPractice.java` and run `./mvnw -Plearner-tests -Dtest=Day01ApiPracticeAcceptanceTest test`. These prove the API bridge only. The original domain command remains separate: `./mvnw -Plearner-tests -Dtest=QuoteSelectionAcceptanceTest,QuoteSelectionApiAcceptanceTest test`.

**Evidence (within 10 minutes):** Record exact commands, status/body observed, assistance used, and a concise request → matched Java method → returned value → HTTP response explanation. If the prerequisite is unclear or the separate domain acceptance remains red, state that gap; do not award completion from the reference test.

**Today’s first independent DSA pattern anchor:** LeetCode 121 Best Time to Buy and Sell Stock (running-state array scan). Planned first-attempt date: Oct 20; actual attempt and review status remain unrecorded. Use 25 minutes to learn the pattern, 20 minutes to solve, and 15 minutes for same-day recall and the error log. D+3/D+7/D+21 reviews fall on Oct 23, Oct 27, and Nov 10 if the actual attempt occurs Oct 20; recalculate from the actual date. Reviews stay within the 60-minute block and are never marked complete automatically. See [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the existing evidence block)

**Question:** Which path invokes `greeting()`, which Java value becomes its JSON `message`, and why is an unmapped route different from a stopped server?

**Observable evidence:** Point to the actual running fixture and record its response. The quote-selection algorithm is not an API-prerequisite question; explain it only after its separate implementation is complete.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. Passing an instructor fixture is not independent understanding or learner completion. Preserve the separate LC 121 history and all original review dates/statuses.

**Related source:** [A3](../interview-evidence-overlay.md#a3) and [A8](../interview-evidence-overlay.md#a8) are anecdotal practice references, not frequency evidence.
