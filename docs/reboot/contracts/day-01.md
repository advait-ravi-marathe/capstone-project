# Day 01 — 2026-10-20: Quote selection identity/order

**State:** CONTRACT SCAFFOLD INSTALLED; learner implementation pending. Learner work is still pending.

Existing feature and acceptance paths are authoritative: `assignments.insurance.QuoteSelectionPolicy` and `POST /api/quote-selections` (existing).

## Fixed acceptance contract

Keep first occurrence of each exact `(journeyId, quoteVersionId)` pair in encounter order. Preserve existing null/validation behavior.

**Edge cases:** Duplicate, interleaving, same version across journeys, distinct versions within a journey, exact strings, and existing invalid-input cases.

**Prerequisite/isolation:** No implementation prerequisite; inline acceptance data.

**Concept (25 minutes):** Composite identity and API contracts; AWS DVA-C02 Domain 1 Task 1 Skill 1.1.6. Distinguish in-memory deduplication from later database uniqueness, atomic writes, and concurrent retry safety.

**System-design prompt (within the 25-minute concept block):** Sketch request → controller → policy → response, mark validation ownership, and explain the composite-identity/order invariants in your own words. Do not add persistence or infrastructure. Use only the applicable blank template in the [system design guide](../system-design-practice.md).

**Evidence:** See [`docs/day-1.md`](../../day-1.md) and the existing acceptance tests. Do not change the established public shape.

**Today’s first independent DSA pattern anchor:** LeetCode 121 Best Time to Buy and Sell Stock (running-state array scan). Planned first-attempt date: Oct 20; actual attempt and review status remain unrecorded. Use 25 minutes to learn the pattern, 20 minutes to solve, and 15 minutes for same-day recall and the error log. D+3/D+7/D+21 reviews fall on Oct 23, Oct 27, and Nov 10 if the actual attempt occurs Oct 20; recalculate from the actual date. Reviews stay within the 60-minute block and are never marked complete automatically. See [`../dsa-error-log-template.md`](../dsa-error-log-template.md).
## Understanding check (within the existing 10-minute evidence block)

Choose one or two prompts and keep the response brief. This check uses the existing 10 evidence minutes; it is not extra work.

1. Explain what Day 1 (Quote selection identity/order) does, why its data structure, module boundary, or transaction choice fits, one alternative, and one failure case.
2. Without AI assistance, make or debug one small change to this feature and explain the observed result.

Record behavior (fixed-test result), explanation (invariants and trade-offs), and independent adaptation as separate outcomes. A scaffold or passing tests alone earns no explanation or adaptation credit.
