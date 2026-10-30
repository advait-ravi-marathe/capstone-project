# Day 12: Check a document record

**Status: Not started.** Follow this page in order. A missed date does not count as a finished day.

**Planned date:** 2026-11-09 (calendar target only; it does not mark the day complete).

## Today: 180 minutes total

- **60 min DSA:** Use the current item in the separate DSA repository. DSA problems and answers are not copied here.
- **30 min AWS:** Learn the short topic below. No AWS account or console is needed.
- **90 min AI/Python:** Learn one idea, follow the example, make one small code change, then run its check. If DSA review runs long, reduce AI time to stay within 180 minutes and carry the unfinished work forward.

## AWS learning — 30 minutes

**Topic:** Learn cost-aware compute sizing

**Read this exact section:** COST06-BP03; select resource type, size, and number using metrics

Read the linked official section for about 20 minutes. Spend the remaining 10 minutes on this learning check: Explain why observed CPU and memory use should inform instance size; do not invent savings without measurements. This is learning, not a readiness score; no AWS account, console action, or spend is needed.

Source: [Learn cost-aware compute sizing](https://docs.aws.amazon.com/wellarchitected/latest/cost-optimization-pillar/cost_type_size_number_resources_metrics.html)

## AI/Python learning — 90 minutes

**Python reference:** [Mapping/dictionary lookups](https://docs.python.org/3/library/stdtypes.html#mapping-types-dict). Focus on `get`, checking whether a value is a dictionary, and required keys.
- Optional Udemy supplement: **Python Variables (3:37), revisit if accessible**. Course access is unverified; skip it if it is not already available. Do not buy or enroll for this plan.

### Worked example (20 minutes)

The assistant-supplied wrapper builds the `Document` and calculates its text hash. Your part is only to decide whether the dictionary has the five required fields, whether the four text values are nonempty, and whether `version` is a positive integer.

```python
record = {"document_id": "toy", "version": 1, "title": "Guide", "source": "fixture/guide", "text": "hello"}
isinstance(record, dict)  # True
record.get("title", "")  # "Guide"
```

This example has a valid shape. The assigned sample below is different.

### Your small coding task (40 minutes)

**File:** [`ai/fde_v2/beginner_first15/learner.py`](../../../ai/fde_v2/beginner_first15/learner.py)
**Function:** `document_record_valid`

Return `True` only when the supplied dictionary has exactly `document_id`, `version`, `title`, `source`, and `text`; `version` must be a positive `int` (not `True`), and the other four values must be nonempty strings. Otherwise return `False`. The supplied CoverStory wrapper uses your check, then constructs the typed record and calculates SHA-256 for you.

Assigned input: `{"document_id": "sample", "version": 1, "title": "Sample", "source": "fixture/sample", "text": "short text"}`
**Expected result:** The assigned record is accepted; blank text, missing/extra fields, `True`, or version `0` is rejected. The supplied wrapper returns the expected `Document` and hash only after your validator passes.

The app plumbing, fixtures, command-line runner and database setup are supplied. You are learning a small function, not building the full application. Reference output teaches the behavior but does not count as your implementation.

### Check your work (15 minutes)

```sh
python3 -m ai.fde_v2.beginner_first15.acceptance --day 12
```

This checks today’s learner behavior. An unsolved TODO should say `NOT YET`; do not switch to instructor reference code to make it green.

## Next step

The next page adds one small step. Keep the supplied application and demo unchanged.
