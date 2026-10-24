# Proposed continuation: Days 197–270

**Status: review proposal only.** This document does not activate Days 197+, create daily contracts, alter the EAI board, or claim learner completion. It is a planning bridge for later approval. The current continuation branch is `work/eai-sprint2-onward`; audited base `c4f84d49630bfa96ef816c529556cd2367b7148b`. No paused day-level 197–210 work was found: the ref `learning/streamforge-days-197-210` points to the same StreamForge Days 183–196 line and contains no 197–210 contracts or transfer plan. Preserve that ref and existing files.

## Guardrails carried forward

- CoverStory remains the primary applied project. The learner implements and tests assistant-supplied, source-accurate architecture decisions; capstone design proposals and post-hoc architecture essays are not assigned. System-design practice remains in the separate interview question bank.
- StreamForge stays conditional. The Day 153/154 evidence files exist but currently record `not_attempted` / `not_run`; a future gate evaluation must use their actual contents. Planning future work does not assert that the gate has passed.
- No real customer, personal data, public deployment, AWS resource, paid exam, credential, or external integration is authorized here. Use synthetic fixtures and offline/local adapters by default.
- A retrieval or agent workflow is justified only if a bounded user/operator task needs source-grounded retrieval or a constrained multi-step tool call. Otherwise use a simpler deterministic workflow. No consequential underwriting, claims, or eligibility decision is automated.
- Professional production ownership, customer delivery, mentoring, and Staff-level influence require real external evidence. Solo portfolio work can demonstrate technical knowledge and reproducibility, not those professional claims.

## Capacity

Days 197–270 are 74 days: five complete 14-day windows (197–266) and a four-day closeout (267–270).

| Allocation | Per day | 74-day total | Use |
|---|---:|---:|---|
| Engineering | 165 min | 12,210 min / 203.5 h | 35 study, 115 practice/tests, 15 evidence, subject to each approved day's actual split |
| Shared interview/review | 45 min | 3,330 min / 55.5 h | One block total for question practice, new questions, or unfinished review; never duplicated |
| Combined | 210 min | 15,540 min / 259 h | Excludes any new DSA assignment; DSA history and review clock remain in `dsa-prep` |

These are ceilings, not a promise that all proposed scope fits. A failed prerequisite consumes the bounded practice slice for recovery and moves unfinished work to the next named window; it does not create overtime. The 35-minute study allocation is shared by engineering concepts and certification study. If the two certification syllabi or the AI work do not fit, extend the roadmap beyond Day 270 rather than dilute project acceptance or add minutes.

## Proposed windows and evidence gates

The following are sprint-level intents, not activated daily assignments. Daily scope, exact selectors and course rows should be written only after inspecting the accepted state at the preceding gate.

