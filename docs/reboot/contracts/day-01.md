# Day 01 — 2026-10-26: Quote selection identity/order

**State:** CONTRACT SCAFFOLD INSTALLED; learner implementation pending. Learner work is still pending.

Existing feature and acceptance paths are authoritative: `assignments.insurance.QuoteSelectionPolicy` and `POST /api/quote-selections` (existing).

## Fixed acceptance contract

**Prerequisite bridge:** Complete the studied reference and separate practice described in [`docs/reboot/day-01-api-bridge.md`](../day-01-api-bridge.md) before beginning quote-selection implementation. The essential course sequence is Step 02 (6:21) then Step 03 (4:30) inside the same 25-minute study block; use Step 00's overview only if API/request terms are unfamiliar. The guide supplies a runnable loopback-only test fixture, actual HTTP observations, and an independent practice TODO. It does not change the production app or solve this domain contract.

**Learner-owned domain behavior remains unchanged:** keep the first occurrence of each exact `(journeyId, quoteVersionId)` pair in encounter order. Reject null list/entries and null, empty or blank key parts before returning any partial output. Do not trim or normalize exact identifiers. Do not mutate input. The original acceptance suite remains authoritative for duplicates, interleaving, same version across journeys, distinct versions within a journey, delimiters, blank values and empty input.

**Repository boundary:** The request route is `POST /api/quote-selections`; the teaching fixture is a separate test-runtime app. The main application currently reaches the Day 2 request-validator TODO before the Day 1 selection TODO. Do not change production wiring or implement the Day 2 seam as part of this prerequisite. Existing isolated Day 1 API tests provide their own validator.

**Course/concept (25 minutes):** See the linked bridge for verified lesson titles, access status, source sections, the worked request→method→Java value→HTTP response trace, and timing. Catalog verification is not proof of viewing. Carry unfinished material forward within the same budget.
**Assistant-provided architecture decision (inside the existing 25-minute concept allocation):** [Keep the existing domain rule in the pure `QuoteSelectionPolicy`; the HTTP adapter owns transport mapping. This preserves the Day 2 validation prerequisite and keeps domain behavior independently testable.](../assistant-architecture-notes.md#day-1-quote-selection-boundary). No capstone design proposal or post-hoc architecture justification is assigned.

**Practice/test (within 85 minutes):** Start with `./mvnw -Dtest=Day01ApiBridgeInfrastructureTest test`; then make the one-field variation only in `src/test/java/assignments/learning/day01/Day01ApiPractice.java` and run `./mvnw -Plearner-tests -Dtest=Day01ApiPracticeAcceptanceTest test`. These prove the API bridge only. The original domain command remains separate: `./mvnw -Plearner-tests -Dtest=QuoteSelectionAcceptanceTest,QuoteSelectionApiAcceptanceTest test`.

**Evidence (within 10 minutes):** Record exact commands, status/body observed, assistance used, and a concise request → matched Java method → returned value → HTTP response explanation. If the prerequisite is unclear or the separate domain acceptance remains red, state that gap; do not award completion from the reference test.

## Interview transfer (inside the existing evidence block)

**Question:** Which path invokes `greeting()`, which Java value becomes its JSON `message`, and why is an unmapped route different from a stopped server?

**Observable evidence:** Point to the actual running fixture and record its response. The quote-selection algorithm is not an API-prerequisite question; explain it only after its separate implementation is complete.

**Related source:** [A3](../interview-evidence-overlay.md#a3) and [A8](../interview-evidence-overlay.md#a8) are anecdotal practice references, not frequency evidence.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
