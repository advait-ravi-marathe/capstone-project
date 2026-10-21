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

## Interview transfer (inside the current evidence block)

**Question:** When are 400, 404, 409 and 500 appropriate? Which details must never reach a client?

**Observable evidence:** Predict mapped responses; explain sanitized 500 and show one test per error class. Distinguish 401 from 403 without changing the contract’s non-disclosing 404 policy.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A4](../interview-evidence-overlay.md#a4) (anecdotal); [A5](../interview-evidence-overlay.md#a5) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided first encounter:** [LeetCode 383 Ransom Note](https://leetcode.com/problems/ransom-note/). Use the Premium editorial/solution only after stating the problem in your own words and making a first attempt; the resulting work remains guided/assisted and earns no independent-transfer credit. Preserve the original canonical anchor identities and record any actual attempt only when it occurs.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date 2026-10-22 --roadmap-day 3` first. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../dsa/guided-transfer-policy.md](../dsa/guided-transfer-policy.md) and [the DSA error log](../dsa-error-log-template.md).
