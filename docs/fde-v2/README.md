# AWS FDE v2.4 learning roadmap

This is the accepted AWS-only, interview-first plan. **No learner days are complete.** A learning-day number advances only when that session is attempted; dates do not automatically change learner status. Sprint 1 covers actual learning Days 1–14. The separate continuation branch has authored Days 15–41 and is published for review only; it is not merged to main. U011 starts as a partial slice on Day14 and continues on Day15. U021 starts on Day28 and ends its scheduled selector mechanics on Day29; real development evaluation remains blocked while model weights are unavailable. U022 runs on Days29–30. Day30 is only the fixture-authoring portion of U023; deterministic execution/regression is scheduled on Day31, but actual generated behavior is still model-gated. U024–U030 have explicit closeout lessons and evidence tooling. No unit or P1 completion is inferred from authored pages or synthetic witnesses. U013 embeddings and U019 generated RAG remain unverified until verified local model artifacts are available.

## Start with actual learning Day 1

Use Python 3.12 and the checked-in fictional corpus. This CPU-only worked example needs no model weights or third-party Python dependency:

```sh
python -m ai.fde_v2.coverstory.day01_baseline
```

It reads the synthetic JSON manifest, loads ten fictional records into in-memory SQLite FTS5, and ranks lexical matches. “What is the individual deductible?” starts with `northstar-deductible`. This is instructor-owned keyword retrieval, not generated text, embeddings, RAG or learner completion. Day 1's own task is the baseline/problem brief; see [Day 1](days/day-01.md).

## Run the supplied Sprint 1 application

Use Python 3.12. This installs pinned CPU-capable local dependencies; it makes no cloud or model-provider calls:

```sh
python3 -m venv .venv-fde-v2
source .venv-fde-v2/bin/activate
python -m pip install --index-url https://download.pytorch.org/whl/cpu 'torch==2.14.1'
python -m pip install -r requirements-fde-v2-sprint1.txt
```

In terminal 1, start the assistant-supplied loopback API:

```sh
python -m ai.fde_v2.coverstory.run_api
```

In terminal 2, from the repository root, install the frontend lockfile dependencies if needed, then launch the separate Vite entrypoint:

```sh
npm --prefix frontend ci
VITE_FDE_V2_API_TARGET=http://127.0.0.1:8765 npm --prefix frontend run dev
```

Open the Vite URL at `/fde-v2.html`. `VITE_FDE_V2_API_TARGET` proxies `/health`, `/documents`, `/generate`, `/retrieve` and `/ask` to the loopback API. Check `/health` and `/documents` without model weights. Until the pinned model is actually installed, `/generate` returns an explicit unavailable response; no mock or extractive fallback is used. Even after weights are available, the route uses the learner-owned `generate_text` seam and remains an explicit TODO failure until U008 is implemented; instructor reference code is never silently substituted. The runtime cannot perform real generation until the recorded Hub-access blocker is resolved and the separately documented pinned-file fetch is permitted. Startup, mocked tests and visible UI states are not inference evidence or learner completion.

These commands only start supplied infrastructure. General HTTP, SQLite, Qdrant and React plumbing are supplied; the learner owns bounded Python AI behaviors and evaluation. From Day 15 onward, the RAG panel exercises the supplied `/retrieve` and `/ask` routes. Tests using fixed vectors or a test double validate mechanics only and never count as real embedding or generated-answer evidence.

## Continuation branch: actual learning Days 15–41

Days 15–41 are authored on `work/aws-fde-v24-continuation` and published for review, not merged to main. In that worktree, install the additional local persistence and embedding packages (no model files are downloaded by this command):

```sh
python -m pip install -r requirements-fde-v2-sprint2.txt
```

After the learner completes U011, seed all ten synthetic documents and prepare versioned chunks:

```sh
python scripts/fde_v2_seed_corpus.py --limit 10
python scripts/fde_v2_prepare_chunks.py
python scripts/fde_v2_evaluate_retrieval.py --split dev
```

To inspect the current persisted document-version distribution, run
`python3 scripts/fde_v2_version_counts.py`. This executes the checked-in U011
query in `scripts/fde_v2_version_counts.sql` and reports observed SQLite rows;
it does not seed documents or imply learner completion.

