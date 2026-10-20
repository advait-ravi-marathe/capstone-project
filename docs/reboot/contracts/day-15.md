# Day 15 — 2026-11-03: Read journey summary

**State:** Real-PostgreSQL test fixture and learner query seam installed. The acceptance tests currently stop at the explicit query TODO; no learner implementation is included.

## Learner seam

`JourneySummaryQuery`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Return exactly `{journeyId, quote, selections}`. `quote` is null or the Day 7 snapshot projection; `selections` contains `{quoteVersionId, selectionOrder}` ordered by `selectionOrder` ascending. Empty selections are valid. Unknown/other-owned journey uses the same non-disclosing 404. No entity or owner field may escape.

## Required edge cases

Empty, populated, unknown, other-owned, and exact JSON field allowlist.

## Prerequisite and isolation

Days 9 and 12; seeded read model. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Choose a module or sequence view for the journey summary read path. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

SQL joins, projections, null/empty semantics [E,F].

## Evidence (10 minutes)

Empty/populated JSON fixtures. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day15ContractTest test`

This selector starts a real embedded PostgreSQL instance with fixed owner/quote/selection rows and exercises the query TODO. Its expected TODO failures are not an infrastructure pass.

## Separate Java DSA block (60 minutes)

**New pattern anchor:** LeetCode 11 Container With Most Water — pointer-elimination proof. Learn the pattern for 25 minutes, solve independently for 20 minutes, and spend 15 minutes on same-day D recall plus the error log. Record the actual first-attempt date; schedule D+3, D+7 and D+21 reviews from that date. Planned dates in the manifest are forecasts only. A failed prerequisite/review displaces the next new anchor; do not add overtime. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).
## Understanding check (within the existing 10-minute evidence block)

Choose one or two prompts and keep the response brief. This check uses the existing 10 evidence minutes; it is not extra work.

1. Explain what Day 15 (Read journey summary) does, why its data structure, module boundary, or transaction choice fits, one alternative, and one failure case.
2. Without AI assistance, make or debug one small change to this feature and explain the observed result.

Record behavior (fixed-test result), explanation (invariants and trade-offs), and independent adaptation as separate outcomes. A scaffold or passing tests alone earns no explanation or adaptation credit.
