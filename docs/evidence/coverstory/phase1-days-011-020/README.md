# CoverStory Phase 1 Days 011–020 readiness

Instructor readiness is based on current setup checks and checked synthetic fixtures. Learner runs are separate and do not determine this result. The saved 150-minute budget is unmeasured. The instructor checks do not claim learner completion.

## Day 011 · 2026-10-24 · CAP-55 — READY

Instructor checks: `/tmp/apache-maven-3.10.0/bin/mvn -Dmaven.repo.local=/tmp/maven-repo -s /tmp/maven-settings.xml -Dtest=CAP55InstructorInfrastructureTest test` (PASS).
Learner acceptance: `EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME`; TODO signal=True.
Fixtures:
- `fixtures/coverstory/CAP-55/synthetic-v1.json` — SHA-256 `59e467f5d8afad2bd44ab7a94893a04e20a4c2f6c314347892a768bca5a27f8f`
Reset/isolation: Read-only synthetic fixture and per-run pure/fake state; no customer data.
Module boundary: Catalog offline trace seam; no Kafka, broker, topic, or cloud resource.
Remaining: Implement the saved learner seam and rerun acceptance separately. The saved 150-minute budget remains unmeasured and does not establish task duration.

## Day 012 · 2026-10-25 · CAP-56 — READY

Instructor checks: `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python -m unittest ai.tests.test_coverstory_cap56_infrastructure` (PASS).
Learner acceptance: `EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME`; TODO signal=True.
Fixtures:
- `fixtures/coverstory/CAP-56/synthetic-v1.json` — SHA-256 `e82c9603042c2f35969bd9a5c0e2076944cc6c897cbf2a95240a8e636c8f5f7e`
Reset/isolation: Read-only synthetic fixture and per-run pure/fake state; no customer data.
Module boundary: Pure Python synthetic retrieval evaluation; no live retrieval provider.
Remaining: Implement the saved learner seam and rerun acceptance separately. The saved 150-minute budget remains unmeasured and does not establish task duration.

## Day 013 · 2026-10-26 · CAP-57 — READY

Instructor checks: `/tmp/apache-maven-3.10.0/bin/mvn -Dmaven.repo.local=/tmp/maven-repo -s /tmp/maven-settings.xml -Dtest=CAP57InstructorInfrastructureTest test` (PASS).
Learner acceptance: `EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME`; TODO signal=True.
Fixtures:
- `fixtures/coverstory/CAP-57/synthetic-v1.json` — SHA-256 `328bc91580abecaad90b72c053bb5a28f72845eaab9283a6c92efc26b9ee4da4`
- `src/main/resources/db/schema/catalog_publication.sql` — SHA-256 `56b479f43d0568ab2b71da1068f19ee0ec246c9957d9c09988fc5448686081a7`
- `src/test/resources/coverstory/catalog/cap_57_fixtures.sql` — SHA-256 `e5d58dda03ba2e5b9b9dc9cb3377018ab803b88cfc97508561bea839945b9405`
Reset/isolation: Each CAP57 learner acceptance method creates and closes a fresh embedded PostgreSQL instance. A positive isolation witness publishes in one database and verifies a second starts with prepared rows but empty active publication state. Synthetic rows only.
Module boundary: Test-only Catalog publication schema against fresh embedded PostgreSQL; not production persistence.
Remaining: Implement the saved learner seam and rerun acceptance separately. The saved 150-minute budget remains unmeasured and does not establish task duration.

## Day 014 · 2026-10-27 · CAP-166 — READY

