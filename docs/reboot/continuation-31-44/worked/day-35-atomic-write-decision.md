# Day 35: Atomic commit and receipt publication

> Project-authored teaching toy; not official course content and not learner completion evidence. Read this before the day's implementation task. All main prerequisite links refer to immutable main `80cb3cd` and commands run only in that main checkout.

## Worked example

Toy booking starts with `old` policy/receipt/event rows (one each). On one connection, begin; construct candidate `new` without publishing it; insert policy, event, then receipt with already-used key-7. The receipt uniqueness conflict forces rollback; fresh read sees only the three old rows and the in-memory service has no replayable `new` receipt. Retry with key-8: insert all three, commit; only then publish candidate as the same-instance replay receipt. Identical replay reuses the committed snapshot without another construction. Failure never publishes a phantom success. The actual SQL trace in the companion guide uses a disposable database and confirms rows, not a returned object alone.

## Transfer task

Transfer: implement existing atomic policy/receipt/event seam. Preserve exact old rows on conflict and assert no replay after rollback; after successful commit, repeat on the same service instance and verify original policy identity and one persisted policy/receipt/event. Decision: one connection/transaction avoids orphan rows; cost is holding transactional resources until writes finish. This is not restart-safe durable replay.

## Readiness check

Before editing, predict one changed input and its observable result. If the prediction is wrong, repeat this toy trace inside the study block and carry forward the learner seam; do not add time or infer completion from this guide.

## Executable SQL teaching trace

The independent `LifecycleWorkedReferenceTest#day35BookingReceiptConflictRollsBackEveryNewRow` uses its own disposable PostgreSQL schema (`toy_booking`, `toy_receipt`, `toy_booking_event`). It seeds one old row in each table, begins a transaction on one connection, inserts a candidate booking and event, then collides on `(member-a,key-7)`. A fresh connection confirms all three tables still contain only `old`; the candidate was never published into the toy's in-memory replay slot. A fresh key-8 commits the three `new` rows; only after `commit()` returns does the toy publish its receipt. A retry returns that same receipt without another candidate construction. Run from repository root: `./mvnw -Dtest=LifecycleWorkedReferenceTest#day35BookingReceiptConflictRollsBackEveryNewRow test`.

The competing decision is commit each row separately. Its failure leaves partial booking history. The chosen transaction keeps one invariant together at the cost of holding a connection/locks through all three writes. This is a local commit boundary, not a guarantee about a lost commit acknowledgement.
