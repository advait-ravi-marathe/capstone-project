# Starter coverage and verification

## Assignment contract coverage

The requested final plan contains 594 consecutive daily primary assignments and six linked component cards (600 contracts total, 44 sprints). Phase 1 is 14 October 2026 through 29 May 2027 (228 days), followed by 366 days of deeper mastery. The authoritative saved-contract archive and final operating record were not available as local bytes in this executor, so this checkout does **not** contain a verified mapping of those 600 contracts. Do not infer missing titles, dates, tests, acceptance criteria, or completion state. Materialize and integrity-check the specified package before claiming complete assignment coverage. The saved day-by-day commands, scaffolds, and exact file mapping therefore remain blocked; no generic placeholder assignments are represented as implemented.

| Area | Runnable scaffolds present | Learner work preserved | Current status |
| --- | --- | --- | --- |
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

## Commands and evidence

Run the independent Python setup suite with:

```sh
python3 -m unittest discover -s ai/tests -p test_infrastructure.py
```

Run the learner ranking suite separately to observe expected TODO failures:

```sh
python3 -m unittest discover -s ai/tests -p test_ranking.py
```

The frontend is pinned in `web/package-lock.json`. `spring-boot:run` also starts, and live local requests return the explicit shell responses documented above (`401` session and `501` catalog). Verified commands in this executor: `npm run build`; `python3 scripts/check_scaffold.py`; `./scripts/test_field_cipher.sh`; `python3 -m unittest discover -s ai/tests -p test_infrastructure.py`; and Maven default tests (7 total, passing) using the temporary launcher/settings described above. Enabling `-Plearner-tests` produces expected failures in CAP-42–45 because the legacy learner TODOs are preserved. No runtime secrets, deployments, remote pushes, or Scrum edits were performed.
