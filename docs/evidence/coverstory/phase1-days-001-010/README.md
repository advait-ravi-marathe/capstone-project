# Phase 1 Days 001–010 instructor readiness

This status is based on executable instructor scaffolds, resettable synthetic inputs, positive/negative controls, and the module boundary named for each day. It is independent of learner implementation and elapsed-time measurement. A READY scaffold is not learner completion or mastery.

Recorded UTC: `2026-10-15T02:24:59.101211+00:00`
Canonical source SHA-256: `e96ae484e4c2c771e44d126f49127ee3ca005c766eb7abcb378e059a236e09de`

**Instructor result:** 10/10 days READY; 20 commands, 72 tests, 0 test failures, 0 test errors, and 4 frontend build/security checks passed.

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
| 010 | CAP-54 / — | READY | 7 | CAP-54 resume and active-list seam |

## Per-day evidence

### Day 001 · 2026-10-14 · CAP-40

**Instructor readiness:** `READY`. Learner complete: `false`; timebox measured: `false`; unmeasured timebox blocks this instructor status: `false`.

**Executed checks:**
- `/tmp/apache-maven-3.10.0/bin/mvn -Dmaven.repo.local=/tmp/maven-repo -s /tmp/maven-settings.xml -Dtest=CAP40ScaffoldInfrastructureTest,ShortlistSelectionControllerInfrastructureTest,ApplicationRuntimeIntegrationTest test`: **PASS** — 7 tests, 0 failures, 0 errors, 0 skipped; raw output (local-only raw output) (`4b119fd3556bfee47806513087c4811d24abd997789fb57110d5b0c8136cc54d`).
- `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python -m unittest ai.tests.test_coverstory_cap41_infrastructure`: **PASS** — 4 tests, 0 failures, 0 errors, 0 skipped; raw output (local-only raw output) (`feabb4add9e2ca3374e538560634d1ec97dc4f4ce89aab12f3d726e36ea9b2f6`).
- `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python -m unittest ai.tests.test_coverstory_cap41_http`: **PASS** — 5 tests, 0 failures, 0 errors, 0 skipped; raw output (local-only raw output) (`03e06225f66a9a758404265502b8dd6002403ba4280b590bd67177d47855fb67`).
- `npm run build`: **PASS**; raw output (local-only raw output) (`b85a759a4cc1d48feff941581afd612b90990f4db3edb47fd62f30efae4507d8`).
- `npm run test:security`: **PASS**; raw output (local-only raw output) (`6995d3b85fe7a4068be8cbecc69d5c0f74c11166578987806563cb1126b98ff1`).

**Separate learner acceptance runs (not used to decide scaffold readiness):**
- `/tmp/apache-maven-3.10.0/bin/mvn -Dmaven.repo.local=/tmp/maven-repo -s /tmp/maven-settings.xml -Plearner-tests -Dtest=CAP40AcceptanceTest test`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 8 tests, 0 failures, 8 errors; TODO signal: `True`; (local-only raw output) (`21c5c687c79bca1b2d613823a7fcbdb190476cba0e51ebc276920fbfed2913b7`).
- `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python -m unittest ai.tests.test_coverstory_cap41_acceptance`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 6 tests, 0 failures, 9 errors; TODO signal: `True`; (local-only raw output) (`d55e69a846089694683b2d791c55c9554ca857d4c8fd924c7a56d95f46ac1d98`).

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
- `/tmp/apache-maven-3.10.0/bin/mvn -Dmaven.repo.local=/tmp/maven-repo -s /tmp/maven-settings.xml -Dtest=CAP42InstructorInfrastructureTest,CAP43InstructorInfrastructureTest,CatalogInstructorHarnessTest,ApplicationRuntimeIntegrationTest test`: **PASS** — 10 tests, 0 failures, 0 errors, 0 skipped; raw output (local-only raw output) (`5b82c4be5eb3d8a6e8b041c9b7dd215ff74e0d554978e6f9bfccb8ba70460eef`).

