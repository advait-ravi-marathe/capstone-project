# Day 02 — 2026-10-25: Validate selection request at HTTP boundary

**State:** Fixed validator and HTTP adapter acceptance scaffolds are installed; learner implementation remains pending. This page is a contract, not a learner result.

**Required course lesson:** [Master Spring Boot 3 & Spring Framework 6 with Java — in28Minutes Official](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/), section “Creating a Java REST API with Spring Boot, Spring Framework and Hibernate”: “Step 09 - Implementing POST Method to create User Resource” (8:36; item 35018816). Study the [Day 2 request-boundary guide](../teaching/day-02-request-boundary.md) before implementation. “Step 14 - Implementing Validations for REST API” (11:47; item 35018798) is optional later reference, not stacked into today. Catalog metadata verified 2026-10-21; playback not verified. See the [Days 1–30 map](../course-learning-map.md).

## Learner seam

request DTO / validator. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Missing or blank `journeyId`/`quoteVersionId` and malformed JSON return HTTP 400 with exactly `{"code":"INVALID_REQUEST","message":"Request validation failed"}`. A valid request invokes the service once; invalid requests produce zero writes.

## Required edge cases

Absent body/property, null, empty, whitespace, malformed JSON, and valid request call count. Keep the Day 1 DTO shape; error response has only the two documented fields.

## Prerequisite and isolation

Day 1 for integration; mocked selection service for isolation. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**Worked design question (after the guide):** Why does the controller reject invalid input before invoking the collaborator? What can a call-count test prove, and what can it not prove about persisted data? Do not create an ADR or architecture case study.

## Concept (25 minutes)

Study the required POST lesson and the Day 2 guide: request-body conversion, explicit validation before collaborator invocation, and the difference between in-process MockMvc and a listening server. The toy test uses a recording collaborator and distinct valid/malformed examples. Do not add `@Valid`, change DTOs, or infer persistence from a mock call count.

## Evidence (10 minutes)

## Instructor reference and preserved learner commands

Run the independent worked example first: `./mvnw -Dtest=Day02RequestBoundaryReferenceTest test`. Then use the existing learner selector: `./mvnw -Plearner-tests -Dtest=Day02ContractTest,Day02ApiAcceptanceTest test`. The validator tests fail at the explicit request-validator TODO until learner implementation. The API adapter contract tests use test-only validation and selection doubles to verify exact error bodies and service call counts without supplying learner logic. An application-context check also keeps malformed/missing-body behavior at HTTP 400 with the Day 3 advice installed. Infrastructure checks are separate.

## Interview-first proposal (inside practice)

Before hints, reference answers, or AI, spend up to five minutes writing a proposal or prediction for the day’s actual engineering task. Then continue the planned implementation/testing inside the existing 85-minute practice block. The 10-minute evidence block records actual command, result, assistance, and blocker; do not add a generic post-hoc design essay. Use the shared [question bank](../interview/question-bank.md) and [pattern library](../interview/pattern-library.md) only when they fit the task.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
