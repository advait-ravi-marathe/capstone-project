# Day 26 — 2026-11-18: Record minimal selection audit

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

First-change versus repeat semantics, one transaction, minimal event fields, and rollback.

## Evidence (10 minutes)

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day26ContractTest test`

This selector runs the fixed cases against real embedded PostgreSQL. The tests are expected to fail at the learner-owned transaction/audit TODO. Infrastructure checks are separate.

## Interview transfer (inside the current evidence block)

**Question:** Can the selection commit while its audit event is missing? What happens on repeat?

**Observable evidence:** Explain shared transaction and minimum audit fields; demonstrate first/repeat/rollback counts; distinguish this local atomic record from external event-delivery guarantees.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [Official expectations](../interview-evidence-overlay.md#official-expectations) (official); [A4](../interview-evidence-overlay.md#a4) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
