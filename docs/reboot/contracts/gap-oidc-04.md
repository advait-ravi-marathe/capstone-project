# Calendar study day 79 — GAP-OIDC-04: Reject issuer, audience, time, and malformed-token failures

**Date:** 2027-01-12  
**Budget:** 25/85/10 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Keep bearer verification in an isolated stateless Spring resource-server chain and browser login in a separate Spring OAuth2 client chain. The loopback issuer serves discovery, JWKS, authorization-code redirects, and a one-use token exchange; Spring enforces state, nonce, signature, issuer, time and audience. The provider and all keys/data are synthetic; the existing cookie-session application stays unchanged. The assistant supplies this architecture; the learner implements and tests the assigned tenant/scope policy seam. No capstone design proposal or post-hoc design defense is assigned.

## Task

Use independently signed wrong-issuer, wrong-audience, expired, and malformed tokens. Assert HTTP 401, no private body, and no successful handler response.

## Source

[Spring Security — Runtime Expectations; Supplying Audiences](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)

## Acceptance

Each counterfactual is independently expected to fail authentication and expose no synthetic subject.

## Command and evidence

`./mvnw -Dtest=OidcContractLabInfrastructureTest#issuerAudienceAndExpiryFailuresReturn401WithoutClaims test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-oidc-04.json`. This is not learner completion until genuinely attempted and independently reviewed.
