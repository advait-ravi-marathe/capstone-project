# 45-minute interview mock and rubric

The mock is an interview simulation, not a post-hoc design essay. Run its
45 minutes inside the existing 85-minute practice allocation and leave the
rest of the daily engineering budget unchanged. Follow the named
**5/5/15/10/10-minute** phases below; do not replace them with a separate
feedback or reflection block.

| Minutes | Activity | Rule |
|---:|---|---|
| 5 | Requirements | Clarify users, goals, critical flows, and measurable acceptance. Save the learner's initial understanding before any answer. |
| 5 | Capacity and constraints | Estimate a bounded starting workload and state latency, durability, privacy, budget, and must-not-happen constraints. Label assumptions. |
| 15 | Initial architecture and data model | Before hints, reference answers, or AI, draw the initial boundary and request/data flow, choose core records and keys, state one invariant, and save the first test. Preserve this proposal. |
| 10 | Bottlenecks, reliability, scaling | Identify likely bottlenecks and partial failures; explain timeouts, retries, recovery, and how the design changes as measured load grows. |
| 10 | Trade-offs, failures, follow-ups | Compare alternatives, walk a concrete failure and its consequence, name evidence that could change the choice, and write the next follow-up question. Apply the rubric to this attempt without adding a post-hoc essay. |

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

Ratings 1 and 3 represent evidence between the adjacent anchors. Apply the
rubric during the final 10-minute trade-off/failure/follow-up phase. Total the
points transparently; report any critical authorization, data-integrity, or
unsafe-retry failure separately rather than hiding it in the total. Mark hints,
AI, and prior exposure. The score measures this practice attempt only, not
employer readiness.
