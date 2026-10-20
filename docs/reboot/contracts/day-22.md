# Day 22 — 2026-11-10: Add safe request correlation

**State:** Fixed filter scaffold and learner acceptance tests are installed; the filter TODO remains unimplemented.

## Learner seam

request filter / error mapping. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Every response has `X-Request-Id` matching `[A-Za-z0-9._-]{1,64}`. Missing IDs are generated; malformed supplied IDs replaced. One error log references the selected ID, never applicant body/token/cookie. Request IDs are not metric labels.

## Required edge cases

Absent, valid, malformed ID; captured-log redaction assertions.

## Prerequisite and isolation

Day 3; fixed request fixtures and fake logger. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Show request-ID creation/validation and which log fields must be excluded. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

Observability vs privacy, logs vs metrics.

## Evidence (10 minutes)

Captured log assertions. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day22ContractTest test`

The fixed request-filter tests run through the named selector and currently fail at the explicit correlation TODO. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**Consolidation day — no new mandatory problem.** Use 25 minutes to refresh due patterns, 20 minutes to cold-solve the weakest due anchor, and 15 minutes for same-day/other due active recall and the error log. Planned review items: LC 121 (D+21 from Day 1), LC 11 (D+7 from Day 15), LC 209 (D+3 from Day 19). Recompute from actual first-attempt dates and record completion only after the review. If no review is actually due, cold-recall the weakest earlier anchor; failed recall displaces the next new anchor. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).
## Understanding check (within the existing 10-minute evidence block)

Choose one or two prompts and keep the response brief. This check uses the existing 10 evidence minutes; it is not extra work.

1. Explain what Day 22 (Add safe request correlation) does, why its data structure, module boundary, or transaction choice fits, one alternative, and one failure case.
2. Without AI assistance, make or debug one small change to this feature and explain the observed result.

Record behavior (fixed-test result), explanation (invariants and trade-offs), and independent adaptation as separate outcomes. A scaffold or passing tests alone earns no explanation or adaptation credit.
