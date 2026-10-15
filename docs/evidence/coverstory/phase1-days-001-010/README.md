# Phase 1 Days 001–010 instructor readiness

This status is based on executable instructor scaffolds, resettable synthetic inputs, positive/negative controls, and the module boundary named for each day. It is independent of learner implementation and elapsed-time measurement. A READY scaffold is not learner completion or mastery.

Recorded UTC: `2026-10-15T01:12:19.463563+00:00`  
Canonical source SHA-256: `e96ae484e4c2c771e44d126f49127ee3ca005c766eb7abcb378e059a236e09de`

**Instructor result:** 10/10 days READY; 17 commands, 71 tests, 0 test failures, 0 test errors, and 2 frontend build/security checks passed.

**Separate learner checks:** 14 commands; 14 observed the preserved TODO/empty learner seam. These red outcomes do not determine instructor readiness and are not completed assignments.

| Day | Primary / linked cards | Instructor status | Instructor tests | Learner-owned seams still TODO |
|---:|---|---|---:|---|
| 001 | CAP-40 / CAP-41 | READY | 16 | CAP-40 uniqueSelections, CAP-41 retrieve |
| 002 | CAP-42 / CAP-43 | READY | 10 | CAP-42 SQL aggregate, CAP-43 classify/accept policy |
| 003 | CAP-44 / CAP-45 | READY | 4 | CAP-44 rank_wording, CAP-45 SQL reconciliation |
| 004 | CAP-46 / CAP-47 | READY | 9 | CAP-46 bounded queue methods, CAP-47 evaluate |
| 005 | CAP-48 / CAP-49 | READY | 5 | CAP-48 date-spine aggregate SQL, CAP-49 classify_failure |
| 006 | CAP-50 / CAP-51 | READY | 8 | CAP-50 acceptedCounts, CAP-51 SQL aggregate |
| 007 | CAP-165 / — | READY | 5 | CAP-165 SourceVersionKey create |
| 008 | CAP-52 / — | READY | 5 | CAP-52 import accept decision |
| 009 | CAP-53 / — | READY | 3 | CAP-53 load_wording |
| 010 | CAP-54 / — | READY | 6 | CAP-54 resume and active-list seam |

## Per-day evidence

### Day 001 · 2026-10-14 · CAP-40

**Instructor readiness:** `READY`. Learner complete: `false`; timebox measured: `false`; unmeasured timebox blocks this instructor status: `false`.

**Executed checks:**
- `mvn -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/capstone-m2 -Dtest=CAP40ScaffoldInfrastructureTest,ShortlistSelectionControllerInfrastructureTest,ApplicationRuntimeIntegrationTest test`: **PASS** — 7 tests, 0 failures, 0 errors, 0 skipped; raw output [log](raw/day-001-maven.log) (`89e5708e0f18f206c4af3c410b7d25b81bc89a70018cf8fe72e86d032eb4e0e3`).
- `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python3 -m unittest ai.tests.test_coverstory_cap41_infrastructure`: **PASS** — 4 tests, 0 failures, 0 errors, 0 skipped; raw output [log](raw/day-001-test_coverstory_cap41_infrastructure.log) (`feabb4add9e2ca3374e538560634d1ec97dc4f4ce89aab12f3d726e36ea9b2f6`).
- `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python3 -m unittest ai.tests.test_coverstory_cap41_http`: **PASS** — 5 tests, 0 failures, 0 errors, 0 skipped; raw output [log](raw/day-001-test_coverstory_cap41_http.log) (`aad49207f6643b866be6dbc8ef285d4be360fc3e60b767614a9b7a3a61ae8633`).
- `npm run build`: **PASS**; raw output [log](raw/day-001-frontend-build.log) (`0c7ee2f3f3be02c8731d9aa60d306bd1a915deb8b821745fada494d34e0b8c1a`).
- `npm run test:security`: **PASS**; raw output [log](raw/day-001-frontend-security.log) (`6995d3b85fe7a4068be8cbecc69d5c0f74c11166578987806563cb1126b98ff1`).

