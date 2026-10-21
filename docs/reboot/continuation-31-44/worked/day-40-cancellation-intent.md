# Day 40: Cancellation intent versus version

> Project-authored teaching toy; not official course content and not learner completion evidence. Read this before the day's implementation task. All main prerequisite links refer to immutable main `80cb3cd` and commands run only in that main checkout.

## Worked example

Toy booking ACTIVE v1 with injected today Dec01 receives key cancel-7/date Dec10/reason CUSTOMER_REQUEST/expected v1 and returns SCHEDULED v2. Same owner/key/intent with stale expected v1 replays the stored v2. Same key but date Dec11 conflicts and preserves Dec10. A new key with wrong expected version is rejected. For this curriculum, today itself is non-past and accepted; past is rejected.

## Transfer task

Transfer: test date, reason, expected version and receipt identity independently. The legacy selector name containing Future does not override the documented non-past rule. No scheduler is added.

## Readiness check

Before editing, predict one changed input and its observable result. If the prediction is wrong, repeat this toy trace inside the study block and carry forward the learner seam; do not add time or infer completion from this guide.
