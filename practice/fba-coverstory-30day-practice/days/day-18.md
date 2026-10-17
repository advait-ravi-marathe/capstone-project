# Day 18 · 2026-11-03 · CAP-619 / CS90-18

## Session contract

- **FBA role:** Front-End Engineer II — FBA, job **10575321**; source assignment 18. **60 minutes**: 35 implementation, 15 design/review, 10 truthful STAR evidence.
- **CoverStory practical:** Build safe catalog metadata tests (saved source seam CAP-53); **90 minutes**: 55 repository implementation/tests, 30 AWS official-pathway lesson/objective, 5 design/evidence.
- **Combined planned time:** 150 minutes. Use checked-in synthetic fixtures only; do not enter real consumer or health data.

## FBA implementation

**Resilient quote status** — `async API / loading-error states`. Read the full task in `practice/fba-coverstory-30day-practice/fba/assignments/frontend.md` under Day 18. Edit `practice/fba-coverstory-30day-practice/fba/frontend/src/ui.jsx` and `practice/fba-coverstory-30day-practice/fba/frontend/src/assignments.js`; use `practice/fba-coverstory-30day-practice/fba/fixtures/cases.json`.

```sh
cd practice/fba-coverstory-30day-practice/fba
make test-frontend-day DAY=18
```

Keep the supplied contract cases and add one synthetic adversarial case. Component behavior belongs in `ui.jsx`; the bounded helper belongs in `assignments.js`.

## CoverStory adversarial-test follow-up

The exact saved source contract is [`009-CAP-53`](../../../docs/assignments/coverstory/saved-days/day-009-cap-53.md), mapped to new daily card CAP-619. Follow the task, full acceptance text, and planned file paths in that source. Today’s repository work: **Add one learner-owned boundary test to `ai/coverstory/tests/test_cap_53.py` from the saved acceptance cases, then run both checks. Use an edge case that distinguishes the required behavior from a plausible wrong implementation; do not edit the instructor oracle or change the saved fixture to make a test pass.**

Run instructor setup independently:

```sh
python3 -m unittest ai.tests.test_coverstory_cap53_infrastructure
```

Then run learner acceptance separately:

```sh
python3 -m unittest ai.coverstory.tests.test_cap_53
```

The learner command is expected to expose the preserved learner TODO before implementation. Keep that result separate from the instructor setup check.

**Harness files:** instructor `ai/tests/test_coverstory_cap53_infrastructure.py`; learner `ai/coverstory/tests/test_cap_53.py`. An instructor pass is setup evidence only. Never edit the fixture oracle to force a green result or mark the saved assignment complete because a test ran.

## AWS pathway and evidence

Use the exact official AWS lesson/objective embedded in the CAP record in [`coverstory-final-saved-contracts-594-days.json`](../../../docs/source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) for 30 minutes. No AWS account, credential, resource, payment, or paid service is created here. If the lesson link is unavailable, record the missing link and intended objective instead of claiming course progress.

Use the final 5 minutes for actual test results, one architecture/design decision, elapsed time, and a short truthful STAR note. Label AI-assisted work. Keep completion status unchanged until you have personally implemented and reviewed the work.