**Separate learner acceptance runs (not used to decide scaffold readiness):**
- `/tmp/apache-maven-3.10.0/bin/mvn -Dmaven.repo.local=/tmp/maven-repo -s /tmp/maven-settings.xml -Plearner-tests -Dtest=CAP42SqlAcceptanceTest,CAP43AcceptanceTest test`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 9 tests, 2 failures, 5 errors; TODO signal: `True`; (local-only raw output) (`9866c609c131a027f7c9e330e3cd9d6c74c96108e6ea976746c6d87321d989c6`).

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
- `/tmp/apache-maven-3.10.0/bin/mvn -Dmaven.repo.local=/tmp/maven-repo -s /tmp/maven-settings.xml -Dtest=CAP45InstructorInfrastructureTest test`: **PASS** — 2 tests, 0 failures, 0 errors, 0 skipped; raw output (local-only raw output) (`f0700fe22cc18fd8e0580f434020a6a3a96948e3541640814f89ea0736ddcb7d`).
- `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python -m unittest ai.tests.test_coverstory_cap44_infrastructure`: **PASS** — 2 tests, 0 failures, 0 errors, 0 skipped; raw output (local-only raw output) (`a6707d4eff38012da33ddc34bc6dc076a3b17151cfc9aef223c8790cbe64c3da`).

**Separate learner acceptance runs (not used to decide scaffold readiness):**
- `/tmp/apache-maven-3.10.0/bin/mvn -Dmaven.repo.local=/tmp/maven-repo -s /tmp/maven-settings.xml -Plearner-tests -Dtest=CAP45SqlAcceptanceTest test`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 4 tests, 4 failures, 0 errors; TODO signal: `True`; (local-only raw output) (`5e109b61b8d5d5247a310267bae438cae2ea11ffe9de6b1e5e6a31583c9d8a8b`).
- `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python -m unittest discover -s ai/coverstory/tests -p test_cap_44.py`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 4 tests, 0 failures, 4 errors; TODO signal: `True`; (local-only raw output) (`bcb364a9fda14165ad747ddf246502c244a56a83a3a32b7a96c0af2642eba0fc`).

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
- `/tmp/apache-maven-3.10.0/bin/mvn -Dmaven.repo.local=/tmp/maven-repo -s /tmp/maven-settings.xml -Dtest=CAP46InstructorInfrastructureTest,CatalogInstructorHarnessTest test`: **PASS** — 7 tests, 0 failures, 0 errors, 0 skipped; raw output (local-only raw output) (`0d008a6a7ebd0d843173984b4c8b57100be85981b7fa7b241f915773f27062c2`).
- `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python -m unittest ai.tests.test_coverstory_cap47_infrastructure`: **PASS** — 2 tests, 0 failures, 0 errors, 0 skipped; raw output (local-only raw output) (`a6707d4eff38012da33ddc34bc6dc076a3b17151cfc9aef223c8790cbe64c3da`).

**Separate learner acceptance runs (not used to decide scaffold readiness):**
- `/tmp/apache-maven-3.10.0/bin/mvn -Dmaven.repo.local=/tmp/maven-repo -s /tmp/maven-settings.xml -Plearner-tests -Dtest=CAP46AcceptanceTest test`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 4 tests, 0 failures, 4 errors; TODO signal: `True`; (local-only raw output) (`de68931c48f8185dd6f38298a2443967c182bd84e40f819389a4f4e3f536274d`).
- `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python -m unittest discover -s ai/coverstory/tests -p test_cap_47.py`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 4 tests, 0 failures, 4 errors; TODO signal: `True`; (local-only raw output) (`918a7f2f48c296da5cb517c4a719fa9c22b2dfabe5bef581af84c50b2566d43d`).

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
- `/tmp/apache-maven-3.10.0/bin/mvn -Dmaven.repo.local=/tmp/maven-repo -s /tmp/maven-settings.xml -Dtest=CAP48InstructorInfrastructureTest test`: **PASS** — 3 tests, 0 failures, 0 errors, 0 skipped; raw output (local-only raw output) (`94ba7f6f17ffeb0f78fa1fc10bf9e5c54b075861ce2ecf0db889f34a773ddf6a`).
- `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python -m unittest ai.tests.test_coverstory_cap49_infrastructure`: **PASS** — 2 tests, 0 failures, 0 errors, 0 skipped; raw output (local-only raw output) (`a6707d4eff38012da33ddc34bc6dc076a3b17151cfc9aef223c8790cbe64c3da`).

