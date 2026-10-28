# First-release AI contracts:30 units, not 30 days

**Controlling priority: interview/job switch first.** These 30 AI units preserve the prior 60-hour first-release scope without squeezing it into the reduced AI allocation. They require 58 hours including 8 hours of genuine buffer, plus weekly visibility. With DSA60 + role preparation 30 + AI90, the whole local prototype is estimated around learning Day 41, not guaranteed Day 30. All code remains proposed_contract_only and learner state not_started.

Exact first 30 daily allocations are in `first-30-days.json` / `.md`. Primary unit tasks remain 90 minutes, but may span days. Do not call a unit completed just because its scheduled segment ended.

## AIFDE-V2-U001: Baseline, scope and ownership
Primary 90 min + 30 min support. Prerequisites: none

Inspect the minimal starter; attempt a small Python transform and SQL query if a runtime is available, otherwise record the unavailable environment as a baseline finding; choose the smallest useful public/synthetic-document workflow.

**Planned outputs (not existing files):** docs/baseline.md; docs/problem-brief.md; baseline attempt/test output

**Acceptance:**
- Record actual attempts and unknowns without marking prior SWE experience as new curriculum completion.
- Name one user, one workflow, a non-AI baseline, one measurable prototype objective and explicit non-goals.
- Explain the intended starter request path from code; execution and an actual code-change check occur after the environment is available, not falsely before setup.

**Support/buffer:** Write the architecture sketch and own-vs-borrowed code note.

## AIFDE-V2-U002: Reproducible environment and real-model feasibility
Primary 90 min + 30 min buffer. Prerequisites: AIFDE-V2-U001

Lock the Python project environment; create safe configuration; measure available CPU/RAM/disk; verify a no-service-fee real local generation and embedding route against official distribution/license information.

**Planned outputs (not existing files):** environment lockfile; .env.example; docs/runtime-feasibility.md; capability-status.json

**Acceptance:**
- A clean environment runs a basic test and the starter request path; no key or private data is committed. Record a blocker rather than claiming execution if unavailable.
- Record machine measurements, model/runtime candidates, download size/license and expected limitations; exact versions are resolved at implementation.
- If inference cannot run within available hardware/permissions, record a blocker and the specific approval/access needed; no mock pass.

**Support/buffer:** Resolve setup or dependency problems; otherwise stop or revisit the baseline.

## AIFDE-V2-U003: Python data contracts and tests
Primary 90 min + 30 min support. Prerequisites: AIFDE-V2-U002

Implement typed request/document records, a small plain-text/Markdown ingestion function and meaningful error handling; use tests rather than re-learning all programming basics. From the first read, constrain ingestion to the owned fixture directory; default raw-content logging off.

**Planned outputs (not existing files):** ingest module; data contracts; ingestion unit tests

**Acceptance:**
- Parse the permitted small fixture format and reject malformed/empty inputs.
- Demonstrate one exception path and test one boundary case.
- Make one small input-format change without copying a finished solution.

**Support/buffer:** Study only the Python typing, collection or exception gaps revealed by the task.

## AIFDE-V2-U004: Small API with observable failure paths
Primary 90 min + 30 min buffer. Prerequisites: AIFDE-V2-U003

Implement /health and a document-metadata endpoint in one Python API stack; connect input validation, request IDs and structured errors. Bind to loopback only by default; enforce fixture-directory scope and keep raw-content logging off from the start.

**Planned outputs (not existing files):** API endpoints; API tests; request/response contract

**Acceptance:**
- Success, invalid-input and not-found requests return the documented status/body.
- A request ID connects response to a log entry without secrets.
- Explain the request path and why this stack is sufficient; no framework collection.
- Default API binding is loopback-only; a filename/path cannot escape the owned fixture directory, and raw document/prompt logging is off.

**Support/buffer:** Repair API/setup failures; avoid optional architecture expansion.

## AIFDE-V2-U005: Understand inference and install a real runtime
Primary 90 min + 30 min support. Prerequisites: AIFDE-V2-U002, AIFDE-V2-U004