**Separate learner acceptance runs (not used to decide scaffold readiness):**
- `mvn -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/capstone-m2 -Plearner-tests -Dtest=CAP40AcceptanceTest test`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 8 tests, 0 failures, 8 errors; TODO signal: `True`; [raw log](raw/day-001-learner-maven.log) (`37e133f27cece9d36c912544dcf567be8bf6cc384ba5e5c0b2db64723dc10dd0`).
- `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python3 -m unittest ai.tests.test_coverstory_cap41_acceptance`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 6 tests, 0 failures, 9 errors; TODO signal: `True`; [raw log](raw/day-001-learner-ai.tests.log) (`d55e69a846089694683b2d791c55c9554ca857d4c8fd924c7a56d95f46ac1d98`).

**Fixture SHA-256:**
- `fixtures/coverstory/CAP-40/synthetic-v1.json` — `c6751ab88acf5ca5570f9f730ecb943d7354457407a1b5e0de735b0d58ebd022`
- `ai/fixtures/coverstory/CAP-41/synthetic-v1.json` — `6be8b8063eb3d052dc0476d26243fbdaae549fd5ae64a011c42fe24dc9a4e6da`

**Reset/isolation:** Read-only fixed synthetic JSON; Python HTTP harness loads the checked-in fixture per process; no shared mutable database.

**Wiring:** Spring app startup, Catalog module, CAP-40 shortlist HTTP route and loopback CAP-41 service; React production build/security checks are run as shared setup checks.

**Positive controls:** first-seen composite selection order; synthetic catalog through CatalogPort -> adapter -> live Spring HTTP; CAP-41 exact citation transport with fixed documents.

**Negative controls:** version-only and sorted-order defects; public endpoint safe 501 at learner TODO; anonymous session/import private routes fail closed; CAP-41 missing/foreign citation and TODO-safe HTTP 501.

**Learner TODOs:** CAP-40 uniqueSelections, CAP-41 retrieve. These are deliberately not implemented by the instructor harness.

**Last recorded learner acceptance receipts:**
- [CAP-40 result](../CAP-40/result.json); recorded status `EXPECTED_FAILURE_AT_UNIMPLEMENTED_TODO`. These are prior separated TODO evidence, not counted in today's instructor PASS.
- [CAP-41 result](../CAP-41/result.json); recorded status `None`. These are prior separated TODO evidence, not counted in today's instructor PASS.
**Remaining requirements:** Implement the two named learner seams; keep public synthetic scope and safe 501 behavior until implemented.; Learner duration remains unmeasured; this does not block instructor harness readiness.

### Day 002 · 2026-10-15 · CAP-42

**Instructor readiness:** `READY`. Learner complete: `false`; timebox measured: `false`; unmeasured timebox blocks this instructor status: `false`.

**Executed checks:**
- `mvn -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/capstone-m2 -Dtest=CAP42InstructorInfrastructureTest,CAP43InstructorInfrastructureTest,CatalogInstructorHarnessTest,ApplicationRuntimeIntegrationTest test`: **PASS** — 10 tests, 0 failures, 0 errors, 0 skipped; raw output [log](raw/day-002-maven.log) (`70fe0f41bfc727c4438f554ae1463a15c2f4dc888b8a467147755a8fd46b7e9d`).

**Separate learner acceptance runs (not used to decide scaffold readiness):**
- `mvn -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/capstone-m2 -Plearner-tests -Dtest=CAP42SqlAcceptanceTest,CAP43AcceptanceTest test`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 9 tests, 2 failures, 5 errors; TODO signal: `True`; [raw log](raw/day-002-learner-maven.log) (`a21360a90ff370b191685f3fc2ae844b97b06cfe663c8862d4a96ed76281bcb2`).

**Fixture SHA-256:**
- `fixtures/coverstory/CAP-42/synthetic-v1.json` — `de78e16318de5ba7861085e01941b0e5d121379fbc44bcb19893a451a9660743`
- `fixtures/coverstory/CAP-43/synthetic-v1.json` — `70a791e430c0443c508af50932c42e2b6b3e459f0ca56a3ff395ef6333f756e9`
- `src/test/resources/coverstory/catalog/cap_42_schema.sql` — `36e3d3238b65ec0f864fbab5914c5ec2b32ee550a9139547e8993246ec0832be`
- `src/test/resources/coverstory/catalog/cap_42_fixtures.sql` — `925660e9cf842f42c09baeb7c4d15b5471834fd78bb95be7f7a6f4baa53aa2d0`

