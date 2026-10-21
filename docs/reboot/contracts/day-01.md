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


## Interview transfer (inside the current evidence block)

**Question:** What makes two selections equal? Why must output retain encounter order? If journeys match but versions differ, what survives?

**Observable evidence:** Trace interleaved duplicates by hand; explain equality/hash contract and time/space cost without being told a collection choice. Separately explain LC 121’s buy-before-sell invariant and a falling-price counterexample.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A3](../interview-evidence-overlay.md#a3) (anecdotal); [A8](../interview-evidence-overlay.md#a8) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided first encounter:** [LeetCode 121 Best Time to Buy and Sell Stock](https://leetcode.com/problems/best-time-to-buy-and-sell-stock/). Use the Premium editorial/solution only after stating the problem in your own words and making a first attempt; the resulting work remains guided/assisted and earns no independent-transfer credit. Preserve the original canonical anchor identities and record any actual attempt only when it occurs.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 1` first. The date command uses the actual Asia/Kolkata local study date; dates shown elsewhere are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../dsa/guided-transfer-policy.md](../dsa/guided-transfer-policy.md) and [the DSA error log](../dsa-error-log-template.md).
