# Capstone roadmap

The active roadmap is the [AWS FDE v2.4 interview-first plan](docs/fde-v2/README.md). It starts at the first actual study session; all learner days remain Not started. Main contains Sprint 1 daily pages for Days 1–14. The separate unpublished `work/aws-fde-v24-continuation` worktree adds authored guidance through Day 28; those pages are not yet part of main or a released starter.

Start with the [Day 1 guide](docs/fde-v2/days/day-01.md) and run the CPU-only synthetic lexical baseline:

```sh
python -m ai.fde_v2.coverstory.day01_baseline
```

This is an instructor-owned keyword baseline, not generated output or an AI/RAG pass. See [runtime status](docs/fde-v2/runtime-verification.md) for the recorded HTTP 403 proxy blocker. Real local generation remains unverified; no fetch retry was made.

The earlier [model-building-first roadmap](docs/model-first-roadmap/README.md) and prior assignment histories are preserved as superseded/deferred records. DSA remains separate; no DSA problems or solutions are copied here. No learner completion, customer adoption, production operation, certification pass or cloud deployment is claimed.

## Existing repository-wide browser tests

The inherited Java CI also runs its existing Playwright browser harnesses. To
reproduce those full-suite checks locally, install the locked frontend
dependencies with `npm ci` and install Chromium. If Chromium is available at
the system path, run the Maven suite with
`CHROMIUM_BIN=/usr/bin/chromium ./mvnw test`. These commands support existing
historical instructor tests; they are not new learner tasks.
