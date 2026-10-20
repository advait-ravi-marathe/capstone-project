# DSA actual-attempt and review log

Append a record only after attempting the problem. Never use the ticket date as a substitute for the actual attempt date. Leave this file's starter `events` empty until the learner records real work.

```json
{"anchor_day":29,"problem_id":153,"kind":"first_attempt","date":"YYYY-MM-DD","result":"attempted"}
{"anchor_day":29,"problem_id":153,"kind":"review","interval_days":3,"date":"YYYY-MM-DD","result":"passed|failed"}
```

The due anchors are D+3, D+7 and D+21 from `first_attempt.date`. A review is a cold recall and re-solve, not rereading. If a review fails, repair the gap inside that same day's 60-minute DSA budget and displace the new problem; do not extend the session. Record the failed retrieval and the next cold attempt honestly.

Run the queue over Days 1–30 and Days 31–44 together:

```sh
python3 scripts/dsa-review-queue.py --on-date 2026-11-19
python3 -m unittest scripts/test_dsa_review_queue.py
```

With the starter log empty, the tool marks every anchor `awaiting_actual_attempt`, creates no due date, and marks no review complete. Tests use synthetic records solely to prove actual-date anchoring, cross-batch carryover, and failed-review displacement.
