# Day 11: Show where a result came from

**Status: Not started.** Follow this page in order. A missed date does not count as a finished day.

**Planned date:** 2026-11-08 (calendar target only; it does not mark the day complete).

## Today: 180 minutes total

- **60 min DSA:** Use the current item in the separate DSA repository. DSA problems and answers are not copied here.
- **30 min AWS:** Learn the short topic below. No AWS account or console is needed.
- **90 min AI/Python:** Learn one idea, follow the example, make one small code change, then run its check. If DSA review runs long, reduce AI time to stay within 180 minutes and carry the unfinished work forward.

## AWS learning — 30 minutes

**Topic:** Match storage class to access pattern

**Read this exact section:** Understanding and managing Amazon S3 storage classes

Read the linked official section for about 20 minutes. Spend the remaining 10 minutes on this learning check: Explain why infrequently accessed data may use a different storage class, while considering retrieval needs. This is learning, not a readiness score; no AWS account, console action, or spend is needed.

Source: [Match storage class to access pattern](https://docs.aws.amazon.com/AmazonS3/latest/userguide/storage-class-intro.html)

## AI/Python learning — 90 minutes

**Python reference:** [Formatted String Literals](https://docs.python.org/3/tutorial/inputoutput.html#formatted-string-literals). Read only this section.
- Optional Udemy supplement: **Functions - Parameter (3:49), if accessible**. Course access is unverified; skip it if it is not already available. Do not buy or enroll for this plan.

### Worked example (20 minutes)

Run the supplied example command; it uses the completed instructor reference, not your learner function:

```sh
python3 -m ai.fde_v2.beginner_first15.examples --day 11
```

It prints the example input and output. Expected output:

```text
Guide — fixture/guide
```

This walkthrough shows the behavior only. Your learner check below still tests your own function.


### Your small coding task (40 minutes)

**File:** [`ai/fde_v2/beginner_first15/learner.py`](../../../ai/fde_v2/beginner_first15/learner.py)
**Function:** `source_label`

Format the supplied title and source as a short citation label; do not invent answer text.

Assigned input: `{"title": "Appeals", "source": "synthetic/appeals"}`
**Expected result:** Appeals — synthetic/appeals

The app plumbing, fixtures, command-line runner and database setup are supplied. You are learning a small function, not building the full application. Reference output teaches the behavior but does not count as your implementation.

### Check your work (15 minutes)

```sh
python3 -m ai.fde_v2.beginner_first15.acceptance --day 11
```

This checks today’s learner behavior. An unsolved TODO should say `NOT YET`; do not switch to instructor reference code to make it green.

## Next step

The next page adds one small step. Keep the supplied application and demo unchanged.
