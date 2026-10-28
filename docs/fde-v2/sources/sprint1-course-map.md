# Sprint 1: exact course and documentation map

Source-checked **2026-10-28**. Actual learning Days 1–14—not elapsed calendar dates.

**Budget unchanged:** 60 min DSA + 30 min interview/AWS + 90 min AI daily. Four AWS sessions total 120 min inside the role bank. Days 7/14 visibility uses 30 min inside AI, not an extra block. Sprint total 42 h; AI bank 21 h; **added coursework: zero**.

Read only the named subsections, capped by the allocation below; spend the rest implementing, explaining and saving evidence. If a gate fails, carry work forward. DSA75/90 leaves AI75/60, including visibility.

## Verified Udemy inventory and access boundary

- [Ultimate AWS Certified Solutions Architect Associate 2026](https://www.udemy.com/course/aws-certified-solutions-architect-associate-saa-c03?utm_source=chatgpt&utm_medium=llm-integration&utm_audience=all&utm_campaign=chatgpt_integration_phase1) — **SAA-C03 primary**. Catalog/curriculum refreshed today; ownership is the separate October 20 user inventory. Current paid-video access and last-update timestamp remain unknown.
- [[NEW] Ultimate AWS Certified Cloud Practitioner CLF-C02 2026](https://www.udemy.com/course/aws-certified-cloud-practitioner-new?utm_source=chatgpt&utm_medium=llm-integration&utm_audience=all&utm_campaign=chatgpt_integration_phase1) — **CLF-C02 foundation only; not SAA readiness**. Catalog/curriculum refreshed today; ownership is the separate October 20 user inventory. Current paid-video access and last-update timestamp remain unknown.
- [6 Practice Exams | AWS Certified Cloud Practitioner CLF-C02](https://www.udemy.com/course/practice-exams-aws-certified-cloud-practitioner?utm_source=chatgpt&utm_medium=llm-integration&utm_audience=all&utm_campaign=chatgpt_integration_phase1) — **CLF-C02 foundation only; not SAA readiness**. Catalog/curriculum refreshed today; ownership is the separate October 20 user inventory. Current paid-video access and last-update timestamp remain unknown.

The SAA course is primary. CLF may replace a short refresher if needed; it is not a required detour. The six-practice-exam product is **CLF**, not SAA; none is assigned in Sprint 1. No purchases, account creation, sign-in, exam dumps or copied paid lesson content. Curriculum IDs are retained only as exact machine locators in the JSON. The connector returned no lesson URLs, so open the course and find the exact section/title rather than relying on invented lecture numbers.

## AWS days: exact title-level selection

### Day 5 — AWS certification: course and syllabus audit

**30 minutes total; video runtime 3:50 at 1×.**

- **Introduction - AWS Certified Solutions Architect Associate** → Course Introduction - AWS Certified Solutions Architect Associate — 3:50.

Timebox: Course title/code/access audit; log latest-update timestamp as unknown if absent: 5:00; Course introduction: 3:50; Compare SAA guide content outline with the three owned catalog identities: 12:00; Record gaps and course-versus-certification distinction: 9:10.

Record SAA-C03 as primary. CLF is a foundation refresher, not a required detour. Catalog title year is not proof of an actual last-update timestamp.

Official source/fallback: [AWS Certified Solutions Architect - Associate (SAA-C03)](https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03.html); [AWS Certified Cloud Practitioner (CLF-C02)](https://docs.aws.amazon.com/aws-certification/latest/cloud-practitioner-02/cloud-practitioner-02.html).

If paid videos are unavailable, use **12 min targeted official reading + 13 min the same diagnostic/audit + 5 min gap log**, instead of the video path. Do not provision anything.

### Day 6 — AWS certification: IAM diagnostic

**30 minutes total; video runtime 9:20 at 1×.**

- **IAM & AWS CLI** → IAM Introduction: Users, Groups, Policies — 3:22.
- **IAM & AWS CLI** → IAM Policies — 2:50.
- **IAM & AWS CLI** → IAM Roles for AWS Services — 1:39.
- **IAM & AWS CLI** → IAM Best Practices — 1:29.

Timebox: Selected IAM videos: 9:20; Official IAM identity/temporary-role/least-privilege sections: 6:00; Design one principal → role/policy → permitted resource scenario without provisioning: 10:00; Explain deny/least-privilege choice and record unknowns: 4:40.

An app needs read-only access to one document bucket. Name the principal, role, allowed action/resource and why hard-coded long-lived credentials are unsuitable.

Official source/fallback: [What is IAM?](https://docs.aws.amazon.com/IAM/latest/UserGuide/introduction.html); [Security best practices in IAM](https://docs.aws.amazon.com/IAM/latest/UserGuide/best-practices.html); [SAA-C03: Design Secure Architectures](https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03-domain1.html).

If paid videos are unavailable, use **12 min targeted official reading + 13 min the same diagnostic/audit + 5 min gap log**, instead of the video path. Do not provision anything.

Optional CLF **replacement only**, if a foundational gap is found: IAM - Identity and Access Management → IAM Introduction: Users, Groups, Policies (3:22); IAM - Identity and Access Management → IAM Policies (2:50); IAM - Identity and Access Management → IAM Roles for AWS Services (1:39); IAM - Identity and Access Management → IAM Best Practices (1:29). Total 9:20; return the remaining bank to the same diagnostic. This is not equivalent to full SAA design coverage.

### Day 12 — AWS certification: networking diagnostic

**30 minutes total; video runtime 14:46 at 1×.**

- **Networking - VPC** → VPC Overview — 1:10.
- **Networking - VPC** → Subnet Overview — 1:40.
- **Networking - VPC** → Internet Gateways & Route Tables — 1:13.
- **Networking - VPC** → NACL & Security Groups — 10:43.

Timebox: Selected networking videos: 14:46; Official VPC features/security-group basics and syllabus task labels: 5:00; Draw one app network boundary and trace one allowed/blocked path: 7:00; Log routing versus security-group uncertainty: 3:14.

Sketch a conceptual public entrypoint and private app/data boundary. Explain route-table reachability separately from allowed traffic; no cloud resource creation.

Official source/fallback: [What is Amazon VPC?](https://docs.aws.amazon.com/vpc/latest/userguide/what-is-amazon-vpc.html); [Control traffic to your AWS resources using security groups](https://docs.aws.amazon.com/vpc/latest/userguide/vpc-security-groups.html); [SAA-C03: Design Secure Architectures](https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03-domain1.html); [SAA-C03: Design High-Performing Architectures](https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03-domain3.html).

If paid videos are unavailable, use **12 min targeted official reading + 13 min the same diagnostic/audit + 5 min gap log**, instead of the video path. Do not provision anything.

Optional CLF **replacement only**, if a foundational gap is found: VPC & Networking → VPC Overview (1:20); VPC & Networking → VPC, Subnet, Internet Gateway & NAT Gateways (3:34); VPC & Networking → Security Groups & Network Access Control List (NACL) (4:15). Total 9:09; return the remaining bank to the same diagnostic. This is not equivalent to full SAA design coverage.

### Day 13 — AWS certification: storage/data diagnostic

**30 minutes total; video runtime 17:55 at 1×.**

- **AWS Storage Extras** → All AWS Storage Options Compared — 3:34.
- **Amazon S3 Introduction** → S3 Security: Bucket Policy — 5:03.
- **Amazon S3 Introduction** → S3 Versioning — 1:13.
- **Amazon S3 Security** → S3 Encryption — 8:05.

Timebox: Selected storage/data videos: 17:55; Official encryption/versioning notes; skim only the relevant storage and syllabus labels: 4:00; Choose storage, access, encryption and recoverability for the narrow document workflow: 6:00; Record one alternative, conceptual cost driver and unresolved gap: 2:05.

Choose object/file/block storage for source documents versus application metadata. Explain least-privilege access, transit/at-rest encryption, accidental overwrite recovery, and one cost driver. Do not call versioning a complete backup strategy or invent a bill.

Official source/fallback: [What is Amazon S3?](https://docs.aws.amazon.com/AmazonS3/latest/userguide/Welcome.html); [Protecting data with encryption](https://docs.aws.amazon.com/AmazonS3/latest/userguide/UsingEncryption.html); [Retaining multiple versions of objects with S3 Versioning](https://docs.aws.amazon.com/AmazonS3/latest/userguide/Versioning.html); [SAA-C03: Design Secure Architectures](https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03-domain1.html); [SAA-C03: Design High-Performing Architectures](https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03-domain3.html); [SAA-C03: Design Cost-Optimized Architectures](https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03-domain4.html).

If paid videos are unavailable, use **12 min targeted official reading + 13 min the same diagnostic/audit + 5 min gap log**, instead of the video path. Do not provision anything.

Optional CLF **replacement only**, if a foundational gap is found: Amazon S3 → S3 Overview (5:30); Amazon S3 → S3 Security: Bucket Policy (5:03); Amazon S3 → S3 Versioning Overview (1:13); Amazon S3 → S3 Encryption (1:01). Total 12:47; return the remaining bank to the same diagnostic. This is not equivalent to full SAA design coverage.

## Daily role/interview + AI mapping

Each AI segment keeps the accepted plan’s exact minutes. Listed reading is a **maximum inside that segment**, not additional time. Buffer = repair time, not a new lesson. The published starter supplies API/model loading/React/SQLite infrastructure; trace and explain it rather than adding a new infrastructure build. Learner-owned seams remain the four bounded Python/JSON/data tasks. Canonical unit gates below are unchanged; supplied scaffolding does not pass them.

### Day 1

**Role/interview (30 min):** 20 min evidence map + 10 min accurate gap review. Use the actual target-role description and genuine CV/work evidence. Build strengths/gaps map; no generic course or invented job link.
References: No external coursework; use the actual task/evidence..

- **AIFDE-V2-U001 — Baseline, scope and ownership: 90 min primary**. Reading ≤10 min; practice/implementation ≥80 min. Baseline attempt first; consult only a revealed Python transform/SQL gap, then write scope and ownership evidence.
  - [Python: Data Structures](https://docs.python.org/3.12/tutorial/datastructures.html#list-comprehensions); [Python sqlite3](https://docs.python.org/3.12/library/sqlite3.html#tutorial)

### Day 2

**Role/interview (30 min):** 9 min selected sections + 16 min attempt/explanation + 5 min gap log. Predict and then run one short closure/promise ordering example; explain the result.
References: [MDN: Closures](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Closures); [MDN: JavaScript execution model](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Execution_model); [MDN: Using promises](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Using_promises).

- **AIFDE-V2-U001 — Baseline, scope and ownership: 30 min support**. Reading ≤0 min; practice/implementation ≥30 min. Finish U001 architecture/own-versus-borrowed note.
  - No external coursework; use the actual task/evidence.
- **AIFDE-V2-U002 — Reproducible environment and real-model feasibility: 60 min primary**. Reading ≤15 min; practice/implementation ≥45 min. Inspect the Python 3.12 environment and supplied pinned configuration. Record hardware/model-license feasibility facts; no retry of the unresolved metadata 403 or download path.
  - [Python venv](https://docs.python.org/3.12/library/venv.html); [Current model lock](../../../ai/fde_v2/coverstory/model_lock.json); [Current runtime verification](../runtime-verification.md); [Hugging Face Hub: Download files](https://huggingface.co/docs/huggingface_hub/guides/download)

### Day 3

**Role/interview (30 min):** 8 min selected sections + 17 min diagnosis/fix + 5 min gap log. Diagnose one stale-state/key preservation bug and explain a minimal fix.
References: [React: State as a Snapshot](https://react.dev/learn/state-as-a-snapshot); [React: Preserving and Resetting State](https://react.dev/learn/preserving-and-resetting-state).

- **AIFDE-V2-U002 — Reproducible environment and real-model feasibility: 30 min primary**. Reading ≤5 min; practice/implementation ≥25 min. Finish reproducible setup and basic request-path/test receipt.
  - [Python venv](https://docs.python.org/3.12/library/venv.html)
- **AIFDE-V2-U002 — Reproducible environment and real-model feasibility: 30 min buffer**. Reading ≤0 min; practice/implementation ≥30 min. Repair setup only; if blocked record it, do not mark U002 passed.
  - No external coursework; use the actual task/evidence.
- **AIFDE-V2-U003 — Python data contracts and tests: 30 min primary**. Reading ≤8 min; practice/implementation ≥22 min. Inspect the supplied frozen Document contract; start learner-owned synthetic-record validation and explicit errors.
  - [Python 3.12: dataclasses](https://docs.python.org/3.12/library/dataclasses.html); [Python typing](https://docs.python.org/3.12/library/typing.html#typing.TypedDict); [Python: Errors and Exceptions](https://docs.python.org/3.12/tutorial/errors.html#handling-exceptions)

### Day 4

**Role/interview (30 min):** 10 min selected sections + 15 min trace + 5 min gap log. Trace a request and one caching/error/performance issue using a narrow example.
References: [MDN: Using the Fetch API](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch); [MDN: HTTP caching](https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/Caching); [MDN: Critical rendering path](https://developer.mozilla.org/en-US/docs/Web/Performance/Guides/Critical_rendering_path).

- **AIFDE-V2-U003 — Python data contracts and tests: 60 min primary**. Reading ≤10 min; practice/implementation ≥50 min. Implement/test fixture-only ingestion, malformed/empty input, and a boundary case.
  - [Python pathlib](https://docs.python.org/3.12/library/pathlib.html#pathlib.Path.resolve); [Python unittest](https://docs.python.org/3.12/library/unittest.html#basic-example)
- **AIFDE-V2-U003 — Python data contracts and tests: 30 min support**. Reading ≤5 min; practice/implementation ≥25 min. Study only a revealed gap, then make a learner-owned input-format change.
  - [Python typing](https://docs.python.org/3.12/library/typing.html#typing.TypedDict); [Python: Errors and Exceptions](https://docs.python.org/3.12/tutorial/errors.html#handling-exceptions)

### Day 5

**Role/AWS (30 min):** use the exact Day 5 selection above.

- **AIFDE-V2-U004 — Small API with observable failure paths: 90 min primary**. Reading ≤15 min; practice/implementation ≥75 min. Trace and exercise the supplied health/metadata/API paths and request IDs; explain invalid-input behavior, loopback and fixture-scope boundaries. Do not reimplement supplied infrastructure.
  - [FastAPI: Request Body](https://fastapi.tiangolo.com/tutorial/body/); [FastAPI: Handling Errors](https://fastapi.tiangolo.com/tutorial/handling-errors/); [Current loopback API](../../../ai/fde_v2/coverstory/api.py)

### Day 6

**Role/AWS (30 min):** use the exact Day 6 selection above.

- **AIFDE-V2-U004 — Small API with observable failure paths: 30 min buffer**. Reading ≤0 min; practice/implementation ≥30 min. Repair API/setup failure only; carry blockers forward.
  - No external coursework; use the actual task/evidence.
- **AIFDE-V2-U005 — Understand inference and install a real runtime: 60 min primary**. Reading ≤12 min; practice/implementation ≥48 min. Trace the supplied in-process Transformers adapter, local_files_only and trust_remote_code=false. Explain tokens/context/output limits; no model HTTP server or cloud key is used.
  - [Transformers: Chat templates](https://huggingface.co/docs/transformers/en/chat_templating); [Transformers: Auto classes](https://huggingface.co/docs/transformers/en/model_doc/auto); [Current local model adapter](../../../ai/fde_v2/coverstory/models.py)

### Day 7

**Role/interview (30 min):** 20 min rehearsal + 10 min review. Practice one real incident/ownership story. State own contribution and actual results; ask follow-up questions.
References: No external coursework; use the actual task/evidence..

- **AIFDE-V2-U005 — Understand inference and install a real runtime: 30 min primary**. Reading ≤5 min; practice/implementation ≥25 min. Check actual pinned adapter configuration and preserve the unresolved download/inference blocker; offline configuration checks do not pass real inference.
  - [Current runtime verification](../runtime-verification.md); [Current model lock](../../../ai/fde_v2/coverstory/model_lock.json)
- **AIFDE-V2-U005 — Understand inference and install a real runtime: 30 min support**. Reading ≤8 min; practice/implementation ≥22 min. Explain generation versus embeddings/retrieval and record the selected 0.5B model/revision, 2048-token input cap and 160-default/256-max output. No embedding model is assigned in Sprint 1.
  - [Qwen/Qwen2.5-0.5B-Instruct model card](https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct); [Transformers: Generation](https://huggingface.co/docs/transformers/en/main_classes/text_generation)
- **30 min visibility/evidence inside AI:** private evidence note; on Day7 re-estimate scope from actual minutes, ownership and blockers. No external posting is authorized by this map.

### Day 8

**Role/interview (30 min):** 8 min selected sections + 17 min code explanation/repair + 5 min gap log. Explain dependency, stale response and cancellation behavior in one code path.
References: [React: useEffect](https://react.dev/reference/react/useEffect#fetching-data-with-effects); [MDN: Using the Fetch API](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch).

- **AIFDE-V2-U006 — First real generated response: 90 min primary**. Reading ≤8 min; practice/implementation ≥82 min. Implement the learner prompt-forwarding seam against generate_messages(messages,max_new_tokens); real-task execution awaits resolved access and local weights. Capture actual output/timing/failure only, never substitute a mock.
  - [Transformers: Generation](https://huggingface.co/docs/transformers/en/main_classes/text_generation); [Current local model adapter](../../../ai/fde_v2/coverstory/models.py)

### Day 9

**Role/interview (30 min):** 10 min selected sections + 15 min test attempt + 5 min gap log. Design/test one loading/error/keyboard path with accessible names and status behavior.
References: [W3C APG: Button Pattern](https://www.w3.org/WAI/ARIA/apg/patterns/button/); [W3C: Understanding SC 4.1.3 Status Messages](https://www.w3.org/WAI/WCAG22/Understanding/status-messages.html); [Testing Library: ByRole](https://testing-library.com/docs/queries/byrole/).

- **AIFDE-V2-U006 — First real generated response: 30 min buffer**. Reading ≤0 min; practice/implementation ≥30 min. Repair real-inference failure; never substitute test-double output.
  - No external coursework; use the actual task/evidence.
- **AIFDE-V2-U007 — Reality check and first evidence note: 60 min primary**. Reading ≤5 min; practice/implementation ≥55 min. Run five actual capability-smoke prompts; inspect plausible versus verified answers and quality/hardware limits.
  - [Qwen/Qwen2.5-0.5B-Instruct model card](https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct)

### Day 10

**Role/interview (30 min):** 10 min selected sections + 15 min design + 5 min gap log. Draw workflow state and server-data/cache boundaries; explain one tradeoff.
References: [React: Choosing the State Structure](https://react.dev/learn/choosing-the-state-structure); [React: useEffect](https://react.dev/reference/react/useEffect#fetching-data-with-effects); [MDN: HTTP caching](https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/Caching).

- **AIFDE-V2-U007 — Reality check and first evidence note: 30 min primary**. Reading ≤0 min; practice/implementation ≥30 min. Complete all five outcomes and viability review.
  - No external coursework; use the actual task/evidence.
- **AIFDE-V2-U008 — Real generation behind the API: 60 min primary**. Reading ≤10 min; practice/implementation ≥50 min. Trace the supplied same-origin POST /generate to in-process generation and its 422 invalid-input, 429 busy, 503 unavailable, 504 timeout and 502 generation-failure responses. Examine the 45s step-aware criterion; a slow kernel can overrun and model loading is outside it. Real-HTTP actual-generation gate stays blocked without model weights.
  - [FastAPI: Handling Errors](https://fastapi.tiangolo.com/tutorial/handling-errors/); [Transformers: Utilities for generation](https://huggingface.co/docs/transformers/en/internal/generation_utils); [Current loopback API](../../../ai/fde_v2/coverstory/api.py)

### Day 11

**Role/interview (30 min):** 10 min targeted lookup + 15 min contract sketch + 5 min gap log. Specify one endpoint validation/status/error/auth-boundary/idempotency contract, not a new framework.
References: [FastAPI: Request Body](https://fastapi.tiangolo.com/tutorial/body/); [FastAPI: Handling Errors](https://fastapi.tiangolo.com/tutorial/handling-errors/); [MDN: HTTP response status codes](https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status); [MDN: Idempotent](https://developer.mozilla.org/en-US/docs/Glossary/Idempotent).

- **AIFDE-V2-U008 — Real generation behind the API: 30 min primary**. Reading ≤5 min; practice/implementation ≥25 min. Finish real HTTP output plus invalid-input/timeout/unavailable-provider tests.
  - [FastAPI: Testing](https://fastapi.tiangolo.com/tutorial/testing/)
- **AIFDE-V2-U008 — Real generation behind the API: 30 min buffer**. Reading ≤0 min; practice/implementation ≥30 min. Repair integration only; distinguish mocked unit tests from real integration receipt.
  - No external coursework; use the actual task/evidence.
- **AIFDE-V2-U009 — Thin React workflow: 30 min primary**. Reading ≤8 min; practice/implementation ≥22 min. Trace the supplied thin UI state and same-origin request; explain loading/empty/error/result and cancellation guards. No new React implementation assignment.
  - [React: Choosing the State Structure](https://react.dev/learn/choosing-the-state-structure); [MDN: Using the Fetch API](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch); [Current React question-answer screen](../../../frontend/src/fde-v2/QuestionAnswerApp.tsx)

### Day 12

**Role/AWS (30 min):** use the exact Day 12 selection above.

- **AIFDE-V2-U009 — Thin React workflow: 60 min primary**. Reading ≤10 min; practice/implementation ≥50 min. Trace and exercise the supplied UI/backend boundary and accessible error behavior. Inspect actual cancel/stale-response handling; do not infer a visible Cancel button or server-side abort from browser abort alone.
  - [W3C: Understanding SC 4.1.3 Status Messages](https://www.w3.org/WAI/WCAG22/Understanding/status-messages.html); [Testing Library: ByRole](https://testing-library.com/docs/queries/byrole/); [Current React question-answer screen](../../../frontend/src/fde-v2/QuestionAnswerApp.tsx)
- **AIFDE-V2-U009 — Thin React workflow: 30 min buffer**. Reading ≤0 min; practice/implementation ≥30 min. Fix integration/accessibility defect; cannot count fake data as a real generated answer.
  - No external coursework; use the actual task/evidence.

### Day 13

**Role/AWS (30 min):** use the exact Day 13 selection above.

- **AIFDE-V2-U010 — Structured output without trusting it: 90 min primary**. Reading ≤15 min; practice/implementation ≥75 min. Implement the plain-JSON learner parser for exact answer/citations/abstained fields, explicit types and duplicate-citation rejection. This adapter has no schema-constrained generation path. Keep malformed output an explicit failure; real-output gate remains blocked until genuine generation.
  - [Python 3.12: json](https://docs.python.org/3.12/library/json.html#json.loads); [Python typing](https://docs.python.org/3.12/library/typing.html#typing.TypedDict)

### Day 14

**Role/interview (30 min):** 20 min diagnosis + 10 min evidence-led review; no preliminary coursework. Attempt diagnosis unaided for 20 minutes. During the 10-minute review open only the source that addresses the actual defect.
References: [React: useEffect](https://react.dev/reference/react/useEffect#fetching-data-with-effects); [Python: Errors and Exceptions](https://docs.python.org/3.12/tutorial/errors.html#handling-exceptions); [MDN: Using the Fetch API](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch).

- **AIFDE-V2-U010 — Structured output without trusting it: 30 min support**. Reading ≤5 min; practice/implementation ≥25 min. Review explicit JSON/schema failure behavior and schema-versus-truth distinction; no extra validation library or grammar runtime.
  - [Python 3.12: json](https://docs.python.org/3.12/library/json.html#json.loads)
- **AIFDE-V2-U011 — Versioned small corpus and SQL metadata: 30 min primary**. Reading ≤5 min; practice/implementation ≥25 min. Start versioned fixture manifest and SQL metadata schema; the remaining U011 work carries into Sprint 2.
  - [Python sqlite3](https://docs.python.org/3.12/library/sqlite3.html#tutorial)
- **30 min visibility/evidence inside AI:** private evidence note; on Day7 re-estimate scope from actual minutes, ownership and blockers. No external posting is authorized by this map.

## Exactly what to read

These are subsection locators, not whole-page completion assignments.

- **SAA-GUIDE** — [AWS Certified Solutions Architect - Associate (SAA-C03)](https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03.html): Introduction; Target candidate description; Content outline.
- **CLF-GUIDE** — [AWS Certified Cloud Practitioner (CLF-C02)](https://docs.aws.amazon.com/aws-certification/latest/cloud-practitioner-02/cloud-practitioner-02.html): Introduction; Job tasks that are out of scope for the target candidate.
- **SAA-D1** — [SAA-C03: Design Secure Architectures](https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03-domain1.html): Task 1.1: Design secure access to AWS resources; Task 1.2: Design secure workloads and applications; Task 1.3: Determine appropriate data security controls.
- **SAA-D3** — [SAA-C03: Design High-Performing Architectures](https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03-domain3.html): Task 3.1: Determine high-performing and/or scalable storage solutions; Task 3.4: Determine high-performing and/or scalable network architectures.
- **SAA-D4** — [SAA-C03: Design Cost-Optimized Architectures](https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03-domain4.html): Task 4.1: Design cost-optimized storage solutions.
- **AWS-IAM** — [What is IAM?](https://docs.aws.amazon.com/IAM/latest/UserGuide/introduction.html): Identities; Access management.
- **AWS-IAM-BEST** — [Security best practices in IAM](https://docs.aws.amazon.com/IAM/latest/UserGuide/best-practices.html): Require workloads to use temporary credentials with IAM roles to access AWS; Apply least-privilege permissions.
- **AWS-VPC** — [What is Amazon VPC?](https://docs.aws.amazon.com/vpc/latest/userguide/what-is-amazon-vpc.html): Features: VPC, Subnets, Routing, Gateways and endpoints.
- **AWS-SG** — [Control traffic to your AWS resources using security groups](https://docs.aws.amazon.com/vpc/latest/userguide/vpc-security-groups.html): Opening explanation; Security group basics.
- **AWS-S3** — [What is Amazon S3?](https://docs.aws.amazon.com/AmazonS3/latest/userguide/Welcome.html): Opening definition; Features: Storage classes; Access management and security.
- **AWS-S3-ENC** — [Protecting data with encryption](https://docs.aws.amazon.com/AmazonS3/latest/userguide/UsingEncryption.html): Opening explanation: transit, at rest, SSE-S3, SSE-KMS.
- **AWS-S3-VERSION** — [Retaining multiple versions of objects with S3 Versioning](https://docs.aws.amazon.com/AmazonS3/latest/userguide/Versioning.html): Opening explanation; Version storage-cost note.
- **PY-DATA** — [Python: Data Structures](https://docs.python.org/3.12/tutorial/datastructures.html#list-comprehensions): 5.1.3 List Comprehensions.
- **PY-SQL** — [Python sqlite3](https://docs.python.org/3.12/library/sqlite3.html#tutorial): Tutorial; placeholders and transaction behavior needed by the attempt.
- **PY-VENV** — [Python venv](https://docs.python.org/3.12/library/venv.html): Opening explanation; Creating virtual environments.
- **PY-TYPING** — [Python typing](https://docs.python.org/3.12/library/typing.html#typing.TypedDict): Opening note: annotations are not runtime validation; TypedDict.
- **PY-ERRORS** — [Python: Errors and Exceptions](https://docs.python.org/3.12/tutorial/errors.html#handling-exceptions): 8.3 Handling Exceptions.
- **PY-PATH** — [Python pathlib](https://docs.python.org/3.12/library/pathlib.html#pathlib.Path.resolve): Path.resolve; PurePath.is_relative_to. Path containment needs tests; path helpers alone are not a complete hostile-filesystem security guarantee.
- **PY-TEST** — [Python unittest](https://docs.python.org/3.12/library/unittest.html#basic-example): Basic example; assertRaises.
- **FASTAPI-BODY** — [FastAPI: Request Body](https://fastapi.tiangolo.com/tutorial/body/): Create your data model; Declare it as a parameter; Results.
- **FASTAPI-ERRORS** — [FastAPI: Handling Errors](https://fastapi.tiangolo.com/tutorial/handling-errors/): Use HTTPException; Add custom headers; Override request validation exceptions.
- **FASTAPI-TEST** — [FastAPI: Testing](https://fastapi.tiangolo.com/tutorial/testing/): Using TestClient; Testing: extended example. Only if FastAPI is the selected starter stack; do not add a second API framework.
- **MDN-CLOSURES** — [MDN: Closures](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Closures): Lexical scoping; Closure.
- **MDN-LOOP** — [MDN: JavaScript execution model](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Execution_model): Job queue and event loop.
- **MDN-PROMISES** — [MDN: Using promises](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Using_promises): Chaining; Error handling.
- **REACT-STATE** — [React: State as a Snapshot](https://react.dev/learn/state-as-a-snapshot): Setting state triggers renders; Rendering takes a snapshot in time.
- **REACT-KEY** — [React: Preserving and Resetting State](https://react.dev/learn/preserving-and-resetting-state): State is tied to a position in the render tree; Resetting state with a key.
- **MDN-CACHE** — [MDN: HTTP caching](https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/Caching): Validation; Force Revalidation.
- **MDN-CRP** — [MDN: Critical rendering path](https://developer.mozilla.org/en-US/docs/Web/Performance/Guides/Critical_rendering_path): Understanding CRP.
- **REACT-EFFECT** — [React: useEffect](https://react.dev/reference/react/useEffect#fetching-data-with-effects): Fetching data with Effects; Specifying reactive dependencies.
- **MDN-FETCH** — [MDN: Using the Fetch API](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch): Making a request; Canceling a request; Handling the response.
- **WAI-BUTTON** — [W3C APG: Button Pattern](https://www.w3.org/WAI/ARIA/apg/patterns/button/): Keyboard Interaction; WAI-ARIA Roles, States, and Properties.
- **WAI-STATUS** — [W3C: Understanding SC 4.1.3 Status Messages](https://www.w3.org/WAI/WCAG22/Understanding/status-messages.html): Intent; Examples.
- **TEST-ROLE** — [Testing Library: ByRole](https://testing-library.com/docs/queries/byrole/): API; name option; one matching component query.
- **REACT-STRUCTURE** — [React: Choosing the State Structure](https://react.dev/learn/choosing-the-state-structure): Principles for structuring state; Avoid contradictions in state.
- **MDN-IDEMPOTENT** — [MDN: Idempotent](https://developer.mozilla.org/en-US/docs/Glossary/Idempotent): Definition and method examples.
- **MDN-STATUS** — [MDN: HTTP response status codes](https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status): Only codes used by the chosen endpoint.
- **HF-CHAT** — [Transformers: Chat templates](https://huggingface.co/docs/transformers/en/chat_templating): Using apply_chat_template; add_generation_prompt; Special-token duplication warning. Current official docs accessible; exact pinned v5.21.0 documentation URL was not accessible. Match installed API before execution; this is not installation proof.
- **HF-GENERATE** — [Transformers: Generation](https://huggingface.co/docs/transformers/en/main_classes/text_generation): GenerationConfig: max_new_tokens, do_sample, max_time; GenerationMixin.generate. The published adapter calls model.generate in-process. Its custom stopping criterion is step-aware, not a hard process deadline.
- **HF-STOP** — [Transformers: Utilities for generation](https://huggingface.co/docs/transformers/en/internal/generation_utils): StoppingCriteria; StoppingCriteriaList. Read only these classes; the supplied adapter checks its own elapsed-time deadline between decoder steps.
- **HF-AUTO** — [Transformers: Auto classes](https://huggingface.co/docs/transformers/en/model_doc/auto): AutoTokenizer.from_pretrained; AutoModelForCausalLM.from_pretrained: local_files_only, trust_remote_code. The published code loads local files only and disables remote code. A successful documentation fetch does not verify model-file availability.
- **HF-DOWNLOAD** — [Hugging Face Hub: Download files](https://huggingface.co/docs/huggingface_hub/guides/download): From specific version; Filter files to download. Read-only provenance/configuration lesson. Do not run the download recipe or retry the recorded metadata 403 blocker without its resolution.
- **QWEN-MODEL** — [Qwen/Qwen2.5-0.5B-Instruct model card](https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct): License label; Model-specific context and output information; Transformers usage. Actual published candidate: revision 7ae557604adf67be50417f59c2c2f167def9a775, declared by model_lock.json. Remote pinned-revision metadata and artifact hashes remain unverified because of the recorded403 blocker.
- **PY-JSON** — [Python 3.12: json](https://docs.python.org/3.12/library/json.html#json.loads): json.loads; JSONDecodeError; Warning on untrusted JSON size. Actual U010 learner seam uses plain JSON and explicit field/type validation, not llama.cpp grammar constraints or a new Pydantic validator.
- **PY-TIME** — [Python 3.12: time](https://docs.python.org/3.12/library/time.html#time.perf_counter): time.perf_counter. Trace elapsed-time measurement; do not claim it interrupts a slow kernel or covers model loading.
- **PY-DATACLASS** — [Python 3.12: dataclasses](https://docs.python.org/3.12/library/dataclasses.html): dataclass decorator; frozen instances. Actual supplied Document contract is a frozen dataclass; learner validation is still explicit.
- **CODE-RUNTIME** — [Current runtime verification](../runtime-verification.md): Pinned candidate and runtime shape; recorded access blocker. Source/configuration inspection only. No model weights or real inference verified.
- **CODE-MODELS** — [Current local model adapter](../../../ai/fde_v2/coverstory/models.py): `LocalModels.load`; `LocalGenerator.generate_messages`; `GenerationDeadline`.
- **CODE-LOCK** — [Current model lock](../../../ai/fde_v2/coverstory/model_lock.json): generation repository/revision/license/expected files; `trust_remote_code`.
- **CODE-API** — [Current loopback API](../../../ai/fde_v2/coverstory/api.py): learner-owned generation seam and `POST /generate`; blank input returns 422, unavailable model 503, timeout 504, generation failure 502, concurrent overlap 429; request IDs are returned/logged.
- **CODE-UI** — [Current React question-answer screen](../../../frontend/src/fde-v2/QuestionAnswerApp.tsx): `ViewState`, submit, explicit Cancel, stale-response protection, effect cleanup and `AbortController`. Cancel aborts the browser request and ignores late results; server-side generation may continue.
- **CODE-SCOPE** — [Current implementation boundary](../implementation-scope.md): ownership, evidence and gate state.

## Gates and unresolved facts

- Preserve all prerequisites and acceptance criteria: exact copies are in `sprint1-course-map.json → unit_gate_contracts`.
- Real-model output, real API/UI behavior and learner-owned explanations remain mandatory; a documentation lookup or assistant scaffold cannot pass them.
- Day14 starts U011 only. Sprint1 is not full RAG, cloud release, production security, certification readiness or a learner-completion claim.
- Historical provenance: the initial implementation snapshot was commit **6b291d95567de5c9f6943931f004e74c5c9d4d3a**. The current code links above point to the current repository tree. The declared candidate remains **Transformers 5.21.0 in-process + Qwen2.5-0.5B-Instruct**, revision `7ae557604adf67be50417f59c2c2f167def9a775`. React calls same-origin `POST /generate`; FastAPI listens on loopback `127.0.0.1:8765`. There is no separate model-server endpoint. Weights remain absent and real inference blocked/unverified.
- The model/revision/package pins are verified as source declarations, not downloaded or executed artifacts. Adapter: 2048-token input cap, 160 default/256 maximum output tokens, 45-second step-aware deadline. A slow kernel can overrun; model loading is outside that deadline. The current adapter does not expose token counts. Current course entitlement/update date and measured model size/dtype/hardware behavior remain unknown.
- Exact Transformers v5.21.0 documentation paths were inaccessible. Current official Transformers pages were accessible and are marked as current/unversioned references, not pinned-version execution proof. Python links now target 3.12.
- Day1 target-role URL must come from the real target/evidence; no job link was invented.

## Provenance

Accepted source: [https://github.com/advait-ravi-marathe/capstone-project/tree/review/aws-fde-v24-plan/docs/fde-v2/accepted](https://github.com/advait-ravi-marathe/capstone-project/tree/review/aws-fde-v24-plan/docs/fde-v2/accepted). Local plan hashes, all source URLs, item IDs/durations, per-day budgets and access limitations are in JSON. `udemy-curriculum-snapshot.json` contains factual catalog/curriculum metadata only—no paid lecture bodies or questions. No repository/board changes were made by this mapping task.
