# CoverStory Phase 1 Days 011–020 readiness

Instructor readiness is based on current setup checks and checked synthetic fixtures. Learner runs are separate and do not determine this result. The saved 150-minute budget is unmeasured.

## Day 011 · 2026-10-24 · CAP-55 — READY

Instructor checks: `mvn -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/capstone-m2 -Dtest=CAP55InstructorInfrastructureTest test` (PASS).
Learner acceptance: `EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME`; TODO signal=True.
Fixtures:
- `fixtures/coverstory/CAP-55/synthetic-v1.json` — SHA-256 `59e467f5d8afad2bd44ab7a94893a04e20a4c2f6c314347892a768bca5a27f8f`
Reset/isolation: Read-only synthetic fixture and per-run pure/fake state; no customer data.
Module boundary: Catalog offline trace seam; no Kafka, broker, topic, or cloud resource.
Remaining: Implement the saved learner seam and rerun acceptance separately. The saved 150-minute budget remains unmeasured and does not establish task duration.

## Day 012 · 2026-10-25 · CAP-56 — READY

Instructor checks: `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python3 -m unittest ai.tests.test_coverstory_cap56_infrastructure` (PASS).
Learner acceptance: `EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME`; TODO signal=True.
Fixtures:
- `fixtures/coverstory/CAP-56/synthetic-v1.json` — SHA-256 `e82c9603042c2f35969bd9a5c0e2076944cc6c897cbf2a95240a8e636c8f5f7e`
Reset/isolation: Read-only synthetic fixture and per-run pure/fake state; no customer data.
Module boundary: Pure Python synthetic retrieval evaluation; no live retrieval provider.
Remaining: Implement the saved learner seam and rerun acceptance separately. The saved 150-minute budget remains unmeasured and does not establish task duration.

## Day 013 · 2026-10-26 · CAP-57 — READY

Instructor checks: `mvn -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/capstone-m2 -Dtest=CAP57InstructorInfrastructureTest test` (PASS).
Learner acceptance: `EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME`; TODO signal=True.
Fixtures:
- `fixtures/coverstory/CAP-57/synthetic-v1.json` — SHA-256 `328bc91580abecaad90b72c053bb5a28f72845eaab9283a6c92efc26b9ee4da4`
- `src/main/resources/db/schema/catalog_publication.sql` — SHA-256 `56b479f43d0568ab2b71da1068f19ee0ec246c9957d9c09988fc5448686081a7`
- `src/test/resources/coverstory/catalog/cap_57_fixtures.sql` — SHA-256 `e5d58dda03ba2e5b9b9dc9cb3377018ab803b88cfc97508561bea839945b9405`
Reset/isolation: Each command executes in a fresh process/test fixture; PostgreSQL cases use a fresh embedded instance per test.
Module boundary: Test-only Catalog publication schema against fresh embedded PostgreSQL; not production persistence.
Remaining: Implement the saved learner seam and rerun acceptance separately. The saved 150-minute budget remains unmeasured and does not establish task duration.

## Day 014 · 2026-10-27 · CAP-166 — READY

Instructor checks: `npm run test:catalog-fixture` (PASS).
Learner acceptance: `EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME`; TODO signal=True.
Fixtures:
- `fixtures/coverstory/CAP-166/synthetic-v1.json` — SHA-256 `1c2d412edf3615948fd511805b3fc443e130db09fba23d5f6a6321c2afe102b4`
Reset/isolation: Read-only synthetic fixture and per-run pure/fake state; no customer data.
Module boundary: React catalog decoder/fixture seam; learner decoder remains TODO and full API/database integration is deferred.
Remaining: Implement the saved learner seam and rerun acceptance separately. The saved 150-minute budget remains unmeasured and does not establish task duration.

## Day 015 · 2026-10-28 · CAP-58 — READY

Instructor checks: `mvn -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/capstone-m2 -Dtest=CAP58InstructorInfrastructureTest test` (PASS).
Learner acceptance: `EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME`; TODO signal=True.
Fixtures:
- `fixtures/coverstory/CAP-58/synthetic-v1.json` — SHA-256 `03595c1783944271f9a214424eebc366527462b63c86a2078d382fed28110b64`
Reset/isolation: Read-only synthetic fixture and per-run pure/fake state; no customer data.
Module boundary: Analytics validator contract only; no HTTP ingestion or persistence service.
Remaining: Implement the saved learner seam and rerun acceptance separately. The saved 150-minute budget remains unmeasured and does not establish task duration.

