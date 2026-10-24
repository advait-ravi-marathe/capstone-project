# Day 57 — 2026-12-19: Gate the local assembly on prior learner evidence

## Contract

**Learner-owned seam:** `LocalReadinessGate.assess`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

A readiness check accepts only actual evidence for required prior milestones. If anything is missing or failed, today becomes a recovery block; it does not add work or imply completion.

## Acceptance and expected TODO state

The fixed learner selector is installed and currently fails at this day’s explicit TODO. Do not implement learner behavior in the starter or mark the contract complete.
Fixed acceptance tests verify the observable contract and remain learner-owned. Do not implement behavior in the starter or weaken assertions. Until the named learner seam is completed, the learner selector is expected to fail at that explicit TODO. Instructor infrastructure checks are separate and should pass. Record the real command, exit code, output, TODO reached and learner-owned paths in `evidence/continuation-57-70/day-57.json`. A green infrastructure harness does not prove learner behavior.

**Acceptance test:** `src/test/java/assignments/local/LocalReadinessGateAcceptanceTest.java`
**Acceptance selector:** `day57MissingOrFailedPriorEvidenceReplacesNewWork`
**Learner acceptance command:** `./mvnw -Plearner-tests -Dtest=LocalReadinessGateAcceptanceTest#day57MissingOrFailedPriorEvidenceReplacesNewWork test`
**Instructor infrastructure test:** `src/test/java/assignments/local/LocalApplicationManifestInfrastructureTest.java`
**Instructor command:** `./mvnw -Dtest=LocalApplicationManifestInfrastructureTest test`
## Learner-first task proposal (5 minutes within the 85-minute practice block)

Before opening the readiness implementation or a worked answer, predict the outcomes for missing day-55, missing day-56, false/null flags, both true, and unrelated true keys. State what flags can and cannot prove about evidence authenticity.

Write the prediction before opening references, worked examples, hints, or AI. Spend the remaining 80 minutes on the existing acceptance only; this proposal is not completion evidence. Keep any unmet acceptance open as **S5-D57-READINESS**.

## Selector limits and open evidence

- The fixed selector checks false day-55 with day-56 true, missing day-55 with day-56 true, and both true only. It does not cover empty input, null required values, unrelated true keys, or both missing directions; those required cases remain open.

## Implementation/testing allocation (85 minutes)

Work only this day's seam. Reuse existing issuance, policy readback, eligibility, snapshot, idempotency and preview rules. Do not duplicate a domain rule or modify prior contracts. Use the fixed synthetic fixture, configured `Clock`, loopback local app and disposable PostgreSQL only where specified. No real identities, PII, passwords, OAuth, secrets, external services or persistent access grants. On a recovery day, replace planned work inside the existing 85 minutes; do not add overtime.

## Concept/course allocation (25 minutes)

**Learning guidance (inside the single 25-minute engineering study allocation):**
- Course: [Learn DevOps: Docker, Kubernetes, Terraform and Azure DevOps](https://www.udemy.com/course/devops-with-docker-kubernetes-and-azure-devops/) — in28Minutes Official; access: unconfirmed reference only. Do not purchase/enroll based on this map.
- Exact lesson title: **Getting Started with Continuous Integration, Deployment and Delivery**; exact section title: **Getting Started with Continuous Integration, Deployment and Delivery** (4:50).
- Fit: partial foundation. This short lesson is CI/CD vocabulary only. Use the supplied GitHub Actions workflow walkthrough for the actual local gate; do not set up Azure DevOps, deploy, or change infrastructure.
- Focused worked references: [GitHub Actions workflow syntax — workflow and job structure](https://docs.github.com/en/actions/writing-workflows/workflow-syntax-for-github-actions).
- Video viewing: 4:50 at 1×; the remaining 20:10 in the 25-minute cap is for pauses, the worked example, focused reference and teach-back. Stop at 25 minutes; carry unfinished material forward and shrink implementation scope. Viewing is not learner completion.

**Prediction record:** Complete the task-specific proposal above before guidance; do not add a post-hoc architecture essay.

**Teach-back:** Explain the invariant, its enforcement point and the observable evidence that proves it.

## Evidence allocation (10 minutes)

Record command, exact result, teach-back/design note and learner-owned changed paths in `evidence/continuation-57-70/day-57.json`. Leave completion and actual dates blank until the work is actually done.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
