# Original first-30 release boundary

The published first-30 release was based on official `main` commit
`8d27f57417f5bddb2a9008d2094f7bd0e79cc337` and contains the model-building
sequence for Days 1–30. The current ownership-amendment review branch starts
from the published first-30 main and adds a separate, proposed integration
slice; it does not alter the first-30 daily contracts or include the earlier
API-first candidate. The 548-day horizon remains an outcome-based planning
map; only Days 1–30 are detailed daily assignments.

## Included

- `ai/model_building/`: the learner-owned small-model seams, fixed synthetic
  examples, acceptance tests and instructor-only independent witnesses needed
  for Days 1–30, plus an intentionally unfinished model-serving adapter and
  assistant-owned local HTTP scaffold for the ownership-amendment review.
- `frontend/src/local/ModelPredictionPanel.tsx` and its gated contract tests:
  a supplied UI shell for the proposed later UI/API exercise. Its learner TODO
  remains red; the main branch does not yet contain this amendment.
- `docs/model-first-roadmap/`: the 30 generated day contracts, source manifest,
  first-party role benchmark snapshot, 305-record historical issue mapping,
  learner guide, and this release boundary. The phase map after Day 30 is
  planning-only, not a set of future assignments.
- `scripts/generate_model_first_roadmap.py` and
  `scripts/generate_model_first_migration.py`: deterministic generators for the
  detailed contracts and migration proposal.
- `scripts/test_model_first_roadmap.py` and the model witness tests: daily
  budget, source/link, acceptance, migration and scope regressions.
- `requirements-model-first.txt`: pinned CPU-only NumPy/scikit-learn packages
  used only from the later first-month exercises.
- The root README, existing roadmap status/alignment pointers, the calendar
  date regression and `.github/workflows/coverstory-starter.yml` changes expose
  the active first-30 candidate and run its checks.

## Excluded

This amendment is not a new daily curriculum release. It does not include
`ai/coverstory_assistant/`, the earlier API-first contracts in
`docs/ai-first-roadmap/`,
`frontend/src/ai/AiFirstCoverStoryApp.tsx`,
`src/main/java/assignments/local/LocalAiAssistantController.java`, related
provider/server code, or the API-first candidate's integration tests. That old
candidate is preserved on the broader review branch for possible later work.
No FE/BE implementation is assigned in the published first-30 daily cards;
Days 26–28 still refer only to existing main-branch source for read-only
comprehension and use the learner's model exercises for AI work. The separate
review amendment proposes a few later small integration changes, described in
`integration-ownership-amendment.md`.

The original first-30 mapping reuses historical EAI-211–240 because those are the
30 existing issue IDs selected in the reviewed migration proposal; native
issue IDs are preserved as provenance and are not interpreted as day numbers.
The remaining 275 old records remain mapped as superseded, not deleted or
completed. This file is a repository release boundary, not a board update or
learner-completion receipt.
