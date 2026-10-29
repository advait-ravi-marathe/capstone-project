# Current continuation implementation boundary

Sprint 1 on main implements the first 14 actual learning days. The published review branch `work/aws-fde-v24-continuation` contains conditional draft guidance and starter scaffolds through Day 270 against the immutable accepted contracts under `accepted/`. This branch is not merged to main, and authorship does not advance learner status. P1 Day 41, M2.1 Days 42–62, and M2.2 Days 62–82 remain conditional on their prerequisites and actual evidence. Standalone daily pages for Days 42–81 link the proposal schedules and exact stage guidance; their conditional status is not a passed assignment.

## Ownership

The learner-owned AI seams include the existing U001–U010 work and, in this continuation, versioned ingestion (U011), chunking/provenance (U012), actual embedding adapter validation (U013), isolated evaluation cases (U014), BM25 baseline/metrics (U015), vector indexing/search/evaluation (U016–U017), safe prompt/citations (U018), grounded generation (U019), abstention (U020), failure classification/redacted records (U021), and bounded fixture-path validation (U022). These functions remain explicit TODOs in `ai/fde_v2/coverstory/learner.py`. Assistant-owned orchestration applies U020 before generation, clears a document's prior Qdrant points before replacing its vector set, honors the requested `/ask` retrieval limit, and initializes retrieval without eagerly loading generation weights. Day30 authors U023 adversarial fixtures and expected deterministic-boundary notes only; it does not execute U023 or complete P1.

The assistant supplies loopback API, model/embedding loaders, SQLite/Qdrant persistence, React RAG panel, fixtures, evaluation routes and deterministic test harness. The learner reads/traces the supplied API/UI and performs the assigned role interview practice; the Day30 rendering walk-through is comprehension only. No general React, Java/Spring, authentication, generic CRUD or infrastructure implementation is assigned in Days 15–30.

## Evidence and gate state

- Every learning day is Not started; no evidence is credited by calendar passage or assistant scaffolding.
- Instructor reference tests, API test doubles and deterministic synthetic fixtures check mechanics only. They do not count as learner completion or model inference.
- The Qwen generation model is pinned, but the required weights are absent because the official Hugging Face metadata request returned HTTP403 through the configured proxy. Real generation remains unverified and blocked; see `runtime-verification.md`.
- Day14 begins U011 only. Day15 extends it to all-ten-document versioned ingestion. U013 real embeddings and U019 actual generated RAG remain blocked/unverified without the pinned model artifacts; fixed-vector or generation test doubles cannot satisfy those gates.
- Future deployment/provider credentials, cloud spend, real customer data and AWS operation require separate authorization.

## Accepted full-plan milestones versus this release

The accepted planning source places generation near Day 9, persisted vector retrieval near Day 22, generative RAG near Day 26 and the full local evaluated prototype around Day 41. These are estimates and gates, not completion claims. Authored daily pages exist for Days 1–270. Days 42–81 are conditional cards linked to M2.1/M2.2 detailed proposals, and Day 62 identifies the shared budget boundary. Their presence does not clear prerequisite/evidence gates. The model-backed P1 and cloud/customer/human-review evidence gates remain unresolved; no learner completion is claimed.
