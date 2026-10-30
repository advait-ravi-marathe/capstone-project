# Day 14: Show sources and handle no matches

**Status: Not started.** Follow this page in order. A missed date does not count as a finished day.

**Planned date:** 2026-11-09 (calendar target only; it does not mark the day complete).

## Today: 180 minutes total

- **60 min DSA:** Use the current item in the separate DSA repository. DSA problems and answers are not copied here.
- **30 min AWS:** Learn the short topic below. No AWS account or console is needed.
- **90 min AI/Python:** Learn one idea, follow the example, make one small code change, then run its check. If DSA review runs long, reduce AI time to stay within 180 minutes and carry the unfinished work forward.

## AWS learning — 30 minutes

**Topic:** Reduce network cost carefully

**Read this exact section:** SAA-C03 Content Domain 4, Task 4.4: Design cost-optimized network architectures

Read the linked official section for about 20 minutes. Spend the remaining 10 minutes answering this aloud or in one short sentence: Find Task 4.4 and name one route or NAT cost tradeoff. This is learning, not a readiness score.

Source: [Reduce network cost carefully](https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03-domain4.html)

## AI/Python learning — 90 minutes

**Python reference:** [String methods](https://docs.python.org/3/library/stdtypes.html#string-methods), focusing on joining strings. Read only this section.
- No Udemy lesson is assigned today; the named official reference is enough.

### Worked example (20 minutes)

For query `"therapy referral"`, the supplied example search finds one record and displays `Physical therapy — synthetic/therapy`. For `"moonstone"`, it displays `No matching documents.` This is a citation label, not a generated answer.

### Your small coding task (40 minutes)

**File:** [`ai/fde_v2/beginner_first15/learner.py`](../../../ai/fde_v2/beginner_first15/learner.py)
**Function:** `format_search_results`

Use the earlier search and source-label functions. Return one source label per matching document, or the exact no-match message when there are no results. Do not invent an answer from the documents.

Assigned query: `appeal denied` over the three supplied records.
**Expected result:** `Appeals — synthetic/appeals`; `unmatched phrase` returns `No matching documents.`

The search, records, worked example and check are supplied. You are adding one small display behavior, not building an application. The reference output teaches the behavior but does not count as your implementation.

### Check your work (15 minutes)

```sh
python -m ai.fde_v2.beginner_first15.acceptance --day 14
```

This beginner exercise does not implement or complete U011. Its accepted contract remains unchanged and is a separate later prerequisite; see the [Day 16 prerequisite handoff](../amendments/day16-prerequisite-handoff.md).

## Next step

Day 15 connects the pieces into a search response over the supplied fictional corpus. The existing Day 16 lesson remains not ready until its original prerequisites are taught and passed.
