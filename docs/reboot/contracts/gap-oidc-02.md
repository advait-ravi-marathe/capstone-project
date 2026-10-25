# Calendar study day 77 — GAP-OIDC-02: Route bearer authentication through Spring Security

**Date:** 2027-01-10  
**Budget:** 25/85/10 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Keep this experiment in an isolated, stateless Spring Security resource-server profile. Spring verifies issuer, time, audience and JWKS signature; a small tenant/scope policy runs only after authentication. The synthetic issuer binds to loopback, preserving the existing cookie-session application. The assistant supplies this architecture; the learner implements and tests its assigned policy seam. No capstone design proposal or post-hoc design defense is assigned.

## Task

Trace the real SecurityFilterChain, issuer/time validator, audience validator, and default-deny route handling. Implement OidcTenantPolicy separately; do not replace token verification with a mock Authentication.

## Source

[Spring Security — Startup Expectations; Runtime Expectations](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)

## Acceptance

Actual MockMvc bearer path proves valid signed tokens reach authorization and invalid tokens fail before the handler.

## Command and evidence

`./mvnw -Dtest=OidcContractLabInfrastructureTest test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-oidc-02.json`. This is not learner completion until genuinely attempted and independently reviewed.
