# Learning day 12

**Status: Not started.** A planned learning-day number advances only when this session is attempted; missed dates do not silently complete a day.

## Fixed daily budget

- DSA: 60 minutes — use the corresponding current item in the separate DSA plan; no problem statements or solutions are copied here.
- Role / certification bank: 30 minutes — AWS certification: networking diagnostic.
- AI engineering: 90 minutes total, including any listed visibility work.
- Total: 180 minutes. If DSA takes 75/90 minutes, AI reduces to 75/60 minutes; carry unfinished AI work forward rather than compressing it.

## Role interview prompt (30 minutes)

A private service needs access to a data store in one VPC. Trace subnet, route table and security-group boundaries; identify a misconfiguration that blocks egress without exposing the database publicly.

Output: Explain VPC/subnet/routing/security-group boundaries for the app; classify uncertainty and review the relevant official syllabus.

Score using the [common rubric](../role-bank.md). Do not claim an interview pass, real customer feedback, or a result that did not occur.

## AWS study (this is today's 30-minute bank, not additional time)

**Focus:** Networking and scalable architecture
**Current source:** [AWS Certified Solutions Architect – Associate (SAA-C03) official exam guide](https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03-domain1.html)
**Exact section:** Content Domain 1; Task 1.2: Design secure workloads and applications; Domain 2, Task 2.1: Design scalable and loosely coupled architectures
Use the official domain/task statement and one original synthetic scenario. No account setup, AWS deployment, course purchase or exam booking is authorized. If an already-owned course cannot be identified/accessed, record that unknown and use the official guide.

**Also review:** [Content Domain 2: Design Resilient Architectures — Task 2.1: Design scalable and loosely coupled architectures](https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03-domain2.html)
## AI engineering work (90 minutes)

Inspect the supplied UI's keyboard-accessible states and run its existing focused tests. Explain one pending→result and one pending→error transition. UI implementation and UI test scaffolding are supplied; do not build a design system or add frontend code.

### Scheduled dependency-unit segments

| Unit | Segment | Minutes | Contract |
|---|---|---:|---|
| AIFDE-V2-U009 — Thin React workflow | primary | 60 | [unit contract](../units/u009.md) |
| AIFDE-V2-U009 — Thin React workflow | buffer | 30 | [unit contract](../units/u009.md) |

### Learning references

- [React: Choosing the State Structure](https://react.dev/learn/choosing-the-state-structure)
- [Vitest Guide](https://vitest.dev/guide/)

### Acceptance and evidence

Run only the acceptance selector named in this day's unit contract after its assigned segment is complete. Tests for future units are deliberately kept out of the daily command. Keep actual model/runtime traces separate from deterministic mocked tests.

Record: command, exit status, changed learner-owned files, relevant output, model/runtime identity where applicable, and unresolved blockers. A mock-only result, reference witness, scaffold output or model-generated artifact is not learner completion.
