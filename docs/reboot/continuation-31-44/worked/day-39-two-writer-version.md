# Day 39: Two writers and one version

> Project-authored teaching toy; not official course content and not learner completion evidence. Read this before the day's implementation task. All main prerequisite links refer to immutable main `80cb3cd` and commands run only in that main checkout.

## Worked example

Toy database document d7 is revision4. A and B read 4. A conditionally updates `WHERE id=d7 AND revision=4`, affected rows=1, appends A event and commits. B's same conditional update then affects 0 rows, adds no event, and reports conflict. Fresh read is revision5/body A with exactly A’s event. If B wins, the persisted row and event must both match B. Use barriers, not sleeps.

## Transfer task

Transfer: use the existing synchronized two-connection PostgreSQL selector. Assert one winner, one conflict, exactly one increment, and that the persisted event contents match the actual winning request. Explain why the losing intent is not silently retried against a newer version.

## Readiness check

Before editing, predict one changed input and its observable result. If the prediction is wrong, repeat this toy trace inside the study block and carry forward the learner seam; do not add time or infer completion from this guide.
