# Day 02 — 2026-10-21: Validate selection request at HTTP boundary

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

Dated attempt note and valid/invalid MockMvc cases. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Instructor reference and preserved learner commands

Run the independent worked example first: `./mvnw -Dtest=Day02RequestBoundaryReferenceTest test`. Then use the existing learner selector: `./mvnw -Plearner-tests -Dtest=Day02ContractTest,Day02ApiAcceptanceTest test`. The validator tests fail at the explicit request-validator TODO until learner implementation. The API adapter contract tests use test-only validation and selection doubles to verify exact error bodies and service call counts without supplying learner logic. An application-context check also keeps malformed/missing-body behavior at HTTP 400 with the Day 3 advice installed. Infrastructure checks are separate.

## Interview transfer (inside the current evidence block)

**Question:** Where does request-body conversion end and explicit validation begin? Why does malformed JSON fail before the service call?

**Observable evidence:** Trace DTO → validation → supplied collaborator; cite the reference call count. State that this does not prove a database write or integrated completion.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A4](../interview-evidence-overlay.md#a4) (anecdotal); [A5](../interview-evidence-overlay.md#a5) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided first encounter:** [LeetCode 1 Two Sum](https://leetcode.com/problems/two-sum/). Use the Premium editorial/solution only after stating the problem in your own words and making a first attempt; the resulting work remains guided/assisted and earns no independent-transfer credit. Preserve the original canonical anchor identities and record any actual attempt only when it occurs.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 2` first. This command uses the actual Asia/Kolkata local study date; contract dates are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../dsa/guided-transfer-policy.md](../dsa/guided-transfer-policy.md) and [the DSA error log](../dsa-error-log-template.md).
