# Actual learning Day 38: Ownership check and held-out freeze

**Status: Not started.** Planned date: **2026-12-03** (calendar planning only; a date never advances status).

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA repository; record attempts only on actual dates.
- Role interview/AWS bank: 30 minutes — **Continue target-role rotation**.
- AI work: 90 minutes total including any scheduled visibility or reserve slice; no extra hours.
- If actual DSA review consumes 75 minutes, reduce AI to75; at90 minutes, reduce AI to60. Carry unfinished scope forward.

## Role/AWS bank (30 minutes)

Use actual interview/recruiter feedback and prior weak dimensions; no extra time.
Use the [role-interview bank](../role-bank.md) or [AWS syllabus bank](../aws-bank.md) as appropriate. Record actual prompt/topic, minutes, one weak dimension and a next action. Certification study stays inside this block; no purchase, account provisioning or exam booking.

## AI work (90 minutes)

Complete the U028 attempt by implementing a small regression patch or recording a defect diagnosis, exact evidence and help used. Do not use assistant/reference code as learner work. In the final 30 minutes, prepare U029's frozen one-shot evaluation: confirm ten-case fixture digest and frozen configuration; do not inspect answers to tune the prompt or retriever.

### Accepted segments

| Segment | Unit | Minutes | Contract |
|---|---|---:|---|
| primary | AIFDE-V2-U028 | 60 min | [AIFDE-V2-U028](../units/u028.md) |
| primary | AIFDE-V2-U029 | 30 min | [AIFDE-V2-U029](../units/u029.md) |

### Learn from exact sources

The [U028/U029 contracts](../units/u028.md) and the frozen [U014 evaluation split](../../../ai/fde_v2/coverstory/evals/README.md).

Run only deterministic selectors assigned to a current segment. Instructor witnesses validate mechanics; they are not learner completion or real-model evidence.

**Real model gate:** pinned local generation/embedding artifacts remain blocked/unverified in this environment. No model request or test double may be presented as real inference. A deterministic pass is **not a pass** of real-model, held-out, production or P1 acceptance.

## Evidence

Save exact command/output, actual elapsed minutes, artifact digests where applicable, observed failures and one next action. Keep synthetic tests, actual local model results, role-play and external customer evidence distinct. Never mark a unit complete because its scheduled date arrived.
