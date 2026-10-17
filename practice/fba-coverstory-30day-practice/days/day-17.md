# Day 17 · 2026-11-02 · CS90-17

## Session contract

- **FBA role:** Front-End Engineer II — FBA, job **10575321**; source assignment 17. **90 minutes**: 35 implementation, 15 design/review, 10 truthful STAR evidence, 30 focused component and adversarial tests.
- **CoverStory practical:** Implement a synthetic wording decoder with explicit unknown-field handling. (saved primary CAP-53); **90 minutes**: 55 repository implementation/tests, 30 AWS official-pathway lesson/objective, 5 design/evidence.
- **Combined planned time:** 180 minutes (90 FBA + 90 CoverStory). Use checked-in synthetic fixtures only; do not enter real consumer or health data.

## FBA implementation

**Stale-response guard** — `async effects / AbortController`. Read the full task in `practice/fba-coverstory-30day-practice/fba/assignments/frontend.md` under Day 17. Edit `practice/fba-coverstory-30day-practice/fba/frontend/src/ui.jsx` and `practice/fba-coverstory-30day-practice/fba/frontend/src/assignments.js`; use `practice/fba-coverstory-30day-practice/fba/fixtures/cases.json`.

```sh
(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=17)
```

Use the authoritative FBA `frontend_17` cases in `practice/fba-coverstory-30day-practice/fba/fixtures/cases.json`. **Additional 30-minute FBA deliverable:** Use the seq-2-new/seq-1-old response fixture. Resolve the older request last; assert the newest response stays rendered and obsolete requests are aborted. Add a learner-owned interaction test in `practice/fba-coverstory-30day-practice/fba/frontend/src/ui.test.jsx`; make the assertion exercise the current day’s actual UI and learner function, not an instructor mock. Run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=17)`.

## CoverStory practical contract

Saved source: [`day-009-cap-53`](../../../docs/assignments/coverstory/saved-days/day-009-cap-53.md). Branch-local practice slot CS90-17 maps to this saved source task: **Implement a synthetic wording decoder with explicit unknown-field handling.**.

**Learner seam:** `ai/coverstory/cap_53.py` (`load_wording`). **Focused learner acceptance:** `ai/coverstory/tests/test_cap_53.py`. **Independent instructor source:** `ai/tests/test_coverstory_cap53_infrastructure.py`. Additional acceptance focus: Source labels remain visible and synthetic; do not silently discard unknown fields.

Do the 55-minute repository work against the actual existing learner seam above. If a source card's historical planned path differs from the checkout, use this verified checkout path and record the discrepancy; do not create a parallel fake implementation.

Setup/instructor check (separate from learner acceptance):

```sh
python3 -m unittest discover -s ai/tests -p 'test_coverstory_cap53_infrastructure.py'
```

Run focused learner acceptance separately (it may fail at the intentional TODO):

```sh
python3 -m unittest discover -s ai/coverstory/tests -p 'test_cap_53.py'
```

An instructor pass proves only harness readiness. Do not claim learner completion from fixture/oracle/browser adapter checks.

## AWS pathway and evidence

Use the exact official AWS lesson/objective embedded in the CAP record in [`coverstory-final-saved-contracts-594-days.json`](../../../docs/source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) for 30 minutes. No AWS account, credential, resource, payment, or paid service is created here. If the lesson link is unavailable, record the missing link and intended objective instead of claiming course progress.

Use the final 5 minutes for actual test results, one architecture/design decision, elapsed time, and a short truthful STAR note. Label AI-assisted work. Keep completion status unchanged until you have personally implemented and reviewed the work.
