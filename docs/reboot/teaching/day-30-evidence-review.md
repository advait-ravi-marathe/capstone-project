# Day 30 — Explain only what the evidence supports

This is a review and communication day. No new course or feature is added. Use 25 minutes to review the weakest already-taught boundary, 65 minutes for the appropriate existing build/test work, 20 minutes for the existing mock, and 10 minutes for evidence. The mock replaces part of practice; it does not add time.

## Three evidence states

- **Component only:** a component assertion passes and a local artifact exists, but the integrated browser/database path was not run. This supports a component claim only.
- **Attempted, blocked:** the browser opens, then an ownership TODO fails before a transaction; no new row is visible after a fresh read. This is a blocked integration, not a pass.
- **Integrated evidence:** the actual end-to-end flow, fresh reload read, row-count check, and controlled failure checks pass with a recorded command, exit status, and linked evidence. Only this supports the integrated claim.

An instructor checklist-integrity test proves the checklist format and status rules; it does not transform the first two states into the third.

## A concise evidence-based explanation

“Database uniqueness arbitrates competing selections; the UI pending guard only reduces duplicate clicks. In this test, two calls returned the same stored key and the observed row count was one. This does not establish performance at ten times the load. My next step would be to measure the existing query plan against a representative workload.”

Keep four things separate: invariant, observed evidence, limitation, next diagnostic. For the real slice, choose one implemented boundary and cite its actual output. If a prerequisite or test is missing, say so and keep the status open or blocked. Do not invent a customer, incident, traffic level, or completion claim. The Day 30 integrity selector checks evidence/checklist structure, not engineering mastery.
