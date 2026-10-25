# Calendar study day 76 — GAP-OIDC-01: Define isolated browser-login and resource-server paths

**Date:** 2027-01-09  
**Budget:** 25/85/10 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Keep bearer verification in an isolated stateless Spring resource-server chain and browser login in a separate Spring OAuth2 client chain. The loopback issuer serves discovery, JWKS, authorization-code redirects, and a one-use token exchange; Spring enforces state, nonce, signature, issuer, time and audience. The provider and all keys/data are synthetic; the existing cookie-session application stays unchanged. The assistant supplies this architecture; the learner implements and tests the assigned tenant/scope policy seam. No capstone design proposal or post-hoc design defense is assigned.

## Task

Review the isolated oidc-contract-lab profile and loopback-only synthetic issuer. Trace its authorization-code and token exchange through Spring OAuth2 Login, then the separate bearer resource-server path; preserve the existing cookie-session configuration and never configure an external issuer.

## Source

[Spring Security — Core Configuration](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html)

## Acceptance

Infrastructure test proves issuer/JWKS URLs are loopback-only and the profile is separate from application sessions.

## Command and evidence

`./mvnw -Dtest=OidcContractLabInfrastructureTest test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-oidc-01.json`. This is not learner completion until genuinely attempted and independently reviewed.
