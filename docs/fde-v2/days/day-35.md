# Actual learning Day 35: Local latency and resource baseline

**Status: Not started.** Planned date: **2026-11-30** (calendar planning only; a date never advances status).

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA repository; record attempts only on actual dates.
- Role interview/AWS bank: 30 minutes — **Continue target-role rotation**.
- AI work: 90 minutes total including any scheduled visibility or reserve slice; no extra hours.
- If actual DSA review consumes 75 minutes, reduce AI to75; at90 minutes, reduce AI to60. Carry unfinished scope forward.

## Role/AWS bank (30 minutes)

Use actual interview/recruiter feedback and prior weak dimensions; no extra time.
Use the [role-interview bank](../role-bank.md) or [AWS syllabus bank](../aws-bank.md) as appropriate. Record actual prompt/topic, minutes, one weak dimension and a next action. Certification study stays inside this block; no purchase, account provisioning or exam booking.

## AI work (90 minutes)

Run the twelve-request, concurrency-one real local benchmark only if the verified API/runtime and learner-owned AI seams are working. Inspect all samples, compare first with subsequent observed requests, and record environment limits. The last 30 minutes is the scheduled visibility/evidence note inside today's 90-minute AI total; it is not extra work. If the real model gate is blocked, preserve that result and do not write invented timings.

### Accepted segments

| Segment | Unit | Minutes | Contract |
|---|---|---:|---|
| primary | AIFDE-V2-U026 | 60 min | [AIFDE-V2-U026](../units/u026.md) |
| scheduled visibility/evidence note | shared within AI total | 30 min | Replaces AI capacity; not additional |

### Learn from exact sources

Python [`time.perf_counter`](https://docs.python.org/3/library/time.html#time.perf_counter) and the bounded [U026 contract](../units/u026.md).

Run only deterministic selectors assigned to a current segment. Instructor witnesses validate mechanics; they are not learner completion or real-model evidence.

**Real model gate:** pinned local generation/embedding artifacts remain blocked/unverified in this environment. No model request or test double may be presented as real inference. A deterministic pass is **not a pass** of real-model, held-out, production or P1 acceptance.

## Evidence

Save exact command/output, actual elapsed minutes, artifact digests where applicable, observed failures and one next action. Keep synthetic tests, actual local model results, role-play and external customer evidence distinct. Never mark a unit complete because its scheduled date arrived.
