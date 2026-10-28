# Model-building-first AI roadmap

> **Superseded as the active sequence on 28 October 2026.** The AWS FDE v2.4
> interview-first roadmap now governs new study: [active plan](../fde-v2/README.md).
> This file and its manifests preserve the earlier 548-day model-building-first
> candidate and history. They do not indicate learner completion or active
> assignments; do not use the old sequence as a second concurrent roadmap.

**Status:** first-30 release candidate; later phases are planning-only. Zero learner days are recorded complete. Day 1 is
**27 October 2026 (Asia/Kolkata)**. The previous API-first candidate is
preserved for later integration, but it is no longer the active sequence.

## Calendar, effort and feasibility

This plan uses **548 study days** (18 months), ending **26 April 2028**. The
date is a planning horizon, not a deadline or expertise guarantee. The
engineering budget remains 120 minutes per day through Day 171, then 165
engineering minutes plus one shared 45-minute interview/review block from Day
172. Certification/course study stays inside these amounts; DSA remains in its
separate repository and daily schedule.

| Window | Engineering capacity | Intended emphasis |
| --- | ---: | --- |
| Days 1–60 (27 Oct–25 Dec 2026) | 120 hours | Python/NumPy, probability and linear-algebra foundations, gradients, leakage-safe data, simple baselines |
| Days 61–150 (26 Dec 2026–25 Mar 2027) | 180 hours | Classical ML, evaluation slices, uncertainty, reproducible experiments |
| Days 151–230 (26 Mar–13 Jun 2027) | 204.25 hours | PyTorch/autograd/optimization, small neural models, educational transformer |
| Days 231–300 (14 Jun–22 Aug 2027) | 192.5 hours | Pretrained-model and fine-tuning readiness; begin CoverStory only after model/data gates |
| Days 301–365 (23 Aug–26 Oct 2027) | 178.75 hours | CoverStory integration and evidence |
| Days 366–450 (27 Oct 2027–19 Jan 2028) | 233.75 hours | StreamForge recommender/ranker |
| Days 451–520 (20 Jan–29 Mar 2028) | 192.5 hours | Developer Workflow Assistant |
| Days 521–548 (30 Mar–26 Apr 2028) | 77 hours | Shared evaluation, recovery, portfolio review |

Total capacity is **1,378.75 engineering hours** and **282.75 shared hours**.
The roadmap reserves 20% of engineering time *inside* each phase envelope for
recovery, integration, observation and reassessment; the planned build envelope
is 1,103 engineering hours. Topic overlap is sequencing guidance, not additive
capacity: reserve is carved out before feature allocation and cannot be spent
by adding work beyond the stated daily 120/165-minute engineering budget.
Unused reserve rolls forward as recovery capacity, not as a requirement to fill
every minute. The 25–39-week model-to-product estimate is conditional, not a
promise that all phases or gates pass on schedule.
At nine months, Day 274, planned engineering capacity is about **625.25
hours**. The target is one strong, evidence-backed CoverStory flagship plus
model experiments; the other two projects are not represented as complete.
Three substantial flagships are a conditional 12–18-month direction, not an
automatic calendar result.

The phase map and measurable exits are in [`manifest.json`](manifest.json).
Project progression is evidence-gated: small model baselines precede a
pretrained-model adaptation; assistant-supplied CoverStory integration begins
around Day 211 only after its baseline and held-out gates; StreamForge and
Developer Workflow follow distinct earlier project evidence. A gate that
has not passed receives bounded remediation inside the existing budget.

### Project targets and gates

