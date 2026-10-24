# Day 35 — 2026-11-27: Atomic PostgreSQL issuance

## Contract

**Learner-owned seam:** `PolicyLifecycleService.persistIssueAtomically(dataSource, request, authenticatedOwnerId)`
**Status:** Contract defined; learner implementation not attempted.

**Prospective criteria version:** `fde-coursework-v2`; existing learner status and historical attempts remain unchanged.

### Expected behavior

In one transaction on one connection, insert policy, issue event and idempotency receipt. On conflict roll back all new rows and publish no in-memory replay receipt. On success commit first, then publish the committed snapshot for same-service replay. Verify exact fresh-read rows and replay identity with disposable embedded PostgreSQL.

## Acceptance and expected red state

Fixed acceptance tests are learner-owned behavior checks; they must assert the cases above and must not be weakened to make the starter green. Until the TODO is implemented, a valid operation is expected to fail at the named TODO. Invalid-input and non-mutation cases should be asserted independently where fixtures permit. Record the exact command, exit code, failing assertions/errors, and TODO reached in `evidence/continuation-31-44/day-35.json`. Instructor infrastructure checks are separate and must pass. No learner completion is inferred from a contract or from infrastructure tests.

**Acceptance selector:** `src/test/java/assignments/reboot/PolicyLifecycleAcceptanceTest.java#day35AtomicPostgresWriteHasOnePolicyAndOneReceipt`
**Infrastructure selector:** `assignments.reboot.ContinuationManifestInfrastructureTest`

**Learner acceptance commands:** `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day35AtomicPostgresWriteHasOnePolicyAndOneReceipt test` and `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day35ReceiptConflictRollsBackPolicyAndEvent test`
**Instructor infrastructure command:** `./mvnw -Dtest=ContinuationManifestInfrastructureTest test`

## Build / test allocation (85 minutes)

Implement only this day's seam and its fixed tests. Preserve prior completed work. Use deterministic fixture values, injected `Clock`, and isolated disposable PostgreSQL where specified. Do not add production secrets, customer data, external services, or extra infrastructure.

## Concept/course allocation (25 minutes)

**Prospective criteria: `fde-coursework-v2`.** This revision teaches the assigned design/API decision before the task. Read [day-35-atomic-write-decision.md](../../continuation-31-44/worked/day-35-atomic-write-decision.md) before editing. The project-authored toy is a worked example, not official course content or completion evidence.

Required course foundation: Step 63 - Implementing Transaction Management - 3 Things to Decide (4:38). Optional reference-only lessons (outside today's 25-minute scheduled path): Step 58 - Introduction to Transaction Management. Course access remains as recorded in the verified catalog; when unavailable, use the listed official source and worked guide in the same time allocation. Do not purchase/enroll or stack optional content.

Focused sources:
- [AWS Prescriptive Guidance: Appendix – ADR template](https://docs.aws.amazon.com/prescriptive-guidance/latest/architectural-decision-records/appendix.html) — Context; Decision; Consequences; Compliance.
- [PostgreSQL 18 tutorial](https://www.postgresql.org/docs/18/tutorial-transactions.html) — 3.4. Transactions.

**Study allocation (exactly 25:00 / 1,500 seconds):** video or tx fallback 4:38; focused ADR 4:00; worked example 12:22; prerequisite prediction 4:00. Stop at the cap and carry forward; shrink implementation scope rather than adding hours. Video viewing, pauses, worked example and teach-back share this block. Viewing is not learner completion.

**Practice allocation (85 minutes):** 10 / 55 / 20 minutes in the order and scope in the worked guide and curriculum JSON. Instructor fixture setup is supplied and is not hidden learner implementation. If readiness prerequisites are not met, record the first blocker and carry forward.

Use the same shared evidence exercise [`lifecycle-delivery-v2.json`](../templates/lifecycle-delivery-v2.json); record actual outcomes only after a real attempt, not from this guide.

**Design/API prompt:** Fresh PostgreSQL reads of exact preserved old rows plus no new policy/event after receipt conflict; success all three rows.

## Evidence allocation (10 minutes)

Record the actual command and result in `evidence/continuation-31-44/day-35.json`, plus changed paths, assistance, any blocker, and relevant before/after assertion or snapshot. Keep design reasoning in the learner-led proposal or review when assigned; no post-hoc architectural justification is required.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
