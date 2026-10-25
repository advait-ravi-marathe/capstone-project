# Calendar study day 81 — GAP-OIDC-06: Test key rotation and overlap

**Date:** 2027-01-14  
**Budget:** 25/85/10 minutes  
**Status:** Not attempted; planning assignment only.

## Assistant-provided architecture

Keep this experiment in an isolated, stateless Spring Security resource-server profile. Spring verifies issuer, time, audience and JWKS signature; a small tenant/scope policy runs only after authentication. The synthetic issuer binds to loopback, preserving the existing cookie-session application. The assistant supplies this architecture; the learner implements and tests its assigned policy seam. No capstone design proposal or post-hoc design defense is assigned.

## Task

Use only the local synthetic issuer. Verify the previously active and newly published key IDs during overlap, then an unknown-key denial; record that this lab does not establish immediate revocation behavior.

## Source

[Keycloak Server Administration Guide — Configuring realm keys; Rotating keys](https://www.keycloak.org/docs/latest/server_admin/)

## Acceptance

Positive controls for both published keys and a negative unknown-key control exercise JWKS retrieval.

## Command and evidence

`./mvnw -Dtest=OidcContractLabInfrastructureTest#rotationAcceptsNewKidAndRejectsUnknownSigningKey test`

Record the actual command, outcome, changed paths, blockers, and exact evidence at `docs/reboot/evidence/gap-oidc-06.json`. This is not learner completion until genuinely attempted and independently reviewed.