Instructor checks: `/tmp/apache-maven-3.10.0/bin/mvn -Dmaven.repo.local=/tmp/maven-repo -s /tmp/maven-settings.xml -Dtest=CAP166CatalogRuntimeIntegrationTest -Dcoverstory.cap166.browser=true test` (PASS), `npm run test:catalog-fixture` (PASS), `npm run build` (PASS).
Learner acceptance: `EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME`; TODO signal=True.
Fixtures:
- `fixtures/coverstory/CAP-166/synthetic-v1.json` — SHA-256 `1c2d412edf3615948fd511805b3fc443e130db09fba23d5f6a6321c2afe102b4`
- `src/main/resources/db/schema/catalog_publication.sql` — SHA-256 `56b479f43d0568ab2b71da1068f19ee0ec246c9957d9c09988fc5448686081a7`
- `src/main/resources/db/schema/catalog_discovery.sql` — SHA-256 `2445fa31cbfb958859dffabe1f0f81bad629bf5096a8d17ef158b913a2dfd477`
- `src/test/resources/coverstory/catalog/cap_166_seed.sql` — SHA-256 `dd3504c47345db2f95cd171a3639cfc89ce7564dbeae973754e72221bfefd227`
Reset/isolation: Fresh embedded PostgreSQL and application context per integration method; each browser run uses a disposable Chromium profile and memory-only selection. Synthetic rows only.
Module boundary: Actual embedded PostgreSQL -> Catalog-owned JDBC adapter -> Spring GET /api/catalog/versions -> React Query component; browser harness injects a narrow instructor decoder adapter and does not implement learner decode behavior.
Remaining: Implement the saved learner seam and rerun acceptance separately. The saved 150-minute budget remains unmeasured and does not establish task duration.

## Day 015 · 2026-10-28 · CAP-58 — PARTIAL

Instructor checks: `/tmp/apache-maven-3.10.0/bin/mvn -Dmaven.repo.local=/tmp/maven-repo -s /tmp/maven-settings.xml -Dtest=CAP58InstructorInfrastructureTest,CAP58AnalyticsHttpIntegrationTest test` (PASS), `/tmp/apache-maven-3.10.0/bin/mvn -Dmaven.repo.local=/tmp/maven-repo -s /tmp/maven-settings.xml -Dtest=CAP58PostgresAcceptanceTest test` (PASS), `npm run build` (PASS), `npm run test:cap58-browser-queue` (PASS).
Learner acceptance: `EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME`; TODO signal=True.
Fixtures:
- `fixtures/coverstory/CAP-58/synthetic-v1.json` — SHA-256 `03595c1783944271f9a214424eebc366527462b63c86a2078d382fed28110b64`
- `web/src/coverstory/analytics/BoundedTelemetryQueue.ts` — SHA-256 `d217df39bf8cdbdb226d21d122ddd3918e28e95bf40f2f83d31cfa29b7fb809b`
- `src/main/java/coverstory/modules/analytics/api/AnalyticsIngestionController.java` — SHA-256 `40e6a5c2abd0e16e5ae2946085e1b0ccada40f37bbb399e4c9e1a89a1a450c1f`
Reset/isolation: Read-only synthetic fixture and per-run pure/fake state; no customer data.
Module boundary: the Spring HTTP controller delegates through a live `CAP58AnalyticsAdapter`. When consent, Application ownership, HMAC scope hashing and JDBC receipt storage are configured, the adapter reaches the actual `CAP58Seam` learner validator. The runnable integration uses an embedded PostgreSQL database and a synthetic consent adapter; it does not create deployment configuration. Missing required dependencies return 503; configured requests currently stop at the learner TODO with 501.
Additional integration check: `mvn -Dtest=CAP58LiveAdapterIntegrationTest test` — 5 tests, 0 failures/errors. The check covers the live application adapter, configured vs absent infrastructure responses, owner/revision scope, consent denial, allowed batch bounds, duplicate replay/conflict, allowlisted durable columns and actual Spring context wiring. Output hash: `e3c773935c8cfdaf3b6101832401fc3f1d7965950241e5b19d571fbe9f3a7a0d`.
Remaining: `CAP58Seam.validateBatch` is still learner-owned and returns the intentional TODO 501. Deployment consent source, versioned HMAC key and application database values are not supplied; no external send worker or business-audit coupling exists. Keep learner acceptance separate. The saved 150-minute budget remains unmeasured and does not establish task duration.

## Day 016 · 2026-10-29 · CAP-59 — READY

