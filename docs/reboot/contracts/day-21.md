# Day 21 — 2026-11-09: Protect cookie-authenticated writes

**State:** Blocked; no Spring Security dependency/configuration is installed. The current Day 1 application remains intentionally unauthenticated. No stub-only test is presented as CSRF evidence.

**Course lesson:** [Master Spring Boot 3 & Spring Framework 6 with Java — in28Minutes Official](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/); section “Spring Security: Securing Spring Boot Applications with Spring Security”. Watch: “Step 08 - Exploring Spring Security - Cross Site Request Forgery - CSRF” (11:08); “Step 09 - Exploring Spring Security - CSRF for REST API” (8:40); “Step 11 - Exploring Spring Security - Getting Started with CORS” (2:34). 22:22 viewing. Retain CSRF protection; do not copy the separate lecture on disabling CSRF. See the [Days 1–30 map](../course-learning-map.md) for course verification and the total-time rule. Worked guide: [course-learning-supplements.md](../course-learning-supplements.md#day-21---csrf-boundary).

## Learner seam

security config / API client. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Keep Spring Security CSRF enabled for cookie-authenticated writes. Missing/invalid token returns 403; valid session plus token is accepted. Use supported session/test principal and default `XSRF-TOKEN` cookie / `X-XSRF-TOKEN` header; do not build a credential system.

## Required edge cases

Anonymous/session, missing/invalid/valid CSRF token.

## Prerequisite and isolation

Days 12 and 18–20; session/CSRF test helpers. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Diagram cookie, CSRF token/header, and write request; identify the trust boundary. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

Cookies, CSRF vs CORS and browser trust boundaries [M].

## Evidence (10 minutes)

Security integration matrix. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day21ContractTest test`

An actual Spring Security filter chain is installed in the instructor test harness. It exercises CSRF rejection on `POST /api/quote-selections` before the learner policy runs. Production cookie/session configuration and the valid-token business success path remain learner work; the test-only users and chain are not application configuration.

## Separate Java DSA block (60 minutes)

**New pattern anchor:** LeetCode 20 Valid Parentheses — stack. Learn the pattern for 25 minutes, solve independently for 20 minutes, and spend 15 minutes on same-day D recall plus the error log. Record the actual first-attempt date; schedule D+3, D+7 and D+21 reviews from that date. Planned dates in the manifest are forecasts only. A failed prerequisite/review displaces the next new anchor; do not add overtime. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** Why do cookie-authenticated writes need CSRF protection? Does CORS replace it?

**Observable evidence:** Explain browser trust boundaries and demonstrate missing/invalid/valid token outcomes without custom cryptography.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A5](../interview-evidence-overlay.md#a5) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.
