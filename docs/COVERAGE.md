# Starter coverage and verification

## Assignment contract coverage

The final plan contains 594 consecutive daily primary assignments and six linked component cards (600 contracts total, 44 sprints). Phase 1 has 228 days; Phase 2 has 366. The project-only input archive was fetched from the pinned input-delivery branch commit `0e70938d94804c23029b39de106b8e7f61dccd8c`, then checked at **1,736,364 bytes** and SHA-256 `ce89674349261792888284ced50a33f3aeab20d95a6ac5a3675c179454ed27e5`. ZIP integrity passed. All 11 extracted package members passed `SHA256SUMS`; the original final curriculum JSON is preserved byte-exact and matches `e96ae484e4c2c771e44d126f49127ee3ca005c766eb7abcb378e059a236e09de`.

The source package is preserved under [`docs/source-inputs/2026-10-14`](source-inputs/2026-10-14/README.md). Its operating rules are explicitly a project-only extract; the source manifest records that provenance and its exclusions. [`curriculum-index.json`](assignments/coverstory/curriculum-index.json) and 594 per-day guides under [`saved-days`](assignments/coverstory/saved-days/) now map every primary and linked component from the exact descriptions, including task-specific acceptance text and source-written path references where supplied. Among the 594 primary cards, 152 explicitly enumerate planned paths, 76 refer to repository paths without an enumerable planned-path list, and 366 give no explicit repository path. Those counts are visible in the index; path references are not claims that files exist. A checker validates all 600 description hashes, consecutive primary dates, phase/component counts, linked relationships, and task/acceptance excerpts. Complete descriptions remain byte-exact in canonical JSON. This provides complete source-backed assignment mapping; it does **not** mean all 594 learner seams have runnable code scaffolds, individually rehearsed tests, or learner completion.

| Area | Runnable scaffolds present | Learner work preserved | Current status |
| --- | --- | --- | --- |
| Day 001 primary CAP-40 | Composite-key DTOs, synthetic fixture, instructor self-check, focused acceptance suite | `CAP40Seam.uniqueSelections` | Scaffold self-check passes; 7 learner tests run as expected TODO failures (4 failures, 3 errors); 95-minute rehearsal not run |
| Linked Day 001 component CAP-41 | Synthetic public wording fixture, typed citation values, lowercase whitespace tokenizer, instructor fixture checks | `retrieve(query, documents, k)` | 2 infrastructure checks pass; 5 learner acceptance methods report 6 expected TODO errors; citations/ordering remain unimplemented |
| CAP-42 PostgreSQL aggregation | Java harness, schema, fixture | `learner-query.sql` | 6 learner acceptance tests remain blocked by the SQL TODO; the shared PostgreSQL fixture check passes |
| CAP-43 event policy and request validation | Spring MVC scaffolds and acceptance tests | Java TODO methods | 11 learner acceptance tests remain blocked by TODOs; both instructor infrastructure tests pass |
| CAP-44 retrieval ranking | Python types, fixture, infrastructure and acceptance tests | `rank_runbooks` | Python infrastructure suite passes; ranking acceptance tests remain blocked on TODO (15 reported test errors) |
| CAP-45 event reconciliation | PostgreSQL harness, schema, fixture, tests | `learner-query.sql` | 5 learner acceptance tests remain blocked by the SQL TODO; the shared PostgreSQL fixture check passes |
| CoverStory React app | Vite/React, session/cache security boundary, responsive health-first synthetic discovery and shortlist/compare slice, semantic tokens, Inter | Authentication, owned resume, real catalog API, save/export, all other categories, and transaction routes remain unimplemented | Build and security smoke pass; selected source previews inspected; Chromium 390×844 interaction smoke passed |
| CoverStory Spring/PostgreSQL modules | Spring Boot API shell, eight ownership packages/ports, PostgreSQL schemas and constraint smoke test | Business application behavior remains TODO/ports | Embedded PostgreSQL schema test passes; no production datasource/auth integration |
| Security, retention, payment and dispatch contracts | AES-GCM field cipher infrastructure; integer minor-unit Money type; idempotency uniqueness, immutable journal, distinct audit/analytics and simulated dispatch schema | Key-management provider, consent/retention policy, payment and dispatch workflows | Crypto smoke and PostgreSQL schema checks pass; legal durations intentionally unspecified |
| Daily primary/component mapping | 594 task guides, index, 600 exact source descriptions and executable integrity/relationship checker | Card-specific learner seams remain TODO; each source's planned files/acceptance are preserved without implementing the answer | Source coverage verified for all 600; per-day harness readiness remains NOT_ESTABLISHED |

