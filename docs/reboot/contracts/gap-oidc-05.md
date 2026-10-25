# Calendar study day 80 — GAP-OIDC-05: Reject unknown key identifiers and invalid token structure

**Date:** 2027-01-13  
**Budget:** 25/85/10 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Keep bearer verification in an isolated stateless Spring resource-server chain and browser login in a separate Spring OAuth2 client chain. The loopback issuer serves discovery, JWKS, authorization-code redirects, and a one-use token exchange; Spring enforces state, nonce, signature, issuer, time and audience. The provider and all keys/data are synthetic; the existing cookie-session application stays unchanged. The assistant supplies this architecture; the learner implements and tests the assigned tenant/scope policy seam. No capstone design proposal or post-hoc design defense is assigned.

## Task

Exercise malformed compact, unsigned, bad-signature, and validly signed unknown-kid tokens through the actual resource-server filter chain. Confirm each returns HTTP 401 without claim disclosure.

## Source

[Spring Security — Runtime Expectations](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)

## Acceptance

Unknown kid, unsigned, bad-signature, and malformed-token controls fail on the actual decoder path with HTTP 401 and no claim disclosure.

## Command and evidence

`./mvnw -Dtest=OidcContractLabInfrastructureTest#invalidTokenStructuresFailClosed test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-oidc-05.json`. This is not learner completion until genuinely attempted and independently reviewed.
