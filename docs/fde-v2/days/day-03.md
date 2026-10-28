# Learning day 03

**Status: Not started.** A planned learning-day number advances only when this session is attempted; missed dates do not silently complete a day.

## Fixed daily budget

- DSA: 60 minutes — use the corresponding current item in the separate DSA plan; no problem statements or solutions are copied here.
- Role / certification bank: 30 minutes — React state and rendering.
- AI engineering: 90 minutes total, including any listed visibility work.
- Total: 180 minutes. If DSA takes 75/90 minutes, AI reduces to 75/60 minutes; carry unfinished AI work forward rather than compressing it.

## Role interview prompt (30 minutes)

A repeated list renders the wrong item after removing row two. Trace state identity and React keys; name one minimal fix and one test that would expose the defect.

Output: Diagnose a state/render/key bug and explain the minimal fix.

Score using the [common rubric](../role-bank.md). Do not claim an interview pass, real customer feedback, or a result that did not occur.

## AI engineering work (90 minutes)

Spend the full 60 minutes on U002: finish the reproducible environment and real-model feasibility record, using the second 30 minutes as its setup-repair buffer if needed. Then spend 30 minutes starting U003: inspect the frozen dataclass fields and write one valid-record and one malformed-record test. Do not implement API or model behavior from a future unit.

### Scheduled dependency-unit segments

| Unit | Segment | Minutes | Contract |
|---|---|---:|---|
| AIFDE-V2-U002 — Reproducible environment and real-model feasibility | primary | 30 | [unit contract](../units/u002.md) |
| AIFDE-V2-U002 — Reproducible environment and real-model feasibility | buffer | 30 | [unit contract](../units/u002.md) |
| AIFDE-V2-U003 — Python data contracts and tests | primary | 30 | [unit contract](../units/u003.md) |

### Learning references

- [Python Tutorial](https://docs.python.org/3/tutorial/)
- [Python json module](https://docs.python.org/3/library/json.html)
- [Python typing module](https://docs.python.org/3/library/typing.html)
- [Python unittest](https://docs.python.org/3/library/unittest.html)

### Acceptance and evidence

Run only the acceptance selector named in this day's unit contract after its assigned segment is complete. Tests for future units are deliberately kept out of the daily command. Keep actual model/runtime traces separate from deterministic mocked tests.

Record: command, exit status, changed learner-owned files, relevant output, model/runtime identity where applicable, and unresolved blockers. A mock-only result, reference witness, scaffold output or model-generated artifact is not learner completion.
