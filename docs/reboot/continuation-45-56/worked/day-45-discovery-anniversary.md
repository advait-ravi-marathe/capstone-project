# Day 45 — Discovery example, then an annual date

Project-authored synthetic exercise; no member interview occurred. A library operator says, “Members should see their next membership date.” Ask what choice the information supports. Supplied exercise answer: “They only need a reminder of the anniversary and the fee on their current membership; this screen must not renew or take payment.”

Worked record: actor/member checks the next anniversary; technical fact is that the stored record already has activation date and current paid fee; assumption is that the reminder is informational; open question about any future renewal price is outside scope. Acceptance: show the anniversary and current fee as reference, and expose no payment or renewal action.

The independent Java reference is [`RenewalTeachingReferenceTest#membershipAnniversaryIsNot365Days`](../../../../src/test/java/assignments/teaching/RenewalTeachingReferenceTest.java). For start `2023-07-01`, `plusYears(1)` gives `2024-07-01` and a 14-day reminder opens `2024-06-17`; adding365 days gives June30 and is wrong. For leap start `2028-02-29`, the next anniversary is `2029-02-28` and the reminder opens February14. The term is `[start,next)`, so the next date belongs to the following term.

Transfer to the existing CoverStory 30-day selector: add the separate `2023-03-01 → 2024-03-01 → 2024-01-31` case. Record one goal, one unanswered question, one explicit assumption, and three observable cases in the existing delivery exercise; do not invent a real insurance rule.

Study is 4:15 LocalDate lesson (or the official fallback), 5:00 focused discovery/acceptance, 11:45 worked example, 4:00 prediction/teach-back. Practice85: 10 case record,55 `termFor`,20 tests. Evidence10: actual command/result and one explanation. Official: [LocalDate `plusYears`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/LocalDate.html).

Command: `./mvnw -Plearner-tests -Dtest=PolicyRenewalAcceptanceTest#day45AnnualTermUsesExclusiveAnniversaryAndThirtyDayWindow test`. Independent reference: `./mvnw -Dtest=RenewalTeachingReferenceTest#membershipAnniversaryIsNot365Days test`.
