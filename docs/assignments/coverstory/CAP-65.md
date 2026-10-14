# CAP-65 — Filter private evidence before scoring

**Learner outcome:** retrieve only sources authorized for the trusted principal, and authorize before any scoring or response construction.

**Saved schedule:** Phase 1, Day 023, 5 November 2026. The daily cap is 150 minutes; this task stays within the saved 95-minute capstone allocation (10 minutes docs, 55 minutes build, 30 minutes tests/evidence). Those are planned bounds, not measured learner completion times.

The saved contract maps this daily assignment to one bounded seam:

- Learner seam: `retrieve_for_principal(principal, query, registry, k)` in [`ai/coverstory/cap_65.py`](../../../ai/coverstory/cap_65.py).
- Synthetic registry: [`fixtures/coverstory/CAP-65/synthetic-v1.json`](../../../fixtures/coverstory/CAP-65/synthetic-v1.json).
- Acceptance suite: [`ai/coverstory/tests/test_cap_65.py`](../../../ai/coverstory/tests/test_cap_65.py).
- Instructor oracle and score-boundary witnesses: [`ai/tests/test_coverstory_cap65_infrastructure.py`](../../../ai/tests/test_coverstory_cap65_infrastructure.py).

## Required behavior

The `Principal` is trusted request context. `RetrievalQuery.owner_id` is untrusted and cannot broaden access. `scope="public"` permits public wording to Visitors. Private retrieval requires an authenticated principal with an explicit ACL grant: an Applicant must match the source owner, and Support must appear in the source's assigned support IDs. CatalogEditor has no private retrieval grant. A missing ACL denies access. An explicit private request with no grant raises `AccessDenied` before scoring.

Build an allowlisted `Candidate` subset before calling `registry.score`. Pass Support only the source's `support_excerpt`; never pass the applicant's full document text to the scorer. Applicants receive their own `applicant_text`. Keep unapproved source IDs and text out of score calls and returned hits. A UUID or query owner field is not an authorization token.

## Saved cases

- **CAP-65-CS-01:** Applicant A receives `case-A-docA`, never B's equal-text document; the scorer sees A only.
- **CAP-65-CS-02:** Support S receives the two explicitly assigned A excerpts only, with minimal text at the scoring boundary.
- **CAP-65-CS-03:** CatalogEditor and a principal facing a missing ACL are denied before scoring.
- **CAP-65-CS-04:** An owner override and unauthenticated private request fail before scoring.
- Visitor public wording is separately exercised so private denial does not hide public content.

The instructor infrastructure tests run a separate reference policy and score spy. A deliberately global-score-then-filter mutant proves the witness observes unauthorized source IDs at the scorer boundary. This witness does not implement the learner seam. `retrieve_for_principal` remains an explicit TODO until the learner implements it.

## Run locally

From the repository root:

```sh
python3 -m unittest discover -s ai/tests -p 'test_coverstory_cap65_infrastructure.py'
python3 -m unittest discover -s ai/coverstory/tests -p 'test_cap_65.py'
```

The instructor suite checks the scaffold, fixture, positive policy paths, negative policy paths, and pre-score exclusion. The acceptance suite is expected to stop at the explicit learner TODO on this starter candidate. That is an unimplemented learner outcome, not a failure of the instructor setup. Passing instructor witnesses do not establish independent learner mastery.

## Architecture reflection

Trace the supplied flow: trusted Visitor/Applicant/assigned Support principal → source ACL → authorized, role-specific candidate text → scorer → safe citation. Explain why redacting a global result after scoring still crosses a private-data boundary. Record a test command and actual outcome in the assignment evidence when the learner performs it; the source contract's expected cases are not completed evidence.
