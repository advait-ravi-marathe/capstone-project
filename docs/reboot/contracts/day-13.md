# Day 13 — 2026-11-07: Add honest repeatable CI

**State:** Instructor-owned fail-closed GitHub Actions workflow and its local infrastructure check are installed. The learner-owned readiness evaluator remains a TODO; no learner attempt is recorded. Remote Actions success is unverified until a run is observed.

**Course lesson:** Conditional reference to [Learn DevOps: Docker, Kubernetes, Terraform and Azure DevOps — in28Minutes Official](https://www.udemy.com/course/devops-with-docker-kubernetes-and-azure-devops/), “Getting Started with Continuous Integration, Deployment and Delivery”: “Getting Started with Continuous Integration, Deployment and Delivery” (4:50). Pair the concept lesson with the existing GitHub Actions failure walkthrough inside the 25-minute limit. Do not install Azure DevOps or Jenkins. Access is unconfirmed; no purchase/enrollment. See the [Days 1–30 map](../course-learning-map.md). Worked guide: [course-learning-supplements.md](../course-learning-supplements.md#day-13--trace-real-workflow-logs-and-readiness-evidence).

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

**Assistant-provided architecture decision (inside the existing 25-minute concept allocation):** [Keep CI as the explicit repository checkout, Java setup, and Maven verification sequence. Propagate any nonzero test result so a broken run cannot appear green.](../assistant-architecture-notes.md#day-13-fail-closed-ci-workflow). No capstone design proposal or post-hoc architecture justification is assigned.

Test pyramid, reproducibility, CI failure signals [I]. Within this same 25-minute block, trace `.github/workflows/coverstory-starter.yml`: push/pull-request trigger → Ubuntu job → Java 21 and Node setup → Maven infrastructure command → `npm ci`, build, instructor tests, and audit. Each nonzero step fails the job. Distinguish this instructor check from the learner readiness TODO; a local green run is not a remote Actions result.

## Evidence (10 minutes)

Actual local/CI-equivalent output; do not claim remote run if absent. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, assistance, and any blocker. Record observed results only; no architecture proposal or retrospective justification is required.

## Planned isolated command

`./mvnw -Dtest=Day13WorkflowInfrastructureTest test`

`./mvnw -Plearner-tests -Dtest=Day13ContractTest test`

The first selector checks the supplied workflow and fail-closed commands; it passes locally. The second runs the learner readiness-policy acceptance tests and is expected red at the explicit TODO. The remote Actions result is not claimed until observed; local evidence and remote CI are separate.

## Capstone implementation guidance

Use the assistant-provided architecture note linked in the concept block. Implement and test the stated decision within the existing acceptance and practice allocation; no pre-work design proposal is requested.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
