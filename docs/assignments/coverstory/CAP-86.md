# CAP-86 · Regress unsafe onboarding source answers

**Saved day:** 047 · **Date:** 29 Nov 2026 · **Timebox:** 150 minutes
**Source contract:** `docs/assignments/coverstory/saved-days/day-047-cap-86.md`
**Saved description SHA-256:** `6f4122325a014f68ba7e421dd9a74edb8c10847dfc5714a6f0f35af12b477bc8`

## Learner task

Implement only `decide_onboarding_answer` in `ai/coverstory/cap_86.py` and the focused assertions in `ai/tests/test_coverstory_cap86_acceptance.py`. Follow the gates in order: authorize for the principal, reject timestamps older than 24 hours or invalid times, verify the exact excerpt digest, require score >= 0.7, then answer from the exact source excerpt. Rejected or missing evidence returns `ABSTAIN`, no citations, and a manual-help path. An answer cites the exact source ID, version, and digest. Neither outcome writes or changes a draft.

The fixture and its expected results are instructor-owned. Do not change owner, source identity, thresholds, clock, digests, or expected outcomes to make the tests pass. The seam receives already retrieved hits; it does not implement retrieval, networking, UI, or a database harness.

## Synthetic cases

`fixtures/coverstory/CAP-86/synthetic-v1.json` has six deterministic cases: eligible answer, high-scoring foreign-owner source, stale source (>24 hours), valid ID with tampered digest, below-threshold and empty results, and the inclusive 0.700 threshold. All source records are synthetic, use a fixed UTC clock, and expect zero writes.

## Commands

From the repository root:

```sh
python3 -m unittest ai.tests.test_coverstory_cap86_infrastructure -v
python3 -m unittest ai.tests.test_coverstory_cap86_acceptance -v
```

The instructor suite should pass independently of the learner method. The acceptance suite currently reports six errors at the explicit learner TODO; those are expected until the learner implements the policy. Do not interpret the instructor oracle pass as learner completion.

## Architecture and evidence

The independent oracle is in `ai/tests/test_coverstory_cap86_infrastructure.py`; it evaluates fixture records directly and does not call the learner seam. It checks all saved outputs and witnesses for foreign-owner, stale, digest, and strict-threshold mutants. This is a pure deterministic policy check, not evidence of a production retrieval pipeline, authenticated session, frontend binding, persistent-key behavior, or deployment security. No React binding or Spring service is claimed by this scaffold.

Evidence artifacts: `docs/evidence/coverstory/CAP-86/fixtures.json`, `harness-self-check.json`, and `result.json`. Learner completion and timebox rehearsal remain unverified.
