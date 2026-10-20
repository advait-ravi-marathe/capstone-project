# Day 03 — 2026-10-22: Map failures consistently

**State:** Contract defined; instructor fixture/test scaffold and learner attempt are pending. This page is a planned contract, not a test result.

## Learner seam

`ApiExceptionHandler`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Preserve the Day 2 error shape `{"code":"...","message":"..."}`. Unknown resource is HTTP 404 with `RESOURCE_NOT_FOUND` and `Resource was not found`; version conflict is HTTP 409 with `VERSION_CONFLICT` and `The resource version conflicts`; unexpected failure is HTTP 500 with `INTERNAL_ERROR` and `The request could not be completed`. Never return stack, SQL, request body or internal exception text.

## Required edge cases

Fixture not-found, conflict and unexpected exception; verify safe fields and no secret text.

## Prerequisite and isolation

Day 2; mocked service throws fixed fixture exceptions. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Map one expected domain failure and one unexpected failure across the HTTP boundary. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

HTTP status semantics and failure boundaries [B].

## Evidence (10 minutes)

Three negative response snapshots. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day03ContractTest test`

This focused test and its learner TODO seam are installed. The command was run with the other installed Day 2–7 selectors; expected TODO failures are recorded in `evidence/reboot/initial-contracts-2026-10-20.json`. It is not a passing learner result. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**New pattern anchor:** LeetCode 1 Two Sum — hash lookup. Learn the pattern for 25 minutes, solve independently for 20 minutes, and spend 15 minutes on same-day D recall plus the error log. Record the actual first-attempt date; schedule D+3, D+7 and D+21 reviews from that date. Planned dates in the manifest are forecasts only. A failed prerequisite/review displaces the next new anchor; do not add overtime. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).
## Understanding check (within the existing 10-minute evidence block)

Choose one or two prompts and keep the response brief. This check uses the existing 10 evidence minutes; it is not extra work.

1. Explain what Day 3 (Map failures consistently) does, why its data structure, module boundary, or transaction choice fits, one alternative, and one failure case.
2. Without AI assistance, make or debug one small change to this feature and explain the observed result.

Record behavior (fixed-test result), explanation (invariants and trade-offs), and independent adaptation as separate outcomes. A scaffold or passing tests alone earns no explanation or adaptation credit.
