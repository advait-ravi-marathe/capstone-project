# CAP-40 — Preserve health shortlist order + source citations

**Day:** 001 · 14 Oct 2026 · **Phase:** 1 · **Role:** primary · **Primary:** CAP-40

**Source title:** `Day 001 · 14 Oct 2026 — Preserve health shortlist order + source citations (150m)`

**Description SHA-256:** `b9c9ea8e9a53cde75b4a67064f1ea1fa5f3ba0126e5788063007f1235a4687a0` (30,065 bytes supplied)

**Source snapshot:** final verified 600-card curriculum; verified curriculum snapshot 13 October 2026. Source curriculum batch SHA-256: `e96ae484e4c2c771e44d126f49127ee3ca005c766eb7abcb378e059a236e09de`.

## Readiness and timebox

This is the instructor scaffold for the supplied first-day contract. Learner logic remains TODO. Test-only reference witnesses check the positive first-seen pair contract and reject version-only identity and sorting defects. The separate learner acceptance run still stops at the TODO seam. The public synthetic discovery dependency now runs through the Catalog module port and is exercised through the actual Spring application and React browser shell; it does not implement the CAP-40 shortlist seam or private shortlist persistence. The selected cloud checkout completed Java/Maven compilation, instructor test discovery, and the linked Python fixture preflight; see [environment preflight](../../evidence/coverstory/CAP-40/environment-preflight.json). The assignment is `READY_TO_ATTEMPT`; this says the executor can run the harness, not that the learner seam is complete.

The source budget remains 95 minutes maximum for the capstone work. The timebox is `TIMEBOX_NOT_VALIDATED` because learner task duration has not been measured; record elapsed slices during the first attempt and stop or rescope explicitly at the cap. Missing timing rehearsal is tracked separately from environment readiness and does not prevent starting. The executor preflight supports `READY_TO_ATTEMPT`; it is not a completion or mastery claim. A rapid instructor test run is not evidence that the learner task fits.

| Scheduled activity | Minutes |
| --- | ---: |
| Capstone docs | 10 |
| Bounded capstone build | 55 |
| Capstone tests and actual evidence | 30 |
| Certification theory/source and questions | 30 |
| DSA retention | 15 |
| DSM/design reflection | 10 |
| **Daily maximum** | **150** |

The capstone envelope is **95 minutes maximum**, including the linked CAP-41 component. CAP-41 adds zero scheduled minutes. Historical subtask budgets are superseded by this allocation; no additional evening block is implied.

## Active learning contract

1. Implement `List<SelectionKey> uniqueSelections(List<SelectionInput> input)` in `src/main/java/coverstory/modules/shortlist/api/CAP40Seam.java`.
2. Treat `(deviceScope, catalogVersionId)` as the composite identity. Return exact pairs in first-seen order, without modifying the caller’s list. Reject null lists/elements and blank key parts; invalid input must not yield a partial result.
3. Retrieve exact synthetic public wording and return traceable citations through the linked CAP-41 function in `ai/retrieval/coverstory_health.py`. The supplied lowercase whitespace tokenizer is instructor-owned; ranking, zero-score exclusion, deterministic source ordering, `k` validation, and citation selection remain learner work.

A device scope is local identity, never authentication or permission to read a saved shortlist. No health answers or real consumer data belong in these fixtures. CAP-40/CAP-41 acceptance tests are learner tests and intentionally fail against the TODO stubs.

## Running app boundary

The app now calls the same `SelectionOrderer` boundary from `POST /api/shortlist/selection-order` when a visitor chooses “Compare selected.” React sends only the ephemeral tab-local scope and selected public catalog version IDs. The endpoint has no persistence adapter, owner lookup, or private data access; device scope is not authorization. Until `uniqueSelections` is implemented the route returns HTTP 501, and the browser shows that compare is waiting on this learner task. After implementation, first-seen composite-key order from the seam will control the visible compare order. Do not add persistence or authorize a saved shortlist as part of this contract.

## Acceptance cases

### CAP-40 shortlist

The synthetic fixture is `fixtures/coverstory/CAP-40/synthetic-v1.json`.