| Days / planned dates | Proposed focus and bounded runnable proof | Gate, fallback and exit evidence |
|---|---|---|
| **197–210 · 8–21 May 2027** | Re-establish CoverStory readiness from actual evidence; select one low-risk, source-grounded policy-explanation or operations-assistance workflow. Build a deterministic offline corpus and baseline retrieval evaluator only if the data/privacy/authorization boundary is met. Test citation presence, abstention on unsupported questions, owner/document filtering and fixture isolation. | If prior data/evaluation gates are unmet, spend the window on named CoverStory recovery and offline test design; do not activate StreamForge or call a retrieval service. Exit with actual gate output, source/permission contract, baseline test report and unresolved risks—not a claim of AI quality. |
| **211–224 · 22 May–4 Jun 2027** | Implement a bounded local retrieval slice where justified: ingestion validation, stable source IDs/versions, chunk/retrieval boundary, owner authorization before retrieval, cited response projection, and unsupported-answer fallback. Add synthetic adversarial documents and deterministic evaluation fixtures. | Requires the preceding authorization and data boundary. If absent, improve ordinary CoverStory search/FAQ or defer. Exit with independent expected answers/citations, negative owner-access tests, and full reproducible local run. |
| **225–238 · 5–18 Jun 2027** | If a real bounded workflow requires actions, add one constrained tool/action with server-side authorization, schema validation, idempotency, human approval for any sensitive write, and an audit record. Prefer a deterministic workflow if an agent adds no measurable value. | No autonomous high-impact decisions. If no justified tool exists, use this window for evaluator breadth and failure handling, not an invented agent. Exit with deny/allow matrix, replay/no-duplicate checks, approval-path tests and a documented simpler alternative. |
| **239–252 · 19 Jun–2 Jul 2027** | Measure a small fixed evaluation set for citation correctness, answerability, owner leakage, prompt-injection resistance, tool misuse, latency and token/call cost. Add trace correlation with sensitive payload redaction, bounded retries/timeouts and safe degraded behavior. Run locally or against a deterministic fake model unless a separate live-service authorization is granted. | Do not treat an LLM judge as ground truth; retain deterministic assertions and human review for sensitive cases. Exit with versioned fixture, metric definitions, before/after results, and known blind spots. |
| **253–266 · 3–16 Jul 2027** | Rehearse one distinct *synthetic* enterprise integration contract (identity, document policy, rate/failure behavior) behind an adapter. Exercise compatibility, migration/rollback limits, operational runbook and a production-like acceptance path using disposable resources only. | It is explicitly a simulation and not a second customer delivery. If core CoverStory or AI controls remain incomplete, recover those first. Exit with a second-contract test, failure/recovery observations, architecture explanation supplied by the assistant and an honest demo boundary. |
| **267–270 · 17–20 Jul 2027** | Four-day evidence closeout: rerun only accepted selectors, assemble reproducible artifact/demo instructions, score the existing capability evidence map, check course/cert study state, and select the next bounded queue. | Do not broaden features. Separate pass, fail, blocked, not-run, instructor scaffold and learner-owned evidence. Application checkpoint is an invitation to compare current evidence with live postings, not a readiness/title guarantee. |

### Example task-sized proof patterns

These examples describe the scale of later assignments, not additional features to stack on one day:

- Java/Spring: a pure retrieval-result validator accepts only known source IDs and bounded snippets; tests cover missing, stale, foreign-owner and malformed citations.
- PostgreSQL: a disposable fixture associates corpus documents and permissions with owners; tests prove foreign documents never enter the retrieval request and reads leave full state unchanged.
- Python evaluation: fixed synthetic questions and expected source IDs produce reproducible citation/abstention metrics; adversarial cases are separate from ordinary quality scores.
- React: an accessible reference-only answer panel renders cited sources and an explicit “not enough evidence” state; tests verify no hidden action is submitted from display state.
- Human-approved write: one explicit server action receives a stable request key; tests prove anonymous, unauthorized, unapproved, replayed and conflicting inputs are handled distinctly and do not create extra audit/domain rows.
- Operations: a fake-model/local adapter and bounded failure controls exercise timeout, unavailable provider, redacted trace and cost ceiling without credentials or cloud resources.

Each accepted day's test should prove only its assigned seam. A passing unit predicate is not packaged/browser/database evidence; a synthetic fixture is not a customer outcome. Release-oriented tests must state their artifact, database lifecycle, selector and exact source identity.

## Certification targets: planning, not readiness

The two candidate AWS credentials are **AWS Certified Solutions Architect – Associate (SAA-C03)** and **AWS Certified Developer – Associate** (AWS currently indicates an updated DVA-C03 registration window begins 27 October 2026). This is a target pair to evaluate, not a claim that both are required by existing verified curriculum, owned course, completed preparation, or purchased exam. Recheck the official exam guide/version and local availability before allocating study or booking. Existing catalog metadata lists *Ultimate AWS Certified Solutions Architect Associate 2026* as owned but unwatched; it does not establish Developer Associate course access. Use official exam guides/Skill Builder material for scope and do not buy/enroll without separate user direction.