**Reset/isolation:** Every PostgreSQL instructor test starts and closes a fresh embedded PostgreSQL instance; JSON fixture is read-only.

**Wiring:** CAP-42 use case delegates through the Catalog evidence port; CAP-43 service resolves the Catalog-owned learner port in the actual Spring context.

**Positive controls:** exact numeric control total and half-open UTC window; Catalog service/port bean graph starts and reaches CAP-43's explicit TODO; sequential exact editor/source/version/hash reference.

**Negative controls:** wrong editor, inclusive upper bound, invalid currency; version-only key and whitespace-trimmed opaque hash mutants; real app does not claim a completed import.

**Learner TODOs:** CAP-42 SQL aggregate, CAP-43 classify/accept policy. These are deliberately not implemented by the instructor harness.

**Last recorded learner acceptance receipts:**
- [CAP-42 result](../CAP-42/result.json); recorded status `TODO_RETAINED`. These are prior separated TODO evidence, not counted in today's instructor PASS.
- [CAP-43 result](../CAP-43/result.json); recorded status `TODO_RETAINED`. These are prior separated TODO evidence, not counted in today's instructor PASS.
**Remaining requirements:** Implement saved SQL/map policy only; persistent import storage, authenticated publishing and concurrent idempotency remain later product work.; Learner duration remains unmeasured and does not block scaffold readiness.

### Day 003 · 2026-10-16 · CAP-44

**Instructor readiness:** `READY`. Learner complete: `false`; timebox measured: `false`; unmeasured timebox blocks this instructor status: `false`.

**Executed checks:**
- `mvn -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/capstone-m2 -Dtest=CAP45InstructorInfrastructureTest test`: **PASS** — 2 tests, 0 failures, 0 errors, 0 skipped; raw output [log](raw/day-003-maven.log) (`469684854795e50111d8510b239f07c917d03e1a3df8203e05e4790ea6b3d9ea`).
- `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python3 -m unittest ai.tests.test_coverstory_cap44_infrastructure`: **PASS** — 2 tests, 0 failures, 0 errors, 0 skipped; raw output [log](raw/day-003-test_coverstory_cap44_infrastructure.log) (`a6707d4eff38012da33ddc34bc6dc076a3b17151cfc9aef223c8790cbe64c3da`).

**Separate learner acceptance runs (not used to decide scaffold readiness):**
- `mvn -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/capstone-m2 -Plearner-tests -Dtest=CAP45SqlAcceptanceTest test`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 4 tests, 4 failures, 0 errors; TODO signal: `True`; [raw log](raw/day-003-learner-maven.log) (`100c302b52eb5792328760a8175ee736ec99523c75fd84d3924b6f8b9309a4c2`).
- `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python3 -m unittest discover -s ai/coverstory/tests -p test_cap_44.py`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 4 tests, 0 failures, 4 errors; TODO signal: `True`; [raw log](raw/day-003-learner-test_cap_44.log) (`bcb364a9fda14165ad747ddf246502c244a56a83a3a32b7a96c0af2642eba0fc`).

**Fixture SHA-256:**
- `fixtures/coverstory/CAP-44/synthetic-v1.json` — `82cf3a6ef7b9bece06656c6059cf6e59a78d4df3ecc2a77d004695cdc19dfa64`
- `fixtures/coverstory/CAP-45/synthetic-v1.json` — `29b0e119401d9888d7dea35f4515eefe874dc713c0d568c935b8c361412d0395`
- `src/test/resources/coverstory/catalog/cap_45_schema.sql` — `f1eec1a80a64cd36f39fd061873b60d0ba3364b8fe2569cd7ce4d8f1df065c57`
- `src/test/resources/coverstory/catalog/cap_45_fixtures.sql` — `58895ca61072eb680d3424f26dfa6a5c7424578d739c53b409333a65b66b3ad6`

**Reset/isolation:** CAP-45 creates a new embedded PostgreSQL schema/fixture per test; CAP-44 reads immutable synthetic public/restricted rows.

