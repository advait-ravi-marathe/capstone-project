# Calendar study day 78 — GAP-OIDC-03: Exercise provider discovery and JWKS signature verification

**Date:** 2027-01-11  
**Budget:** 25/85/10 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Keep bearer verification in an isolated stateless Spring resource-server chain and browser login in a separate Spring OAuth2 client chain. The loopback issuer serves discovery, JWKS, authorization-code redirects, and a one-use token exchange; Spring enforces state, nonce, signature, issuer, time and audience. The provider and all keys/data are synthetic; the existing cookie-session application stays unchanged. The assistant supplies this architecture; the learner implements and tests the assigned tenant/scope policy seam. No capstone design proposal or post-hoc design defense is assigned.

## Task

Use the disposable local issuer's discovery metadata and JWKS. Follow one signed synthetic token through discovery, key selection, signature verification, and the protected handler.

## Source

[Spring Security — Specifying the Authorization Server; Startup Expectations](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)

## Acceptance

The test observes a real signed bearer request and successful protected response; mock tokens are not accepted by this path.

## Command and evidence

`./mvnw -Dtest=OidcContractLabInfrastructureTest#signedTokenDiscoveredThroughLocalIssuerReachesProtectedHandler test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-oidc-03.json`. This is not learner completion until genuinely attempted and independently reviewed.
