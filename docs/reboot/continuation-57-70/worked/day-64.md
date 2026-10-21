# Day 64 — Trace a thin issue adapter

**Study: 25 minutes.** Use Spring `ResponseEntity` and Authentication argument entries, then run the supplied parcel desk example.

## Worked example

`ParcelDeskReferenceTest` receives a precomputed dispatcher result. Created maps to 201 and the same ticket; replay maps to 200; conflict maps to 409 with a fixed code; anonymous maps to 401 without dispatch. Run `./mvnw -Dtest=ParcelDeskReferenceTest test`. Its dispatcher is a recording collaborator, not persistence or idempotency.

Native trace: owner resolution → owner/key receipt lookup → lifecycle decision → replay200, rejection safe response, or persist once for201. `issue` takes `(PolicyIssueRequest, Authentication)`. The domain service owns the issue decision and atomic persistence. Course Location semantics are not automatically part of this endpoint.

**Practice 85:** annotate 10, branch tests 35, trace 25, predict 15. **Selector:** `./mvnw -Plocal-app,learner-tests -Dtest=LocalApplicationAcceptanceTest#day64IssueAdapterDelegatesExactlyOnceAndReturnsTheServiceSnapshot test`.
