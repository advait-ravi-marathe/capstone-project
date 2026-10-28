# Learning day 11

**Status: Not started.** A planned learning-day number advances only when this session is attempted; missed dates do not silently complete a day.

## Fixed daily budget

- DSA: 60 minutes — use the corresponding current item in the separate DSA plan; no problem statements or solutions are copied here.
- Role / certification bank: 30 minutes — Backend API contract.
- AI engineering: 90 minutes total, including any listed visibility work.
- Total: 180 minutes. If DSA takes 75/90 minutes, AI reduces to 75/60 minutes; carry unfinished AI work forward rather than compressing it.

## Role interview prompt (30 minutes)

Design a POST /generate request contract. Name validation boundaries, status/error behavior, request identity, timeout and whether an automatic retry could duplicate work.

Output: Design validation, status/error contract, auth boundary and idempotency for one endpoint.

Score using the [common rubric](../role-bank.md). Do not claim an interview pass, real customer feedback, or a result that did not occur.

## AI engineering work (90 minutes)

Complete the assigned AI adapter seam and inspect the assistant-supplied /generate integration. Trace the React pending, empty, error, cancellation and result states from the supplied code and identify one stale-response race; no UI/controller edits are assigned.

### Scheduled dependency-unit segments

| Unit | Segment | Minutes | Contract |
|---|---|---:|---|
| AIFDE-V2-U008 — Real generation behind the API | primary | 30 | [unit contract](../units/u008.md) |
| AIFDE-V2-U008 — Real generation behind the API | buffer | 30 | [unit contract](../units/u008.md) |
| AIFDE-V2-U009 — Thin React workflow | primary | 30 | [unit contract](../units/u009.md) |

### Learning references

- [React: Choosing the State Structure](https://react.dev/learn/choosing-the-state-structure)
- [React: You Might Not Need an Effect](https://react.dev/learn/you-might-not-need-an-effect)
- [FastAPI Testing](https://fastapi.tiangolo.com/tutorial/testing/)

### Acceptance and evidence

Run only the acceptance selector named in this day's unit contract after its assigned segment is complete. Tests for future units are deliberately kept out of the daily command. Keep actual model/runtime traces separate from deterministic mocked tests.

Record: command, exit status, changed learner-owned files, relevant output, model/runtime identity where applicable, and unresolved blockers. A mock-only result, reference witness, scaffold output or model-generated artifact is not learner completion.
