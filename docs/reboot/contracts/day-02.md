# Day 02 — 2026-10-21: Validate selection request at HTTP boundary

**State:** Fixed validator and HTTP adapter acceptance scaffolds are installed; learner implementation remains pending. This page is a contract, not a learner result.

## Learner seam

request DTO / validator. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Missing or blank `journeyId`/`quoteVersionId` and malformed JSON return HTTP 400 with exactly `{"code":"INVALID_REQUEST","message":"Request validation failed"}`. A valid request invokes the service once; invalid requests produce zero writes.

## Required edge cases

Absent body/property, null, empty, whitespace, malformed JSON, and valid request call count. Keep the Day 1 DTO shape; error response has only the two documented fields.

## Prerequisite and isolation

Day 1 for integration; mocked selection service for isolation. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Draw the request validation boundary and mark the point before any service call. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

HTTP request contracts and boundary vs domain validation [B].

## Evidence (10 minutes)

Dated attempt note and valid/invalid MockMvc cases. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day02ContractTest,Day02ApiAcceptanceTest test`

The validator tests fail at the explicit request-validator TODO until learner implementation. The API adapter contract tests use test-only validation and selection doubles to verify exact error bodies and service call counts without supplying learner logic. An application-context check also keeps malformed/missing-body behavior at HTTP 400 with the Day 3 advice installed. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**Consolidation day — no new mandatory problem.** Continue or cold-review LC 121 as needed. Use 25 minutes to refresh due patterns, 20 minutes to cold-solve the weakest due anchor, and 15 minutes for same-day/other due active recall and the error log. Planned review items: No review is scheduled from the planned anchor dates today. Recompute from actual first-attempt dates and record completion only after the review. If no review is actually due, cold-recall the weakest earlier anchor; failed recall displaces the next new anchor. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** Why validate at the boundary if the service also has invariants? What happens before any write on malformed input?

**Observable evidence:** Trace DTO → validation → service; explain dependency injection and controller responsibility; show invalid input never reaches a write.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A4](../interview-evidence-overlay.md#a4) (anecdotal); [A5](../interview-evidence-overlay.md#a5) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.
