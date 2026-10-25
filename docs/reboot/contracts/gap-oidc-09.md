# Calendar study day 84 — GAP-OIDC-09: Run full protocol and authorization acceptance

**Date:** 2027-01-17  
**Budget:** 25/85/10 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Keep bearer verification in an isolated stateless Spring resource-server chain and browser login in a separate Spring OAuth2 client chain. The loopback issuer serves discovery, JWKS, authorization-code redirects, and a one-use token exchange; Spring enforces state, nonce, signature, issuer, time and audience. The provider and all keys/data are synthetic; the existing cookie-session application stays unchanged. The assistant supplies this architecture; the learner implements and tests the assigned tenant/scope policy seam. No capstone design proposal or post-hoc design defense is assigned.

## Task

Run the complete isolated protocol suite and learner policy acceptance. Preserve the actual issuer/JWKS test path and record any failure as a blocker, not completion.

## Source

[Spring Security — OAuth 2.0 Login](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/index.html)

## Acceptance

Protocol infrastructure and learner authorization acceptance are separately reported.

## Command and evidence

`./mvnw -Dtest=OidcContractLabInfrastructureTest test && ./mvnw -Plearner-tests -Dtest=OidcTenantPolicyAcceptanceTest test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-oidc-09.json`. This is not learner completion until genuinely attempted and independently reviewed.
