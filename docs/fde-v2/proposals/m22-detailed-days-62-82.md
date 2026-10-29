# M2.2 conditional implementation plan: security, identity and deployment preparation

**Status: proposed scaffold and conditional teaching design only.** The accepted outcome contract is unchanged. This does not activate M2.2, complete a learning day, prove P1/M2.1 prerequisites, create an identity credential, configure a real identity service, or claim an AWS deployment. No learner days are complete. The learner seams in `ai/fde_v2/m22/learner.py` remain TODOs. No new credential, account, cloud resource or network service is required by this draft.

The prerequisite is accepted **M2.1: working data and evaluation baseline**. P1 actual model, embedding, vector retrieval and learner-ownership evidence is also still blocked. Authors can prepare this conditional scaffold now; the learner must not advance into M2.2 until those gates have separately passed. Its mechanics witnesses test only small policy and boundary functions.

## Scope, capacity and actual-day accounting

This design maps the accepted `AIFDE-V2-M2.2` deliverables to a proposed 21-session window, actual learning Days 62–82. It preserves 60 minutes DSA +30 minutes interview/AWS +90 minutes AI per day. If DSA takes 75/90 minutes, AI becomes 75/60 and the same ordered work carries forward; it is not compressed. DSA attempts remain on actual dates with the existing D/D+3/D+7/D+21 reviews. The 30-minute visibility slot remains every seventh actual learning day inside AI time.

| AI time category | Minutes | Accounting |
|---|---:|---|
| M2.2 substantive work | 1,500 | 25.0 hours across ten 150-minute stages |
| Genuine M2.2 repair/retest buffer | 240 | 4.0 hours on Days 80–82 |
| Weekly visibility | 90 | Days 63, 70, 77; included in daily AI total, not charged twice |
| M2.1 boundary buffer | 30 | Day 62 only; preserves the prior M2.1 contract |
| Unallocated outside M2.2 | 30 | Day 82; no feature/task is assigned to it |
| AI capacity across Days 62–82 | 1,890 | 21 × 90 minutes |

Thus M2.2 is exactly **29 AI hours** (1,500+240), not 29 hours plus visibility or the M2.1 boundary. The prior M2.1 schedule stays unchanged: M2.2 proposes using Day 62's otherwise-unassigned 60 minutes for S01 while preserving its 30-minute M2.1 buffer. The Day 82 final 30 minutes remain unallocated. Role/AWS sessions continue twice per seven actual days on Days 62, 68, 69, 75, 76 and 82 (inside the existing 30-minute role bank); the other 15 role-bank slots stay interview practice. The proposed M2.2 AWS gap work uses the already-recorded C0 result; it does not repeat the initial audit or assume a result exists.

For a 150-minute stage: 30 study +20 worked example +80 learner implementation/testing +20 evidence/error log. Buffers are reserved for repair/retest and measured overrun, not new lessons. Time automated runs as work unless they are explicitly queued during a nonconflicting scheduled segment. If measured demand exceeds unspent buffer, pause and re-estimate; do not extend hours, cut tests or borrow DSA/interview time.

Exact relative-day allocation is machine-readable in [`draft_schedule.json`](../../../ai/fde_v2/m22/draft_schedule.json). Days 63/70/77 allocate 60 M2.2 minutes plus 30 visibility minutes. The boundary accounting on Days 62/82 is explicit there.

## Teaching resources and worked concepts

Read only the headings listed. These are source pointers for teaching, not a requirement to finish whole vendor documents or install a tool. Dependency versions remain pinned separately; documentation updates do not silently upgrade packages.

