# Days 197–210: offline evidence-grounded explanation foundations

**Starter only.** No learner completion or board publication is claimed. This increment follows the accepted Days 197–270 proposal while narrowing the initial 14 days to prerequisites and a deterministic local baseline. The proposal’s later retrieval/draft workflow stages are not activated.

## Scope and guardrails

- One fixed, synthetic clause fixture and a deterministic literal-match example in `ai.grounded`; standard library only.
- Owner filtering, exact versioned citations, and abstention are testable behaviors. This is a teaching seam, not production authorization or semantic retrieval.
- The Days 153/154 learner-owned A/B gate remains required for StreamForge. Run `python3 scripts/check_streamforge_169_182_entry_gate.py`; its actual current result governs. This toy exercise does not qualify as evidence for that gate.
- Daily budget: 165 engineering minutes (35 study, 115 practice/tests, 15 evidence) plus exactly one shared 45-minute interview/review block. DSA remains external.
- AWS SAA and Developer Associate remain confirmed targets. Consult the current official guides when doing the work; DVA-C03 detail is used only after official publication. No paid exam, booking, cloud account, credential, lecture or live model is included.
- Capstone architecture and its rationale are supplied here. Learner work is implementation and tests. System design remains in the separate interview question bank.

## Files

- `contracts/day-197.md` through `contracts/day-210.md`: daily scope, source support, acceptance and recovery path.
- `assistant-architecture-notes.md`: what/why decisions.
- `verified-sources.json`: public primary sources and course-source audit.
- `manifest.json`: canonical export.
- `scrum-owner-manifest.json`: owner export; board writes are disabled.
- `days-197-210-transfer-plan.json`: dependency, date, selector and architecture export.

## Verification

```sh
python3 -m unittest ai.grounded.tests.test_evidence scripts.test_continuation_197_210_manifest
python3 scripts/check_streamforge_169_182_entry_gate.py
```

A blocked gate is expected to remain blocked until its exact learner-owned evidence requirements are met. Scaffold tests are not completion evidence.
