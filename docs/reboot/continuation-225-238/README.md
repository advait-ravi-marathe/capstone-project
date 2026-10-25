# Engineering units 225–238 · calendar study days 246–259 · 2027-06-28–2027-07-11 — synthetic policy draft review

Calendar mapping: stable engineering unit IDs 225–238 occupy study days 246–259 (2027-06-28 through 2027-07-11).

Status: reviewable starter curriculum; no learner completion or board publication is claimed. Dates are 7–20 June 2027. This bounded increment is based on the reviewed Days 197–270 proposal and the existing synthetic workflow in `ai/grounded/continuation_211.py`.

<a id="entry-gate-and-recovery"></a>
## Entry gate and recovery

Before attempting bundle ingestion or the new workflow, run `python3 scripts/check_continuation_211_entry_gate.py`. As of this scaffold, it reports `not_met`: the learner-owned Day 210 boundary review is absent, and `completion_awarded` is false. Only a genuine learner-owned record satisfying the existing strict schema can change eligibility. The checker grants no completion. If the gate is absent or blocked, use the first unmet CoverStory recovery selector recorded for the day in `plan-source.json`; keep AI work to fixed offline evaluation labels. The Day 225 fixed-label fixture test is safe before the gate because it neither classifies candidates nor runs the workflow. Do not run other new-workflow acceptance selectors while blocked.

The new path remains conditional even after the Day 210 gate: all preceding learner-owned evidence must still be evaluated as it actually exists. CI, starter files and synthetic fixtures are not learner evidence. Record source identity as a 40-character Git commit SHA and artifact identity as a 64-character SHA-256 digest.

## Assistant-supplied architecture

The workflow is a local synthetic policy-explanation draft and review path. Candidate text is bound to exact owner, source/version, and snapshot digest. Staging creates only `DRAFT`; it cannot change policy state or expose text. A separate human review action checks owner scope, current policy version, expected ledger revision and idempotency key, then records one minimal append-only audit event for approval or rejection. Only an approved same-owner draft is renderable. Unsupported evidence, foreign ownership, stale state and adapter failures abstain or conflict without mutation. There is no live model/provider, AWS call, credential, cloud resource, external exposure or consequential policy decision.

The assistant supplies these architecture choices and their reasons. The learner implements and tests them. No capstone design proposal or post-hoc architecture justification is assigned. System-design practice stays in the separate interview question bank.

## Starter and acceptance

`ai/grounded/continuation_225.py` contains learner TODOs. The fixed acceptance harness at `ai/grounded/learner_acceptance/test_continuation_225.py` specifies exact expected citations, states, audit fields, replay behavior, failure outputs and metrics. Day 225 only authors fixed labels; the observable classifier is introduced on Days 226–227 and evaluated on Day 235 using an injected `classify_candidate(snapshot, candidate)` call that cannot read expected labels. A counterfactual label change must change the score. Negative cases cover foreign reviewers and sources, stale versions and revisions, altered text, duplicate/reused keys, unsupported evidence and failures. Harness witnesses under `scripts/test_continuation_225_238.py` are test-only fixtures, separate from learner code; they drive the actual acceptance methods and prove that wrong classifiers, reset audit history, duplicate sequence numbers, ignored revisions and authorization-bypassing replays fail. These witnesses are not learner implementations or completion evidence.

The learner acceptance suite is intentionally red until its TODOs are implemented. An expected TODO failure is not an infrastructure failure and never awards completion. The instructor CI checks generator reproducibility, metadata/source consistency, link targets and oracle quality; it does not mark learner work passed.

## Budget

Every day has 165 engineering minutes: 35 study, 115 practice/tests and 15 evidence, plus one shared 45-minute block. Window engineering totals are 19 hours project implementation/tests, 6 hours AI workflow/evaluation, 1 hour 50 minutes certification questions, 2 hours 30 minutes AI concept study, 2 hours certification study, 3 hours 40 minutes core systems/security/cloud study and 3 hours 30 minutes evidence. The shared block appears once per day and is excluded from the 165 engineering minutes.

The proposed credential targets remain AWS Certified Solutions Architect – Associate (SAA-C03) and AWS Certified Developer – Associate. Recheck official guide titles, versions and domains at study time. Local examples do not establish AWS deployment experience or exam readiness; there are no paid exams or bookings.

## Sources and generation

Official titles and named headings were checked on 24 October 2026 and are listed in `verified-sources.json`. Recheck the pages in June 2027 because content can change. OpenAI evaluation guidance is used only for general local test design; no deprecated Evals API/platform workflow is assigned. AWS materials are comparisons only; no service is invoked.

`plan-source.json` is the canonical daily source. Regenerate all manifests, rendered contracts and assistant notes with:

```sh
python3 scripts/generate_continuation_225_238.py
```

Validate synchronization and oracle negative controls with:

```sh
python3 -m unittest scripts.test_continuation_225_238
```

DSA practice remains in the separate [`dsa-prep` repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0).
