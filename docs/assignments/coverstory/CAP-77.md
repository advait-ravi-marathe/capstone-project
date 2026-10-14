# CAP-77 — Check extractive answer and source consistency

**Learner outcome:** reject any answer claim that does not exactly match its cited, authorized, applicable source slice.

**Saved schedule:** Phase 1, Day 037, 19 November 2026. The daily cap is 150 minutes; this task uses the planned 95-minute capstone allocation (10 minutes docs, 55 minutes build, 30 minutes tests/evidence). Those figures are planned bounds, not measured learner completion times.

## Exact source mapping

- Learner seam: `check_answer(StructuredAnswer, authorizedSources)` in [`ai/coverstory/cap_77.py`](../../../ai/coverstory/cap_77.py).
- Synthetic source and acceptance vectors: [`fixtures/coverstory/CAP-77/synthetic-v1.json`](../../../fixtures/coverstory/CAP-77/synthetic-v1.json).
- Learner acceptance suite: [`ai/coverstory/tests/test_cap_77.py`](../../../ai/coverstory/tests/test_cap_77.py).
- Independent instructor oracle and witnesses: [`ai/tests/test_coverstory_cap77_infrastructure.py`](../../../ai/tests/test_coverstory_cap77_infrastructure.py).

## Required behavior

For each claim, locate the exact source ID and immutable version in the authorized source map. Check authorization and applicability before comparing the claim. Validate the cited content hash and bounds, then require the citation excerpt and claim text to equal the exact source slice. A missing or unauthorized source, invalid citation, or mismatched claim makes the complete answer `UNSUPPORTED`. Count valid claims individually in a mixed answer. An empty claim list is `ABSTAIN_REQUIRED`, never vacuously supported.

The saved CAP-59 citation contract is the provenance boundary. This assignment does not implement or replace CAP-59's citation verification seam. Its status `SUPPORTED` means only that the claim matches an authorized extract under the supplied registry. It does not assert authoritative payment or proposal state, give payment advice, change a quote, accept terms, or submit a proposal. Only authoritative domain workflows can establish those states.

## Saved cases

- **CAP-77-CS-01:** `Payment is pending.` exactly matches the authorized source and citation: `SUPPORTED`, one valid claim, `authoritative_state=False`.
- **CAP-77-CS-02:** `Payment is confirmed.` cites the pending extract: `CLAIM_TEXT_MISMATCH`, `UNSUPPORTED`.
- **CAP-77-CS-03:** Exact wording from the B-only source is absent from the authorized registry: `UNKNOWN_SOURCE`, `UNSUPPORTED`.
- **CAP-77-CS-04:** No claims: `ABSTAIN_REQUIRED`.
- **CAP-77-CS-05:** One exact claim plus one mismatched claim: `UNSUPPORTED`, with one valid claim counted.

All fixture strings and source identifiers are synthetic. The instructor suite verifies fixture hashes and uses an independent exact-slice oracle. It also checks that a bad source hash cannot produce `SUPPORTED`. The learner function remains an explicit TODO; the acceptance suite should stop there on the starter candidate. Instructor setup passing does not establish learner completion or independent mastery.

## Run locally

From the repository root:

```sh
python3 -m unittest discover -s ai/tests -p 'test_coverstory_cap77_infrastructure.py'
python3 -m unittest discover -s ai/coverstory/tests -p 'test_cap_77.py'
```

Record the actual command output, candidate commit, Python version, and fixture hash in the assignment evidence when performed. Expected cases are not completed evidence.

## Architecture reflection

Trace claim → citation → authorized immutable source slice → per-claim check → overall answer status. Explain what exact extractive agreement proves and why it cannot establish current payment state or trigger a payment/proposal transition. Keep the explanation within the existing reflection allocation; do not add another implementation task.