## Verified preflight

- Repository path: `/workspace/capstone-project`.
- Remote: `origin https://github.com/advait-ravi-marathe/capstone-project.git`.
- Branch: `feature/coverstory-capstone-starter`; initial checkout HEAD `4f362eb30739d73a4e90405a30aada6401145afe`; initial worktree clean. The feature branch was created for isolated, additive setup work.
- The checkout is isolated from the Mac task. No Mac files were read or synchronized.
- Java runtime 21.0.12.1 and Python 3.12.14 are present. Maven 3.10.0 was downloaded from the official Apache distribution in `/tmp` and its SHA-512 matched Apache’s published checksum. The Java runtime image includes the compiler module but omits `javac` and the Java API release archive; Maven validation therefore used a temporary launcher for `jdk.compiler` with source/target 21 and a temporary Maven settings file built from the existing environment proxy (no repository or trust/certificate configuration was changed). The executor still has no system `mvn` command or normal JDK installation.
- No repository `AGENTS.md`, `.agents`, or `.codex` instructions were found.
- The earlier Library transfer failure and its signed-URL details are historical. They were not retried; the later pinned repository delivery was used instead. The exact archive and member integrity checks are recorded above.
- A separate visual delivery branch file was fetched from pinned commit `a7570fe75a275058e1042f34794bbee9007afc05`, size **2,795,378 bytes**, SHA-256 `68443daaa4c59147af5622e9931578ee82f7e8d26b39784818dff34c398a4baa`. ZIP integrity passed and all **118** entries in its `SHA256SUMS` passed. The 87 wireframes, 24 previews, CSS/theme/inventory, and generators are preserved byte-exact under [`docs/source-inputs/visual-2026-10-14`](source-inputs/visual-2026-10-14/README.md). Four actual previews were inspected. The rest of the full private design archive and demo assets are not included in this selected bundle.

## Implemented CoverStory setup

- `src/main/java/coverstory/modules/` now has explicit public ports and value types for Catalog, Shortlist, Application, Payments, Policy, Support, business Audit, and Analytics. There is no business workflow implementation yet. The static boundary suite checks these module packages and prevents cross-module persistence imports.
- `src/main/resources/db/schema/ownership.sql` creates the module schemas. `constraints.sql` establishes owner-scoped application journeys and revisions, integer minor-unit money with currency, hashed durable payment idempotency, append-only payment journal, distinct dispatch state, and separate business-audit and allowlisted analytics tables.
- `AesGcmFieldCipher` provides authenticated encryption with caller-supplied AAD and key ID/version metadata. The provider is only an interface; key management, durable key storage, rotation operations, retention policies, and consent workflows are not implemented.
- `web/` contains a built React + TypeScript client shell using TanStack Query, semantic Tailwind tokens, and open-source Inter. Private query keys are session scoped; account changes/logout cancel and clear private queries and response generations guard stale results. Health discovery remains public during session checks. The React view uses clearly labeled local synthetic layout fixtures; no real catalog API, authentication, or transaction flow exists.
- `src/main/java/coverstory/api/StarterApiController.java` intentionally returns `401` for session resolution and `501` for the unfinished catalog endpoint. The local Spring Boot process was started and both HTTP responses were observed.
- The module contract and architecture guides include system context, module ownership, journey/payment sequence, data flow, class relationships, and interview prompts. Selected visual previews were inspected: the reference uses a warm neutral canvas, thin borders, four pastel category entry cards, a simple journey stepper, compact source labels, Unknown states, and desktop/mobile layouts. Current implementation keeps the React/Spring/PostgreSQL skeleton, adopts that restrained layout and category palette, and uses the governing Inter/system-ui sans stack rather than older serif heading samples. This is a focused design reconciliation, not recreation of all 87 wireframes.
- Day 001’s verified source card was supplied directly with curriculum SHA-256 `e96ae484e4c2c771e44d126f49127ee3ca005c766eb7abcb378e059a236e09de` and description SHA-256 `b9c9ea8e9a53cde75b4a67064f1ea1fa5f3ba0126e5788063007f1235a4687a0`. The mapped scaffold is documented in [CAP-40](assignments/coverstory/CAP-40.md). Its Java and Python acceptance runs fail only on learner TODO stubs; instructor fixture self-checks pass. Raw reports, fixture hashes, and status distinctions are in [CAP-40 evidence](evidence/coverstory/CAP-40/).
- The only card-specific runnable learner harnesses currently prepared are Day 001 CAP-40/CAP-41 and legacy generic CAP-42–45 scaffolds. The remaining daily guides retain exact source tasks, planned files when enumerated, concrete fixtures, and expected acceptance criteria, but their corresponding CoverStory tests/fixtures/code seams have not all been authored or rehearsed. The input itself sometimes refers to existing interfaces without naming a file; those records are marked as having no explicit planned path rather than assigning a guessed path. Visual design input is recovered and a health discovery/compare shell is implemented; broader screens still need code-owner mapping and implementation.

