# Starter coverage and verification

## Assignment contract coverage

The final plan contains 594 consecutive daily primary assignments and six linked component cards (600 contracts total, 44 sprints). Phase 1 has 228 days; Phase 2 has 366. Per parent direction, the consumer-local project input derivative fetched from pinned delivery commit `0e70938d94804c23029b39de106b8e7f61dccd8c` is the authoritative implementation input. It is **1,736,364 bytes**, SHA-256 `ce89674349261792888284ced50a33f3aeab20d95a6ac5a3675c179454ed27e5`; the exact archive hash and size differ from the original Library record by design. Its ZIP integrity and every internal member checksum pass, and the final curriculum JSON is byte-exact at SHA-256 `e96ae484e4c2c771e44d126f49127ee3ca005c766eb7abcb378e059a236e09de`.

The source package is preserved under [`docs/source-inputs/2026-10-14`](source-inputs/2026-10-14/README.md). Its operating rules are explicitly a project-only extract; the source manifest records that provenance and its exclusions. [`curriculum-index.json`](assignments/coverstory/curriculum-index.json) and 594 per-day guides under [`saved-days`](assignments/coverstory/saved-days/) map every primary and linked component from the exact descriptions, including task-specific acceptance text and source-written paths where supplied. A corrected parser recognizes labelled path bullets: 228 primary cards explicitly enumerate planned files and 366 provide no planned repository path. A source path is a planned name, not proof the file exists. The checker independently recomputes all 600 description hashes, dates, phase/component counts, linked relationships, task and acceptance excerpts, and path lists from canonical JSON. Complete descriptions remain byte-exact in canonical JSON. See [harness coverage](assignments/coverstory/HARNESS-COVERAGE.md) for separate scaffold, harness, readiness, and learner-result counts.

| Area | Runnable scaffolds present | Learner work preserved | Current status |
| --- | --- | --- | --- |
| Day 001 primary CAP-40 | Composite-key DTOs, synthetic fixture, positive and deliberately defective instructor witnesses, focused acceptance suite | `CAP40Seam.uniqueSelections` | 2 instructor checks pass; 7 learner tests stop at TODO (4 failures, 3 errors); one instructor command took 4.03s; learner task time remains unmeasured |
| Linked Day 001 component CAP-41 | Synthetic public wording fixture, typed citation values, lowercase whitespace tokenizer, positive/defective citation-order witness | `retrieve(query, documents, k)` | 3 infrastructure checks pass; 5 learner acceptance methods report 6 TODO errors; citations/ordering remain unimplemented |
| CAP-42 PostgreSQL aggregation | Java harness, schema, fixture | `learner-query.sql` | 6 learner acceptance tests remain blocked by the SQL TODO; the shared PostgreSQL fixture check passes |
| CAP-43 event policy and request validation | Spring MVC scaffolds and acceptance tests | Java TODO methods | 11 learner acceptance tests remain blocked by TODOs; both instructor infrastructure tests pass |
| CAP-44 retrieval ranking | Python types, fixture, infrastructure and acceptance tests | `rank_runbooks` | Python infrastructure suite passes; ranking acceptance tests remain blocked on TODO (15 reported test errors) |
| CAP-45 event reconciliation | PostgreSQL harness, schema, fixture, tests | `learner-query.sql` | 5 learner acceptance tests remain blocked by the SQL TODO; the shared PostgreSQL fixture check passes |
| CoverStory CAP-42/48 SQL | Separate queries, schema, deterministic fixtures, embedded PostgreSQL instructor checks | Learner aggregation/date-spine queries | Both DB setup checks pass; focused acceptance runs reach the SQL TODO's zero-row result |
| CoverStory CAP-44/46/50/53/56/59/61/62/165 | Typed Python/Java learner seams, synthetic fixtures, acceptance suites and instructor checks | Ranking, review queue, import counts, safe loading, evaluation, citation/answer gates, bounded cancellation and source identity | Instructor fixtures pass; focused behavior runs stop at learner TODOs. No learner behavior is marked complete |
| CAP-40 / CAP-41 first-day contracts | Synthetic pair/citation fixtures; test-only correct and intentionally defective witnesses | Composite shortlist identity and public wording ranking/citations | CAP-40 instructor witness rejects version-only and sort defects; CAP-41 witness rejects reordered/stale citations. Learner suites remain TODO-only |
| CAP-54 owned journey resume | Owner/revision fixture, typed Application port seams, instructor owner-scope oracle | Authenticated session, safe 404 equivalence, authoritative resume action, owner-bound cursors | Instructor oracle rejects UUID-only access. Public catalog `200` integration remains deferred while the starter API returns `501`; auth/learner seam remains TODO |
| CAP-55 offline partition trace | Synthetic recorded-offset fixture, typed publication/observation DTOs, positive/negative instructor oracle | Same-source key locality, increasing offsets, truthful evidence provenance | Correct source-A and source-B shared-partition trace passes; reordered/split/false-live evidence fails. No broker is installed or claimed |
| CoverStory React app | Vite/React, session/cache security boundary, responsive health-first synthetic discovery and shortlist/compare slice, semantic tokens, Inter | Authentication, owned resume, real catalog API, save/export, all other categories, and transaction routes remain unimplemented | Build and security smoke pass; selected source previews inspected; Chromium 390×844 interaction smoke passed |
| CoverStory Spring/PostgreSQL modules | Spring Boot API shell, eight ownership packages/ports, PostgreSQL schemas and constraint smoke test | Business application behavior remains TODO/ports | Embedded PostgreSQL schema test passes; no production datasource/auth integration |
| Security, retention, payment and dispatch contracts | AES-GCM field cipher infrastructure; integer minor-unit Money type; idempotency uniqueness, immutable journal, distinct audit/analytics and simulated dispatch schema | Key-management provider, consent/retention policy, payment and dispatch workflows | Crypto smoke and PostgreSQL schema checks pass; legal durations intentionally unspecified |
| Daily primary/component mapping | 594 source-backed task guides and independent source checker; 15 scaffold families authored with fixtures and instructor checks | Fifteen primary suites plus linked CAP-41 were run on this candidate and stopped at TODO seams; 579 other primaries are mapped-only | No assignment is marked ready; instructor setup timing does not establish learner task duration; see [harness coverage](assignments/coverstory/HARNESS-COVERAGE.md) and [candidate validation receipt](assignments/coverstory/evidence/harness-candidate-2026-10-14.md) |

