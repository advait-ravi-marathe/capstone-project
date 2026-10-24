# Proposed continuation: Days 197–270

**Status: review proposal only.** This document does not activate Days 197+, create daily contracts, alter the EAI board, or claim learner completion. It is a planning bridge for later approval. The current continuation branch is `work/eai-sprint2-onward`; audited base `c4f84d49630bfa96ef816c529556cd2367b7148b`. No paused day-level 197–210 work was found: the ref `learning/streamforge-days-197-210` points to the same StreamForge Days 183–196 line and contains no 197–210 contracts or transfer plan. Preserve that ref and existing files.

## Guardrails carried forward

- CoverStory remains the primary applied project. The learner implements and tests assistant-supplied, source-accurate architecture decisions; capstone design proposals and post-hoc architecture essays are not assigned. System-design practice remains in the separate interview question bank.
- StreamForge stays conditional. The Day 153/154 evidence files exist but currently record `not_attempted` / `not_run`; a future gate evaluation must use their actual contents. Planning future work does not assert that the gate has passed.
- No real customer, personal data, public deployment, AWS resource, paid exam, credential, or external integration is authorized here. Use synthetic fixtures and offline/local adapters by default.
- A retrieval or agent workflow is justified only if a bounded user/operator task needs source-grounded retrieval or a constrained multi-step tool call. Otherwise use a simpler deterministic workflow. No consequential underwriting, claims, or eligibility decision is automated.
- Professional production ownership, customer delivery, mentoring, and Staff-level influence require real external evidence. Solo portfolio work can demonstrate technical knowledge and reproducibility, not those professional claims.

## Capacity and proposed allocation

Days 197–270 are 74 days: five complete 14-day windows (197–266) and a four-day closeout (267–270).

| Allocation | Per day | 74-day total | Use |
|---|---:|---:|---|
| Engineering | 165 min | 12,210 min / 203.5 h | 35 study, 115 practice/tests, 15 evidence, subject to each approved day's actual split |
| Shared interview/review | 45 min | 3,330 min / 55.5 h | One block total for question practice, new questions, or unfinished review; never duplicated |
| Combined | 210 min | 15,540 min / 259 h | Excludes any new DSA assignment; DSA history and review clock remain in `dsa-prep` |

These are ceilings, not a promise that all proposed scope fits. The engineering allocation assumes the established 35-minute study / 115-minute practice-and-test / 15-minute evidence shape. Across the 74 days that yields 43.17 study hours, 141.83 practice-and-test hours and 18.5 evidence hours. A failed prerequisite consumes its bounded practice slice for recovery and moves unfinished work to the next named window; it does not create overtime.

The table divides every engineering minute into one exclusive bucket. Certification practice is deliberately small and cannot establish readiness by itself. Certification study, AI concepts and core systems study share the 35-minute study budget. The separate shared block is also allocated once; it is not extra engineering time.

| Window | Project implementation/tests | AI workflow implementation/evaluation | Certification labs/questions | AI concept/source study | Certification domain study | Core systems/security/cloud study | Evidence | Engineering total |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| 197–210 (14 days) | 23.00 h | 3.00 h | 0.83 h | 1.00 h | 1.50 h | 5.67 h | 3.50 h | **38.50 h** |
| 211–224 (14 days) | 22.00 h | 4.00 h | 0.83 h | 2.00 h | 1.50 h | 4.67 h | 3.50 h | **38.50 h** |
| 225–238 (14 days) | 19.00 h | 6.00 h | 1.83 h | 2.50 h | 2.00 h | 3.67 h | 3.50 h | **38.50 h** |
| 239–252 (14 days) | 14.00 h | 11.00 h | 1.83 h | 3.50 h | 2.50 h | 2.17 h | 3.50 h | **38.50 h** |
| 253–266 (14 days) | 10.00 h | 15.00 h | 1.83 h | 3.50 h | 2.50 h | 2.17 h | 3.50 h | **38.50 h** |
| 267–270 (4 days) | 2.50 h | 3.00 h | 2.17 h | 1.50 h | 0.50 h | 0.33 h | 1.00 h | **11.00 h** |
| **Total** | **90.50 h** | **42.00 h** | **9.32 h** | **14.00 h** | **10.50 h** | **18.68 h** | **18.50 h** | **203.50 h** |

Window allocations are displayed to 0.01 hour; the total is calculated from the exact minute budget, so displayed component rounding can differ by a few hundredths.

