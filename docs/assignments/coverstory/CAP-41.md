# CAP-41 — Retrieve cited synthetic public wording

**Day:** 001 · 14 Oct 2026 · **Phase:** 1 · **Role:** linked component · **Component:** CAP-41

CAP-41 is the linked retrieval component of the CAP-40 first-day assignment and adds no scheduled minutes. It is a local learning fixture only: its excerpts are synthetic phrases, not policy terms or insurer evidence.

## Learner task

Implement `retrieve(query, documents, k)` in [`ai/retrieval/coverstory_health.py`](../../../ai/retrieval/coverstory_health.py). Preserve the supplied `PublicWording`, `Hit`, and lowercase whitespace-tokenizer types. Learner-owned behavior includes positive-overlap scoring, exclusion of zero-score documents, deterministic score/source ordering, `k` validation, and exact citation selection. The acceptance suite in [`ai/tests/test_coverstory_cap41_acceptance.py`](../../../ai/tests/test_coverstory_cap41_acceptance.py) intentionally reaches the TODO.

## Running app boundary

The browser's “Find a synthetic wording excerpt” panel posts only `{query, k}` to `/retrieval/health`. Vite proxies that path to a loopback-only standard-library Python service. The service loads only the fixed `ai/fixtures/coverstory/CAP-41/synthetic-v1.json` documents; it rejects caller-supplied documents and unknown request fields, bounds request size/query length/`k`, suppresses access logs, and sends `Cache-Control: no-store`. It returns exact excerpts with source ID, version, synthetic evidence status, and score. When the learner function still raises `NotImplementedError`, the endpoint returns a safe 501 and the UI says no wording was returned. The UI is wired to the actual seam, not an instructor implementation.

No login is required for this route because it exposes only the fixed public synthetic fixture. The route does not accept consumer health answers, call a model, fetch external sources, or write analytics. Do not add actual insurer wording or a recommendation.

## Commands

Run the local services in separate terminals from the repository root:

```sh
./mvnw spring-boot:run
python3 -m ai.retrieval.coverstory_health_api
cd web && npm run dev
```

Instructor transport and boundary checks:

```sh
python3 -m unittest ai.tests.test_coverstory_cap41_http
python3 -m unittest ai.tests.test_coverstory_cap41_infrastructure
```

Run learner acceptance separately (expected to fail at the explicit TODO until implemented):

```sh
python3 -m unittest ai.tests.test_coverstory_cap41_acceptance
```

The current test-only witness proves the HTTP adapter passes the fixed documents and returns citations through the same response contract. It does not implement `retrieve` in production. Read [integration evidence](../../evidence/coverstory/CAP-40/integration-result.json) and the [CAP-40 primary guide](CAP-40.md) for the joined first-day contract.
