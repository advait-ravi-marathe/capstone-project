# M51 conditional learning plan

**Status: proposal/scaffold only. Not activated and no learner work is recorded.**

**Status: Not started. Conditional design/scaffold only; prerequisite capability and learner evidence are not cleared.**

## Fixed 180-minute day

- DSA: 60 minutes, or DSA mock inside this block; record actual attempt date.
- Role/AWS bank: 30 minutes; preserve the existing bank and do not double-book formal assessment.
- AI: 90 minutes. Longer DSA or interview reallocations carry unfinished work forward.

Record actual date, minutes, command and output. A reference fixture, review receipt, course completion or elapsed day is not learner evidence. See the milestone proposal guide for gates and source links.

## Stage sequence

### M51-S01 — Inventory prior attempts

**Worked example and learner task:** Gather dated candidate attempts and classify six gates: coding/DSA, unfamiliar repo, system design, AI evaluation/delivery, discovery/ambiguity, behavioral/project. DSA mocks replace the DSA block.

**Acceptance/evidence:** Attempt ledger references existing artifacts; missing gates stay missing.

### M51-S02 — Freeze unseen task pairs

**Worked example and learner task:** For each critical gate, identify two original unseen exercises on separate days; preserve task hashes, allowed help/tools and expected rubric.

**Acceptance/evidence:** No copied or previously revealed tasks counted as unseen.

### M51-S03 — Score and target repairs

**Worked example and learner task:** Score correctness, reasoning, communication and safety/integrity 1–4. Both attempts need ≥3/4 per critical dimension and no critical failure for this rehearsal threshold.

**Acceptance/evidence:** Threshold is not employer policy. Failed dimensions yield bounded remediation.

### M51-S04 — Buffer / carryover

**Worked example and learner task:** Use only for repairs, retests on fresh variants, or evidence gaps. Preserve baseline attempt and actual dates.

**Acceptance/evidence:** Stop if evidence is insufficient; do not manufacture passes.

### M51-S05 — Buffer / carryover

**Worked example and learner task:** Repair the highest-value failed dimension within remaining capacity. Rebalance daily blocks; do not compress DSA/AI.

**Acceptance/evidence:** Formal non-DSA assessment accrues across recurring role-bank windows through core; AWS windows excluded.

### M51-S06 — Final evidence readback

**Worked example and learner task:** Reconcile six gate states, actual feedback and next smallest repair.

**Acceptance/evidence:** No tenure, Staff impact, hiring prediction or employer cutoff inferred.

## Official short readings

- [Amazon SDE II interview preparation](https://www.amazon.jobs/content/en/how-we-hire/sde-ii-interview-prep)
- [Python unittest](https://docs.python.org/3.12/library/unittest.html)

## Stage reading anchors and attempt binding

| Stages | Exact section or API heading |
|---|---|
| S01–S06 | [Amazon SDE II interview preparation](https://www.amazon.jobs/content/en/how-we-hire/sde-ii-interview-prep): Interview preparation; technical and behavioral dimensions, used as rehearsal only |
| S02, S03, S04, S05, S06 | [Python unittest](https://docs.python.org/3.12/library/unittest.html): Test fixtures; Test cases; Assertions; Test discovery |

Each attempt record binds gate and task_type to the report key, and supplies task_content_sha256 plus task_hash = SHA256(gate + ":" + task_content_sha256). The workflow rejects malformed hashes, repeated attempt IDs, reused task hashes, same-day pairs, cross-gate relabeling and adapter reports that do not name the expected gate and attempts. These checks validate artifact consistency; they do not verify learner authorship or unseen status.

The 20 hours of non-DSA formal assessment accrue across recurring role-bank windows during the core, excluding AWS sessions. They are tracked across those windows and are not added to or compressed into this final five-hour substantive AI scope. Long mock sessions rebalance adjacent blocks or carry forward.

A passing scaffold check does not imply learner mastery, certification or professional experience; none is inferred from these artifacts.
No mastery or professional outcome is inferred from scaffold artifacts.
Professional experience is not inferred from this plan.
