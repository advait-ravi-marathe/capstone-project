# Day 21 — 2026-11-09: Protect cookie-authenticated writes

**State:** Blocked; no Spring Security dependency/configuration is installed. The current Day 1 application remains intentionally unauthenticated. No stub-only test is presented as CSRF evidence.

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

The selector is not runnable yet because the Spring Security dependency and production filter-chain seam are absent. Add the security dependency and isolated route configuration before acceptance testing. Keep the Day 1 route behavior intact while doing so; use a real Spring Security filter chain for the 401/403/valid-token matrix. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**New pattern anchor:** LeetCode 20 Valid Parentheses — stack. Learn the pattern for 25 minutes, solve independently for 20 minutes, and spend 15 minutes on same-day D recall plus the error log. Record the actual first-attempt date; schedule D+3, D+7 and D+21 reviews from that date. Planned dates in the manifest are forecasts only. A failed prerequisite/review displaces the next new anchor; do not add overtime. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).
## Understanding check (within the existing 10-minute evidence block)

Choose one or two prompts and keep the response brief. This check uses the existing 10 evidence minutes; it is not extra work.

1. Explain what Day 21 (Protect cookie-authenticated writes) does, why its data structure, module boundary, or transaction choice fits, one alternative, and one failure case.
2. Without AI assistance, make or debug one small change to this feature and explain the observed result.

Record behavior (fixed-test result), explanation (invariants and trade-offs), and independent adaptation as separate outcomes. A scaffold or passing tests alone earns no explanation or adaptation credit.
