# Day 26 — 2026-11-14: Record minimal selection audit

**State:** Real PostgreSQL scaffold and fixed learner tests are installed; the JDBC transaction/audit behavior remains a learner TODO.

**Course lesson:** No exact verified course lecture covers atomic audit/data minimization. Use the linked short transaction-bound example with only contract-approved fields. See the [Days 1–30 map](../course-learning-map.md) for verified course source, gaps, and time policy. Worked guide: [course-learning-supplements.md](../course-learning-supplements.md#day-26---atomic-audit-and-minimization).

## Learner seam

audit migration/write service. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

First successful selection appends exactly `{action:"SELECTION_CREATED", resourceId, actorId, occurredAt}` in the same transaction; rollback removes both selection and audit; a repeated request appends no creation event. Store no request or quote payload.

## Required edge cases

Success, repeat, rollback; exact event count/fields; no payload column.

## Prerequisite and isolation

Days 10, 12 and 25; fixed actor/time and seeded input. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Trace the minimal audit fields from action to stored record without copying personal data. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

Atomic audit and data minimization.

## Evidence (10 minutes)

Success/repeat/rollback database counts. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day26ContractTest test`

This selector runs the fixed cases against real embedded PostgreSQL. The tests are expected to fail at the learner-owned transaction/audit TODO. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**Consolidation day — no new mandatory problem.** Use 25 minutes to refresh due patterns, 20 minutes to cold-solve the weakest due anchor, and 15 minutes for same-day/other due active recall and the error log. Planned review items: LC 383 (D+21 from Day 5), LC 209 (D+7 from Day 19), LC 155 (D+3 from Day 23). Recompute from actual first-attempt dates and record completion only after the review. If no review is actually due, cold-recall the weakest earlier anchor; failed recall displaces the next new anchor. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** Can the selection commit while its audit event is missing? What happens on repeat?

**Observable evidence:** Explain shared transaction and minimum audit fields; demonstrate first/repeat/rollback counts; distinguish this local atomic record from external event-delivery guarantees.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [Official expectations](../interview-evidence-overlay.md#official-expectations) (official); [A4](../interview-evidence-overlay.md#a4) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.
