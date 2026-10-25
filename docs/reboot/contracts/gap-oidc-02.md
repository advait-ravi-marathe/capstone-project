# Calendar study day 77 — GAP-OIDC-02: Route bearer authentication through Spring Security

**Date:** 2027-01-10  
**Budget:** 25/85/10 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Keep bearer verification in an isolated stateless Spring resource-server chain and browser login in a separate Spring OAuth2 client chain. The loopback issuer serves discovery, JWKS, authorization-code redirects, and a one-use token exchange; Spring enforces state, nonce, signature, issuer, time and audience. The provider and all keys/data are synthetic; the existing cookie-session application stays unchanged. The assistant supplies this architecture; the learner implements and tests the assigned tenant/scope policy seam. No capstone design proposal or post-hoc design defense is assigned.

## Task

Trace the real SecurityFilterChain, issuer/time validator, audience validator, and default-deny route handling. Implement OidcTenantPolicy separately; do not replace token verification with a mock Authentication.

## Source

[Spring Security — Startup Expectations; Runtime Expectations](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)

## Acceptance

Actual MockMvc bearer path proves valid signed tokens reach authorization and invalid tokens fail before the handler.

## Command and evidence

`./mvnw -Dtest=OidcContractLabInfrastructureTest test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-oidc-02.json`. This is not learner completion until genuinely attempted and independently reviewed.
