# Starter coverage and verification

## Assignment contract coverage

The requested final plan contains 594 consecutive daily primary assignments and six linked component cards (600 contracts total, 44 sprints). Phase 1 is 14 October 2026 through 29 May 2027 (228 days), followed by 366 days of deeper mastery. The authoritative saved-contract archive and final operating record were not available as local bytes in this executor, but the parent supplied the exact verified Day 001 card text separately. This checkout now maps that one primary card (CAP-40) and linked component (CAP-41); it does **not** contain a verified mapping of the other 598 contracts. Do not infer missing titles, dates, tests, acceptance criteria, or completion state. Materialize and integrity-check the remaining authoritative package before claiming complete assignment coverage. No generic placeholder-only assignments are represented as implemented.

| Area | Runnable scaffolds present | Learner work preserved | Current status |
| --- | --- | --- | --- |
| Day 001 primary CAP-40 | Composite-key DTOs, synthetic fixture, instructor self-check, focused acceptance suite | `CAP40Seam.uniqueSelections` | Scaffold self-check passes; 7 learner tests run as expected TODO failures (4 failures, 3 errors); 95-minute rehearsal not run |
| Linked Day 001 component CAP-41 | Synthetic public wording fixture, typed citation values, lowercase whitespace tokenizer, instructor fixture checks | `retrieve(query, documents, k)` | 2 infrastructure checks pass; 5 learner acceptance methods report 6 expected TODO errors; citations/ordering remain unimplemented |
| CAP-42 PostgreSQL aggregation | Java harness, schema, fixture | `learner-query.sql` | 6 learner acceptance tests remain blocked by the SQL TODO; the shared PostgreSQL fixture check passes |
| CAP-43 event policy and request validation | Spring MVC scaffolds and acceptance tests | Java TODO methods | 11 learner acceptance tests remain blocked by TODOs; both instructor infrastructure tests pass |
| CAP-44 retrieval ranking | Python types, fixture, infrastructure and acceptance tests | `rank_runbooks` | Python infrastructure suite passes; ranking acceptance tests remain blocked on TODO (15 reported test errors) |
| CAP-45 event reconciliation | PostgreSQL harness, schema, fixture, tests | `learner-query.sql` | 5 learner acceptance tests remain blocked by the SQL TODO; the shared PostgreSQL fixture check passes |
| CoverStory React app | Vite/React shell, React Query session/private-cache scoping, semantic Tailwind tokens, open-source Inter | API-backed catalog rendering and auth are intentionally not implemented | Frontend build passes; source design archive not inspected |
| CoverStory Spring/PostgreSQL modules | Spring Boot API shell, eight ownership packages/ports, PostgreSQL schemas and constraint smoke test | Business application behavior remains TODO/ports | Embedded PostgreSQL schema test passes; no production datasource/auth integration |
| Security, retention, payment and dispatch contracts | AES-GCM field cipher infrastructure; integer minor-unit Money type; idempotency uniqueness, immutable journal, distinct audit/analytics and simulated dispatch schema | Key-management provider, consent/retention policy, payment and dispatch workflows | Crypto smoke and PostgreSQL schema checks pass; legal durations intentionally unspecified |
| Daily primary/component mapping | No verified contract files present | N/A | Blocked on authoritative contract archive |

## Verified preflight

