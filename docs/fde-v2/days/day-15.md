# Day 15: Search the sample collection end to end

**Status: Not started.** Follow this page in order. A missed date does not count as a finished day.

**Planned date:** 2026-11-12 (calendar target only; it does not mark the day complete).

## Today: 180 minutes total

- **60 min DSA:** Use the current item in the separate DSA repository. DSA problems and answers are not copied here.
- **30 min AWS:** Learn the short topic below. No AWS account or console is needed.
- **90 min AI/Python:** Learn one idea, follow the example, make one small code change, then run its check. If DSA review runs long, reduce AI time to stay within 180 minutes and carry the unfinished work forward.

## AWS learning — 30 minutes

**Topic:** Understand the AWS Well-Architected pillars

**Read this exact section:** AWS Well-Architected Framework; the six pillars

Read the linked official section for about 20 minutes. Spend the remaining 10 minutes on this learning check: Name the six pillars and pick one that would guide a small document-search service. This is learning, not a readiness score; no AWS account, console action, or spend is needed.

Source: [Understand the AWS Well-Architected pillars](https://docs.aws.amazon.com/wellarchitected/latest/framework/welcome.html)

## AI/Python learning — 90 minutes

**Python reference:** [Defining Functions](https://docs.python.org/3/tutorial/controlflow.html#defining-functions) and [String methods](https://docs.python.org/3/library/stdtypes.html#string-methods). Read only those sections.
- No Udemy lesson is assigned today; the pinpointed Python references are enough.

### Worked example (20 minutes)

The worked example searches `"therapy referral"` and returns the matching source label. The assigned query is different so you trace the whole path instead of copying the output.

### Your small coding task (40 minutes)

**File:** [`ai/fde_v2/beginner_first15/learner.py`](../../../ai/fde_v2/beginner_first15/learner.py)
**Function:** `search_response`

Use the functions from Days 2, 10, 11, 13 and 14: clean the question, search the supplied ten fictional records, and show the source label. A blank question gets a prompt; a query with no matches gets the no-match message. Do not generate an answer.

Assigned query: `  INDIVIDUAL deductible? `.
**Expected result:** `Northstar synthetic plan: deductible — CC0 synthetic training fixture; fictional policy statement` is the first line. Blank input returns `Please enter a question.`; `unmatched moonstone` returns `No matching documents.`

The collection, baseline, examples and check are supplied. The task is one learner function that connects earlier small functions, not a database, API or full application. Reference output teaches the behavior but does not count as your implementation.

### Check your work (15 minutes)

```sh
python3 -m ai.fde_v2.beginner_first15.acceptance --day 15
```

This search exercise does **not** satisfy the accepted U011 contract, versioned ingestion, embeddings, RAG or Day 16. Those remain separate, uncompleted prerequisites; see the [Day 16 prerequisite handoff](../amendments/day16-prerequisite-handoff.md). The beginner-first sequence may be completed without claiming the later route is ready.

The original [U011 contract](../units/u011.md) is preserved as history and is **not** today's assigned task.

## Next step

The historical Day 16 chunking lesson is **not ready** under this beginner-first reset. Do not proceed to it or mark it complete until its prerequisites have been separately taught and the original gates pass.
