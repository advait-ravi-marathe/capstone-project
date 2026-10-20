# Small system design inside the daily engineering block

Use system design as the short reasoning step that shapes the feature being
built that day. It is part of the existing 180-minute plan, not a separate
assignment: 85 engineering minutes include design, implementation and testing;
25 concept/course minutes cover the day's relevant design topic; 10 evidence
minutes capture the decision and result. Choose only the artifact the feature
needs. Do not draw every diagram type every day.

For each feature, answer briefly:

1. **Requirement and target:** What does the user need? What measurable quality
   target matters today (latency, throughput, data retention, availability,
   recoverability), if any? Do not invent a target without a reason.
2. **Invariant:** What must always be true in domain terms or stored data?
3. **Responsibility and boundary:** Which module owns the rule? Where does a
   transaction begin and end? What failure can cross the boundary?
4. **One useful model:** Pick a module/UML view, ER diagram, sequence diagram,
   or data-flow diagram appropriate to the day's change.
5. **Alternatives:** Record one short ADR when a design choice has a material
   trade-off. Name at least one rejected option and why it did not fit this
   requirement.
6. **Verification:** Add one failure case or load/concurrency check relevant
   to the invariant. Compare measurements to a baseline only when a later
   contract calls for a measurable comparison.
7. **Trade-off:** Explain what the chosen design makes easier and what it costs.

Start as a modular monolith with clear module ownership and transaction
boundaries. Add a cache, Kafka, another service, or horizontal scaling only
after a measured requirement or failure demonstrates why the extra moving
parts are needed. PostgreSQL exercises should explain keys, normalization,
constraints, and isolation as the feature requires. Later index and concurrency
work should compare a repeatable baseline with the changed design.

Use the blank [design note](templates/design-note.md), [diagram prompt](templates/diagram-prompt.md),
and [ADR template](templates/adr.md). Across the month, the appropriate prompts accumulate ER, module/UML, sequence,
data-flow/trust diagrams and short ADRs; a day calls for only the view that
answers its specific question. Across the month, the appropriate prompts accumulate ER, module/UML, sequence,
data-flow/trust diagrams and short ADRs; a day calls for only the view that
answers its specific question. These templates are prompts, not completed
architecture answers. A day's contract identifies the one design question to
focus on; all work stays inside the stated daily budget.
