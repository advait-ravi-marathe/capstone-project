# Calendar study day 71 — GAP-OPENAPI-01: Observe producer and consumer behavior

**Date:** 2027-01-04  
**Budget:** 25/85/10 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Treat the versioned OpenAPI file as the consumer contract and compare it with the existing controller/DTO boundary. The assistant supplies the current routes and expected compatibility policy; the learner implements the checker and its tests. No capstone design proposal or post-hoc design defense is assigned.

## Task

Inspect LocalPolicyIssueController and PolicyIssueRequest. Record the two operations, auth boundary, request fields, returned status codes, and independent expected field sets. Do not change application behavior.

## Source

[OpenAPI 3.2.1 — 4.1.2 OpenAPI Description Structure](https://spec.openapis.org/oas/v3.2.1.html)

## Acceptance

OpenAPICompatibilityInfrastructureTest: operation set and source DTO fields are fixed independently.

## Command and evidence

`./mvnw -Dtest=OpenApiCompatibilityInfrastructureTest test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-openapi-01.json`. This is not learner completion until genuinely attempted and independently reviewed.
