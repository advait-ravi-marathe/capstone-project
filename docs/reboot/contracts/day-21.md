# Day 21 — 2026-11-13: Protect cookie-authenticated writes

**State:** A test-only form-login/CSRF HTTP harness and `Day21ContractTest` are present; they do not configure production authentication. The instructor harness verifies server-issued cookie/header behavior. Learner acceptance calls the real `JourneyAccessPolicy` and remains blocked by its existing Day 12 TODO.

**Course lesson:** Required owned-course 4993276, “Step 08 - Exploring Spring Security - Cross Site Request Forgery - CSRF” (11:08). The REST-CSRF (8:40) and CORS (2:34) videos are optional later references, not stacked into today. Use the [Day 21 guide](../teaching/day-21-csrf-boundaries.md#token-session-identity-and-ownership-trace) for the remaining study allocation. See the [Days 1–30 map](../course-learning-map.md); exact catalog title metadata is not a claim of playback.

## Learner seam

Test-only form-login/session and server-issued CSRF cookie/header flow, followed by the existing `JourneyAccessPolicy.assertCanAccess` owner-policy prerequisite. No production security configuration or API-client change is assigned; keep the owner rule in its existing learner-owned seam.

## Acceptance invariants

Keep Spring Security CSRF enabled for cookie-authenticated writes. Missing/invalid token returns 403; a valid session plus token passes the CSRF check, after which authentication, authorization, and business validation still apply. Use supported session/test principal and default `XSRF-TOKEN` cookie / `X-XSRF-TOKEN` header; do not build a credential system.

## Required edge cases

Anonymous/session, missing/invalid/valid CSRF token.

## Prerequisite and isolation

Days 12 and 18–20; session/CSRF test helpers. The owner acceptance invokes the existing `JourneyAccessPolicy`; it does not replace that learner-owned rule with a canned result. Keep the security harness test-only. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**Worked prompt:** Which boundary checks the CSRF token, establishes identity, and authorizes this object? Distinguish the passing instructor CSRF test from the owner acceptance blocked by the Day 12 policy TODO.

## Concept (25 minutes)

**Assistant-provided capstone architecture:** [../assistant-architecture-notes-15-70.md#day-21](<../assistant-architecture-notes-15-70.md#day-21>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

Cookie session, CSRF header, identity, and object authorization are separate checks. The acceptance test exists, but its real owner-policy prerequisite remains unfinished.

## Interview-first system-design mock (45 minutes inside practice)

**Prompt:** [Q07](../interview/question-bank.md#q07) — API gateway route and protect. For this interview question only, write and save your initial proposal before hints, reference answers, or AI. Use the exact 5/5/15/10/10 phases and 100-point rubric in [the mock rubric](../interview/mock-rubric.md).

**Timebox:** 45 minutes of the existing 85-minute practice block for the mock, then 40 minutes for this EAI slice: Run the bounded security infrastructure matrix; keep Day 21 learner authorization blocked at its actual Day 12 prerequisite until that TODO is genuinely complete. No overtime. The unchanged full acceptance selector remains the completion gate.

**Deferred acceptance:** `S2-D21-1` is in the [Sprint 2 session scope](../interview/sprint2-session-scope.md). Preserve the actual result and keep this day pending until the named completion check and all original acceptance criteria pass.

## Evidence (10 minutes)

Security integration matrix. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only. Record the assistant-provided architecture note used; no capstone design proposal or retrospective justification is assigned.

## Planned isolated command

Instructor HTTP boundary: `./mvnw -Dtest=Day21SecurityInfrastructureTest test`

Learner acceptance: `./mvnw -Plearner-tests -Dtest=Day21ContractTest test` (expected to stop at the existing `JourneyAccessPolicy` TODO until that prerequisite is completed).

The instructor test obtains and sends an actual server-issued CSRF cookie/header pair over loopback HTTP. Missing/invalid tokens return 403 before the handler; a valid token passes the CSRF filter. It does not prove production authentication or ownership.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
