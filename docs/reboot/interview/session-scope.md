# Sprint 1 mock-day implementation slices and deferred queue

Each interview session occupies 45 minutes of the day's 85-minute practice
allocation. The other 40 minutes have a named EAI scope. That is a partial
subtask, not a shortened definition of the day's acceptance. The original
contract remains pending until its complete acceptance selector passes. The
remaining work below is an explicit post-Sprint-1 queue; it must not be silently
dropped, counted as complete, or added as overtime.

| Day/date | 45-minute interview | 40-minute EAI slice | Explicit deferred work and completion check |
|---|---|---|---|
| 3 · 2026-10-28 | Q08 gateway boundary and safe errors | Inspect `ApiExceptionHandler`; implement/map only the expected 404 and 409 responses, then run `./mvnw -Plearner-tests -Dtest=Day03ContractTest#mapsUnknownResourceToNonDisclosingNotFound+mapsStaleVersionToConflict test`. Save actual result. | **Queue S1-D03-1:** sanitized unexpected 500 and secret-detail rejection; run the full `./mvnw -Plearner-tests -Dtest=Day03ContractTest test`. Day 3 remains pending until all three assertions pass. |
| 7 · 2026-11-01 | Q03 URL expiry and deterministic time | Implement the fixed-clock quote snapshot and capture its versions, price, and 15-minute expiry; run `./mvnw -Plearner-tests -Dtest=Day07ContractTest#capturesVersionsPriceAndExactFifteenMinuteExpiry test`. | **Queue S1-D07-1:** before/equal/after expiry, ineligible input with zero pricing calls, and full `./mvnw -Plearner-tests -Dtest=Day07ContractTest test`. Day 7 remains pending until all cases pass. |
| 10 · 2026-11-04 | Q13 booking transaction and atomic writes | Implement the ordered batch happy path and empty batch; run `./mvnw -Plearner-tests -Dtest=Day10ContractTest#persistsRowsInInputOrderWithContiguousZeroBasedPositions+emptyBatchCreatesNoRows test`. | **Queue S1-D10-1:** injected second-row failure rollback preserving preexisting rows, plus full `./mvnw -Plearner-tests -Dtest=Day10ContractTest test`. Day 10 remains pending until rollback and positive cases pass. |
| 14 · 2026-11-08 | Q05 CoverStory workflow and first failure boundary | Run `./mvnw -Plearner-tests -Dtest=Day14BackendSliceIT test` within the timebox, preserve the exact first failure, and trace the earliest incomplete dependency without solving a later TODO. | **Queue S1-D14-1:** rerun the same full integration selector after the actual Day 1–13 prerequisites pass; compare persisted rows and all declared acceptance cases. Day 14 remains blocked until this run passes. |

## Queue order

After Sprint 1, process `S1-D03-1`, `S1-D07-1`, and `S1-D10-1` in day order,
then `S1-D14-1` when its prerequisites are genuinely ready. A task that still
fails stays at the front with its actual blocker. Later work that depends on
that contract is not reported as integrated. This queue records displaced
acceptance work; it is not a new sprint or an added course assignment.

The independent system-design mock and its scored feedback are separate from
the EAI test result. Keep the saved first proposal, exact command/exit/result,
assistance label, and any deferred queue ID in the existing evidence record.
