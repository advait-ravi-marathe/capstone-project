**Active cloud/certification scope: AWS only.**

# Later milestone contracts — interview-first reflow

**Contract-only, not implemented or learned.** These 12 milestones follow the first-release AI units; they have capability prerequisites, not fixed day completion. Total later AI unit work is 327 hours (excluding weekly visibility), after transferring 20 hours of formal interview assessment into the recurring role-preparation bank. Default daily cap remains 60 DSA + 30 roleprep + 90 AI = 180. Missing live/cloud permission does not stop independent learning or pass the missing gate.

## AIFDE-V2-M2.1: Reliable retrieval and evaluation
AI work 29 h = 25 h substantive + 4 h buffer, excluding globally scheduled weekly visibility. Formal interview assessment from existing role bank: 0 h.

**Prerequisites:**
- P1: actual generation/embeddings/vector retrieval and learner ownership

**Deliverables:**
- Versioned ingestion, upsert, deletion and provenance behavior; data-quality tests.
- Keyword/vector comparison; hybrid/reranking only if a measured failure justifies it.
- Labeled evaluation set targeting at least 100 total cases with documented source, development/held-out split, answerability and adversarial categories.
- Applied Python/SQL/statistics/ML exercises linked to retrieval metrics, leakage, baselines and error analysis.

**Acceptance:**
- Run an unchanged baseline and one proposed change on the same development cases; report exact counts and regression/failure taxonomy.
- Show that deletion/version updates remove obsolete evidence from retrieval.
- Held-out release evaluation is separate from tuning; dataset size alone is not a quality pass.
- Before held-out release scoring, predeclare and justify a measurable useful-workflow criterion (grounded-answer correctness, abstention, unacceptable errors and latency as relevant) against the non-AI baseline. Freeze config/data, score the held-out set and require the criterion to pass; a large dataset or an improvement alone is not a release pass.

**Permissions / limits:**
- No mandatory paid service; local real model/vector capacity still required.

**Evidence:**
- Dataset manifest/rubric, retrieval/data tests, per-case eval results, failure analysis and learner explanation.

## AIFDE-V2-M2.2: Security, identity and deployment preparation
AI work 29 h = 25 h substantive + 4 h buffer, excluding globally scheduled weekly visibility. Formal interview assessment from existing role bank: 0 h.

**Prerequisites:**
- M2.1: working data and evaluation baseline

**Deliverables:**
- Actual authentication and server-side document/tenant access enforcement using an appropriate established mechanism.
- Threat model, secrets/config separation, retention/deletion, input/output boundaries and audit trail.
- Containerized service and reproducible CI/CD preparation, trace propagation, bounded retries/deadlines and performance/cost budget.
- Incremental practical cloud/course gaps only: use C0 first 30-day diagnostic evidence to select AWS project labs; do not charge the initial audit twice.

**Acceptance:**
- Cross-user/tenant access tests fail closed at the server and retrieval boundary, not only in the UI/prompt.
- Clean build/tests and configuration work without embedding secrets; prove logs do not expose protected content by default.
- Explain selected AWS identity/network/storage/compute choices and remaining real-deployment steps.

**Permissions / limits:**
- Local identity/security tests can proceed without paid cloud; no live-cloud credit yet.

**Evidence:**
- Threat model, auth/isolation/security test receipts, reproducible build, tracing sample and cloud diagnostic gap map.

## AIFDE-V2-M2.3: Real AWS and hosted-model delivery
AI work 29 h = 24 h substantive + 5 h buffer, excluding globally scheduled weekly visibility. Formal interview assessment from existing role bank: 0 h.

**Prerequisites:**
- M2.2: safe application and deployment preparation

**Deliverables:**
- User-authorized AWS account/region/services with spend ceiling, alerts/limits and teardown plan.
- Actual cloud application deployment with real identity, network, storage, configuration and logging controls; IaC or reproducible equivalent.
- At least one authorized real AWS-hosted model integration (AWS Bedrock or an approved real AWS model sandbox) with actual request/result, model/config, exposed usage and honest cost methodology.
- Redeploy, rollback/restore, teardown/recreate evidence and one failure/incident drill.

