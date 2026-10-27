# Preserved API-first candidate (superseded)

> **Status:** This is the preserved, unshipped API-first review candidate. It is
> not the active learning sequence and must not be used to infer learner
> progress. The active plan is the [model-building-first roadmap](../model-first-roadmap/README.md).
> Its existing starter code and contracts are retained as possible later
> product-integration material; no files or issue history were deleted.

**Status:** local review draft. No learner day is recorded complete. The prior
305 EAI Story definitions, issue IDs, statuses, and history remain available
unchanged for a later, separately reviewed board migration.

## Calendar and capacity

- Day 1: **27 October 2026**, Asia/Kolkata.
- Day 30: 25 November 2026; Day 365: 26 October 2027.
- Days 1–171: 120 engineering minutes per study day.
- Days 172–365: 165 engineering minutes plus one shared 45-minute
  interview/review block per study day. System-design practice stays in the
  separate question bank. Certifications use this existing study allocation.
- Total: **875.5 engineering hours + 145.5 shared hours**. DSA remains in the
  separate repository; its content and actual-attempt review schedule are not
  copied or edited here.

The calendar is a planning horizon, not a deadline that overrides evidence
gates or a guarantee of job readiness. The October 2029 professional-ownership
review remains separate from this 365-day learning horizon.

## Three substantial, distinct projects

| Days | Project | Evidence gate |
| --- | --- | --- |
| 1–150 (27 Oct 2026–25 Mar 2027) | **CoverStory AI**, a grounded assistant for public or synthetic policy documents. | 300 engineering hours: 60 reserved for remediation/integration/observation/reassessment and 240 planned build hours. Day 30 integrated offline demo; then ingestion, permission-aware retrieval, citations, abstention, safe failures, evaluation and observability. A real configured provider path is provided; live evidence remains open until separately authorized and independently verified. |
| 151–255 (26 Mar–8 Jul 2027) | **StreamForge AI**, using the existing media/platform plumbing for search, recommendation and ranking. | 273 engineering hours: 54.6 reserved and 218.4 planned build hours. A distinct ranking capability over fixed public/synthetic data, with baseline comparisons, cold-start and failure slices, and independently labeled evaluation. Do not claim real users or adoption from local fixtures. |
| 256–340 (9 Jul–1 Oct 2027) | **Developer Workflow Assistant**, distinct developer tasks with controlled tool use. | 233.75 engineering hours: 46.75 reserved and 187 planned build hours. Read-only tools first; writes require explicit approval and authorization tests. Include recovery and injection tests and measured task outcomes. Day 274 is early/in progress in this project. |
| 341–365 (2–26 Oct 2027) | Shared evaluation/observability integration and portfolio closeout. | 68.75 engineering hours: 13.75 reserved and 55 planned build hours. Apply one reusable evaluation capability across the three projects, rerun evidence gates, package limitations honestly, and close no gate by calendar alone. This is shared tooling, not a fourth flagship. |

The plan allocates 365 days and 875.5 engineering hours at the established
budget. The phase plan reserves 20% (175.1 hours) inside those hours for
remediation, integration, observation and reassessment; 700.4 hours remain
planned build capacity. These are planning envelopes, not guaranteed actual
time use or a promise of mastery. No hours are added.
Evaluation starts in the first month, with an independent fixed label set on
Day 7, then carries across all projects. Retrieval learns a lexical baseline
first, then a tiny local vector similarity comparison labeled as a teaching
proxy, not a production embedding. A reranker remains optional until its
prerequisites are met.

The nine-month checkpoint is **27 July 2027 (Day 274)**: review the CoverStory
and StreamForge exit gates conditionally, while Developer Workflow Assistant
is early/in progress. It grants no title, job, or automatic completion.

## FE/BE and AI ownership

The assistant supplies React/TypeScript UI and Spring/API/security/CRUD
plumbing and explains what each piece does and why. Learner FE/BE work is
reading, request/data-flow tracing, diagnosis, and interview questions. Days
26–28 are explicitly comprehension-only. No learner Java, TypeScript, UI,
controller, or authentication edits are assigned. Thin AI serving/testing and
Python AI components may be learner-owned. DSA remains the learner's work in
the separate repository.

### Assistant-selected first-month architecture