Learn token/context/output-limit basics and the difference between generation, embeddings and retrieval; install/configure the selected real local model runtime and a narrow provider adapter.

**Planned outputs (not existing files):** docs/inference-contract.md; real provider adapter configuration

**Acceptance:**
- Model identifier/revision, license, quantization, context and output limits are recorded where available.
- Adapter accepts a prompt and exposes explicit timeout/error behavior; credentials, if ever authorized, remain outside code.
- Explain why an extractive/template answer and a mocked provider are not generative inference.

**Support/buffer:** Use current official runtime/model documentation; do not memorize a stale model list.

## AIFDE-V2-U006: First real generated response
Primary 90 min + 30 min buffer. Prerequisites: AIFDE-V2-U005

Call the actual pretrained generative model through the adapter on three simple tasks; capture real outputs, elapsed time and failures; tune only output/context limits for the machine.

**Planned outputs (not existing files):** artifacts/inference-smoke.json; real-local integration test; docs/model-limitations.md

**Acceptance:**
- At least one non-canned generated response is produced by the recorded real model, not a test double.
- Record hardware, request settings, actual timing and actual token counts if exposed; otherwise mark counts unavailable.
- A forced timeout/unavailable runtime is handled honestly; failed inference keeps the capability gate blocked.

**Support/buffer:** Repair runtime or adapter issues; request a bounded alternative only if local capability is insufficient.

## AIFDE-V2-U007: Reality check and first evidence note
Primary 90 min + 0 min none_visibility_scheduled_weekly. Prerequisites: AIFDE-V2-U006

Run a five-prompt capability smoke set including an impossible/unanswerable request; inspect failures and confirm what the model can and cannot support in this prototype.

**Planned outputs (not existing files):** evals/inference-smoke.jsonl; artifacts/inference-smoke-review.md

**Acceptance:**
- All five actual outcomes are saved, including failure or uncertainty.
- Distinguish a plausible response from a verified answer.
- Decide whether the local model is viable for the narrow corpus; do not hide a hardware/quality blocker.

**Support/buffer:** The evidence note is scheduled weekly in the daily schedule, not charged twice to this unit.

## AIFDE-V2-U008: Real generation behind the API
Primary 90 min + 30 min buffer. Prerequisites: AIFDE-V2-U004, AIFDE-V2-U006

Implement a /generate request path using the real adapter with a bounded request deadline and client cancellation/error behavior.

**Planned outputs (not existing files):** /generate implementation; API integration tests; one real API trace

**Acceptance:**
- A real HTTP request returns actual model output.
- Invalid input, timeout and unavailable provider paths are tested.
- Separate deterministic mocked unit tests from the real-model integration receipt.

**Support/buffer:** Debug the integration; defer streaming until the basic path is reliable.

## AIFDE-V2-U009: Thin React workflow
Primary 90 min + 30 min buffer. Prerequisites: AIFDE-V2-U008

Use existing React/TypeScript skills to build a minimal question/answer screen with loading, empty, cancel/error and result states; avoid visual-polish work unrelated to the workflow.

**Planned outputs (not existing files):** CoverStory UI; frontend state tests; local screenshot

**Acceptance:**
- The UI calls the real backend and shows a real generated answer.
- A failed/canceled request cannot leave the UI falsely showing success.
- Explain the frontend/backend contract and one accessible error-state choice.

**Support/buffer:** Fix integration or accessibility defects; no extra design system.

## AIFDE-V2-U010: Structured output without trusting it
Primary 90 min + 30 min support. Prerequisites: AIFDE-V2-U008

Define an answer schema, validate actual model output, and implement one bounded repair/retry or explicit failure path; compare success and malformed output.

**Planned outputs (not existing files):** answer schema; output validator; malformed-output tests; real structured-output sample

**Acceptance:**
- Malformed or missing fields are rejected rather than silently coerced into a valid answer.
- Retries are capped and observable; failure is still possible.
- Explain that schema validity does not establish factual correctness.

