# AI-first learning roadmap

**Active local review draft:** the learner has completed zero days. Day 1 is
planned for **27 October 2026 (Asia/Kolkata)**. No learner status is inferred
from the age of the repository or existing test scaffolds.

The active sequence is [the 365-day AI-first roadmap](docs/ai-first-roadmap/README.md).
Its detailed first 30 contracts are generated from
[`manifest.json`](docs/ai-first-roadmap/manifest.json); the existing 305 EAI
Story IDs are accounted for in the [read-only migration proposal](docs/ai-first-roadmap/legacy-issue-migration.json).
No YouTrack or DSA repository writes are part of this draft.

## Day 1 now

Run the short Python/JSON worked example:

```sh
python3 -m ai.coverstory_assistant.day01_example
python3 -m unittest ai.coverstory_assistant.tests.test_day01_infrastructure ai.coverstory_assistant.tests.test_day01_oracle
```

Then implement `build_policy_prompt` in
[`learner.py`](ai/coverstory_assistant/learner.py) and run its learner test:

```sh
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_01
```

The learner command is expected to fail at the explicit TODO until implemented.
This is not a setup failure and does not record completion. Day 1 uses one
fixed synthetic excerpt and makes no model call.

## Provider boundary

The starter includes a real, configurable OpenAI Responses API client and
hermetic loopback protocol tests. Infrastructure tests do not call a provider.
A live call requires externally supplied configuration and an explicit
`--confirm-provider-usage` flag; no such call, credential creation, cloud
deployment, purchase, or enrollment is authorized or performed here. A local
HTTP fixture or mock is not live-model evidence.

## Historical material

The old quote-selection/FE/BE curriculum, its contracts, manifests, receipts,
and issue definitions are preserved as historical artifacts. They do not
represent the active sequence. The detailed legacy entrypoint is
[marked historical here](docs/reboot/README.md); the pre-migration root README
is retained at
[`docs/ai-first-roadmap/archive/root-README-8d27f57.md`](docs/ai-first-roadmap/archive/root-README-8d27f57.md).

FE/BE learning in the new sequence means reading supplied application code,
tracing requests, diagnosing seeded failures, and answering interview
questions. The assistant supplies React/Spring plumbing. The learner writes
DSA in the separate `dsa-prep` repository and AI components in this repository.
System-design practice remains in the separate question bank.
