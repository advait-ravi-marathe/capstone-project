# Engineering units 239–252 · calendar study days 260–273 · 2027-07-12–2027-07-25 — local policy evaluation and fake-adapter controls

Calendar mapping: stable engineering unit IDs 239–252 occupy study days 260–273 (2027-07-12 through 2027-07-25).

Status: bounded starter curriculum for independent review. Dates are 21 June–4 July 2027. This increment extends the existing synthetic HomeSecure policy-explanation workflow in one context only: fictional customer `cust-synth-01`, owner `synthetic-owner-a`, policy version `v4`, and invented renewal date 1 July 2027. The fixture is local and does not describe a real customer or real outcome.

## Dynamic entry gate and recovery

Run `python3 scripts/check_continuation_239_entry_gate.py` before learner acceptance work. It requires the existing Day 210 learner-owned boundary record and a future Day 238 learner-owned closeout record matching that day's real synthetic draft/review selector: draft-only generation, pre-approval abstention, explicit human review, exact same-owner rendering, unchanged policy snapshot, and separate source/artifact digests. Both are absent at this scaffold: the gate reports `not_met`; it awards no completion. A malformed record reports `blocked`. A valid result permits an attempt only.

When the gate is absent or blocked, use the first unmet CoverStory recovery command, in order, from `plan-source.json`. Keep this window's learner workflow selectors unrun until both gates are met. Day 239 fixed labels are inert fixture data; instructor CI may inspect and mutation-test them without counting them as learner evidence. A learner TODO raising `NotImplementedError` is an expected incomplete starter, not an infrastructure failure or credit.

## Assistant-supplied architecture

The assistant chooses a deterministic policy-explanation path with owner filtering before the local adapter. The adapter is a scripted fake with fixed response, error, latency, token, and call values. The output validator requires exact text and citations from approved owner-scoped excerpts; all unsupported, foreign, malformed, stale, action-seeking, and transiently failed responses abstain. Timeout and unavailable outcomes may receive one retry; malformed output does not. Metrics compare observed outputs against independently authored fixed labels. Stable trace correlation contains only allowlisted synthetic metadata and excludes owner IDs, questions, policy excerpts, answers, and prompt content.

This is one local synthetic evaluation exercise. It is not a second enterprise-integration project, a StreamForge activation, a customer deployment, an underwriting/eligibility decision, or a provider performance claim. No live provider, network, AWS service, cloud spend, credential, real data, or external exposure is part of the task. No LLM judge supplies ground truth. The assistant explains what was chosen and why; the learner implements and tests the supplied design. Capstone design proposals and post-hoc justifications are not assigned. System-design practice appears only in the separate interview question bank.

## Workload

Each day has 165 engineering minutes (35 study, 115 practice/tests, 15 evidence) plus exactly one shared 45-minute block. Window allocations are:

| Category | Minutes | Hours |
|---|---:|---:|
| Project implementation/tests | 840 | 14.00 |
| AI workflow implementation/evaluation | 660 | 11.00 |
| Certification labs/questions | 110 | 1.83 |
| AI concept/source study | 210 | 3.50 |
| Certification domain study | 150 | 2.50 |
| Core systems/security/cloud study | 130 | 2.17 |
| Evidence | 210 | 3.50 |
| **Engineering total** | **2,310** | **38.50** |

The shared block is allocated once daily: four 45-minute question-bank sessions (180 minutes), four certification retrieval blocks (180 minutes), and six unfinished reviews (270 minutes). It is excluded from the engineering budget. Question sessions use only the specified separate system-design interview prompts; they do not ask for capstone design choices.

The confirmed certification targets remain AWS Certified Solutions Architect – Associate (SAA-C03) and AWS Certified Developer – Associate. The AWS Developer Associate page checked on 24 October 2026 said DVA-C03 registration opens 27 October 2026, the final DVA-C02 test date is 30 November 2026, and the updated guide would be posted on 27 October. The contracts therefore name only the verified current certification page and its exact “Review the exam guide” link; they do not invent DVA-C03 domains. Recheck the current version and headings in June 2027. The small local/offline allocation is not exam readiness, a pass guarantee, AWS deployment experience, or a booking. No paid exam is scheduled.

## Source identity and generation

`plan-source.json` is the canonical daily source. Official titles and named headings in `verified-sources.json` were checked on 24 October 2026; recheck them in June 2027. OpenAI's *Evaluation best practices — Design your eval process* is used only as methodology. The OpenAI Evals API/platform is not used. AWS Guardrails, AgentCore Evaluation, Observability, and certification materials are reference-only; no AWS account, resource, trace upload, or spend is authorized.

Regenerate all structured exports, contracts, and architecture notes with:

```sh
python3 scripts/generate_continuation_239_252.py
```

Check source/export synchronization, contract links, gate schema, actual acceptance assertions, and test-only mutation witnesses with:

```sh
python3 -m unittest scripts.test_continuation_239_252
```

Learner seams are in `ai/grounded/continuation_239.py`; the substantive acceptance contract is in `ai/grounded/learner_acceptance/test_continuation_239.py`. Reference witnesses under `scripts/test_continuation_239_252.py` are instructor-only, are not imported by learner code, and never count as learner implementation or completion. The fixture uses fixed independent expected statuses/citations and tests counterfactual labels, owner filtering before the adapter, injection/action rejection, bounded retry, latency, trace redaction, token/call estimates, mutation controls, and fresh-process CLI replay with fixed canonical bytes/digest and changed-input control; Day 252 runs the full pipeline against independent paired outcomes. No nonempty-only assertions or expected-label-to-classifier shortcuts are intended.

For every future learner evidence record, preserve a **40-character source commit SHA** separately from a **64-character artifact SHA-256**. Preserve actual attempt time, commands/results, changed files, assistance, and blockers. Keep learner status `unobserved` until actual evidence exists; CI and scaffold status are not completion. This portfolio can evidence implementation and evaluation knowledge; it does not demonstrate professional customer ownership, production operation, or Staff/Principal/FDE-level organizational influence.

The longer progression remains knowledge/application checkpoints at SDE II and Senior, followed by Staff/Principal/FDE aspirations that require real workplace evidence such as production ownership, incident responsibility, customer outcomes, cross-team impact, or mentorship. Solo synthetic artifacts are not substituted for those professional ownership claims. DSA remains only in the separate [`dsa-prep` repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0).
