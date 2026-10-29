# Draft M2.1 detailed continuation — actual learning Days 42–62

**Status: proposed scaffold only. This is not an active learner assignment, an accepted change to the milestone contract, or completion evidence.** The learner must not start M2.1 until P1's actual generation, actual embeddings, persistent vector retrieval, and learner-ownership prerequisite is independently cleared. Current model/runtime access remains blocked. Deterministic witnesses below validate mechanics only.

This proposal maps only the accepted `AIFDE-V2-M2.1: Reliable retrieval and evaluation` outcome contract. It does not reorder the roadmap. The outcome envelope remains **25 substantive hours + 4 genuine buffer hours = 29 AI hours**, excluding the pre-existing weekly visibility allowance. No cloud, paid API, credential, account, data purchase, or model-download action is included.

The proposed work covers the contract's **versioned ingestion, upsert, deletion, and provenance**; keyword/vector comparison; at least 100 labeled cases with documented development/held-out, answerability, and adversarial coverage; and applied Python/SQL/statistics exercises. It compares the unchanged baseline and one proposed change on the same development cases, reports exact counts, regressions, and failure taxonomy, checks obsolete-evidence removal, and predeclares a useful-workflow criterion before held-out release evaluation. These are design mappings to accepted deliverables; the actual dataset and evidence do not yet exist.

## Capacity and recurring study blocks

The canonical machine-readable proposal is [`draft_schedule.json`](../../../ai/fde_v2/m21/draft_schedule.json). Days 42–62 are relative learning-session indices, not calendar dates or deadlines.

- Each nominal study day remains 60 minutes DSA + 30 minutes role/AWS + 90 minutes AI = 180 total. If DSA takes 75 or 90 minutes, AI reduces to 75 or 60; carry unfinished work forward and recalculate actual end dates.
- The schedule allocates 1,500 substantive M2.1 minutes, 240 genuine buffer minutes, 90 minutes visibility within the AI blocks on Days 42/49/56, and 60 unassigned AI minutes on Day 62. The 60 minutes on Day 62 are outside M2.1 and cannot be silently reused.
- Six role/AWS blocks (Days 47/48, 54/55, 61/62) continue the twice-per-seven-day AWS syllabus cadence. The other 15 blocks are role-interview practice. Certifications add no time.
- A 90-minute substantive block is 20 minutes assigned study + 10 worked example + 50 learner task + 10 evidence. A 60-minute substantive block is 10 + 10 + 30 + 10. Buffer minutes repair/retest work already scheduled; they do not add features.
- The curriculum is conditional on capacity pilots. If measured task capacity cannot fit the assigned work, preserve the evidence and seek a reflow rather than extending study hours, reducing evaluation quality, or taking time from DSA/interview. The proposed hours do not guarantee completion by Day 62.

## Exact references for the assigned concepts

Read only the named headings; versions and local package pins remain governed by repository lockfiles. These references support the proposed lessons but do not change the accepted milestone outcomes.

