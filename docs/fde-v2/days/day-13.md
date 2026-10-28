# Learning day 13

**Status: Not started.** A planned learning-day number advances only when this session is attempted; missed dates do not silently complete a day.

## Fixed daily budget

- DSA: 60 minutes — use the corresponding current item in the separate DSA plan; no problem statements or solutions are copied here.
- Role / certification bank: 30 minutes — AWS certification: storage/data diagnostic.
- AI engineering: 90 minutes total, including any listed visibility work.
- Total: 180 minutes. If DSA takes 75/90 minutes, AI reduces to 75/60 minutes; carry unfinished AI work forward rather than compressing it.

## Role interview prompt (30 minutes)

Choose storage for synthetic source documents and derived vectors. Explain encryption, backup, retention and deletion consequences; state which details cannot be inferred without a recovery objective.

Output: Choose storage, encryption, backup and data-access controls for one scenario; explain alternatives and costs conceptually, not as measured billing.

Score using the [common rubric](../role-bank.md). Do not claim an interview pass, real customer feedback, or a result that did not occur.

## AWS study (this is today's 30-minute bank, not additional time)

**Focus:** Data security and storage choices
**Current source:** [AWS Certified Solutions Architect – Associate (SAA-C03) official exam guide](https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03-domain1.html)
**Exact section:** Content Domain 1; Task 1.3: Determine appropriate data security controls
**Verified course titles and timebox:** See the [Sprint 1 course map](../sources/sprint1-course-map.md); use the listed free official fallback if paid-video access is unavailable.
Use the official domain/task statement and one original synthetic scenario. No account setup, AWS deployment, course purchase or exam booking is authorized. If an already-owned course cannot be identified/accessed, record that unknown and use the official guide.

## AI engineering work (90 minutes)

Define the exact answer JSON schema (answer string, citations array, abstained boolean). Implement strict parsing: reject malformed JSON, missing/extra keys, wrong types and duplicate citations. Validate against actual generation output when available; a valid schema is not evidence of factual correctness.

### Scheduled dependency-unit segments

| Unit | Segment | Minutes | Contract |
|---|---|---:|---|
| AIFDE-V2-U010 — Structured output without trusting it | primary | 90 | [unit contract](../units/u010.md) |

### Learning references

- [Python json module](https://docs.python.org/3/library/json.html)
- [Python unittest](https://docs.python.org/3/library/unittest.html)

### Acceptance and evidence

Run only the acceptance selector named in this day's unit contract after its assigned segment is complete. Tests for future units are deliberately kept out of the daily command. Keep actual model/runtime traces separate from deterministic mocked tests.

Record: command, exit status, changed learner-owned files, relevant output, model/runtime identity where applicable, and unresolved blockers. A mock-only result, reference witness, scaffold output or model-generated artifact is not learner completion.
