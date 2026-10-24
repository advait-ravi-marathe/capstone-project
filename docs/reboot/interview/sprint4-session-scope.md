# Sprint 4 interview sessions and engineering slices

Sprint 3 ends at Day 42. Sprint 4 covers Days 43–56 (5–18 December 2026).
Days 43–44 remain stored in the `continuation-31-44` folder for continuity, but
are the first two Sprint 4 days. Four dedicated 45-minute mocks are
placed in the remaining Sprint 4 window: Days 45 and 49 in the first seven-day
practice week, then Days 52 and 56 in the second. Each mock occupies 45 minutes
of the existing 85-minute practice block and leaves a bounded 40-minute task
slice (5 minutes preconditions/prediction, 25 minutes learner seam or bounded
observation, 10 minutes named selector and result/blocker evidence). The full
120-minute engineering day remains 25 study, 85 practice, and 10 evidence.

For every mock, the learner saves an initial proposal before hints, reference
answers, or AI. The mock phases are Requirements (5), Capacity and constraints
(5), Initial architecture and data model (15), Bottlenecks, reliability,
scaling (10), and Trade-offs, failures, follow-ups (10). Use the established
[100-point rubric](mock-rubric.md). The mock is a design-transfer exercise;
it does not add product features, mark a day complete, or substitute for the
existing acceptance tests. Use each day's already-recorded course foundation
and official documentation inside its 25-minute study cap. Optional or
unconfirmed course items remain replacement-only; no purchase or enrollment.