| Case | Expected outcome |
| --- | --- |
| CAP-40-CS-01 | Input `[(device-A, health-A-v1), (device-A, health-B-v1), (device-A, health-A-v1)]` returns the first two exact pairs in that order; input remains unchanged. |
| CAP-40-CS-02 | Empty input returns `[]`. |
| CAP-40-CS-03 | The same catalog version in `device-A` and `device-B` returns two independent keys. |
| CAP-40-CS-04 | A blank `catalogVersionId` throws `IllegalArgumentException` and produces no partial result. |
| Validation | Blank device scope, null list, and null elements are rejected without mutation. |

### CAP-41 public wording retrieval

The synthetic fixture is `ai/fixtures/coverstory/CAP-41/synthetic-v1.json`. `retrieve(query, documents, k)` uses distinct lowercase whitespace tokens, scores only positive token overlap, sorts by score descending then `source_id` ascending, returns at most `k` exact excerpts, and retains source ID, version, and evidence status.

| Case | Expected outcome |
| --- | --- |
| CAP-41-CS-01 | `waiting period`: `w-health-A/v1` score 2, then `w-health-B/v1` score 1, with exact synthetic excerpts. |
| CAP-41-CS-02 | `guaranteed acceptance` has no match and returns `[]`; blank query also returns `[]`. |
| CAP-41-CS-03 | `k` outside 1–3 or a missing source version raises `ValueError`, with no partial result. |
| CAP-41-CS-04 | Reversing input documents does not change ordered hits. |

There is no model call, external request, medical advice, or invented citation.

## Scaffold paths and commands

- Instructor DTOs/public seam: `src/main/java/coverstory/modules/shortlist/api/`.
- Learner acceptance: `src/test/java/coverstory/modules/shortlist/CAP40AcceptanceTest.java`; instructor positive/negative witness and fixture check: `src/test/java/coverstory/modules/shortlist/CAP40ScaffoldInfrastructureTest.java`; test-only HTTP route witness: `src/test/java/coverstory/modules/shortlist/ShortlistSelectionControllerInfrastructureTest.java`.
- CAP-40 synthetic fixture: `fixtures/coverstory/CAP-40/synthetic-v1.json`.
- CAP-41 instructor types/tokenizer and learner TODO: `ai/retrieval/coverstory_health.py`.
- CAP-41 acceptance and infrastructure tests: `ai/tests/test_coverstory_cap41_acceptance.py`, `ai/tests/test_coverstory_cap41_infrastructure.py`.

With Java 21, Maven, and Python installed, run the focused learner checks from the repository root:

```sh
mvn -Plearner-tests -Dtest=CAP40AcceptanceTest test
python3 -m unittest discover -s ai/tests -p test_coverstory_cap41_acceptance.py
```

Both commands are expected to fail on the explicit learner TODOs. Instructor-only setup remains separate:

```sh
mvn -Dtest=CAP40ScaffoldInfrastructureTest test
python3 -m unittest discover -s ai/tests -p test_coverstory_cap41_infrastructure.py
python3 -m unittest ai.tests.test_coverstory_cap41_http
mvn -Dtest=ShortlistSelectionControllerInfrastructureTest,ApplicationRuntimeIntegrationTest test
```

The instructor witness accepts an exact pair/order implementation and rejects a version-only key and a sorted-order defect. The HTTP witness verifies that a correct seam result controls browser comparison order; a separate actual-app test verifies that the unimplemented learner seam currently returns 501. The learner test command is expected to stop at the explicit TODO. Do not count this as a passing learner outcome. Current endpoint verification is in [Day 001 integration evidence](../../evidence/coverstory/CAP-40/integration-result.json). The [candidate receipt](evidence/harness-candidate-2026-10-14.md) is historical and pinned to its recorded candidate commit; the learner's time-to-completion remains unmeasured.

## Day allocation details retained from the source contract

