# AWS FDE v2.4 learning roadmap

This is the accepted AWS-only, interview-first plan. **No learner days are complete.** A learning-day number advances only when that session is attempted; dates do not automatically change learner status. This repository release implements Sprint 1: actual learning Days 1–14. Only the first 14 daily pages and U001–U010 contracts plus the explicitly partial U011 slice are active executable guidance here. The remaining canonical first-30 and later milestones are preserved as planning inputs, not shipped daily tasks.

## Start with actual learning Day 1

Use Python 3.12 and the checked-in fictional corpus. This CPU-only worked example needs no model weights or third-party Python dependency:

```sh
python -m ai.fde_v2.coverstory.day01_baseline
```

It reads the synthetic JSON manifest, loads ten fictional records into in-memory SQLite FTS5, and ranks lexical matches. “What is the individual deductible?” starts with `northstar-deductible`. This is instructor-owned keyword retrieval, not generated text, embeddings, RAG or learner completion. Day 1's own task is the baseline/problem brief; see [Day 1](days/day-01.md).

## Sprint 1: actual learning Days 1–14

- [Daily pages, Days 1–14](days/)
- [Unit contracts U001–U010](units/)
- [U011 partial Day 14 slice](units/u011.md)
- [Role interview bank and rubric](role-bank.md)
- [AWS study map](aws-bank.md)
- [Role benchmark sources](role-benchmarks.md)
- [Architecture and later outcome map](phase-map.md)
- [Model runtime verification status](runtime-verification.md)
- [Accepted v2.4 planning inputs](accepted/)

Each day's page assigns DSA60 + role/AWS bank30 + AI90 = 180 minutes. A 75-minute DSA revision reduces AI to75; a90-minute revision reduces AI to60. The actual Day 14 allocation spends U010 support30 + the first U011 segment30 + a private evidence note30. U011 remains incomplete. The full accepted unit schedule continues beyond this Sprint 1 release; no Day 15+ pages or learner code are included.

The daily role pages are the exact practice sequence. AWS work is inside the same 30-minute bank: Day5 course/syllabus audit; Day6 secure-access diagnostic; Day12 networking; Day13 data/storage protection. Current official SAA-C03 exam guide and its exact content-domain task headings are linked in [AWS bank](aws-bank.md). The user-reported owned Maarek course is identified there by course ID; current access and lecture numbering are not verified, so no guessed video number is assigned. Official AWS documentation is the fallback. Nothing here books an exam or claims a certificate.

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

The real-only smoke selectors are documented in [U006/U007](units/u006.md) and [runtime verification](runtime-verification.md). They fail closed if the pinned local artifacts are missing. Embedding inference, persistent vector search, full RAG, held-out evaluation and cloud operation are later planned gates and are not part of this Sprint 1 code release.

## History and evidence

The accepted JSON/Markdown source contracts are retained byte-for-byte under `accepted/`; the receipt lists their SHA-256 values. The earlier 548-day model-building-first roadmap and prior assignment histories remain preserved as superseded/deferred. DSA stays in its separate repository; this checkout contains no DSA problems or solutions. Every new task remains Not started until the learner records actual evidence. No customer adoption, production use, cloud deployment, certification, interview pass or job readiness is asserted by this plan.
