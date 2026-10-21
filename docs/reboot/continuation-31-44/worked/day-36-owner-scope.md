# Day 36: Owner-scoped read

> Project-authored teaching toy; not official course content and not learner completion evidence. Read this before the day's implementation task. All main prerequisite links refer to immutable main `80cb3cd` and commands run only in that main checkout.

## Worked example

Toy document `d7` belongs to team-a. `find(d7,team-a)` returns its allowlisted title; `find(d7,team-b)` and `find(missing,team-a)` both return empty. An argument-recording store captures `(d7,team-a)`, proving both identifiers reach the seam in the right order. UI hiding after an unscoped lookup would not provide this boundary.

## Transfer task

Transfer: use `PolicyReadStore` with policy ID and owner ID; assert the owner sees the exact snapshot and foreign/absent both look absent. State the enforcement point and one missing production-auth capability.

## Readiness check

Before editing, predict one changed input and its observable result. If the prediction is wrong, repeat this toy trace inside the study block and carry forward the learner seam; do not add time or infer completion from this guide.
