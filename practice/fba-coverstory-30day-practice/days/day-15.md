# Day 15 · 2026-10-31 · CS90-15

## Session contract

- **FBA role:** Front-End Engineer II — FBA, job **10575321**; source assignment 15. **90 minutes**: 35 implementation, 15 design/review, 10 truthful STAR evidence, 30 focused component and adversarial tests.
- **CoverStory practical:** Implement one catalog import controller-to-service contract. (saved primary CAP-52); **90 minutes**: 55 repository implementation/tests, 30 AWS official-pathway lesson/objective, 5 design/evidence.
- **Combined planned time:** 180 minutes (90 FBA + 90 CoverStory). Use checked-in synthetic fixtures only; do not enter real consumer or health data.

## FBA implementation

**URL-synced placement filters** — `URL state / React state`. Read the full task in `practice/fba-coverstory-30day-practice/fba/assignments/frontend.md` under Day 15. Edit `practice/fba-coverstory-30day-practice/fba/frontend/src/ui.jsx` and `practice/fba-coverstory-30day-practice/fba/frontend/src/assignments.js`; use `practice/fba-coverstory-30day-practice/fba/fixtures/cases.json`.

```sh
(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=15)
```

Use the authoritative FBA `frontend_15` cases in `practice/fba-coverstory-30day-practice/fba/fixtures/cases.json`. **Additional 30-minute FBA deliverable:** Use the padded lowercase market and duplicate-filter fixture. Test query-parameter normalization/deduplication and preserve an unrelated URL parameter after changing filters. Add a learner-owned interaction test in `practice/fba-coverstory-30day-practice/fba/frontend/src/ui.test.jsx`; make the assertion exercise the current day’s actual UI and learner function, not an instructor mock. Run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=15)`.

## CoverStory practical contract

Saved source: [`day-008-cap-52`](../../../docs/assignments/coverstory/saved-days/day-008-cap-52.md). Branch-local practice slot CS90-15 maps to this saved source task: **Implement one catalog import controller-to-service contract.**.

**Learner seam:** `src/main/java/coverstory/modules/catalog/api/CAP52Seam.java` (`import`). **Focused learner acceptance:** `src/test/java/coverstory/modules/catalog/CAP52AcceptanceTest.java`. **Instructor source:** `src/test/java/coverstory/modules/catalog/CAP52ControllerInfrastructureTest.java`. Additional acceptance focus: The learner acceptance calls the actual CAP52 seam; instructor controller test is separate setup evidence.

Do the 55-minute repository work against the actual existing learner seam above. If a source card's historical planned path differs from the checkout, use this verified checkout path and record the discrepancy; do not create a parallel fake implementation.

Setup/instructor check (separate from learner acceptance):

```sh
./mvnw -Dtest=CAP52ControllerInfrastructureTest test
```

Run focused learner acceptance separately (it may fail at the intentional TODO):

```sh
./mvnw -Plearner-tests -Dtest=CAP52AcceptanceTest test
```

An instructor pass proves only harness readiness. Do not claim learner completion from fixture/oracle/browser adapter checks.

## AWS pathway and evidence

Use the exact official AWS lesson/objective embedded in the CAP record in [`coverstory-final-saved-contracts-594-days.json`](../../../docs/source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) for 30 minutes. No AWS account, credential, resource, payment, or paid service is created here. If the lesson link is unavailable, record the missing link and intended objective instead of claiming course progress.

Use the final 5 minutes for actual test results, one architecture/design decision, elapsed time, and a short truthful STAR note. Label AI-assisted work. Keep completion status unchanged until you have personally implemented and reviewed the work.