The 55.5 shared hours are provisionally divided into **18 hours** for 24 × 45-minute interview-question sessions (about 2.3 sessions per week), **18 hours** for certification retrieval/practice against the selected official guides, and **19.5 hours** for unfinished engineering review and question-bank refinement. If prerequisite recovery needs the latter, it displaces certification review; no extra block is created. Across engineering and shared time, the certification allocation is at most 37.82 hours, before any such displacement. A provisional split is 19.00 hours for SAA-C03 and 18.82 hours for Developer Associate, including each credential's portion of the shared 18 hours. This is a bounded start on two credentials, not a credible guarantee of two passes. If either readiness threshold is unmet, that credential remains in the post-Day-270 queue.

## Proposed windows and evidence gates

The following are sprint-level intents, not activated daily assignments. Daily scope, exact selectors and course rows should be written only after inspecting the accepted state at the preceding gate.

| Days / planned dates | Proposed focus and bounded runnable proof | Gate, fallback and exit evidence |
|---|---|---|
| **197–210 · 8–21 May 2027** | Project/tests 23 h; AI practice 3 h; certification practice 0.83 h; study 8.17 h; evidence 3.5 h. Re-establish CoverStory readiness from actual evidence. The one selected workflow is a **source-grounded explanation of an already-issued synthetic policy**: authorize owner first, retrieve only versioned approved clause snippets, return cited explanation or abstain. Start with deterministic retrieval and evaluator fixtures; no model call is needed to test the contract. | If prior data/privacy/evaluation gates are unmet, project time recovers the first named CoverStory blocker; AI practice is reduced to offline fixture/evaluator design, not a live retrieval service or StreamForge activation. Exit with actual gate output, the assistant-supplied architecture boundary, baseline citation/abstention results and unresolved risks. |
| **211–224 · 22 May–4 Jun 2027** | Project/tests 22 h; AI practice 4 h; certification practice 0.83 h; study 8.17 h; evidence 3.5 h. Implement validated synthetic clause ingestion, stable source/version IDs, bounded retrieval, owner filter before retrieval, citations, and explicit unsupported-question fallback. | Requires actual authorization and data boundary from the first window. Otherwise use the AI practice slice to add adversarial fixtures to the deterministic evaluator and use project time on prior CoverStory recovery. Exit with independent expected source IDs, foreign-owner denial tests and reproducible full-state snapshots. |
| **225–238 · 5–18 Jun 2027** | Project/tests 19 h; AI practice 6 h; certification practice 1.83 h; study 8.17 h; evidence 3.5 h. Add a local draft-explanation workflow with explicit `DRAFT → APPROVED` human review before the explanation is exposed outside the reference-only workspace; approval is version-bound, owner-scoped, idempotent and audited. The model cannot alter policy state. | If approval/audit cannot be represented safely in the existing bounded seam, keep output reference-only and defer the write path. Exit with deny/allow/replay/conflict tests and proof that generation alone cannot approve or mutate policy. |
| **239–252 · 19 Jun–2 Jul 2027** | Project/tests 14 h; AI practice 11 h; certification practice 1.83 h; study 8.17 h; evidence 3.5 h. Expand fixed evaluation for source citation, abstention, owner leakage, malicious instructions in retrieved documents, latency, token/call cost and failure fallback. Add bounded timeout/retry and redacted trace correlation using a fake provider by default. | If model-provider access is not separately authorized, keep the full path local/deterministic and test the adapter with controlled responses/failures. Do not use an LLM judge as ground truth. Exit with versioned fixture, metric definitions, paired results and known blind spots. |
| **253–266 · 3–16 Jul 2027** | Project/tests 10 h; AI practice 15 h; certification practice 1.83 h; study 8.17 h; evidence 3.5 h. Choose **one** bounded branch: (A) rehearse one distinct *synthetic* enterprise document/identity contract behind an adapter, or (B) continue an already-eligible StreamForge seam. Branch B requires actual Day 153/154, 168, 182 and 196 learner evidence to satisfy existing gates and completion of prior StreamForge seams. Never stack both branches into the window. | CoverStory remains primary: if its AI/security/operational controls need recovery, use the window there. Branch A is a simulation, not a second customer delivery; branch B remains conditional. Exit with the selected branch's exact tests, failure/recovery observations and an honest demo boundary. |
| **267–270 · 17–20 Jul 2027** | Project/tests 2.5 h; AI practice 3 h; certification practice 2.17 h; study 2.33 h; evidence 1 h. Re-run only accepted selectors, assemble reproducible artifact/demo instructions, review certification diagnostics, and choose the next queue. | Do not broaden features. If any evidence gate or exam-readiness threshold is unmet, state it and continue that item after Day 270. Separate pass/fail/blocked/not-run, scaffold and learner-owned evidence. |

### Example task-sized proof patterns

These examples describe the scale of later assignments, not additional features to stack on one day:

