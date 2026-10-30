# Day 4: Read fields from a document

**Status: Not started.** Follow this page in order. A missed date does not count as a finished day.

**Planned date:** 2026-11-01 (calendar target only; it does not mark the day complete).

## Today: 180 minutes total

- **60 min DSA:** Use the current item in the separate DSA repository. DSA problems and answers are not copied here.
- **30 min AWS:** Learn the short topic below. No AWS account or console is needed.
- **90 min AI/Python:** Learn one idea, follow the example, make one small code change, then run its check. If DSA review runs long, reduce AI time to stay within 180 minutes and carry the unfinished work forward.

## AWS learning — 30 minutes

**Topic:** Understand why Availability Zones are separated

**Read this exact section:** REL10-BP01; deploy to multiple locations, especially Availability Zones

Read the linked official section for about 20 minutes. Spend the remaining 10 minutes on this learning check: Explain how separating resources across Availability Zones can reduce one building-level failure affecting everything. This is learning, not a readiness score; no AWS account, console action, or spend is needed.

Source: [Understand why Availability Zones are separated](https://docs.aws.amazon.com/wellarchitected/latest/reliability-pillar/rel_fault_isolation_multiaz_region_system.html)

## AI/Python learning — 90 minutes

**Python reference:** [Dictionaries](https://docs.python.org/3/tutorial/datastructures.html#dictionaries). Read only this section.
- Optional Udemy supplement: **Dictionaries (4:04), if accessible**. Course access is unverified; skip it if it is not already available. Do not buy or enroll for this plan.

### Worked example (20 minutes)

Run the supplied example command; it uses the completed instructor reference, not your learner function:

```sh
python3 -m ai.fde_v2.beginner_first15.examples --day 4
```

It prints the example input and output. Expected output:

```text
['plan', 'family', 'coverage']
```

This walkthrough shows the behavior only. Your learner check below still tests your own function.


### Your small coding task (40 minutes)

**File:** [`ai/fde_v2/beginner_first15/learner.py`](../../../ai/fde_v2/beginner_first15/learner.py)
**Function:** `record_words`

Read title and text fields from a supplied record and return their words.

Assigned input: `{"title": "Deductible", "text": "Individual coverage is 500 dollars"}`
**Expected result:** ['deductible', 'individual', 'coverage', 'is', '500', 'dollars']

The app plumbing, fixtures, command-line runner and database setup are supplied. You are learning a small function, not building the full application. Reference output teaches the behavior but does not count as your implementation.

### Check your work (15 minutes)

```sh
python3 -m ai.fde_v2.beginner_first15.acceptance --day 4
```

This checks today’s learner behavior. An unsolved TODO should say `NOT YET`; do not switch to instructor reference code to make it green.

## Next step

The next page adds one small step. Keep the supplied application and demo unchanged.
