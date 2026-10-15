# CAP-122 instructor scaffold — bounded offline rerank evaluation

**Canonical contract:** Day 089, 10 Jan 2027, 150 minutes. See [saved card](saved-days/day-089-cap-122.md), description SHA-256 `045c51a3c86bfa376c5a485363b233219dbb2648c27fb52b092fb52309e271ec`.

## Prepared scope

`EvidenceRerankEvaluator` is an empty learner seam. Its independent offline reference oracle and visible fixture contract are separate from it. The fixed synthetic fixture tests only two precomputed rankings, latency thresholds 99/100/101ms, timeout fallback, and rejection of invented ID `z9`. It makes no model call, production retrieval claim, statistical claim, insurer recommendation, or policy advice.

The subset check is against candidates already authorized before ranking. The evaluator has no fetch port: a proposed unknown ID must be rejected before any evidence can be fetched or returned. Deadline equality is accepted; a result above 100ms uses the original authorized order once and does not retry.

## Commands

Instructor fixture oracle:

```sh
./mvnw -Dtest=CAP122InstructorInfrastructureTest test
```

Learner acceptance (expected to fail at the preserved TODO until implemented):

```sh
./mvnw -Plearner-tests -Dtest=EvidenceRerankEvaluatorTest test
```

The learner suite has three methods. A test failure at `TODO(CAP-122)` is an expected unimplemented learner seam, not a passing learner result. Passing tests alone would not establish independent mastery.

## Prerequisites and limits

CAP-121’s owned source-lineage product seam is not represented as completed here. No SupportReadApi integration, public owning-module adapters, application workflow, persistence transaction, production retrieval, external model, or insurer fact source is supplied. Do not treat this fixture-only oracle as product readiness.

Fixture SHA-256: `2e9bfd5c96a776e3e86fdc9dc8169f3055593ae6a85441198d23f0d979e98f55`.
