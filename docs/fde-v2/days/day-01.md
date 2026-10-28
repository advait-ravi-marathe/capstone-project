# Learning day 01

**Status: Not started.** A planned learning-day number advances only when this session is attempted; missed dates do not silently complete a day.

## Fixed daily budget

- DSA: 60 minutes — use the corresponding current item in the separate DSA plan; no problem statements or solutions are copied here.
- Role / certification bank: 30 minutes — Target-role and baseline map.
- AI engineering: 90 minutes total, including any listed visibility work.
- Total: 180 minutes. If DSA takes 75/90 minutes, AI reduces to 75/60 minutes; carry unfinished AI work forward rather than compressing it.

## Role interview prompt (30 minutes)

Review one target FDE / applied-AI SWE role. Which three requirements have concrete evidence in your existing work, which three remain gaps, and what can you apply for now without waiting for this roadmap?

Output: Use verified current FE/full-stack strengths and an actual target role; identify gaps and accurate CV evidence. Applications need not wait for AI work.

Score using the [common rubric](../role-bank.md). Do not claim an interview pass, real customer feedback, or a result that did not occur.

## AI engineering work (90 minutes)

Read the starter boundary and inspect the provided request/data path. Write a one-page problem brief: one synthetic user, one question workflow, a non-AI baseline (find matching text), one measurable objective and explicit non-goals. Make a small standard-library JSON/SQLite attempt with the worked example; label all outputs as instructor scaffold.

### Scheduled dependency-unit segments

| Unit | Segment | Minutes | Contract |
|---|---|---:|---|
| AIFDE-V2-U001 — Baseline, scope and ownership | primary | 90 | [unit contract](../units/u001.md) |

### Learning references

- [Python Tutorial](https://docs.python.org/3/tutorial/)
- [Python json module](https://docs.python.org/3/library/json.html)

### Acceptance and evidence

Run only the acceptance selector named in this day's unit contract after its assigned segment is complete. Tests for future units are deliberately kept out of the daily command. Keep actual model/runtime traces separate from deterministic mocked tests.

Record: command, exit status, changed learner-owned files, relevant output, model/runtime identity where applicable, and unresolved blockers. A mock-only result, reference witness, scaffold output or model-generated artifact is not learner completion.
