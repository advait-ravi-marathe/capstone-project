# Original checkout reconciliation — 15 October 2026

The original checkout at `/workspace/capstone-project` was not modified, staged, committed, or published. It contained 100 dirty paths when reviewed. A read-only comparison against the isolated published-tree snapshot found 94 byte-identical paths; no change was blindly copied from the original checkout.

The six differences were reviewed individually:

- `docs/COVERAGE.md` held stale 78-suite/285-test counts and was not copied.
- `CAP54HttpRuntimeIntegrationTest.java` added subprocess output capture/draining for diagnostics. The existing published CAP54 test passed; the diagnostic-only patch was unnecessary to the requested first-sprint behavior and remains only in the untouched original checkout.
- `CAP166CatalogRuntimeIntegrationTest.java` had a stronger Spring stop/restart flow. The isolated cloud version already exercises that same-port service restart with PostgreSQL kept alive; we extended that version with separate instructor and actual learner-decoder browser modes, storage denial, and sanitized export rather than copying the old file.
- `CAP57InstructorInfrastructureTest.java` in the original checkout referenced a missing SQL fixture there. The isolated cloud snapshot has its own registered fixture and instructor checks; the original test file was not copied.
- The original `.mvn/` wrapper configuration is already represented in the published snapshot.
- The original `docs/supply-chain/` documents are already represented in the published snapshot.

The original checkout remains untouched with its local changes intact. No Mac synchronization, Scrum edits, merge, deployment, or force push occurred.
