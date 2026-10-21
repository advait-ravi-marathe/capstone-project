# Day 67 — 2026-12-25: Inspect the disposable packaged runtime lifecycle

## Contract

**Instructor-provided scaffold to inspect:** `scripts/package-local-demo.sh and scripts/local-demo.sh`
**Status:** Scripts and runtime are instructor scaffolding; learner run/inspection evidence is pending.
**Time:** 180/195/210 minutes by DSA tier (60/75/90 DSA + unchanged 120 engineering) (85 implementation/testing, 25 concept/course, 10 evidence). No mandatory setup work.

### Expected behavior

The documented workflow builds the root UI into the executable JAR, starts a loopback-only app with a unique, disposable PostgreSQL container, and prints the health-checkable URL. The database uses tmpfs and is automatically destroyed when the launcher exits or is interrupted; there is no persistence mode and this is not a user-database reset. The separate guarded fixture-reset script applies only to an explicitly confirmed database ending in `_test`.

## Acceptance and expected TODO state

The instructor scaffold-verification selector is installed and passed; that result does not establish learner completion.

**Acceptance test:** `src/test/java/assignments/local/LocalPackageInfrastructureTest.java`
**Acceptance selector:** `day67PackageIncludesNormalUiAndNoTestRoutes`
**Scaffold verification command:** `./mvnw -Plearner-tests -Dtest=LocalPackageInfrastructureTest#day67PackageIncludesNormalUiAndNoTestRoutes test`
**Instructor infrastructure test:** `src/test/java/assignments/local/LocalApplicationManifestInfrastructureTest.java`
**Instructor command:** `./mvnw -Dtest=LocalApplicationManifestInfrastructureTest test`
## Learner task — explain/debug acceptance

Run the package and local-demo scripts; inspect the JAR root entry and routes, verify loopback health/session behavior, then stop the unique ephemeral demo and confirm its disposable database is removed. There is no persistent local mode.

Capture the requested trace and explanation in the day evidence file; use `learner_changed_paths: []` when this inspection task makes no source changes.

## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**

- Required foundation: [Master Spring Boot 3 & Spring Framework 6 with Java](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/) — in28Minutes Official; access `confirmed_owned`. **Step 09 - Get Production Ready with Spring Boot - 1 - Profiles**, section **Getting Started with Spring Boot** (7:05, item `35019912`).

**Study sequence (seconds; total 1500):**
- Profiles foundation or retrieval: 425 seconds.
- official packaging focus: 300 seconds.
- complete artifact/process evidence example: 480 seconds.
- prediction and cleanup boundary: 295 seconds.

**Official references:**
- [Spring Boot: Packaging Executable Archives](https://docs.spring.io/spring-boot/maven-plugin/packaging.html) — Packaging Executable Archives, mainClass, classifier
- [Spring Boot: Profiles](https://docs.spring.io/spring-boot/reference/features/profiles.html) — Profiles, Adding Active Profiles

**Worked guide:** [day-67.md](docs/reboot/continuation-57-70/worked/day-67.md). Read the complete supplied reference before applying the distinct native variation. This is instructor teaching, not learner completion.

**Time rule:** Optional lessons replace time inside these 1500 seconds; pauses do too. No overtime. Access and viewing are not inferred.

**Runnable acceptance commands:**
- `./mvnw -Plocal-app,sprint5-local -DskipTests package`
- `./mvnw -Plocal-app,sprint5-local,learner-tests -Dtest=Sprint5PackagedInfrastructureTest#stageArchiveHasExplicitEntryAndPackagedUi+stageJarServesOnlyTheBoundedRuntime test`

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner explanation/trace and any changed paths (use `[]` for a read-only exercise) in `evidence/continuation-57-70/day-67.json`. Leave completion and actual dates blank until the work is actually done.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 67` first. The date command uses the actual Asia/Kolkata local study date; dates shown elsewhere are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../../dsa/guided-transfer-policy.md](../../dsa/guided-transfer-policy.md) and [the DSA error log](../../dsa-error-log-template.md).