**Support/buffer:** Review JSON/schema/validation behavior using the actual selected SDK or plain API.

## AIFDE-V2-U011: Versioned small corpus and SQL metadata
Primary 90 min + 30 min buffer. Prerequisites: AIFDE-V2-U003, AIFDE-V2-U004

Create or use ten licensed public/synthetic Markdown/text documents (roughly 150–300 words each); persist IDs, source, content hash and version in SQLite with idempotent ingestion.

**Planned outputs (not existing files):** data/corpus manifest; metadata schema; ingestion idempotency tests

**Acceptance:**
- Reingesting unchanged input does not duplicate documents.
- A changed document produces the intended version/update behavior.
- Run and explain a SQL query that joins or aggregates document/ingestion metadata; record licensing/source.

**Support/buffer:** Repair schema/data bugs; keep PDF/OCR and private uploads out of month one.

## AIFDE-V2-U012: Chunking with source provenance
Primary 90 min + 30 min support. Prerequisites: AIFDE-V2-U011

Implement a small deterministic chunker that preserves document/version/source offsets or headings; set a measured context-compatible chunk budget and test boundary behavior.

**Planned outputs (not existing files):** chunker; chunk manifest; chunk/provenance tests

**Acceptance:**
- Every chunk maps back to one source document/version and actual text.
- Empty, very short and over-limit documents have tested behavior.
- Explain the retrieval/context tradeoff; do not claim one chunk size is universally optimal.

**Support/buffer:** Study tokens versus characters and the effect of overlap using one concrete example.

## AIFDE-V2-U013: Real embeddings and vector intuition
Primary 90 min + 30 min support. Prerequisites: AIFDE-V2-U012

Run an actual pretrained embedding model over chunks; validate dimensions/finite values and examine similarities on a few related/unrelated pairs.

**Planned outputs (not existing files):** embedding adapter; embedding model metadata; embedding sanity-check output

**Acceptance:**
- Vectors come from the real recorded model, not random/hash/mock embeddings.
- Dimensions, normalization assumptions and model revision are recorded.
- Explain dot product/cosine similarity and why similarity alone does not prove answerability.

**Support/buffer:** Work one small vector/similarity example by hand and check it in code.

## AIFDE-V2-U014: Evaluation cases before retrieval tuning
Primary 90 min + 0 min none_visibility_scheduled_weekly. Prerequisites: AIFDE-V2-U011, AIFDE-V2-U012

Create/review 30 small question cases with expected evidence and answerability labels; separate 20 development cases and 10 held-out cases before tuning.

**Planned outputs (not existing files):** evals/questions-dev.jsonl; evals/questions-heldout.jsonl; evals/rubric.md

**Acceptance:**
- Development: 12 answerable, 4 unanswerable and 4 adversarial/instruction-conflict cases; held-out: 6 answerable, 2 unanswerable and 2 adversarial cases.
- Each answerable case names supporting document/chunk evidence; labels and limitations are reviewed.
- Held-out cases are not used for tuning; an independent reviewer/author is preferable, not claimed if absent.

**Support/buffer:** The evidence note is scheduled weekly in the daily schedule, not charged twice to this unit.

## AIFDE-V2-U015: Non-neural retrieval baseline
Primary 90 min + 30 min buffer. Prerequisites: AIFDE-V2-U012, AIFDE-V2-U014

Implement SQLite FTS/BM25 or another documented keyword baseline over the same chunks; run retrieval evaluation on development answerable questions.

**Planned outputs (not existing files):** keyword retriever; artifacts/keyword-dev-results.json; retrieval metric tests

**Acceptance:**
- Results expose source/chunk IDs and ranking, not fabricated citations.
- Recall@k and/or MRR use the correct answerable-case denominator and declared evidence mapping.
- Save all development results, including misses; no held-out tuning.

**Support/buffer:** Repair baseline or labels; do not add hybrid/reranking before measuring the baseline.

