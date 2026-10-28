# Actual learning Day 34: Bounded recovery and performance setup

**Status: Not started.** Planned date: **2026-11-29** (calendar planning only; a date never advances status).

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA repository; record attempts only on actual dates.
- Role interview/AWS bank: 30 minutes — **AWS certification: targeted syllabus/gap review**.
- AI work: 90 minutes total including any scheduled visibility or reserve slice; no extra hours.
- If actual DSA review consumes 75 minutes, reduce AI to75; at90 minutes, reduce AI to60. Carry unfinished scope forward.

## Role/AWS bank (30 minutes)

Use this 30-minute bank to address one C0 domain gap with current official AWS material and one scenario explanation. Do not repeat the initial diagnostic.
Use the [role-interview bank](../role-bank.md) or [AWS syllabus bank](../aws-bank.md) as appropriate. Record actual prompt/topic, minutes, one weak dimension and a next action. Certification study stays inside this block; no purchase, account provisioning or exam booking.

## AI work (90 minutes)

Continue U025 by inspecting timeout, unavailable, malformed-response and cancellation behavior; do not add retries just for appearance. Spend the final 30 minutes opening the bounded U026 measurement command, checking loopback-only targeting, sample count and no-overwrite behavior. If the real API/model is unavailable, record U026 blocked; do not replace its endpoint with a fake.

### Accepted segments

| Segment | Unit | Minutes | Contract |
|---|---|---:|---|
| primary | AIFDE-V2-U025 | 30 min | [AIFDE-V2-U025](../units/u025.md) |
| buffer | AIFDE-V2-U025 | 30 min | [AIFDE-V2-U025](../units/u025.md) |
| primary | AIFDE-V2-U026 | 30 min | [AIFDE-V2-U026](../units/u026.md) |

### Learn from exact sources

FastAPI official [handling errors](https://fastapi.tiangolo.com/tutorial/handling-errors/), Python [`time.perf_counter`](https://docs.python.org/3/library/time.html#time.perf_counter), and [U025/U026 contracts](../units/u025.md).

Run only deterministic selectors assigned to a current segment. Instructor witnesses validate mechanics; they are not learner completion or real-model evidence.

**Real model gate:** pinned local generation/embedding artifacts remain blocked/unverified in this environment. No model request or test double may be presented as real inference. A deterministic pass is **not a pass** of real-model, held-out, production or P1 acceptance.

## Evidence

Save exact command/output, actual elapsed minutes, artifact digests where applicable, observed failures and one next action. Keep synthetic tests, actual local model results, role-play and external customer evidence distinct. Never mark a unit complete because its scheduled date arrived.