**Separate learner acceptance runs (not used to decide scaffold readiness):**
- `/tmp/apache-maven-3.10.0/bin/mvn -Dmaven.repo.local=/tmp/maven-repo -s /tmp/maven-settings.xml -Plearner-tests -Dtest=CAP48SqlAcceptanceTest test`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 4 tests, 4 failures, 0 errors; TODO signal: `True`; (local-only raw output) (`d4fcfa2bfe7538815af92c20451e345e49d5981e1ddea3b7ca7e328cc2114114`).
- `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python -m unittest discover -s ai/coverstory/tests -p test_cap_49.py`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 3 tests, 0 failures, 10 errors; TODO signal: `True`; (local-only raw output) (`fd7d5378cedac579ab7b9ad84e8fb3e6208f6c5dae81d0db0cefab377b479a98`).

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
- `/tmp/apache-maven-3.10.0/bin/mvn -Dmaven.repo.local=/tmp/maven-repo -s /tmp/maven-settings.xml -Dtest=CAP50InstructorInfrastructureTest,CAP51InstructorInfrastructureTest,CatalogInstructorHarnessTest test`: **PASS** — 8 tests, 0 failures, 0 errors, 0 skipped; raw output (local-only raw output) (`1346b40e550f7cb14e201dc02699951be3a7276599bafb2cb90b55303ec8ff7c`).

**Separate learner acceptance runs (not used to decide scaffold readiness):**
- `/tmp/apache-maven-3.10.0/bin/mvn -Dmaven.repo.local=/tmp/maven-repo -s /tmp/maven-settings.xml -Plearner-tests -Dtest=CAP50AcceptanceTest,CAP51SqlAcceptanceTest test`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 7 tests, 5 failures, 2 errors; TODO signal: `True`; (local-only raw output) (`77952dc3da4eed3df05334f31b1a6f7fa1606d8c7dc47fc86bc0579c9c98e12d`).

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
- `/tmp/apache-maven-3.10.0/bin/mvn -Dmaven.repo.local=/tmp/maven-repo -s /tmp/maven-settings.xml -Dtest=CAP165HarnessDiscriminationTest,CatalogInstructorHarnessTest test`: **PASS** — 5 tests, 0 failures, 0 errors, 0 skipped; raw output (local-only raw output) (`5e106a7ceb57cc83f1f4b7d9ad14618efcf6597ae5983d847046f01ed4284f5a`).

**Separate learner acceptance runs (not used to decide scaffold readiness):**
- `/tmp/apache-maven-3.10.0/bin/mvn -Dmaven.repo.local=/tmp/maven-repo -s /tmp/maven-settings.xml -Plearner-tests -Dtest=CAP165AcceptanceTest test`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 4 tests, 1 failures, 3 errors; TODO signal: `True`; (local-only raw output) (`0ef31cf2160436206ba5dfa7f131b40558e828f91723ec686c98645d4893d4ba`).

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
- `/tmp/apache-maven-3.10.0/bin/mvn -Dmaven.repo.local=/tmp/maven-repo -s /tmp/maven-settings.xml -Dtest=CAP52ControllerInfrastructureTest,ApplicationRuntimeIntegrationTest test`: **PASS** — 5 tests, 0 failures, 0 errors, 0 skipped; raw output (local-only raw output) (`7ddd907b0966e780e9dfa5a3f831a8a0ce091ed8b4c2c68412248aa7e3983eed`).

**Separate learner acceptance runs (not used to decide scaffold readiness):**
- `/tmp/apache-maven-3.10.0/bin/mvn -Dmaven.repo.local=/tmp/maven-repo -s /tmp/maven-settings.xml -Plearner-tests -Dtest=CAP52AcceptanceTest test`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 2 tests, 0 failures, 2 errors; TODO signal: `True`; (local-only raw output) (`8c75b7ddfe886b01ca80419d46d199eea27fce298217fac70b27b0cbea884e25`).

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
- `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python -m unittest ai.tests.test_coverstory_cap53_infrastructure`: **PASS** — 3 tests, 0 failures, 0 errors, 0 skipped; raw output (local-only raw output) (`199ba496f288116d36c62f29346d4fbf0bd75745a1c893abfcbdaa4c6d5b1033`).