- **Certification:** proposed AIF-C01 learning allocation, not a learner-ready or completed assignment. The three selected Section 3 lectures are `What is Cloud Computing?` (7:52), `The Different Types of Cloud Computing` (3:19), and `Shared Responsibility Model & AWS Acceptable Policy` (1:36), totaling 12:47 at 1×. Playback is capped at 18 minutes inside the 20-minute theory/source part of the 30-minute certification slot; use remaining time for objective review or retrieval practice.
- **Access gate:** course ownership/subscription access and prior attempts are unknown (`PENDING_ENTITLEMENT_CHECK`). If unavailable, use the linked official source in the same slot and record `ACCESS_PENDING`. No purchase, enrollment, account creation, credentials, IAM/security changes, cloud labs, spend, or exam booking is authorized.
- **Objective relevance:** AWS AIF-C01 task 5.1 is a verified-current-objective link with proposed lecture relevance, not a mastery claim. Keep no-direct-mapping for today’s capstone implementation.
- **Questions:** up to 2–3 relevant items from the linked section quiz; exact unused item IDs and inventory are unknown. Check the attempt ledger first, record source/count/first-attempt result/correction/review date, and do not reproduce proprietary question text. Candidate practice tests 1 and 2 may be rotated only when bounded topic practice is supported; tests 3 and 4 remain reserved unseen full-readiness simulations.
- **DSA:** retain 15 minutes of minimum practice/review and the existing Java/Top150 order and history. Do not count attempted as solved or remove assigned outcomes.
- **Reflection:** architecture/interview evidence stays inside existing caps: at most 3 minutes within docs/evidence, at most 2 changed facts and either 4 nodes or 7 sequence messages (shared primary/component sheet maximum: 3 changed facts and 6 nodes or 10 messages); interview/reflection contribution at most 2 minutes individually and 3 minutes combined.

## Source history and migration

Stable ID `CAP-40`, date `2026-10-14`, role `primary`, and saved-board state `saved_board_snapshot_with_architecture_2026_10_13` are retained. Baseline title was `Day 001 · 14 Oct 2026 — Java collections + cited retrieval (150m)`; original source title/date were `12 Oct — Engineering + DSA: Java collections, Top 150 (60m)` / `2026-10-12`. The previous description hash is `b4641e1e35346a88babc35278c583453457faec9d42dc69223753e187b91321d`. Earlier generic event/runbook contracts and receipts remain historical generic evidence and are not CoverStory product evidence.

## Path reconciliation against the authoritative saved card

The saved CAP-40/CAP-41 card proposes `src/main/java/capstone/coverstory/shortlist/api/CAP40Seam.java`, `src/test/java/capstone/coverstory/shortlist/CAP40Test.java`, `ai/coverstory/cap_41.py`, and `ai/coverstory/tests/test_cap_41.py`. This checkout's existing module ownership layout is `coverstory.modules.shortlist.api`, so the instructor seam is implemented at `src/main/java/coverstory/modules/shortlist/api/CAP40Seam.java`; the Java tests are `src/test/java/coverstory/modules/shortlist/`; and the AI seam/tests are `ai/retrieval/coverstory_health.py` and `ai/tests/test_coverstory_cap41_acceptance.py`. The planned and implemented paths differ; the implementation uses the inspected checkout's module API convention rather than creating a second competing package. The fixture and evidence paths match the saved card. This local mapping does not imply the other 599 contracts were reconciled or scaffolded.

The implementation adds new CoverStory DTOs, tests, and synthetic fixtures without renaming old routes/entities or overwriting generic fixtures. The source's broader proposed journey-security contract and synthetic registry remain unimplemented and outside this day's learner seam.

## Official references

- [Java 21 `Map`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html) — `containsKey`, `get`, `put`, immutable keys, and collection behavior.
- [Python sorting](https://docs.python.org/3/howto/sorting.html) — key functions and stable sorting.
- [Python JSON](https://docs.python.org/3/library/json.html) — `load`/`loads`, decoding errors, and supported types.
- [AWS AIF-C01 task 5.1](https://docs.aws.amazon.com/aws-certification/latest/ai-practitioner-01/ai-practitioner-01-domain5.html) — objective relevance.
- [AWS Software Development Engineer certification path](https://d1.awsstatic.com/onedam/marketing-channels/website/aws/en_US/certification/approved/pdfs/AWS_certification_paths.pdf) and [exam eligibility policy](https://aws.amazon.com/certification/policies/before-testing/).
