# PostgreSQL support transition — Days 57–70

The base branch pins Zonky embedded PostgreSQL binaries at 14.22. The
PostgreSQL project's current support table lists 14.24 as supported only
through November 12, 2026. Day 57 begins December 15, 2026, so this batch must
not continue to represent PostgreSQL 14 as a supported test target.

## Options reviewed

| Target | PostgreSQL support through | Zonky embedded binary evidence | Change size |
|---|---|---|---|
| PostgreSQL 18.6 | November 14, 2030 | Zonky publishes the 18.6.0 BOM and platform binaries for Linux amd64, macOS ARM64 and macOS Intel | One explicit major-version test-harness transition; longest runway |
| PostgreSQL 17.11 | November 8, 2029 | Zonky lists 17.11.0 in its binary BOM line | Smaller step, but one year less support |

References: [PostgreSQL versioning and support table](https://www.postgresql.org/support/versioning/), [Zonky embedded PostgreSQL version configuration](https://github.com/zonkyio/embedded-postgres#postgres-version), and [Zonky binary artifacts](https://github.com/zonkyio/embedded-postgres-binaries#postgres-version).

**Decision:** PostgreSQL 18.6, selected for this new branch's disposable
embedded test database and local demo. PostgreSQL's support table gives 18.6
the longest runway among the reviewed supported targets. Zonky artifact checks
on Maven Central returned HTTP 200 for the 18.6.0 BOM and binary artifact POMs
for Linux amd64, macOS ARM64 and macOS Intel. The project documents that major
versions are not data-directory compatible; this branch uses fresh synthetic
instances and tests migrations from an empty database. No existing user
database is upgraded.

**Harness change:** the POM imports Zonky's 18.6.0 binary BOM and uses its
platform artifacts. Prior contract pages remain historical; the shared test
harness on this branch now runs those embedded PostgreSQL tests on 18.6.