Instructor checks: `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python -m unittest ai.tests.test_coverstory_cap59_infrastructure` (PASS).
Learner acceptance: `EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME`; TODO signal=True.
Fixtures:
- `fixtures/coverstory/CAP-59/synthetic-v1.json` — SHA-256 `4bd6d9ca70f0f8b2f6e37e1283617ffd159136943ce22820435be9133b408cf9`
Reset/isolation: Read-only synthetic fixture and per-run pure/fake state; no customer data.
Module boundary: Pure verifier seam over fixed synthetic text; hash validity is not source authority.
Remaining: Implement the saved learner seam and rerun acceptance separately. The saved 150-minute budget remains unmeasured and does not establish task duration.

## Day 017 · 2026-10-30 · CAP-60 — READY

Instructor checks: `/tmp/apache-maven-3.10.0/bin/mvn -Dmaven.repo.local=/tmp/maven-repo -s /tmp/maven-settings.xml -Dtest=CAP60InstructorInfrastructureTest test` (PASS).
Learner acceptance: `EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME`; TODO signal=True.
Fixtures:
- `fixtures/coverstory/CAP-60/synthetic-v1.json` — SHA-256 `d98fd7b600458f256bdd2518895fac9dc15d35c345b961191d5f99ab453ba79c`
- `src/test/resources/coverstory/CAP-60/schema.sql` — SHA-256 `b8a21503f15a614918256e21c9386a273c3e69c2396e8387a9061f124ea6eecc`
Reset/isolation: Each CAP60 command/race check starts from a fresh embedded schema and stops its PostgreSQL instance; racers use independent connections and barrier/future waits bounded to five seconds. Synthetic rows only.
Module boundary: Instructor-only callable command adapter and schema run against disposable embedded PostgreSQL. It is a reference command for learner race tests, not a production endpoint or application service.
Remaining: Implement the saved learner seam and rerun acceptance separately. The saved 150-minute budget remains unmeasured and does not establish task duration.

## Day 018 · 2026-10-31 · CAP-61 — READY

Instructor checks: `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python -m unittest ai.tests.test_coverstory_cap61_infrastructure` (PASS).
Learner acceptance: `EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME`; TODO signal=True.
Fixtures:
- `fixtures/coverstory/CAP-61/synthetic-v1.json` — SHA-256 `9e270fc30a239cd85be04f1a5a48997dbc55f6692dec014244d9eebe432ab25e`
Reset/isolation: Read-only synthetic fixture and per-run pure/fake state; no customer data.
Module boundary: Synthetic grounded-answer evaluation gate; not calibrated or connected to a production model.
Remaining: Implement the saved learner seam and rerun acceptance separately. The saved 150-minute budget remains unmeasured and does not establish task duration.

## Day 019 · 2026-11-01 · CAP-62 — READY

Instructor checks: `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python -m unittest ai.tests.test_coverstory_cap62_infrastructure` (PASS).
Learner acceptance: `EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME`; TODO signal=True.
Fixtures:
- `fixtures/coverstory/CAP-62/synthetic-v1.json` — SHA-256 `496b81896f97ffe4d8eb39cc3855bca1b21c668705ad5f3a776f0282be46742b`
Reset/isolation: Read-only synthetic fixture and per-run pure/fake state; no customer data.
Module boundary: Local async fakes only; no broker, cache, retrieval backend, or workflow.
Remaining: Implement the saved learner seam and rerun acceptance separately. The saved 150-minute budget remains unmeasured and does not establish task duration.

## Day 020 · 2026-11-02 · CAP-63 — READY

Instructor checks: `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python -m unittest ai.tests.test_coverstory_cap63_infrastructure` (PASS).
Learner acceptance: `EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME`; TODO signal=True.
Fixtures:
- `fixtures/coverstory/CAP-63/synthetic-v1.json` — SHA-256 `337004bb8fa11b80b23b08bd9448daf6dd9f019dacc9c67fd762c07f5590545f`
Reset/isolation: Read-only synthetic fixture and per-run pure/fake state; no customer data.
Module boundary: Pure evidence-index oracle; it does not validate real Java/SQL/deployment/customer evidence.
Remaining: Implement the saved learner seam and rerun acceptance separately. The saved 150-minute budget remains unmeasured and does not establish task duration.
