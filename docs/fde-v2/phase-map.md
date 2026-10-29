# AWS FDE v2.4 phase map

This is a capacity plan, not a completion calendar. Day 1 is the first actual learning session. The current main branch supplies daily guidance for Days 1–270, with scaffold code and tests for the accepted milestones. Authored content does not advance learner state. A missed date does not advance learner state; conditional milestone prerequisites and external evidence gates remain in force.

## Capacity

Default: 60 minutes DSA + 30 minutes role interview/AWS preparation + 90 minutes AI work = 180 minutes. When a DSA revision takes 75 or 90 minutes, AI work falls to 75 or 60 minutes. Interview-near blocks also replace existing time. Carry work forward; never compress acceptance criteria or add hours. Visibility work is inside AI time. Certification and vendor study stay inside existing blocks.

First 30 actual days contain 30 DSA hours, 11 role-interview hours, 4 AWS study hours, 43 hours of AI unit work and 2 hours of visibility, totaling 90 hours. The first 30-day schedule only partially completes the 30 dependency units; the full evaluated local prototype is estimated around actual Day 41.

## Outcomes and gates

| Window (approximate learning days) | Planned outcome and evidence gate | Status |
|---|---|---|
| P1, approximately Days 1–41 | CoverStory local generative RAG: real pretrained generation, real embeddings, persisted vector store, source provenance, lexical baseline, abstention and dev/held-out evaluation. | Starter scaffolds cover Days 1–41. Real embedding/generation and learner evaluation remain unverified; authored code and synthetic tests are not completion evidence. |
| P2, roughly Days 42–102 | Ingestion/version/deletion controls; permission-aware retrieval; stronger evaluation/security/traces; then separately authorized AWS deployment, rollback and observed operation. Local or mock work cannot satisfy live cloud/provider gates. | Local starter scaffolds and checks are present. Cloud/model execution and learner evidence remain gated. |
| P3, roughly Days 103–163 | Developer Workflow Assistant: read-only tools first, bounded actions, human approval, genuine MCP client/server when justified, task-success baseline, injection/authorization tests and failure handoff. | Local workflow and protocol scaffolds are present. Actual learner attempts, model quality and human approval evidence remain unverified. |
| P4, roughly Days 164–224 | Customer discovery and scope; compatibility/release/incident/recovery; real authorized feedback where available; independent ownership and handoff. Role-play, peer review, pilot and production remain distinct. | Exercise scaffolds are present. Real customer/reviewer availability and learner performance remain separate gates. |
| P5, roughly Days 225–270 | Evidence-gate remediation, optional AWS readiness decision, ranking/recommendation slice with leakage-safe evaluation, truthful role gap/case study review. | Local ranker and evidence workflow scaffolds are present. Evaluation results, certification, job readiness and external outcomes are not claimed. |
| Further depth, optional roughly 60–90 days | Choose one gap from actual role and project evidence: a second integration, data/MLOps depth, owned AWS operations or a meaningful StreamForge extension. No fixed extra daily tickets or guarantee of mastery. | Outcome-based options only. |

Redis, Kafka, containers, CI/CD, IaC and MLOps appear only when a project requirement makes them useful. The assistant supplies general deployment/infra scaffolding with a walkthrough; the learner diagnoses behavior, evaluates tradeoffs and owns the model/evaluation seam. Real deployments need separate cost/permission approval.

## Learner ownership

The runnable paths connect learner-owned TODO adapters to local services and checks. Reference adapters are selected explicitly for mechanics; they do not count as learner implementation. The scaffold includes bounded backend, UI, evaluation, security, MCP, and operational exercises without turning the roadmap into a general full-stack course. The learner must still attempt the work, retain failed attempts, and provide evidence for capability gates.

## Role checkpoints

Use evidence reviews, not automatic titles: interview-ready for existing SWE/FDE-compatible roles as evidence supports; later AI product/FDE breadth; finally longer-term ownership evidence. FDE-level production/customer expectations require real authorized delivery and sustained operation. Role titles vary by company; calendar completion never grants a title or job readiness.
