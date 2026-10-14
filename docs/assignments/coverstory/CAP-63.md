# CAP-63 — Review first CoverStory slice evidence

**Saved date:** 2 November 2026 · **Status:** NOT_RUN / NOT_READY_FOR_LEARNER

Produce an executable evidence index for Java, SQL, source integrity, and the Day014 customer slice. This starter provides a typed report seam, synthetic inputs, an instructor reference oracle, and focused acceptance tests. The learner implements `validate_evidence`; no passing learner solution is included.

## Exact implementation and evidence paths

- Learner seam: `ai/coverstory/cap_63.py` — `validate_evidence(records, required_ids) -> EvidenceReport`
- Synthetic cases: `fixtures/coverstory/CAP-63/synthetic-v1.json`
- Learner acceptance: `ai/tests/test_coverstory_cap63_acceptance.py`
- Instructor-only reference witnesses: `ai/tests/test_coverstory_cap63_infrastructure.py`
- Planned result receipt: `docs/evidence/coverstory/CAP-63/result.json`
- Planned instructor self-check: `docs/evidence/coverstory/CAP-63/harness-self-check.json`

The evidence index should keep each record intact, list missing required IDs, identify invalid receipts, and return `HOLD` until every required result has valid provenance and is neither blocked nor unrun. A claimed `PASS` requires its exact command, commit, environment, matching fixture ID, exit code zero, and a raw artifact path. An absent artifact or nonzero/missing exit code makes that claim invalid. Historical generic evidence remains in the report but cannot establish CoverStory status.

## Focused cases

1. CAP-57 has a complete synthetic PASS receipt and CAP-166 is BLOCKED: report both outcomes and keep the gate at HOLD.
2. CAP-60 is absent: list it as missing and keep HOLD.
3. A claimed PASS with exit code 1 or no artifact is invalid evidence.
4. A legacy event-domain PASS remains retained as historical evidence and is rejected as CoverStory proof.

The instructor oracle is test-only and does not call or implement the learner seam. Its positive witness checks that an evidenced PASS plus a blocked dependency holds. Negative witnesses mutate exit code, artifact, domain, or fixture identity and verify rejection; a missing-record witness checks required-ID ordering. Acceptance tests are expected to stop at the learner TODO until implemented.

## Commands and result interpretation

From the repository root:

```bash
python -m unittest -v ai.tests.test_coverstory_cap63_infrastructure
python -m unittest -v ai.tests.test_coverstory_cap63_acceptance
```

The infrastructure command must pass independently of learner code. The acceptance command currently raises four `NotImplementedError` exceptions at the explicit CAP-63 learner TODO. That is an expected learner seam result, not evidence that the instructor setup passed or that the assignment is complete. Record actual commit, interpreter version, fixture hash, commands, raw output, and result before assigning. The inherited 95-minute practical budget still requires a timed rehearsal; no task-time claim is made here.

No actual Java, SQL, customer slice, deployment, or historical-receipt verification is claimed by these synthetic tests. Do not convert a test expectation into a PASS receipt. DSA attempted and solved outcomes remain separate evidence.

## Retained architecture boundaries

Only current, matching CoverStory evidence can satisfy required IDs. The old event/runbook/schema harness is historical generic evidence; it is not renamed or promoted. Do not change ticket status or imply readiness from a test count. No customer data, external service, real payment, or cloud resource is used.
