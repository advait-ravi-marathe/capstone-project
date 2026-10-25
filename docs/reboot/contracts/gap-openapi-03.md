# Calendar study day 73 — GAP-OPENAPI-03: Compare the contract with actual Spring mappings

**Date:** 2027-01-06  
**Budget:** 25/85/10 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Treat the versioned OpenAPI file as the consumer contract and compare it with the existing controller/DTO boundary. The assistant supplies the current routes and expected compatibility policy; the learner implements the checker and its tests. No capstone design proposal or post-hoc design defense is assigned.

## Task

Extend OpenApiCompatibilityInfrastructureTest to compare documented verb/path pairs against LocalPolicyIssueController annotations; include method, missing-route, and extra-route counterfactuals.

## Source

[OpenAPI 3.2.1 — 4.9 Path Item Object; 4.10 Operation Object](https://spec.openapis.org/oas/v3.2.1.html)

## Acceptance

Actual Spring annotations and contract operation sets must be equal; mutants adding/removing a route fail.

## Command and evidence

`./mvnw -Dtest=OpenApiCompatibilityInfrastructureTest#contractOperationsMatchTheActualSpringControllerMappings test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-openapi-03.json`. This is not learner completion until genuinely attempted and independently reviewed.
