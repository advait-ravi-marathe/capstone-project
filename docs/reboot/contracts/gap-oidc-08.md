# Calendar study day 83 — GAP-OIDC-08: Apply least-privilege scope checks

**Date:** 2027-01-16  
**Budget:** 25/85/10 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Keep this experiment in an isolated, stateless Spring Security resource-server profile. Spring verifies issuer, time, audience and JWKS signature; a small tenant/scope policy runs only after authentication. The synthetic issuer binds to loopback, preserving the existing cookie-session application. The assistant supplies this architecture; the learner implements and tests its assigned policy seam. No capstone design proposal or post-hoc design defense is assigned.

## Task

Require the exact policy.read permission for this GET route. Verify policy.write, unknown scopes, substrings, blank scopes, and tenant mismatch do not authorize.

## Source

[Spring Security — Runtime Expectations; Supplying Audiences](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)

## Acceptance

The acceptance matrix tests scope tokenization and tenant relation independently.

## Command and evidence

`./mvnw -Plearner-tests -Dtest=OidcTenantPolicyAcceptanceTest test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-oidc-08.json`. This is not learner completion until genuinely attempted and independently reviewed.
