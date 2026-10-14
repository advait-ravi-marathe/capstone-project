# CAP-62 — Bound async retrieval and keep cancellation honest

**Learner outcome:** Bound retrieval waiting and preserve cancellation as cancellation.

- Learner seam: `bounded_lookup` in `ai/coverstory/cap_62.py`.
- Synthetic timing/cancellation vectors: `fixtures/coverstory/CAP-62/synthetic-v1.json`.
- Acceptance suite: `ai/coverstory/tests/test_cap_62.py`.
- Instructor-only setup check: `ai/tests/test_coverstory_cap62_infrastructure.py`.

The saved cases cover a successful completion before deadline, an indefinite operation that times out and is cleaned up, outer-task cancellation propagation, a worker that finishes late after timeout, and a zero timeout rejected before the operation starts. The acceptance tests use local async fakes only; there is no broker, cache service, real retrieval backend, or business workflow.

The `bounded_lookup` body remains a learner TODO. The instructor setup check only verifies the typed seam and fixture vector shape. The learner suite must not be counted as a current-candidate result until the learner supplies the implementation and records the candidate and raw output.

```sh
python3 -m unittest discover -s ai/tests -p 'test_coverstory_cap62_infrastructure.py'
python3 -m unittest discover -s ai/coverstory/tests -p 'test_cap_62.py'
```
