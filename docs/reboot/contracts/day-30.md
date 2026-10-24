# Day 30 — 2026-11-22: Defend and assess the slice

**State:** Machine-checkable evidence checklist and instructor integrity test are installed; learner status remains evidence-driven and may evolve truthfully from open/blocked to passed when local evidence exists.

**Course lesson:** No new lecture required; review the weakest previously mapped lesson. Use the evidence checklist and explain observed behavior. Do not impose new architecture or unexplained SDE II expectations. See the [Days 1–30 map](../course-learning-map.md) for verified course source, gaps, and time policy. Worked guide: [day-30-evidence-review.md](../teaching/day-30-evidence-review.md).

## Learner seam

`docs/reboot/acceptance-checklist.md` plus `Day30ContractTest` in `src/test/java/assignments/reboot/Day30ContractTest.java`. The test verifies evidence links and manifest statuses, leaves absent/unverified records open, and accepts valid evidence-backed completion; it does not mark learner work complete. The architecture note records the explanation and ranked backlog. Do not implement later-day behavior early.

## Acceptance invariants

Rerun appropriate gate; independently explain dedup, money, transactions, authorization and UI race choices; list TODOs and evidence links; choose next scope only from demonstrated gaps; StreamForge remains deferred.

## Required edge cases

Machine-check checklist against actual manifest and referenced evidence; missing/unverified entries remain open, never auto-pass. The day-specific command checks this artifact:

## Prerequisite and isolation

Days 28–29; no manufactured integrated success. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

## Existing implementation boundary

Implement and test the existing learner-owned seam using the architecture note in the concept block. No separate capstone design proposal or post-hoc justification is assigned.

**Task boundary:** Keep the existing Day 30 evidence checklist and 20-minute review inside its original budget. Use the existing day's acceptance selector and preserve its full criteria.
## Concept (25 minutes)

**Assistant-provided capstone architecture:** [../assistant-architecture-notes-15-70.md#day-30](<../assistant-architecture-notes-15-70.md#day-30>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

Evidence-based explanation and honest limits. Review the [Day 30 worked example](../teaching/day-30-evidence-review.md#a-concise-evidence-based-explanation); no new SDE II vocabulary or feature is assigned.

## Evidence (10 minutes)

5-minute demo script, current gate, prerequisite-ranked backlog. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only. Record the assistant-provided architecture note used; no capstone design proposal or retrospective justification is assigned.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day30ContractTest test`

This instructor integrity selector is expected to pass against fixed status/evidence fixtures and the live evolving checklist. It rejects unsupported statuses and passed rows without a repository-local evidence file; it does not require the live checklist to stay open. Infrastructure checks are separate.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
