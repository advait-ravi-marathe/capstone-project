# Day 33: Captured immutable facts

> Project-authored teaching toy; not official course content and not learner completion evidence. Read this before the day's implementation task. All main prerequisite links refer to immutable main `80cb3cd` and commands run only in that main checkout.

## Worked example

Toy booking records lesson-v1, price-v1, 80.00 EUR and notes `[captured at booking]`. Later catalog is lesson-v2 / 95.00 EUR. The old booking still reports v1 / 80.00 EUR. A defensive `List.copyOf` also means adding to the source notes does not alter the record and `original.notes().add(...)` throws. This protects the collection boundary; it does not claim arbitrary nested objects are deeply immutable.

## Transfer task

Transfer: change one captured policy field at a time and compare the supplied before/after snapshots. Explain which later lifecycle state/history fields may legitimately differ. The current selector compares values; it does not execute catalog SQL.

## Readiness check

Before editing, predict one changed input and its observable result. If the prediction is wrong, repeat this toy trace inside the study block and carry forward the learner seam; do not add time or infer completion from this guide.
