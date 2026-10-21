# Day 37: Independent acceptance cases

> Project-authored teaching toy; not official course content and not learner completion evidence. Read this before the day's implementation task. All main prerequisite links refer to immutable main `80cb3cd` and commands run only in that main checkout.

## Worked example

Toy room-change baseline revision4, date Dec02, code CAPACITY, capacity30 is valid. Holding all else fixed, UNKNOWN code, past date Nov30, stale revision3, future revision5, and unchanged capacity20 each fail independently. A row that combines UNKNOWN and past date would not tell which guard worked.

## Transfer task

Transfer: preserve existing endorsement reason/amount/date/version rules. Add only orthogonal fixtures for already specified behavior (for example a past date with a known reason). Do not invent insurance policy rules.

## Readiness check

Before editing, predict one changed input and its observable result. If the prediction is wrong, repeat this toy trace inside the study block and carry forward the learner seam; do not add time or infer completion from this guide.
