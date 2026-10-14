# CAP-89 · Gate grounded onboarding evaluation

**Saved day:** 051 · **Date:** 3 Dec 2026 · **Timebox:** 150 minutes
**Source contract:** `docs/assignments/coverstory/saved-days/day-051-cap-89.md`

## Learner task

Implement `gate(report, baseline, policy)` in `ai/coverstory/cap_89.py` and the focused assertions in `ai/tests/test_coverstory_cap89_acceptance.py`. The report and baseline use synthetic mean reciprocal rank (mean RR), integer safety counts, and named completed cases. Require all named cases, finite numeric metrics, zero authorization violations, zero unsupported answers, and `mean_rr >= baseline.mean_rr - allowed_regression`. Safety violations fail the report regardless of its ranking score. Return `PASS`/0, `FAIL`/1, or `INVALID`/2.

The learner owns the decision logic. Keep the fixture and expected outcomes unchanged. Do not change the threshold, owner rules, source identity, public interface, or dependencies to satisfy tests. The seam does not train a model, access held-out data, retrieve evidence, call a service, or mutate onboarding.

## Synthetic fixture

`ai/fixtures/coverstory/CAP-89/synthetic-v1.json` contains the exact saved cases: RR 0.78 against 0.80 passes at the inclusive -0.02 boundary; RR 0.779 fails; one cross-owner citation fails even with RR 1.0; a missing required case and a NaN metric are invalid. The report is synthetic and contains no actual sources.

## Commands

Run the instructor infrastructure checks and learner acceptance contract from the repository root:

```sh
python3 -m unittest ai.tests.test_coverstory_cap89_infrastructure -v
python3 -m unittest ai.tests.test_coverstory_cap89_acceptance -v
```

The infrastructure suite uses a separate reference gate over fixture dictionaries and does not call the learner function. The acceptance suite currently errors at the explicit learner TODO; these errors do not indicate a broken infrastructure harness.

## Exit status and limits

The infrastructure suite launches a known failing child process and observes its nonzero status. This is a local subprocess witness only. The repository does not have a CAP-89 workflow integration in this scaffold, so no claim is made that a CI workflow preserves the status or has no `continue-on-error` masking. A workflow-level check remains deferred until such integration exists.

Evidence files under `docs/evidence/coverstory/CAP-89/` record the fixture hash, actual local command results, and limitations. This synthetic gate does not establish real model quality, independent learner mastery, production retrieval safety, or released behavior.
