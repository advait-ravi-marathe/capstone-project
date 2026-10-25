# Calendar study day 82 — GAP-OIDC-07: Enforce tenant ownership in the authorization path

**Date:** 2027-01-15  
**Budget:** 25/85/10 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Keep this experiment in an isolated, stateless Spring Security resource-server profile. Spring verifies issuer, time, audience and JWKS signature; a small tenant/scope policy runs only after authentication. The synthetic issuer binds to loopback, preserving the existing cookie-session application. The assistant supplies this architecture; the learner implements and tests its assigned policy seam. No capstone design proposal or post-hoc design defense is assigned.

## Task

Implement the learner OidcTenantPolicy using exact tenant claim equality. Reject foreign, absent, blank, and wrongly typed claims; the instructor witness remains separate from learner implementation.

## Source

[Spring Security — Multi-tenancy](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/multitenancy.html)

## Acceptance

Same-tenant positive control and malformed/foreign-tenant negative controls use fixed claims.

## Command and evidence

`./mvnw -Plearner-tests -Dtest=OidcTenantPolicyAcceptanceTest test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-oidc-07.json`. This is not learner completion until genuinely attempted and independently reviewed.
