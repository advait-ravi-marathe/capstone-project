# CAP-83 — Return safe checkout and draft errors

**Learner outcome:** map known draft/payment failures to one safe RFC ProblemDetail response contract.

**Saved schedule:** Phase 1, Day 044, 26 November 2026. The daily cap is 150 minutes; this task uses the planned 95-minute capstone allocation (10 minutes docs, 55 minutes build, 30 minutes tests/evidence). These are planned limits, not measured learner completion times.

## Exact implementation and fixture mapping

- Learner seam: [`ReturnSafeCheckoutAndDraftErrors.mapException(KnownException, RequestContext)`](../../../src/main/java/coverstory/application/ReturnSafeCheckoutAndDraftErrors.java).
- Shared HTTP adapter and typed error DTOs: [`CoverStoryExceptionHandler`](../../../src/main/java/coverstory/application/CoverStoryExceptionHandler.java), [`CoverStoryProblemDetail`](../../../src/main/java/coverstory/application/CoverStoryProblemDetail.java), [`KnownException`](../../../src/main/java/coverstory/application/KnownException.java), and [`RequestContext`](../../../src/main/java/coverstory/application/RequestContext.java).
- Synthetic matrix: [`fixtures/coverstory/CAP-83/synthetic-v1.json`](../../../fixtures/coverstory/CAP-83/synthetic-v1.json).
- Learner MockMvc acceptance tests: [`src/test/java/coverstory/application/CAP83AcceptanceTest.java`](../../../src/test/java/coverstory/application/CAP83AcceptanceTest.java).
- Independent reference mapping and working MockMvc witnesses: [`src/test/java/coverstory/application/CAP83InstructorInfrastructureTest.java`](../../../src/test/java/coverstory/application/CAP83InstructorInfrastructureTest.java); test-only exception sources are in [`CAP83MockController`](../../../src/test/java/coverstory/application/CAP83MockController.java).

The repository uses the `coverstory.application` package; this reconciles the saved draft's proposed `capstone.coverstory.application` path with the actual source tree. Test routes are synthetic exception fixtures, not product endpoints or draft/payment implementations.

## Contract

The only learner-owned behavior is mapping the supplied typed exception category and request context. The common response is Spring's RFC ProblemDetail representation with `code`, `message`, `fieldErrors`, `requestId`, and `retryable` extensions. `X-Correlation-Id` and legacy `correlationId` are accepted as request-ID aliases; output always uses `requestId`. An absent or malformed request ID is replaced by a generated value. Messages and field errors are fixed safe strings; never copy exception messages, raw answers, SQL, secrets, or owner-specific details.

| Failure | HTTP | Code |
| --- | ---: | --- |
| Malformed request | 400 | `INVALID_REQUEST` |
| Missing session | 401 | `UNAUTHENTICATED` |
| Absent or foreign resource | 404 | `NOT_FOUND` |
| Quote conflict | 409 | `QUOTE_CONFLICT` |
| Idempotency conflict | 409 | `IDEMPOTENCY_CONFLICT` |
| Stale `If-Match` | 412 | `STALE_VERSION` |
| Incomplete fields | 422 | `INCOMPLETE_FIELDS` |
| Unexpected exception | 500 | `INTERNAL_ERROR` |

The 404 response is identical for a foreign and absent resource. The generic 500 body must not contain database details. The current React shell does not implement the saved-draft compare flow, so preservation of unsaved edits after 412 remains untested; the API contract still returns the safe stale-version response.

## Instructor setup versus learner outcome

The independent instructor mapper drives MockMvc without calling the learner mapper. It checks the status map, malformed JSON without payload echo, identical foreign/absent 404, stale version, generic SQLException handling and secret exclusion from the response and captured console output, generated/legacy request IDs, safe field-linked errors, and ProblemDetail envelope fields. The test controller throws only synthetic exceptions and contains no business behavior.

The mapper method remains an explicit `CAP-83 learner TODO`. The HTTP adapter has a generic no-log 500 fallback if the mapper itself throws, so the focused learner suite currently reports expected status mismatches for the unimplemented known categories; its SQL case can pass through the generic safety fallback. The instructor suite separately verifies the full response mapping with its independent reference mapper and directly confirms the learner seam is still TODO. Neither suite certifies production readiness.

## Run locally

From the repository root:

```sh
PATH=/tmp/javac-launcher:/tmp/apache-maven-3.10.0/bin:$PATH mvn -q -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/maven-repo -Dmaven.compiler.fork=true -Dmaven.compiler.executable=/tmp/javac-launcher/javac -Dmaven.compiler.release= -Dmaven.compiler.source=21 -Dmaven.compiler.target=21 -Dtest=CAP83InstructorInfrastructureTest test
PATH=/tmp/javac-launcher:/tmp/apache-maven-3.10.0/bin:$PATH mvn -q -s /tmp/maven-settings.xml -Dmaven.repo.local=/tmp/maven-repo -Dmaven.compiler.fork=true -Dmaven.compiler.executable=/tmp/javac-launcher/javac -Dmaven.compiler.release= -Dmaven.compiler.source=21 -Dmaven.compiler.target=21 -Plearner-tests -Dtest=CAP83AcceptanceTest test
```

Record the actual command, source commit, JDK/Maven versions, fixture hash, and raw output in the assignment evidence when executed. No remote service, payment, account, or real consumer data is involved.
