# Day 13 · 2028-06-30 · CAP-644 / CSM14-13

**Board contract:** [CAP-644](https://dwait-to-adwait.youtrack.cloud/issue/CAP-644) — CSM14-13 · 30 Jun 2028 — Implement an independent reordered-branch variation (180m)

**Status:** instructor scaffold and synthetic checks are runnable; assessed learner behavior remains TODO. Readiness `NOT_READY`, learner evidence `NOT_RUN`.

**Budget:** 125 minutes practical repository work + 30 AWS + 15 DSA + 10 evidence/design = 180 minutes. Sunday no-DSM: `false`.

**Dependencies:** CSM14-02, CSM14-07, CSM14-12

## Contract focus

Solve a fresh narrow requirement independently: a local branch may explicitly reorder the three selected products while upstream updates their versions; rebase must preserve that intentional order without losing per-slot conflicts or attaching a decision to the wrong product. The instructor supplies only the public contract, input fixture and review constraints, not a reducer, solution patch or diagnosis. Implement the smallest Java/React ordering seam and its tests using existing stable slot identities.

## Synthetic fixture

Base order is s1(H1/v1), s2(H2/v1), s3(H3/v1). The local branch order is s3,s1,s2; upstream supplies H1/v2 and a divergent H2 version while returning its map in reverse insertion order. Expected local order remains s3,s1,s2, H1 can refresh, and H2 retains its own explicit conflict. Before assignment, the instructor substitutes fresh version IDs/digests and one unseen legal order; the learner cannot claim a rehearsed exact fixture as unseen.

## Acceptance checklist

- [ ] The implemented result preserves explicit local order and binds each conflict/decision to the correct stable slot and exact source versions.
- [ ] Upstream iteration order cannot silently reorder selections or transfer a resolution to another product.
- [ ] A malformed order with repeated/missing slot IDs is rejected with no partial save; valid selection bounds still hold.
- [ ] Run the focused Java and React regression against the same fixture and verify the accepted persisted order through the existing API.
- [ ] Record actual independent work, sources and hints; generated/copied solution code or a disclosed implementation counts as guided practice.

## Architecture/evidence

Explain why identity and order are different dimensions of the rebase contract. Demonstrate one failed alternative, such as positional conflict binding, using the actual counterexample and state the extra invariants the change introduces.

## Scope and provenance

This is a new cross-layer order-intent requirement on the sprint feature, distinct from the earlier source-map-order/full-stack variation.

Structured contract hash: `98beb85cc7dbfca7a21d585d3c33b3190ff941ce28afee85d042dccb8914fdc0`. Proposed paths are mapped in `IMPLEMENTATION-PATH-MAP.json`; no learner completion or mastery is implied.

## Commands

Instructor fixture/schema harness: `./mvnw -Dtest=CSM14ContractInfrastructureTest,CSM14CheckpointSchemaIntegrationTest test`
Learner acceptance: `./mvnw -Plearner-tests -Dtest=CSM14AcceptanceTest#cap644_conflictDecisionFollowsStableSlotAndExplicitLocalOrder test`
