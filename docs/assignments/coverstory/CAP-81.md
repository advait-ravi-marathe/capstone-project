# CAP-81 — Check authoritative transaction and journey audits

**Saved day:** 041 · 23 November 2026 · **Primary ID:** CAP-81 · **Maximum allocation:** 150 minutes

## Purpose

Produce a read-only, owner- and version-level completeness report for business transitions, transaction audits, the current application revision, journey projection, and append-only journey/payment histories. Lossy screen analytics remain optional observations and never prove a required audit or step event.

## Files

- Learner Python seam: `ai/coverstory/cap_81.py`
- Learner acceptance cases: `ai/tests/test_coverstory_cap81_acceptance.py`
- Instructor reference checks: `ai/tests/test_coverstory_cap81_infrastructure.py`
- Synthetic input: `fixtures/coverstory/CAP-81/synthetic-v1.json`

The learner implements only `check_invariants(snapshot: AuthoritativeJourneySnapshot) -> InvariantReport`. Instructor supplied adapters provide immutable public read models and transaction-failure snapshots. This task does not add SQL transactions, database access, crypto operations, screen tracking, repair, or payload decryption.

## Report contract

Match each committed transition to an exact owner-scoped audit identity and a journey step event with the matching journey ID and version. Check the current application revision against the projection and its protected-envelope reference. Preserve prior `FAILED` and `UNKNOWN` observations when a reconciled `SUCCEEDED` result arrives. Count posted money only from the linked owning payment effect, once per effect.

Compare before/after rollback snapshots across domain, audit, projection, and step-event record families. Check owner mismatches, contiguous history, run commit/command metadata, and duplicate plaintext state. Return violation codes, safe record identifiers, and aggregate counts only. Never echo payload text, hashes, request bodies, or secrets in the report.

Expected violation codes include `MISSING_AUDIT`, `MISSING_STEP_EVENT`, `OWNER_MISMATCH`, `DUPLICATE_SENSITIVE_STATE`, `NONCONTIGUOUS_HISTORY`, and `UNVERIFIED_SNAPSHOT`. A report with incomplete or unverified evidence must never say `PASS`.

## Checks

Run the fixture and independent reference witnesses:

```sh
python3 -m unittest ai.tests.test_coverstory_cap81_infrastructure -v
```

Run the learner acceptance contract separately. It is expected to reach the explicit TODO until implemented:

```sh
python3 -m unittest ai.tests.test_coverstory_cap81_acceptance -v
```

Instructor reference results and learner outcomes must be recorded separately. A passing reference does not establish learner mastery, database integration, cryptographic behavior, or timebox fit.

## Saved acceptance mapping

| Case | Input | Expected |
|---|---|---|
| CAP-81-CS-01 | Payment confirmation, application revision, and journey version 5 have exact audit and step links; analytics is empty | `PASS` with commit and run command metadata |
| CAP-81-CS-02 | Required audit and step event are missing while a browser click is present | `MISSING_AUDIT` and `MISSING_STEP_EVENT`; analytics cannot mask either gap |
| CAP-81-CS-03 | Injected failures at each transaction boundary | Before/after snapshots match across all four record families |
| CAP-81-CS-04 | Append-only `FAILED` → `UNKNOWN` → reconciled `SUCCEEDED` observations | Preserve the history; count the linked posted effect once |
| CAP-81-CS-05 | Owner B has a colliding event ID, and A has a duplicate plaintext draft reference | `OWNER_MISMATCH`, missing A evidence, and `DUPLICATE_SENSITIVE_STATE`, without content |
| CAP-81-CS-06 | Journey history skips a sequence or commit/run command is absent | `NONCONTIGUOUS_HISTORY` or `UNVERIFIED_SNAPSHOT`; never `PASS` |

## Evidence boundary

The fixture contains synthetic IDs, status values, hashes, and integer minor-unit amounts only. It does not contain consumer draft content or invoke a database, provider, or cryptographic adapter. The six-module transaction and protected-payload integrations remain instructor-owned prerequisites outside the learner seam.
