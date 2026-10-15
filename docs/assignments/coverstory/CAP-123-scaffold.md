# CAP-123 instructor scaffold: policy projection and grounded help

The saved Day 090 contract is [day-090-cap-123.md](saved-days/day-090-cap-123.md), source description SHA-256 `c38a0de1744ffa65de7b59b3ceb2638a9d82ee53c27d14f174ce8cfca290c8a6`.

## Bounded exercise

Run the instructor oracle with `./mvnw -Dtest=CAP123InstructorInfrastructureTest test`. It reads only [cap-123.json](../../../coverstory/fixtures/cap-123.json), a synthetic static fixture with network and writes disabled. Cases illustrate (1) exactly one owned simulated policy with chronological issuance timeline and exact source citation, (2) accepted offer plus payment without issuance remaining `ISSUANCE_PENDING` with explicit no-cover wording, and (3) unsupported or foreign-owner questions abstaining/denying with no fixture write count.

The learner-owned method is `PolicyHelpDemoGate.answer`. Acceptance cases are in `PolicyHelpDemoGateTest`, tagged `learnerAcceptance`; run with `./mvnw -Plearner-tests -Dtest=PolicyHelpDemoGateTest test`. Until the TODO is implemented, three tests are expected to error at the explicit `UnsupportedOperationException`; they are not instructor failures.

## Scope and missing prerequisites

This is a test-only contract exercise. It does not invoke database state, application/review/payment/issuance services, insurer, provider, support API, UI, or model. No end-to-end transition harness was found assembled. Public module adapters, authoritative owner authorization, durable policy projection, and transaction boundaries remain blocked. Do not describe this oracle as a working lifecycle or a live-sales control.

See [architecture delta](../../../architecture/coverstory/CAP-123-delta.md), [result receipt](../../../evidence/coverstory/CAP-123/result.json), and [explain-back template](../../../evidence/coverstory/CAP-123/explain-back.md). No learner result or timebox rehearsal is claimed.
