# Calendar study day 84 — GAP-OIDC-09: Run full protocol and authorization acceptance

**Date:** 2027-01-17  
**Budget:** 25/85/10 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Keep this experiment in an isolated, stateless Spring Security resource-server profile. Spring verifies issuer, time, audience and JWKS signature; a small tenant/scope policy runs only after authentication. The synthetic issuer binds to loopback, preserving the existing cookie-session application. The assistant supplies this architecture; the learner implements and tests its assigned policy seam. No capstone design proposal or post-hoc design defense is assigned.

## Task

Run the complete isolated protocol suite and learner policy acceptance. Preserve the actual issuer/JWKS test path and record any failure as a blocker, not completion.

## Source

[Spring Security — JWT Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)

## Acceptance

Protocol infrastructure and learner authorization acceptance are separately reported.

## Command and evidence

`./mvnw -Dtest=OidcContractLabInfrastructureTest test && ./mvnw -Plearner-tests -Dtest=OidcTenantPolicyAcceptanceTest test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-oidc-09.json`. This is not learner completion until genuinely attempted and independently reviewed.
