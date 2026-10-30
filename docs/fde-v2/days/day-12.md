# Day 12: Check a document record

**Status: Not started.** Follow this page in order. A missed date does not count as a finished day.

**Planned date:** 2026-11-07 (calendar target only; it does not mark the day complete).

## Today: 180 minutes total

- **60 min DSA:** Use the current item in the separate DSA repository. DSA problems and answers are not copied here.
- **30 min AWS:** Learn the short topic below. No AWS account or console is needed.
- **90 min AI/Python:** Learn one idea, follow the example, make one small code change, then run its check. If DSA review runs long, reduce AI time to stay within 180 minutes and carry the unfinished work forward.

## AWS learning — 30 minutes

**Topic:** Control compute costs

**Read this exact section:** SAA-C03 Content Domain 4, Task 4.2: Design cost-optimized compute solutions

Read the linked official section for about 20 minutes. Spend the remaining 10 minutes answering this aloud or in one short sentence: Find Task 4.2 and name one scaling or purchasing option. This is learning, not a readiness score.

Source: [Control compute costs](https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03-domain4.html)

## AI/Python learning — 90 minutes

**Python reference:** [Basic Usage; Decoding JSON](https://docs.python.org/3/library/json.html). Read only this section.
- Optional Udemy supplement: **Python Variables (3:37), revisit if accessible**. Course access is unverified; skip it if it is not already available. Do not buy or enroll for this plan.

### Worked example (20 minutes)

Worked example: `{'document_id':'toy','version':1,'title':'Guide','source':'fixture/guide','text':'hello'}` becomes a `Document`; its `content_sha256` is the SHA-256 of `hello`. A blank title is rejected. Your assigned sample is different.

### Your small coding task (40 minutes)

**File:** [`ai/fde_v2/coverstory/learner.py`](../../../ai/fde_v2/coverstory/learner.py)
**Function:** `parse_document_record`

Implement the existing small record-validation seam: valid ID, positive integer version, nonempty title/source/text, and content hash.

Assigned input: `{"document_id": "sample", "version": 1, "title": "Sample", "source": "fixture/sample", "text": "short text"}`
**Expected result:** A Document with the supplied values and SHA-256 of its text; malformed version or blank text raises ValueError.

The app plumbing, fixtures, command-line runner and database setup are supplied. You are learning a small function, not building the full application. Reference output teaches the behavior but does not count as your implementation.

### Check your work (15 minutes)

```sh
python -m ai.fde_v2.beginner_first15.acceptance --day 12
```

This checks today’s learner behavior. An unsolved TODO should say `NOT YET`; do not switch to instructor reference code to make it green.

## Next step

The next page adds one small step. Keep the supplied application and demo unchanged.
