# Day 41: Clock-controlled transition

> Project-authored teaching toy; not official course content and not learner completion evidence. Read this before the day's implementation task. All main prerequisite links refer to immutable main `80cb3cd` and commands run only in that main checkout.

## Worked example

Toy scheduled state effective Dec10 is unchanged when fixed clock is Dec09; becomes CANCELLED v3 when fixed clock is Dec10; Dec11 applied to the original scheduled value also yields CANCELLED v3; reapplying to cancelled state changes nothing. Use `Clock.fixed(..., UTC)` so the result does not depend on workstation date.

## Transfer task

Transfer: implement both existing `applyDueCancellation` overloads in Day41: first the pure policy transition, then `applyDueCancellation(DataSource, PolicySnapshot)` using the same PostgreSQL state/event transaction boundary taught and verified on Day35. The persisted overload is Day41 work and a Day44 prerequisite; it must not be deferred to integration or called by the pure transition selector. Run the pure selector and `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day41PersistedDueCancellationUpdatesPolicyAndEventAtomically test`; the latter checks before-date no-op, due-date state plus event, and idempotent retry using a disposable embedded PostgreSQL fixture. Repeated local invocation safety does not prove cross-service exactly-once processing.

## Readiness check

Before editing, predict one changed input and its observable result. If the prediction is wrong, repeat this toy trace inside the study block and carry forward the learner seam; do not add time or infer completion from this guide.
