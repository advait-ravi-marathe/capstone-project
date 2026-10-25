# Calendar study day 72 — GAP-OPENAPI-02: Complete the versioned API description

**Date:** 2027-01-05  
**Budget:** 25/85/10 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Treat the versioned OpenAPI file as the consumer contract and compare it with the existing controller/DTO boundary. The assistant supplies the current routes and expected compatibility policy; the learner implements the checker and its tests. No capstone design proposal or post-hoc design defense is assigned.

## Task

Edit src/main/resources/contracts/coverstory-policies-v1.json to describe the existing POST and GET routes, request/response schemas, auth, and exact status variants including POST 200/201/400/401/404/409. Preserve the independent fixture assertions.

## Source

[OpenAPI 3.2.1 — 4.8 Paths Object; 4.10 Operation Object; 4.24 Schema Object](https://spec.openapis.org/oas/v3.2.1.html)

## Acceptance

Infrastructure checks resolve required fields/statuses and reject undocumented or extra controller operations.

## Command and evidence

`./mvnw -Dtest=OpenApiCompatibilityInfrastructureTest test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-openapi-02.json`. This is not learner completion until genuinely attempted and independently reviewed.