## Security and boundary audit

- Journey authorization is **not implemented**. `ApplicationPort.resumeOwned` documents that the authenticated owner must be checked, but there is no journey endpoint, application service, repository, authenticated principal, or row-level security policy. A journey UUID remains only a lookup identifier; the interface is not proof of access control.
- Private client queries are memory-only and scoped by owner plus session generation. `fetchPrivateJson` checks that generation before and after JSON parsing; logout/account changes cancel and clear private queries. An audit reproduced overlapping session checks in which a late older response reverted the active owner. A monotonically increasing session-check sequence now makes stale responses return the current session state instead. Focused smoke checks cover the race, account-switch cache clearing, late responses before and during body parsing, `401` clearing, and rejection of non-API paths.
- AES-GCM uses a 96-bit `SecureRandom` nonce and 128-bit authentication tag, binds caller-supplied AAD, and copies ciphertext buffers. Focused smoke checks reject AAD and ciphertext tampering and found no collisions in 1,000 generated nonce samples; sampling does not prove uniqueness for arbitrary volume. Key storage, separation, rotation operations, and production key limits are not implemented.
- The PostgreSQL analytics schema rejects unknown event names and screens and has no arbitrary properties, URL, email, health, or token columns. The Java event type also uses event/screen enums, accepts only a 32-byte session-scope hash value, and copies its mutable byte array. The value type cannot prove that bytes were actually hashed; no concrete analytics writer, hash/key derivation, business-audit transaction coupling, or best-effort delivery is implemented.
- The static module check passes for current source and its detector rejects both imported and fully qualified cross-module persistence references. No repositories exist yet; this is a static source boundary, not runtime enforcement.
- A targeted scan of tracked source/configuration found no credential-shaped values or private-key blocks. The check is pattern-based and is not a general secret-scanner certification.
- No `.github/workflows` files are present in the published branch. For hardening commit `23b644ef669604392e1f148e4f5be2cbbac6c034`, GitHub returned no commit status entries and no associated pull-request workflow runs; CI is not available to classify as passing or failing.

## Commands and evidence

Run the independent Python setup suite with:

```sh
python3 -m unittest discover -s ai/tests -p test_infrastructure.py
```

Verify the full saved curriculum source and generated guides with:

```sh
python3 scripts/check_coverstory_curriculum.py
python3 -m unittest discover -s scripts -p 'test_coverstory_curriculum.py'
```

Regenerate the source-backed daily guides only after editing the verified input source:

```sh
python3 scripts/materialize_coverstory_curriculum.py
```

See [visual reconciliation](design/visual-reconciliation.md) for the inspected input screens and implemented shell scope.

Run the learner ranking suite separately to observe expected TODO failures:

```sh
python3 -m unittest discover -s ai/tests -p test_ranking.py
```

The frontend is pinned in `web/package-lock.json`. `spring-boot:run` also starts, and live local requests return the explicit shell responses documented above (`401` session and `501` catalog). Verified commands in this executor: `npm run build`; `npm run test:security`; `python3 scripts/check_scaffold.py`; `./scripts/test_field_cipher.sh`; curriculum integrity test (1 passed); `test_infrastructure.py` (6 passed); `test_coverstory_cap41_infrastructure.py` (2 passed); and the Maven default suite (9 passed) using the temporary launcher/settings described above. Targeted Day 001 learner acceptance is recorded separately as expected TODO failures. Enabling `-Plearner-tests` also produces expected failures in legacy CAP-42–45 because those TODOs are preserved. No merge, deployment, Mac access, or Scrum edits were performed.
