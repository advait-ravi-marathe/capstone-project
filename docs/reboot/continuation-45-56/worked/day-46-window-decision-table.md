# Day 46 — A state-and-date decision table

Independent toy rule: a library reminder opens14 calendar days before `2024-07-01`, and a stopped membership never receives it. The opening is included and the next anniversary is excluded.

| State | Date | Decision |
|---|---|---|
| ACTIVE | 2024-06-16 | TOO_EARLY |
| ACTIVE | 2024-06-17 | AVAILABLE |
| ACTIVE | 2024-06-30 | AVAILABLE |
| ACTIVE | 2024-07-01 | TERM_ENDED |
| STOPPED | each of the four dates | NOT_AVAILABLE |

The complete executable example is `RenewalTeachingReferenceTest#membershipReminderChecksStateAndBothBounds`. It checks both bounds and the unconditional status rule at every boundary. A test only on a date in the middle of the window would not prove ordering. The supplied pure function takes a `LocalDate`; a controller obtains the date from a fixed `Clock`, never the workstation clock.

Transfer: retain the existing policy eligibility enum, ISSUED window, cancellation rules, and Day45 term. Do not add instants or time zones to the `LocalDate` contract. Explain which opening-boundary and status-precedence mutants the cases reject.

Study25: 4:00 Clock/LocalDate focus,17:00 worked matrix,4:00 prediction. Practice85: 10 matrix,55 `eligibility`,20 tests. Evidence10. Official sources: Java [Clock](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Clock.html) and [LocalDate](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/LocalDate.html). Optional lesson9565624 is recovery-only, not additional required study.

Commands: `./mvnw -Plearner-tests -Dtest=PolicyRenewalAcceptanceTest#day46RenewalWindowHasFixedOpenCloseAndLifecycleBoundaries test`; `./mvnw -Dtest=RenewalTeachingReferenceTest#membershipReminderChecksStateAndBothBounds test`.