- Java/Spring: a pure retrieval-result validator accepts only known source IDs and bounded snippets; tests cover missing, stale, foreign-owner and malformed citations.
- PostgreSQL: a disposable fixture associates corpus documents and permissions with owners; tests prove foreign documents never enter the retrieval request and reads leave full state unchanged.
- Python evaluation: fixed synthetic questions and expected source IDs produce reproducible citation/abstention metrics; adversarial cases are separate from ordinary quality scores.
- React: an accessible reference-only answer panel renders cited sources and an explicit “not enough evidence” state; tests verify no hidden action is submitted from display state.
- Human-approved write: one explicit server action receives a stable request key; tests prove anonymous, unauthorized, unapproved, replayed and conflicting inputs are handled distinctly and do not create extra audit/domain rows.
- Operations: a fake-model/local adapter and bounded failure controls exercise timeout, unavailable provider, redacted trace and cost ceiling without credentials or cloud resources.

Each accepted day's test should prove only its assigned seam. A passing unit predicate is not packaged/browser/database evidence; a synthetic fixture is not a customer outcome. Release-oriented tests must state their artifact, database lifecycle, selector and exact source identity.

## Proposed certification pair, domain mapping and readiness gate

The confirmed target pair is **AWS Certified Solutions Architect – Associate (SAA-C03)** plus **AWS Certified Developer – Associate**. As checked on 24 October 2026, AWS says registration for Developer Associate DVA-C03 opens on 27 October and the current DVA-C02 exam retires on 30 November; the future plan must use the DVA-C03 guide once published, not invent its domains from DVA-C02. The official SAA-C03 domains are **Design Secure Architectures; Design Resilient Architectures; Design High-Performing Architectures; Design Cost-Optimized Architectures**. The current DVA-C02 reference domains are **Development with AWS Services; Security; Deployment; Troubleshooting and Optimization**, explicitly provisional until the new guide is available.

The allocation across the whole 74 days is **203.5 engineering hours plus 55.5 shared hours**, exactly the fixed budget. Of engineering time, the proposal assigns 90.5 hours to CoverStory/StreamForge implementation and tests; 56 hours to AI concepts, implementation and evaluation; 19.82 hours to certificate study/labs; 18.68 hours to core systems/security/cloud concepts; and 18.5 hours to evidence. The shared block assigns 18 hours to 24 interview-question sessions, 18 hours to certification review, and 19.5 hours to unfinished review/question refinement. Certification therefore has a maximum of **37.82 hours** (19.82 engineering +18 shared), not a promise of adequate preparation; project recovery can displace the shared certification review. Existing course metadata records *Ultimate AWS Certified Solutions Architect Associate 2026* as owned but unwatched; it does not establish Developer Associate course access. No new course title is presumed.

| Proposed credential | Study/domain review | Local or offline practice | Shared timed/retrieval review | Proposed total |
|---|---:|---:|---:|---:|
| SAA-C03 | 5.25 h | 4.50 h | 9.25 h | **19.00 h** |
| Developer Associate (current version to be rechecked) | 5.25 h | 4.82 h | 8.75 h | **18.82 h** |

SAA-C03 study follows its four official domains: secure, resilient, high-performing and cost-optimized architectures. The small offline practice portion can compare the CoverStory decisions against these domains; it is not hands-on AWS experience. Developer Associate uses the DVA-C03 guide once published; until then its provisional DVA-C02 guide has Development with AWS Services, Security, Deployment, and Troubleshooting and Optimization. The local equivalents exercise API/package/security/troubleshooting concepts only; they do not claim AWS service deployment. If the new guide changes scope, update this allocation and move unfinished topics beyond Day 270 rather than implying coverage.

**Proposed internal readiness threshold (not an AWS rule):** for each credential separately, after reading the current official exam guide, complete two separate timed assessments drawn from current official AWS practice material, including a full-length Official Practice Exam when available; score at least 80% on both, review every missed item, and have no exam-domain topic marked “not studied.” If an official domain diagnostic is available, no domain should remain below 70% on the final diagnostic. These are conservative planning thresholds, not equivalent to AWS scaled scores or guarantees of a pass. AWS reports a scaled score with a credential-specific passing standard. If two separate assessments or domain diagnostics are unavailable, record that limitation and do not claim readiness. No paid exam is scheduled. Any actual booking remains a separate user decision after readiness review and version/region confirmation.

Mapping: SAA study connects its four official domains to the synthetic workflow's owner/data boundary, failure/recovery, bounded retrieval/latency, and token/storage cost decisions. Developer study connects to application API/SDK concepts, least privilege, repeatable package/deployment concepts and troubleshooting; it does **not** imply that local tests prove cloud deployment. Cloud labs stay offline unless separately authorized. The owned SAA course can support gaps only after exact lesson titles and current catalog availability are checked; this proposal assigns no fabricated lecture.

## Learning sources to use when daily scope is approved

Primary documentation titles below were checked on **24 October 2026**. They support concepts; they do not imply a service is selected or that the learner watched anything.

