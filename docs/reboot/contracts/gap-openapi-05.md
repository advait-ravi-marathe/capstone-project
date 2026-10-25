# Calendar study day 75 — GAP-OPENAPI-05: Run compatibility mutation controls and capture evidence

**Date:** 2027-01-08  
**Budget:** 25/85/10 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Treat the versioned OpenAPI file as the consumer contract and compare it with the existing controller/DTO boundary. The assistant supplies the current routes and expected compatibility policy; the learner implements the checker and its tests. No capstone design proposal or post-hoc design defense is assigned.

## Task

Run the compatibility witness against unchanged, additive, and breaking documents; introduce a deliberate always-compatible mutant and confirm the independent actual-path assertion rejects it.

## Source

[OpenAPI 3.2.1 — 4.16 Responses Object; 4.24 Schema Object](https://spec.openapis.org/oas/v3.2.1.html)

## Acceptance

The immutable expected operation/field/status diagnostics reject always-compatible and wrong-diagnostic mutants.

## Command and evidence

`./mvnw -Dtest=OpenApiCompatibilityInfrastructureTest test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-openapi-05.json`. This is not learner completion until genuinely attempted and independently reviewed.
