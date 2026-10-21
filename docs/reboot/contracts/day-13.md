# Day 13 — 2026-11-01: Add honest repeatable CI

**State:** Instructor-owned fail-closed GitHub Actions workflow and its local infrastructure check are installed. The learner-owned readiness evaluator remains a TODO; no learner attempt is recorded. Remote Actions success is unverified until a run is observed.

**Course lesson:** Conditional reference to [Learn DevOps: Docker, Kubernetes, Terraform and Azure DevOps — in28Minutes Official](https://www.udemy.com/course/devops-with-docker-kubernetes-and-azure-devops/), “Getting Started with Continuous Integration, Deployment and Delivery”: “Getting Started with Continuous Integration, Deployment and Delivery” (4:50). Pair the concept lesson with the existing GitHub Actions failure walkthrough inside the 25-minute limit. Do not install Azure DevOps or Jenkins. Access is unconfirmed; no purchase/enrollment. See the [Days 1–30 map](../course-learning-map.md). Worked guide: [course-learning-supplements.md](../course-learning-supplements.md#day-13--ci-concept-to-this-repository).

## Learner seam

`CiReadinessPolicy` for the evidence-checking part of CI readiness. The instructor supplies `.github/workflows/coverstory-starter.yml`, which runs the Java 21 infrastructure suite, frontend install/build/instructor tests, and frontend audit with failures propagated. Learner work is the evidence-based readiness evaluation and recording truthful local/remote results; a workflow file alone does not establish a successful remote run.

## Acceptance invariants

Java 21 compile, infrastructure tests and explicitly selected verified contracts run from clean checkout; failures propagate nonzero; unfinished contracts remain pending, not green. No deploy credentials.

## Required edge cases

Clean-checkout commands; deliberate test failure proves nonzero propagation; manifest not calendar-derived.

## Prerequisite and isolation

Days 1–12 status manifest and fixture smoke. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**Readiness question (inside the existing evidence slot):** What could a green infrastructure build still fail to establish? Trace a real workflow log step separately from the local readiness record; neither a date nor a local pass creates remote CI evidence.

## Concept (25 minutes)

Test pyramid, reproducibility, CI failure signals [I]. Within this same 25-minute block, trace `.github/workflows/coverstory-starter.yml`: push/pull-request trigger → Ubuntu job → Java 21 and Node setup → Maven infrastructure command → `npm ci`, build, instructor tests, and audit. Each nonzero step fails the job. Distinguish this instructor check from the learner readiness TODO; a local green run is not a remote Actions result.

## Evidence (10 minutes)

Actual local/CI-equivalent output; do not claim remote run if absent. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Dtest=Day13WorkflowInfrastructureTest test`

`./mvnw -Plearner-tests -Dtest=Day13ContractTest test`

The first selector checks the supplied workflow and fail-closed commands; it passes locally. The second runs the learner readiness-policy acceptance tests and is expected red at the explicit TODO. The remote Actions result is not claimed until observed; local evidence and remote CI are separate.

## Separate Java DSA block (60 minutes)

**New pattern anchor:** LeetCode 167 Two Sum II - Input Array Is Sorted — sorted two pointers. Learn the pattern for 25 minutes, solve independently for 20 minutes, and spend 15 minutes on same-day D recall plus the error log. Record the actual first-attempt date; schedule D+3, D+7 and D+21 reviews from that date. Planned dates in the manifest are forecasts only. A failed prerequisite/review displaces the next new anchor; do not add overtime. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** How can a green build lie? What proves learner logic rather than fixture wiring?

**Observable evidence:** Distinguish infrastructure, isolated contract and integrated tests; show that an intentional contract failure exits nonzero and unfinished work stays visible.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [Official expectations](../interview-evidence-overlay.md#official-expectations) (official). These references guide practice; they do not establish question frequency or predict interview content.
