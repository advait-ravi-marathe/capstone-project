# Day 29 — Understand the disposable reset guard

The required Profiles lesson (7:05) is configuration context; it does not grant authority to destroy data. The ConfigurationProperties and Embedded Servers videos are optional later references, not required today. Course access is owned; exact titles are catalog metadata, not a claim that videos were watched. Fit the required lesson, this walkthrough, and readiness inside 25 minutes.

## Guard decision examples

The external-URI script requires the explicit `--confirm-disposable` flag, parses a PostgreSQL URI with a plain database name, requires that name to end in `_test`, and checks for `psql` only after those guards. A `_test` substring in a password does not count as a test database name. A test profile or a suffix alone does not prove an arbitrary remote target is disposable. Never print credentials.

| Target and confirmation | Result |
|---|---|
| `library_test`, confirmation absent | Refuse before connecting |
| `library_prod`, confirmation present | Refuse before connecting |
| malformed database name, confirmation present | Refuse before connecting |
| trusted, owned disposable `*_test` target with confirmation | Eligible for the guarded reset |

## Existing safe exercise

The documented command uses the integration test's own newly started embedded PostgreSQL instance; it does not reset an external database:

```sh
./mvnw -Dtest=Day29ResetIntegrationTest test
# or the wrapper:
./scripts/reset-synthetic-homesecure.sh --embedded-test --confirm-disposable
```

The test initializes the schema, runs the reset twice, then reads through a new connection. It asserts exactly one journey and one quote with the fixed synthetic identifiers and `125.00` premium. That verifies repeatability for this disposable fixture. Do not run the external URI mode as part of this coursework or substitute a personal/shared target. No external database was reset for this teaching update.

Study remains 25 minutes; practice/testing remains 85 and evidence 10.
