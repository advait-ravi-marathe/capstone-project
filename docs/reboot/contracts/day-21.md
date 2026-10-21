# Day 21 — 2026-11-09: Protect cookie-authenticated writes

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

Cookie session, CSRF header, identity, and object authorization are separate checks. The acceptance test exists, but its real owner-policy prerequisite remains unfinished.

## Evidence (10 minutes)

Security integration matrix. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

Instructor HTTP boundary: `./mvnw -Dtest=Day21SecurityInfrastructureTest test`

Learner acceptance: `./mvnw -Plearner-tests -Dtest=Day21ContractTest test` (expected to stop at the existing `JourneyAccessPolicy` TODO until that prerequisite is completed).

The instructor test obtains and sends an actual server-issued CSRF cookie/header pair over loopback HTTP. Missing/invalid tokens return 403 before the handler; a valid token passes the CSRF filter. It does not prove production authentication or ownership.

## Interview transfer (inside the current evidence block)

**Question:** Why do cookie-authenticated writes need CSRF protection? Does CORS replace it?

**Observable evidence:** Explain browser trust boundaries and demonstrate missing/invalid/valid token outcomes without custom cryptography.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A5](../interview-evidence-overlay.md#a5) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 21` first. This command uses the actual Asia/Kolkata local study date; contract dates are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../dsa/guided-transfer-policy.md](../dsa/guided-transfer-policy.md) and [the DSA error log](../dsa-error-log-template.md).
