# First 12 deep-practice sessions

Each session is 45 minutes inside the existing 85-minute practice block. The
first four are in Sprint 1 (two per week); the next eight remain a concrete
post-Sprint-1 sequence, two per week. Their exact calendar placement belongs in
the next sprint's revised story export after Sprint 1 review. Do not add an
extra study block or weaken a day's acceptance criteria to call a session
complete.

| # | Planned day/date | Prompt | Focus | Runnable evidence or artifact |
|---:|---|---|---|---|
| 1 | 3 · 2026-10-30 | Q08 | HTTP error ownership and safe client contract | Day 3 focused MockMvc error tests |
| 2 | 7 · 2026-11-03 | Q03 | Clock boundary and deterministic expiry | Day 7 fixed-clock contract tests |
| 3 | 10 · 2026-11-06 | Q13 | Transaction boundary for multiple writes | `Sprint1DesignExperimentsTest#rollbackKeepsAggregateAndReceiptConsistent` |
| 4 | 14 · 2026-11-10 | Q31 | Trace first incomplete backend boundary | Day 14 isolated integration selector |
| 5 | 17 · 2026-11-13 | Q01 | Selection identity and exact duplicates | Day 1 learner acceptance selector |
| 6 | 21 · 2026-11-17 | Q02 | Validate before invoking collaborators | Day 2 call-count acceptance selector |
| 7 | 24 · 2026-11-20 | Q10 | Composite keys and database constraints | Day 8 disposable PostgreSQL selector |
| 8 | 28 · 2026-11-24 | Q12 | Authenticated owner and foreign reads | Day 12 security acceptance selector |
| 9 | 31 · 2026-11-27 | Q14 | Owner-scoped request key | `Sprint1DesignExperimentsTest#ownerScopedKeyRejectsOnlySameOwnerDuplicate` |
| 10 | 35 · 2026-12-01 | Q16 | Evidence before index change | Day 9 JDBC query and plan output |
| 11 | 38 · 2026-12-04 | Q25 | React state during owner change | Day 12/14 browser state exercise |
| 12 | 42 · 2026-12-08 | Q30 | Independent UI/API/database oracle | Day 14 integration acceptance |

For each, spend 5 minutes clarifying and writing a no-AI proposal, 5 minutes
checking assumptions and acceptance, 15 minutes on the data/control-flow
choice, 10 minutes on a failure or trade-off, and 10 minutes comparing actual
evidence with the rubric. On session days, the remaining 40 practice minutes
stay on the daily EAI task. If it does not fit, carry the unfinished task
forward; do not claim acceptance from a mock.
