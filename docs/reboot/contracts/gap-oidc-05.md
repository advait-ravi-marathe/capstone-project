# Calendar study day 80 — GAP-OIDC-05: Reject unknown key identifiers and invalid token structure

**Date:** 2027-01-13  
**Budget:** 25/85/10 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Keep this experiment in an isolated, stateless Spring Security resource-server profile. Spring verifies issuer, time, audience and JWKS signature; a small tenant/scope policy runs only after authentication. The synthetic issuer binds to loopback, preserving the existing cookie-session application. The assistant supplies this architecture; the learner implements and tests its assigned policy seam. No capstone design proposal or post-hoc design defense is assigned.

## Task

Exercise malformed compact token and a validly signed token whose kid is absent from the JWKS. Confirm the filter chain returns 401 without claim disclosure.

## Source

[Spring Security — Runtime Expectations](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)

## Acceptance

Unknown kid and malformed token controls fail on actual decoder path; no unsigned-token bypass.

## Command and evidence

`./mvnw -Dtest=OidcContractLabInfrastructureTest#rotationAcceptsNewKidAndRejectsUnknownSigningKey test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-oidc-05.json`. This is not learner completion until genuinely attempted and independently reviewed.
