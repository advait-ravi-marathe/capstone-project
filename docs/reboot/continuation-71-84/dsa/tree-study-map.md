# Java DSA continuation map — Days 71–84

The independent DSA block remains 60 minutes: 25 minutes pattern learning,
20 minutes independent solving or actual due cold review, and 15 minutes
active retrieval/error logging. Run the shared queue first. The attempt log is
currently empty; planned dates below never count as attempts or reviews.

| Day | Date | Assignment | Role |
|---:|---|---|---|
| 71 | 2026-12-29 | Queue and cold recall; no new problem | review_and_consolidation |
| 72 | 2026-12-30 | Queue and cold recall; no new problem | review_and_consolidation |
| 73 | 2026-12-31 | [LC 112 Path Sum](https://leetcode.com/problems/path-sum/) — root-to-leaf recursion with remaining target | new_pattern_anchor |
| 74 | 2027-01-01 | Queue/cold recall; no new problem | review_and_consolidation |
| 75 | 2027-01-02 | Queue/cold recall; no new problem | review_and_consolidation |
| 76 | 2027-01-03 | Queue; planned LC112 D+3 review only if an actual attempt exists | review_and_consolidation |
| 77 | 2027-01-04 | Queue/cold recall; no new problem | review_and_consolidation |
| 78 | 2027-01-05 | Queue/cold recall; no new problem | review_and_consolidation |
| 79 | 2027-01-06 | [LC 230 Kth Smallest Element in a BST](https://leetcode.com/problems/kth-smallest-element-in-a-bst/) — inorder traversal and rank | new_pattern_anchor |
| 80 | 2027-01-07 | Queue; planned LC112 D+7 review only if an actual attempt exists | review_and_consolidation |
| 81 | 2027-01-08 | Queue/cold recall; no new problem | review_and_consolidation |
| 82 | 2027-01-09 | Queue; planned LC230 D+3 review only if an actual attempt exists | review_and_consolidation |
| 83 | 2027-01-10 | Recovery and cold retrieval; no new problem | review_and_consolidation |
| 84 | 2027-01-11 | Independent assessment; no new problem | review_and_consolidation |

LC112 planned attempt is Dec 31; its forecast reviews are Jan 3, Jan 7 and
Jan 21. LC230 planned attempt is Jan 6; forecast reviews are Jan 9, Jan 13 and
Jan 27. Forecasts are not actual attempts. The shared queue computes due dates
only from the actual first-attempt date. Failed reviews displace any new work
inside the same 60-minute DSA block.

LeetCode Top Interview 150 membership was not independently verified for
these tree questions; the links are the official problem statements. New
anchors are provisional until the learner completes a recorded independent
first attempt.

- Queue: `python3 scripts/dsa-review-queue.py --on-date YYYY-MM-DD`
- Queue tests: `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py scripts/test_dsa_review_queue_71_84.py`
- Shared actual-attempt log: `docs/reboot/continuation-31-44/dsa/actual-attempt-log.json`