## AIFDE-V2-U016: A genuine persisted vector database
Primary 90 min + 30 min buffer. Prerequisites: AIFDE-V2-U013, AIFDE-V2-U015

Create a real persisted vector collection using Qdrant local mode or another current supported vector database; upsert chunk vectors/metadata and perform actual nearest-neighbor queries.

**Planned outputs (not existing files):** vector-store integration; persistence/upsert tests; real query receipt

**Acceptance:**
- Restarting the process preserves the intended collection/data.
- Upsert/version handling and the configured distance metric match the embedding contract.
- A real vector-store call returns meaningful chunk IDs; an in-memory mock or NumPy-only search does not pass this gate.

**Support/buffer:** Resolve actual DB/model integration problems; server/container mode can wait until P2.

## AIFDE-V2-U017: Retrieval comparison and inspection
Primary 90 min + 30 min support. Prerequisites: AIFDE-V2-U014, AIFDE-V2-U015, AIFDE-V2-U016

Expose a /retrieve path; evaluate keyword versus vector retrieval on the same development set; inspect misses and make one evidence-based choice.

**Planned outputs (not existing files):** /retrieve endpoint; artifacts/retrieval-comparison.md; per-case results

**Acceptance:**
- Declare k, model/config, metric definitions and actual sample counts.
- Do not compare unlike raw score scales as if calibrated.
- Explain one retrieval miss and one next experiment; an improvement is measured, not guaranteed.

**Support/buffer:** Study precision/recall/ranking and small-sample limitations using these actual results.

## AIFDE-V2-U018: Grounding prompt and trustworthy source plumbing
Primary 90 min + 30 min buffer. Prerequisites: AIFDE-V2-U010, AIFDE-V2-U017

Build a context envelope that separates instructions from untrusted retrieved text; require answers to cite supplied source IDs, and validate all cited IDs against retrieved evidence.

**Planned outputs (not existing files):** grounding prompt; context builder; citation-ID validator/tests

**Acceptance:**
- Only supplied source IDs can appear as valid citations.
- No secret or unrelated private context is placed in the prompt.
- Explain that ID validity alone does not prove that a cited passage supports a claim.

**Support/buffer:** Debug prompt/context limits and citation plumbing; do not hide unsupported claims.

## AIFDE-V2-U019: First genuine generative RAG path
Primary 90 min + 30 min buffer. Prerequisites: AIFDE-V2-U009, AIFDE-V2-U018

Connect question → real retrieval → actual generative model → validated answer/citations; render source snippets from the original corpus in the UI.

**Planned outputs (not existing files):** /ask RAG path; source-linked UI; real end-to-end RAG trace

**Acceptance:**
- A real user query retrieves actual stored vectors and produces a new model-generated answer grounded in returned text.
- Displayed snippets come from corpus storage, not a model-invented URL or quote.
- Save one successful and one failing real trace, including retrieval and generation stages.

**Support/buffer:** Repair end-to-end wiring; this gate cannot be replaced by extractive/template output.

## AIFDE-V2-U020: Abstention and user-facing limits
Primary 90 min + 30 min support. Prerequisites: AIFDE-V2-U019

Implement an explicit insufficient-evidence/clarification path and evaluate it on development unanswerable cases; distinguish retrieval weakness from answer-generation weakness.

**Planned outputs (not existing files):** answerability/abstention behavior; development review; UI limitation text

**Acceptance:**
- Missing evidence does not force a confident answer.
- Tests cover empty retrieval and unsupported requested claims.
- Any score threshold is documented as a heuristic, not proof; manually review actual generated claims against evidence.

**Support/buffer:** Review calibration/uncertainty and why model confidence is not a truth score.

## AIFDE-V2-U021: Failure taxonomy and traces
Primary 90 min + 0 min none_visibility_scheduled_weekly. Prerequisites: AIFDE-V2-U019, AIFDE-V2-U020

Run the development evaluation and classify failures as retrieval, unsupported generation, wrong citation, schema, latency or operational failure; connect the stages using trace IDs.

