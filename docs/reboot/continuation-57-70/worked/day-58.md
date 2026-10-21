# Day 58 — Follow explicit composition

**Study: 25 minutes.** Spring Boot “Profiles” and “Adding Active Profiles”; Spring “Constructor-based Dependency Injection”; Boot “Importing Additional Configuration Classes”. Trace the independent desk example before tracing the supplied Sprint 5 stage.

## Worked example

`scripts/teaching/DeskCompositionReference.java` builds a `ShelfSummary` from an explicitly supplied `Clock` and `Shelf`. It asserts object identity, then passes a second clock/shelf with different values and confirms the first result remains unchanged. Run `java scripts/teaching/DeskCompositionReference.java`; expected output is `PASS: supplied dependencies, changed assembly, first assembly unchanged`. A non-null check alone would not catch a factory that ignores its arguments.

The native trace is: stage entry → `Sprint5LocalConfiguration` → existing two-argument `LocalApplicationComposition.compose(clock, dataSource)` → lifecycle/read adapters. The same objects must reach the components. The stage explicitly imports its controllers/security and does not scan `assignments.local`; later timed-read, capacity, delivery and manual-recovery beans remain absent.

**Tradeoff:** an explicit import list makes this early slice reviewable; broad scanning would accidentally include later work. This is supplied instructor wiring, not learner framework implementation.

**Practice 85:** 15 predict dependencies, 35 draw the four-arrow trace, 20 run selectors, 15 explain the tradeoff. **Selector:** `./mvnw -Plocal-app,sprint5-local,learner-tests -Dtest=LocalApplicationAcceptanceTest#day58DedicatedEntryPointWiresClockAndDataSourceExplicitly test`.
