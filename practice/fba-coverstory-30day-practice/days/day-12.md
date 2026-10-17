# Day 12 · 2026-10-28 · CAP-613 / CS90-12

## Session contract

- **FBA role:** Front-End Engineer II — FBA, job **10575321**; source assignment 12. **90 minutes**: 35 implementation, 15 design/review, 10 truthful STAR evidence, 30 focused component and adversarial tests.
- **CoverStory practical:** Wire one supplied Spring seam and assert request/response failure mapping. (saved primary CAP-50); **90 minutes**: 55 repository implementation/tests, 30 AWS official-pathway lesson/objective, 5 design/evidence.
- **Combined planned time:** 180 minutes (90 FBA + 90 CoverStory). Use checked-in synthetic fixtures only; do not enter real consumer or health data.

## FBA implementation

**Async placement cost summary** — `derived server state`. Read the full task in `practice/fba-coverstory-30day-practice/fba/assignments/frontend.md` under Day 12. Edit `practice/fba-coverstory-30day-practice/fba/frontend/src/ui.jsx` and `practice/fba-coverstory-30day-practice/fba/frontend/src/assignments.js`; use `practice/fba-coverstory-30day-practice/fba/fixtures/cases.json`.

```sh
cd practice/fba-coverstory-30day-practice/fba
make test-frontend-day DAY=12
```

Keep the supplied contract cases. **Additional 30-minute FBA deliverable:** add a focused component test in `practice/fba-coverstory-30day-practice/fba/frontend/src/ui.test.jsx` and an adversarial synthetic vector in `practice/fba-coverstory-30day-practice/fba/fixtures/cases.json` for this day's behavior: wire one supplied spring seam and assert request/response failure mapping. Test keyboard interaction and visible failure/empty feedback where relevant, then run `cd practice/fba-coverstory-30day-practice/fba/frontend && npm test` and `cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=12`. Component behavior belongs in `ui.jsx`; the bounded helper belongs in `assignments.js`.

## CoverStory practical contract

Saved source: [`day-006-cap-50`](../../../docs/assignments/coverstory/saved-days/day-006-cap-50.md). Daily card CAP-613 maps to this specific task: **Wire one supplied Spring seam and assert request/response failure mapping.**.

**Learner seam:** `src/main/java/coverstory/modules/catalog/api/CAP50Seam.java` (`acceptedCounts`). **Focused learner acceptance:** `src/test/java/coverstory/modules/catalog/CAP50AcceptanceTest.java`. **Existing HTTP boundary:** `src/main/java/coverstory/modules/catalog/api/CatalogController.java`, exercised by `src/test/java/coverstory/modules/catalog/CatalogControllerModuleIntegrationTest.java`. There is no CAP50-specific route; use the existing Catalog route for the malformed-request/response assertion and keep the CAP50 acceptance tied to its actual seam. **Instructor source:** `src/test/java/coverstory/modules/catalog/CAP50InstructorInfrastructureTest.java`. Linked component: preserve nullable amounts and successful-attempt counts without fanout.

Do the 55-minute repository work against the actual existing learner seam above. If a source card's historical planned path differs from the checkout, use this verified checkout path and record the discrepancy; do not create a parallel fake implementation.

Run existing HTTP request/response boundary checks separately (bad category and oversized query must return 400):

```sh
./mvnw -Dtest=CatalogControllerModuleIntegrationTest test
```

Setup/instructor check (separate from learner acceptance):

```sh
./mvnw -Dtest=CAP50InstructorInfrastructureTest test
```

Run focused learner acceptance separately (it may fail at the intentional TODO):

```sh
./mvnw -Plearner-tests -Dtest=CAP50AcceptanceTest test
```

An instructor pass proves only harness readiness. Do not claim learner completion from fixture/oracle/browser adapter checks.

## AWS pathway and evidence

Use the exact official AWS lesson/objective embedded in the CAP record in [`coverstory-final-saved-contracts-594-days.json`](../../../docs/source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) for 30 minutes. No AWS account, credential, resource, payment, or paid service is created here. If the lesson link is unavailable, record the missing link and intended objective instead of claiming course progress.

Use the final 5 minutes for actual test results, one architecture/design decision, elapsed time, and a short truthful STAR note. Label AI-assisted work. Keep completion status unchanged until you have personally implemented and reviewed the work.
