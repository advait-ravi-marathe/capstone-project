# Learning day 07

**Status: Not started.** A planned learning-day number advances only when this session is attempted; missed dates do not silently complete a day.

## Fixed daily budget

- DSA: 60 minutes — use the corresponding current item in the separate DSA plan; no problem statements or solutions are copied here.
- Role / certification bank: 30 minutes — Real-work story + short interview rehearsal.
- AI engineering: 90 minutes total, including any listed visibility work.
- Total: 180 minutes. If DSA takes 75/90 minutes, AI reduces to 75/60 minutes; carry unfinished AI work forward rather than compressing it.

## Role interview prompt (30 minutes)

Tell one true work story about diagnosing a difficult failure. Separate your own action from the team's, state observable evidence and outcome, then answer: what would you do differently?

Output: Practice one genuine incident/ownership story plus follow-up questions; distinguish own contribution and actual results. Re-estimate AI scope inside the weekly evidence note.

Score using the [common rubric](../role-bank.md). Do not claim an interview pass, real customer feedback, or a result that did not occur.

## AI engineering work (90 minutes)

Inspect the supplied generation adapter and its explicit no-artifact failure path. No real generation trace exists because the model download was blocked; document the exact recorded blocker without producing a mock as evidence. Use this week's 30-minute visibility slot to write a concise, factual profile/evidence note; visibility is inside AI's 90 minutes, not extra time.

### Scheduled dependency-unit segments

| Unit | Segment | Minutes | Contract |
|---|---|---:|---|
| AIFDE-V2-U005 — Understand inference and install a real runtime | primary | 30 | [unit contract](../units/u005.md) |
| AIFDE-V2-U005 — Understand inference and install a real runtime | support | 30 | [unit contract](../units/u005.md) |

**Visibility/re-estimation:** 30 minutes included in the AI total. Use the output described in the accepted contract; do not add another time block.

### Learning references

- [Qwen2.5-0.5B-Instruct model card](https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct)

### Acceptance and evidence

Run only the acceptance selector named in this day's unit contract after its assigned segment is complete. Tests for future units are deliberately kept out of the daily command. Keep actual model/runtime traces separate from deterministic mocked tests.

Record: command, exit status, changed learner-owned files, relevant output, model/runtime identity where applicable, and unresolved blockers. A mock-only result, reference witness, scaffold output or model-generated artifact is not learner completion.