The assistant supplies the architecture and its rationale; no capstone design
proposal or post-hoc design defense is assigned. The existing React app hosts
the AI route; the existing Spring app keeps the synthetic session/CSRF boundary
and derives tenant identity from the authenticated principal; Python owns the
learner AI prompt, retrieval and evaluation seams. A supplied Python fixture
service gives the UI/API a deterministic loopback-only offline path. A separate
configurable OpenAI Responses adapter is available for a future, separately
authorized live-provider exercise. This reuses the existing application
boundary, keeps AI experiments in Python, and keeps offline demos distinct
from provider evidence. See the [architecture metadata](manifest.json),
[React route](../../frontend/src/ai/AiFirstCoverStoryApp.tsx),
[Spring controller](../../src/main/java/assignments/local/LocalAiAssistantController.java),
[Python service](../../ai/coverstory_assistant/server.py), and
[provider adapter](../../ai/coverstory_assistant/provider.py).

## Day 1–30 contracts

The exact dates, sources, prerequisites, learner scope, acceptance command,
negative case, and evidence category are in the generated
[`manifest.json`](manifest.json) and individual
[`contracts`](contracts/). Every coding task has a learner acceptance test
that is expected to be red while its TODO remains. Tests under `tests/` check
the independent instructor oracle and local infrastructure; they do not
credit learner completion.

Start with [Day 1](contracts/day-01.md):

```sh
python3 -m ai.coverstory_assistant.day01_example
python3 -m unittest ai.coverstory_assistant.tests.test_day01_infrastructure ai.coverstory_assistant.tests.test_day01_oracle
python3 -m unittest ai.coverstory_assistant.learner_acceptance.test_day_01
```

The last command is expected to reach the explicit learner TODO and fail until
the bounded prompt behavior is implemented. It makes no provider call.

The Day 30 demonstration is a bounded synthetic vertical slice: supplied
React/session/Spring plumbing calls a loopback Python fixture service, while
the learner-owned Python pipeline is evaluated against fixed expected outputs
and tenant-leak/unsupported-answer controls. The fixture path is labeled
offline and is not live model validation. See [Day 29](contracts/day-29.md)
and [Day 30](contracts/day-30.md) for the learner path and evidence categories.
The supplied full local launcher is `scripts/package-local-demo.sh` followed
by `scripts/local-demo.sh`; it requires Docker for disposable local PostgreSQL
and prints a loopback UI URL ending in `/#/ai-first/coverstory`. It sends no
provider request.

Day 3 includes a real, configurable Responses API client. Protocol tests use a
local loopback HTTP server; no paid/provider call occurs in tests. A live
request requires user-managed configuration and an explicit command flag, and
is not currently authorized or performed. Local fixtures and mocks are not
live-model evidence.

## Credentials and courses

Preparation targets are **NVIDIA Certified Associate: Generative AI LLMs
(NCA-GENL)**; **AWS Certified Solutions Architect – Associate (SAA-C03)**
using the already-owned Stephane Maarek course; and **AWS Certified Generative
AI Developer – Professional (AIP-C01)** as a later, readiness-based target
after substantial project evidence. Current exam titles/codes were checked on
official [AWS SAA-C03](https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03.html),
[AWS AIP-C01](https://docs.aws.amazon.com/aws-certification/latest/ai-professional-01/ai-professional-01.html),
and [NVIDIA NCA-GENL](https://www.nvidia.com/en-us/learn/certification/generative-ai-llm-associate/)
pages on 27 October 2026; verify again before study or any booking. The
NVIDIA “Building RAG Agents with LLMs” course certificate remains optional
only if access is already available. No new purchase, enrollment, credential,
exam booking, or certification pass is claimed or authorized.

## Historical sequence and issue mapping

The old quote-selection and conventional FE/BE assignment sequence is
historical. Its manifests, contracts, source receipts, code, and tests remain
preserved. At the time this proposal was drafted, entrypoints pointed to it;
current entrypoints direct learners to the model-building-first plan. The separate
[`legacy issue migration proposal`](legacy-issue-migration.json) maps 30
existing EAI issues to the detailed active units and accounts for the other
275 as superseded—not deleted or completed. It records the original title and
definition digest for each issue. No board writes or DSA repository writes
occurred.

The migration export preserves DSA pointers only; it does not copy DSA
problems or solutions. The issue IDs remain their native IDs and are never
treated as day numbers.

## Role alignment snapshot

See the dated [first-party role sample](role-alignment.md). It is a sample of
current postings, not a complete or permanent survey. Job titles, hiring
requirements, and exam versions can change; recheck them before decisions.
Portfolio capability checkpoints are not professional ownership claims or
guarantees of hiring, level, or promotion.
