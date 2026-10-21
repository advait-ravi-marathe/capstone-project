# Day 12 — 2026-10-31: Enforce owner-scoped access

**State:** Installed: a real Spring Security test filter chain verifies HTTP Basic authentication and CSRF token rejection/acceptance. The production owner policy and production security configuration remain learner TODOs. The owner-policy HTTP acceptance test is tagged and expected red until the policy is implemented.

## Learner seam

authorization policy / security config. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Anonymous returns 401. An authenticated owner can read/select that owner’s journey; another owner gets the Day 3 non-disclosing 404 envelope. Client-supplied `ownerId` is ignored and never establishes identity. Use Spring’s supported test principal/session fixture; no custom JWT or cryptography.

## Required edge cases

Anonymous, own, other owner, forged owner field.

## Prerequisite and isolation

Days 3, 9–11; Spring test principals and fixed owned-resource fixture. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Map identity source, ownership check, and resource lookup boundary; note unresolved production filter-chain wiring. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

Authentication vs authorization and object access control [H].

## Evidence (10 minutes)

Owner/other/anonymous matrix. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day12ContractTest test`

This selector runs the installed owner-policy cases with a fixed principal fixture. It currently fails at the explicit TODO. It does not prove HTTP 401/404 behavior or install Spring Security; those integration checks remain blocked. Infrastructure checks are separate.

## Interview transfer (inside the current evidence block)

**Question:** Who supplies identity? Why is filtering by a client owner ID insufficient?

**Observable evidence:** Trace trusted principal to scoped query; demonstrate owner/other/anonymous outcomes; explain authentication versus authorization.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A4](../interview-evidence-overlay.md#a4) (anecdotal); [A5](../interview-evidence-overlay.md#a5) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 12` first. This command uses the actual Asia/Kolkata local study date; contract dates are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../dsa/guided-transfer-policy.md](../dsa/guided-transfer-policy.md) and [the DSA error log](../dsa-error-log-template.md).
