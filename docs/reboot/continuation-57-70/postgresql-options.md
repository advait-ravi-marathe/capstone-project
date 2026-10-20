# PostgreSQL support transition — Days 57–70

The source branch pins Zonky embedded PostgreSQL binaries at 14.22. The
PostgreSQL project's current support table lists 14.24 as supported only
through November 12, 2026. Day 57 begins December 15, 2026, so this batch must
not continue to represent PostgreSQL 14 as a supported test target.

## Options reviewed

| Target | PostgreSQL support through | Zonky embedded binary evidence | Change size |
|---|---|---|---|
| PostgreSQL 18.6 | November 14, 2030 | Zonky publishes the 18.6.0 BOM and platform binaries, including Linux amd64 and optional macOS ARM64 | One explicit major-version test-harness transition; longest runway |
| PostgreSQL 17.11 | November 8, 2029 | Zonky lists 17.11.0 in its binary BOM line | Smaller step, but one year less support |

References: [PostgreSQL versioning and support table](https://www.postgresql.org/support/versioning/), [Zonky embedded PostgreSQL version configuration](https://github.com/zonkyio/embedded-postgres#postgres-version), and [Zonky binary artifacts](https://github.com/zonkyio/embedded-postgres-binaries#postgres-version).

**Recommendation:** PostgreSQL 18.6 for the new disposable embedded test
database and local demo container. The project documents that major versions
are not data-directory compatible; this branch will create fresh synthetic
instances and test the migration from empty state. No existing user database
will be upgraded.

**Decision status:** Awaiting user selection. The POM and embedded binary
versions have not been changed in this branch yet.