**Acceptance:**
- Read back the actual resources/configuration and live request/trace; local containers, emulators or Terraform plan alone do not pass.
- Observe a real successful hosted-model request and bounded error/deadline behavior; use safely injected tests for rare failures rather than deliberately exhausting quotas.
- Show observed usage/billing where available; mark unavailable fields instead of fabricating values.
- Cloud and hosted-provider capability gates remain blocked separately if their permissions/budgets are unavailable.

**Permissions / limits:**
- Cloud and hosted inference require explicit account/service/cost authorization.
- A real vendor sandbox can supply only the controls it actually exposes; record provisioned-lab limitations.
- Independent tools/eval/DSA learning continues while external access is blocked.
- AWS budget alerts are not a guaranteed hard spending cap. Bound workloads/tokens/requests, apply available quotas, short resource lifetimes and teardown, and obtain approval acknowledging residual billing risk before provisioning.

**Evidence:**
- Authorized-resource inventory, deployment/rollback/teardown receipts, live trace, hosted-provider receipt, usage/cost evidence and incident note.

## AIFDE-V2-M3.1: Bounded developer workflow
AI work 29 h = 25 h substantive + 4 h buffer, excluding globally scheduled weekly visibility. Formal interview assessment from existing role bank: 0 h.

**Prerequisites:**
- P1: actual RAG/ownership
- M2.1: evaluation baseline
- M2.2: local security and input boundaries

**Not a blocker to independent work:**
- M2.3: live cloud and hosted provider are not required merely to start local tools work

**Deliverables:**
- Small developer assistant that reads an allowed scratch repository and test log, forms a bounded plan and proposes a reviewable patch.
- Compare deterministic workflow versus agentic planner; use the simpler design when it performs adequately.
- Actual allowlisted tool execution with typed arguments, scope/resource limits, cancellation and trace IDs.

**Acceptance:**
- A real tool reads actual allowed repository content; no canned-output-only proof.
- Attempts to read outside scope or execute an unapproved action are denied by code.
- Explain one agent failure from an actual trace and the chosen deterministic alternative/fallback.

**Permissions / limits:**
- No external repository write or arbitrary terminal authority is implied.
- Local model capability may be insufficient for planning quality; record that blocker honestly.

**Evidence:**
- Workflow implementation, task set, actual tool trace, baseline comparison, scope/cancellation tests and limitations.

## AIFDE-V2-M3.2: Genuine MCP and exact-action approval
AI work 28.5 h = 24.5 h substantive + 4 h buffer, excluding globally scheduled weekly visibility. Formal interview assessment from existing role bank: 0 h.

**Prerequisites:**
- M3.1: actual bounded tools

**Deliverables:**
- An actual MCP server and client connection using the current official protocol/SDK documentation.
- Tool schemas, allowed resources, error handling and one meaningful controlled local mutation such as writing a patch proposal into an owned scratch workspace.
- Explicit user approval bound to exact action/arguments/target, invalidated on changes; audit and replay protection appropriate to the scope.

**Acceptance:**
- Record a real protocol client/server tool exchange, not merely JSON shaped like a tool call.
- Changed, expired, replayed or missing approval cannot authorize a write.
- Model output and retrieved tool results remain untrusted; no prompt can expand filesystem/network authority.

**Permissions / limits:**
- Local stdio/server integration is valid protocol evidence, not a claim of remote enterprise OAuth.
- Any real external write still requires explicit separate authorization.

**Evidence:**
- Protocol/version receipt, server/client tests, approval-denial tests, real controlled-write receipt and audit trace.

## AIFDE-V2-M3.3: Agent evaluation and operational safety
AI work 29 h = 24 h substantive + 5 h buffer, excluding globally scheduled weekly visibility. Formal interview assessment from existing role bank: 0 h.

**Prerequisites:**
- M3.1: bounded workflow
- M3.2: actual MCP and approval

**Deliverables:**
- Task-success evaluation with development/held-out tasks, tool/planning/retrieval/authorization failure classes and repeated-action cases.
- Injection/exfiltration tests with synthetic data, limits/timeouts, idempotency where applicable and human escalation.
- Runbook and authorized deployment/operation of the scoped assistant when cloud permission exists; otherwise preserve the deployment blocker.