**Wiring:** CAP-44 is a bounded pure Python function seam; CAP-45 test-only SQL adapter executes against real embedded PostgreSQL 14. No external retrieval provider.

**Positive controls:** category/scenario/visibility-constrained stable ranking; left-join evidence rows preserve nulls, attempts and exact source count.

**Negative controls:** visibility/category/order/duplicate-token ranking mutants; inner join, source-only owner join and attempt fanout SQL mutants.

**Learner TODOs:** CAP-44 rank_wording, CAP-45 SQL reconciliation. These are deliberately not implemented by the instructor harness.

**Last recorded learner acceptance receipts:**
- [CAP-44 result](../CAP-44/result.json); recorded status `TODO_RETAINED`. These are prior separated TODO evidence, not counted in today's instructor PASS.
- [CAP-45 result](../CAP-45/result.json); recorded status `TODO_RETAINED`. These are prior separated TODO evidence, not counted in today's instructor PASS.
**Remaining requirements:** Implement the two saved learner seams; no product import ledger/provider is implied.; Learner duration remains unmeasured and does not block scaffold readiness.

### Day 004 · 2026-10-17 · CAP-46

**Instructor readiness:** `READY`. Learner complete: `false`; timebox measured: `false`; unmeasured timebox blocks this instructor status: `false`.

**Executed checks:**
- `mvn -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/capstone-m2 -Dtest=CAP46InstructorInfrastructureTest,CatalogInstructorHarnessTest test`: **PASS** — 7 tests, 0 failures, 0 errors, 0 skipped; raw output [log](raw/day-004-maven.log) (`3f0a616ccaadeb830628a6644779fde4ee6971d9560b8e1feab31e65bea70177`).
- `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python3 -m unittest ai.tests.test_coverstory_cap47_infrastructure`: **PASS** — 2 tests, 0 failures, 0 errors, 0 skipped; raw output [log](raw/day-004-test_coverstory_cap47_infrastructure.log) (`a6707d4eff38012da33ddc34bc6dc076a3b17151cfc9aef223c8790cbe64c3da`).

**Separate learner acceptance runs (not used to decide scaffold readiness):**
- `mvn -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/capstone-m2 -Plearner-tests -Dtest=CAP46AcceptanceTest test`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 4 tests, 0 failures, 4 errors; TODO signal: `True`; [raw log](raw/day-004-learner-maven.log) (`a4faf9b3b9ca363b7d58297e53d3e76c61a7c0687ae85f9f6e29dffc949e2a13`).
- `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python3 -m unittest discover -s ai/coverstory/tests -p test_cap_47.py`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 4 tests, 0 failures, 4 errors; TODO signal: `True`; [raw log](raw/day-004-learner-test_cap_47.log) (`918a7f2f48c296da5cb517c4a719fa9c22b2dfabe5bef581af84c50b2566d43d`).

**Fixture SHA-256:**
- `fixtures/coverstory/CAP-46/synthetic-v1.json` — `3efacef586b0bf76fc20c59e0cca0caebec2fb259ad1b8e55d36e1f45d5c7fd2`
- `fixtures/coverstory/CAP-47/synthetic-v1.json` — `ca94a213d22795fe688d8af33808c298a80c347df84c8f3aaa0fc59fe310dfb9`

**Reset/isolation:** Each oracle replay constructs fresh in-memory queue/evaluation state from immutable fixtures.

**Wiring:** Typed Catalog SourceVersionKey and CAP-46 TODO seam are compiled/discoverable; evaluation component is isolated Python fixture code. No broker or model is started.

**Positive controls:** bounded FIFO operations, empty poll and pending-only uniqueness; exact source/version support and correct abstention metrics.

**Negative controls:** capacity overflow, LIFO ordering and permanent-duplicate mutants; presence-only, foreign citation, invalid answer map and abstention-with-citation mutants.

**Learner TODOs:** CAP-46 bounded queue methods, CAP-47 evaluate. These are deliberately not implemented by the instructor harness.

