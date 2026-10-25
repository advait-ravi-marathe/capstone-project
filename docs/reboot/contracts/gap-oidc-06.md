# Calendar study day 81 — GAP-OIDC-06: Test key rotation and overlap

**Date:** 2027-01-14  
**Budget:** 25/85/10 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Keep bearer verification in an isolated stateless Spring resource-server chain and browser login in a separate Spring OAuth2 client chain. The loopback issuer serves discovery, JWKS, authorization-code redirects, and a one-use token exchange; Spring enforces state, nonce, signature, issuer, time and audience. The provider and all keys/data are synthetic; the existing cookie-session application stays unchanged. The assistant supplies this architecture; the learner implements and tests the assigned tenant/scope policy seam. No capstone design proposal or post-hoc design defense is assigned.

## Task

Use only the local synthetic issuer. Verify the previously active and newly published key IDs during overlap, then an unknown-key denial; record that this lab does not establish immediate revocation behavior.

## Source

[Keycloak Server Administration Guide — Configuring realm keys; Rotating keys](https://www.keycloak.org/docs/latest/server_admin/)

## Acceptance

Positive controls for both published keys and a negative unknown-key control exercise JWKS retrieval.

## Command and evidence

`./mvnw -Dtest=OidcContractLabInfrastructureTest#rotationAcceptsNewKidAndRejectsUnknownSigningKey test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-oidc-06.json`. This is not learner completion until genuinely attempted and independently reviewed.
