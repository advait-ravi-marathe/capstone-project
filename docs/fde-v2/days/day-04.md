# Learning day 04

**Status: Not started.** A planned learning-day number advances only when this session is attempted; missed dates do not silently complete a day.

## Fixed daily budget

- DSA: 60 minutes — use the corresponding current item in the separate DSA plan; no problem statements or solutions are copied here.
- Role / certification bank: 30 minutes — Browser and network behavior.
- AI engineering: 90 minutes total, including any listed visibility work.
- Total: 180 minutes. If DSA takes 75/90 minutes, AI reduces to 75/60 minutes; carry unfinished AI work forward rather than compressing it.

## Role interview prompt (30 minutes)

A browser retries a slow request while a cached response is stale. Trace request, cache and error states; what evidence distinguishes a server delay from a client race?

Output: Trace an HTTP request, caching/error behavior and one browser performance issue.

Score using the [common rubric](../role-bank.md). Do not claim an interview pass, real customer feedback, or a result that did not occur.

## AI engineering work (90 minutes)

Finish the document-record seam with exact field/type/empty-input checks and run its named acceptance test. Inspect the supplied loopback-only API request path and trace health, document metadata, request ID and structured error behavior.

### Scheduled dependency-unit segments

| Unit | Segment | Minutes | Contract |
|---|---|---:|---|
| AIFDE-V2-U003 — Python data contracts and tests | primary | 60 | [unit contract](../units/u003.md) |
| AIFDE-V2-U003 — Python data contracts and tests | support | 30 | [unit contract](../units/u003.md) |

### Learning references

- [FastAPI Tutorial](https://fastapi.tiangolo.com/tutorial/)
- [FastAPI Testing](https://fastapi.tiangolo.com/tutorial/testing/)

### Acceptance and evidence

Run only the acceptance selector named in this day's unit contract after its assigned segment is complete. Tests for future units are deliberately kept out of the daily command. Keep actual model/runtime traces separate from deterministic mocked tests.

Record: command, exit status, changed learner-owned files, relevant output, model/runtime identity where applicable, and unresolved blockers. A mock-only result, reference witness, scaffold output or model-generated artifact is not learner completion.
