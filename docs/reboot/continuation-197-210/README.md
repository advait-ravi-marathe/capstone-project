# Engineering units 197–210 · calendar study days 218–231 · 2027-05-31–2027-06-13 — offline evidence-grounded explanation foundations

Calendar mapping: stable engineering unit IDs 197–210 occupy study days 218–231 (2027-05-31 through 2027-06-13).

**Starter only.** No learner completion or board publication is claimed. This increment restores the proposal’s 23-hour CoverStory project/recovery and 3-hour offline AI practice split. Its Days 153/154 A/B entry gate remains unmet because the evidence records lack a pinned baseline artifact/source/build provenance.

## Scope and guardrails

- The first seven days target named existing CoverStory readiness, offline AWS-design, rehearsal-evidence, owner-read, and packaged-browser acceptance seams. Days 153/154 historical evidence remains read-only until the original A artifact and provenance are available.
- Three hours maximum are reserved for a small fixed synthetic local evaluator with independent expected citations, outcome counts and fresh-process canonical report checks; standard library only.
- Owner filtering, exact versioned citations, and abstention are testable behaviors. This is a teaching seam, not production authorization or semantic retrieval.
- The Days 153/154 learner-owned A/B gate remains required for StreamForge. Run `python3 scripts/check_streamforge_169_182_entry_gate.py`; its actual current result governs. This toy exercise does not qualify as evidence for that gate.
- Daily budget: 165 engineering minutes (35 study, 115 practice/tests, 15 evidence) plus exactly one shared 45-minute interview/review block. DSA remains external.
- AWS SAA and Developer Associate remain confirmed targets. Consult the current official guides when doing the work; DVA-C03 detail is used only after official publication. No paid exam, booking, cloud account, credential, lecture or live model is included.
- Capstone architecture and its rationale are supplied here. Learner work is implementation and tests. System design remains in the separate interview question bank.

## Files

- `contracts/day-197.md` through `contracts/day-210.md`: daily scope, exact existing selectors, source support, shared-block question/review assignment, and recovery path.
- `assistant-architecture-notes.md`: what/why decisions.
- `verified-sources.json`: public primary sources and course-source audit.
- `manifest.json`: canonical export.
- `scrum-owner-manifest.json`: owner export; board writes are disabled.
- `days-197-210-transfer-plan.json`: dependency, date, selector and architecture export.

## Verification

```sh
python3 -m unittest discover -s scripts -p 'test_*.py'
python3 -m unittest ai.grounded.tests.test_evidence ai.grounded.tests.test_evaluation ai.grounded.tests.test_cli
python3 scripts/generate_continuation_197_210.py
python3 scripts/check_continuation_197_210_sources.py
python3 scripts/check_streamforge_169_182_entry_gate.py
```

The official source file records the corrected OpenAI page title/heading (“Retrieval”) and the existing eval page’s “Create an eval for a task” heading. The eval service is read-only from 31 October 2026 and shut down 30 November 2026, so no May 2027 work depends on it. AWS and OpenAI URLs were independently checked by review; recheck AWS guide versions when performing study.

The focused Java selectors are existing learner acceptance harnesses and remain TODO-red until learner work. Local Java execution in the current environment is blocked before compilation because Maven Central DNS is unavailable and no complete dependency cache is present.

A blocked gate is expected to remain blocked until its exact learner-owned evidence requirements are met. Scaffold tests are not completion evidence.