## Verified preflight

- Repository path: `/workspace/capstone-project`.
- Remote: `origin https://github.com/advait-ravi-marathe/capstone-project.git`.
- Branch: `feature/coverstory-capstone-starter`; initial checkout HEAD `4f362eb30739d73a4e90405a30aada6401145afe`; initial worktree clean. The feature branch was created for isolated, additive setup work.
- The checkout is isolated from the Mac task. No Mac files were read or synchronized.
- Java runtime 21.0.12.1 and Python 3.12.14 are present. Maven 3.10.0 was downloaded from the official Apache distribution in `/tmp` and its SHA-512 matched Apache’s published checksum. The Java runtime image includes the compiler module but omits `javac` and the Java API release archive; Maven validation therefore used a temporary launcher for `jdk.compiler` with source/target 21 and a temporary Maven settings file built from the existing environment proxy (no repository or trust/certificate configuration was changed). The executor still has no system `mvn` command or normal JDK installation.
- No repository `AGENTS.md`, `.agents`, or `.codex` instructions were found.
- The project derivative is the authorized input for implementation. Its internal member checksums pass and its exact curriculum payload matches the frozen source hash as described above.
- Per parent direction, the selected visual bundle is the authorized design input. It is **2,795,378 bytes**, SHA-256 `68443daaa4c59147af5622e9931578ee82f7e8d26b39784818dff34c398a4baa`; ZIP integrity and all **118** member checksums pass. The 87 wireframes, 24 previews, CSS/theme/inventory, and generators are preserved byte-exact under [`docs/source-inputs/visual-2026-10-14`](source-inputs/visual-2026-10-14/README.md). Four actual previews were inspected. The selected scope intentionally excludes the full older design archive and demo assets.

## Implemented CoverStory setup