**Planned outputs (not existing files):** artifacts/rag-dev-eval.json; docs/failure-taxonomy.md; redacted trace example

**Acceptance:**
- Every scored case has a result and failure/review reason, not only a summary percentage.
- Trace includes model/config and stage timing; sensitive content is not logged by default.
- Select one next repair based on observed failures; do not tune on held-out data.

**Support/buffer:** The evidence note is scheduled weekly in the daily schedule, not charged twice to this unit.

## AIFDE-V2-U022: Input, data and secret boundaries
Primary 90 min + 30 min support. Prerequisites: AIFDE-V2-U011, AIFDE-V2-U019

Keep the prototype local-only/single-user; enforce allowed file types, size/path bounds and safe rendering; add secret/data-handling checks and a short threat model.

**Planned outputs (not existing files):** docs/threat-model.md; input/path/rendering tests; secret-scan result

**Acceptance:**
- Untrusted filenames/path traversal and oversized/invalid inputs are rejected.
- Model output is not executed or rendered as unsafe HTML.
- State that authentication, multi-tenancy and public exposure are not yet established; no production-security claim.

**Support/buffer:** Study actual trust boundaries and least privilege for the planned next phase.

## AIFDE-V2-U023: Adversarial documents and regression tests
Primary 90 min + 30 min support. Prerequisites: AIFDE-V2-U018, AIFDE-V2-U020, AIFDE-V2-U022

Add controlled malicious-document instructions and misleading evidence cases; verify deterministic access boundaries, inspect model behavior and turn observed defects into regression tests.

**Planned outputs (not existing files):** evals/adversarial-dev.jsonl; security/regression tests; observed-behavior notes

**Acceptance:**
- Retrieved text cannot authorize tools, filesystem access or external actions; this prototype has no such tool permission.
- No credentials or hidden real secrets exist in model context; use synthetic canaries for tests.
- Record any prompt-following/unsupported-answer failures honestly; passing a few cases is not universal injection resistance.

**Support/buffer:** Read current official guidance for the chosen runtime/SDK and explain prompt-level versus enforced controls.

## AIFDE-V2-U024: Reproducible build and CI boundary
Primary 90 min + 30 min buffer. Prerequisites: AIFDE-V2-U022, AIFDE-V2-U023

Add one clean setup/test command, CI for deterministic tests, and a container build/smoke test for the API; document the host model runtime and persisted local vector data.

**Planned outputs (not existing files):** setup/test commands; CI configuration; API container smoke receipt; deployment-prep notes

**Acceptance:**
- A fresh environment runs offline tests without real-provider credentials.
- The API container starts and passes its stated health smoke test.
- Separate this limited container check from the real local RAG receipt; it is not a cloud or full production deployment.

**Support/buffer:** Fix reproducibility defects; full hosted/container orchestration belongs in P2.

## AIFDE-V2-U025: Bounded failure and recovery behavior
Primary 90 min + 30 min buffer. Prerequisites: AIFDE-V2-U008, AIFDE-V2-U019, AIFDE-V2-U024

Test provider unavailable, slow response, malformed response and canceled request paths; cap retries/deadlines and prevent accidental duplicate work where applicable.

**Planned outputs (not existing files):** failure-injection tests; retry/deadline configuration; recovery notes

**Acceptance:**
- Every failure terminates within a declared budget or is explicitly reported as a defect.
- No unbounded retry loop; user-visible state and trace show failure/cancellation.
- At least one actual runtime outage/recovery is observed; simulated failures are labeled test injections.

**Support/buffer:** Repair the most important reliability failure; do not add a queue solely for architecture appearance.

## AIFDE-V2-U026: Honest latency and resource baseline
Primary 90 min + 30 min buffer. Prerequisites: AIFDE-V2-U021, AIFDE-V2-U025

Run a small reproducible local benchmark (for example 12 requests with declared concurrency); record retrieval/generation/end-to-end latency, model settings and machine resource limitations.

**Planned outputs (not existing files):** benchmark script; artifacts/local-performance.json; docs/performance-baseline.md