- AWS, [*Retrieve data and generate AI responses with Amazon Bedrock Knowledge Bases*](https://docs.aws.amazon.com/bedrock/latest/userguide/knowledge-base.html): RAG, source retrieval and citations; use as an architectural comparison, not a required managed AWS deployment.
- AWS, [*Detect and filter harmful content by using Amazon Bedrock Guardrails*](https://docs.aws.amazon.com/bedrock/latest/userguide/guardrails.html): input/output controls and contextual grounding checks; these filters do not replace authorization or application-specific testing.
- OpenAI, [*Working with evals*](https://developers.openai.com/api/docs/guides/evals): evaluation dataset/workflow concepts. Use deterministic local expected results alongside any model-based grader.
- AWS, [*Evaluate agent performance with Amazon Bedrock AgentCore Evaluations*](https://docs.aws.amazon.com/bedrock-agentcore/latest/devguide/evaluations.html) and [*Observe your agent applications on Amazon Bedrock AgentCore Observability*](https://docs.aws.amazon.com/bedrock-agentcore/latest/devguide/observability.html): evaluation traces and operational signals. These current service docs are reference material only; no account, trace upload or spend is authorized.
- AWS, [*AWS Well-Architected Framework*](https://docs.aws.amazon.com/wellarchitected/latest/framework/welcome.html): trade-off review for security, reliability, performance and cost; use only when a later cloud decision is authorized.
- Certification scope: AWS [*Certified Solutions Architect – Associate*](https://aws.amazon.com/certification/certified-solutions-architect-associate/) and [*Certified Developer – Associate*](https://aws.amazon.com/certification/certified-developer-associate/) official pages and their currently linked exam guides. Version and registration information can change; verify again when scheduling.
- Exact guides checked 24 October 2026: [*AWS Certified Solutions Architect - Associate (SAA-C03)*](https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03.html) and current [*AWS Certified Developer - Associate (DVA-C02)*](https://docs.aws.amazon.com/aws-certification/latest/developer-associate-02/developer-associate-02.html); replace the latter mapping when AWS publishes DVA-C03.

No new Udemy lesson is assigned in this proposal because course catalog access/title metadata for post-Day-196 AI content was not verified. Select exact lesson titles and durations only in the later approved daily contracts; keep any course within the existing study minutes and substitute official docs where access is unavailable.

## Progressive application checkpoints

| Checkpoint | Evidence that would support an application comparison | What this plan cannot establish |
|---|---|---|
| **SDE II knowledge / application checkpoint** | A learner-owned bounded Java/Spring feature with API, relational data, React integration, meaningful tests, debugging notes and safe local operation. Compare these artifacts with current role postings; apply only if the actual requirements match. | Employment-level delivery history, peer review, on-call responsibility or production ownership. |
| **Senior / SDE III knowledge checkpoint** | End-to-end synthetic workflow with owner/security boundaries, replay and recovery behavior, latency/cost evaluation, observability and reproducible handover. | Production incident accountability, independent service ownership, mentoring, cross-team influence or customer outcomes. |

By Day 270 the plausible portfolio outcome is a reviewable, local/synthetic AI-assisted workflow with source/permission boundaries, evaluation cases, failure handling and honest operational measurements—*if* its entrance gates and task checks are actually satisfied. That can support applications targeting roles whose requirements match demonstrated backend, integration and AI-evaluation knowledge. It does not prove cloud production operations, customer discovery, adoption, business value, mentorship, or professional Staff/Principal influence.

Longer-term (the existing North Star's roughly three-year direction, not an automatic schedule): a genuinely authorized production model/provider integration with target-environment identity, privacy/security review, real SLO/latency/cost telemetry, incident ownership and supported rollback; customer/stakeholder discovery and measured outcomes; repeated ownership across more than one integration; design-review influence and mentorship; reusable platform impact measured by other teams adopting it. The 197–270 budget can target a local integrated workflow and production-readiness evidence, but actual production deployment/customer outcomes cannot be promised without explicit authorization, operational partners and suitable professional context. These require external workplace evidence and cannot be manufactured by extending this solo roadmap. Re-evaluate target postings at each application checkpoint and make no hiring or promotion promise.

## Review checklist before activation

1. Re-read actual learner evidence and evaluate gates from authoritative files; do not infer success from scaffold CI.
2. Convert only the next 14-day window into daily contracts, with exact source titles, executable selectors, bounded acceptance, prerequisites and named deferred work.
3. Check that each daily workload fits 165 engineering minutes plus one shared 45-minute block; keep DSA solely in its separate repository.
4. Require offline/local alternatives and explicit permission boundaries for every proposed cloud or live-model action.
5. Keep the proposal status until the user approves a window. Do not create tickets or learner-completion records from this document.
