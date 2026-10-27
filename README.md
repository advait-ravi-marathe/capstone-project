# Model-building-first AI learning roadmap

**Active local review draft:** no learner days are recorded complete. Day 1 is
**27 October 2026 (Asia/Kolkata)**. The planned learning horizon is 548 days,
ending 26 April 2028, subject to evidence gates and recovery time.

Read the [active model-first roadmap](docs/model-first-roadmap/README.md), its
[manifest and first 30 contracts](docs/model-first-roadmap/manifest.json), and
the [read-only 305-issue migration proposal](docs/model-first-roadmap/legacy-issue-migration.json).
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

The earlier API-first roadmap candidate, code and experiments remain preserved
for possible later integration under
[`docs/ai-first-roadmap`](docs/ai-first-roadmap/README.md). It is not the active
sequence. No YouTrack, DSA repository, deployment, paid service, credential,
purchase or enrollment action is included in this draft.
