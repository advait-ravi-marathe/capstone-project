# Day 30 · 2026-11-15 · CAP-631 / CS90-30

## Session contract

- **FBA role:** Front-End Engineer II — FBA, job **10575321**; source assignment 30. **90 minutes**: 35 implementation, 15 design/review, 10 truthful STAR evidence, 30 focused component and adversarial tests.
- **CoverStory practical:** First-14 integration — Regress first-14-unit rollback and explicit recovery. **90 minutes**: 55 minutes repository implementation/tests, 30 minutes AWS pathway, 5 minutes evidence/design.
- **Combined planned time:** 180 minutes (90 FBA + 90 CoverStory). Use checked-in synthetic fixtures only; no real consumer or health data.

## FBA implementation

**Practice retrospective panel** — `progress visualization / reflection`. Read the full task in `practice/fba-coverstory-30day-practice/fba/assignments/frontend.md` under Day 30. Edit `practice/fba-coverstory-30day-practice/fba/frontend/src/ui.jsx` and `practice/fba-coverstory-30day-practice/fba/frontend/src/assignments.js`; use `practice/fba-coverstory-30day-practice/fba/fixtures/cases.json`.

```sh
(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=30)
```

Use the authoritative FBA `frontend_30` cases in `practice/fba-coverstory-30day-practice/fba/fixtures/cases.json`. **Additional 30-minute FBA deliverable:** Use the attempted/passed and tied-weakest fixtures. Test all tied weakest topics are shown, counts remain distinct including zero attempts, and next-step notes do not claim mastery. Add a learner-owned interaction test in `practice/fba-coverstory-30day-practice/fba/frontend/src/ui.test.jsx`; make the assertion exercise the current day’s actual UI and learner function, not an instructor mock. Run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=30)`.



## CoverStory practical contract

**Authoritative active session:** [CAP-631](https://dwait-to-adwait.youtrack.cloud/issue/CAP-631/CS90-30-2026-11-15-CAP-58-B-Add-PII-unknown-field-rejection-and-duplicate-event-tests-90m) · First-14 integration — Regress first-14-unit rollback and explicit recovery. This replaces the former CAP-58 slice on the same daily-session identity; CAP-58 remains original source unit 15 and resumes on 16 November. Work only in the original S01 first-14 scope and its retained components.

Extend the first-14 integration witness with the exact deterministic failure and recovery sequence using existing CAP-57/PostgreSQL and the CAP-166 React comparison. Seed only synthetic A/v1, B/v1 and C/v1. Stage a new A/v2 inside the test-only CAP-57 transaction; inject a fault after the staged database writes and before commit. Confirm safe HTTP 503, no A/v2 row or active export, no audit receipt, and all original A/v1/B/v1/C/v1 records intact.

The browser keeps its selected A/v1 and B/v1. Temporarily make the disposable Catalog read unavailable and explicitly refresh; distinguish unavailable from empty, retain the selected values, show the truthful stale/unavailable state, and disable comparison. Restore only the fixture view; retry the same A/v2 command, then explicitly refresh through the existing adapters. Assert immutable A/v1 remains selected history, A/v2 becomes visible as the new published version, and retrying again returns duplicate without another audit. Implement only the regression and the smallest test-only recovery adapter correction if needed. Do not implement analytics, background writes, silent version replacement, account flows, or new product recovery infrastructure.

### Proposed-to-actual path mapping

The board path proposal did not match this checkout's module layout. These instructor/test paths were created in the existing Catalog-owned package and existing React `web` workspace; no parallel frontend or production learner seam was added.

- Proposed: `src/test/java/capstone/coverstory/integration/First14RollbackRecoveryTest.java` → actual: `src/test/java/coverstory/modules/catalog/First14RollbackRecoveryTest.java`
- Proposed: `frontend/src/coverstory/catalog/first14Recovery.test.tsx` → actual: `web/src/coverstory/catalog/first14Recovery.test.tsx`
- Proposed: `fixtures/coverstory/CS90-30/rollback-schedule.json` → actual: `fixtures/coverstory/CS90-30/rollback-schedule.json`
- Proposed: `docs/evidence/coverstory/CS90-30/result.json` → actual: `docs/evidence/coverstory/CS90-30/result.json`

### Timebox and acceptance commands

Keep the CoverStory block at 90 minutes: 5 minutes inspect the prepared fixture, 25 minutes implement the named test/adapter seam, 20 minutes run the positive and failure cases, 5 minutes retain raw results, 30 minutes continue the existing AWS pathway, and the final 5 minutes record evidence/design. Stop after the 55-minute practical block and preserve partial results; do not claim any parent card complete.

Instructor/runtime setup and learner acceptance remain distinct. The learner-owned parent TODOs are unchanged. Run from repository root:

```sh
./mvnw -Dtest=First14RollbackRecoveryTest test
COVERSTORY_CHROMIUM=/usr/bin/chromium ./mvnw -Dtest=First14RollbackRecoveryTest -Dcoverstory.cs90.browser=true -Dcoverstory.cs90.chromium=/usr/bin/chromium test
(cd web && npm run build)
```

Use only synthetic fixtures; do not send data outside the local environment. Record actual command/output, fixture digest, help received, design decision, and unresolved blockers. PASS/FAIL/BLOCKED/NOT_RUN stay distinct.

## AWS pathway and evidence



Use the exact official AWS lesson/objective embedded in the CAP record in [`coverstory-final-saved-contracts-594-days.json`](../../../docs/source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) for 30 minutes. No AWS account, credential, resource, payment, or paid service is created here. If the lesson link is unavailable, record the missing link and intended objective instead of claiming course progress.

Use the final 5 minutes for actual test results, one architecture/design decision, elapsed time, and a short truthful STAR note. Label AI-assisted work. Keep completion status unchanged until you have personally implemented and reviewed the work.