| Ref | Source and exact section |
|---|---|
| R1 | [SQLite Transactions](https://www.sqlite.org/lang_transaction.html): “2. Transactions” and “2.3. Implicit versus explicit transactions”. SQLite transactions do not include Qdrant. |
| R2 | [Qdrant Points](https://qdrant.tech/documentation/manage-data/points/): “Point IDs”, “Upload Points”, “Idempotence”, “Delete Points”, “Retrieve Points”. Reuploading a point ID does not prove older point IDs were removed. |
| R3 | [SQLite FTS5](https://www.sqlite.org/fts5.html): “1. Overview of FTS5”, “3.1. FTS5 Strings”, “5.1.1. The bm25() function”, “4.4.4. External Content Table Pitfalls”. SQLite BM25 best-first order is ascending. |
| R4 | [Sentence Transformers Evaluation](https://www.sbert.net/docs/package_reference/sentence_transformer/evaluation.html): `InformationRetrievalEvaluator` queries/corpus/relevant_docs and MRR/Recall/NDCG descriptions. Use to read conventions; no evaluator package/model is required. |
| R5 | [scikit-learn GroupShuffleSplit](https://scikit-learn.org/stable/modules/generated/sklearn.model_selection.GroupShuffleSplit.html): description, `groups`, `test_size`, `random_state`, and worked Example. Its split fraction refers to groups. Do not resample a held-out set until outcomes improve. |
| R6 | [Python 3.12 statistics](https://docs.python.org/3.12/library/statistics.html): `median`, `quantiles`, “Statistics for samples”. This draft specifically proposes nearest-rank p95 rather than silently changing estimators. |
| R7 | [SQLite SELECT](https://www.sqlite.org/lang_select.html): “2.1. Determination of input data”, “2.3. WHERE clause filtering”, “2.4. Generation of the set of result rows”. Use for joining version manifests and evaluation rows. |

## Proposed daily progression

“Practice” means a deterministic worked example while the P1 gate remains blocked. The listed actual model operations cannot pass via a fixed embedding/generator, stored output, test double, extracted text, or local plumbing alone. Visibility is inside the AI total.

| Day | AI allocation | Study/example/task/evidence focus | Proposed learner-owned output |
|---:|---|---|---|
| 42 | 60 substantive + 30 visibility | Review P1 receipts; R4 metric inputs; hand-score two supported queries and one unsupported query. | Entry-gate audit, workflow/non-goals/rubric, and one measurable usefulness criterion. Visibility is private unless separately authorized. |
| 43 | 90 substantive | R5 group split; show how paraphrases/old versions leak when split by row. Run a development-only annotation capacity pilot. | Draft fixed 20-family split and time six development-only annotation items. |
| 44 | 90 substantive | R4 relevant-document judgments; distinguish answerable-but-hard from unsupported. | Verify development labels and record disagreements using actual source spans. |
| 45 | 90 substantive | R7 manifest join and group-level count. | Audit 100 draft cases for provenance, labels, exact split, duplicates and family leakage. Held-out key stays separate from tuning. |
| 46 | 90 substantive | R1 explicit transaction and R2 stable point identity; trace a document revision that removes old chunks. | Plan add/replace/remove sets and stable IDs; old version remains authoritative until commit. |
| 47 | 90 substantive | R2 upload/idempotence; replay one fixed update twice. | Stage new vectors without changing active SQL head; inject staging failure and prove old head remains active. |
| 48 | 90 substantive | R2 delete; trace tombstone, candidate filtering and physical cleanup separately. | Tombstone first; stale candidates cannot surface; verify vector deletion, with failure cleanup visibly pending. |
| 49 | 60 substantive + 30 visibility | R7 join/count for orphaned chunks and versions. | Reconcile active SQL versions with persisted Qdrant IDs after close/reopen; private visibility note. |
| 50 | 90 substantive | R4 hand-calculate Recall@3 and MRR; R6 median and nearest-rank p95. | Implement/verify metric edge cases, duplicates, empty relevant sets and failed requests. |
| 51 | 90 substantive | R3 FTS5 query and BM25 ordering; compare to the unchanged P1 keyword path. | Run and preserve the actual baseline per case, including no-result behavior. |
| 52 | 90 substantive | R4 inputs and existing P1 embedding receipt; study a semantically similar wrong hit. | Run real vector retrieval on the identical development cases, or record BLOCKED if P1 artifacts are unavailable. |
| 53 | 90 substantive | Score supported/unsupported examples with the frozen rubric; R7 join a failure table. Pilot four development-only paired system runs. | Record human source judgments, per-case outcomes, and observed task capacity. |
| 54 | 90 substantive | R6 distribution caveats; R7 failure aggregation. | Exact same-case comparison and freeze the usefulness criterion from development evidence, before held-out exposure. |
| 55 | 90 substantive | Work one bounded retrieval change using R2 or R3 only where justified. | Compare unchanged baseline and one proposed change on the same development cases. Hybrid/reranking only if a measured failure justifies it. |
| 56 | 60 substantive + 30 visibility | Demonstrate how one config checksum change invalidates a frozen run. | Freeze code/config/corpus/model/case split/rubric/criterion digests; private status note. |
| 57 | 90 substantive | Check the frozen run instructions before any held-out access. | Run one real held-out baseline and candidate on identical cases. No operator answer rehearsal or selective retries. |
| 58 | 90 substantive | Apply the frozen rubric to all results, including errors/timeouts. | Complete scoring/adjudication only if the Day 43/53 capacity pilots support it; otherwise record carryover and keep the gate blocked. |
| 59 | 60 substantive + 30 buffer | Review taxonomy and evidence links. | Finish failure analysis and explanation; buffer repairs documentation/tests only, never tunes exposed held-out data. |
| 60 | 90 buffer | No new concept. | Genuine remediation/retest capacity. Preserve any exposed held-out set. |
| 61 | 90 buffer | No new concept. | Continue the highest-value unresolved repair, or stop early. No feature work is required to consume buffer. |
| 62 | 30 buffer + 60 unassigned | No new concept. | Final readback in the 30-minute buffer. The other 60 AI minutes are explicitly outside M2.1 and unassigned. |

## Proposed case schema and split (not yet a verified dataset)

The proposal targets 100 cases: 20 scenario families × 5 records, with 12 families/60 development cases and 8 families/40 held-out cases. Held-out has 24 answerable and 16 unanswerable cases; at least 10 of the 100 cases carry an adversarial tag (tags may overlap). The code validates this proposed shape, but no source-verified 100-case dataset is supplied here. Draft or unverified rows are ineligible.

Each row records `case_id`, `scenario_family`, source document/version/hash, question, answerability, atomic facts or abstention reason, evidence spans/current relevant chunk IDs, category tags, public/synthetic origin, author, reviewer, annotation status, and split. The fixed split manifest must be group-disjoint and frozen before outcome measurement. Indexed corpus content may be common, but evaluation questions/answers cannot leak across splits. The case count alone is not quality evidence.

Development labels are the learner's tuning material. Held-out source labels need separate source verification, key separation and an access/exposure log before tuning starts. A case authored by the assistant or a model is not presumed ground truth. If the held-out key is visible to the tuning learner or source verification is missing, mark the release set invalid/blocked; do not claim blind evaluation.

## Proposed release criterion and capacity check

The comparator is the unchanged P1 keyword search and existing excerpts/source IDs/no-result behavior. The candidate is real model-backed retrieval/generation. Same task, corpus and case set; no manually composed baseline answer and no quietly upgraded baseline. If that comparison is unfair to actual P1 behavior, settle it from development evidence before freeze.

Proposed numeric thresholds (design judgments, not accepted facts or benchmarks): at least 22/24 supported answerable cases; at least 15/16 unsupported cases abstain without fabricated material claims; zero observed severe access/obsolete evidence/injection failures; at least four additional successes over the same 40-case non-AI baseline; and nearest-rank p95 end-to-end latency ≤30 seconds on the frozen local setup. A failure/timeout is a failed task and receives the declared 60-second latency penalty. Report raw per-case timings and failure counts. No dropped failures, selective retries, best-run selection, or fast-error advantage.

Before freeze, record development-only capacity pilots: Day 43 six annotation items; Day 53 four paired baseline/candidate outputs plus one failure. The corresponding learner task capacity is 150 hands-on minutes across Days 43–45 and 100 across Days 57–58 under the proposed study/example/evidence template. Use measured conservative rates plus fixed work and adjudication. If the work does not fit, revise and review the proposal before activation; do not silently spend buffers, exceed the 29-hour envelope, or take DSA/interview time.

The default local run proposal is concurrency one with a 60-second timeout, one development-only warmup per system, no held-out cache warmup, and exact run/config/data/rubric digests. Measure request acceptance to complete output, report model loading separately, and preserve every failed case. No actual run exists yet.

## Evidence tiers and fail-closed gate

1. **Deterministic mechanics:** schema and split invariants, lifecycle failure/restart behavior, metrics, exact joins, frozen config and mutant rejection. Test fixtures are synthetic and do not count as model evidence.
2. **Local lifecycle integration mechanics:** `python3 -m unittest ai.fde_v2.tests.test_m21_feature_integration` exercises the additive HTTP API, persisted SQLite/Qdrant, staged update failure, reopen and tombstone filtering with a fixed embedding fixture and explicit reference adapter. This is not actual pinned embedding/generation evidence. Actual model-backed per-case development/held-out responses, output digests and measured timings remain blocked until P1 is cleared and artifacts are available.
3. **Independent human evidence:** source-verified labels, per-case grounding judgments, frozen criterion and review of failures. A validator can check structure, not whether a reviewer or model truly performed the claimed work.

Release status remains `BLOCKED` unless P1 is independently cleared, the data/capacity/freeze requirements hold, real candidate and baseline runs cover every frozen case, and independent review accepts the evidence. Synthetic controls, code presence, green CI and elapsed days can never produce an M2.1 pass. The proposal gate returns at most `ELIGIBLE_FOR_INDEPENDENT_REVIEW`, never a learner completion status.

## Implementation map and limitations

- Learner TODO seams and instructor reference mechanics: `ai/fde_v2/m21/`.
- Assistant-owned staged-version ledger and local vector-store inspection: `ai/fde_v2/m21/ledger.py` and the additive version-delete/list methods in `ai/fde_v2/coverstory/vector_store.py`. `ai/fde_v2/m21/workflow.py` and `m21/api.py` provide an explicit versioned publish/search/delete feature path that calls the selected lifecycle adapter; it is not silently substituted for the original P1 `/ask` pipeline. Its SQLite/Qdrant tests use fixed vectors only and do not satisfy real embedding or P1 evidence.
- Proposed learner selectors and default TODOs: `ai/fde_v2/learner_acceptance/m21_acceptance.py` and `ai/fde_v2/m21/learner.py`. They are not part of the active first-30 unit commands.
- Instructor-only local mechanics witnesses: `python3 -m unittest scripts.test_fde_v2_m21_draft ai.fde_v2.tests.test_m21_draft` and `PYTHONPATH=<pinned local dependencies> M21_CANDIDATE=ai.fde_v2.m21.reference python3 -m unittest ai.fde_v2.tests.test_m21_feature_integration`. The app workflow now selects the assistant-supplied local reference adapter by default, so the feature is usable without learner FE/BE implementation. The explicit `M21_CANDIDATE` setting still tests the learner TODO separately and never silently changes that learner assessment. This fixed-vector mechanics run does not count as learner progress.
- This remains a conditional draft and does not change the accepted outcome contract or learner status. No training labels or held-out gold have been fabricated as verified. Later M2.2–M5.3 conditional detailed guides and schedules are now present in this continuation; standalone learner day pages for Days 42–81 are still missing. The separate M2.2 guide is at [`m23-m32-conditional-guide.md`](m23-m32-conditional-guide.md) and covers the M2.2 plan plus subsequent milestone detail; those authored plans do not clear prerequisites or learner evidence.
