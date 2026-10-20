# CoverStory local application continuation — Days 57–70

This branch proposes a dedicated loopback-only, passwordless local runtime for
synthetic policy issuance/readback and renewal preview. See the
[architecture boundary](architecture.md),
[PostgreSQL target options](postgresql-options.md),
[chronological manifest](manifest.json), and
[Scrum export](scrum-owner-manifest.json). The Scrum export is a file only; no
board was changed.

## Current scaffold status

- Fixed selectors are installed for Days 57–70. Instructor/runtime scaffolding
  passes for Days 58, 59, 61, 64, 65, 67 and 70. Learner selectors for Days 57,
  60, 62, 63 and 66 remain red at explicit TODOs; Days 68–69 reach the browser
  and PostgreSQL but stop at the existing Day 49 preview TODO.
- The packaged local route, cookie-backed synthetic owner selection, security
  chain, migrations and safe disposable PostgreSQL launcher are implemented as
  instructor scaffolding. Domain/owner/preview learner logic remains TODO.
- PostgreSQL 18.6 was selected for the disposable embedded test harness after
  checking official support dates and Linux amd64/macOS ARM64/macOS Intel
  artifact availability. Prior test contracts are preserved as historical
  records; the shared test harness on this branch now uses the 18.6.0 binary
  BOM. No existing user database is upgraded.
- `evidence/continuation-57-70/scaffold-verification.json` records actual
  instructor commands, runtime smoke results and expected TODO failures. Day
  learner evidence stays unclaimed until learner work is completed.

## Checks available now

```sh
python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py
npm --prefix frontend ci --cache /tmp/capstone-npm-cache
npm --prefix frontend run build:local
npm --prefix frontend test
scripts/package-local-demo.sh
scripts/local-demo.sh
```

Java commands require a JDK 21 and Maven Central access. The cloud run used
Oracle JDK 21 and temporary proxy settings outside the repository. `npm audit
--audit-level=low` reported zero frontend vulnerabilities; a Maven dependency
advisory review is still pending. See
[`evidence/continuation-57-70/scaffold-verification.json`](../../../evidence/continuation-57-70/scaffold-verification.json)
for exact results and blockers.
