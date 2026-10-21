# Day 38: Append without rewriting history

> Project-authored teaching toy; not official course content and not learner completion evidence. Read this before the day's implementation task. All main prerequisite links refer to immutable main `80cb3cd` and commands run only in that main checkout.

## Worked example

Toy document revision2 contains `(1,Dec01,CREATED),(2,Dec03,RENAMED)`. Valid Dec04 edit yields revision3 and the same two-entry prefix plus `(3,Dec04,RETITLED)`; original input remains revision2 with its exact original list. Returning only the last event loses history; mutating or sorting the input corrupts prior readers.

## Transfer task

Transfer: compare full original and returned endorsement histories, version and effective order, then use a changed second fixture. Follow the existing ordering rule rather than inventing one.

## Readiness check

Before editing, predict one changed input and its observable result. If the prediction is wrong, repeat this toy trace inside the study block and carry forward the learner seam; do not add time or infer completion from this guide.
