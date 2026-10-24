# Day 57 — 2026-12-19: Gate the local assembly on prior learner evidence

## Contract

**Learner-owned seam:** `LocalReadinessGate.assess`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

A readiness check accepts only actual evidence for required prior milestones. If anything is missing or failed, today becomes a recovery block; it does not add work or imply completion.

## Acceptance and expected TODO state

**Acceptance harness:** learner-owned bounded contract

Checks empty/null inputs, both missing directions, false/null required evidence, unrelated true flags, both required flags, and preserves inputs. The learner-owned gate remains TODO-red until implemented; a flag map does not authenticate evidence.

**Acceptance selector:** `day57MissingOrFailedPriorEvidenceReplacesNewWork`
**Acceptance source:** `src/test/java/assignments/local/LocalReadinessGateAcceptanceTest.java`
**Command:**

```sh
./mvnw -Plearner-tests -Dtest=LocalReadinessGateAcceptanceTest#day57MissingOrFailedPriorEvidenceReplacesNewWork test
```

The selector is intentionally expected to fail at this day’s unchanged learner TODO until implementation evidence exists. This prospective harness is not completion credit.

## Learner-first task proposal (5 minutes within the 85-minute practice block)

Before opening the readiness implementation or a worked answer, predict the outcomes for missing day-55, missing day-56, false/null flags, both true, and unrelated true keys. State what flags can and cannot prove about evidence authenticity.

Write the prediction before opening references, worked examples, hints, or AI. Spend the remaining 80 minutes on the existing acceptance only; this proposal is not completion evidence. Keep any unmet acceptance open as **S5-D57-READINESS**.

## Selector limits and open evidence

Checks empty/null inputs, both missing directions, false/null required evidence, unrelated true flags, both required flags, and preserves inputs. The learner-owned gate remains TODO-red until implemented; a flag map does not authenticate evidence. Any learner-owned behavior not implemented yet remains explicitly incomplete; these assertions only make its first failure observable.

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
