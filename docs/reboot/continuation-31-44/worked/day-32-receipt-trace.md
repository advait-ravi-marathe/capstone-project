# Day 32: Receipt identity and retry

> Project-authored teaching toy; not official course content and not learner completion evidence. Read this before the day's implementation task. All main prerequisite links refer to immutable main `80cb3cd` and commands run only in that main checkout.

## Worked example

Toy workshop booking starts with no receipt. `reserve(workshop-a,reserve-7,woodwork-17,2026-12-05)` constructs `reservation-r7`, stores the complete intent under `(owner,key)`, and returns it (count 0→1). An identical retry returns the same receipt (count remains 1). Changing only date to Dec06 conflicts; count stays 1 and original Dec05 receipt remains. Retrying original still returns r7. `workshop-b` may reuse key reserve-7 for its own seat and receives a separate receipt. A new key for a different valid intent must not return r7. This in-memory toy is single-threaded and not durable.

## Transfer task

Transfer: call the real issue seam repeatedly with same owner/key/intent; compare the entire original snapshot. Change quote, journey and effective date independently, require the named conflict, then replay original after each. Use another owner’s supplied valid fixture and a distinct key/intent. The instructor `PolicyIssueSnapshotFactory` supplies fresh candidates only; it does not implement receipt logic. Run Day32 with Day34+ TODOs intact.

## Readiness check

Before editing, predict one changed input and its observable result. If the prediction is wrong, repeat this toy trace inside the study block and carry forward the learner seam; do not add time or infer completion from this guide.

## Construction boundary supplied by the instructor

`PolicyIssueSnapshotFactory.create(request, authenticatedOwnerId)` constructs a fresh synthetic candidate. Its fixture key is `(owner, journeyId, quoteVersionId)`; it intentionally excludes `clientRequestId`. It has no receipt lookup/write, SQL, validation, payload comparison, or HTTP behavior. A fresh candidate counts as a construction. Thus the acceptance can observe: first request constructs once; identical replay and changed-intent conflict construct zero additional candidates; another owner’s valid key gets that owner’s own candidate. The existing `PolicyLifecycleService(Clock)` constructor remains and uses the fixed training fixture. This makes the seam runnable before Day35 without turning this factory into an issuance solution.

Runnable independent toy: `./mvnw -Dtest=LifecycleWorkedReferenceTest#day32WorkshopReceiptsPreserveIntentAndScope test`. It exercises the workshop Map itself, not the capstone service.