**Last recorded learner acceptance receipts:**
- [CAP-46 result](../CAP-46/result.json); recorded status `TODO_RETAINED`. These are prior separated TODO evidence, not counted in today's instructor PASS.
- [CAP-47 result](../CAP-47/result.json); recorded status `TODO_RETAINED`. These are prior separated TODO evidence, not counted in today's instructor PASS.
**Remaining requirements:** Implement only the saved queue and evaluation seams; a real message broker and factual evaluation are outside these contracts.; Learner duration remains unmeasured and does not block scaffold readiness.

### Day 005 · 2026-10-18 · CAP-48

**Instructor readiness:** `READY`. Learner complete: `false`; timebox measured: `false`; unmeasured timebox blocks this instructor status: `false`.

**Executed checks:**
- `mvn -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/capstone-m2 -Dtest=CAP48InstructorInfrastructureTest test`: **PASS** — 3 tests, 0 failures, 0 errors, 0 skipped; raw output [log](raw/day-005-maven.log) (`af159eaeed4b2a12a85f66da580257534c7c49ee0748f46669660fb7f56cadac`).
- `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python3 -m unittest ai.tests.test_coverstory_cap49_infrastructure`: **PASS** — 2 tests, 0 failures, 0 errors, 0 skipped; raw output [log](raw/day-005-test_coverstory_cap49_infrastructure.log) (`a6707d4eff38012da33ddc34bc6dc076a3b17151cfc9aef223c8790cbe64c3da`).

**Separate learner acceptance runs (not used to decide scaffold readiness):**
- `mvn -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/capstone-m2 -Plearner-tests -Dtest=CAP48SqlAcceptanceTest test`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 4 tests, 4 failures, 0 errors; TODO signal: `True`; [raw log](raw/day-005-learner-maven.log) (`56a987a489ad3da56501fc11912c57cb871c7e1c1c8a2f17eb00b3bc63c937b3`).
- `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python3 -m unittest discover -s ai/coverstory/tests -p test_cap_49.py`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 3 tests, 0 failures, 10 errors; TODO signal: `True`; [raw log](raw/day-005-learner-test_cap_49.log) (`fd7d5378cedac579ab7b9ad84e8fb3e6208f6c5dae81d0db0cefab377b479a98`).

**Fixture SHA-256:**
- `fixtures/coverstory/CAP-48/synthetic-v1.json` — `3ff4c1f3a6e60802fc806a9c7ab975f9206dae10e5aa2ee323e6c654d082369e`
- `fixtures/coverstory/CAP-49/synthetic-v1.json` — `051951df77bac81eef345999b4e205936e7292e601d571026ba384af958b9002`
- `src/test/resources/coverstory/catalog/cap_48_schema.sql` — `8ccb1b2d1eed7e4a8fbcf6261cd365d2b23bd39a5381ff67d229503c5ee46ca6`
- `src/test/resources/coverstory/catalog/cap_48_fixtures.sql` — `edff1a2e1ec5cf462f0a17449c37783036cce8bc125d3c02831336293cfe2a90`

**Reset/isolation:** Fresh embedded PostgreSQL schema and fixed UTC rows for each test; classifier fixture is read-only.

**Wiring:** CAP-48 SQL is exercised through test-only JDBC against embedded PostgreSQL; CAP-49 is a pure Python classification seam.

**Positive controls:** explicit UTC date spine under UTC and Asia/Kolkata session zones; failure taxonomy precedence for correct abstention and citation errors.

**Negative controls:** observed-only dates, local timestamp cast, wrong owner; unsupported-answer, source/version precedence and false-abstention controls.

**Learner TODOs:** CAP-48 date-spine aggregate SQL, CAP-49 classify_failure. These are deliberately not implemented by the instructor harness.

**Last recorded learner acceptance receipts:**
- [CAP-48 result](../CAP-48/result.json); recorded status `TODO_RETAINED`. These are prior separated TODO evidence, not counted in today's instructor PASS.
- [CAP-49 result](../CAP-49/result.json); recorded status `TODO_RETAINED`. These are prior separated TODO evidence, not counted in today's instructor PASS.
**Remaining requirements:** Implement only saved SQL/classifier seams; no live wording or provider source is connected.; Learner duration remains unmeasured and does not block scaffold readiness.

### Day 006 · 2026-10-19 · CAP-50

