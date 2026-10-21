# Day 42: Operational handover

> Project-authored teaching toy; not official course content and not learner completion evidence. Read this before the day's implementation task. All main prerequisite links refer to immutable main `80cb3cd` and commands run only in that main checkout.

## Worked example

Worked local handover (no external database): use `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day42CancelledPolicyRejectsWritesButRemainsReadable test` from the repository root. This selector opens no database connection; its in-memory fixture is policy-a owned by owner-a in CANCELLED state. It reads the exact snapshot through `readForOwner(store, "owner-a", "policy-a")`, then calls the write-barrier predicate and expects `false`; it passes only when the owned read returns that snapshot and the cancelled write is rejected. Preconditions are the local test fixture and current source SHA. If the read is empty/wrong or the predicate allows the write, stop, save the command output and SHA, and report the first mismatch; do not restart or edit fixture data to get a green result. After a bounded learner fix, rerun the same selector and preserve both results. This is a solo synthetic rehearsal unless a second person actually observes it.

## Transfer task

Transfer: implement the existing write barrier. Use the supplied selector to rehearse the real branch seam, then record its actual result and first unresolved prerequisite in the shared delivery exercise. No customer handover or production ownership claim.

## Readiness check

Before editing, predict one changed input and its observable result. If the prediction is wrong, repeat this toy trace inside the study block and carry forward the learner seam; do not add time or infer completion from this guide.
