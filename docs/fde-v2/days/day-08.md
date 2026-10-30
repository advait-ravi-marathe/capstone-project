# Day 8: Count shared words

**Status: Not started.** Follow this page in order. A missed date does not count as a finished day.

**Planned date:** 2026-11-05 (calendar target only; it does not mark the day complete).

## Today: 180 minutes total

- **60 min DSA:** Use the current item in the separate DSA repository. DSA problems and answers are not copied here.
- **30 min AWS:** Learn the short topic below. No AWS account or console is needed.
- **90 min AI/Python:** Learn one idea, follow the example, make one small code change, then run its check. If DSA review runs long, reduce AI time to stay within 180 minutes and carry the unfinished work forward.

## AWS learning — 30 minutes

**Topic:** Understand a VPC and its subnets

**Read this exact section:** What is Amazon VPC?; VPCs, subnets, and Availability Zones

Read the linked official section for about 20 minutes. Spend the remaining 10 minutes on this learning check: Explain what a VPC is and why each subnet belongs to one Availability Zone. This is learning, not a readiness score; no AWS account, console action, or spend is needed.

Source: [Understand a VPC and its subnets](https://docs.aws.amazon.com/vpc/latest/userguide/what-is-amazon-vpc.html)

## AI/Python learning — 90 minutes

**Python reference:** [Set operations](https://docs.python.org/3/library/stdtypes.html#set-types-set-frozenset). Read only this section.
- Optional Udemy supplement: **For Loops (2:56), revisit only if accessible**. Course access is unverified; skip it if it is not already available. Do not buy or enroll for this plan.

### Worked example (20 minutes)

Example: `overlap_score("red red blue", {"title": "", "text": "blue green"})` returns `1`: `blue` is the only distinct shared word.

### Your small coding task (40 minutes)

**File:** [`ai/fde_v2/beginner_first15/learner.py`](../../../ai/fde_v2/beginner_first15/learner.py)
**Function:** `overlap_score`

Count distinct words shared by the question and one record.

Assigned input: `{"question": "deductible deductible individual", "record": "individual deductible is 500"}`
**Expected result:** 2

The app plumbing, fixtures, command-line runner and database setup are supplied. You are learning a small function, not building the full application. Reference output teaches the behavior but does not count as your implementation.

### Check your work (15 minutes)

```sh
python3 -m ai.fde_v2.beginner_first15.acceptance --day 8
```

This checks today’s learner behavior. An unsolved TODO should say `NOT YET`; do not switch to instructor reference code to make it green.

## Next step

The next page adds one small step. Keep the supplied application and demo unchanged.