| Day/date | Interview prompt and patterns | Bounded engineering slice | Acceptance and unfinished work |
|---|---|---|---|
| 45 · 2026-12-07 | [Q03 — URL expiry and deterministic time](question-bank.md#q03); [expiry and retention](pattern-library.md#expiry-and-retention), [deterministic time](pattern-library.md#deterministic-time). Transfer the exclusive-end and leap-date reasoning to a different system. | Confirm actual Day 44 readiness, implement only `PolicyRenewalPreviewPolicy.termFor`, then run the existing Day 45 selector. | `./mvnw -Plearner-tests -Dtest=PolicyRenewalAcceptanceTest#day45AnnualTermUsesExclusiveAnniversaryAndThirtyDayWindow test`. **S4-D45-1** retains the full ordinary/leap/March 1 and 30-day boundary acceptance; partial work is not completion. |
| 49 · 2026-12-11 | [Q07 — API gateway: route and protect](question-bank.md#q07); [gateway boundary](pattern-library.md#gateway-boundary), [ownership boundary](pattern-library.md#ownership-boundary), [HTTP boundary](pattern-library.md#http-boundary). | Implement only `PolicyRenewalPreviewController.authenticatedOwner` and run the named Principal/forgery selector. | `./mvnw -Plearner-tests -Dtest=PolicyRenewalControllerAcceptanceTest#day49OwnerComesOnlyFromPrincipalAndUnauthenticatedDoesNotRead+day49MockMvcPassesPrincipalRatherThanForgedQueryOrHeaderToTheReadStore test`. **S4-D49-1** keeps the 401/no-read and query/header-forgery cases open until their actual selector evidence exists. |
| 52 · 2026-12-14 | [Q14 — Booking: retry after a lost response](question-bank.md#q14); [frontend state machine](pattern-library.md#frontend-state-machine), [idempotency](pattern-library.md#idempotency), [HTTP boundary](pattern-library.md#http-boundary). Transfer the state and intent distinction to the read-only preview UI; do not add a write action. | Inspect the existing props, implement only `PolicyRenewalPreviewPanel` reference rendering, and run its named learner selector. | `npm --prefix frontend run test:learner -- src/__tests__/PolicyRenewalPreviewPanel.test.tsx -t "reference-only"`. **S4-D52-1** retains every approved-field, changed-fixture, and no-mutation-control requirement; this is component evidence, not browser/API/database integration. |
| 56 · 2026-12-18 | [Q12 — Scheduler: missed or duplicate execution](question-bank.md#q12); [scheduled work](pattern-library.md#scheduled-work), [idempotency](pattern-library.md#idempotency), [resilience](pattern-library.md#resilience). Use the prompt only to discuss recovery boundaries; do not add a scheduler to CoverStory. | Confirm actual Day 55 readiness and fixed DB/clock prerequisites, run the existing restart selector, and capture the first checkpoint/result. This 40-minute slice is bounded observation/debug work. | `./mvnw -Plearner-tests -Dtest=RenewalPreviewTransportInfrastructureTest#day56PreviewReadIsStableAcrossRestartAndDoesNotWrite test`. **S4-D56-1** retains all live-context and post-close snapshots, two successful semantic reads, and same database, seeded once before the first context and not reseeded between contexts. Do not infer packaged-JAR or crash-durability proof. |

## Prerequisites and open work

Each daily contract retains its original readiness gate. Day 45 requires actual
Day 44 learner evidence; later tasks require their stated preceding dates and
real evidence. An instructor witness, course playback, or interview mock cannot
satisfy a learner prerequisite. Sprint 3 queue item **S3-D42-2** remains open
and prerequisite-gated until its actual endorsement/reschedule write boundary,
Day 41 persisted transition, retries, and complete history snapshots are
verified. Sprint 4 does not claim or close it.

The four 40-minute slices are explicit reductions of that day's available
implementation/testing time. The original acceptance remains intact; its
unfinished portion is named above and remains open. If a prerequisite fails or
the allotted slice ends, record the first blocker and stop within the 85-minute
practice cap. Do not add overtime or replace a learner result with a mock,
worked guide, infrastructure smoke, or another day’s passing test.

## Task-first proposals on non-mock days

Each proposal takes 5 minutes before hints, worked answers, or AI; the remaining 80 minutes stay with that day’s existing acceptance. These are preparation prompts, not extra mocks or completion evidence. The 25-minute study and 10-minute evidence allocations remain unchanged.

| Day/date | Saved pre-work prompt | Existing worked guide | Exact acceptance selector(s) |
|---|---|---|---|
| 43 · 2026-12-05 | Before implementation, list the exact allowed servicing fields and the loading, empty, 401 and 404 states | [day-43-response-state-ui.md](../continuation-31-44/worked/day-43-response-state-ui.md) | `npm --prefix frontend run test:learner -- src/__tests__/Day43PolicyServicingPanel.test.tsx`<br>`./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day43ServicingProjectionOmitsOwnerJourneyAndQuoteIds test` |
| 44 · 2026-12-06 | Before running the harness, make an expected-versus-observed table for the learner browser/API/database path, list the prerequisites that are actually evidenced, and name the first selector you will run. Do not assume a prior gate passed | [day-44-observed-uat-handover.md](../continuation-31-44/worked/day-44-observed-uat-handover.md) | `./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day44LifecycleIntegrationSeamMustVerifyReadback test`<br>`./mvnw -Plearner-tests -Dtest=Day44BrowserTransportInfrastructureTest#realBrowserDrivesPolicyLifecycleApiThroughSpringAndPostgres test`<br>`./mvnw -Plearner-tests -Dtest=Day44BrowserTransportInfrastructureTest#realBrowserObservesServicingFailureRecoveryAndRestart test`<br>`./mvnw -Plearner-tests -Dtest=PolicyLifecycleAcceptanceTest#day44ReadbackRejectsMissingPolicyIncompleteHistoryAndStatusVersionMismatch test` |
| 46 · 2026-12-08 | Write the open/closed date-boundary outcomes for each lifecycle status before editing eligibility | [day-46-window-decision-table.md](../continuation-45-56/worked/day-46-window-decision-table.md) | `./mvnw -Plearner-tests -Dtest=PolicyRenewalAcceptanceTest#day46RenewalWindowHasFixedOpenCloseAndLifecycleBoundaries test` |
| 47 · 2026-12-09 | List the immutable source fields that must be copied, the one permitted derived date, and the fields that must remain absent before changing the preview projection | [day-47-reference-projection-decision.md](../continuation-45-56/worked/day-47-reference-projection-decision.md) | `./mvnw -Plearner-tests -Dtest=PolicyRenewalAcceptanceTest#day47PreviewCopiesCurrentSnapshotAndMakesNoRenewalPriceOrCommitment test` |
| 48 · 2026-12-10 | Write the two SQL predicates, expected owned/foreign/absent results, and the full tables that must remain unchanged before editing the JDBC read | [day-48-scoped-jdbc-observation.md](../continuation-45-56/worked/day-48-scoped-jdbc-observation.md) | `./mvnw -Plearner-tests -Dtest=PolicyRenewalRepositoryAcceptanceTest#day48ReadIsOwnerScopedAndReturnsOnlyRenewalInputsWithoutWrites test` |
| 50 · 2026-12-12 | Map missing, foreign and each ineligible outcome to its exact status/code before editing the controller | [day-50-unavailable-response-table.md](../continuation-45-56/worked/day-50-unavailable-response-table.md) | `./mvnw -Plearner-tests -Dtest=PolicyRenewalControllerAcceptanceTest#day50EndpointConcealsMissingAndForeignRowsAndUsesStableConflictCodes+day50FoundIneligibleGetMaps409WithoutCallingAvailablePreviewBuilder test` |
| 51 · 2026-12-13 | List the exact approved serialized keys, values and JSON types, then list one private and one mutation field that must be absent before changing the response adapter | [day-51-serialized-public-projection.md](../continuation-45-56/worked/day-51-serialized-public-projection.md) | `./mvnw -Plearner-tests -Dtest=PolicyRenewalControllerAcceptanceTest#day51PublicPreviewIsMinimalAndContainsNoPrivateOrMutationFields test`<br>`./mvnw -Plearner-tests -Dtest=PolicyRenewalControllerAcceptanceTest#day51SerializedPreviewHasExactlyApprovedFieldsAndNumericPremium test` |
| 53 · 2026-12-15 | Sketch the existing loading, unavailable, ready and refresh transitions | [day-53-accessible-read-refresh.md](../continuation-45-56/worked/day-53-accessible-read-refresh.md) | `npm --prefix frontend run test:learner -- src/__tests__/PolicyRenewalPreviewPanel.test.tsx -t "explicit|refresh"` |
| 54 · 2026-12-16 | List owner/foreign/absent/anonymous response expectations and the three ordered database snapshots to compare after every request before running the PostgreSQL acceptance | [day-54-complete-database-observation.md](../continuation-45-56/worked/day-54-complete-database-observation.md) | `./mvnw -Plearner-tests -Dtest=PolicyRenewalPostgresAcceptanceTest#day54OwnerScopedEndpointReadsPostgresAndLeavesPolicyRowsUnchanged test` |
| 55 · 2026-12-17 | Write the expected browser request, status, approved response fields and rendered values for both fixtures | [day-55-observed-preview-demo.md](../continuation-45-56/worked/day-55-observed-preview-demo.md) | `./mvnw -Plearner-tests -Dtest=RenewalPreviewTransportInfrastructureTest#day55RealBrowserLoadsOwnerScopedPreviewFromSpringAndPostgres test` |

Course metadata and focused official sources remain in each contract and export; optional/unconfirmed course material stays replacement-only. Prerequisites are unchanged, so a blocked learner records the first blocker and leaves acceptance open.