**Instructor readiness:** `READY`. Learner complete: `false`; timebox measured: `false`; unmeasured timebox blocks this instructor status: `false`.

**Executed checks:**
- `mvn -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/capstone-m2 -Dtest=CAP50InstructorInfrastructureTest,CAP51InstructorInfrastructureTest,CatalogInstructorHarnessTest test`: **PASS** — 8 tests, 0 failures, 0 errors, 0 skipped; raw output [log](raw/day-006-maven.log) (`697cd251f7a8016929a8b0253a15e0944acfc46997ecf2d1bdec49b9514bd8e6`).

**Separate learner acceptance runs (not used to decide scaffold readiness):**
- `mvn -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/capstone-m2 -Plearner-tests -Dtest=CAP50AcceptanceTest,CAP51SqlAcceptanceTest test`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 7 tests, 5 failures, 2 errors; TODO signal: `True`; [raw log](raw/day-006-learner-maven.log) (`a59e63d8fdddf25d374d57f499c4d5265fa42f80fd3582b0a038dc5bed42af70`).

**Fixture SHA-256:**
- `fixtures/coverstory/CAP-50/synthetic-v1.json` — `35c63e1f976102111fbeeab48908de8ade842c0ffc3b53b21c9dc9eefc3bad81`
- `fixtures/coverstory/CAP-51/synthetic-v1.json` — `162f652433c5914bdd8e68bca88b6f9ddb99f12fc7198d896a4a11f314096c14`
- `src/test/resources/coverstory/catalog/cap_51_schema.sql` — `a91837027a79efed015e6352933730f0c9d63db9cfcbceffe191077b7b99b6c1`
- `src/test/resources/coverstory/catalog/cap_51_fixtures.sql` — `62bc3e03a63495dc885581342d20d88de5e3c83d8375925d8ffbc67d9bfb0f5d`

**Reset/isolation:** Fresh fake identity map per CAP-50 oracle; fresh embedded PostgreSQL schema/fixtures per CAP-51 test.

**Wiring:** CAP-50 service/fake lives behind Catalog-owned ports; CAP-51 SQL is a test-only adapter on PostgreSQL. No durable production import repository.

**Positive controls:** ACCEPT counted once through test-only Catalog service fake; EXISTS-style confirmed-attempt totals for own fixture owner.

**Negative controls:** same-hash duplicate, changed-hash conflict and foreign-editor scope; raw join fanout and source-only cross-editor attempt mutants.

**Learner TODOs:** CAP-50 acceptedCounts, CAP-51 SQL aggregate. These are deliberately not implemented by the instructor harness.

**Last recorded learner acceptance receipts:**
- [CAP-50 result](../CAP-50/result.json); recorded status `TODO_RETAINED`. These are prior separated TODO evidence, not counted in today's instructor PASS.
- [CAP-51 result](../CAP-51/result.json); recorded status `TODO_RETAINED`. These are prior separated TODO evidence, not counted in today's instructor PASS.
**Remaining requirements:** Implement saved learner methods; persistent identity/retries and transaction guarantees remain unclaimed.; Learner duration remains unmeasured and does not block scaffold readiness.

### Day 007 · 2026-10-20 · CAP-165

**Instructor readiness:** `READY`. Learner complete: `false`; timebox measured: `false`; unmeasured timebox blocks this instructor status: `false`.

**Executed checks:**
- `mvn -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/capstone-m2 -Dtest=CAP165HarnessDiscriminationTest,CatalogInstructorHarnessTest test`: **PASS** — 5 tests, 0 failures, 0 errors, 0 skipped; raw output [log](raw/day-007-maven.log) (`910472c34e82e4f9b8c52abb2dfff6a12adbb0b89ad20bff03938f5563fee2db`).

**Separate learner acceptance runs (not used to decide scaffold readiness):**
- `mvn -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/capstone-m2 -Plearner-tests -Dtest=CAP165AcceptanceTest test`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 4 tests, 1 failures, 3 errors; TODO signal: `True`; [raw log](raw/day-007-learner-maven.log) (`8a101cde23acbe98bbdaa90dac92c35c83f8c828eb0778cd7369505ec983d579`).

