# Day 4: Read fields from a document

**Status: Not started.** Follow this page in order. A missed date does not count as a finished day.

**Planned date:** 2026-10-30 (calendar target only; it does not mark the day complete).

## Today: 180 minutes total

- **60 min DSA:** Use the current item in the separate DSA repository. DSA problems and answers are not copied here.
- **30 min AWS:** Learn the short topic below. No AWS account or console is needed.
- **90 min AI/Python:** Learn one idea, follow the example, make one small code change, then run its check. If DSA review runs long, reduce AI time to stay within 180 minutes and carry the unfinished work forward.

## AWS learning — 30 minutes

**Topic:** Plan for an unavailable component

**Read this exact section:** SAA-C03 Content Domain 2, Task 2.2: Design highly available and/or fault-tolerant architectures

Read the linked official section for about 20 minutes. Spend the remaining 10 minutes answering this aloud or in one short sentence: Find Task 2.2 and name one recovery approach from the task description. This is learning, not a readiness score.

Source: [Plan for an unavailable component](https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03-domain2.html)

## AI/Python learning — 90 minutes

**Python reference:** [Dictionaries](https://docs.python.org/3/tutorial/datastructures.html#dictionaries). Read only this section.
- Optional Udemy supplement: **Dictionaries (4:04), if accessible**. Course access is unverified; skip it if it is not already available. Do not buy or enroll for this plan.

### Worked example (20 minutes)

Input `{'title': 'Plan', 'text': 'Family coverage'}` gives `['plan', 'family', 'coverage']`. This demonstrates the idea with a different input from your exercise.

### Your small coding task (40 minutes)

**File:** [`ai/fde_v2/beginner_first15/learner.py`](../../../ai/fde_v2/beginner_first15/learner.py)
**Function:** `record_words`

Read title and text fields from a supplied record and return their words.

Assigned input: `{"title": "Deductible", "text": "Individual coverage is 500 dollars"}`
**Expected result:** ['deductible', 'individual', 'coverage', 'is', '500', 'dollars']

The app plumbing, fixtures, command-line runner and database setup are supplied. You are learning a small function, not building the full application. Reference output teaches the behavior but does not count as your implementation.

### Check your work (15 minutes)

```sh
python -m ai.fde_v2.beginner_first15.acceptance --day 4
```

This checks today’s learner behavior. An unsolved TODO should say `NOT YET`; do not switch to instructor reference code to make it green.

## Next step

The next page adds one small step. Keep the supplied application and demo unchanged.
