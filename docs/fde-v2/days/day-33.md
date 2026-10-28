# Actual learning Day 33: CI boundary and failure handling

**Status: Not started.** Planned date: **2026-11-28** (calendar planning only; a date never advances status).

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA repository; record attempts only on actual dates.
- Role interview/AWS bank: 30 minutes — **AWS certification: targeted syllabus/gap review**.
- AI work: 90 minutes total including any scheduled visibility or reserve slice; no extra hours.
- If actual DSA review consumes 75 minutes, reduce AI to75; at90 minutes, reduce AI to60. Carry unfinished scope forward.

## Role/AWS bank (30 minutes)

Use this 30-minute bank to address one C0 domain gap with current official AWS material and one scenario explanation. Do not repeat the initial diagnostic.
Use the [role-interview bank](../role-bank.md) or [AWS syllabus bank](../aws-bank.md) as appropriate. Record actual prompt/topic, minutes, one weak dimension and a next action. Certification study stays inside this block; no purchase, account provisioning or exam booking.

## AI work (90 minutes)

Use the U024 buffer to fix or reproduce one clean setup/test issue. Then inspect the assistant-supplied FastAPI failure boundary and status mapping using the official error-handling page. Do not add learner code to the infrastructure layer. The final 60 minutes begin U025: run the one-failure-then-recovery API acceptance and distinguish simulated injection from a real runtime outage.

### Accepted segments

| Segment | Unit | Minutes | Contract |
|---|---|---:|---|
| buffer | AIFDE-V2-U024 | 30 min | [AIFDE-V2-U024](../units/u024.md) |
| primary | AIFDE-V2-U025 | 60 min | [AIFDE-V2-U025](../units/u025.md) |

### Learn from exact sources

FastAPI official [handling errors](https://fastapi.tiangolo.com/tutorial/handling-errors/) and the [U024/U025 contracts](../units/u024.md).

Run only deterministic selectors assigned to a current segment. Instructor witnesses validate mechanics; they are not learner completion or real-model evidence.

**Real model gate:** pinned local generation/embedding artifacts remain blocked/unverified in this environment. No model request or test double may be presented as real inference. A deterministic pass is **not a pass** of real-model, held-out, production or P1 acceptance.

## Evidence

Save exact command/output, actual elapsed minutes, artifact digests where applicable, observed failures and one next action. Keep synthetic tests, actual local model results, role-play and external customer evidence distinct. Never mark a unit complete because its scheduled date arrived.
