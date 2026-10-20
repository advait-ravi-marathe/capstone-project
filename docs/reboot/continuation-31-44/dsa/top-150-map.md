# Java DSA Top 150 continuation map — Days 31–44

Scope continues the sequence through LC 153; it adds linked-list anchors on odd study days and no mandatory new problem on even days. Every planned first attempt is pending until recorded by the learner.

| Day | Date | Assignment | Role / forecast |
|---:|---|---|---|
| 31 | 2026-11-19 | LC 141 fast/slow pointers | new_pattern_anchor |
| 32 | 2026-11-20 | LC 153 D+3 | review_and_consolidation |
| 33 | 2026-11-21 | LC 206 pointer rewiring | new_pattern_anchor |
| 34 | 2026-11-22 | LC 141 D+3 | review_and_consolidation |
| 35 | 2026-11-23 | LC 21 two-pointer merge | new_pattern_anchor |
| 36 | 2026-11-24 | LC 206 D+3 | review_and_consolidation |
| 37 | 2026-11-25 | LC 143 split, reverse, weave | new_pattern_anchor |
| 38 | 2026-11-26 | LC 141 D+7; LC 21 D+3 | review_and_consolidation |
| 39 | 2026-11-27 | LC 19 lead/lag pointers | new_pattern_anchor |
| 40 | 2026-11-28 | LC 206 D+7; LC 143 D+3 | review_and_consolidation |
| 41 | 2026-11-29 | LC 138 identity map | new_pattern_anchor |
| 42 | 2026-11-30 | LC 21 D+7; LC 19 D+3 | review_and_consolidation |
| 43 | 2026-12-01 | LC 2 carry state | new_pattern_anchor |
| 44 | 2026-12-02 | LC 143 D+7; LC 138 D+3 | review_and_consolidation |

D+3, D+7, and D+21 are recalculated from the **actual** first-attempt date. Dates in the canonical JSON are conditional forecasts, never completion claims. For reviews, spend 25 minutes refreshing the pattern, 20 minutes solving cold, and 15 minutes on retrieval and a concrete error-log correction. For new anchors, use 25 minutes to learn the pattern, 20 minutes for an independent attempt, and 15 minutes for retrieval/error log.

Problems: LC 141 Linked List Cycle; LC 206 Reverse Linked List; LC 21 Merge Two Sorted Lists; LC 143 Reorder List; LC 19 Remove Nth Node From End of List; LC 138 Copy List with Random Pointer; LC 2 Add Two Numbers. Use the learner's chosen Java platform/editor; this repo does not ship problem solutions.

## Cross-batch recall queue (Days 1–30 carried into Days 31–44)

The first-30 manifest has 15 planned pattern anchors and no recorded actual first-attempt dates. Their D+3/D+7/D+21 dates remain conditional forecasts. The continuation carries all 15 anchors without inventing dates or completions. `actual-attempt-log.json` intentionally starts empty.

Before taking a new problem, compute the queue using the learner's real log. Reviews are cold recall and re-solving, not rereading. If a review fails, repair the gap in that same 60-minute DSA block and displace that day's new assignment; do not add study time.

- Queue: `python3 scripts/dsa-review-queue.py --on-date YYYY-MM-DD`
- Representative behavior tests: `python3 -m unittest scripts/test_dsa_review_queue.py`
- Log format and examples: [revision log template](revision-log-template.md)