U013 and U019 require actual verified local embedding and generation artifacts. The current model gate remains blocked; do not use test doubles, fixed vectors or extracted answers as completion evidence. With real artifacts separately available, the same-origin Vite proxy forwards `/retrieve` and `/ask` to the supplied loopback API. The RAG panel distinguishes client-side cancellation from server work cancellation; canceling the browser request does not stop generation already running on the server.

Run continuation contract and deterministic local witness checks from the repository root:

```sh
python -m unittest scripts.test_fde_v2_sprint2
FDE_V2_CANDIDATE=ai.fde_v2.coverstory.reference python -m unittest ai.fde_v2.learner_acceptance.test_continuation_units ai.fde_v2.tests.test_continuation_witnesses ai.fde_v2.tests.test_continuation_api ai.fde_v2.tests.test_local_pipeline_witness
cd frontend && RUN_FDE_V2=true npx vitest run
```

Fixed-vector and injected-pipeline tests verify mechanics only. They never satisfy real inference or model-backed retrieval.

## Sprint 1: actual learning Days 1–14

- [Daily pages, Days 1–14](days/)
- [Unit contracts U001–U010](units/)
- [U011 partial Day 14 slice](units/u011.md)
- [Role interview bank and rubric](role-bank.md)
- [AWS study map](aws-bank.md)
- [Verified Sprint 1 course and documentation map](sources/sprint1-course-map.md)
- [North Star and evidence-gated OKRs](governance/north-star-and-okrs.md)
- [Role benchmark sources](role-benchmarks.md)
- [Architecture and later outcome map](phase-map.md)
- [Model runtime verification status](runtime-verification.md)
- [Accepted v2.4 planning inputs](accepted/)

Each day's page assigns DSA60 + role/AWS bank30 + AI90 = 180 minutes. A 75-minute DSA revision reduces AI to75; a90-minute revision reduces AI to60. The actual Day14 allocation spends U010 support30 + the first U011 segment30 + a private evidence note30. U011 remains incomplete. In the separate continuation branch, actual Days 15–30 extend U011 and introduce U012–U023, with Day30 limited to the accepted U023 fixture-authoring slice. The pages and seams remain unpublished pending review. This is not a claim that the full accepted first-30 schedule, U023, or its milestones are complete.

## P1 closeout: actual learning Days 31–41

The accepted schedule assigns Days31–41 to deterministic security regressions, a minimal API container smoke, bounded failure/recovery practice, a real-only local latency attempt, role-play/handoff, independent learner ownership, one frozen held-out evaluation and an honest closeout. See [daily pages](days/), [unit contracts U023–U030](units/) and the [Days31–41 verification report](reports/days31-41-verification.md). No real model run has been produced by this repository update. The held-out and benchmark runners target loopback only, require the pinned model identity, and fail closed; their instructor schema tests use synthetic fixtures and are not performance or quality evidence. Container health is separate from actual RAG.

Days31–41 retain the accepted 60 DSA +30 role/AWS +90 AI budget. Day35 visibility is inside the AI total. Day41's remaining 60 minutes are reserve, not a new unit or feature task. The missing secret-scanner executable and missing pinned weights keep the associated acceptance gates unmet.

### Select the intended DSA learning position

The DSA plan lives in its separate repository. From that repository's root, select the nominal learning position explicitly:

```sh
python3 tools/dsa_operations.py plan --day N
python3 tools/dsa_operations.py queue --on-date ACTUAL_LOCAL_DATE --roadmap-day N
```

Here `N` is the intended nominal learning position; `ACTUAL_LOCAL_DATE` is the real local date on which you are checking the queue, so review-due status uses actual elapsed dates. Always pass `--roadmap-day N`: leaving it out infers a position from the prior October 27 calendar and can select the wrong item after a zero-completion restart. The verified restart check on October 28 selected LC121 with explicit `--roadmap-day 1`, while omission selected LC1. The CLI does not persist a position cursor. Record attempts only on the dates they actually happen; never backdate attempts or clear review history.