**Acceptance:**
- Show actual task outcome traces and a targeted regression improvement, not just an agent demo.
- No unapproved write, scope escape or secret access in deterministic boundary tests.
- A human can inspect the proposed action, deny it and recover from a partial failure.
- Predeclare a scoped task-success criterion and zero-tolerance deterministic authorization failures before release evaluation; freeze the version and score a held-out task set. A largely nonfunctional agent does not pass merely because traces exist or one metric improved.

**Permissions / limits:**
- Local evaluation proceeds independently; real deployment remains blocked/unpassed without authorized live access.

**Evidence:**
- Task dataset, eval report/traces, safety tests, incident/recovery exercise and deployment status with exact limits.

## AIFDE-V2-M4.1: Customer discovery and pilot contract
AI work 29 h = 25 h substantive + 4 h buffer, excluding globally scheduled weekly visibility. Formal interview assessment from existing role bank: 0 h.

**Prerequisites:**
- P1: problem brief and honest scope
- At least one functioning application with known limitations

**Not a blocker to independent work:**
- Cloud completion is not required to conduct discovery; use only an honest available demo

**Deliverables:**
- Discovery conversation or clearly labeled role-play covering workflow, stakeholders, data/access, non-AI baseline, risks and success measures.
- Small pilot scope, non-goals, responsibility map, acceptance/rollback criteria and a customer-facing explanation.
- Seek an authorized real reviewer/user opportunity; no automatic outreach or data sharing.

**Acceptance:**
- Questions precede solution design and can lead to a non-AI or no-go decision.
- Real-user, peer and role-play evidence are labeled distinctly.
- No adoption, business savings, customer identity or production history is fabricated.

**Permissions / limits:**
- Real external contact/data access needs the applicable user authorization.
- If no real user is available, simulated discovery can pass the exercise but not a real-customer-experience gate.

**Evidence:**
- Problem/pilot brief, discovery notes, data-access constraints, proposed baseline and truthful evidence classification.

## AIFDE-V2-M4.2: Change, operate and recover
AI work 29 h = 25 h substantive + 4 h buffer, excluding globally scheduled weekly visibility. Formal interview assessment from existing role bank: 0 h.

**Prerequisites:**
- M4.1: pilot/change scope
- Working local app and security fundamentals

**Not a blocker to independent work:**
- Live cloud operations require M2.3; local drills can proceed separately

**Deliverables:**
- Feedback-driven change request with data/API compatibility and rollback concerns.
- Quality/availability/latency/cost observation, trace-based incident diagnosis and recovery.
- Retention/deletion verification, runbook updates and cloud/certification gap study tied to real operational decisions.

**Acceptance:**
- A changed requirement produces a minimal tested patch and explicit risk/rollback record.
- An incident drill has evidence of detection, diagnosis, mitigation, recovery and prevention; label drill versus real incident.
- Do not claim sustained production reliability from a short benchmark or local drill.

**Permissions / limits:**
- Actual cloud/pilot evidence requires the relevant access gates; missing scope is retained.

**Evidence:**
- Change diff/tests, incident timeline, operational observations, runbook and updated risk/evidence map.

## AIFDE-V2-M4.3: Independent integration and handoff
AI work 29 h = 24 h substantive + 5 h buffer, excluding globally scheduled weekly visibility. Formal interview assessment from existing role bank: 0 h.

**Prerequisites:**
- Working app with reproducible setup/runbook
- M4.1: stakeholder/scope explanation

**Deliverables:**
- Two unfamiliar-repository debugging/integration exercises using original lawful tasks.
- Clean-clone handoff/reproduction by a peer where available; otherwise accurately labeled self-run verification.
- Five-minute technical demo with failure path, architecture Q&A and nontechnical value explanation.

**Acceptance:**
- Independently reproduce a defect, identify root cause, patch minimally, add regression coverage and explain blast radius.
- Separate personal implementation from borrowed/tutorial/assistant work.
- Document real reviewer feedback and one response to it if a reviewer is available; no invented independent review.

**Permissions / limits:**
- A human review is preferred, not assumed or forged; obtain appropriate sharing authorization.

**Evidence:**
- Two attempt receipts/rubrics, patch/tests, clean-run result, handoff feedback/status and demo/decision log.

