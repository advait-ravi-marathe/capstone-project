# Day 07 — 2026-11-01: Create and expire quote version

**State:** Contract defined; instructor fixture/test scaffold and learner attempt are pending. This page is a planned contract, not a test result.

**Focused official supplement:** No exact verified course lecture covers `Clock` injection or deterministic business time. Read Java 21 [`Clock.fixed(Instant, ZoneId)`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Clock.html) and [`Instant.plusSeconds`/`isBefore`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Instant.html), then use the [worked deterministic-time guide](../teaching/day-07-deterministic-time.md). Total study remains 25 minutes: 6 minutes focused reading, 15 minutes worked example, 4 minutes readiness.

## Learner seam

`QuoteVersionService`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Immutable snapshot stores fixture product version `home-secure-v1`, rate version `home-secure-training-rate-v1`, amount, premium and expiry exactly 15 minutes after injected creation time. It is valid strictly before expiry and expired at equality. Reject ineligible input and preserve quoted price if the catalog/rate fixture later changes.

## Required edge cases

One tick before/at/after expiry; fixed ID and clock; ineligible fixture; exact snapshot retained after a changed rate fixture.

## Prerequisite and isolation

Days 5–6; canned eligibility/premium; fixed Clock. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.


## Concept (25 minutes)

**Assistant-provided architecture decision (inside the existing 25-minute concept allocation):** [Inject `Clock` and create an immutable quote snapshot with an explicit expiry instant. This makes time boundaries deterministic and prevents a later version from mutating an earlier snapshot.](../assistant-architecture-notes.md#day-7-fixed-clock-quote-version). No capstone design proposal or post-hoc architecture justification is assigned.

Wall time vs elapsed time; deterministic tests [D].

## Evidence (10 minutes)

Record the fixed-clock before/equal/after prediction, the existing quote snapshot result, exact selector command/exit and assistance. A reference result is not integrated learner completion.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day07ContractTest test`

This focused test and its learner TODO seam are installed. The command was run with the other installed Day 2–7 selectors; expected TODO failures are recorded in `evidence/reboot/initial-contracts-2026-10-20.json`. It is not a passing learner result. Infrastructure checks are separate.

## Interview-first system-design session (45 minutes inside the existing 85-minute practice block)

**Prompt:** [Q03](../interview/question-bank.md#q03) from the original question bank. Before any hint, reference answer, or AI, write your own initial proposal. Follow the exact 5/5/15/10/10 phases in [the mock rubric](../interview/mock-rubric.md): requirements; capacity and constraints; initial architecture and data model (save before help); bottlenecks, reliability and scaling; trade-offs, failures and follow-ups.

**Schedule:** 2026-11-01. This session replaces 45 minutes of implementation/test practice; the remaining 40 minutes stay for the day’s implementation and tests. Do not add time or mark a partial acceptance gate complete. Use the existing 10-minute evidence block for command, result, assistance, and blocker only. For the exact 40-minute EAI slice and deferred acceptance queue, follow [Sprint 1 session scope](../interview/session-scope.md); the original day stays pending until its full acceptance passes.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
