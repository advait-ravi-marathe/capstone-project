# CoverStory local application continuation — Days 57–70

This is the existing Sprint 5 course revision for December 15–28, 2026. It preserves the existing day sequence, learner statuses, DSA records, and all TODOs. The implementation separates the early Sprint 5 runtime from the later default application graph so Day 57–70 work does not require Day 79+ components.

## Scope and evidence

- [Architecture boundary](architecture.md) explains the dedicated stage entry, fresh target-3 database, separate classifier archive, security boundary and later-runtime preservation.
- [Daily contracts](contracts/) contain runnable selectors and the fixed 25-study / 85-practice / 10-evidence allocation for each day.
- [Worked references](worked/) provide the independent example, native transfer and command before each assignment.
- [Canonical manifest](manifest.json) and [Scrum export](scrum-owner-manifest.json) are synchronized curriculum files. The export does not write to a board.
- Days 57, 60, 62, 63 and 66 remain learner-owned. Stage references and instructor checks are not learner completion. Browser acceptance is expected to fail at unresolved learner TODOs and never accepts an HTTP 500.

## Sprint 5 packaging and checks

Build a fresh frontend and classifier JAR before stage browser checks:

```sh
scripts/package-sprint5-demo.sh
./mvnw -Plocal-app,sprint5-local,learner-tests -Dtest=Sprint5PackagedInfrastructureTest test
```

The package receipt binds the source commit, working-tree state and archive SHA-256. Stage browser tests launch the `-sprint5.jar` artifact with a live disposable embedded database. `scripts/sprint5-demo.sh` is an optional loopback-only Docker Compose demonstration that creates and removes only its unique project; no persistent database mode exists.

Useful focused references include `npm --prefix frontend run test:sprint5`, `./mvnw -Plocal-app,sprint5-local,learner-tests -Dtest=LocalApplicationAcceptanceTest test`, `./mvnw -Plocal-app,sprint5-local,learner-tests -Dtest=LocalSecurityAcceptanceTest test`, and the exact per-day commands in each contract. The full `test:local` and default application runtime keep their later coverage separately.
