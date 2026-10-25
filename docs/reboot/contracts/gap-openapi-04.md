# Calendar study day 74 — GAP-OPENAPI-04: Evaluate consumer compatibility for provider changes

**Date:** 2027-01-07  
**Budget:** 25/85/10 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Treat the versioned OpenAPI file as the consumer contract and compare it with the existing controller/DTO boundary. The assistant supplies the current routes and expected compatibility policy; the learner implements the checker and its tests. No capstone design proposal or post-hoc design defense is assigned.

## Task

Implement the compatibility learner seam for additive optional response fields versus removed consumed fields, new required request fields, changed response status, and changed field types.

## Source

[OpenAPI 3.2.1 — 4.16 Responses Object; 4.24 Schema Object](https://spec.openapis.org/oas/v3.2.1.html)

## Acceptance

Independent expected diagnostics verify exact changed operations and preserve optional additions as compatible.

## Command and evidence

`./mvnw -Plearner-tests -Dtest=OpenApiCompatibilityAcceptanceTest test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-openapi-04.json`. This is not learner completion until genuinely attempted and independently reviewed.
