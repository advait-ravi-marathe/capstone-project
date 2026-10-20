# CoverStory local application continuation — Days 57–70

This branch proposes a dedicated loopback-only, passwordless local runtime for
synthetic policy issuance/readback and renewal preview. See the
[architecture boundary](architecture.md),
[PostgreSQL target options](postgresql-options.md),
[chronological manifest](manifest.json), and
[Scrum export](scrum-owner-manifest.json). The Scrum export is a file only; no
board was changed.

## Current scaffold status

- Days 57 and 70 have fixed learner/instructor Java tests; Day 58 has the
  composition seam but its acceptance test is not installed.
- Days 59–65 and 67–69 still need their fixed test sources and some production
  seams. Their commands in the contracts are planned selectors, not runnable
  evidence; the manifest marks them `not_installed`.
- Day 66 has a dedicated React entry shell; its behavioral acceptance test and
  typed API client are not installed.
- PostgreSQL target selection is pending. No existing embedded PostgreSQL
  version was changed and no migration was applied.
- The day evidence files remain blank until a learner executes the work.

## Checks available now

```sh
python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py
npm --prefix frontend ci --cache /tmp/capstone-npm-cache
npm --prefix frontend run build:local
npm --prefix frontend test
```

Java commands require a JDK 21 and Maven Central access. The current execution
environment has Java 21 without `javac`; resolving the Spring Boot BOM also
failed because Maven Central DNS was unavailable. See
[`evidence/continuation-57-70/scaffold-verification.json`](../../../evidence/continuation-57-70/scaffold-verification.json)
for exact results and blockers.
