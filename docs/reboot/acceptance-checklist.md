# Day 30 acceptance checklist — open until verified

This checklist starts with no completed claims. The learner records actual
evidence paths and results only after running the named checks. A planned date,
installed scaffold, or TODO failure is not completion evidence.

| Area | Required evidence | Status | Result |
|---|---|---|---|
| Quote identity/order and request errors | [instructor receipt](../../evidence/day-1/verification.json); learner attempt note still missing | open | learner evidence missing |
| HomeSecure quote, eligibility, and premium invariants | Day 4–7 learner attempt notes | open | missing |
| PostgreSQL schema, transactions, and minimal audit | Day 8–10 and Day 26 real PostgreSQL output | open | missing |
| Freshness, owner access, and safe API errors | Day 3, 11–12 attempt output | open | missing |
| Browser-to-database acceptance | Day 28 integrated test and PostgreSQL output | blocked | prerequisites incomplete |
| Disposable fixture reset | Day 29 command output on a disposable `*_test` database | open | missing |
| DSA attempts and due reviews | [error-log template](dsa-error-log-template.md); actual attempt and D/D+3/D+7/D+21 dates missing | open | missing |
| Remaining blockers and ranked next scope | Learner's Day 30 architecture note | open | missing |

Change `open` only when linked evidence exists and records the command, exit
status, and observed result. Change `blocked` only after prerequisites are
actually complete and the integrated gate runs against real components and
PostgreSQL. Missing, failed, or TODO-blocked evidence remains open or blocked.

## Interview readiness rubric

| Axis | Evidence | Result
|---|---|---|
| Behavior | Exact fixed-test command, exit status, and observed output | open until run
| Explanation | Learner explains invariant, chosen boundary/transaction, alternative, and failure in own words | open until demonstrated
| Independent adaptation | Small change/debug prompt completed without AI assistance | open until demonstrated

These axes are assessed separately. A passing test suite or installed scaffold does not imply conceptual understanding or independent adaptation. A milestone mock interview replaces part of the 85-minute implementation block; it does not extend the daily budget.
