# Sprint 1 implementation boundary

Sprint 1 on main implements actual learning Days 1–14. This separate continuation branch adds Days 15–28 against the same immutable accepted contracts under `accepted/`; it remains unpublished and requires review. Day14 is only the first 30-minute U011 slice. This continuation does not complete the accepted first-30 schedule.

## Ownership

The learner-owned AI seams include the existing U001–U010 work and, in this continuation, versioned ingestion (U011), chunking/provenance (U012), actual embedding adapter validation (U013), isolated evaluation cases (U014), BM25 baseline/metrics (U015), vector indexing/search/evaluation (U016–U017), safe prompt/citations (U018), grounded generation (U019), abstention (U020), and failure classification/redacted records (U021). These functions remain explicit TODOs in `ai/fde_v2/coverstory/learner.py`.

The assistant supplies loopback API, model/embedding loaders, SQLite/Qdrant persistence, React RAG panel, fixtures, evaluation routes and deterministic test harness. The learner reads/traces the supplied API/UI, explains request/data flow, and performs the assigned role interview practice. No general React, Java/Spring, authentication, generic CRUD or infrastructure implementation is assigned in Days 15–28.

## Evidence and gate state

- Every learning day is Not started; no evidence is credited by calendar passage or assistant scaffolding.
- Instructor reference tests, API test doubles and deterministic synthetic fixtures check mechanics only. They do not count as learner completion or model inference.
- The Qwen generation model is pinned, but the required weights are absent because the official Hugging Face metadata request returned HTTP403 through the configured proxy. Real generation remains unverified and blocked; see `runtime-verification.md`.
- Day14 begins U011 only. Day15 extends it to all-ten-document versioned ingestion. U013 real embeddings and U019 actual generated RAG remain blocked/unverified without the pinned model artifacts; fixed-vector or generation test doubles cannot satisfy those gates.
- Future deployment/provider credentials, cloud spend, real customer data and AWS operation require separate authorization.

## Accepted full-plan milestones versus this release

The accepted planning source places generation near Day9, persisted vector retrieval near Day22, generative RAG near Day26 and the full local evaluated prototype around Day41. These are estimates and gates, not completion claims. Daily guidance is authored through Day28 in this unpublished continuation. U021 starts on Day28 and has its final 30-minute segment on Day29, which is outside this branch scope; later first-30 units remain planning contracts and must not be run as available.
