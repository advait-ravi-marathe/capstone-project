# Day 08 · 2026-10-24 · CS90-08

## Session contract

- **FBA role:** Front-End Engineer II — FBA, job **10575321**; source assignment 08. **90 minutes**: 35 implementation, 15 design/review, 10 truthful STAR evidence, 30 focused component and adversarial tests.
- **CoverStory practical:** Add citation-quality evaluation for missing and contradictory sources. (saved primary CAP-46); **90 minutes**: 55 repository implementation/tests, 30 AWS official-pathway lesson/objective, 5 design/evidence.
- **Combined planned time:** 180 minutes (90 FBA + 90 CoverStory). Use checked-in synthetic fixtures only; do not enter real consumer or health data.

## FBA implementation

**Placement timeline** — `interval merging / responsive visualization`. Read the full task in `practice/fba-coverstory-30day-practice/fba/assignments/frontend.md` under Day 08. Edit `practice/fba-coverstory-30day-practice/fba/frontend/src/ui.jsx` and `practice/fba-coverstory-30day-practice/fba/frontend/src/assignments.js`; use `practice/fba-coverstory-30day-practice/fba/fixtures/cases.json`.

```sh
(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=8)
```

Use the authoritative FBA `frontend_08` cases in `practice/fba-coverstory-30day-practice/fba/fixtures/cases.json`. **Additional 30-minute FBA deliverable:** Use the touching-window fixture. Test adjacent intervals render as one span, accessible text reports that merged span, and empty input gives an explicit empty state. Add a learner-owned interaction test in `practice/fba-coverstory-30day-practice/fba/frontend/src/ui.test.jsx`; make the assertion exercise the current day’s actual UI and learner function, not an instructor mock. Run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=8)`.

## CoverStory practical contract

Saved source: [`day-004-cap-46`](../../../docs/assignments/coverstory/saved-days/day-004-cap-46.md). Branch-local practice slot CS90-08 maps to this saved source task: **Add citation-quality evaluation for missing and contradictory sources.**.

**Learner seam:** `ai/coverstory/cap_47.py` (`evaluate`). **Focused learner acceptance:** `ai/coverstory/tests/test_cap_47.py`. **Instructor source:** `src/test/java/coverstory/modules/catalog/CAP46InstructorInfrastructureTest.java`. Linked component: CAP47 is the linked component; preserve correct abstention and reject foreign/missing citation pairs.

Do the 55-minute repository work against the actual existing learner seam above. If a source card's historical planned path differs from the checkout, use this verified checkout path and record the discrepancy; do not create a parallel fake implementation.

Setup/instructor check (separate from learner acceptance):

```sh
python3 -m unittest discover -s ai/tests -p 'test_coverstory_cap47_infrastructure.py'
```

Run focused learner acceptance separately (it may fail at the intentional TODO):

```sh
python3 -m unittest discover -s ai/coverstory/tests -p 'test_cap_47.py'
```

An instructor pass proves only harness readiness. Do not claim learner completion from fixture/oracle/browser adapter checks.

## AWS pathway and evidence

Use the exact official AWS lesson/objective embedded in the CAP record in [`coverstory-final-saved-contracts-594-days.json`](../../../docs/source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) for 30 minutes. No AWS account, credential, resource, payment, or paid service is created here. If the lesson link is unavailable, record the missing link and intended objective instead of claiming course progress.

Use the final 5 minutes for actual test results, one architecture/design decision, elapsed time, and a short truthful STAR note. Label AI-assisted work. Keep completion status unchanged until you have personally implemented and reviewed the work.
