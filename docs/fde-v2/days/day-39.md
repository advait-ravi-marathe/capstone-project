# Actual learning Day 39: Held-out evaluation and audit

**Status: Not started.** Planned date: **2026-12-04** (calendar planning only; a date never advances status).

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA repository; record attempts only on actual dates.
- Role interview/AWS bank: 30 minutes — **Continue target-role rotation**.
- AI work: 90 minutes total including any scheduled visibility or reserve slice; no extra hours.
- If actual DSA review consumes 75 minutes, reduce AI to75; at90 minutes, reduce AI to60. Carry unfinished scope forward.

## Role/AWS bank (30 minutes)

Use actual interview/recruiter feedback and prior weak dimensions; no extra time.
Use the [role-interview bank](../role-bank.md) or [AWS syllabus bank](../aws-bank.md) as appropriate. Record actual prompt/topic, minutes, one weak dimension and a next action. Certification study stays inside this block; no purchase, account provisioning or exam booking.

## AI work (90 minutes)

Run the held-out command once only after verifying the configuration is frozen. If it writes a blocked/failed attempt because real model artifacts are unavailable, preserve that file and do not rerun or relabel it. If it produces actual pinned-model outputs, inspect every cited source passage and complete a separate review bound to the exact run digest. The acceptance tool validates structure, not semantic truth.

### Accepted segments

| Segment | Unit | Minutes | Contract |
|---|---|---:|---|
| primary | AIFDE-V2-U029 | 60 min | [AIFDE-V2-U029](../units/u029.md) |
| buffer | AIFDE-V2-U029 | 30 min | [AIFDE-V2-U029](../units/u029.md) |

### Learn from exact sources

The [U029 contract](../units/u029.md), [held-out rubric](../templates/u029-grounding-review.json), and checked-in [ten-case fixture](../../../ai/fde_v2/coverstory/evals/questions-heldout.jsonl).

Run only deterministic selectors assigned to a current segment. Instructor witnesses validate mechanics; they are not learner completion or real-model evidence.

**Real model gate:** pinned local generation/embedding artifacts remain blocked/unverified in this environment. No model request or test double may be presented as real inference. A deterministic pass is **not a pass** of real-model, held-out, production or P1 acceptance.

## Evidence

Save exact command/output, actual elapsed minutes, artifact digests where applicable, observed failures and one next action. Keep synthetic tests, actual local model results, role-play and external customer evidence distinct. Never mark a unit complete because its scheduled date arrived.
