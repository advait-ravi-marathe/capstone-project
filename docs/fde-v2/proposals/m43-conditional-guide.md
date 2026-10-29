# M43 conditional learning plan

**Status: proposal/scaffold only. Not activated and no learner work is recorded.**

**Status: Not started. Conditional design/scaffold only; prerequisite capability and learner evidence are not cleared.**

## Fixed 180-minute day

- DSA: 60 minutes, or DSA mock inside this block; record actual attempt date.
- Role/AWS bank: 30 minutes; preserve the existing bank and do not double-book formal assessment.
- AI: 90 minutes. Longer DSA or interview reallocations carry unfinished work forward.

Record actual date, minutes, command and output. A reference fixture, review receipt, course completion or elapsed day is not learner evidence. See the milestone proposal guide for gates and source links.

## Stage sequence

### M43-S01 — Unfamiliar exercise integrity

**Worked example and learner task:** Check license, task hash, allowed tools, timebox and hidden reference solution. Use two separate lawful defect/integration tasks; never relabel a known tutorial as unseen.

**Acceptance/evidence:** Exercise manifest, source/license and initial hash. No independent attempt claim yet.

### M43-S02 — Reproduce and localize A

**Worked example and learner task:** Use an unrelated worked defect first. Then run the original failing command, preserve its output, and write a minimal regression before changing code.

**Acceptance/evidence:** Actual failure receipt, root-cause note and date; do not reveal solution before learner attempt.

### M43-S03 — Patch A and inspect blast radius

**Worked example and learner task:** Make the smallest change that satisfies regression. Run focused and impacted tests; explain touched interfaces/data and variant behavior.

**Acceptance/evidence:** Before/after diff, red-green test and patch hash attributable to learner.

### M43-S04 — Exercise B integration attempt

**Worked example and learner task:** On a separate actual day, integrate unfamiliar modules against the existing API. Reproduce the second root cause and implement a learner patch.

**Acceptance/evidence:** Distinct task hash and attempt date; assistance disclosure and failing trace.

### M43-S05 — Review B and retain first result

**Worked example and learner task:** Score both attempts against correctness, tests, debugging, safety and explanation. Preserve first score; create bounded repair and fresh variant retest.

**Acceptance/evidence:** Reviewer type is explicit. Never overwrite failed attempt with a repaired result.

### M43-S06 — Clean clone reproducibility

**Worked example and learner task:** Start from an empty owned workspace; follow the runbook and verify dependency/config commands, startup, feature and failure path.

**Acceptance/evidence:** Clean clone receipt or BLOCKED/NOT_RUN with reason. Warm checkout is not a clean-clone proof.

### M43-S07 — Peer handoff or self-run

**Worked example and learner task:** Prepare a narrow handoff and feedback form. Ask for a reviewer only with approved sharing; otherwise run it yourself and label that accurately.

**Acceptance/evidence:** No invented peer feedback, dates or public sharing. Record reviewer availability.

### M43-S08 — Five-minute demo with failure

**Worked example and learner task:** Show actual feature, a failure path and recovery; answer one architecture question and explain value without technical shorthand.

**Acceptance/evidence:** Local/cloud/simulated labels and actual run IDs; no production/customer claims.

### M43-S09 — Architecture and ownership review

**Worked example and learner task:** Explain a measured design tradeoff and map personal changes/tests separately from supplied, tutorial and assistant work.

**Acceptance/evidence:** Contribution map links each claim to source files/attempts.

### M43-S10 — Integration release gate

**Worked example and learner task:** Check both reproduce→root cause→minimal patch→regression→blast radius chains, clean-run status, reviewer status and demo.

**Acceptance/evidence:** Gate is exercise-only; unavailable peer review stays NOT_EVIDENCED.

## Official short readings

- [Python unittest](https://docs.python.org/3.12/library/unittest.html)
- [pytest monkeypatch](https://docs.pytest.org/en/stable/how-to/monkeypatch.html)
- [Python pathlib](https://docs.python.org/3.12/library/pathlib.html)
- [Docker build best practices](https://docs.docker.com/build/building/best-practices/)
- [GOV.UK interview guidance](https://www.gov.uk/service-manual/user-research/using-in-depth-interviews)
- [Amazon SDE II interview preparation](https://www.amazon.jobs/content/en/how-we-hire/sde-ii-interview-prep)

## Stage reading anchors and runnable examples

Read only these named sections during the assigned study minutes:

| Stages | Exact section or API heading |
|---|---|
| S01, S02, S03, S04, S10 | [Python unittest](https://docs.python.org/3.12/library/unittest.html): Test fixtures; Test cases; Assertions; Test discovery |
| S02, S03, S04, S05 | [pytest monkeypatch](https://docs.pytest.org/en/stable/how-to/monkeypatch.html): Monkeypatching functions; Environment variables; Global patches |
| S06, S09 | [Python pathlib](https://docs.python.org/3.12/library/pathlib.html): Path.resolve; Path.is_relative_to; Path.open |
| S06 | [Docker build best practices](https://docs.docker.com/build/building/best-practices/): Multi-stage builds; .dockerignore; Pin base image versions; USER |
| S07, S10 | [GOV.UK in-depth interviews](https://www.gov.uk/service-manual/user-research/using-in-depth-interviews): When to use in-depth interviews; Steps to follow |
| S01, S05, S08, S09 | [Amazon SDE II interview preparation](https://www.amazon.jobs/content/en/how-we-hire/sde-ii-interview-prep): Interview preparation; technical and behavioral dimensions, used only as rehearsal |

The supplied [A/B exercise factory and reference controls](../../../ai/fde_v2/m43/exercises.py) create two distinct failing scratch repositories, an unrelated worked example, executable red/green reference copies and clean-copy runs. The command python -m unittest ai.fde_v2.tests.test_m43 exercises these mechanics. Reference copies never modify the learner's A/B sources and never create learner-attempt evidence. The default attempt adapter remains intentionally unimplemented.

A test pass, artifact authored by an assistant, or elapsed day is not inferred learner skill; the independent-integration gate requires actual learner attempts and their retained evidence.

A passing scaffold check does not imply learner mastery, certification or professional experience; none is inferred from these artifacts.
No mastery or professional outcome is inferred from scaffold artifacts.
Professional experience is not inferred from this plan.
