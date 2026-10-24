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

## 100-point rubric

Rate each dimension from 0 to 4. Award `dimension weight × rating ÷ 4` points;
the seven weights total 100. Use the anchors as observable evidence, not as a
confidence or personality score.

| Dimension | Weight | 0 | 2 | 4 |
|---|---:|---|---|---|
| Requirements and constraints | 15 | Misses the user outcome or invents requirements. | States main use cases but misses an important constraint or acceptance condition. | Clarifies actors, workload, latency/durability needs, must-not-happen cases, and measurable acceptance. |
| Architecture and boundaries | 20 | Components, ownership, or authority are misplaced. | A workable flow exists, but a key responsibility or failure boundary is unclear. | Shows an appropriately simple boundary, trust/ownership, APIs, and the database or external-system responsibility. |
| Data consistency and correctness | 20 | Core invariant or state transitions are wrong. | Invariant is named but enforcement, concurrency, or replay is incomplete. | Data model and constraints enforce the invariant under retry/concurrency; state and readback are consistent. |
| Scalability and capacity | 10 | No workload reasoning; proposes unbounded resources. | Identifies a likely bottleneck but lacks a target or evidence. | Gives a bounded workload estimate, bottleneck observation, and a scale path with limits. |
| Reliability and operations | 15 | No useful failure, recovery, or diagnostic path. | Handles one failure but leaves uncertain outcomes or recovery unclear. | Bounds timeouts/retries, identifies partial failure, and names monitoring/recovery evidence. |
| Trade-offs and alternatives | 10 | Declares a pattern without fit or cost. | Compares one plausible alternative but misses its consequence. | Compares viable options, states concrete costs/failure modes, and names evidence that would change the choice. |
| Communication and verification | 10 | Cannot make the proposal or evidence understandable. | Gives a coherent summary but has a weak test or does not interpret its result. | Communicates in stages, predicts a negative case, and explains actual runnable evidence and remaining limits. |

Ratings 1 and 3 represent evidence between the adjacent anchors. Total the
points transparently; report any critical authorization, data-integrity, or
unsafe-retry failure separately rather than hiding it in the total. Mark hints,
AI, and prior exposure. The score measures this practice attempt only, not
employer readiness.
