# Day 41: Clock-controlled transition

> Project-authored teaching toy; not official course content and not learner completion evidence. Read this before the day's implementation task. All main prerequisite links refer to immutable main `80cb3cd` and commands run only in that main checkout.

## Worked example

Toy scheduled state effective Dec10 is unchanged when fixed clock is Dec09; becomes CANCELLED v3 when fixed clock is Dec10; Dec11 applied to the original scheduled value also yields CANCELLED v3; reapplying to cancelled state changes nothing. Use `Clock.fixed(..., UTC)` so the result does not depend on workstation date.

## Transfer task

Transfer: test before/equal/after and repeat boundaries. The persisted overload must keep state/event atomic when integrated; a pure function does not prove cross-service exactly-once processing.

## Readiness check

Before editing, predict one changed input and its observable result. If the prediction is wrong, repeat this toy trace inside the study block and carry forward the learner seam; do not add time or infer completion from this guide.
