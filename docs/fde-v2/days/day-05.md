# Learning day 05

**Status: Not started.** A planned learning-day number advances only when this session is attempted; missed dates do not silently complete a day.

## Fixed daily budget

- DSA: 60 minutes — use the corresponding current item in the separate DSA plan; no problem statements or solutions are copied here.
- Role / certification bank: 30 minutes — AWS certification: course and syllabus audit.
- AI engineering: 90 minutes total, including any listed visibility work.
- Total: 180 minutes. If DSA takes 75/90 minutes, AI reduces to 75/60 minutes; carry unfinished AI work forward rather than compressing it.

## Role interview prompt (30 minutes)

State the exact AWS course you already own, if known. Map one lesson to a current SAA-C03 domain; if the course identity is unknown, say so and use the official exam guide.

Output: Verify the purchased course exact title/code/update if accessible; map it to the current official SAA-C03 guide. If unknown, record unknown and use the official guide; no purchase or completion claim.

Score using the [common rubric](../role-bank.md). Do not claim an interview pass, real customer feedback, or a result that did not occur.

## AWS study (this is today's 30-minute bank, not additional time)

**Focus:** Course identity and official scope
**Current source:** [AWS Certified Solutions Architect – Associate (SAA-C03) official exam guide](https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03.html)
**Exact section:** Exam guide: Content outline; Scope; revisions
Use the official domain/task statement and one original synthetic scenario. No account setup, AWS deployment, course purchase or exam booking is authorized. If an already-owned course cannot be identified/accessed, record that unknown and use the official guide.

## AI engineering work (90 minutes)

Trace the supplied /health and /documents routes and run their deterministic contracts. Inspect loopback binding, fixture-root checks, request IDs and logging. The app plumbing is assistant-owned; record one failure-path diagnosis and do not edit route/controller code.

### Scheduled dependency-unit segments

| Unit | Segment | Minutes | Contract |
|---|---|---:|---|
| AIFDE-V2-U004 — Small API with observable failure paths | primary | 90 | [unit contract](../units/u004.md) |

### Learning references

- [Transformers pipeline tutorial](https://huggingface.co/docs/transformers/en/pipeline_tutorial)
- [Qwen2.5-0.5B-Instruct model card](https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct)

### Acceptance and evidence

Run only the acceptance selector named in this day's unit contract after its assigned segment is complete. Tests for future units are deliberately kept out of the daily command. Keep actual model/runtime traces separate from deterministic mocked tests.

Record: command, exit status, changed learner-owned files, relevant output, model/runtime identity where applicable, and unresolved blockers. A mock-only result, reference witness, scaffold output or model-generated artifact is not learner completion.
