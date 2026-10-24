# Day 73 — 2027-01-04: Implement immediate two-permit preview admission

## Contract

**Learner-owned seam:** `PreviewAdmission.tryEnter`
**Status:** Contract defined; learner work not attempted.

### Expected behavior

Admit at most two concurrent preview callers. `tryEnter()` returns a lease on acquisition and `Optional.empty()` immediately when full. It never blocks or queues. Each successful lease owns one release.

### Acceptance and expected TODO state

The selector checks two distinct accepted leases and an immediately rejected third attempt while both permits are occupied. It does not test permit release; closing/releasing leases is the separate Day 74 contract. The expected learner TODO is in `PreviewAdmission.tryEnter`; do not substitute a pre-solved implementation.

**Learner acceptance test:** `src/test/java/assignments/renewal/PreviewAdmissionAcceptanceTest.java`
**Learner command:** `./mvnw -Plearner-tests -Dtest=PreviewAdmissionAcceptanceTest test`
**Infrastructure test:** `src/test/java/assignments/renewal/Day71To84InfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=Day71To84InfrastructureTest test`

## Practice allocation (85 minutes)

45 minutes are reserved for the Day 73 interview mock below; the remaining 40 minutes are the bounded engineering slice. Use the existing `PreviewAdmission` semaphore and `PreviewLease` boundary. Do not add a queue, wait, new HTTP route, or extra feature.

**Bounded engineering slice (40 minutes):** 5 minutes check Day 72 validation and predict the full-gate result; 25 minutes implement/test only immediate acquisition and lease return; 10 minutes run the named selector and record its first real failure or result. The complete selector remains the completion gate.

**Deferred work:** **S6-D73-MOCK-A** — Any unmet original admission acceptance remains open. Resume from the actual failing assertion; a mock or partial slice is not completion.

## Concept/course allocation (25 minutes)

**Learning guidance:** The saved catalog lists `3 Questions - ThreadLocal vs Semaphore vs CountDownLatch` (6:23), in28Minutes Official, Java Programming for Complete Beginners (course 1535678; access unconfirmed). It is partial conceptual support, not a required entitlement. [Java SE 21 Semaphore `tryAcquire`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Semaphore.html) is the API reference. Course metadata is saved evidence; `videos_watched` remains false. Study, pauses, note and retrieval all fit within 25 minutes; do not purchase or enroll.

**Assistant-provided architecture note:** [Day 73](../assistant-architecture-notes.md#day-73). It explains the exact nonblocking `tryAcquire` and lease boundary. No capstone design proposal or retrospective justification is assigned.

## Interview system-design mock (45 minutes within practice)

**Question:** [Q04 — Rate limiter: one client and one window](../../interview/question-bank.md#q04). Save a proposal before hints or references for this interview question only. Use [the 5/5/15/10/10 phases and 100-point rubric](../../interview/mock-rubric.md). The mock does not choose or justify the capstone architecture.

## Evidence allocation (10 minutes)

Record exact command, exit status, observed assertion, learner-owned paths, assistance, and first blocker in `evidence/continuation-71-84/day-73.json`. Do not mark the implementation complete from a plan or reference.

## Separate DSA track

Daily DSA assignments, tests, reviews, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract has no DSA assignment.
