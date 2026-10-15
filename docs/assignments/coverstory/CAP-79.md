# CAP-79 — Reject stale CoverStory source evidence

**Saved day:** 039 · 21 November 2026 · **Primary ID:** CAP-79 · **Maximum allocation:** 150 minutes

## Scope

Apply the fixed-clock freshness rule to source evidence shown beside a health quote or read-only help answer. The learner implements the pure Python `filter_fresh(hits, now, max_age)` seam and renders the supplied result component when that component is available. Freshness is a display recheck; it does not establish source trustworthiness, quote eligibility, or payable eligibility.

## Repository files

- Learner seam: `ai/coverstory/cap_79.py`
- Learner tests: `ai/coverstory/tests/test_cap_79.py`
- Instructor oracle and harness checks: `ai/tests/test_coverstory_cap79_infrastructure.py`
- Synthetic contract: `fixtures/coverstory/CAP-79/synthetic-v1.json`
- Saved source card: `docs/assignments/coverstory/saved-days/day-039-cap-79.md`

The acceptance interface remains Python and pure-function based. An opt-in local adapter invokes the actual `filter_fresh` seam against fixed fixture case CS-04 and maps the safe typed result into the existing freshness panel. It uses a bounded local subprocess (5-second timeout, 64 KiB output cap), contains no source text in output, and maps the preserved TODO to HTTP 501. The default profile has no provider and remains unavailable. This local binding does not make the pure-function TODO complete or establish production process supervision.

## Required behavior

1. Use the supplied fixed aware clock and 24-hour maximum age. Retain timestamps with `0 <= age <= 24 hours`, including the exact boundary.
2. Exclude older hits as `STALE`.
3. Reject future and timezone-naive timestamps as `INVALID_TIME` before subtracting timestamps. If no fresh hits remain, return `ABSTAIN` and no evidence answer.
4. Preserve the exact source ID, version, digest, evidence type, and synthetic label. Do not substitute a newer version's freshness for an older version.
5. A fresh `ADVERTISEMENT_ILLUSTRATION` can appear as labelled evidence, but `quote_order_eligible` remains false.

The fixture's example source scope is limited to synthetic CUSTOMER_A and PUBLIC entries. CAP-79 does not change source authorization policy.

## Validation

Run the independent instructor witnesses and fixture integrity checks:

```sh
python -m unittest ai.tests.test_coverstory_cap79_infrastructure
python3 -m unittest discover -s ai/tests -p test_coverstory_cap79_infrastructure.py
python3 scripts/cap79_runtime_adapter.py
```

Run the saved learner acceptance cases after implementing the TODO:

```sh
python -m unittest ai.coverstory.tests.test_cap_79
```

Before implementation, acceptance tests are expected to raise `NotImplementedError` at the explicit learner TODO. Record that outcome separately from the instructor suite. No network source, external provider, database, or payment operation is used. Runtime demo bridge requires `SPRING_PROFILES_ACTIVE=synthetic-demo` and still returns 501 until the learner seam is implemented.

## Saved case mapping

| Case | Input | Expected |
|---|---|---|
| CAP-79-CS-01 | Allowed synthetic source, 12 hours old | Retain exact source/version/digest and SYNTHETIC label |
| CAP-79-CS-02 | Exactly 24 hours old and 24 hours + 1 second | Keep boundary row; reject older row as STALE |
| CAP-79-CS-03 | Future and naive timestamps | Both INVALID_TIME; ABSTAIN with no retained evidence |
| CAP-79-CS-04 | Stale v1 and fresh v2 | Retain only exact v2 citation; do not relabel v1 |
| CAP-79-CS-05 | Fresh advertised illustration | Keep labelled evidence; quote/order eligibility stays false |

## Interpretation

The independent harness includes a strict-boundary mutant, a latest-version relabel witness, and a payable-illustration mutant. These check that the fixture distinguishes expected behavior from plausible defects. They do not demonstrate learner mastery or the saved 150-minute task envelope.
