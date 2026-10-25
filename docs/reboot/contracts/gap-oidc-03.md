# Calendar study day 78 — GAP-OIDC-03: Exercise provider discovery and JWKS signature verification

**Date:** 2027-01-11  
**Budget:** 25/85/10 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Keep this experiment in an isolated, stateless Spring Security resource-server profile. Spring verifies issuer, time, audience and JWKS signature; a small tenant/scope policy runs only after authentication. The synthetic issuer binds to loopback, preserving the existing cookie-session application. The assistant supplies this architecture; the learner implements and tests its assigned policy seam. No capstone design proposal or post-hoc design defense is assigned.

## Task

Use the disposable local issuer's discovery metadata and JWKS. Follow one signed synthetic token through discovery, key selection, signature verification, and the protected handler.

## Source

[Spring Security — Specifying the Authorization Server; Startup Expectations](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)

## Acceptance

The test observes a real signed bearer request and successful protected response; mock tokens are not accepted by this path.

## Command and evidence

`./mvnw -Dtest=OidcContractLabInfrastructureTest#signedTokenDiscoveredThroughLocalIssuerReachesProtectedHandler test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-oidc-03.json`. This is not learner completion until genuinely attempted and independently reviewed.
