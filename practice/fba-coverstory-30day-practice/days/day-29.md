# Day 29 · 2026-11-14 · CAP-630 / CS90-29

## Session contract

- **FBA role:** Front-End Engineer II — FBA, job **10575321**; source assignment 29. **90 minutes**: 35 implementation, 15 design/review, 10 truthful STAR evidence, 30 focused component and adversarial tests.
- **CoverStory practical:** First-14 integration — Verify first-14-unit catalog-to-comparison consistency. **90 minutes**: 55 minutes repository implementation/tests, 30 minutes AWS pathway, 5 minutes evidence/design.
- **Combined planned time:** 180 minutes (90 FBA + 90 CoverStory). Use checked-in synthetic fixtures only; no real consumer or health data.

## FBA implementation

**Timed mock: quote comparison feature** — `role mock / async integration`. Read the full task in `practice/fba-coverstory-30day-practice/fba/assignments/frontend.md` under Day 29. Edit `practice/fba-coverstory-30day-practice/fba/frontend/src/ui.jsx` and `practice/fba-coverstory-30day-practice/fba/frontend/src/assignments.js`; use `practice/fba-coverstory-30day-practice/fba/fixtures/cases.json`.

```sh
(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=29)
```

Use the authoritative FBA `frontend_29` cases in `practice/fba-coverstory-30day-practice/fba/fixtures/cases.json`. **Additional 30-minute FBA deliverable:** Use the visible/hidden fee options fixture. Test a hidden cheaper option never wins, IDs/minimum fee match the fixture, and partial refresh failure preserves safe prior results. Add a learner-owned interaction test in `practice/fba-coverstory-30day-practice/fba/frontend/src/ui.test.jsx`; make the assertion exercise the current day’s actual UI and learner function, not an instructor mock. Run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=29)`.



## CoverStory practical contract

**Authoritative active session:** [CAP-630](https://dwait-to-adwait.youtrack.cloud/issue/CAP-630/CS90-29-2026-11-14-CAP-58-A-Implement-allowlisted-page-load-click-event-validation-90m) · First-14 integration — Verify first-14-unit catalog-to-comparison consistency. This replaces the former CAP-58 slice on the same daily-session identity; CAP-58 remains original source unit 15 and resumes on 16 November. Work only in the original S01 first-14 scope and its retained components.

Implement a cross-boundary identity check through the existing CAP-57 publication, public Catalog API, and CAP-166 comparison UI. Work only within the original first 14 units (CAP-40, 42, 44, 46, 48, 50, 165, 52, 53, 54, 55, 56, 57, 166) and their six retained components. CAP-58 is source unit 15 and receives no credit from this session. Use synthetic A/B/C health versions and public scenario only.

Read SQL publication IDs/hashes, fetch the actual Spring catalog API, and inspect the real React comparison through an injected instructor-only decoder adapter. Assert that SQL identity, API envelope identity, and visible comparison source/version identity agree. Do not implement or alter the learner decoder.

Add the two exact saved cases: (1) publish A/v1 twice; the second response is the adapter’s duplicate result, the immutable source record is unchanged, the public response remains identical, and no second publication audit row appears; (2) use an intentionally stale A/v2 export-reference fixture while current SQL/API/UI show A/v1, and ensure the oracle rejects it for identity/provenance mismatch. Keep the negative oracle check in-memory, then re-run the correct A/v1 snapshot to show it remains valid. Preserve the correct fixture snapshot and add no production feature.

### Proposed-to-actual path mapping

The board path proposal did not match this checkout's module layout. These instructor/test paths were created in the existing Catalog-owned package and existing React `web` workspace; no parallel frontend or production learner seam was added.

- Proposed: `src/test/java/capstone/coverstory/integration/First14CatalogConsistencyTest.java` → actual: `src/test/java/coverstory/modules/catalog/First14CatalogConsistencyTest.java`
- Proposed: `frontend/src/coverstory/catalog/first14Consistency.test.tsx` → actual: `web/src/coverstory/catalog/first14Consistency.test.tsx`
- Proposed: `fixtures/coverstory/CS90-29/catalog-consistency.json` → actual: `fixtures/coverstory/CS90-29/catalog-consistency.json`
- Proposed: `docs/evidence/coverstory/CAP-630/result.json` → actual: `docs/evidence/coverstory/CAP-630/result.json`

### Timebox and acceptance commands

Keep the CoverStory block at 90 minutes: 5 minutes inspect the prepared fixture, 25 minutes implement the named test/adapter seam, 20 minutes run the positive and failure cases, 5 minutes retain raw results, 30 minutes continue the existing AWS pathway, and the final 5 minutes record evidence/design. Stop after the 55-minute practical block and preserve partial results; do not claim any parent card complete.

Instructor/runtime setup and learner acceptance remain distinct. The learner-owned parent TODOs are unchanged. Run from repository root:

```sh
./mvnw -Dtest=First14CatalogConsistencyTest test
COVERSTORY_CHROMIUM=/usr/bin/chromium ./mvnw -Dtest=First14CatalogConsistencyTest -Dcoverstory.cs90.browser=true -Dcoverstory.cs90.chromium=/usr/bin/chromium test
(cd web && npm run build)
```

Use only synthetic fixtures; do not send data outside the local environment. Record actual command/output, fixture digest, help received, design decision, and unresolved blockers. PASS/FAIL/BLOCKED/NOT_RUN stay distinct.

## AWS pathway and evidence



Use the exact official AWS lesson/objective embedded in the CAP record in [`coverstory-final-saved-contracts-594-days.json`](../../../docs/source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) for 30 minutes. No AWS account, credential, resource, payment, or paid service is created here. If the lesson link is unavailable, record the missing link and intended objective instead of claiming course progress.

Use the final 5 minutes for actual test results, one architecture/design decision, elapsed time, and a short truthful STAR note. Label AI-assisted work. Keep completion status unchanged until you have personally implemented and reviewed the work.
