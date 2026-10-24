# Day 38 — 2026-11-30: Append immutable endorsement

## Contract

**Learner-owned seam:** `PolicyLifecycleService.endorse`
**Status:** Contract defined; learner implementation not attempted.

**Prospective criteria version:** `fde-coursework-v2`; existing learner status and historical attempts remain unchanged.

### Expected behavior

Append a new immutable version and retain prior snapshots; GET history is chronological; each accepted change increments version exactly once.

## Acceptance and expected red state

Fixed acceptance tests are learner-owned behavior checks; they must assert the cases above and must not be weakened to make the starter green. Until the TODO is implemented, a valid operation is expected to fail at the named TODO. Invalid-input and non-mutation cases should be asserted independently where fixtures permit. Record the exact command, exit code, failing assertions/errors, and TODO reached in `evidence/continuation-31-44/day-38.json`. Instructor infrastructure checks are separate and must pass. No learner completion is inferred from a contract or from infrastructure tests.

**Acceptance selector:** `src/test/java/assignments/reboot/PolicyLifecycleAcceptanceTest.java#day38EndorsementAppendsAndPreservesPriorHistoryInEffectiveOrder`
**Infrastructure selector:** `assignments.reboot.ContinuationManifestInfrastructureTest`

**Learner acceptance command:** `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day38EndorsementAppendsAndPreservesPriorHistoryInEffectiveOrder test`
**Instructor infrastructure command:** `./mvnw -Dtest=ContinuationManifestInfrastructureTest test`

## Build / test allocation (85 minutes)

Implement only this day's seam and its fixed tests. Preserve prior completed work. Use deterministic fixture values, injected `Clock`, and isolated disposable PostgreSQL where specified. Do not add production secrets, customer data, external services, or extra infrastructure.

## Interview-first system-design mock (45 minutes; inside the 85-minute practice block)

**Prompt:** [Q16 — Messaging: ordering and replay](../../interview/question-bank.md#q16). Save the initial proposal during the 15-minute architecture phase before hints, reference answers, or AI. Use the exact [100-point rubric](../../interview/mock-rubric.md) and its 5/5/15/10/10 phases.

**Existing EAI work (40 minutes):** 40 minutes: implement the existing local endorsement path to append one immutable snapshot while preserving prior history, then run the named Day 38 selector. Use the messaging prompt only to reason about ordered durable history; do not add a broker, outbox, or StreamForge code.

**Runnable acceptance:** `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day38EndorsementAppendsAndPreservesPriorHistoryInEffectiveOrder test`. This selector may remain red at an earlier learner-owned prerequisite; record the actual first blocker. Do not weaken it or claim the fixture is learner integration.

**Deferred queue:** **S3-D38-1** — Complete the full effective-order, exactly-one-version increment, unchanged-input, and repeated-history assertions in the existing selector; retain any persistence or later-streaming work behind its original prerequisite. Day acceptance stays pending until the original full criteria pass.

**Supporting source bridge:** No additional course lesson is assigned. Within the existing 25-minute block, use the already listed PostgreSQL 18 constraints and Java SE 21 Record documentation plus the Day 38 append-history worked guide.

## Concept/course allocation (25 minutes)

**Prospective criteria: `fde-coursework-v2`.** This revision teaches the assigned design/API decision before the task. Read [day-38-append-history.md](../../continuation-31-44/worked/day-38-append-history.md) before editing. The project-authored toy is a worked example, not official course content or completion evidence.

Required course foundation: none; use the bounded authored/official bridge. Optional reference-only lessons (outside today's 25-minute scheduled path): none. Course access remains as recorded in the verified catalog; when unavailable, use the listed official source and worked guide in the same time allocation. Do not purchase/enroll or stack optional content.

Focused sources:
- [PostgreSQL 18: Constraints](https://www.postgresql.org/docs/18/ddl-constraints.html) — 5.5.3. Unique Constraints; 5.5.4. Primary Keys.
- [Java SE 21: Class Record](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Record.html) — Class Record; API Note.

**Study allocation (exactly 25:00 / 1,500 seconds):** focused official source 5:00; worked example 16:00; prerequisite prediction 4:00. Stop at the cap and carry forward; shrink implementation scope rather than adding hours. Video viewing, pauses, worked example and teach-back share this block. Viewing is not learner completion.

**Practice allocation (85 minutes):** 45 minutes for the named interview mock, then 40 minutes for this day’s bounded engineering slice: 5 minutes to confirm preconditions and state the first expected result, 25 minutes on the named learner seam, and 10 minutes for the named selector plus actual result/blocker evidence. This replaces the former full 85-minute implementation breakdown on this mock day only; the original acceptance remains required and unfinished work stays in the named deferred queue. Do not add time or mark the day complete from the mock or partial slice.

Use the same shared evidence exercise [`lifecycle-delivery-v2.json`](../templates/lifecycle-delivery-v2.json); record actual outcomes only after a real attempt, not from this guide.

**Design/API prompt:** Complete original and returned history; unchanged input; expected increment/order and changed fixture.

## Evidence allocation (10 minutes)

Record the actual command and result in `evidence/continuation-31-44/day-38.json`, plus changed paths, assistance, any blocker, and relevant before/after assertion or snapshot. Keep design reasoning in the learner-led proposal or review when assigned; no post-hoc architectural justification is required.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
