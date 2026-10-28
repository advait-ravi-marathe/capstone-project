# Actual learning Day 32: Reproducible build and CI boundary

**Status: Not started.** Planned date: **2026-11-27** (calendar planning only; a date never advances status).

## Fixed 180-minute budget

- DSA: 60 minutes in the separate DSA repository; record attempts only on actual dates.
- Role interview/AWS bank: 30 minutes — **Continue target-role rotation**.
- AI work: 90 minutes total including any scheduled visibility or reserve slice; no extra hours.
- If actual DSA review consumes 75 minutes, reduce AI to75; at90 minutes, reduce AI to60. Carry unfinished scope forward.

## Role/AWS bank (30 minutes)

Use actual interview/recruiter feedback and prior weak dimensions; no extra time.
Use the [role-interview bank](../role-bank.md) or [AWS syllabus bank](../aws-bank.md) as appropriate. Record actual prompt/topic, minutes, one weak dimension and a next action. Certification study stays inside this block; no purchase, account provisioning or exam booking.

## AI work (90 minutes)

Read the supplied Dockerfile, minimal API dependency list and CI workflow. Trace the health check and explain why weights are excluded. Run the bounded container smoke command; it builds a local image and checks loopback health plus expected model-absent 503. If Docker/base-image access is unavailable, report that exact blocker; do not call a static test a container pass.

### Accepted segments

| Segment | Unit | Minutes | Contract |
|---|---|---:|---|
| primary | AIFDE-V2-U024 | 90 min | [AIFDE-V2-U024](../units/u024.md) |

### Learn from exact sources

Official [Docker build overview](https://docs.docker.com/build/concepts/overview/) and [GitHub Actions workflow syntax](https://docs.github.com/en/actions/writing-workflows/workflow-syntax-for-github-actions); inspect the checked-in workflow rather than assuming a green manifest proves a container run.

Run only deterministic selectors assigned to a current segment. Instructor witnesses validate mechanics; they are not learner completion or real-model evidence.

**Real model gate:** pinned local generation/embedding artifacts remain blocked/unverified in this environment. No model request or test double may be presented as real inference. A deterministic pass is **not a pass** of real-model, held-out, production or P1 acceptance.

## Evidence

Save exact command/output, actual elapsed minutes, artifact digests where applicable, observed failures and one next action. Keep synthetic tests, actual local model results, role-play and external customer evidence distinct. Never mark a unit complete because its scheduled date arrived.