| Target window | Project | Entry and exit evidence |
| --- | --- | --- |
| Days 211–365 (25 May–26 Oct 2027) | CoverStory AI | Enter only after a model baseline and held-out evaluation. A nine-month (Day 274) review may accept one coherent vertical slice only if its data, evaluation and integration evidence is real; otherwise log bounded recovery. |
| Days 366–450 (27 Oct 2027–19 Jan 2028) | StreamForge | Start only after CoverStory evidence or a recorded recovery route. Compare a recommender/ranker with a trivial baseline on held-out synthetic/public data and measure cold-start/failure slices. |
| Days 451–520 (20 Jan–29 Mar 2028) | Developer Workflow Assistant | Start after StreamForge evidence or a recovery route. Measure task outcomes, tool authorization, prompt-injection handling and recovery. |
| Days 521–548 (30 Mar–26 Apr 2028) | Shared evaluation and closeout | Reuse one evaluation/observability format across three distinct model capabilities; keep unresolved external/production gates open. |

## What gets built and who codes it

The learner writes DSA in the separate repository and AI/model components here:
small CPU models, data splits, baselines, training/evaluation loops, an
educational tiny transformer, and later useful model integration. FE/BE
application code, UI, Spring/API/auth/CRUD plumbing and load-test harnesses are
assistant-supplied. The learner reads/traces that code, diagnoses seeded
failures, interprets measured behavior, and answers interview questions. No
learner UI/controller/auth implementation or capstone architecture essay is
assigned. The assistant states what architecture is used and why; system
design practice remains in the question bank.

FE/BE/HLD/LLD and scalability are a continuing comprehension/question-bank
lane within existing engineering and shared time. It covers UI state/rendering,
network/caching/streaming/large lists; API contracts, query plans, concurrency,
queues, cache, timeouts/retries/idempotency; LLD boundaries/state machines and
testability; and HLD capacity estimation, load balancing, partitioning,
replication, consistency, backpressure, rate limits, failure recovery and
observability. AI-serving review includes batch size, model memory, throughput,
latency, cost and quality. Supplied local harnesses record declared hardware,
concurrency, duration, p50/p95 latency, errors and resource bounds. Local
measurements do not establish global or production scale. Redis is considered
only for a demonstrated cache/rate-limit need (TTL, invalidation, atomicity and
failure); Kafka only for a justified asynchronous flow (partitions, consumer
groups, ordering, replay, lag and idempotency). Compare simpler alternatives
first. Assistant-supplied containers, CI/CD, IaC, secrets boundary,
rollout/rollback, monitoring and incident walkthroughs appear when project
prerequisites justify them; Kubernetes follows simpler deployment concepts.
MLOps coverage includes dataset/model versioning, reproducible training,
experiment tracking/registry, evaluation gates, deployment readiness, drift
and retraining. All fit existing hours. No external load generation, cloud
spend or deployment is included.

## First 30 days

All days are actionable from Python/JSON basics through an independently
evaluated synthetic document-type model. Day 1 is a CPU-only logistic-regression
step with a tiny worked example; it is not an API call. No paid service or
dependency installation is required for Day 1. Day 1's contract contains the sigmoid, loss, gradient and parameter-update
walkthrough before the learner task. Days 2–6 also include small formula-level
examples. The individual day contracts are generated from [`manifest.json`](manifest.json) by
[`generate_model_first_roadmap.py`](../../scripts/generate_model_first_roadmap.py).

Run the worked example and instructor-only tests:

```sh
python3 -m ai.model_building.day01_example
python3 -m unittest ai.model_building.tests.test_day01_infrastructure
python3 -m unittest ai.model_building.tests.test_acceptance_witnesses
python3 -S -m ai.model_building.tests.stdlib_days_02_14
```

The learner-owned implementation remains an explicit TODO. Its contract is
expected to be red until attempted and implemented; this is not an
infrastructure failure and does not record completion:

```sh
python3 -m unittest ai.model_building.learner_acceptance.test_day_01
```

Each of the 30 contracts identifies the concept, prerequisite, source, bounded
AI/model work, exact acceptance command, negative case and evidence. Days
26–28 are manual, read-only application comprehension and source diagnosis;
they reuse previously introduced model error helpers and do not claim new
model experiments or assign FE/BE code changes. Their automated checks validate
small model-output helpers only; they cannot prove a learner avoided every
out-of-scope FE/BE edit. Day 30 is a model training/evaluation slice,
not a production or live-provider claim.

