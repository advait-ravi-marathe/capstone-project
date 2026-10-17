# Day 09 · 2028-06-26 · CAP-640 / CSM14-09

**Board contract:** [CAP-640](https://dwait-to-adwait.youtrack.cloud/issue/CAP-640) — CSM14-09 · 26 Jun 2028 — Close rebase authorization and publication races (180m)

**Status:** instructor scaffold and synthetic checks are runnable; assessed learner behavior remains TODO. Readiness `NOT_READY`, learner evidence `NOT_RUN`.

**Budget:** 125 minutes practical repository work + 30 AWS + 15 DSA + 10 evidence/design = 180 minutes. Sunday no-DSM: `false`.

**Dependencies:** CSM14-06, CSM14-08, CAP-259, CAP-187

## Contract focus

Strengthen the acceptance boundary with the existing current-authorization generation/public access guard and execute a deterministic revocation-versus-accept schedule. A previously authorized preview must not grant later mutation rights. Use the established same-database serialization/guard mechanism rather than holding a client-side flag. Extend the UI regression to ensure late private refresh responses cannot populate another signed-in owner’s view.

## Synthetic fixture

A is authorized at authorization generation 4 and receives a preview. The prepared schedule revokes the relevant access at generation 5 before acceptance reaches its commit decision. Another schedule keeps authorization at 4 and succeeds. A late response from A then arrives after an existing session switch to B. Only synthetic owner/source markers are present.

## Acceptance checklist

- [ ] When revocation is ordered before the acceptance decision, no new checkpoint/audit domain effect or private response is produced for the revoked actor.
- [ ] When acceptance serializes before a later revocation, the receipt describes that precise order; the subsequent revoked read is denied. No impossible retroactive rollback guarantee is claimed.
- [ ] An unchanged-authority control case can still succeed; the fix is not blanket denial.
- [ ] B never sees A’s branch/operation identifiers or private selection context from a late response; public catalog data remains separately scoped.
- [ ] The regression records the actual barrier/transaction schedule and server enforcement point; client cancellation alone is insufficient.

## Architecture/evidence

Record a happens-before diagram with the current-authorization check and transaction decision. Explain the limited, testable revocation guarantee and the alternative behavior if no serializable authoritative guard is available.

## Scope and provenance

This is a new mutation authorization race over branch acceptance, rather than repeating read-only explorer cache/authorization-generation mocks.

Structured contract hash: `060862c244b93483712a8621b54f56e39d409d799b898981f9c18292585b5b1f`. Proposed paths are mapped in `IMPLEMENTATION-PATH-MAP.json`; no learner completion or mastery is implied.

## Commands

Browser owner-switch acceptance: `node web/scripts/csm14-learner-acceptance.mjs CAP-640` (asserts current-owner delivery and rejects prior-owner and signed-out private responses). It currently fails the behavioral assertion at the preserved learner seam.

Instructor fixture/schema harness: `./mvnw -Dtest=CSM14ContractInfrastructureTest,CSM14CheckpointSchemaIntegrationTest test`
Learner acceptance: `./mvnw -Plearner-tests -Dtest=CSM14AcceptanceTest#cap640_revocationAtSerializedDecisionDeniesAndLateOwnerResponseIsDropped test`
