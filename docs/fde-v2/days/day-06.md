# Learning day 06

**Status: Not started.** A planned learning-day number advances only when this session is attempted; missed dates do not silently complete a day.

## Fixed daily budget

- DSA: 60 minutes — use the corresponding current item in the separate DSA plan; no problem statements or solutions are copied here.
- Role / certification bank: 30 minutes — AWS certification: IAM diagnostic.
- AI engineering: 90 minutes total, including any listed visibility work.
- Total: 180 minutes. If DSA takes 75/90 minutes, AI reduces to 75/60 minutes; carry unfinished AI work forward rather than compressing it.

## Role interview prompt (30 minutes)

For a synthetic document-ingestion job, who or what should be the principal? Explain the least-privilege actions, resource scope and one way to avoid long-lived credentials.

Output: Explain principals, policies, roles and least privilege through one short design scenario; record actual gaps, without provisioning resources.

Score using the [common rubric](../role-bank.md). Do not claim an interview pass, real customer feedback, or a result that did not occur.

## AWS study (this is today's 30-minute bank, not additional time)

**Focus:** Design secure access
**Current source:** [AWS Certified Solutions Architect – Associate (SAA-C03) official exam guide](https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03-domain1.html)
**Exact section:** Content Domain 1; Task 1.1: Design secure access to AWS resources
Use the official domain/task statement and one original synthetic scenario. No account setup, AWS deployment, course purchase or exam booking is authorized. If an already-owned course cannot be identified/accessed, record that unknown and use the official guide.

## AI engineering work (90 minutes)

Spend 30 minutes closing the API failure-path gap, then study tokenizer/context/output limits and the local runtime setup. Run the explicit dependency/model-feasibility check; do not silently substitute a mock if model installation is blocked.

### Scheduled dependency-unit segments

| Unit | Segment | Minutes | Contract |
|---|---|---:|---|
| AIFDE-V2-U004 — Small API with observable failure paths | buffer | 30 | [unit contract](../units/u004.md) |
| AIFDE-V2-U005 — Understand inference and install a real runtime | primary | 60 | [unit contract](../units/u005.md) |

### Learning references

- [Qwen2.5-0.5B-Instruct model card](https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct)

### Acceptance and evidence

Run only the acceptance selector named in this day's unit contract after its assigned segment is complete. Tests for future units are deliberately kept out of the daily command. Keep actual model/runtime traces separate from deterministic mocked tests.

Record: command, exit status, changed learner-owned files, relevant output, model/runtime identity where applicable, and unresolved blockers. A mock-only result, reference witness, scaffold output or model-generated artifact is not learner completion.