- `src/main/java/coverstory/modules/` now has explicit public ports and value types for Catalog, Shortlist, Application, Payments, Policy, Support, business Audit, and Analytics. There is no business workflow implementation yet. The static boundary suite checks these module packages and prevents cross-module persistence imports.
- `src/main/resources/db/schema/ownership.sql` creates the module schemas. `constraints.sql` establishes owner-scoped application journeys and revisions, integer minor-unit money with currency, hashed durable payment idempotency, append-only payment journal, distinct dispatch state, and separate business-audit and allowlisted analytics tables.
- `AesGcmFieldCipher` provides authenticated encryption with caller-supplied AAD and key ID/version metadata. The provider is only an interface; key management, durable key storage, rotation operations, retention policies, and consent workflows are not implemented.
- `web/` contains a built React + TypeScript client shell using TanStack Query, semantic Tailwind tokens, and open-source Inter. Private query keys are session scoped; account changes/logout cancel and clear private queries and response generations guard stale results. Health discovery remains public during session checks. The React view uses clearly labeled local synthetic layout fixtures; no real catalog API, authentication, or transaction flow exists.
- `src/main/java/coverstory/api/StarterApiController.java` intentionally returns `401` for session resolution and `501` for the unfinished catalog endpoint. The local Spring Boot process was started and both HTTP responses were observed.
- The module contract and architecture guides include system context, module ownership, journey/payment sequence, data flow, class relationships, and interview prompts. Selected visual previews were inspected: the reference uses a warm neutral canvas, thin borders, four pastel category entry cards, a simple journey stepper, compact source labels, Unknown states, and desktop/mobile layouts. Current implementation keeps the React/Spring/PostgreSQL skeleton, adopts that restrained layout and category palette, and uses the governing Inter/system-ui sans stack rather than older serif heading samples. This is a focused design reconciliation, not recreation of all 87 wireframes.
- Day 001’s verified source card has curriculum SHA-256 `e96ae484e4c2c771e44d126f49127ee3ca005c766eb7abcb378e059a236e09de` and description SHA-256 `b9c9ea8e9a53cde75b4a67064f1ea1fa5f3ba0126e5788063007f1235a4687a0`. The mapped scaffold and measured setup bound are documented in [CAP-40](assignments/coverstory/CAP-40.md). Current acceptance failures stop at learner TODO stubs; prior reports under [CAP-40 evidence](evidence/coverstory/CAP-40/) remain historical for this candidate.
- Fifteen focused CoverStory assignment families now have separate instructor fixture checks and learner acceptance suites: CAP-40/42/44/46/48/50/53/54/55/56/57/59/61/62/165. CAP-42/48/57 execute against actual embedded PostgreSQL; CAP-44/53/56/59/61/62 are Python learner seams; CAP-46/50/54/55/57/165 are Java learner seams. The current candidate's 15 primary suites and linked CAP-41 suite stop at TODOs; no learner outcome is recorded as mastery. The remaining 579 primary guides retain exact source tasks and acceptance cases without authored family harnesses. 228 contracts enumerate a planned path; 366 do not. Where source provides no path, the guide records that fact rather than presenting an inferred path as source truth. Visual design input is checksum-verified at member level and a health discovery/compare shell is implemented; broader screens still need code-owner mapping and implementation.

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
python3 scripts/materialize_assignment_harnesses.py
```

See [visual reconciliation](design/visual-reconciliation.md) for the inspected input screens and implemented shell scope.

Run the learner ranking suite separately to observe expected TODO failures:

```sh
python3 -m unittest discover -s ai/tests -p test_ranking.py
```

The frontend is pinned in `web/package-lock.json`. `spring-boot:run` also starts, and live local requests return the explicit shell responses documented above (`401` session and `501` catalog). Verified commands in this executor: `npm run build`; `npm run test:security`; `python3 scripts/check_scaffold.py`; `./scripts/test_field_cipher.sh`; curriculum integrity test (1 passed); `test_infrastructure.py` (6 passed); `test_coverstory_cap41_infrastructure.py` (2 passed); and the Maven default suite (9 passed) using the temporary launcher/settings described above. Targeted Day 001 learner acceptance is recorded separately as expected TODO failures. Enabling `-Plearner-tests` also produces expected failures in legacy CAP-42–45 because those TODOs are preserved. No merge, deployment, Mac access, or Scrum edits were performed.
