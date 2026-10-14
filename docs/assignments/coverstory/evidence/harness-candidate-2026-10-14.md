# Candidate harness receipt — 14 October 2026

This receipt describes the current local candidate on `feature/coverstory-capstone-starter`. It does not mark any learner assignment complete or ready. Preserve raw Maven reports under the ignored `target/surefire-reports/` output when reproducing.

## Instructor setup checks

- Curriculum generator + independent checker: **600 contracts, 594 primary guides, six linked components; PASS**. The independent path count is 228 explicit planned paths and 366 cards without an explicit repository path.
- Curriculum parser tests: **3 passed**.
- Python instructor infrastructure: general **2 passed**, CAP-41 **2 passed**, CoverStory fixture/setup suites **8 passed** (CAP-44/53/56/59/61/62 plus CAP-41 in the wildcard run).
- Maven default `clean test`: **18 passed, 0 failures, 0 errors, 0 skipped**. This includes the instructor Java checks and actual embedded PostgreSQL setup/fixture tests for CAP-42 and CAP-48.
- `git diff --check`: **PASS** at time of receipt generation.

## Learner acceptance suites, separate from setup

- Python CoverStory acceptance: **28 tests, 32 reported errors**, all observed traces terminate at explicit `NotImplementedError` learner seams in CAP-44/53/56/59/61/62. Subtest error totals can exceed test method totals. No discovery or fixture parsing error was observed.
- Java/PostgreSQL learner acceptance: **19 tests, 8 assertion failures and 9 errors**, all at unimplemented CAP-42/46/48/50/165 seams. CAP-42/48 intentionally return zero rows until their learner SQL TODOs are written. The CAP-50/CAP-165 invalid-input cases see the starter's `UnsupportedOperationException` TODO before learner validation can be assessed; these are not evidence that the required invalid-input behavior passes or fails.
- Existing generic ranking acceptance: **10 tests, 15 reported errors**, all at its existing CAP-44 `NotImplementedError`. This legacy suite remains separate from the CoverStory fixture suite.
- Eleven of the twelve authored families have current-candidate learner acceptance runs. CAP-40 remains `NOT_RUN` on this candidate; its earlier CAP-40 evidence is historical. No current candidate learner result is recorded as mastery.

## Remaining readiness work

All twelve authored scaffold families remain conditional. Required work includes a timed learner rehearsal, broader correct/defective witness discrimination per family, and checking each day's complete timebox and prerequisites. Phase 2's one hidden-fault experiment still requires a private fresh-fault preflight and symptom-only learner brief. The other 582 primary days remain mapped-only. Authentication/owned-journey authorization and complete business workflows remain unfinished.