The daily role pages are the exact practice sequence. AWS work is inside the same 30-minute bank: Day5 course/syllabus audit; Day6 secure-access diagnostic; Day12 networking; Day13 data/storage protection. Current official SAA-C03 exam guide and its exact content-domain task headings are linked in [AWS bank](aws-bank.md). The connected catalog's Maarek course title, ID and listed curriculum durations are mapped in the [Sprint 1 course map](sources/sprint1-course-map.md); personal paid-video entitlement remains unverified. The connector did not return lesson URLs, so no guessed video number is assigned. Official AWS documentation is the fallback. Nothing here books an exam or claims a certificate.

## Deterministic checks

The learner seams deliberately remain TODOs. Run the independent instructor/reference witnesses separately:

```sh
FDE_V2_CANDIDATE=ai.fde_v2.coverstory.reference python -m unittest \
  ai.fde_v2.learner_acceptance.test_units
python -m unittest ai.fde_v2.tests.test_sprint1 ai.fde_v2.tests.test_sprint1_witnesses
npm --prefix frontend run test:fde-v2
```

To check a learner seam, run only its assigned selector from that unit contract. The default learner module raises `NotImplementedError` until the learner implements the task; that intentional red is not an infrastructure failure or completion. Test doubles validate request/response contracts only and never count as real model evidence.

## Real generation gate and current blocker

The generation candidate is the pinned Apache-2.0 Qwen2.5-0.5B-Instruct revision recorded in [`model_lock.json`](../../ai/fde_v2/coverstory/model_lock.json). `trust_remote_code` is false. The model weights are not present; the configured proxy returned HTTP403 on the Hugging Face metadata request before transfer. See [the verification record](runtime-verification.md). Do not interpret a successful unit test, API test double, extracted answer or template as a model pass. Real inference stays **unverified and blocked** until the network access issue is resolved; this release did not retry it.

The real-only smoke selectors are documented in [U006/U007](units/u006.md) and [runtime verification](runtime-verification.md). They fail closed if the pinned local artifacts are missing. Embedding inference, persistent vector search, full RAG and held-out evaluation are in the continuation scope. Deterministic local tests do not satisfy those real-model gates. AWS operation remains a later separately authorized gate.

## History and evidence

The accepted JSON/Markdown source contracts are retained byte-for-byte under `accepted/`; the receipt lists their SHA-256 values. The earlier 548-day model-building-first roadmap and prior assignment histories remain preserved as superseded/deferred. DSA stays in its separate repository; this checkout contains no DSA problems or solutions. Every new task remains Not started until the learner records actual evidence. No customer adoption, production use, cloud deployment, certification, interview pass or job readiness is asserted by this plan.

## Proposed M2.1 detail (not active)

[Draft Days 42–62 design](proposals/m21-detailed-days-42-62.md) maps a possible detailed sequence to accepted milestone AIFDE-V2-M2.1. It is a proposal only, does not advance the active calendar, and remains blocked until P1 actual generation, embeddings, persistent vector retrieval and learner ownership are independently cleared. Its mechanics tests use synthetic records and vectors; they are not actual model, labeling, or learner evidence.

## Conditional next milestone: M2.2

[`M2.2 detailed design and deterministic mechanics`](proposals/m22-detailed-days-62-82.md) maps the accepted security/identity/deployment-preparation outcome contract to a proposed Days62–82 sequence. It remains a conditional draft: M2.1's learner-evidenced data/evaluation baseline is a prerequisite, actual identity integration is not implemented, and no cloud/account setup or deployment has occurred. The M2.2 TODOs and synthetic witnesses do not advance learner status. The proposal preserves the accepted 29-hour AI envelope, weekly visibility accounting and all prior learner-day records.

## Conditional M2.3, M3.1 and M3.2 drafts

[The conditional guides, daily cards and reference mechanics](proposals/m23-m32-conditional-guide.md) cover accepted M2.3 real AWS/hosted-model delivery, M3.1 bounded local developer workflow, and M3.2 genuine MCP stdio plus exact-action approval. They are not activated learner days. M2.3 remains blocked pending scoped AWS/model authorization; M3.1 retains P1/M2.1/M2.2 learner prerequisites; M3.2 retains M3.1 and a trusted human-approval UI gate. Current official MCP SDK2.3.0 stdio mechanics run in a temporary workspace, but test-fixture elicitation is not human approval evidence. Daily cards label the AI minutes left unallocated and do not convert them into extra work.
