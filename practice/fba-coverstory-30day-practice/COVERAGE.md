# Coverage and verification

## Scope materialized

- Thirty FBA assignment records for Front-End Engineer II — FBA, job `10575321`, dated 17 October–15 November 2026. SDE/Seller Springboard content is excluded. The source and exact prompt/case provenance are recorded in `fba/SPRINT-MANIFEST.json`.
- Thirty paired CoverStory practice cards `CS90-01`–`CS90-30` / `CAP-602`–`CAP-631`, with a 90-minute CoverStory timebox (55 minutes repository work/tests, 30 minutes AWS pathway study, 5 minutes design/evidence) and exact links to saved primary contracts CAP-40 through CAP-58. The FBA block is 90 minutes, including 30 minutes of task-specific component and adversarial testing, for 180 planned minutes total.
- Each CoverStory source contract is paired across two consecutive days: first implement the bounded learner seam, then add an adversarial learner-owned test. All guides name the exact source guide, commands, and test paths. The instructor/oracle test remains separate and must not be edited to force a pass.
- A forward transition starts 16 November at 180 minutes/day. It sets a review gate for the first fifteen units before retiring their future recurrence, then continues the original saved roadmap in source order from unit 16 (CAP-59), with the original daily sequence represented through unit 35 and inserted primary cards CAP-167/168/169. Fifteen new proposed cards CAP-632–646 are separately appended after original unit 594; they are not saved contracts and do not replace continuation work.
- Progress rows are all `NOT_STARTED`. No completion or learner mastery is claimed. The published source worktree, main, existing CoverStory branch, and Amazon interview branch remain untouched.

## Verified in this executor

- `python3 practice/fba-coverstory-30day-practice/fba/scripts/validate_course.py` — 30 date/source/role mappings, 30 daily guides, actual learner seam and test paths, 30 unstarted FBA progress entries, source-unit continuation order, and the separate append-only proposal passed.
- FBA `npm ci` succeeded from the copied lockfile. After the timebox expansion, `npm test` passed 12/12 UI-shell interaction checks; these use injected functions for several learner behaviors and do not establish learner logic. `npm run build` passed. The 30 additional test instructions use the authoritative FBA fixtures and do not change source cases or learner TODO logic. Day-specific contract commands still execute the actual TODO learner functions.
- CAP-166 web dependencies were installed from the existing lockfile using a `/tmp` npm cache. `npm run test:catalog-acceptance` reached `TODO(CAP-166)` in the actual `decodeCatalogEnvelope` learner seam. The separate `test:catalog-fixture` instructor smoke is not learner acceptance.
- CAP44/CAP53/CAP56 instructor Python infrastructure checks passed 6/6. Their separate learner acceptance suites each reached four expected TODO errors.
- Maven instructor test selection covering CAP40, 42, 46, 48, 50, 52, 54, 55, 57, 58, 165, and 166 passed 37 tests with 0 failures/errors and 1 skipped (the explicitly opt-in CAP166 Chromium flow). CAP57 exercises the test-only publication runtime path. CAP166 default coverage verifies persisted HTTP/database behavior; its real browser exercise is opt-in.
- CAP40 learner acceptance is preserved and separately available; no code was written to make it pass.

The Maven run required environment-specific overrides because this container has Java 21 runtime only, no `javac`, and a read-only default Maven cache. It succeeded with Maven 3.10.0, `JAVA_TOOL_OPTIONS=--add-modules=jdk.compiler`, `-Dmaven.compiler.release=`, and source/target 21, using a temporary Maven repository/settings under `/tmp`. The ordinary `./mvnw` path is intended for a writable JDK 21 installation. The alternative source/target run is local evidence, not a replacement for standard release-mode CI.

## Not verified / limits

- Instructor checks were not run for every mapped pair or every continuation day. The 15 appended CAP-632–646 cards are proposals, with proposed paths not yet scaffolded or runnable. Existing source scaffolds/tests do not guarantee each 90-minute timebox fits; record actual timing and readiness when attempted.
- CAP166 full browser recovery is required saved acceptance, separate from the focused decoder contract. The learner-mode browser run was attempted with `/usr/bin/chromium` and disposable PostgreSQL; it reached the explicit `TODO(CAP-166)` decoder failure before the outage/restart assertions. Therefore the full recovery acceptance remains NOT_YET_VERIFIED (not waived or marked complete); rerun after the learner implements the decoder. If a later executor lacks Chromium, mark that run BLOCKED with its environment error.
- The selected AWS course entitlement/link was not authenticated or checked in this workspace. The source cards retain the official objective and selected lesson references, but access may be pending. No account, credential, cloud resource, purchase, or course-completion claim was created.
- The repository has no CI workflow for this new branch yet. No deployment, merge, or external provider action was performed.
