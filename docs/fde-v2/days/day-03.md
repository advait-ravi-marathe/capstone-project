# Day 3: Split a question into words

**Status: Not started.** Follow this page in order. A missed date does not count as a finished day.

**Planned date:** 2026-10-31 (calendar target only; it does not mark the day complete).

## Today: 180 minutes total

- **60 min DSA:** Use the current item in the separate DSA repository. DSA problems and answers are not copied here.
- **30 min AWS:** Learn the short topic below. No AWS account or console is needed.
- **90 min AI/Python:** Learn one idea, follow the example, make one small code change, then run its check. If DSA review runs long, reduce AI time to stay within 180 minutes and carry the unfinished work forward.

## AWS learning — 30 minutes

**Topic:** Know which security tasks belong to AWS and the customer

**Read this exact section:** Shared responsibility model; security of the cloud and security in the cloud

Read the linked official section for about 20 minutes. Spend the remaining 10 minutes on this learning check: Compare one responsibility for a virtual server with one responsibility AWS handles for its physical infrastructure. This is learning, not a readiness score; no AWS account, console action, or spend is needed.

Source: [Know which security tasks belong to AWS and the customer](https://docs.aws.amazon.com/whitepapers/latest/aws-risk-and-compliance/shared-responsibility-model.html)

## AI/Python learning — 90 minutes

**Python reference:** [String `split`](https://docs.python.org/3/library/stdtypes.html#str.split). Read only this method.
- Optional Udemy supplement: **For Loops (2:56), if accessible**. Course access is unverified; skip it if it is not already available. Do not buy or enroll for this plan.

### Worked example (20 minutes)

Run the supplied example command; it uses the completed instructor reference, not your learner function:

```sh
python3 -m ai.fde_v2.beginner_first15.examples --day 3
```

It prints the example input and output. Expected output:

```text
['blue', 'sky']
```

This walkthrough shows the behavior only. Your learner check below still tests your own function.


### Your small coding task (40 minutes)

**File:** [`ai/fde_v2/beginner_first15/learner.py`](../../../ai/fde_v2/beginner_first15/learner.py)
**Function:** `question_words`

Return `_split_words(question)`. A list keeps the words in order; you do not need to write the punctuation cleanup yourself.

Assigned input: `The deductible is 500`
**Expected result:** ['the', 'deductible', 'is', '500']

The helper, fixtures, command-line runner and app plumbing are supplied. You are learning to call a function and return its result, not building the full application. Reference output teaches the behavior but does not count as your implementation.

### Check your work (15 minutes)

```sh
python3 -m ai.fde_v2.beginner_first15.acceptance --day 3
```

This checks today’s learner behavior. An unsolved TODO should say `NOT YET`; do not switch to instructor reference code to make it green.

## Next step

The next page adds one small step. Keep the supplied application and demo unchanged.