The [bounded integration ownership amendment](integration-ownership-amendment.md)
does not change those first-30 lessons. It proposes five small later slots in
the separate 548-day plan: a model-serving contract, API failure diagnosis, a
stale-response UI/API repair, score presentation in StreamForge, and safe
interaction states in the Developer Workflow Assistant. The assistant owns
the server/UI scaffolding; the learner owns the narrow integration behavior,
tests, error handling and debugging. This review branch supplies a loopback
model API scaffold and an intentionally unfinished React panel for the first
CoverStory slice. Those future assignments are not yet published as daily
contracts, do not add hours, and are not completion evidence.

Days 1–14 use only Python's standard library. The acceptance module imports
without third-party packages; days 2–14 can run before any scientific package
is installed. Day 15 introduces NumPy; Day 21 introduces scikit-learn. Create
an isolated environment at that point and install the pinned CPU dependencies:

```sh
python3 -m venv .venv-model-first
. .venv-model-first/bin/activate
python -m pip install -r requirements-model-first.txt
```

No installation is performed by this draft. PyTorch is deferred to the later
neural-model phase; the first month does not require it. The [first three
calendar sprints](manifest.json) are 27 Oct–9 Nov (14 detailed cards), 10–23
Nov (14), and 24 Nov–7 Dec (2 detailed cards); Day 30 is 25 November and the
third sprint continues beyond the detailed first month.

## Sources and study targets

The first month uses official Python Tutorial, NumPy beginner, and
scikit-learn pitfalls/evaluation material. PyTorch's free **Learn the Basics**
supports the later tensors/autograd/training phase. The Hugging Face **LLM
Course** chapters on transformer models and fine-tuning are deferred until
Python and introductory deep learning are established. Exact references and
limitations are listed in the manifest and contracts; no course video is
claimed watched.

The **Machine Learning Specialization** (Stanford Online + DeepLearning.AI)
is a beginner preparation target, not a Stanford degree. Provider certificate
access and graded assessments may require paid enrollment; none is authorized.
The **Deep Learning Specialization** is later and optional: its official labs
use TensorFlow, while this project uses PyTorch for model experiments.
Reimplementing exercises in PyTorch does not satisfy provider certificate
assessments. NVIDIA open-source notebooks are optional practice, not an NVIDIA
certification. AWS Machine Learning Engineer – Associate is a later
readiness-based target only after genuine AWS ML deployment experience; verify
the exam version and blueprint before preparation. No enrollment, exam booking,
purchase, credential, or certification pass is claimed.

Role benchmarks are restricted to the approved top-company list in the dated
[role alignment snapshot](role-alignment.md). They are
signals for skill selection, not guarantees or an exhaustive live vacancy
survey. Research Scientist is not the default target.

## Existing issue definitions and prior candidate

The read-only [issue migration proposal](legacy-issue-migration.json) maps the
30 current detailed days to existing EAI-211–240 and preserves the original
305 definitions, statuses and history. The other 275 are superseded, not
deleted or completed. No YouTrack writes occur here. The first-30 candidate
excludes the deferred API-first app/provider code; that separate work remains
on the broader review branch for later review. The migration is generated
directly from the immutable published issue export, so no API-first candidate
files are needed to reproduce this mapping.

The October 2029 professional-ownership review remains distinct from this
learning calendar and requires real external ownership, influence and adoption
evidence. Calendar completion cannot grant a title, role, certification, user
adoption or production-operating claim.

The dated portfolio and role-family checkpoints are maintained in the
[manifest](manifest.json) and the existing [dashboard alignment source](../reboot/fde-alignment.json).
They review evidence only: AI Product/Applied AI SWE is the first role-family
benchmark, FDE comes later if external delivery evidence exists, and the
October 2029 Staff-responsibility review remains a separate professional
ownership question.
