# Learning day 08

**Status: Not started.** A planned learning-day number advances only when this session is attempted; missed dates do not silently complete a day.

## Fixed daily budget

- DSA: 60 minutes — use the corresponding current item in the separate DSA plan; no problem statements or solutions are copied here.
- Role / certification bank: 30 minutes — React async correctness.
- AI engineering: 90 minutes total, including any listed visibility work.
- Total: 180 minutes. If DSA takes 75/90 minutes, AI reduces to 75/60 minutes; carry unfinished AI work forward rather than compressing it.

## Role interview prompt (30 minutes)

An older React effect response overwrites a newer result. Explain the race and give two safe remedies; identify what the supplied test actually proves.

Output: Explain effect/dependency/cancellation or stale-state behavior using a small code path.

Score using the [common rubric](../role-bank.md). Do not claim an interview pass, real customer feedback, or a result that did not occur.

## AI engineering work (90 minutes)

Load the pinned real model runtime and attempt a non-canned response to three simple tasks (plain explanation, JSON-shaped request, impossible request). Capture actual output, elapsed time and any failure. If weights cannot be obtained, stop at the exact blocker and keep this evidence gate blocked.

### Scheduled dependency-unit segments

| Unit | Segment | Minutes | Contract |
|---|---|---:|---|
| AIFDE-V2-U006 — First real generated response | primary | 90 | [unit contract](../units/u006.md) |

### Learning references

- [FastAPI Tutorial](https://fastapi.tiangolo.com/tutorial/)
- [FastAPI Testing](https://fastapi.tiangolo.com/tutorial/testing/)

### Acceptance and evidence

Run only the acceptance selector named in this day's unit contract after its assigned segment is complete. Tests for future units are deliberately kept out of the daily command. Keep actual model/runtime traces separate from deterministic mocked tests.

Record: command, exit status, changed learner-owned files, relevant output, model/runtime identity where applicable, and unresolved blockers. A mock-only result, reference witness, scaffold output or model-generated artifact is not learner completion.
