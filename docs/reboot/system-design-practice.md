# Interview-first system design practice

This is an interview practice path attached to the existing CoverStory roadmap.
It does not add a product, a cloud deployment, or extra daily hours. The first
14-day sprint starts **24 October 2026**. The daily contract and its existing
course lesson remain authoritative for the engineering task.

## Start with the learner's design

Before hints, reference answers, worked solutions, or AI answers:

1. Write the requirement and two questions that would change the design.
2. Sketch the smallest useful boundary and data flow.
3. State one invariant, one likely failure, and the first test you would run.
4. Save that first proposal with the timestamp and assistance state.

Afterward, compare it with docs or AI if useful. Keep the first proposal and
the comparison separate. Do not use a generic post-hoc design explanation as a
daily evidence task. Daily evidence remains the existing exact command, actual
result, and any blocker. A dedicated mock uses the rubric in
[`interview/mock-rubric.md`](interview/mock-rubric.md).

## Cadence and time

Run two 45-minute interview sessions each week; a third is optional only when
it replaces other practice time and the week's acceptance work still fits.
On a session day, the 45 minutes come from the existing 85-minute practice
block, leaving 40 minutes for the day's engineering task. The 25-minute
course/concept block and 10-minute evidence block stay unchanged. Carry
unfinished acceptance work forward; do not claim a milestone passed or add
hours to fit a mock.

Sprint 1 schedules two sessions in each seven-day week:

| Sprint day | Date | Focus | Practice prompt |
|---:|---|---|---|
| 3 | 2026-10-26 | HTTP errors and boundary ownership | Q08 |
| 7 | 2026-10-30 | time and deterministic quote expiry | Q03 |
| 10 | 2026-11-02 | transaction scope and atomic writes | Q13 |
| 14 | 2026-11-06 | end-to-end backend slice and first failure | Q31 |

The session sequence and remaining question prompts are in the
[`36-question bank`](interview/question-bank.md),
[`12-session deep-practice plan`](interview/deep-practice.md), and
[`pattern library`](interview/pattern-library.md). They are curated exercises,
not verbatim employer questions.

## Interview-first, not a replacement for the engineering contract

CoverStory remains the primary application. Use Java 21, Spring, PostgreSQL,
and the existing React path to test a design claim with a small experiment.
Keep StreamForge gated by its existing prerequisites. Do not introduce AI
features before the existing foundations and gates are met. Later AI practice
uses Python where appropriate and remains a design prompt until authorized by
the roadmap.

Keep the existing exact course titles, lesson titles, durations, access labels,
and 25-minute caps in `course-learning-blocks.json`. No new course lecture is
required by this interview plan. The focused experiment uses the repository's
existing Java/Spring/PostgreSQL dependencies; run it with:

```sh
./mvnw -Dtest=Sprint1DesignExperimentsTest test
```

Before running it, record a design proposal using
[`interview/design-proposal-template.md`](interview/design-proposal-template.md).
The runnable test is an observation tool, not a learner-completion result.
It covers owner-scoped reads, a unique idempotency key, and rollback when a
multi-row write fails. It creates only a disposable embedded PostgreSQL test
instance.

## Existing templates

Use the existing [ADR](templates/adr.md), [diagram](templates/diagram-prompt.md),
and [design note](templates/design-note.md) templates only when the prompt
needs them. A question can be answered with a short sketch; no diagram or ADR
is mandatory when it does not help the decision.
