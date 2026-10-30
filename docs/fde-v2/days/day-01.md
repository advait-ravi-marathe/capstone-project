# Day 1: Read one supplied result

**Status: Not started.** Follow this page in order. A missed date does not count as a finished day.

**Planned date:** 2026-10-27 (calendar target only; it does not mark the day complete).

## Today: 180 minutes total

- **60 min DSA:** Use the current item in the separate DSA repository. DSA problems and answers are not copied here.
- **30 min AWS:** Learn the short topic below. No AWS account or console is needed.
- **90 min AI/Python:** Learn one idea, follow the example, make one small code change, then run its check. If DSA review runs long, reduce AI time to stay within 180 minutes and carry the unfinished work forward.

## AWS learning — 30 minutes

**Topic:** What AWS is and how its global infrastructure is named

**Read this exact section:** AWS Overview: Global Infrastructure

Read the linked official section for about 20 minutes. Spend the remaining 10 minutes answering this aloud or in one short sentence: Name one AWS Region and one Availability Zone from the reading; no account or console required. This is learning, not a readiness score.

Source: [What AWS is and how its global infrastructure is named](https://docs.aws.amazon.com/whitepapers/latest/aws-overview/global-infrastructure.html)

## AI/Python learning — 90 minutes

First run the supplied example. It is the working application behavior, not your implementation:

```sh
python -m ai.fde_v2.coverstory.day01_baseline
```

Expected first line: `rank=1 document=northstar-deductible title=Northstar synthetic plan: deductible`. Then use the task below to practice reading one Python value.

**Python reference:** [Using Python as a Calculator; Strings](https://docs.python.org/3/tutorial/introduction.html). Read only this section.
- Optional Udemy supplement: **Python Variables (3:37), if present in the learner's course catalog**. Course access is unverified; skip it if it is not already available. Do not buy or enroll for this plan.

### Worked example (20 minutes)

Input `{'title': 'Appeals'}` gives `Appeals`. This demonstrates the idea with a different input from your exercise.

### Your small coding task (40 minutes)

**File:** [`ai/fde_v2/beginner_first15/learner.py`](../../../ai/fde_v2/beginner_first15/learner.py)
**Function:** `document_title`

Return the title from a supplied dictionary; return an empty string if it is missing.

Assigned input: `{"document_id": "deductible", "title": "Deductible"}`
**Expected result:** Deductible

The app plumbing, fixtures, command-line runner and database setup are supplied. You are learning a small function, not building the full application. Reference output teaches the behavior but does not count as your implementation.

### Check your work (15 minutes)

```sh
python -m ai.fde_v2.beginner_first15.acceptance --day 1
```

This checks today’s learner behavior. An unsolved TODO should say `NOT YET`; do not switch to instructor reference code to make it green.

## Next step

The next page adds one small step. Keep the supplied application and demo unchanged.
