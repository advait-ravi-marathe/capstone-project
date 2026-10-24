# Day 70 — 2027-01-01: Complete the mastery, recovery and design mock

## Contract

**Learner-owned seam:** `Evidence review and local application design explanation`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

Present the system boundary, trust/ownership flow, CSRF choice, migration lifecycle, no-write preview invariant, and recovery evidence. Report exact passing and failing selectors. No new feature or extra minutes is assigned.

## Acceptance and expected TODO state

The instructor integrity selector is installed and passed 3/3; it checks the evidence boundary only and is not a learner completion test. The learner task is to review the recorded results and complete the existing 20-minute explain/design handover mock. No TODO failure is expected from this integrity selector.

**Acceptance test:** `src/test/java/assignments/local/LocalApplicationManifestInfrastructureTest.java`
**Acceptance selector:** `day70EvidenceAndDesignMockStayWithinTheDefinedSlice`
**Instructor integrity command:** `./mvnw -Plearner-tests -Dtest=LocalApplicationManifestInfrastructureTest#day70EvidenceAndDesignMockStayWithinTheDefinedSlice test`
**Instructor infrastructure test:** `src/test/java/assignments/local/LocalApplicationManifestInfrastructureTest.java`
**Instructor command:** `./mvnw -Dtest=LocalApplicationManifestInfrastructureTest test`
## Learner-first recovery proposal (5 minutes within the existing practice block)

Before reading the handover example or discussing the design mock, list the actual pass/fail/blocked/not-run evidence and select the first blocker. Then use up to 50 minutes on first-blocker recovery, retain the existing 20-minute handover/mock and 10-minute triage: 5 + 50 + 20 + 10 = 85 minutes. If recovery needs more, shorten the mock; no overtime. The integrity selector checks structure, not mastery. Unfinished work stays open as **S5-D70-HANDOVER**.

## Selector limits and open evidence

- The integrity selector verifies manifest/evidence scope only. It does not assess mastery, validate a real demo, or prove the learner completed the bounded handover.

## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- No exact matching course lesson was verified for this task; no unrelated video is assigned.
- Fit: assessment review no new lecture. No new lecture. Use the existing 25-minute block to explain one observed boundary using actual selector evidence, then identify recovery work; the design mock is not evidence of implementation completion.
- Focused worked references: [C4 Software System — software system](https://c4model.com/abstractions/software-system); [C4 Container — container](https://c4model.com/abstractions/container); [C4 Component — component](https://c4model.com/abstractions/component).
- Video viewing: 0:00 at 1×; the remaining 25:00 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Prediction record:** Complete the task-specific proposal above before guidance; do not add a post-hoc architecture essay.

**Teach-back:** Explain the invariant, its enforcement point and the observable evidence that proves it.

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner-owned changed paths in `evidence/continuation-57-70/day-70.json`. Leave completion and actual dates blank until the work is actually done.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
