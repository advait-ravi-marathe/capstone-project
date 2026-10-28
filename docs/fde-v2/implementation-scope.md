# Sprint 1 implementation boundary

This branch release implements the accepted AWS FDE v2.4 plan only through its first 14 actual learning days. The eight files under `accepted/` are the immutable full-plan inputs; their hash receipt is checked by the Sprint 1 contract test. Later accepted units/outcomes remain planning material and are not represented as executable code here.

## Ownership

The learner-owned AI seams assigned by this release are: validate one synthetic document record (Day3), forward a bounded prompt through the real local generator (Day8), strictly parse structured answer JSON (Day13), and begin idempotent insertion of one validated metadata row (Day14, partial U011 slice). These functions are explicit TODOs in `ai/fde_v2/coverstory/learner.py`.

The assistant supplies loopback API, generation model loading, React page, SQLite metadata schema, fixtures and deterministic test harness. The learner reads/traces the supplied API/UI, explains request/data flow, and performs the assigned role interview practice. There is no React, Java/Spring, authentication, generic CRUD or infrastructure implementation task in this Sprint 1 release. Future bounded integration ownership checkpoints are planning-only and require separate review.

## Evidence and gate state

- Every learning day is Not started; no evidence is credited by calendar passage or assistant scaffolding.
- Instructor reference tests, API test doubles and deterministic synthetic fixtures check mechanics only. They do not count as learner completion or model inference.
- The Qwen generation model is pinned, but the required weights are absent because the official Hugging Face metadata request returned HTTP403 through the configured proxy. Real generation remains unverified and blocked; see `runtime-verification.md`.
- Day14 begins U011 only. It does not pass all-ten-document ingestion, version-update policy, chunking, embeddings, vector persistence, retrieval or any RAG gate.
- Future deployment/provider credentials, cloud spend, real customer data and AWS operation require separate authorization.

## Accepted full-plan milestones versus this release

The accepted planning source places generation near Day9, persisted vector retrieval near Day22, generative RAG near Day26 and the full local evaluated prototype around Day41. Those are estimates and gates, not claims about this branch. Only Days1–14 have daily pages and U011 is partial; units scheduled on later days remain unimplemented and must not be run as if they were available.
