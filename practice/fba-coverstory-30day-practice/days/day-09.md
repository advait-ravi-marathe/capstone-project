# Day 09 · 2026-10-25 · CS90-09

## Session contract

- **FBA role:** Front-End Engineer II — FBA, job **10575321**; source assignment 09. **90 minutes**: 35 implementation, 15 design/review, 10 truthful STAR evidence, 30 focused component and adversarial tests.
- **CoverStory practical:** Implement UTC day bucketing for source evidence. (saved primary CAP-48); **90 minutes**: 55 repository implementation/tests, 30 AWS official-pathway lesson/objective, 5 design/evidence.
- **Combined planned time:** 180 minutes (90 FBA + 90 CoverStory). Use checked-in synthetic fixtures only; do not enter real consumer or health data.

## FBA implementation

**Dependency-driven wizard** — `topological order / guarded navigation`. Read the full task in `practice/fba-coverstory-30day-practice/fba/assignments/frontend.md` under Day 09. Edit `practice/fba-coverstory-30day-practice/fba/frontend/src/ui.jsx` and `practice/fba-coverstory-30day-practice/fba/frontend/src/assignments.js`; use `practice/fba-coverstory-30day-practice/fba/fixtures/cases.json`.

```sh
(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=9)
```

Use the authoritative FBA `frontend_09` cases in `practice/fba-coverstory-30day-practice/fba/fixtures/cases.json`. **Additional 30-minute FBA deliverable:** Use the prerequisite and cycle fixtures. Test an unmet prerequisite visibly blocks navigation with an explanation, cycles stay blocked, and eligible navigation works by keyboard. Add a learner-owned interaction test in `practice/fba-coverstory-30day-practice/fba/frontend/src/ui.test.jsx`; make the assertion exercise the current day’s actual UI and learner function, not an instructor mock. Run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=9)`.

## CoverStory practical contract

Saved source: [`day-005-cap-48`](../../../docs/assignments/coverstory/saved-days/day-005-cap-48.md). Branch-local practice slot CS90-09 maps to this saved source task: **Implement UTC day bucketing for source evidence.**.

**Learner seam:** `src/main/resources/coverstory/catalog/cap_48.sql` (`SQL`). **Focused learner acceptance:** `src/test/java/coverstory/modules/catalog/CAP48SqlAcceptanceTest.java`. **Instructor source:** `src/test/java/coverstory/modules/catalog/CAP48InstructorInfrastructureTest.java`. Linked component: source/version evidence failure classification in `ai/coverstory/cap_49.py`, tests `ai/coverstory/tests/test_cap_49.py`.

Do the 55-minute repository work against the actual existing learner seam above. If a source card's historical planned path differs from the checkout, use this verified checkout path and record the discrepancy; do not create a parallel fake implementation.

Setup/instructor check (separate from learner acceptance):

```sh
./mvnw -Dtest=CAP48InstructorInfrastructureTest test
```

Run focused learner acceptance separately (it may fail at the intentional TODO):

```sh
./mvnw -Plearner-tests -Dtest=CAP48SqlAcceptanceTest test
```

An instructor pass proves only harness readiness. Do not claim learner completion from fixture/oracle/browser adapter checks.

## AWS pathway and evidence

Use the exact official AWS lesson/objective embedded in the CAP record in [`coverstory-final-saved-contracts-594-days.json`](../../../docs/source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) for 30 minutes. No AWS account, credential, resource, payment, or paid service is created here. If the lesson link is unavailable, record the missing link and intended objective instead of claiming course progress.

Use the final 5 minutes for actual test results, one architecture/design decision, elapsed time, and a short truthful STAR note. Label AI-assisted work. Keep completion status unchanged until you have personally implemented and reviewed the work.
