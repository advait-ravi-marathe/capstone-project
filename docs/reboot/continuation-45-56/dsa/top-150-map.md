# Java DSA continuation map — Days 45–56

One 60-minute block daily: odd study dates carry a new pattern anchor; even dates prioritize actual due reviews and active retrieval. The shared actual-attempt log and queue remain authoritative. No actual attempt dates or mastery are claimed by this plan.

| Day | Date | Assignment | Role / conditional review |
|---:|---|---|---|
| 45 | 2026-12-03 | LC 128 Longest Consecutive Sequence (hash-set sequence starts) | new_pattern_anchor |
| 46 | 2026-12-04 | LC 2 D+3 (forecast 2026-12-04) | review_and_consolidation |
| 47 | 2026-12-05 | LC 15 3Sum (sort, then move two pointers) | new_pattern_anchor |
| 48 | 2026-12-06 | LC 128 D+3 (forecast 2026-12-06) | review_and_consolidation |
| 49 | 2026-12-07 | LC 49 Group Anagrams (canonical keys and hash grouping) | new_pattern_anchor |
| 50 | 2026-12-08 | LC 2 D+7 (forecast 2026-12-08); LC 15 D+3 (forecast 2026-12-08) | review_and_consolidation |
| 51 | 2026-12-09 | LC 20 Valid Parentheses (stack invariants and matching delimiters) | new_pattern_anchor |
| 52 | 2026-12-10 | LC 128 D+7 (forecast 2026-12-10); LC 49 D+3 (forecast 2026-12-10) | review_and_consolidation |
| 53 | 2026-12-11 | LC 150 Evaluate Reverse Polish Notation (stack evaluation) | new_pattern_anchor |
| 54 | 2026-12-12 | LC 15 D+7 (forecast 2026-12-12); LC 20 D+3 (forecast 2026-12-12) | review_and_consolidation |
| 55 | 2026-12-13 | LC 155 Min Stack (stack plus cached minimum invariant) | new_pattern_anchor |
| 56 | 2026-12-14 | LC 49 D+7 (forecast 2026-12-14); LC 150 D+3 (forecast 2026-12-14) | review_and_consolidation |

Problems: [LC 128 Longest Consecutive Sequence](https://leetcode.com/problems/longest-consecutive-sequence/); [LC 15 3Sum](https://leetcode.com/problems/3sum/); [LC 49 Group Anagrams](https://leetcode.com/problems/group-anagrams/); [LC 20 Valid Parentheses](https://leetcode.com/problems/valid-parentheses/); [LC 150 Evaluate Reverse Polish Notation](https://leetcode.com/problems/evaluate-reverse-polish-notation/); [LC 155 Min Stack](https://leetcode.com/problems/min-stack/). These are selected Top Interview 150 continuation problems; verify problem-list membership in the plan UI if uncertain, and use each official problem statement linked here.

D+3/D+7/D+21 are conditional forecasts only. Append an actual first-attempt event to the shared [actual-attempt log](../../continuation-31-44/dsa/actual-attempt-log.json) only after doing the work. The common queue reads the Days 1–30, 31–44 and 45–56 manifests so carryover can be recalculated from real dates.

- Queue: `python3 scripts/dsa-review-queue.py --on-date YYYY-MM-DD`
- Tests: `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py`
- New anchors use 25 minutes pattern learning, 20 independent solve, 15 retrieval/error log.
- Consolidation days use 25 minutes pattern recall, 20 cold review(s), 15 error-log repair.
- A failed review displaces new work inside the same 60-minute block.
