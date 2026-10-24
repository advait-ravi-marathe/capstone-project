# Assistant-provided architecture notes: Days 113–126

These notes state the implementation decisions for the local training system.
The learner implements and tests the assigned seam. System-design interview
practice is a separate question-bank exercise; no capstone architecture proposal
or post-hoc design justification is assigned.

## Day 113 — Classify durable recovery evidence

Keep `RecoveryDecisionPolicy` as a pure classifier over one complete
`RecoveryObservation`. It does not query storage or attempt delivery. This
separates evidence interpretation from side effects and makes missing, matching,
conflicting, acknowledged, and quarantined states testable as a decision table.

## Day 114 — Constrain reversible recovery metadata

Keep V005 additive to V004. Store quarantine timestamp, fixed reason, control
revision, and append-only decision history separately from the immutable
issuance source and delivery receipt. The migration is instructor-owned; the
learner verifies its constraints and preservation behavior.

## Day 115 — Inspect one retained work identity

Use one exact `(policy_id, source_version)` query to map the retained work and
optional receipt into `RecoveryObservation`. Query the last 20 control-history
rows separately as supplementary context. A failed query remains an error,
never an `ABSENT` receipt.

## Day 116 — Quarantine atomically

`JdbcManualRecoveryStore.quarantine` locks the work row, observes current
receipt/acknowledgment state, checks the expected revision and eligibility, then
updates control columns and appends history in the same transaction. A rollback
must remove both writes while preserving source, work payload, and receipts.

## Day 117 — Release the expected hold revision

Release uses the same work-row lock and checks that the requested revision is
the current active hold. It clears only hold metadata, advances control revision,
and appends a release record. It does not repair conflicting data or alter the
source event, work payload, receipt, or acknowledgment.

## Day 118 — Filter held work before the batch limit

`RecoveryAwareIssuanceWorkStore` wraps the existing JDBC store for receipt and
acknowledgment gates. Its pending query excludes quarantined work in SQL before
ordering and `LIMIT`; filtering a limited delegate result could hide eligible
work behind held rows. The work-row lock remains shared with control transitions.

## Day 119 — Recovery checkpoint

Use the existing Days 113–118 acceptance only. No new seam is introduced; repair
the earliest actual learner failure and keep later recovery work deferred.

## Day 120 — Retry one identity across two commits

`ManualRecoveryService.retryOne` owns the orchestration. It first inspects and
locks the exact key, commits receipt creation or reconciliation, then commits
acknowledgment separately. This preserves a durable receipt if the acknowledgment
step fails and permits a later explicit reconciliation. It never adds an
automatic in-process retry.

## Day 121 — Report uncertain outcomes

Use the observation captured before an attempt and its verified result to map a
successful outcome. If the caller loses the reply or storage is unavailable, do
not infer whether a commit occurred; perform a fresh exact-key read or surface
the error as unknown. A receipt's original payload and timestamp remain stable.

## Day 122 — Serialize competing recovery commands

Quarantine, release, receipt creation, and acknowledgment all lock the same work
row. After a lock wait, evaluate receipt evidence in a new READ COMMITTED
statement so a transaction that just committed a receipt is visible. Tests use
barriers and PostgreSQL lock observation, not guessed sleeps.

## Day 123 — Keep decisions across fresh JVMs

Persist recovery state in PostgreSQL and reconstruct services in each child JVM.
The test keeps the same database running while replacing application processes;
it proves process restart behavior only, not database crash recovery.

## Day 124 — Expose one explicit local command

`LocalCoverStoryApplication` dispatches explicit recovery-action arguments to
the minimal non-web `RecoveryOnlyApplication`. Its runner validates one action,
one exact key, and the required revision/reason before calling the service.
Ordinary startup remains inert, and mixed delivery/recovery flags fail before
mutation.

## Day 125 — Verify packaged operational recovery

Build the standard executable local JAR with profile `local`. When an explicit
recovery action is present, its main entry point hands off to the minimal
recovery-only context; the packaged selector uses a disposable PostgreSQL
instance and full row snapshots. It makes no browser/UI claim.

## Day 126 — Recovery exit gate

Run only the already introduced recovery selectors and review their real outputs.
Keep unresolved learner seams, setup blockers, and unsupported external-delivery
claims visible. No feature or new runtime behavior is added.
