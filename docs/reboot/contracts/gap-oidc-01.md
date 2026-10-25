# Calendar study day 76 — GAP-OIDC-01: Define an isolated resource-server lab seam

**Date:** 2027-01-09  
**Budget:** 25/85/10 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Keep this experiment in an isolated, stateless Spring Security resource-server profile. Spring verifies issuer, time, audience and JWKS signature; a small tenant/scope policy runs only after authentication. The synthetic issuer binds to loopback, preserving the existing cookie-session application. The assistant supplies this architecture; the learner implements and tests its assigned policy seam. No capstone design proposal or post-hoc design defense is assigned.

## Task

Review the isolated oidc-contract-lab profile and loopback-only synthetic issuer. Trace the learner policy seam; preserve the existing cookie-session configuration and never configure an external issuer.

## Source

[Spring Security — Specifying the Authorization Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)

## Acceptance

Infrastructure test proves issuer/JWKS URLs are loopback-only and the profile is separate from application sessions.

## Command and evidence

`./mvnw -Dtest=OidcContractLabInfrastructureTest test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-oidc-01.json`. This is not learner completion until genuinely attempted and independently reviewed.
