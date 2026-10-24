# CoverStory local application continuation — Days 57–70

This branch proposes a dedicated loopback-only, passwordless local runtime for
synthetic policy issuance/readback and renewal preview. See the
[architecture boundary](architecture.md),
[PostgreSQL target options](postgresql-options.md),
[chronological manifest](manifest.json), and
[Scrum export](scrum-owner-manifest.json). The Scrum export is a file only; no
board was changed.

Each day uses 120 engineering minutes: 25 study, 85 practice/testing, and 10 evidence.

## Current scaffold status

- Instructor scaffolding is complete for composition, migrations, security,
  CSRF, the thin policy adapter and packaging. Their green selectors verify
  setup only; the scheduled learner work is a trace/debug explanation with
  evidence, not a claim that the learner completed the supplied code.
- Learner implementation remains for readiness/recovery gates, owner-scoped
  reads and owner resolution, and the normal UI. Packaged browser selectors
  drive actual React controls and assert preview, issue/readback/replay, and
  restart behavior. They remain blocked by those UI/domain TODOs; an HTTP 500
  is a failing acceptance result.
- The canonical local UI route is `/`. The packaged-JAR infrastructure test
  verifies V001–V003 actually run against disposable PostgreSQL 18.6, the
  health endpoint, anonymous denial, CSRF rejection and absence of test routes.
- `scripts/local-demo.sh` uses a unique tmpfs-backed database container and
  automatically deletes it on exit. There is no persistent local-demo mode.
- PostgreSQL 18.6 was selected for the disposable embedded test harness after
  checking official support dates and Linux amd64/macOS ARM64/macOS Intel
  artifact availability. Prior test contracts are preserved as historical
  records; the shared test harness on this branch now uses the 18.6.0 binary
  BOM. No existing user database is upgraded.
- `evidence/continuation-57-70/scaffold-verification.json` records actual
  instructor commands, runtime smoke results and expected TODO failures. Day
  learner evidence stays unclaimed until learner work is completed.

Each day retains 120 engineering minutes: 25 study, 85 practice/testing, and 10 evidence. Incomplete study carries forward by reducing implementation scope; it does not add hours.

The [Sprint 5 interview-first schedule](../interview/sprint5-session-scope.md) and [machine-readable curriculum](fde57-70-v2-curriculum.json) define four bounded mock days and five-minute task proposals on the remaining days without changing original acceptance or status.

## Checks available now

```sh
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
