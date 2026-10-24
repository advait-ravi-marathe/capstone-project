# Assistant-provided architecture notes: Days 99–112

These notes state the capstone implementation choices and why they fit the existing synthetic local workflow. The learner implements and tests the named seams; system-design decision practice remains in the separate interview question bank. No capstone proposal or post-hoc design justification is assigned.

<a id="day-99"></a>
## Day 99

**What and why:** Validate the envelope before it reaches persistence. `IssuanceEnvelope` is a small immutable value containing policy ID, source version, event type, effective date and schema version; `validateSupported()` rejects blank identity, nonpositive version, non-ISSUED types, absent date or unknown schema. This keeps the delivery projection narrow and prevents invalid rows from reaching the store. Day 35 remains the atomic source-of-truth transaction.

<a id="day-100"></a>
## Day 100

**What and why:** Use the supplied additive V004 migration to create separate work and receipt records keyed by `(policy_id, source_version)`. The source event foreign key ties work to an existing event; an ISSUED-only check and non-null payload fields constrain captured work; receipt references work. Database constraints protect these invariants even when a caller is buggy. The learner inspects the instructor migration and does not write SQL.

<a id="day-101"></a>
## Day 101

**What and why:** Materialize work by rescanning committed ISSUED events and anti-joining missing `(policy_id, version)` keys. Order deterministically and insert at most the requested number with conflict-safe insertion. Do not advance a maximum-key cursor: a transaction for an earlier key may commit after a later key. Reconciliation sees it on a subsequent call.

<a id="day-102"></a>
## Day 102

**What and why:** Read captured work rows, not current lifecycle events, because the event payload is an immutable historical fact. Apply a validated finite limit, stable key ordering and `acknowledged_at IS NULL`; map the stored schema version and reject unsupported versions. This makes later source-row edits irrelevant to the captured delivery payload.

<a id="day-103"></a>
## Day 103

**What and why:** Accept a receipt only when its stable key and immutable payload match the captured work row. Insert it once; on duplicate identity compare the stored payload and report identical replay without changing the original timestamp; mismatched payload is a visible conflict. The receipt is the complete local synthetic effect, not a call to an external provider.

<a id="day-104"></a>
## Day 104

**What and why:** Acknowledge only work with a matching committed receipt. Update the pending marker once and preserve the first `acknowledged_at`; a repeat does not rewrite it. Keeping the receipt check and acknowledgment predicate at the store boundary makes the completion rule durable and locally enforceable.

<a id="day-105"></a>
## Day 105

**What and why:** Keep recovery limited to the already assigned Days 99–104. Use independent fresh-connection observations of source event, work, receipt and acknowledgment at each known boundary; rerun the existing selectors and repair only the first failed prerequisite. Day 100 is an instructor-owned migration assessment. No dispatcher, process restart or new feature belongs in this recovery block.

<a id="day-106"></a>
## Day 106

**What and why:** `OneBatchDispatcher.runOnce(limit)` first rejects an ambient transaction, then performs one capture transaction and reads one bounded page. For each row it commits receipt creation before a separate acknowledgment transaction. Stop on the first error and report only already committed progress; do not loop, auto-retry or skip. The separated transactions preserve visible recovery state.

<a id="day-107"></a>
## Day 107

**What and why:** Use two independent connections to control commit order: keep earlier key A uncommitted, commit and capture later key B, then commit A and reconcile again. The expected work set must contain both A and B, while rolled-back and non-ISSUED events remain absent. A high-watermark implementation loses A; the recurring anti-join is the chosen repair.

<a id="day-108"></a>
## Day 108

**What and why:** Inject a failure after receipt work is attempted but before the receipt transaction commits. Observe from fresh connections that the receipt rolled back, pending work remains, and acknowledgment is absent. Then explicitly retry and confirm one receipt plus acknowledgment. This tests recovery of a transaction boundary, not automatic retry policy.

<a id="day-109"></a>
## Day 109

**What and why:** Use a child JVM that commits the receipt, writes a synchronization marker, then blocks before acknowledgment. The parent verifies the durable receipt and pending work, terminates and reaps that JVM, and starts a fresh JVM against the same live disposable database. Replay completes acknowledgment without duplicating the receipt. This proves process restart behavior only, not database crash durability.

<a id="day-110"></a>
## Day 110

**What and why:** Keep the command opt-in. `LocalIssuanceDeliveryCommand` returns without dispatch during normal web startup and only runs for `--coverstory.delivery.run-once`; it uses non-web mode for command execution. Parse one batch size in `[1,20]`, print truthful committed counts, return usage code 2 for invalid arguments and nonzero on dispatch failure. No scheduler or background worker is introduced.

<a id="day-111"></a>
## Day 111

**What and why:** Exercise the built local JAR with a real authenticated Java HTTP client and disposable PostgreSQL. Reuse the native issue endpoint and same request key to assert 201 then 200 with identical policy identity and unchanged full snapshots. Invoke the explicit one-shot command against committed work, then verify preview remains read-only. This is packaged HTTP API evidence, not Chromium UI evidence.

<a id="day-112"></a>
## Day 112

**What and why:** Use the existing selectors to assess recovery across capture, receipt, acknowledgment, child-process restart, packaged command/API and preview-read boundaries. Diagnose a deliberately bad high-watermark or early acknowledgment by its persisted row effect. Keep unresolved prerequisite failures and setup blockers separate; add no new feature or completion claim.
