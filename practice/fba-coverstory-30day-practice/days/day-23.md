# Day 23 · 2026-11-08 · CAP-624 / CS90-23

## Session contract

- **FBA role:** Front-End Engineer II — FBA, job **10575321**; source assignment 23. **90 minutes**: 35 implementation, 15 design/review, 10 truthful STAR evidence, 30 focused component and adversarial tests.
- **CoverStory practical:** Implement bounded public wording retrieval baseline. (saved primary CAP-56); **90 minutes**: 55 repository implementation/tests, 30 AWS official-pathway lesson/objective, 5 design/evidence.
- **Combined planned time:** 180 minutes (90 FBA + 90 CoverStory). Use checked-in synthetic fixtures only; do not enter real consumer or health data.

## FBA implementation

**Bounded quote polling** — `async polling / cancellation`. Read the full task in `practice/fba-coverstory-30day-practice/fba/assignments/frontend.md` under Day 23. Edit `practice/fba-coverstory-30day-practice/fba/frontend/src/ui.jsx` and `practice/fba-coverstory-30day-practice/fba/frontend/src/assignments.js`; use `practice/fba-coverstory-30day-practice/fba/fixtures/cases.json`.

```sh
cd practice/fba-coverstory-30day-practice/fba
make test-frontend-day DAY=23
```

Keep the supplied contract cases. **Additional 30-minute FBA deliverable:** add a focused component test in `practice/fba-coverstory-30day-practice/fba/frontend/src/ui.test.jsx` and an adversarial synthetic vector in `practice/fba-coverstory-30day-practice/fba/fixtures/cases.json` for this day's behavior: implement bounded public wording retrieval baseline. Test keyboard interaction and visible failure/empty feedback where relevant, then run `cd practice/fba-coverstory-30day-practice/fba/frontend && npm test` and `cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=23`. Component behavior belongs in `ui.jsx`; the bounded helper belongs in `assignments.js`.

## CoverStory practical contract

Saved source: [`day-012-cap-56`](../../../docs/assignments/coverstory/saved-days/day-012-cap-56.md). Daily card CAP-624 maps to this specific task: **Implement bounded public wording retrieval baseline.**.

**Learner seam:** `ai/coverstory/cap_56.py` (`evaluate_retriever`). **Focused learner acceptance:** `ai/coverstory/tests/test_cap_56.py`. **Independent instructor source:** `ai/tests/test_coverstory_cap56_infrastructure.py`. Additional acceptance focus: Retrieval is offline and synthetic; learner acceptance exercises the named function.

Do the 55-minute repository work against the actual existing learner seam above. If a source card's historical planned path differs from the checkout, use this verified checkout path and record the discrepancy; do not create a parallel fake implementation.

Setup/instructor check (separate from learner acceptance):

```sh
python3 -m unittest discover -s ai/tests -p 'test_coverstory_cap56_infrastructure.py'
```

Run focused learner acceptance separately (it may fail at the intentional TODO):

```sh
python3 -m unittest discover -s ai/coverstory/tests -p 'test_cap_56.py'
```

An instructor pass proves only harness readiness. Do not claim learner completion from fixture/oracle/browser adapter checks.

## AWS pathway and evidence

Use the exact official AWS lesson/objective embedded in the CAP record in [`coverstory-final-saved-contracts-594-days.json`](../../../docs/source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) for 30 minutes. No AWS account, credential, resource, payment, or paid service is created here. If the lesson link is unavailable, record the missing link and intended objective instead of claiming course progress.

Use the final 5 minutes for actual test results, one architecture/design decision, elapsed time, and a short truthful STAR note. Label AI-assisted work. Keep completion status unchanged until you have personally implemented and reviewed the work.
