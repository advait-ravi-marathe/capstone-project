# First 12 deep-practice sessions

Each session is 45 minutes inside the existing 85-minute practice block. The
first four are in Sprint 1 (two per week); the next eight continue the same
two-per-week cadence. Dates follow the shifted 24 October 2026 roadmap. Each
session replaces 45 minutes of that day's 85-minute implementation/test
allocation. The corresponding 40-minute EAI slices and exact deferred queue
are defined in [Sprint 1 session scope](session-scope.md); a partial slice does
not complete the original day's acceptance.

| # | Planned day/date | Prompt | Focus | Prerequisite | Runnable evidence or artifact |
|---:|---|---|---|---|---|
| 1 | 3 · 2026-10-26 | Q08 | Gateway boundary, safe errors, client contract | HTTP route, response, and error basics | Day 3 `mapsUnknownResourceToNonDisclosingNotFound` + `mapsStaleVersionToConflict` |
| 2 | 7 · 2026-10-30 | Q03 | URL expiry and deterministic time | Stable key plus injected-clock idea | Day 7 `capturesVersionsPriceAndExactFifteenMinuteExpiry` |
| 3 | 10 · 2026-11-02 | Q13 | Booking transaction and atomic writes | Relational keys and one-connection SQL | `Sprint1DesignExperimentsTest#rollbackKeepsAggregateAndReceiptConsistent` |
| 4 | 14 · 2026-11-06 | Q05 | CoverStory domain workflow and first failed boundary | Actual Days 1–13 work; if blocked, trace only the first failure | Day 14 `Day14BackendSliceIT` first-failure trace |
| 5 | 17 · 2026-11-09 | Q01 | URL-key identity and exact duplicates | Session 1 API boundary and Day 1 composite identity | Day 1 learner acceptance selector |
| 6 | 21 · 2026-11-13 | Q07 | Gateway identity and owner-scoped reads | Session 1 trust boundary and Day 12 auth fixture | Day 12 security acceptance selector |
| 7 | 24 · 2026-11-16 | Q10 | Notification retry and durable intent | Session 3 transaction and key uniqueness | Day 8 disposable PostgreSQL selector |
| 8 | 28 · 2026-11-20 | Q14 | Booking retries and owner-scoped request keys | Sessions 3/7 atomicity and idempotency | `Sprint1DesignExperimentsTest#ownerScopedKeyRejectsOnlySameOwnerDuplicate` |
| 9 | 31 · 2026-11-23 | Q18 | Cache freshness and tenant isolation | Session 6 ownership; define permitted staleness | Day 12 owner state / read acceptance |
| 10 | 35 · 2026-11-27 | Q16 | Messaging order and replay | Sessions 7/8 durable intent and duplicate handling | Day 9 JDBC query and plan output |
| 11 | 38 · 2026-11-30 | Q28 | Feed fan-out and measured skew | Sessions 7/10 plus a measured workload | Day 12/14 browser state exercise |
| 12 | 42 · 2026-12-04 | Q30 | Search freshness and independent UI/API/database oracle | Session 6 authorization and independent acceptance | Day 14 integration acceptance |

Every session uses the exact rubric phases: **5 minutes Requirements; 5
minutes Capacity and constraints; 15 minutes Initial architecture and data
model; 10 minutes Bottlenecks, reliability, scaling; 10 minutes Trade-offs,
failures, follow-ups.** Save the initial proposal during the 15-minute
architecture phase before any hints, reference answer, or AI. On session days,
use the named 40-minute EAI slice and deferred queue. Do not claim acceptance
from a mock or add hours to complete deferred work.
