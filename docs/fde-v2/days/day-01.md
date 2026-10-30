# Day 1: Read one supplied result

**Status: Not started.** Follow this page in order. A missed date does not count as a finished day.

**Planned date:** 2026-10-29 (calendar target only; it does not mark the day complete).

## Today: 180 minutes total

- **60 min DSA:** Use the current item in the separate DSA repository. DSA problems and answers are not copied here.
- **30 min AWS:** Learn the short topic below. No AWS account or console is needed.
- **90 min AI/Python:** Learn one idea, follow the example, make one small code change, then run its check. If DSA review runs long, reduce AI time to stay within 180 minutes and carry the unfinished work forward.

## AWS learning — 30 minutes

**Topic:** Understand Regions and Availability Zones

**Read this exact section:** AWS Overview: Global Infrastructure; Regions and Availability Zones

Read the linked official section for about 20 minutes. Spend the remaining 10 minutes on this learning check: Explain how a Region differs from an Availability Zone. This is learning, not a readiness score; no AWS account, console action, or spend is needed.

Source: [Understand Regions and Availability Zones](https://docs.aws.amazon.com/whitepapers/latest/aws-overview/global-infrastructure.html)

## AI/Python learning — 90 minutes

First run the supplied example. It is the working application behavior, not your implementation:

```sh
python3 -m ai.fde_v2.coverstory.day01_baseline
```

Expected first line: `rank=1 document=northstar-deductible title=Northstar synthetic plan: deductible`. Then use the task below to practice reading one Python value.

**Python reference:** [Dictionaries](https://docs.python.org/3/tutorial/datastructures.html#dictionaries), focusing on looking up a key with `get` and providing a default value.
- Optional Udemy supplement: **Python Variables (3:37), if present in the learner's course catalog**. Course access is unverified; skip it if it is not already available. Do not buy or enroll for this plan.

### Worked example (20 minutes)

In Python a dictionary is a small set of labeled values. Here, `record` is the dictionary and `"title"` is the label to look up:

```python
def read_title(record):
    return record.get("title", "")

read_title({"title": "Appeals"})  # "Appeals"
```

`get` returns the value for that label. If the label is missing, it returns the second value (`""`) instead of crashing. This is a worked example, not your assigned function.

### Your small coding task (40 minutes)

**File:** [`ai/fde_v2/beginner_first15/learner.py`](../../../ai/fde_v2/beginner_first15/learner.py)
**Function:** `document_title`

`record` is the supplied dictionary. Return the value stored under its `"title"` label; if that label is missing, return an empty string.

Assigned input: `{"document_id": "deductible", "title": "Deductible"}`
**Expected result:** Deductible

The app plumbing, fixtures, command-line runner and database setup are supplied. You are learning a small function, not building the full application. Reference output teaches the behavior but does not count as your implementation.

### Check your work (15 minutes)

```sh
python3 -m ai.fde_v2.beginner_first15.acceptance --day 1
```

This checks today’s learner behavior. An unsolved TODO should say `NOT YET`; do not switch to instructor reference code to make it green.

## Next step

The next page adds one small step. Keep the supplied application and demo unchanged.
