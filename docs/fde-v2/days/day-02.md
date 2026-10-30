# Day 2: Clean up a question

**Status: Not started.** Follow this page in order. A missed date does not count as a finished day.

**Planned date:** 2026-10-28 (calendar target only; it does not mark the day complete).

## Today: 180 minutes total

- **60 min DSA:** Use the current item in the separate DSA repository. DSA problems and answers are not copied here.
- **30 min AWS:** Learn the short topic below. No AWS account or console is needed.
- **90 min AI/Python:** Learn one idea, follow the example, make one small code change, then run its check. If DSA review runs long, reduce AI time to stay within 180 minutes and carry the unfinished work forward.

## AWS learning — 30 minutes

**Topic:** Secure access: users, roles, and least privilege

**Read this exact section:** SAA-C03 Content Domain 1, Task 1.1: Design secure access to AWS resources

Read the linked official section for about 20 minutes. Spend the remaining 10 minutes answering this aloud or in one short sentence: Explain in one spoken sentence why an application should receive only the permissions it needs. This is learning, not a readiness score.

Source: [Secure access: users, roles, and least privilege](https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03-domain1.html)

## AI/Python learning — 90 minutes

**Python reference:** [String methods: strip and lower](https://docs.python.org/3/library/stdtypes.html#text-sequence-type-str). Read only this section.
- Optional Udemy supplement: **Types of Data - Strings (5:40), if accessible**. Course access is unverified; skip it if it is not already available. Do not buy or enroll for this plan.

### Worked example (20 minutes)

Input `  HELLO  ` gives `hello`. This demonstrates the idea with a different input from your exercise.

### Your small coding task (40 minutes)

**File:** [`ai/fde_v2/beginner_first15/learner.py`](../../../ai/fde_v2/beginner_first15/learner.py)
**Function:** `normalize_question`

Remove spaces at the two ends and make the remaining question lowercase.

Assigned input: `  What is the deductible?  `
**Expected result:** what is the deductible?

The app plumbing, fixtures, command-line runner and database setup are supplied. You are learning a small function, not building the full application. Reference output teaches the behavior but does not count as your implementation.

### Check your work (15 minutes)

```sh
python -m ai.fde_v2.beginner_first15.acceptance --day 2
```

This checks today’s learner behavior. An unsolved TODO should say `NOT YET`; do not switch to instructor reference code to make it green.

## Next step

The next page adds one small step. Keep the supplied application and demo unchanged.
