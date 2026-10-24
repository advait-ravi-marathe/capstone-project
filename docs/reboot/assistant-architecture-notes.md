# Assistant-provided architecture notes: Sprint 1

These concise decisions belong to the capstone implementation work. They are
provided by the assistant before the learner codes; the learner implements and
tests the stated behavior. They fit inside the existing 25-minute concept
allocation and replace any task-specific design-proposal prompt. No capstone
ADR, architecture choice, or post-hoc trade-off essay is required.

System-design practice remains separate: it happens only in the assigned
question-bank prompts and dedicated 45-minute interview mocks. In those mocks,
the learner practices proposing a design as an interview skill.

## Day 1: Quote selection boundary

Keep the existing domain rule in the pure `QuoteSelectionPolicy`; let the
existing HTTP adapter translate request and response data. This isolates
ordering/deduplication behavior from transport, and avoids changing the
production wiring while the Day 2 validator remains unfinished.

## Day 2: Validate before delegation

Validate the request at the controller boundary before invoking the
collaborator. Keep transport validation separate from the learner-owned
domain policy; this makes malformed input a safe HTTP response and allows a
call-count test to prove invalid requests do not reach the service.

## Day 3: Centralized safe error mapping

Use the supplied controller advice to map expected exceptions to stable
client responses and unexpected exceptions to a sanitized server response.
Keep internal exception details out of public bodies. This centralizes HTTP
translation while leaving domain decisions in their service/policy.

## Day 4: Immutable catalog boundary

Return an immutable copy of the catalog collection from the catalog adapter;
keep the existing product values and order. The defensive collection copy
prevents caller changes to list membership, while the existing immutable
product values avoid a deeper mutable graph.

## Day 5: Pure eligibility policy

Keep eligibility as a deterministic policy over the supplied product and
applicant snapshot. Do not add database or controller dependencies: this
makes the domain rule directly testable and keeps data access outside the
decision.

## Day 6: Explicit decimal calculation

Use `BigDecimal` for premium calculation and an explicit scale/rounding rule
from the contract. Avoid binary floating point so the same input yields the
same monetary result across runs.

## Day 7: Fixed-clock quote version

Inject `Clock` and create an immutable quote snapshot with an explicit
expiry instant. This makes creation and expiry boundaries deterministic in
tests, and keeps a later quote version from mutating an earlier snapshot.

## Day 8: Database-enforced quote invariants

Use PostgreSQL constraints for required values, keys, and uniqueness, with
foreign keys for relationships. The database remains the final integrity
boundary even if a caller bypasses application validation.

## Day 9: Parameterized snapshot persistence

Use the existing JDBC adapter with prepared statements and explicit
row-to-snapshot mapping. Bind `journeyId` and `quoteVersionId`, the adapter's
composite lookup scope, so values remain data and the lookup returns absence
when that pair is missing. Authentication and owner authorization are a
separate boundary, introduced on Day 12 through the trusted principal; do not
add an owner argument to this Day 9 adapter.

## Day 10: One connection, one transaction

Keep the batch write and its related records on one connection/transaction;
commit only after all writes succeed and roll back on failure. This prevents
a partially persisted selection batch.

## Day 11: Declared freshness-check order

Apply the existing freshness checks in the contract's specified order and
use the injected current time. Stable precedence gives callers a predictable
result when more than one stale condition is present.

## Day 12: Trusted principal owns the read

Resolve the owner from the authenticated principal, never from a request
field. Apply that owner predicate in the data access path; this prevents a
caller from changing identity by forging a query or body value.

## Day 13: Fail-closed CI workflow

Keep CI as an explicit sequence of checkout, Java setup, and the repository's
Maven verification command. Let the shell/workflow fail on a nonzero test
result so a broken acceptance run cannot be reported as green.

## Day 14: Diagnose the existing vertical slice

Use the bounded integration test to find and report the first incomplete
dependency. Preserve the existing application composition and learner TODOs;
do not redesign wiring or replace an unfinished dependency with a fake pass.
