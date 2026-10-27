# Model-building-first AI learning roadmap

**First-30 review candidate:** no learner days are recorded complete. Day 1 is
**27 October 2026 (Asia/Kolkata)**. The planned learning horizon is 548 days,
ending 26 April 2028, subject to evidence gates and recovery time.

Read the [active model-first roadmap](docs/model-first-roadmap/README.md), its
[manifest and first 30 contracts](docs/model-first-roadmap/manifest.json), and
the [read-only 305-issue migration proposal](docs/model-first-roadmap/legacy-issue-migration.json).
The exact release allowlist and exclusions are in
[`release-scope.md`](docs/model-first-roadmap/release-scope.md).
DSA remains in its separate repository and is not copied or changed here.

## Day 1

Run the CPU-only worked example and instructor checks:

```sh
python3 -m ai.model_building.day01_example
python3 -m unittest ai.model_building.tests.test_day01_infrastructure
```

Then attempt the learner-owned one-step logistic regression contract:

```sh
python3 -m unittest ai.model_building.learner_acceptance.test_day_01
```

That command is expected to fail at the explicit TODO until implemented. It
does not indicate a setup failure or learner completion. Day 1 makes no
provider call and needs only Python's standard library.

The first-30 release does not include the separate API-first candidate or its
provider integration code. Those files remain preserved on the broader review
branch for later consideration. No YouTrack, DSA repository, deployment, paid
service, credential, purchase or enrollment action is included in this draft.

## Release boundary

This candidate contains exactly 30 detailed learner days plus their model
starter, independent instructor witnesses, migration metadata, generators and
checks. The 548-day manifest keeps later work as outcome-based planning; it
does not publish daily assignments after Day 30. Existing main-branch FE/BE
application code is referenced for read-only comprehension only.
