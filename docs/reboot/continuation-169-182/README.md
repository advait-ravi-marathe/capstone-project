# StreamForge Days 169–182: frozen offline baseline

Draft starter artifacts based on the verified predecessor source `bb4cc3e5df7336c74a3f086b24510d325ea871e1`. No board writes, cloud resources, external datasets, paid resources, or learner-history changes are part of this batch.

## Outcome and boundaries

A standard-library global popularity baseline counts distinct positive training viewers per eligible media item, compares it with a fixed ascending-ID reference, and reports macro Precision@3 and Recall@3 on a frozen synthetic temporal holdout. This is not personalization, a trained ML model, a service, or a real-user quality claim.

The fixed protocol is `streamforge-cutoff-popularity-v1`, K=3, watch_seconds > 0, candidate availability `from <= cutoff < to`, support by distinct training viewer, descending support then exact ID ascending, one global list, and all distinct positive holdout targets retained even when unreachable. Precision always divides by 3. Metrics are unweighted macro values over positive-target holdout viewers. Empty cases are explicit and never use NaN.

The hand-computed independent oracle is in `manifest.json`: supports a=0,b=2,c=1,d=3; popularity [d,b,c], ID-order reference [a,b,c]; macro popularity precision 4/9 and recall 5/6, reference precision 2/9 and recall 1/3. It is an arithmetic test fixture, not a tuning target or performance claim.

## Clock and DSA queue

Days 169–171 retain 120 engineering minutes and existing DSA tiers. Day 172 onward is 165 engineering plus one shared 45-minute interview/unfinished-review block, 210 total. The proposed 35/115/15 engineering split is explicitly a planning allocation. The shared block budgets one substantive review at most; overdue/failed work keeps priority and original due dates. No new guided DSA anchors after Day 150.

## Learner seams

Daily drafts are in `contracts/`. Pure functions depend on typed inputs only. Day 179 composes the existing preparation CLI; it must not bypass the earlier validator or emit partial success output. Temporary witnesses and passing infrastructure checks do not mean learner work is complete.

## Required reading and access caveats

The manifest contains only the scoped official/public primary references used by each day. Optional Maarek lessons are conceptual alternatives inside the study time. Course catalog data does not verify playback entitlement; the videos were not watched. No purchase or course completion is asserted. AI certification material is partial conceptual alignment, not exam readiness.

## Verification commands

```sh
python3 -m unittest ai.streamforge.tests.test_evaluation_infrastructure scripts.test_streamforge_169_182_manifest scripts.test_dsa_post171_budget
python3 -m unittest discover -s scripts -p 'test_*.py'
python3 -m unittest ai.streamforge.tests.test_infrastructure
python3 -m unittest discover -s ai/tests -p test_infrastructure.py
```

Learner acceptance remains expected red at named TODOs until actual work is completed. AWS execution, production validation, exam choice, and future model work remain deferred.
FDE alignment: See [the North Star amendment](../fde-north-star-amendment.md) and [reference-only alignment overlay](../fde-alignment.json). Existing curriculum, prerequisites, budgets, DSA history and learner status remain unchanged; this adds purpose and evidence boundaries, not completion credit.