## AIFDE-V2-M5.1: Unseen interview gates and targeted repair
AI work 9 h = 5 h substantive + 4 h buffer, excluding globally scheduled weekly visibility. Formal interview assessment from existing role bank: 20 h.

**Prerequisites:**
- Baseline attempts and available project evidence

**Not a blocker to independent work:**
- Applications/practice need not wait for every project or cloud permission

**Deliverables:**
- Six gates: independent coding/DSA; unfamiliar repository; system design; AI evaluation/delivery; discovery/ambiguity; behavioral/project explanation.
- Use two unseen original exercises on separate days for each critical gate, honoring tool/AI allowances.
- DSA coding mocks replace DSA blocks. The 20 hours of non-DSA formal assessment accrue across existing role-bank windows throughout the core, excluding AWS sessions—not 20 hours compressed into P5 or additionally charged to AI.

**Acceptance:**
- Proposed rehearsal threshold: at least 3/4 in critical dimensions on both attempts, no critical correctness/safety/integrity failure; this is not an employer cutoff.
- Record help/tool allowances and reviewer type; prefer a human/peer review without claiming one occurred.
- Failed dimensions create bounded repair work; passing topics does not create professional tenure or Staff impact.

**Permissions / limits:**
- An integrated mock spans two existing engineering blocks, not added hours.

**Evidence:**
- Attempt dates/artifacts, dimension rubrics, actual feedback and remediation/retest plan.

## AIFDE-V2-M5.2: Small StreamForge ranking depth sample
AI work 29 h = 25 h substantive + 4 h buffer, excluding globally scheduled weekly visibility. Formal interview assessment from existing role bank: 0 h.

**Prerequisites:**
- Applied Python/SQL/metrics and train-validation-test foundations

**Deliverables:**
- Lawful public/synthetic dataset, documented splits/leakage checks and a popularity/simple baseline.
- One personally implemented simple ranker/recommender and an offline comparison using justified ranking metrics.
- Serve predictions through a small API and explain reproducibility, monitoring/drift and future deployment limits.

**Acceptance:**
- Report actual dataset/split/metric definitions and held-out results without tuning the test set.
- Show own code/understanding of the baseline/model/metric and analyze one failure.
- No fabricated online conversion/A-B uplift; full third flagship is optional and not an application prerequisite.

**Permissions / limits:**
- GPU, paid compute and large-model training are not prerequisites for this sample.

**Evidence:**
- Data/model manifest, learner implementation/tests, evaluation, served prediction receipt and limitations.

## AIFDE-V2-M5.3: Role-fit evidence and conditional certification decision
AI work 28.5 h = 23.5 h substantive + 5 h buffer, excluding globally scheduled weekly visibility. Formal interview assessment from existing role bank: 0 h.

**Prerequisites:**
- Available actual evidence from prior units; unpassed gates remain visible

**Deliverables:**
- One strong end-to-end case study plus a bounded-tools/MCP case, accurate ownership/results/limits and reproducibility.
- Role-by-role evidence map including experience, location/work authorization/travel and customer-production gaps.
- Current AWS syllabus/practical readiness review; SAA exam only if useful, independently ready and separately authorized. AIP remains a later experience/readiness target, not automatic core completion completion.

**Acceptance:**
- Every claim links to actual evidence; no scaffold/elapsed day/credential study is counted as learner success.
- Mark local, hosted-provider, live-cloud, real-user, production and organizational-impact levels separately.
- Public posting, applications, outreach and exam spending occur only with the applicable user instruction/approval.
- If required capability gates remain blocked, publish an honest partial-readiness/gap report, not universal FDE-ready status.

**Permissions / limits:**
- Paid exams/cloud/labs are not authorized by this contract.
- Theapproximately 270-day capacity estimate can extend when prerequisites, DSA debits or failed gates consume the buffer.

**Evidence:**
- Case-study drafts, role-fit matrix, final capability audit, certification decision and remaining blockers.

Long uninterrupted mocks require explicit neighboring-block rebalancing within 180 minutes/day or AI carryover. M5.1 consolidates assessment evidence accumulated earlier, then repairs remaining gaps.
