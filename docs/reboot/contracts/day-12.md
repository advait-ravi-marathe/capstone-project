# Day 12 — 2026-10-31: Enforce owner-scoped access

**State:** The isolated owner-policy seam and fixed learner tests are installed. HTTP security configuration/session wiring remains open; learner attempt is pending and the focused suite is expected to fail at its TODO.

## Learner seam

authorization policy / security config. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Anonymous returns 401. An authenticated owner can read/select that owner’s journey; another owner gets the Day 3 non-disclosing 404 envelope. Client-supplied `ownerId` is ignored and never establishes identity. Use Spring’s supported test principal/session fixture; no custom JWT or cryptography.

## Required edge cases

Anonymous, own, other owner, forged owner field.

## Prerequisite and isolation

Days 3, 9–11; Spring test principals and fixed owned-resource fixture. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Map identity source, ownership check, and resource lookup boundary; note unresolved HTTP security wiring. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

Authentication vs authorization and object access control [H].

## Evidence (10 minutes)

Owner/other/anonymous matrix. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day12ContractTest test`

This selector runs the installed owner-policy cases with a fixed principal fixture. It currently fails at the explicit TODO. It does not prove HTTP 401/404 behavior or install Spring Security; those integration checks remain blocked. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**Consolidation day — no new mandatory problem.** Use 25 minutes to refresh due patterns, 20 minutes to cold-solve the weakest due anchor, and 15 minutes for same-day/other due active recall and the error log. Planned review items: LC 383 (D+7 from Day 5), LC 238 (D+3 from Day 9). Recompute from actual first-attempt dates and record completion only after the review. If no review is actually due, cold-recall the weakest earlier anchor; failed recall displaces the next new anchor. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).
## Understanding check (within the existing 10-minute evidence block)

Choose one or two prompts and keep the response brief. This check uses the existing 10 evidence minutes; it is not extra work.

1. Explain what Day 12 (Enforce owner-scoped access) does, why its data structure, module boundary, or transaction choice fits, one alternative, and one failure case.
2. Without AI assistance, make or debug one small change to this feature and explain the observed result.

Record behavior (fixed-test result), explanation (invariants and trade-offs), and independent adaptation as separate outcomes. A scaffold or passing tests alone earns no explanation or adaptation credit.
