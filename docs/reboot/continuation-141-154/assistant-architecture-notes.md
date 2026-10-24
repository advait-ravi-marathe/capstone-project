# Assistant-provided architecture notes: Days 141–154

These are capstone implementation decisions supplied by the assistant. The learner implements and tests the named seam. System-design practice stays in the separate interview question set; no capstone architecture proposal or post-hoc justification is assigned.

## Day 141 — Bind a real baseline and candidate artifact pair

Treat artifact identity as the measured JAR digest plus truthful source, build, and capability provenance. Require a same-database/schema fixture for the pair. A filename or declared label cannot establish that two binaries differ or that a capability exists.

## Day 142 — Project existing recovery evidence into a report

Map the existing immutable recovery observation and classifier result into a narrow allowlisted DTO. Keep history supplementary and bounded. Reusing the classifier avoids inventing a second recovery policy, and DTO mapping adds no database authority.

## Day 143 — Encode one framed JSON report

Use the Spring Boot-managed Jackson mapper and serialize the complete DTO before writing the fixed prefix and one compact JSON record. Separate diagnostics from the data frame so partial serialization cannot be mistaken for a result.

## Day 144 — Decode bounded structurally valid evidence

Parse exactly one size/depth-bounded frame, reject duplicate JSON members, validate required types and requested identity, then compare the recommendation to the existing classifier. Structural validity alone cannot prove that the report reflects database state.

## Day 145 — Reject mixed or malformed inspection requests early

Parse one explicit read-only mode and exact key before creating Spring or a datasource. Keep all mutation/delivery modes out of this command boundary; early refusal ensures invalid input has no service or database side effect.

## Day 146 — Run one real read-only inspection

Use an isolated non-web Spring context with Flyway and SQL initialization disabled. Verify the existing V001–V005 contract before querying and compare complete database snapshots around inspection. A missing schema is a refusal, never an invitation to migrate.

## Day 147 — Recovery checkpoint

Rerun only Days 141–146, repair the earliest failed prerequisite, and preserve later items as deferred. Treat an independent database oracle as primary; supplementary report history cannot replace it.

## Day 148 — Run distinct packaged artifacts

Freeze artifact A before B-only code, then build B independently. Bind both digests and provenance, verify A lacks the additive entry point, and prove B executes the new report mode while both preserve the same fixture.

## Day 149 — Prove retained state across A and B

Run quarantine, release, retry, and replay through A/B/A against one retained database without resetting or restoring between requests. Compare ordered full-state snapshots at every boundary; an identifier or row-count check can hide altered payloads.

## Day 150 — Gate fallback on measured pair evidence

Permit fallback only when the actual artifact identities, fixture/schema, legacy behavior, new report mode, retained-state matrix, and cleanup evidence all match. Missing or stale evidence blocks the transition; policy fixtures alone do not prove runtime compatibility.

## Day 151 — Reject deceptive candidate evidence

Compare report fields and recommendation against actual independent database state and process outcome. Reject plausible-looking canned output, identity drift, same-JAR relabeling, or nonzero exit even when a report-like line was printed.

## Day 152 — Bound child-process output and cleanup

Run verifier children with finite time and output limits, terminate and await only owned processes, and keep one report frame separate from diagnostics. Verify an unrelated sentinel survives; broad process cleanup can harm unrelated work.

## Day 153 — Complete the same-database A to B to A rehearsal

Run the complete frozen A/B matrix on one owned PostgreSQL fixture, compare full data and unchanged schema at each operation, and record artifact provenance and cleanup. This demonstrates local unchanged-schema compatibility only.

## Day 154 — Recovery and defend the limited compatibility claim

Rerun prior Days 141–153 only. State which evidence is local and what remains unproven, including schema-changing releases, zero downtime, production identity, public availability, and AWS deployment. Add no feature or cloud action.
