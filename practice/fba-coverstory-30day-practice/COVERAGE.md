# Coverage and verification

## Scope materialized

- Thirty FBA assignment records for Front-End Engineer II — FBA, job `10575321`, dated 17 October–15 November 2026. SDE/Seller Springboard content is excluded. The source and exact prompt/case provenance are recorded in `fba/SPRINT-MANIFEST.json`.
- Thirty paired CoverStory practice cards `CS90-01`–`CS90-30` / `CAP-602`–`CAP-631`, with a 90-minute CoverStory timebox (55 minutes repository work/tests, 30 minutes AWS pathway study, 5 minutes design/evidence) and exact links to saved primary contracts CAP-40 through CAP-58. The FBA block is 60 minutes, for 150 planned minutes total.
- Each CoverStory source contract is paired across two consecutive days: first implement the bounded learner seam, then add an adversarial learner-owned test. All guides name the exact source guide, commands, and test paths. The instructor/oracle test remains separate and must not be edited to force a pass.
- A forward transition starts 16 November at 180 minutes/day. It sets a review gate for the first fifteen units before retiring their future recurrence, then schedules fifteen specific additive saved contracts CAP-59–CAP-73. Fifteen individual guides specify practical repository work, existing tests, and paths.
- Progress rows are all `NOT_STARTED`. No completion or learner mastery is claimed. The published source worktree, main, existing CoverStory branch, and Amazon interview branch remain untouched.

## Verified in this executor

- `python3 practice/fba-coverstory-30day-practice/fba/scripts/validate_course.py` — 30 date/source/role mappings, 30 daily guides, source and test paths, 30 unstarted FBA progress entries, and 15 concrete transition units passed.
- FBA `npm ci` succeeded from the copied lockfile. `npm test` passed 12/12 component tests; `npm run build` passed. `DAY=1 npm run test:contracts` reached two expected Day 1 learner TODO failures.
- CAP44/CAP53/CAP56 instructor Python infrastructure checks passed 6/6. Their separate learner acceptance suites each reached four expected TODO errors.
- Maven instructor test selection covering CAP40, 42, 46, 48, 50, 52, 54, 55, 57, 58, 165, and 166 passed 37 tests with 0 failures/errors and 1 skipped (the explicitly opt-in CAP166 Chromium flow). CAP57 exercises the test-only publication runtime path. CAP166 default coverage verifies persisted HTTP/database behavior; its real browser exercise is opt-in.
- CAP40 learner acceptance is preserved and separately available; no code was written to make it pass.

The Maven run required environment-specific overrides because this container has Java 21 runtime only, no `javac`, and a read-only default Maven cache. It succeeded with Maven 3.10.0, `JAVA_TOOL_OPTIONS=--add-modules=jdk.compiler`, `-Dmaven.compiler.release=`, and source/target 21, using a temporary Maven repository/settings under `/tmp`. The ordinary `./mvnw` path is intended for a writable JDK 21 installation. The alternative source/target run is local evidence, not a replacement for standard release-mode CI.

## Not verified / limits

- Instructor checks were not run for every mapped pair and the fifteen post-transition units. Existing source scaffolds/tests do not guarantee each 90-minute timebox fits; record actual timing and readiness when attempted.
- The optional CAP166 full browser recovery flow is not included in the 37-test command; it needs `web` npm dependencies and Chromium. The learner decoder acceptance remains TODO.
- The selected AWS course entitlement/link was not authenticated or checked in this workspace. The source cards retain the official objective and selected lesson references, but access may be pending. No account, credential, cloud resource, purchase, or course-completion claim was created.
- The repository has no CI workflow for this new branch yet. No deployment, merge, or external provider action was performed.
