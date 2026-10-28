# AWS FDE v2.4 phase map

This is a capacity plan, not a completion calendar. Day 1 is the first actual learning session. Sprint 1 supplies daily guidance for Days 1–14; remaining first-30 units and later windows remain planning contracts until separately implemented and reviewed. A missed date does not advance learner state.

## Capacity

Default: 60 minutes DSA + 30 minutes role interview/AWS preparation + 90 minutes AI work = 180 minutes. When a DSA revision takes 75 or 90 minutes, AI work falls to 75 or 60 minutes. Interview-near blocks also replace existing time. Carry work forward; never compress acceptance criteria or add hours. Visibility work is inside AI time. Certification and vendor study stay inside existing blocks.

First 30 actual days contain 30 DSA hours, 11 role-interview hours, 4 AWS study hours, 43 hours of AI unit work and 2 hours of visibility, totaling 90 hours. The first 30-day schedule only partially completes the 30 dependency units; the full evaluated local prototype is estimated around actual Day 41.

## Outcomes and gates

| Window (approximate learning days) | Planned outcome and evidence gate | Status |
|---|---|---|
| P1, approximately Days 1–41 | CoverStory local generative RAG: real pretrained generation, real embeddings, persisted vector store, source provenance, lexical baseline, abstention and dev/held-out evaluation. Day 30 only begins adversarial fixture work. | Sprint 1 daily pages cover Days 1–14. Remaining first-release units are planning-only; real model currently blocked/unverified. |
| P2, roughly Days 42–102 | Ingestion/version/deletion controls; permission-aware retrieval; stronger evaluation/security/traces; then separately authorized AWS deployment, rollback and observed operation. Local or mock work cannot satisfy live cloud/provider gates. | Planning-only. |
| P3, roughly Days 103–163 | Developer Workflow Assistant: read-only tools first, bounded actions, human approval, genuine MCP client/server when justified, task-success baseline, injection/authorization tests and failure handoff. | Planning-only. |
| P4, roughly Days 164–224 | Customer discovery and scope; compatibility/release/incident/recovery; real authorized feedback where available; independent ownership and handoff. Role-play, peer review, pilot and production remain distinct. | Planning-only. |
| P5, roughly Days 225–271 | Evidence-gate remediation, optional AWS readiness decision, StreamForge ranking/recommendation slice with leakage-safe evaluation, truthful role gap/case study review. | Planning-only. |
| Further depth, optional roughly 60–90 days | Choose one gap from actual role and project evidence: a second integration, data/MLOps depth, owned AWS operations or a meaningful StreamForge extension. No fixed extra daily tickets or guarantee of mastery. | Outcome-based options only. |

Redis, Kafka, containers, CI/CD, IaC and MLOps appear only when a project requirement makes them useful. The assistant supplies general deployment/infra scaffolding with a walkthrough; the learner diagnoses behavior, evaluates tradeoffs and owns the model/evaluation seam. Real deployments need separate cost/permission approval.

## Bounded integration ownership

Interview comprehension remains in the question bank. To address practical FDE ownership, five future checkpoints replace existing comprehension allocations: (1) Day 225 plan to validate a small AI-serving contract; (2) Day 332 diagnose and fix a bounded API failure; (3) Day 333 resolve a stale-response UI defect; (4) Day 420 explain/fix score presentation against evaluation evidence; (5) Day 507 handle interaction states in a supplied screen. Only the first is a near-term planning concept in this release; Days 332, 420 and 507 are not executable starters here. Each eventual checkpoint needs a learner-authored small change, a failing-before/fixed-after test, debugging notes and independent review. Do not expand these into a general full-stack course or add time.

## Role checkpoints

Use evidence reviews, not automatic titles: interview-ready for existing SWE/FDE-compatible roles as evidence supports; later AI product/FDE breadth; finally longer-term ownership evidence. FDE-level production/customer expectations require real authorized delivery and sustained operation. Role titles vary by company; calendar completion never grants a title or job readiness.