**Separate learner acceptance runs (not used to decide scaffold readiness):**
- `/opt/codex/runtimes/codex-primary-runtime/dependencies/python/bin/python -m unittest discover -s ai/coverstory/tests -p test_cap_53.py`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 4 tests, 0 failures, 4 errors; TODO signal: `True`; (local-only raw output) (`50496ba51b856dc5fd3997dbeb835f1e550dda85a0e8c36819054f3f9aa2b9dc`).

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
- `/tmp/apache-maven-3.10.0/bin/mvn -Dmaven.repo.local=/tmp/maven-repo -s /tmp/maven-settings.xml -Dtest=CAP54InstructorInfrastructureTest test`: **PASS** — 6 tests, 0 failures, 0 errors, 0 skipped; raw output (local-only raw output) (`938caf6736ac52a3e1cec4f7b97f9265d11db27afbc7f221441bcc1a92a29f5f`).
- `/tmp/apache-maven-3.10.0/bin/mvn -Dmaven.repo.local=/tmp/maven-repo -s /tmp/maven-settings.xml -Dcoverstory.cap54.browser=true -Dtest=CAP54HttpRuntimeIntegrationTest test`: **PASS** — 1 tests, 0 failures, 0 errors, 0 skipped; raw output (local-only raw output) (`c4fc1a9f352b6bdc38440463d741b561c015b17e63c571caf8fe7a93c4f8c695`).
- `npm run build`: **PASS**; raw output (local-only raw output) (`459ce32570969e32e70c0813a727f8ef485bb79197b62cfbecb80431fee47090`).
- `npm run test:security`: **PASS**; raw output (local-only raw output) (`6995d3b85fe7a4068be8cbecc69d5c0f74c11166578987806563cb1126b98ff1`).

**Separate learner acceptance runs (not used to decide scaffold readiness):**
- `/tmp/apache-maven-3.10.0/bin/mvn -Dmaven.repo.local=/tmp/maven-repo -s /tmp/maven-settings.xml -Plearner-tests -Dtest=CAP54AcceptanceTest test`: **EXPECTED_UNIMPLEMENTED_LEARNER_OUTCOME** — 5 tests, 0 failures, 5 errors; TODO signal: `True`; (local-only raw output) (`6e6c5ca00aea866480bb1d5badae9e445a87ebed982282667671a58726a1bd4f`).

**Fixture SHA-256:**
- `fixtures/coverstory/CAP-54/synthetic-v1.json` — `270e204da80c8c572c9e1b706a9a5004c50b3e25f5e377f53c746872cd0a29bb`

**Reset/isolation:** Test-only fixture port graph is reconstructed per test from immutable synthetic owner/revision rows.

**Wiring:** Actual Spring /api/session and /api/v1/me/journeys routes resolve only a trusted request principal. Application-owned JDBC reads join the owner-scoped journey to the exact linked application revision. A test-only synthetic session and independent PostgreSQL adapter exercise the actual HTTP/browser path; production auth binding and the saved CAP54Seam remain learner work and fail closed.

**Positive controls:** owner-bound lookup and exact authoritative application revision; safe status/action summaries and bounded owner listing.

**Negative controls:** foreign and absent UUID produce same safe 404 before application read; UUID-only access, private-field output, oversized page and foreign cursor.

**Learner TODOs:** CAP-54 resume and active-list seam. These are deliberately not implemented by the instructor harness.

**Last recorded learner acceptance receipts:**
- [CAP-54 result](../CAP-54/result.json); recorded status `TODO_RETAINED`. These are prior separated TODO evidence, not counted in today's instructor PASS.
**Remaining requirements:** Bind the trusted servlet principal to the deployment's authenticated session adapter; no login provider or real credential flow is included. Implement the CAP54Seam learner methods; the configured application route returns safe 501 until they are complete. Learner duration remains unmeasured.