**Acceptance:**
- Report actual sample size, concurrency, warm/cold conditions and latency values.
- Cost/token fields are actual or explicitly unavailable; no invented cloud/API estimate is presented as measured.
- Explain one quality/latency/resource tradeoff and the limits of this small local test.

**Support/buffer:** Resolve benchmark defects; optimize only if measured evidence justifies it.

## AIFDE-V2-U027: Discovery, scope change and handoff draft
Primary 90 min + 30 min buffer. Prerequisites: AIFDE-V2-U001, AIFDE-V2-U021, AIFDE-V2-U026

Revisit the problem brief in a short stakeholder role-play or authorized peer review; ask data/access/value questions, identify one useful scoped change and draft a runbook/handoff.

**Planned outputs (not existing files):** docs/discovery-notes.md; docs/change-request.md; docs/runbook.md

**Acceptance:**
- Label role-play, peer feedback and actual user feedback accurately.
- State baseline, proposed success measure, non-goals, data restrictions and go/no-go limits.
- No invented business savings/customer adoption; one tradeoff is explained to a nontechnical audience.

**Support/buffer:** Repair scope or documentation gaps; external outreach is not automatically authorized.

## AIFDE-V2-U028: Independent ownership check
Primary 90 min + 0 min none_visibility_scheduled_weekly. Prerequisites: AIFDE-V2-U019, AIFDE-V2-U023, AIFDE-V2-U025

Without receiving a finished solution, trace a question end-to-end and implement one small reviewer-selected change or diagnose an injected defect; add a regression test.

**Planned outputs (not existing files):** ownership-check attempt; minimal patch/test; self/reviewer rubric

**Acceptance:**
- Explain retrieval, context, generation, citation validation and failure handling in your own words.
- The attempt shows learner-owned reasoning/code, not a replay of supplied solution text.
- Record help used and unresolved gaps; a failed check becomes remediation rather than a completion badge.

**Support/buffer:** The evidence note is scheduled weekly in the daily schedule, not charged twice to this unit.

## AIFDE-V2-U029: Held-out check and acceptance audit
Primary 90 min + 30 min buffer. Prerequisites: AIFDE-V2-U014, AIFDE-V2-U021, AIFDE-V2-U023, AIFDE-V2-U025, AIFDE-V2-U028

Freeze the current configuration, run the ten held-out cases once for this release, score against the rubric and audit mandatory capability/security evidence.

**Planned outputs (not existing files):** artifacts/heldout-release-eval.json; artifacts/p 1-gate-audit.md; remediation list

**Acceptance:**
- For this narrow prototype, proposed release target: at least 5/6 answerable cases correct and supported, 2/2 unanswerable cases not fabricated, and no deterministic access-boundary failure; report exact counts and adversarial behavior.
- All citations resolve; unsupported-claim failures are visible. The tiny dataset does not establish enterprise safety/quality.
- A failed gate remains failed; remediate on development cases and use a fresh held-out release set later rather than tuning the same test cases.

**Support/buffer:** Fix non-model test/audit defects or schedule failed-quality remediation; do not silently lower thresholds.

## AIFDE-V2-U030: Clean-run demo and honest first checkpoint
Primary 90 min + 30 min buffer. Prerequisites: AIFDE-V2-U024, AIFDE-V2-U026, AIFDE-V2-U027, AIFDE-V2-U029

Run setup/tests from a clean environment/profile; perform a real RAG smoke request and a five-minute demo with source evidence plus a failure/abstention path; finalize the concise case-study draft.

**Planned outputs (not existing files):** README setup/demo; clean-run receipt; demo recording/script; P1 status/evidence map

**Acceptance:**
- Another person can follow the instructions where available; otherwise label the verification self-run.
- All required real capabilities have receipts, or P1 remains blocked/incomplete with exact gaps.
- Clearly call this a local single-user prototype; cloud deployment, real-customer production and certification are not claimed.

**Support/buffer:** Close reproducibility/ownership gaps; do not advance solely because Day 30 arrived.
