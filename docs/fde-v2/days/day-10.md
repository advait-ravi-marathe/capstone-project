# Learning day 10

**Status: Not started.** A planned learning-day number advances only when this session is attempted; missed dates do not silently complete a day.

## Fixed daily budget

- DSA: 60 minutes — use the corresponding current item in the separate DSA plan; no problem statements or solutions are copied here.
- Role / certification bank: 30 minutes — Frontend design.
- AI engineering: 90 minutes total, including any listed visibility work.
- Total: 180 minutes. If DSA takes 75/90 minutes, AI reduces to 75/60 minutes; carry unfinished AI work forward rather than compressing it.

## Role interview prompt (30 minutes)

Scope the smallest useful policy-answer screen. Which state belongs in the browser, what may be cached, and how do you prevent a result for the prior question from appearing as current?

Output: Scope a workflow; choose state/data caching boundaries and explain tradeoffs.

Score using the [common rubric](../role-bank.md). Do not claim an interview pass, real customer feedback, or a result that did not occur.

## AI engineering work (90 minutes)

Begin the /generate integration seam: implement the bounded learner adapter call and test success, invalid request, timeout and unavailable runtime. Keep HTTP request parsing errors distinct from internal/model failures. The service scaffolding is provided; edit only the assigned seam and tests.

### Scheduled dependency-unit segments

| Unit | Segment | Minutes | Contract |
|---|---|---:|---|
| AIFDE-V2-U007 — Reality check and first evidence note | primary | 30 | [unit contract](../units/u007.md) |
| AIFDE-V2-U008 — Real generation behind the API | primary | 60 | [unit contract](../units/u008.md) |

### Learning references

- [Python json module](https://docs.python.org/3/library/json.html)

### Acceptance and evidence

Run only the acceptance selector named in this day's unit contract after its assigned segment is complete. Tests for future units are deliberately kept out of the daily command. Keep actual model/runtime traces separate from deterministic mocked tests.

Record: command, exit status, changed learner-owned files, relevant output, model/runtime identity where applicable, and unresolved blockers. A mock-only result, reference witness, scaffold output or model-generated artifact is not learner completion.
