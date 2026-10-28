# Days 15–28 continuation repair verification

**Implementation commit:** `de1341ebc002114fe8799157222c3bac1e0d63e1`  
**Parent:** `a1031108862c46b76b85040919660e73736631e4`  
**Branch:** `work/aws-fde-v24-continuation`  
**Publication:** local only; no push, merge, board write, model request, or learner-status change.

## Review findings and corrections

| Finding | Correction | Verification path |
|---|---|---|
| U020 abstention was not connected to application behavior | `CoverStoryPipeline.answer` now checks the learner's abstention seam before loading/calling generation; empty or below-threshold evidence returns a no-evidence answer with no citations. `/ask` serializes this as a normal abstention response. | `ContinuationAcceptance.test_u020_abstention_boundaries`; `LocalPipelineWitness.test_api_abstains_end_to_end_without_loading_generator`; reference selector and behavior-mutant witness. |
| Version updates left old Qdrant points searchable | Ingestion deletes the document's old vector set before embedding/indexing the replacement. On embedding failure, superseded vectors are unavailable; retrying the same stored version rebuilds its vectors. | `LocalPipelineWitness.test_version_replacement_removes_old_vector_evidence`; `LocalPipelineWitness.test_failed_vector_replacement_fails_closed_and_retry_repairs`. |
| U011 claimed ten-record/version aggregate acceptance without exercising it | Added a checked-in SQL query and Python runner. The learner selector ingests all ten CC0 fixtures, checks exact IDs/metadata and the independent version counts `(1,10)`, updates one document, then checks `(1,9),(2,1)` using both SQLite and the documented runner. | `ContinuationAcceptance.test_u011_versioned_ingestion`; `scripts.test_fde_v2_sprint2`. |
| `/ask` ignored its requested retrieval limit | API forwards `RagRequest.limit` to the pipeline, which forwards it to vector retrieval. | API request test plus pipeline retrieval-call assertion in the end-to-end empty-evidence test. |
| Retrieval construction eagerly loaded generation weights | The default RAG factory now receives the generator factory, and the pipeline invokes it only after evidence clears abstention. | `ContinuationApiTests.test_default_rag_factory_receives_lazy_generator_without_loading_it`; end-to-end API abstention test asserts zero generator loads. |

The learner TODO module was not changed. Reference implementations and fixed fixtures remain instructor/test-only and are not learner completion evidence.

## Checks

- Focused continuation selectors, reference/mutation witnesses, API tests, and SQLite/Qdrant pipeline tests: **21 passed**. Command:
  `PYTHONPATH=/tmp/fde-v2-qdrant FDE_V2_CANDIDATE=ai.fde_v2.coverstory.reference python3 -m unittest ai.fde_v2.learner_acceptance.test_continuation_units ai.fde_v2.tests.test_continuation_witnesses ai.fde_v2.tests.test_continuation_api ai.fde_v2.tests.test_local_pipeline_witness`
- Continuation contract tests: **8 passed**. Command: `python3 -m unittest scripts.test_fde_v2_sprint2`.
- Repository script test discovery: **208 passed**. Command: `python3 -m unittest discover -s scripts -p 'test_*.py'`.
- Python compile check passed: `python3 -m compileall -q ai/fde_v2/coverstory ai/fde_v2/learner_acceptance ai/fde_v2/tests scripts/fde_v2_version_counts.py`.
- `git diff --check` passed before commit.

The Qdrant package was unavailable in the base runtime, so the already-present local package tree `/tmp/fde-v2-qdrant` was added to `PYTHONPATH`; no dependency was downloaded or installed for these checks. FastAPI emitted a Starlette `httpx` deprecation warning; tests passed.

## Limits

These results validate deterministic reference, test-fixture, SQLite and local Qdrant behavior only. No embedding or generation model was loaded and no real inference ran. The pre-existing model-access restriction remains unresolved; no access attempt was made during this repair. GitHub CI was not run because this candidate remains unpublished. Learner-owned seams are still TODO and all learning status remains not started.
