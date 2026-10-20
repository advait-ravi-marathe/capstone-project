# Day 21 — 2026-11-09: Protect cookie-authenticated writes

**State:** Contract defined; instructor fixture/test scaffold and learner attempt are pending. This page is a planned contract, not a test result.

## Learner seam

security config / API client. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Keep Spring Security CSRF enabled for cookie-authenticated writes. Missing/invalid token returns 403; valid session plus token is accepted. Use supported session/test principal and default `XSRF-TOKEN` cookie / `X-XSRF-TOKEN` header; do not build a credential system.

## Required edge cases

Anonymous/session, missing/invalid/valid CSRF token.

## Prerequisite and isolation

Days 12 and 18–20; session/CSRF test helpers. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

## Concept (25 minutes)

Cookies, CSRF vs CORS and browser trust boundaries [M].

## Evidence (10 minutes)

Security integration matrix. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day21ContractTest test`

This exact selector is part of the contract, but this test class is not installed yet. The command must not be reported as passing until the instructor scaffold exists and is actually run. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

LeetCode 15 3Sum. Use 10 minutes recall/example, 35 minutes independent attempt, 10 minutes tests/complexity and 5 minutes error log. An unfinished attempt is valid work, not a solved claim. Prior solved history is preserved and must not be inferred from a scaffold.
