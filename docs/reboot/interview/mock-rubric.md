# 45-minute interview mock and rubric

The mock is an interview simulation, not a post-hoc design essay. Run its
45 minutes inside the existing 85-minute practice allocation and leave the
rest of the daily engineering budget unchanged. The segment lengths are
**5/5/15/10/10 minutes**.

| Minutes | Activity | Rule |
|---:|---|---|
| 5 | Clarify | Learner asks about users, inputs, constraints, and must-not-happen cases. |
| 5 | First proposal | Learner writes their own boundary, invariant, and first test before hints, reference answers, or AI. Save this version. |
| 15 | Deep practice | Develop the smallest design that satisfies the stated contract; make a runnable change when the assigned task supports it. |
| 10 | Failure and trade-off | Walk one failure, one consistency/security boundary, and the operational cost of the chosen approach. |
| 10 | Feedback and evidence | Score the rubric, record the exact command/output, then optionally compare with an AI suggestion. Keep the initial proposal unchanged. |

## Score each dimension 0–2

- **Problem framing:** 0 misses the user or constraints; 1 identifies the happy
  path but leaves a material ambiguity; 2 asks useful questions and states
  observable acceptance.
- **System boundary:** 0 assigns authority or state to the wrong component; 1
  has a workable boundary with one unclear responsibility; 2 identifies
  ownership, trust boundary, and transaction or API boundary correctly.
- **Correctness and data:** 0 misses the core invariant; 1 states it but has no
  convincing durable enforcement; 2 ties the invariant to a concrete model and
  concurrency/replay behavior.
- **Failure and operations:** 0 has no failure path; 1 handles a local error
  but misses recovery/observability; 2 states a bounded failure response and
  how to detect or recover it.
- **Verification and communication:** 0 gives no testable evidence; 1 names a
  test but cannot interpret its result; 2 predicts a negative case and explains
  actual runnable evidence clearly.

Record scores individually; do not average away a security or data-integrity
failure. Mark hints, AI, or prior exposure. A score measures this practice
attempt only, not employer readiness.
