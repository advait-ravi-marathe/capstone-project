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
  instance. Repeated application startup preserves issued test rows; only an
  explicit reset removes the disposable instance.
- The packaged local page uses a dedicated React entry point and typed API
  client. The packaged server exposes no `/test/**` or `__e2e/**` route.
- Security, owner propagation, request composition, and the local preview UI
  remain learner-owned TODO seams. Instructor fixtures, dependencies, migration
  setup, and deterministic harnesses do not claim those seams are implemented.

## Test boundaries

Instructor tests prove schema migration repeatability, loopback safety,
packaging/wiring prerequisites, browser harness plumbing, and fixture
integrity. Tagged learner acceptance tests assert real API, security, session,
CSRF, ownership, UI, restart, and no-write behavior. They remain expected red at
the explicit day-specific TODO until the learner completes it. A temporary
service witness may be used locally only to verify the Day 68 browser harness;
it must be removed before publication and must not count as learner evidence.

## PostgreSQL compatibility decision

See [`postgresql-options.md`](postgresql-options.md). The branch started from a
test harness pinned to embedded PostgreSQL 14.22. The test binary migration is
held until the user selects the major target. The local application uses a new,
disposable synthetic database and does not migrate any existing user database.
