# Learning day 09

**Status: Not started.** A planned learning-day number advances only when this session is attempted; missed dates do not silently complete a day.

## Fixed daily budget

- DSA: 60 minutes — use the corresponding current item in the separate DSA plan; no problem statements or solutions are copied here.
- Role / certification bank: 30 minutes — Accessible components and testing.
- AI engineering: 90 minutes total, including any listed visibility work.
- Total: 180 minutes. If DSA takes 75/90 minutes, AI reduces to 75/60 minutes; carry unfinished AI work forward rather than compressing it.

## Role interview prompt (30 minutes)

For a question-answer panel, describe keyboard-accessible loading, empty, success and error states. What should a screen reader announce when a request fails?

Output: Design/test loading/error/keyboard behavior for one component.

Score using the [common rubric](../role-bank.md). Do not claim an interview pass, real customer feedback, or a result that did not occur.

## AI engineering work (90 minutes)

Finish the first real-response record, including machine, model revision, actual latency and output-token counts if exposed. Run the five-prompt smoke set with an impossible question. Trace the supplied React request/response path; do not edit UI code. Separate plausible text from verified facts; no mocked output in this evidence file.

### Scheduled dependency-unit segments

| Unit | Segment | Minutes | Contract |
|---|---|---:|---|
| AIFDE-V2-U006 — First real generated response | buffer | 30 | [unit contract](../units/u006.md) |
| AIFDE-V2-U007 — Reality check and first evidence note | primary | 60 | [unit contract](../units/u007.md) |

### Learning references

- [React: You Might Not Need an Effect](https://react.dev/learn/you-might-not-need-an-effect)
- [React: Choosing the State Structure](https://react.dev/learn/choosing-the-state-structure)
- [Vitest Guide](https://vitest.dev/guide/)

### Acceptance and evidence

Run only the acceptance selector named in this day's unit contract after its assigned segment is complete. Tests for future units are deliberately kept out of the daily command. Keep actual model/runtime traces separate from deterministic mocked tests.

Record: command, exit status, changed learner-owned files, relevant output, model/runtime identity where applicable, and unresolved blockers. A mock-only result, reference witness, scaffold output or model-generated artifact is not learner completion.
