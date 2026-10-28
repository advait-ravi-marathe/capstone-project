# Learning day 02

**Status: Not started.** A planned learning-day number advances only when this session is attempted; missed dates do not silently complete a day.

## Fixed daily budget

- DSA: 60 minutes — use the corresponding current item in the separate DSA plan; no problem statements or solutions are copied here.
- Role / certification bank: 30 minutes — JavaScript execution and async.
- AI engineering: 90 minutes total, including any listed visibility work.
- Total: 180 minutes. If DSA takes 75/90 minutes, AI reduces to 75/60 minutes; carry unfinished AI work forward rather than compressing it.

## Role interview prompt (30 minutes)

Explain one closure and one promise/event-loop trace from the supplied frontend. When can a callback observe a changed variable, and what ordering is guaranteed?

Output: Explain closures/event loop/promises with one short runnable example; record a weak point.

Score using the [common rubric](../role-bank.md). Do not claim an interview pass, real customer feedback, or a result that did not occur.

## AI engineering work (90 minutes)

Create an isolated Python environment and run the deterministic tests. Record CPU, RAM, free disk and operating system. Check the pinned generation-model card, revision, license and documented size; explain the separate jobs of generation, embeddings and retrieval. Record the known proxy 403. Do not rerun the model fetch or seek another endpoint while access remains blocked.

### Scheduled dependency-unit segments

| Unit | Segment | Minutes | Contract |
|---|---|---:|---|
| AIFDE-V2-U001 — Baseline, scope and ownership | support | 30 | [unit contract](../units/u001.md) |
| AIFDE-V2-U002 — Reproducible environment and real-model feasibility | primary | 60 | [unit contract](../units/u002.md) |

### Learning references

- [Python Tutorial](https://docs.python.org/3/tutorial/)
- [Transformers pipeline tutorial](https://huggingface.co/docs/transformers/en/pipeline_tutorial)
- [Qwen2.5-0.5B-Instruct model card](https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct)
- [all-MiniLM-L6-v2 model card](https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2)

### Acceptance and evidence

Run only the acceptance selector named in this day's unit contract after its assigned segment is complete. Tests for future units are deliberately kept out of the daily command. Keep actual model/runtime traces separate from deterministic mocked tests.

Record: command, exit status, changed learner-owned files, relevant output, model/runtime identity where applicable, and unresolved blockers. A mock-only result, reference witness, scaffold output or model-generated artifact is not learner completion.
