# CAP-52 — Catalog import request seam

**Saved contract:** `POST /api/v1/catalog/imports` accepts a source ID, source version, and canonical hash. The server derives the editor from a trusted request attribute. The learner-owned seam decides accept, duplicate, conflict, or invalid; it remains a TODO.

## What the instructor scaffold covers

- `CAP52Controller` enforces the exact JSON field set, nonblank identifiers, 64-character hexadecimal hash, and role boundary before invoking the decision port.
- MockMvc infrastructure checks use a fake decision port and verify 401/403/400 paths make zero seam calls; the valid editor route demonstrates one request mapping and 201 transport mapping.
- `CAP52AcceptanceTest` exercises same-content retry and changed-hash conflict against the real learner seam. It is expected to stop at `TODO(CAP-52)` until the learner implements it.

This is not an authentication filter: a trusted upstream adapter must set `trustedEditor`, and no such adapter is wired here. The fake service makes no persistence claim. Durable idempotency and conflict handling require the learner seam and later database work.

## Paths and commands

- Learner seam: `src/main/java/coverstory/modules/catalog/api/CAP52Seam.java`
- Transport scaffold: `src/main/java/coverstory/modules/catalog/api/CAP52Controller.java`
- Synthetic fixture: `fixtures/coverstory/CAP-52/synthetic-v1.json`
- Instructor checks: `mvn -Dtest=CAP52ControllerInfrastructureTest test`
- Learner behavior: `mvn -Plearner-tests -Dtest=CAP52AcceptanceTest test`