- Repository path: `/workspace/capstone-project`.
- Remote: `origin https://github.com/advait-ravi-marathe/capstone-project.git`.
- Branch: `work`; HEAD `4f362eb30739d73a4e90405a30aada6401145afe`; initial worktree clean.
- The checkout is isolated from the Mac task. No Mac files were read or synchronized.
- Java runtime 21.0.12.1 and Python 3.12.14 are present. Maven 3.10.0 was downloaded from the official Apache distribution in `/tmp` and its SHA-512 matched Apache’s published checksum. The Java runtime image includes the compiler module but omits `javac` and the Java API release archive; Maven validation therefore used a temporary launcher for `jdk.compiler` with source/target 21 and a temporary Maven settings file built from the existing environment proxy (no repository or trust/certificate configuration was changed). The executor still has no system `mvn` command or normal JDK installation.
- No repository `AGENTS.md`, `.agents`, or `.codex` instructions were found.
- The input archive record resolves to the expected name and size (1,761,461 bytes); expected SHA-256 from the task is `fd756c634d38120e15e31f89d9060239972f034b54265055a8859865ce46adde`. Earlier, the bundled Library download helper failed twice before transfer with `hosted apps tools/list request failed: network` (authenticated app-list discovery/control-plane request). The fallback read route reports no readable content for this ZIP. No archive bytes or SHA-256 have been verified in this executor.
- On the continuation request, direct resolved-reference `prepare_materialize` returned signed-URL transfer metadata twice, each for the exact Library and backing file IDs above. The current `library_file_transfer.py` was fetched from the Library skill and run unchanged on each fresh complete transfer record to the consumer-local destination; both attempts returned `library file transfer failed: download failed`. The helper is present, but no local ZIP was installed and no hash was verified. No raw URL transfer fallback was used.
- The original design/source archive has not been materialized; no visual source assets were inspected.

## Implemented CoverStory setup

- `src/main/java/coverstory/modules/` now has explicit public ports and value types for Catalog, Shortlist, Application, Payments, Policy, Support, business Audit, and Analytics. There is no business workflow implementation yet. The static boundary suite checks these module packages and prevents cross-module persistence imports.
- `src/main/resources/db/schema/ownership.sql` creates the module schemas. `constraints.sql` establishes owner-scoped application journeys and revisions, integer minor-unit money with currency, hashed durable payment idempotency, append-only payment journal, distinct dispatch state, and separate business-audit and allowlisted analytics tables.
- `AesGcmFieldCipher` provides authenticated encryption with caller-supplied AAD and key ID/version metadata. The provider is only an interface; key management, durable key storage, rotation operations, retention policies, and consent workflows are not implemented.
- `web/` contains a built React + TypeScript client shell using TanStack Query, semantic Tailwind tokens, and the open-source Inter font package. Private query keys are session scoped; account changes/logout cancel and clear private queries and response generations guard stale results. The screen is a scaffold and has no real session or catalog API.
- `src/main/java/coverstory/api/StarterApiController.java` intentionally returns `401` for session resolution and `501` for the unfinished catalog endpoint. The local Spring Boot process was started and both HTTP responses were observed.
- The module contract and architecture guides include system context, module ownership, journey/payment sequence, data flow, class relationships, and interview prompts. The authoritative design archive still needs inspection and reconciliation.
- Day 001’s verified source card was supplied directly with curriculum SHA-256 `e96ae484e4c2c771e44d126f49127ee3ca005c766eb7abcb378e059a236e09de` and description SHA-256 `b9c9ea8e9a53cde75b4a67064f1ea1fa5f3ba0126e5788063007f1235a4687a0`. The mapped scaffold is documented in [CAP-40](assignments/coverstory/CAP-40.md). Its Java and Python acceptance runs fail only on learner TODO stubs; instructor fixture self-checks pass. Raw reports, fixture hashes, and status distinctions are in [CAP-40 evidence](evidence/coverstory/CAP-40/).
- This implements one primary card and its linked component only. The rest of the verified 600-card plan and exact daily file/command mappings are not present in this checkout.

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

Run the learner ranking suite separately to observe expected TODO failures:

```sh
python3 -m unittest discover -s ai/tests -p test_ranking.py
```

The frontend is pinned in `web/package-lock.json`. `spring-boot:run` also starts, and live local requests return the explicit shell responses documented above (`401` session and `501` catalog). Verified commands in this executor: `npm run build`; `npm run test:security`; `python3 scripts/check_scaffold.py`; `./scripts/test_field_cipher.sh`; `python3 -m unittest discover -s ai/tests -p test_infrastructure.py`; and Maven default tests (8 total, passing) using the temporary launcher/settings described above. Enabling `-Plearner-tests` produces expected failures in CAP-42–45 because the legacy learner TODOs are preserved. The partial starter is published on `feature/coverstory-capstone-starter`; no merge, deployment, Mac access, or Scrum edits were performed.
