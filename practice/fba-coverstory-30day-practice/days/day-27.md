# Day 27 · 2026-11-12 · CS90-27

## Session contract

- **FBA role:** Front-End Engineer II — FBA, job **10575321**; source assignment 27. **90 minutes**: 35 implementation, 15 design/review, 10 truthful STAR evidence, 30 focused component and adversarial tests.
- **CoverStory practical:** Wire existing persisted health discovery/comparison path. (saved primary CAP-166); **90 minutes**: 55 repository implementation/tests, 30 AWS official-pathway lesson/objective, 5 design/evidence.
- **Combined planned time:** 180 minutes (90 FBA + 90 CoverStory). Use checked-in synthetic fixtures only; do not enter real consumer or health data.

## FBA implementation

**Responsive image gallery** — `responsive image / accessibility`. Read the full task in `practice/fba-coverstory-30day-practice/fba/assignments/frontend.md` under Day 27. Edit `practice/fba-coverstory-30day-practice/fba/frontend/src/ui.jsx` and `practice/fba-coverstory-30day-practice/fba/frontend/src/assignments.js`; use `practice/fba-coverstory-30day-practice/fba/fixtures/cases.json`.

```sh
(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=27)
```

Use the authoritative FBA `frontend_27` cases in `practice/fba-coverstory-30day-practice/fba/fixtures/cases.json`. **Additional 30-minute FBA deliverable:** Use the empty-alt/zero-width and valid-image fixtures. Test each descriptor error, meaningful alt text, responsive sizing, and a safe fallback for missing image data. Add a learner-owned interaction test in `practice/fba-coverstory-30day-practice/fba/frontend/src/ui.test.jsx`; make the assertion exercise the current day’s actual UI and learner function, not an instructor mock. Run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=27)`.

## CoverStory practical contract

Saved source: [`day-014-cap-166`](../../../docs/assignments/coverstory/saved-days/day-014-cap-166.md). Branch-local practice slot CS90-27 maps to this saved source task: **Wire existing persisted health discovery/comparison path.**.

**Learner seam:** `web/src/coverstory/catalog/decodeCatalogEnvelope.ts` (`decodeCatalogEnvelope`). **Focused learner acceptance:** `web/scripts/catalog-envelope-acceptance.mjs`. **Instructor-only fixture smoke:** `web/scripts/catalog-envelope-smoke.mjs`. Additional acceptance focus: Focused acceptance: `(cd web && npm run test:catalog-acceptance)`; it compiles/tests the actual TODO decoder.

Do the 55-minute repository work against the actual existing learner seam above. If a source card's historical planned path differs from the checkout, use this verified checkout path and record the discrepancy; do not create a parallel fake implementation.

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
