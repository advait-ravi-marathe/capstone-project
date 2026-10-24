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

**Review prompt (within the 25-minute concept block):** Select one observed behavior and explain its invariant, evidence, limitation, and next diagnostic. Use the [evidence-state example](../teaching/day-30-evidence-review.md#three-evidence-states); do not add an architecture exercise.

## Concept (25 minutes)

Evidence-based explanation and honest limits. Review the [Day 30 worked example](../teaching/day-30-evidence-review.md#a-concise-evidence-based-explanation); no new SDE II vocabulary or feature is assigned.

## Evidence (10 minutes)

5-minute demo script, current gate, prerequisite-ranked backlog. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day30ContractTest test`

This instructor integrity selector is expected to pass against fixed status/evidence fixtures and the live evolving checklist. It rejects unsupported statuses and passed rows without a repository-local evidence file; it does not require the live checklist to stay open. Infrastructure checks are separate.

## Interview transfer (inside the current evidence block)

**Question:** What invariant did you observe, which evidence supports it, what limitation remains, and what bounded measurement or repair comes next? Treat 10× load as a hypothesis unless measured.

**Observable evidence:** 20-minute mock replaces implementation: explain one existing sequence/data view, one already-taught trade-off, one actual observation, and one open gap. Do not invent users, impact, incidents, or performance measurements.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [Official expectations](../interview-evidence-overlay.md#official-expectations) (official); [A3](../interview-evidence-overlay.md#a3) (anecdotal); [A5](../interview-evidence-overlay.md#a5) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.

**Milestone timebox:** Use 20 minutes from the existing 85-minute implementation block (65 build/test + 20 mock); do not add hours. If the integration gate is blocked, explain the actual failing boundary and next diagnostic step, without presenting a passing demo.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
