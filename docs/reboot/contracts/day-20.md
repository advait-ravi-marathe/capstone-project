# Day 20 — 2026-11-08: Connect selection action UI

**State:** React/TypeScript component scaffold and fixed acceptance tests are installed; learner implementation remains pending. The focused suite is expected to fail at unimplemented rendering/interaction behavior.

## Learner seam

`SelectQuoteButton.tsx`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

POST exact journey/version once while pending. HTTP 200 is server-confirmed selection; 409 prompts refresh, 401 prompts sign-in, 404 shows unavailable. Never imply purchase or active coverage.

## Required edge cases

Success, conflict, auth, unavailable, network error, double click.

## Prerequisite and isolation

Days 1, 3 and 11 response contracts; fake API. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Draw the selection click-to-response flow and mark stale-version handling. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

HTTP side effects, retry decisions and truthful UI state.

## Evidence (10 minutes)

Four component scenarios. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`npm --prefix frontend test -- --run src/__tests__/Day20Contract.test.tsx`

This selector runs the installed component contract tests. Expected red assertions identify the unimplemented learner behavior; that is not a passing learner result. Infrastructure checks are separate.

## Interview transfer (inside the current evidence block)

**Question:** Should a timeout be retried blindly? How do 401 and 409 alter UI behavior?

**Observable evidence:** Trace success/conflict/unknown-outcome states; avoid claiming coverage was purchased; distinguish transport retry from safe domain repetition.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A4](../interview-evidence-overlay.md#a4) (anecdotal); [A6](../interview-evidence-overlay.md#a6) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date 2026-11-08 --roadmap-day 20` first. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../dsa/guided-transfer-policy.md](../dsa/guided-transfer-policy.md) and [the DSA error log](../dsa-error-log-template.md).
