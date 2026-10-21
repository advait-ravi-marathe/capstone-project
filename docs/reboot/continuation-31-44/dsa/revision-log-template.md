# DSA actual-attempt and review log

Append a record only after a real attempt or observed exposure. Planned dates,
editorial reading, tickets and this policy change never create or reset an
actual attempt. Retain old v1 records exactly as written; the queue adapts them
as legacy events with assistance unclassified.

A new v2 event uses the stable canonical identity rather than the day on which
an anchor was moved. Example shapes (examples only; do not copy as learner
history):

```json
{"schema_version":2,"event_id":"unique-local-id","date":"YYYY-MM-DD","timezone":"Asia/Kolkata","anchor_id":"lc:121","kind":"anchor_attempt","activity":"guided_anchor","target_problem_id":121,"assistance":["editorial"],"result":"attempted","minutes":25}
{"schema_version":2,"event_id":"unique-local-id-2","date":"YYYY-MM-DD","timezone":"Asia/Kolkata","anchor_id":"lc:121","kind":"retrieval_attempt","activity":"independent_transfer","review_interval_days":3,"review_slot_id":"lc:121+3","target_problem_id":2016,"attempt_id":"physical-attempt-1","evidence_reference":"local-evidence-1","assistance":["none"],"result":"passed","minutes":20,"invariant_explanation":"Write your own evidence here."}
{"schema_version":2,"event_id":"unique-local-id-3","date":"YYYY-MM-DD","timezone":"Asia/Kolkata","anchor_id":"lc:121","kind":"retrieval_attempt","activity":"repair","review_interval_days":3,"review_slot_id":"lc:121+3","target_problem_id":2016,"attempt_id":"physical-repair-1","evidence_reference":"local-repair-evidence-1","repairs_event_id":"earlier-failed-event-id","assistance":["none"],"result":"passed","minutes":15}
{"schema_version":2,"event_id":"unique-local-id-4","date":"YYYY-MM-DD","timezone":"Asia/Kolkata","anchor_id":"lc:121","kind":"retrieval_attempt","activity":"qualified_extension","review_interval_days":7,"review_slot_id":"lc:121+7","target_problem_id":1014,"attempt_id":"physical-extension-1","evidence_reference":"local-extension-evidence-1","assistance":["none"],"result":"attempted","minutes":20}
```

Use `kind` values `anchor_attempt`, `retrieval_attempt`, `exposure`, or
`correction`. A correction references the event being corrected; never erase or
rewrite an earlier event. Identical duplicate event IDs are idempotent;
conflicting duplicates are invalid. Record only actual local dates in the
learner's timezone. Future-dated events remain visible but do not enter an
earlier as-of queue.

For an independent transfer, attempt before viewing the LeetCode Premium
editorial or a targeted hint. Editorial, hint, walkthrough, other assistance,
or unknown help is not an independent pass, even if LeetCode accepts the code.
After help, record the work as assisted study/repair. A repair references the
earlier failed event and must keep its same anchor, slot and target. Only a
passing unaided repair resolves that failure; it does not close independent
transfer credit. A transfer is not a new canonical anchor and never starts
its own review clock. A catalog-qualified extension or partial component uses
`activity: "qualified_extension"`; it remains separate from independent
transfer credit and leaves the checkpoint open.

The only review clock starts at the earliest actual first attempt for that
canonical anchor, including an assisted/guided attempt. D+3, D+7 and D+21 are
separate calendar checkpoints. A successful earlier checkpoint cannot close a
later one; later failures stay visible. A short original-anchor recall is useful
but does not close a missing independent-transfer checkpoint. Carry overflow
with its original due date.

## Reading the daily queue

```sh
python3 scripts/dsa-review-queue.py --on-date YYYY-MM-DD --roadmap-day N
python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py scripts/test_dsa_review_queue_71_84.py scripts/test_dsa_guided_transfer_policy.py
```

The session budget is selected from scheduled substantive exercises: one or
two = 60 minutes, three = 75 minutes, four = 90 minutes. Engineering remains
120 minutes (25 study, 85 practice, 10 evidence). Do not add an exercise to fill
a tier. Failed repair and overdue retrieval displace new guidance. More than
four required items are visibly carried with their original dates; no D+21 is
dropped. If no exercise is queued, use existing recall/error-log work without
inventing a question or an attempt.

See [`../../dsa/guided-transfer-policy.md`](../../dsa/guided-transfer-policy.md),
[`../../dsa/canonical-anchors.json`](../../dsa/canonical-anchors.json), and the
verified candidate/gap catalog. Catalog suggestions require history,
freshness, access and prerequisite checks; no candidate is assumed unseen.
The starter history remains empty and is not evidence that the learner has no
private practice.
