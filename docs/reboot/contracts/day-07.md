# Day 07 — 2026-10-26: Create and expire quote version

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

**Design/readiness prompt (inside the existing evidence block):** Explain why time is injected instead of waiting, name the captured price that remains fixed, and distinguish business wall time from elapsed-time measurement. No architecture drawing is added.

## Concept (25 minutes)

Wall time vs elapsed time; deterministic tests [D].

## Evidence (10 minutes)

Record the fixed-clock before/equal/after prediction, the existing quote snapshot result, exact selector command/exit and assistance. A reference result is not integrated learner completion. DSA notes remain separate.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day07ContractTest test`

This focused test and its learner TODO seam are installed. The command was run with the other installed Day 2–7 selectors; expected TODO failures are recorded in `evidence/reboot/initial-contracts-2026-10-20.json`. It is not a passing learner result. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**New pattern anchor:** LeetCode 49 Group Anagrams — grouping key. Learn the pattern for 25 minutes, solve independently for 20 minutes, and spend 15 minutes on same-day D recall plus the error log. Record the actual first-attempt date; schedule D+3, D+7 and D+21 reviews from that date. Planned dates in the manifest are forecasts only. A failed prerequisite/review displaces the next new anchor; do not add overtime. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** Why inject time rather than wait? Which captured value stays fixed when the catalog changes?

**Observable evidence:** Predict before/equal/after with a fixed clock, then explain the existing 15-minute snapshot fixture and its captured values.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [Official expectations](../interview-evidence-overlay.md#official-expectations) (official); [A3](../interview-evidence-overlay.md#a3) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.