**Fixture SHA-256:**
- `fixtures/coverstory/CAP-165/synthetic-v1.json` — `8cc070c55cecf6ba767f348b259cbf3f72ee1777da9d02935c106f8ce1c7155c`

**Reset/isolation:** Fresh pure-value vectors are read from immutable JSON; no database or process state persists.

**Wiring:** Catalog-owned SourceVersionKey is the module value type; CAP165Seam remains the learner-owned factory TODO.

**Positive controls:** exact source/version pair key creates a typed identity.

**Negative controls:** delimiter-concatenation collision and null/blank components.

**Learner TODOs:** CAP-165 SourceVersionKey create. These are deliberately not implemented by the instructor harness.

**Last recorded learner acceptance receipts:**
- [CAP-165 result](../CAP-165/result.json); recorded status `TODO_RETAINED`. These are prior separated TODO evidence, not counted in today's instructor PASS.
**Remaining requirements:** Implement and use the saved key factory; no persistence is in this day's contract.; Learner duration remains unmeasured and does not block scaffold readiness.

### Day 008 · 2026-10-21 · CAP-52

**Instructor readiness:** `READY`. Learner complete: `false`; timebox measured: `false`; unmeasured timebox blocks this instructor status: `false`.

**Executed checks:**
- `mvn -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/capstone-m2 -Dtest=CAP52ControllerInfrastructureTest,ApplicationRuntimeIntegrationTest test`: **PASS** — 5 tests, 0 failures, 0 errors, 0 skipped; raw output [log](raw/day-008-maven.log) (`f663f4aa669c1283fa127f4b1c0b03a7ee8f09d690e0d194435bfd3a1fb2838c`).

**Separate learner acceptance runs (not used to decide scaffold readiness):**
- `mvn -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/capstone-m2 -Plearner-tests -Dtest=CAP52AcceptanceTest test`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 2 tests, 0 failures, 2 errors; TODO signal: `True`; [raw log](raw/day-008-learner-maven.log) (`846ff2621388512bcd914fc7911173c6fefdd24fdf4da854fc09b4957b9b8729`).

**Fixture SHA-256:**
- `fixtures/coverstory/CAP-52/synthetic-v1.json` — `d750ef664f24f26ae6fb72f69ff596646543c8fcfc62c79996b98a184c4ae308`

**Reset/isolation:** Each MockMvc request uses fresh in-memory decision fake/counters; actual Spring check uses a fresh random-port application context.

**Wiring:** Spring controller is component-scanned and tested; trusted identity adapter and durable decision persistence remain explicitly absent.

**Positive controls:** trusted test principal reaches the injected fake decision port once; route is registered in the actual application context.

**Negative controls:** anonymous/applicant denied, spoofed owner and malformed hash rejected before seam; actual route returns 401 without the trusted-principal adapter.

**Learner TODOs:** CAP-52 import accept decision. These are deliberately not implemented by the instructor harness.

**Last recorded learner acceptance receipts:**
- [CAP-52 result](../CAP-52/result.json); [supplementary acceptance](../CAP-52/learner-acceptance.json); recorded status `NOT_COMPLETE_TODO_REMAINS`. These are prior separated TODO evidence, not counted in today's instructor PASS.
**Remaining requirements:** Implement saved decision seam; production authentication and durable idempotency are not supplied by this day.; Learner duration remains unmeasured and does not block scaffold readiness.

### Day 009 · 2026-10-22 · CAP-53

**Instructor readiness:** `READY`. Learner complete: `false`; timebox measured: `false`; unmeasured timebox blocks this instructor status: `false`.

**Executed checks:**
- `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python3 -m unittest ai.tests.test_coverstory_cap53_infrastructure`: **PASS** — 3 tests, 0 failures, 0 errors, 0 skipped; raw output [log](raw/day-009-test_coverstory_cap53_infrastructure.log) (`199ba496f288116d36c62f29346d4fbf0bd75745a1c893abfcbdaa4c6d5b1033`).

**Separate learner acceptance runs (not used to decide scaffold readiness):**
- `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python3 -m unittest discover -s ai/coverstory/tests -p test_cap_53.py`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 4 tests, 0 failures, 4 errors; TODO signal: `True`; [raw log](raw/day-009-learner-test_cap_53.log) (`a09370ea46dd8a0a0e6ed6afdb7c9645422db34fd643fbfbeb2f737061a85c1d`).

