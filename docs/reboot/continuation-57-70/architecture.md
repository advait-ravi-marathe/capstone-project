# Days 57–70 — proposed local application boundary

This additive batch continues from commit `023117b0f966da3d4e1153029e642f0c345b5f3a`.
It adds a separately runnable, protected local CoverStory application assembly
for the existing synthetic policy issuance/readback and read-only renewal
preview. It does not add another insurance product, policy lifecycle feature,
StreamForge work, production customer data, or a board update.

## Runtime boundary

- A dedicated `assignments.local.LocalCoverStoryApplication` entry point owns
  the local runtime. Day 1's `InsuranceDayOneApplication` remains a separate,
  unchanged application.
- Local runtime configuration binds only to loopback. Startup fails closed if
  configuration attempts to bind a non-loopback address.
- The local browser selects only fixed synthetic owners. Any authenticated
  identity exists in a non-persistent server session; there are no passwords,
  password records, OAuth clients, secrets, or database-backed grants.
- The entry point explicitly composes the existing `Clock`, configured
  `DataSource`, `PolicyLifecycleService`, `PolicyRenewalPreviewPolicy`, and
  owner-scoped JDBC adapters. It never scans test packages or uses static
  mutable collaborators.
- A versioned local schema migration runs against a disposable PostgreSQL
  instance. Repeated application startup preserves synthetic test rows. The
  launcher uses a unique tmpfs-backed container and automatically removes only
  that disposable instance on exit; there is no persistent local mode.
- The packaged local page uses a dedicated React entry point and instructor
  shell. Its typed API client and policy/preview states remain learner TODOs.
  The packaged server exposes no `/test/**` or `/__e2e/**` route.
- Instructor setup is already present for explicit application composition,
  Flyway bootstrap, the loopback-only security chain and synthetic session,
  the thin policy issue adapter, CSRF protection, package/launch scripts, and
  deterministic fixtures. Their passing selectors verify scaffold behavior;
  they do not establish learner mastery. Days 58, 59, 61, 64, 65, and 67 ask
  learners to trace, explain, and verify the supplied boundary rather than
  reimplement it.
- Learner-owned implementation remains in `LocalReadinessGate.assess`,
  `LocalReadinessGate.securityCheckpoint`, owner-scoped JDBC reads,
  authenticated owner resolution, and the normal local React UI. The Day 48
  renewal-read and Day 49 preview TODOs also remain unresolved. Days 68–69
  currently stop at the Day 49 HTTP 500; that response is a blocker, never a
  successful end-to-end acceptance.

## Test boundaries

Instructor tests prove schema migration repeatability, loopback safety,
packaging/wiring prerequisites, browser harness plumbing, and fixture
integrity. Passing scaffold selectors verify instructor-provided setup only.
Learner selectors cover the remaining implementation seams and are expected
red until those are completed. Days 68–69 are browser integration checks
blocked by the existing Day 49 TODO. HTTP 500 is recorded as failure, never
acceptance success, and no temporary service witness is counted as learner
evidence.

## PostgreSQL compatibility decision

See [`postgresql-options.md`](postgresql-options.md). The branch started from a
test harness pinned to embedded PostgreSQL 14.22. PostgreSQL 18.6 is selected
for this isolated disposable harness after official support and platform
artifact checks; the actual embedded binary reports 18.6. The local app uses a
fresh synthetic database and does not migrate any existing user database.