1. [FastAPI OAuth2/JWT tutorial](https://fastapi.tiangolo.com/tutorial/security/oauth2-jwt/): “About JWT”; “Password hashing”; “Handle JWT tokens”; “Get the current user.” Teach a signed-token verifier supplied by a maintained library; learners must never implement cryptography or accept a decoded-but-unverified token.
2. [Qdrant multitenancy](https://qdrant.tech/documentation/manage-data/multitenancy/): “Partition by payload”; query filtering; limitations. Build filters from authenticated server context, then validate returned IDs against authoritative SQL ACL and active-version state. A post-filter alone can reduce recall; a UI/prompt filter is not authorization.
3. [OpenTelemetry Python instrumentation](https://opentelemetry.io/docs/languages/python/instrumentation/): “Tracing,” “Spans,” “Attributes,” and “Exceptions.” Show how exception text and span attributes can reveal prompts or tokens; allowlist safe fields.
4. [Docker build best practices](https://docs.docker.com/build/building/best-practices/): multi-stage builds, `.dockerignore`, pinned base images and non-root `USER`. Explain immutable input images, narrow copy boundaries and writable runtime directories.
5. [pytest monkeypatch](https://docs.pytest.org/en/stable/how-to/monkeypatch.html): patching functions and environment variables. Use only for deterministic failure injection; a patched identity/model is not a real identity exchange.
6. [Python 3.12 asyncio subprocesses](https://docs.python.org/3.12/library/asyncio-subprocess.html): `create_subprocess_exec`, process `communicate` and `wait`. Preserve one end-to-end deadline across retrieval/generation; bound retries and cancellation.
7. [AWS Well-Architected Framework](https://docs.aws.amazon.com/wellarchitected/latest/framework/welcome.html): Introduction and the one pillar chosen from an existing C0 gap. [AWS Budgets](https://docs.aws.amazon.com/cost-management/latest/userguide/budgets-managing-costs.html): budget/usage alerts and their limitations. Use diagrams and cost assumptions only; do not provision anything.

Worked examples use fictional in-memory identities and synthetic content: Alice can read her tenant's shared document; a forged request-body tenant cannot change that. A vector hit for a revoked same-tenant document must be rechecked against SQL before prompt assembly. A trace records action, outcome, pseudonymous subject, trace ID and timestamp, never raw prompt/token/exception content. A deadline example spends one budget across retrieval and generation rather than restarting the clock for every retry.

## Proposed stage contracts

Each stage is 150 substantive minutes. These are proposed detailed units inside the existing accepted outcome, not new accepted outcomes. The names and functions are scaffolding interfaces, not implemented identity/deployment features.

| Stage | Study + worked example | Learner-owned seam and independent acceptance | Negative control |
|---|---|---|---|
| S01 — threat boundary / access matrix | FastAPI JWT current-user sections; trace subject→SQL ownership→vector filtering and show why hiding a button proves nothing. | `authorize_document(context, document, action)`: decide own/shared-read/foreign/revoked/unknown actions from server-authenticated context. Cover read/write/delete and missing or malformed claims. | Substitute request-body tenant for authenticated tenant; cross-tenant fixtures must be denied. |
| S02 — established local identity adapter | FastAPI token verification and hashing sections; distinguish signature-verified issuer/audience/expiry from base64 decoding. | `verify_identity(token, verified_claims_provider, subject_lookup, ...)` is intentionally unsolved; the provider must call a maintained verifier that rejects signature/algorithm errors. The wrapper checks issuer/audience/expiry and server-derived subject/tenant. Never implement crypto. | Accept unsigned, wrong-key, wrong issuer/audience, expired or disabled identity; identity acceptance must fail. The current callback mechanics tests do not validate cryptographic signatures or constitute authentication. |
| S03 — server/SQL boundary | FastAPI current-user dependency; parameterized ownership predicates across every protected route. | Bind validated identity to read/search/ask/export/update/delete service calls. Existing route/service scaffold remains instructor-owned; learner owns a small policy integration and tests. Legacy records default-deny until an explicit ownership mapping exists. | Remove a route guard or interpolate a client tenant into SQL; contract and exposure checks must fail. |
| S04 — retrieval/cache isolation | Qdrant payload filtering/limitations; pre-filter versus top-k/post-filter example. | `filter_retrieved_hits(context, hits, authoritative_documents)` is a second authoritative check. Any cache must be keyed by effective authorization/version or revalidated against current ACL/source dependencies. | Cross-tenant same-text hit, guessed ID, missing payload, stale/deleted version, warmed cache after revocation; none may enter prompt/output. |
| S05 — config, secrets and audit | OTel spans/attributes/exceptions; show token/document leakage through exception representation. | `safe_audit_event(event)` emits only subject pseudonym, action, outcome, trace ID and timestamp. Read config through supplied boundary; explain secret injection without creating persistent credentials. | Put prompt/token in logs, trace attributes or exception body; synthetic-canary scanner must detect it. |
| S06 — retention/deletion/recovery | Qdrant query filters; trace logical revocation before physical cleanup, plus SQL/vector/cache/backup boundaries. | `revoke_access` denies immediately; `purge_document` performs idempotent cleanup; `reconcile_deleted_content` reports each store and backup state. Existing assistant-owned storage primitives remain instructor-supplied. | Remove only SQL row while vector/cache still returns content; fail closed and report cleanup pending. Do not imply backups were erased. |
| S07 — container/reproducible build | Docker best-practice headings; review supplied minimal Dockerfile and config. | Learner verifies clean build/test/config path, health/readiness distinction, non-root identity and image/context exclusion. General Dockerfile/CI plumbing is supplied; learner repairs only the scoped AI/security seam. | Run as root or include fixture secrets/private corpus from build context; policy check must fail. Missing local Docker is BLOCKED, not pass. |
| S08 — deadlines/retries/traces | OTel tracing and Python subprocess deadlines; worked shared-clock failure timeline. | `remaining_budget(deadline, now)` and `retry_decision(error_kind, attempt, remaining)`; propagate trace ID; bound payload, output and concurrency using declared local limits. Measure a small development fixture before selecting thresholds. | Restart deadline per retry, retry permanent auth/input failures, exceed attempt limit or leak content into spans. |
| S09 — AWS gaps, no provisioning | AWS Well-Architected introduction/chosen pillar and AWS Budgets; map local identity/network/storage/compute to alternatives. | Use the existing C0 result if it exists; record one selected gap, least-privilege service choice, region/data boundary, cost assumptions and teardown steps. If C0 is absent, mark missing. | Fabricate C0 evidence or label a diagram “deployed/live”; evidence validator rejects both. No AWS account/configuration/resource action. |
| S10 — release evidence / explanation | FastAPI and Qdrant sections; walk one synthetic denial end-to-end with server and retrieval checks separately. | `evaluate_evidence(receipts)` can report only BLOCKED or ELIGIBLE_FOR_INDEPENDENT_REVIEW. Learner explains one actual failure/fix; independent review is still required. | A missing actual receipt, synthetic auth/model, clean build only, or local-only plan cannot become M2.2 PASS. |

Daily slices and role-bank labels are listed per actual Day 62–82 in the JSON schedule. These allocations are relative learning-session positions, not dates/deadlines; a slower DSA day shifts the schedule. No public posting is implied by the visibility slot.

## Security and evidence contract

`authorize_document` and retrieval filtering are mechanics, not authentication. The real application path must eventually obtain subject/tenant context from an actual verified identity exchange and enforce it at **both** server and retrieval boundary. No prompt, browser state, vector payload or client tenant field is an authorization authority. The local synthetic witnesses cannot satisfy the actual identity-flow gate.

The proposed final receipt matrix requires all of: passed M2.1 prerequisite; actual identity flow; actual server access tests; actual retrieval-boundary isolation; reviewed threat model; actual secret/log scan; retention/deletion evidence; clean reproducible build; redacted actual trace; reviewed cost/performance budget; and reviewed learner explanation. The helper never declares M2.2 passed: only an external independent review can turn complete actual evidence into a learner outcome. Test doubles never count as identity/authentication or real deployment.

Deterministic layer: policy table, stale/revoked result filtering, audit allowlist, deadline/retry logic and fail-closed receipt handling. Actual local integration layer: established identity exchange plus real protected route/service and persisted retrieval boundary, then clean container/build and captured redacted trace. AWS/cloud evidence remains separate, unavailable without later explicit authority. No credentials, model requests, cloud setup, deployment, account activity or spending were performed by this scaffold.

## Commands and readiness state

From repository root after existing pinned requirements are installed:

```sh
python -m ai.fde_v2.m22.acceptance --milestone AIFDE-V2-M2.2 --mode mechanics
M22_CANDIDATE=ai.fde_v2.m22.reference python -m ai.fde_v2.m22.acceptance --milestone AIFDE-V2-M2.2 --mode learner
python -m ai.fde_v2.m22.acceptance --milestone AIFDE-V2-M2.2 --mode learner
python -m ai.fde_v2.m22.acceptance --milestone AIFDE-V2-M2.2 --mode integration
python -m ai.fde_v2.m22.acceptance --milestone AIFDE-V2-M2.2 --mode evidence
```

The first two commands are deterministic instructor mechanics/acceptance witnesses. The third is expected to fail at unsolved learner TODOs. Integration and evidence commands exit blocked until real prerequisites/receipts exist; they are not production-readiness claims. Exact test output and mutations are recorded in [`m22-draft-verification.md`](../reports/m22-draft-verification.md).

The accepted v2.4 contract remains the only roadmap authority. This is conditional design detail to review and implement; it is not added to the active days directory, does not overwrite prior plans, and does not advance learner status.
