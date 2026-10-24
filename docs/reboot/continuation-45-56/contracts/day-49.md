# Day 49 — 2026-12-11: Derive preview identity from the principal

## Contract

**Learner-owned seam:** `PolicyRenewalPreviewController.authenticatedOwner`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

Only Principal supplies trusted owner identity. Missing/blank Principal is401 with zero store calls. An owner query/header cannot override the Principal. An empty recording store ends at404 without invoking Day50/51.

### Readiness before the task

- Main Day2–3 MVC, Day31/34 identity distinction
- Supplied empty/recording store avoids future methods

### Acceptance and expected TODO state

The fixed selectors remain learner-owned. Before the named TODO is completed, the semantic test is expected to stop at that exact seam. Instructor references and transport smoke are separate evidence, not learner completion. Do not implement behavior in starter code or weaken assertions. Record the actual command, result and first blocker in `evidence/continuation-45-56/day-49.json` only after a real learner attempt.

**Acceptance commands:**
- `./mvnw -Plearner-tests -Dtest=PolicyRenewalControllerAcceptanceTest#day49OwnerComesOnlyFromPrincipalAndUnauthenticatedDoesNotRead+day49MockMvcPassesPrincipalRatherThanForgedQueryOrHeaderToTheReadStore test`

**Instructor infrastructure/reference commands:**
- `./mvnw -Dtest=RenewalPreviewManifestInfrastructureTest test`

## Interview-first system-design mock (45 minutes; within the 85-minute practice block)

**Prompt:** [Q07 — API gateway: route and protect](../../interview/question-bank.md#q07). For this interview question only, save your initial proposal during the 15-minute architecture phase before hints, reference answers, or AI. Use the [100-point rubric](../../interview/mock-rubric.md) with 5/5/15/10/10-minute phases. This is a design-transfer exercise; it adds no product implementation.

**Existing engineering work (40 minutes):** 5 minutes separate trusted Principal identity from request-supplied owner data; 25 minutes implement only `PolicyRenewalPreviewController.authenticatedOwner`; 10 minutes run the named principal/forgery selector and record its first actual result or blocker.

**Runnable acceptance:** Use the exact learner acceptance command(s) already named above. For Day 49, those remain `./mvnw -Plearner-tests -Dtest=PolicyRenewalControllerAcceptanceTest#day49OwnerComesOnlyFromPrincipalAndUnauthenticatedDoesNotRead+day49MockMvcPassesPrincipalRatherThanForgedQueryOrHeaderToTheReadStore test`. The command may stop at a prerequisite TODO; record that exact result without substituting an instructor witness.

**Deferred queue:** **S4-D49-1** — The original acceptance remains required: missing/blank Principal yields 401 before store access, and forged query/header values cannot replace Principal in the MVC boundary. If either named test remains unrun or blocked, retain it here; the mock is not HTTP/authentication proof.

## Practice and evidence

**Practice allocation (85 minutes):** 45 minutes for the named mock, then 40 minutes for the existing engineering task: 5 minutes to confirm its preconditions and predict the first result, 25 minutes on the bounded learner seam or observation, and 10 minutes for the named selector and actual blocker/result record. This replaces the prior full 85-minute implementation breakdown on this mock day only. Original acceptance remains required; carry any unfinished portion in the named queue without overtime.

**10-minute evidence allocation:** record the saved pre-assistance proposal; actual acceptance command, result or first blocker; changed paths and assistance; and which original acceptance remains open. Do not claim completion from a mock, smoke test, or partial slice.

## Study block — exactly 25 minutes

**Assistant-provided capstone architecture:** [../../assistant-architecture-notes-15-70.md#day-49](<../../assistant-architecture-notes-15-70.md#day-49>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

- Required source: **Step 21 - Understanding Spring Security Authentication**, section **Securing Spring Boot Applications with Spring Security** (4:17), access `confirmed_owned`. No viewing is inferred.
- Worked guide: [A caller cannot choose their own owner identity](../worked/day-49-principal-trust-boundary.md). The exact 25-minute segments are in `fde45-56-v2-curriculum.json`; all study items share that cap.

**Focused official sources:**
- [Spring Framework: Method Arguments](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/arguments.html) — java.security.Principal table entry; @PathVariable table entry.

Optional references are deferred and replacement-only; no purchase/enrollment or extra minutes are required.

## Safety and scope

This is a synthetic, read-only reference preview. It does not renew, accept, bind, price a new offer, collect payment or promise coverage. The browser harness uses a test-injected principal; that fixture is not production authentication. Real PostgreSQL/browser checks are restricted to disposable local test infrastructure.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
