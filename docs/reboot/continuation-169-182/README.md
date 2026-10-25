# Engineering units 169–182 · calendar study days 190–203 · 2027-05-03–2027-05-16 — StreamForge Days 169–182: frozen offline baseline

Calendar mapping: stable engineering unit IDs 169–182 occupy study days 190–203 (2027-05-03 through 2027-05-16).

Draft starter artifacts on continuation branch `work/eai-sprint2-onward`, based on `ae21926a7d61ef25db8609ddc533d9cdeeacff88`; original starter reference `bb4cc3e5df7336c74a3f086b24510d325ea871e1` is retained as provenance. No board writes, cloud resources, external datasets, paid resources, or learner-history changes are part of this batch.

## Outcome and boundaries

A standard-library global popularity baseline counts distinct positive training viewers per eligible media item, compares it with a fixed ascending-ID reference, and reports macro Precision@3 and Recall@3 on a frozen synthetic temporal holdout. This is not personalization, a trained ML model, a service, or a real-user quality claim.

The fixed protocol is `streamforge-cutoff-popularity-v1`, K=3, watch_seconds > 0, candidate availability `from <= cutoff < to`, support by distinct training viewer, descending support then exact ID ascending, one global list, and all distinct positive holdout targets retained even when unreachable. Precision always divides by 3. Metrics are unweighted macro values over positive-target holdout viewers. Empty cases are explicit and never use NaN.

The hand-computed independent oracle is in `manifest.json`: supports a=0,b=2,c=1,d=3; popularity [d,b,c], ID-order reference [a,b,c]; macro popularity precision 4/9 and recall 5/6, reference precision 2/9 and recall 1/3. It is an arithmetic test fixture, not a tuning target or performance claim.

## Learner seams

Daily drafts are in `contracts/`. Pure functions depend on typed inputs only. Day 179 composes the existing preparation CLI; it must not bypass the earlier validator or emit partial success output. Temporary witnesses and passing infrastructure checks do not mean learner work is complete.

## Required reading and access caveats

The manifest contains only the scoped official/public primary references used by each day. Optional Maarek lessons are conceptual alternatives inside the study time. Course catalog data does not verify playback entitlement; the videos were not watched. No purchase or course completion is asserted. AI certification material is partial conceptual alignment, not exam readiness.

## Verification commands

```sh
python3 -m unittest discover -s scripts -p 'test_*.py'
python3 -m unittest ai.streamforge.tests.test_infrastructure
python3 -m unittest discover -s ai/tests -p test_infrastructure.py
```

Learner acceptance remains expected red at named TODOs until actual work is completed. AWS execution, production validation, exam choice, and future model work remain deferred.


## Prior A/B gate (re-evaluated from authoritative records)

Before any StreamForge implementation, run `python3 scripts/check_streamforge_169_182_entry_gate.py`. The actual records are `evidence/continuation-141-154/day-153.json` and `evidence/continuation-141-154/day-154.json`; both exist. Their current factual state is `not_attempted` / `not_run`, with no tested source SHA, artifact hashes, or learner completion, so the gate is **not met**. An exit status of 2 means carry forward A/B recovery; it does not mean the evidence files are missing. The checker re-evaluates the records, so later qualifying learner-owned evidence can satisfy the gate. Assistant/instructor witnesses cannot qualify.

## Architecture and interview schedule

See [assistant-supplied architecture notes](assistant-architecture-notes.md) for the exact implementation choices and reasons; learners implement and test them without capstone design proposals. System-design practice stays in the separate [question bank](../interview/question-bank.md), on the bounded days in [the interview/review schedule](interview-and-review-schedule.md). Days 169–171 retain 120 engineering minutes plus the external DSA tier; Days 172–182 use 165 engineering minutes plus one shared 45-minute block, for 210 total. There is no extra DSA task after the external mapped sequence is complete.
