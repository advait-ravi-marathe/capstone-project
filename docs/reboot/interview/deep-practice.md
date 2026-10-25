# First 12 deep-practice sessions

Each session is 45 minutes inside the existing 85-minute practice block. The
first four are in Sprint 1 (two per week); sessions 5–8 are in Sprint 2 (two
per week); the next four continue the same cadence. Dates follow the shifted
26 October 2026 roadmap. Each session replaces 45 minutes of that day's
85-minute implementation/test allocation. The corresponding 40-minute EAI
slices and named deferred acceptance are defined in [Sprint 1 session
scope](session-scope.md) for sessions 1–4, [Sprint 2 session
scope](sprint2-session-scope.md) for sessions 5–8, and [Sprint 3 session
scope](sprint3-session-scope.md) for sessions 9–12. A partial slice does not
complete the original day's acceptance.

| # | Planned day/date | Prompt | Focus | Prerequisite | Runnable evidence or artifact |
|---:|---|---|---|---|---|
| 1 | 3 · 2026-10-28 | Q08 | Gateway boundary, safe errors, client contract | HTTP route, response, and error basics | Day 3 `mapsUnknownResourceToNonDisclosingNotFound` + `mapsStaleVersionToConflict` |
| 2 | 7 · 2026-11-01 | Q03 | URL expiry and deterministic time | Stable key plus injected-clock idea | Day 7 `capturesVersionsPriceAndExactFifteenMinuteExpiry` |
| 3 | 10 · 2026-11-04 | Q13 | Booking transaction and atomic writes | Relational keys and one-connection SQL | `Sprint1DesignExperimentsTest#rollbackKeepsAggregateAndReceiptConsistent` |
| 4 | 14 · 2026-11-08 | Q05 | CoverStory domain workflow and first failed boundary | Actual Days 1–13 work; if blocked, trace only the first failure | Day 14 `Day14BackendSliceIT` first-failure trace |
| 5 | 17 · 2026-11-11 | Q01 | URL-key identity and exact duplicates | Session 1 API boundary and Day 1 composite identity | [Sprint 2 40-minute slice and queue](sprint2-session-scope.md#sprint-2-interview-sessions-and-eai-slices) |
| 6 | 21 · 2026-11-15 | Q07 | Gateway identity and owner-scoped reads | Session 1 trust boundary and Day 12 auth fixture | [Sprint 2 40-minute slice and queue](sprint2-session-scope.md#sprint-2-interview-sessions-and-eai-slices) |
| 7 | 24 · 2026-11-18 | Q10 | Notification retry and durable intent | Session 3 transaction and key uniqueness | [Sprint 2 40-minute slice and queue](sprint2-session-scope.md#sprint-2-interview-sessions-and-eai-slices) |
| 8 | 28 · 2026-11-22 | Q14 | Booking retries and owner-scoped request keys | Sessions 3/7 atomicity and idempotency | [Sprint 2 40-minute slice and queue](sprint2-session-scope.md#sprint-2-interview-sessions-and-eai-slices) |
| 9 | 31 · 2026-11-25 | Q05 | CoverStory issue workflow and quote eligibility | Actual Day 12 owner identity and Days 13/28 quote/selection prerequisites | Day 31 `day31RejectsForeignUnselectedStaleOrIneligibleQuoteWithoutIssue`; [Sprint 3 40-minute slice and queue](sprint3-session-scope.md) |
| 10 | 35 · 2026-11-29 | Q13 | Atomic booking and durable uniqueness | Sessions 3/8 transaction and retry concepts; actual Day 31–34 issuance decisions | Day 35 `day35AtomicPostgresWriteHasOnePolicyAndOneReceipt`; [Sprint 3 40-minute slice and queue](sprint3-session-scope.md) |
| 11 | 38 · 2026-12-02 | Q16 | Ordered durable history, duplicate delivery, and replay | Actual issue event and Day 37 validation; use messaging only as a comparison | Day 38 `day38EndorsementAppendsAndPreservesPriorHistoryInEffectiveOrder`; [Sprint 3 40-minute slice and queue](sprint3-session-scope.md) |
| 12 | 42 · 2026-12-06 | Q11 | Scheduled transition and safe recovery | Actual Day 41 clock transition; explain schedule state without implementing a scheduler | Day 42 `day42CancelledPolicyRejectsWritesButRemainsReadable`; [Sprint 3 40-minute slice and queue](sprint3-session-scope.md) |

Every session uses the exact rubric phases: **5 minutes Requirements; 5
minutes Capacity and constraints; 15 minutes Initial architecture and data
model; 10 minutes Bottlenecks, reliability, scaling; 10 minutes Trade-offs,
failures, follow-ups.** Save the initial proposal during the 15-minute
architecture phase before any hints, reference answer, or AI. On session days,
use the named 40-minute EAI slice and deferred queue. Do not claim acceptance
from a mock or add hours to complete deferred work.
