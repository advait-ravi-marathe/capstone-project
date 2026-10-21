# Day 42: Operational handover

> Project-authored teaching toy; not official course content and not learner completion evidence. Read this before the day's implementation task. All main prerequisite links refer to immutable main `80cb3cd` and commands run only in that main checkout.

## Worked example

Toy handover: (1) confirm disposable synthetic DB and current source; (2) run the actual focused read command and retain output; (3) expect cancelled state and original captured values; (4) run the fixed rejected-write case; (5) on mismatch, stop and preserve output/SHA—do not restart or edit data to get a green result; (6) after a bounded fix, rerun the same selector and keep both attempts. Mark solo rehearsal versus peer-observed accurately.

## Transfer task

Transfer: implement the existing write barrier and replace the generic read instruction with a command present in this branch. No customer handover or production ownership claim.

## Readiness check

Before editing, predict one changed input and its observable result. If the prediction is wrong, repeat this toy trace inside the study block and carry forward the learner seam; do not add time or infer completion from this guide.
