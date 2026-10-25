# Day 26 — 2026-11-20: Record minimal selection audit

**State:** Real PostgreSQL scaffold and fixed learner tests are installed; the JDBC transaction/audit behavior remains a learner TODO.

**Course/concept bridge:** No exact verified audit lecture. Read the named PostgreSQL transactions and `INSERT ... RETURNING` sections, then run the [Day 26 guide](../teaching/day-26-audit.md#first-change-repeat-and-failure) within the existing 25-minute block. See the [Days 1–30 map](../course-learning-map.md); exact catalog title metadata is not a claim of playback.

## Learner seam

audit migration/write service. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

First successful selection appends exactly `{action:"SELECTION_CREATED", resourceId, actorId, occurredAt}` in the same transaction; rollback removes both selection and audit; a repeated request appends no creation event. Store no request or quote payload.

## Required edge cases

Success, repeat, rollback; exact event count/fields; no payload column.

## Prerequisite and isolation

Days 10, 12 and 25; fixed actor/time and seeded input. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**Worked prompt:** Compare first, repeated, and failed-audit paths. Why is a business creation event not the same as a request log?

## Concept (25 minutes)

**Assistant-provided capstone architecture:** [../assistant-architecture-notes-15-70.md#day-26](<../assistant-architecture-notes-15-70.md#day-26>). This note supplies the implementation decision and concise rationale; read it within this day's existing 25-minute concept/course allocation, not as extra work. Use it as context for the day's existing task; on implementation days, implement and test the stated design. It is supplied awareness, not a design exercise. No capstone architecture proposal or post-hoc justification is assigned; learner design proposals occur only for the assigned system-design interview question on scheduled mock days.

First-change versus repeat semantics, one transaction, minimal event fields, and rollback.

## Evidence (10 minutes)

Success/repeat/rollback database counts. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only. Record the assistant-provided architecture note used; no capstone design proposal or retrospective justification is assigned.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day26ContractTest test`

This selector runs the fixed cases against real embedded PostgreSQL. The tests are expected to fail at the learner-owned transaction/audit TODO. Infrastructure checks are separate.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
