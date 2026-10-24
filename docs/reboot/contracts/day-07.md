# Day 07 — 2026-10-30: Create and expire quote version

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

## Interview-first system-design session (45 minutes inside the existing 85-minute practice block)

**Prompt:** [Q03](../interview/question-bank.md#q03) from the original question bank. Before any hint, reference answer, or AI, write your own initial proposal. Follow the 45-minute 5/5/15/10/10 format in [the mock rubric](../interview/mock-rubric.md): clarify, preserve your first proposal, work the design, inspect a failure and trade-off, then score feedback.

**Schedule:** 2026-10-30. This session replaces 45 minutes of implementation/test practice; the remaining 40 minutes stay for the day’s implementation and tests. Do not add time or mark a partial acceptance gate complete. Use the existing 10-minute evidence block for command, result, assistance, and blocker only.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 7` first. This command uses the actual Asia/Kolkata local study date; contract dates are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../dsa/guided-transfer-policy.md](../dsa/guided-transfer-policy.md) and [the DSA error log](../dsa-error-log-template.md).
