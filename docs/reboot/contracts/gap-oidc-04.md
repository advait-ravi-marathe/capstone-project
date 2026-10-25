# Calendar study day 79 — GAP-OIDC-04: Reject issuer, audience, time, and malformed-token failures

**Date:** 2027-01-12  
**Budget:** 25/85/10 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Keep this experiment in an isolated, stateless Spring Security resource-server profile. Spring verifies issuer, time, audience and JWKS signature; a small tenant/scope policy runs only after authentication. The synthetic issuer binds to loopback, preserving the existing cookie-session application. The assistant supplies this architecture; the learner implements and tests its assigned policy seam. No capstone design proposal or post-hoc design defense is assigned.

## Task

Use independently signed wrong-issuer, wrong-audience, expired, and malformed tokens. Assert HTTP 401, no private body, and no successful handler response.

## Source

[Spring Security — Runtime Expectations; Supplying Audiences](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)

## Acceptance

Each counterfactual is independently expected to fail authentication and expose no synthetic subject.

## Command and evidence

`./mvnw -Dtest=OidcContractLabInfrastructureTest#issuerAudienceAndExpiryFailuresReturn401WithoutClaims test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-oidc-04.json`. This is not learner completion until genuinely attempted and independently reviewed.
