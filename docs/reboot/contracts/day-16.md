# Day 16 — 2026-11-04: Bound quote-history reads

**State:** Real-PostgreSQL pagination fixture and learner query seam installed. The focused tests currently stop at the explicit pagination TODO.

## Learner seam

`QuoteHistoryQuery`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

`GET /api/journeys/{journeyId}/quotes` uses zero-based `page` (default 0), `size` (default 10, maximum 20), newest first by `createdAt` descending, then `quoteVersionId` ascending for ties. Empty/final pages are valid. Negative page, size below 1 or above 20 returns 400 `INVALID_REQUEST`.

## Required edge cases

Timestamp ties, page size 0/over 20, empty/final page, adjacent pages have no duplicate.

## Prerequisite and isolation

Days 9 and 12; seeded history. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Choose a data-flow or sequence view for bounded quote history and its ownership boundary. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

Sorting, pagination cost and query complexity.

## Evidence (10 minutes)

Adjacent page fixture assertions. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day16ContractTest test`

This selector starts a real embedded PostgreSQL instance with timestamp ties and multiple pages. Expected TODO failures are not a pagination or infrastructure pass.

## Interview transfer (inside the current evidence block)

**Question:** Why does sorting only by timestamp fail? What happens when rows are inserted between offset-page requests?

**Observable evidence:** Demonstrate stable fixture ties; explain offset limitation and cursor alternative conceptually without expanding implementation scope.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A5](../interview-evidence-overlay.md#a5) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date 2026-11-04 --roadmap-day 16` first. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../dsa/guided-transfer-policy.md](../dsa/guided-transfer-policy.md) and [the DSA error log](../dsa-error-log-template.md).
