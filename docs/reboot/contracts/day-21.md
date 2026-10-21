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

An actual Spring Security filter chain is installed in the instructor test harness. It exercises CSRF rejection on `POST /api/quote-selections` before the learner policy runs. Production cookie/session configuration and the valid-token business success path remain learner work; the test-only users and chain are not application configuration.

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