The 74-day study ceiling is only 2,590 minutes (43.2 hours) in total. Even assigning all study time to certification preparation would leave no engineering concept study, so that is not a viable default. A future approved plan should reserve a modest portion of the 35-minute daily study block, alternate SAA and Developer Associate only after choosing the current exam versions, and assess readiness with official practice material. If either preparation does not fit alongside prerequisites and AI acceptance, continue it after Day 270. No exam pass is assumed.

## Learning sources to use when daily scope is approved

Primary documentation titles below were checked on **24 October 2026**. They support concepts; they do not imply a service is selected or that the learner watched anything.

- AWS, [*Retrieve data and generate AI responses with Amazon Bedrock Knowledge Bases*](https://docs.aws.amazon.com/bedrock/latest/userguide/knowledge-base.html): RAG, source retrieval and citations; use as an architectural comparison, not a required managed AWS deployment.
- AWS, [*Detect and filter harmful content by using Amazon Bedrock Guardrails*](https://docs.aws.amazon.com/bedrock/latest/userguide/guardrails.html): input/output controls and contextual grounding checks; these filters do not replace authorization or application-specific testing.
- OpenAI, [*Working with evals*](https://developers.openai.com/api/docs/guides/evals): evaluation dataset/workflow concepts. Use deterministic local expected results alongside any model-based grader.
- AWS, [*Evaluate agent performance with Amazon Bedrock AgentCore Evaluations*](https://docs.aws.amazon.com/bedrock-agentcore/latest/devguide/evaluations.html) and [*Observe your agent applications on Amazon Bedrock AgentCore Observability*](https://docs.aws.amazon.com/bedrock-agentcore/latest/devguide/observability.html): evaluation traces and operational signals. These current service docs are reference material only; no account, trace upload or spend is authorized.
- AWS, [*AWS Well-Architected Framework*](https://docs.aws.amazon.com/wellarchitected/latest/framework/welcome.html): trade-off review for security, reliability, performance and cost; use only when a later cloud decision is authorized.
- Certification scope: AWS [*Certified Solutions Architect – Associate*](https://aws.amazon.com/certification/certified-solutions-architect-associate/) and [*Certified Developer – Associate*](https://aws.amazon.com/certification/certified-developer-associate/) official pages and their currently linked exam guides. Version and registration information can change; verify again when scheduling.

No new Udemy lesson is assigned in this proposal because course catalog access/title metadata for post-Day-196 AI content was not verified. Select exact lesson titles and durations only in the later approved daily contracts; keep any course within the existing study minutes and substitute official docs where access is unavailable.

## Progressive application checkpoints

By Day 270 the plausible portfolio outcome is a reviewable, local/synthetic AI-assisted workflow with source/permission boundaries, evaluation cases, failure handling and honest operational measurements—*if* its entrance gates and task checks are actually satisfied. That can support applications targeting roles whose requirements match demonstrated backend, integration and AI-evaluation knowledge. It does not prove cloud production operations, customer discovery, adoption, business value, mentorship, or professional Staff/Principal influence.

Longer-term (the existing North Star's roughly three-year direction, not an automatic schedule): authorized production delivery; real customer/stakeholder discovery and outcomes; repeated ownership across more than one integration; incident/reliability responsibility; design-review influence and mentorship; reusable platform impact measured by other teams adopting it. These require external workplace evidence and cannot be manufactured by extending this solo roadmap. Re-evaluate target postings at each application checkpoint and make no hiring or promotion promise.

## Review checklist before activation

1. Re-read actual learner evidence and evaluate gates from authoritative files; do not infer success from scaffold CI.
2. Convert only the next 14-day window into daily contracts, with exact source titles, executable selectors, bounded acceptance, prerequisites and named deferred work.
3. Check that each daily workload fits 165 engineering minutes plus one shared 45-minute block; keep DSA solely in its separate repository.
4. Require offline/local alternatives and explicit permission boundaries for every proposed cloud or live-model action.
5. Keep the proposal status until the user approves a window. Do not create tickets or learner-completion records from this document.