**Fixture SHA-256:**
- `fixtures/coverstory/CAP-53/synthetic-v1.json` — `7b16ab2608dcb67cf43ccb566eb8e736002c24ebd49c7295ced7dec07f6844f9`
- `fixtures/coverstory/CAP-53/corpus/manifest.json` — `f26a5ebbfbf74104010faeb9f4d97e15c71ef33363d93c32ad704c423ce76e1b`
- `fixtures/coverstory/CAP-53/corpus/a.json` — `c237a760c9644d1b1c7ad295cd9f4fe9b192268b5146e2240dada3fe58c2a7d1`
- `fixtures/coverstory/CAP-53/corpus/b.json` — `07a2e334d1a6fd2d2f09c419382c4263eb578407b4076fb20e30cbe09e2f2ae8`

**Reset/isolation:** Immutable checked-in corpus plus TemporaryDirectory cases; every oracle invocation rebuilds its state and does not call the learner loader.

**Wiring:** Typed PublicWording/LoadError seam and acceptance tests compile; test-only reference loader does not implement the learner function.

**Positive controls:** allowlisted filename ordering, exact source/version labels and repeatable corpus read; unlisted file ignored.

**Negative controls:** path traversal rejected before outside read, duplicate file/source-version, restricted content and atomic rejection.

**Learner TODOs:** CAP-53 load_wording. These are deliberately not implemented by the instructor harness.

**Last recorded learner acceptance receipts:**
- [CAP-53 result](../CAP-53/result.json); recorded status `TODO_RETAINED`. These are prior separated TODO evidence, not counted in today's instructor PASS.
**Remaining requirements:** Implement allowlisted local-file loading; no online/provider import is in scope.; Learner duration remains unmeasured and does not block scaffold readiness.

### Day 010 · 2026-10-23 · CAP-54

**Instructor readiness:** `READY`. Learner complete: `false`; timebox measured: `false`; unmeasured timebox blocks this instructor status: `false`.

**Executed checks:**
- `mvn -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/capstone-m2 -Dtest=CAP54InstructorInfrastructureTest test`: **PASS** — 6 tests, 0 failures, 0 errors, 0 skipped; raw output [log](raw/day-010-maven.log) (`fad8505d930f7e90972b10318755dab4717269b74532fed1be89ab38f5cfcfcf`).

**Separate learner acceptance runs (not used to decide scaffold readiness):**
- `mvn -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/capstone-m2 -Plearner-tests -Dtest=CAP54AcceptanceTest test`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 5 tests, 0 failures, 5 errors; TODO signal: `True`; [raw log](raw/day-010-learner-maven.log) (`0947a206935a923a6c13188cf9d1d4b4b5e38b5fcbf68b18b8d45a7ceceb9978`).

**Fixture SHA-256:**
- `fixtures/coverstory/CAP-54/synthetic-v1.json` — `270e204da80c8c572c9e1b706a9a5004c50b3e25f5e377f53c746872cd0a29bb`

**Reset/isolation:** Test-only fixture port graph is reconstructed per test from immutable synthetic owner/revision rows.

**Wiring:** Instructor fixture adapter implements actual Application module OwnedJourneyReader/ApplicationStateReader ports. No production authentication/session adapter or resume endpoint is claimed.

**Positive controls:** owner-bound lookup and exact authoritative application revision; safe status/action summaries and bounded owner listing.

**Negative controls:** foreign and absent UUID produce same safe 404 before application read; UUID-only access, private-field output, oversized page and foreign cursor.

**Learner TODOs:** CAP-54 resume and active-list seam. These are deliberately not implemented by the instructor harness.

**Last recorded learner acceptance receipts:**
- [CAP-54 result](../CAP-54/result.json); recorded status `TODO_RETAINED`. These are prior separated TODO evidence, not counted in today's instructor PASS.
**Remaining requirements:** Implement only the owner-scoped query/adapter methods; authenticated runtime resume, persistence and private payload access remain outside this day's test harness.; Learner duration remains unmeasured and does not block scaffold readiness.
