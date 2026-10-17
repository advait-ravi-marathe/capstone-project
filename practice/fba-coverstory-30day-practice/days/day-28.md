# Day 28 · 2026-11-13 · CS90-28

## Session contract

- **FBA role:** Front-End Engineer II — FBA, job **10575321**; source assignment 28. **90 minutes**: 35 implementation, 15 design/review, 10 truthful STAR evidence, 30 focused component and adversarial tests.
- **CoverStory practical:** Prove end-to-end failure recovery and record qualified demo evidence. (saved primary CAP-166); **90 minutes**: 55 repository implementation/tests, 30 AWS official-pathway lesson/objective, 5 design/evidence.
- **Combined planned time:** 180 minutes (90 FBA + 90 CoverStory). Use checked-in synthetic fixtures only; do not enter real consumer or health data.

## FBA implementation

**Keyboard tab bar** — `ARIA tabs / focus management`. Read the full task in `practice/fba-coverstory-30day-practice/fba/assignments/frontend.md` under Day 28. Edit `practice/fba-coverstory-30day-practice/fba/frontend/src/ui.jsx` and `practice/fba-coverstory-30day-practice/fba/frontend/src/assignments.js`; use `practice/fba-coverstory-30day-practice/fba/fixtures/cases.json`.

```sh
(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=28)
```

Use the authoritative FBA `frontend_28` cases in `practice/fba-coverstory-30day-practice/fba/fixtures/cases.json`. **Additional 30-minute FBA deliverable:** Use the three-tab and invalid-active fixtures. Test Arrow/Home/End, roving tabindex, selected state, and exposure of only the active panel. Add a learner-owned interaction test in `practice/fba-coverstory-30day-practice/fba/frontend/src/ui.test.jsx`; make the assertion exercise the current day’s actual UI and learner function, not an instructor mock. Run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=28)`.

## CoverStory practical contract

Saved source: [`day-014-cap-166`](../../../docs/assignments/coverstory/saved-days/day-014-cap-166.md). Branch-local practice slot CS90-28 maps to this saved source task: **Prove end-to-end failure recovery and record qualified demo evidence.**.

**Learner seam:** `web/src/coverstory/catalog/decodeCatalogEnvelope.ts` (`decodeCatalogEnvelope`). **Focused learner acceptance:** `web/scripts/catalog-envelope-acceptance.mjs`. **Instructor-only fixture smoke:** `web/scripts/catalog-envelope-smoke.mjs`. Required E2E acceptance: the saved CAP-166 learner browser flow must persist all three versions, stop Spring, fail closed without stale comparisons, restart on the same port, refetch all versions, and complete comparison/export. This is required acceptance, not optional coverage.

Do the 55-minute repository work against the actual existing learner seam above. If a source card's historical planned path differs from the checkout, use this verified checkout path and record the discrepancy; do not create a parallel fake implementation.

Run the required learner-mode end-to-end recovery acceptance (separate from the decoder-focused acceptance below):

```sh
./mvnw -Dtest=CAP166CatalogRuntimeIntegrationTest -Dcoverstory.cap166.browser=true -Dcoverstory.cap166.decoder=learner test
```

If Chromium or its dependencies are unavailable, mark this required acceptance **BLOCKED** with the exact environment error; do not replace it with fixture smoke or mark it optional. If the current decoder TODO prevents later outage/restart assertions, record the E2E acceptance as **NOT_YET_VERIFIED** and rerun after implementing the decoder; do not claim CAP-166 complete until the full recovery assertions pass.

Setup/instructor check (separate from learner acceptance):

```sh
(cd web && npm run test:catalog-fixture)
```

Run focused learner acceptance separately (it may fail at the intentional TODO):

```sh
(cd web && npm run test:catalog-acceptance)
```

An instructor pass proves only harness readiness. Do not claim learner completion from fixture/oracle/browser adapter checks.

## AWS pathway and evidence

Use the exact official AWS lesson/objective embedded in the CAP record in [`coverstory-final-saved-contracts-594-days.json`](../../../docs/source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) for 30 minutes. No AWS account, credential, resource, payment, or paid service is created here. If the lesson link is unavailable, record the missing link and intended objective instead of claiming course progress.

Use the final 5 minutes for actual test results, one architecture/design decision, elapsed time, and a short truthful STAR note. Label AI-assisted work. Keep completion status unchanged until you have personally implemented and reviewed the work.
