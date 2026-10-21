# Day 31: Discovery and server facts

> Project-authored teaching toy; not official course content and not learner completion evidence. Read this before the day's implementation task. All main prerequisite links refer to immutable main `80cb3cd` and commands run only in that main checkout.

## Worked example

A fictional room operator says “people reserve rooms they cannot use.” The supplied exercise assumption is that a member must be active, the room must belong to their branch, and the slot must be available. No interview occurred. Body `{roomId:r1,slotId:s1}` is intent; the test principal is `m1`; server fixtures say `membership(m1,active,b1)`, `room(r1,b1)`, `slot(s1,available)`. Result: allowed. Change only room branch to `b2`: denied before any reservation. Adding `memberId:m2` to the body must not change principal `m1` (strict decoding may reject the extra field).

## Transfer task

Transfer: keep the existing owner/selected/current/eligible quote matrix. Write one goal, one open question, one stated assumption and three observable cases in the shared delivery exercise; then implement only `validateIssue`. Name which `PolicyIssueFacts` values were looked up, not trusted from the request. Read the main-only Day02/03 request and errors guides at immutable main SHA 80cb3cd.

## Readiness check

Before editing, predict one changed input and its observable result. If the prediction is wrong, repeat this toy trace inside the study block and carry forward the learner seam; do not add time or infer completion from this guide.
