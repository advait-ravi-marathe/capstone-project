# Day 9: Sort the best matches first

**Status: Not started.** Follow this page in order. A missed date does not count as a finished day.

**Planned date:** 2026-11-06 (calendar target only; it does not mark the day complete).

## Today: 180 minutes total

- **60 min DSA:** Use the current item in the separate DSA repository. DSA problems and answers are not copied here.
- **30 min AWS:** Learn the short topic below. No AWS account or console is needed.
- **90 min AI/Python:** Learn one idea, follow the example, make one small code change, then run its check. If DSA review runs long, reduce AI time to stay within 180 minutes and carry the unfinished work forward.

## AWS learning — 30 minutes

**Topic:** Follow an object from upload to retrieval

**Read this exact section:** Getting started with Amazon S3; upload and download an object

Read the linked official section for about 20 minutes. Spend the remaining 10 minutes on this learning check: Describe the relationship between uploading an object and later retrieving it by its key. No bucket or account is needed. This is learning, not a readiness score; no AWS account, console action, or spend is needed.

Source: [Follow an object from upload to retrieval](https://docs.aws.amazon.com/AmazonS3/latest/userguide/GetStartedWithS3.html)

## AI/Python learning — 90 minutes

**Python reference:** [Key Functions; Sort Stability and Complex Sorts](https://docs.python.org/3/howto/sorting.html). Read only this section.
- Optional Udemy supplement: **Lists (4:02), if accessible**. Course access is unverified; skip it if it is not already available. Do not buy or enroll for this plan.

### Worked example (20 minutes)

Example: two records with IDs `b` and `a`, each containing `red`, both score 1 for query `red`; the output order is `a`, then `b`.

### Your small coding task (40 minutes)

**File:** [`ai/fde_v2/beginner_first15/learner.py`](../../../ai/fde_v2/beginner_first15/learner.py)
**Function:** `rank_records`

Sort records from highest overlap score to lowest, break ties by document ID, omit zero-score records, and honor the requested result limit.

Assigned input: four records: `top` contains `red blue`, `a` and `z` each contain `red`, and `none` contains `green`; request `limit=2` for `red blue`.
**Expected result:** `['top', 'a']`; zero-score rows are omitted, the strongest score comes first, and the tie breaks by ID.

The app plumbing, fixtures, command-line runner and database setup are supplied. You are learning a small function, not building the full application. Reference output teaches the behavior but does not count as your implementation.

### Check your work (15 minutes)

```sh
python3 -m ai.fde_v2.beginner_first15.acceptance --day 9
```

This checks today’s learner behavior. An unsolved TODO should say `NOT YET`; do not switch to instructor reference code to make it green.

## Next step

The next page adds one small step. Keep the supplied application and demo unchanged.