## Day 016 · 2026-10-29 · CAP-59 — READY

Instructor checks: `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python3 -m unittest ai.tests.test_coverstory_cap59_infrastructure` (PASS).
Learner acceptance: `EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME`; TODO signal=True.
Fixtures:
- `fixtures/coverstory/CAP-59/synthetic-v1.json` — SHA-256 `4bd6d9ca70f0f8b2f6e37e1283617ffd159136943ce22820435be9133b408cf9`
Reset/isolation: Read-only synthetic fixture and per-run pure/fake state; no customer data.
Module boundary: Pure verifier seam over fixed synthetic text; hash validity is not source authority.
Remaining: Implement the saved learner seam and rerun acceptance separately. The saved 150-minute budget remains unmeasured and does not establish task duration.

## Day 017 · 2026-10-30 · CAP-60 — READY

Instructor checks: `mvn -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/capstone-m2 -Dtest=CAP60InstructorInfrastructureTest test` (PASS).
Learner acceptance: `EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME`; TODO signal=True.
Fixtures:
- `fixtures/coverstory/CAP-60/synthetic-v1.json` — SHA-256 `d98fd7b600458f256bdd2518895fac9dc15d35c345b961191d5f99ab453ba79c`
- `src/test/resources/coverstory/CAP-60/schema.sql` — SHA-256 `a0adb10f1c381a9626fd93397202f9af76172c72be95c7c0b7bc8b37061f8f91`
Reset/isolation: Each command executes in a fresh process/test fixture; PostgreSQL cases use a fresh embedded instance per test.
Module boundary: Test-only schema/oracle; no application command or endpoint exists.
Remaining: Implement the saved learner seam and rerun acceptance separately. The saved 150-minute budget remains unmeasured and does not establish task duration.

## Day 018 · 2026-10-31 · CAP-61 — READY

Instructor checks: `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python3 -m unittest ai.tests.test_coverstory_cap61_infrastructure` (PASS).
Learner acceptance: `EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME`; TODO signal=True.
Fixtures:
- `fixtures/coverstory/CAP-61/synthetic-v1.json` — SHA-256 `9e270fc30a239cd85be04f1a5a48997dbc55f6692dec014244d9eebe432ab25e`
Reset/isolation: Read-only synthetic fixture and per-run pure/fake state; no customer data.
Module boundary: Synthetic grounded-answer evaluation gate; not calibrated or connected to a production model.
Remaining: Implement the saved learner seam and rerun acceptance separately. The saved 150-minute budget remains unmeasured and does not establish task duration.

## Day 019 · 2026-11-01 · CAP-62 — READY

Instructor checks: `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python3 -m unittest ai.tests.test_coverstory_cap62_infrastructure` (PASS).
Learner acceptance: `EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME`; TODO signal=True.
Fixtures:
- `fixtures/coverstory/CAP-62/synthetic-v1.json` — SHA-256 `496b81896f97ffe4d8eb39cc3855bca1b21c668705ad5f3a776f0282be46742b`
Reset/isolation: Read-only synthetic fixture and per-run pure/fake state; no customer data.
Module boundary: Local async fakes only; no broker, cache, retrieval backend, or workflow.
Remaining: Implement the saved learner seam and rerun acceptance separately. The saved 150-minute budget remains unmeasured and does not establish task duration.

## Day 020 · 2026-11-02 · CAP-63 — READY

Instructor checks: `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python3 -m unittest ai.tests.test_coverstory_cap63_infrastructure` (PASS).
Learner acceptance: `EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME`; TODO signal=True.
Fixtures:
- `fixtures/coverstory/CAP-63/synthetic-v1.json` — SHA-256 `337004bb8fa11b80b23b08bd9448daf6dd9f019dacc9c67fd762c07f5590545f`
Reset/isolation: Read-only synthetic fixture and per-run pure/fake state; no customer data.
Module boundary: Pure evidence-index oracle; it does not validate real Java/SQL/deployment/customer evidence.
Remaining: Implement the saved learner seam and rerun acceptance separately. The saved 150-minute budget remains unmeasured and does not establish task duration.
