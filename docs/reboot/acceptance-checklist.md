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

Score each dimension 0–2: 0 cannot demonstrate/incorrect; 1 demonstrates with hints or misses a boundary; 2 demonstrates independently with a counterexample and accurate explanation. Record assistance. This is a learning rubric, not a hiring score.

| Dimension | Evidence | Score / note |
|---|---|---|
| Contract | Inputs, outputs, ownership, invariant; identifies ambiguity before coding | open |
| Reasoning | Why approach preserves invariant, complexity/cost, one alternative | open |
| Implementation | Independently navigates/changes relevant Java, SQL or TypeScript; code matches explanation | open |
| Verification | Predicts negative result, runs targeted test, interprets failure, checks affected behavior | open |
| Transfer | Handles a changed assumption and identifies design pressure without unsupported claims | open |

Keep behavior (test result), explanation (invariants/trade-offs), and independent adaptation (small change/debug without AI) separate. A passing test or installed scaffold earns no credit on the other dimensions. A real result is required for `contract passed`; only an unassisted explanation supports `explanation independent`. Do not average away an authorization or atomicity defect. A gap becomes bounded repair work.

Days 14, 28 and 30 use a 20-minute mock/teach-back taken from implementation time (65 build/test + 20 mock + 25 concept + 10 evidence); no extra hours. When integration is blocked, explain the actual failing boundary and next diagnostic step, not a passing demo. See the [interview evidence overlay](interview-evidence-overlay.md) for dated official/anecdotal source distinctions.
