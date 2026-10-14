# Historical candidate harness receipt — 14 October 2026

This is a historical snapshot pinned to candidate commit `e7236826708b480f7ac1dc6e57750466882fd5cd` on `feature/coverstory-capstone-starter`. Its counts and readiness statements describe that candidate only; they are not current status. Preserve raw Maven reports under the ignored `target/surefire-reports/` output when reproducing.

## Instructor setup checks

- Curriculum generator + independent checker: **600 contracts, 594 primary guides, six linked components; PASS**. The independent path count is 228 explicit planned paths and 366 cards without an explicit repository path.
- Curriculum parser tests: **3 passed**.
- Python instructor infrastructure: general **2 passed**, CAP-41 **3 passed**, CoverStory daily fixture/setup suites **6 passed** (CAP-44/53/56/59/61/62).
- CAP-40 first-day instructor oracle: **2 passed**. Its positive witness passes exact composite keys and first-seen order; negative witnesses reject version-only identity and sorted-order defects. Maven command elapsed **4.03 seconds** once in this executor. CAP-41 fixture/oracle infrastructure: **3 passed**; elapsed **0.06 seconds**.
- CAP-54 owner/revision instructor oracle: **3 passed**, including a UUID-only authorization defect witness and cursor bounds/owner check.
- CAP-55 offline trace instructor oracle: **2 passed**. It accepts the recorded same-source order and two keys sharing partition 0; it rejects reordered offsets, split partitions, missing metadata and false live-broker claims. No Kafka runtime is used.
- CAP-57 instructor oracle: **3 passed** against embedded PostgreSQL; it verifies fixture hash integrity, immutable prepared rows, safe export, duplicate/conflict outcomes, and rollback under injected failure.
- Maven default `clean test`: **27 passed, 0 failures, 0 errors, 0 skipped**. Tests include actual embedded PostgreSQL setup/fixture checks for CAP-42/48/57 and CAP-40/54/55 instructor checks.
- `git diff --check`: **PASS** at time of receipt generation.

## Learner acceptance suites, separate from setup

- Python CoverStory acceptance: **33 tests, 38 reported errors** across the primary/component Python seams (CAP-41/44/53/56/59/61/62); all observed traces terminate at explicit `NotImplementedError` learner seams. Subtest error totals can exceed test method totals. No discovery or fixture parsing error was observed.
- Java/PostgreSQL learner acceptance: **40 tests, 12 assertion failures and 26 errors** across CAP-40/42/46/48/50/54/55/57/165, all at unimplemented learner seams; the four CAP-57 tests reached its explicit TODO after PostgreSQL setup succeeded. CAP-42/48 intentionally return zero rows until their learner SQL TODOs are written. CAP-40/50/54 invalid-input cases reach the starter `UnsupportedOperationException` TODO before learner validation can be assessed; these do not establish the required invalid-input behavior.
- Existing generic ranking acceptance: **10 tests, 15 reported errors**, all at its existing CAP-44 `NotImplementedError`. This legacy suite remains separate from the CoverStory fixture suite.
- At this historical candidate, all fifteen then-authored primary families had learner acceptance runs, and the linked CAP-41 suite had also run. Every behavior result was an expected TODO stop, not mastery or a pass. The recorded CAP-40 instructor setup run took 4.03 seconds; learner implementation duration was unmeasured. No 95-minute simulated wait was run.

## Remaining readiness work

At the pinned historical candidate, all fifteen then-authored scaffold families were considered conditional. The current checkout has since added more instructor scaffolds and an actual public catalog route. The 4.03-second instructor run still does not estimate learner coding time. Phase 2's one hidden-fault experiment still requires a private fresh-fault preflight and symptom-only learner brief. The product's actual authentication/owned-journey authorization and complete business workflows remain unfinished.
