# Calendar study day 82 — GAP-OIDC-07: Enforce tenant ownership in the authorization path

**Date:** 2027-01-15  
**Budget:** 25/85/10 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Keep bearer verification in an isolated stateless Spring resource-server chain and browser login in a separate Spring OAuth2 client chain. The loopback issuer serves discovery, JWKS, authorization-code redirects, and a one-use token exchange; Spring enforces state, nonce, signature, issuer, time and audience. The provider and all keys/data are synthetic; the existing cookie-session application stays unchanged. The assistant supplies this architecture; the learner implements and tests the assigned tenant/scope policy seam. No capstone design proposal or post-hoc design defense is assigned.

## Task

Implement the learner OidcTenantPolicy using exact tenant claim equality. Reject foreign, absent, blank, and wrongly typed claims; the instructor witness remains separate from learner implementation.

## Source

[Spring Security — Multi-tenancy](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/multitenancy.html)

## Acceptance

Same-tenant positive control and malformed/foreign-tenant negative controls use fixed claims.

## Command and evidence

`./mvnw -Plearner-tests -Dtest=OidcTenantPolicyAcceptanceTest test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-oidc-07.json`. This is not learner completion until genuinely attempted and independently reviewed.
