# Day 13: Combine the search steps

**Status: Not started.** Follow this page in order. A missed date does not count as a finished day.

**Planned date:** 2026-11-08 (calendar target only; it does not mark the day complete).

## Today: 180 minutes total

- **60 min DSA:** Use the current item in the separate DSA repository. DSA problems and answers are not copied here.
- **30 min AWS:** Learn the short topic below. No AWS account or console is needed.
- **90 min AI/Python:** Learn one idea, follow the example, make one small code change, then run its check. If DSA review runs long, reduce AI time to stay within 180 minutes and carry the unfinished work forward.

## AWS learning — 30 minutes

**Topic:** Design cost-aware database choices

**Read this exact section:** SAA-C03 Content Domain 4, Task 4.3: Design cost-optimized database solutions

Read the linked official section for about 20 minutes. Spend the remaining 10 minutes answering this aloud or in one short sentence: Find Task 4.3 and name one cost-related database choice. This is learning, not a readiness score.

Source: [Design cost-aware database choices](https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03-domain4.html)

## AI/Python learning — 90 minutes

**Python reference:** [Defining Functions](https://docs.python.org/3/tutorial/controlflow.html#defining-functions). Read only this section.
- Optional Udemy supplement: **Modules and Packages - Standard Library (4:24), if accessible**. Course access is unverified; skip it if it is not already available. Do not buy or enroll for this plan.

### Worked example (20 minutes)

Worked example: question `family plan` over two supplied records returns only the record whose title/text shares those words. This shows the combination; your assigned query below is different.

### Your small coding task (40 minutes)

**File:** [`ai/fde_v2/beginner_first15/learner.py`](../../../ai/fde_v2/beginner_first15/learner.py)
**Function:** `search_records`

Use the earlier match and ranking functions together. A blank question returns an empty list.

Assigned input: `{"question": "individual deductible", "corpus": "the ten checked-in fictional records"}`
**Expected result:** northstar-deductible is the first result; an empty question returns [].

The app plumbing, fixtures, command-line runner and database setup are supplied. You are learning a small function, not building the full application. Reference output teaches the behavior but does not count as your implementation.

### Check your work (15 minutes)

```sh
python -m ai.fde_v2.beginner_first15.acceptance --day 13
```

This checks today’s learner behavior. An unsolved TODO should say `NOT YET`; do not switch to instructor reference code to make it green.

## Next step

The next page adds one small step. Keep the supplied application and demo unchanged.
