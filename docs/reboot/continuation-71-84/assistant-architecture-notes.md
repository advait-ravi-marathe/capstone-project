# Assistant-provided architecture notes: Days 71–84

These concise decisions are supplied before implementation and fit inside each day’s existing 25-minute study allocation. The learner implements and tests the stated seam. No capstone design proposal or post-hoc architecture justification is assigned. System-design proposals belong only to the separate interview question sessions.

The source notes are checked against the named methods and acceptance harnesses. They do not complete learner TODOs or prove runtime outcomes.

## <a id="day-71"></a> Day 71: Establish the measured baseline without adding runtime behavior

Keep the existing route synchronous: request → owner-scoped read adapter → JDBC/Hikari. The pool maximum of four and 1,000 ms acquisition timeout are the measured baseline; the two-permit gate is a later bounded change. This day records prerequisites and observations only.

## <a id="day-72"></a> Day 72: Reject unsafe capacity configuration before serving

Validate positive permit and pool sizes, permit capacity no greater than the pool maximum, and the project's 250–30,000 ms Hikari acquisition-timeout range in `PreviewCapacityPolicy.validate()`. The dedicated `Sprint6PreviewCapacityConfiguration` validates during bean construction so invalid settings fail at startup; the broader later runtime is not part of this sprint profile.

## <a id="day-73"></a> Day 73: Use immediate semaphore admission and return a lease

`PreviewAdmission.tryEnter()` uses `Semaphore.tryAcquire()` so a full gate returns empty immediately, with no waiter or queue. Successful acquisition creates a `PreviewLease` whose single close releases exactly one permit.

## <a id="day-74"></a> Day 74: Make lease close idempotent under races

Guard the release callback with `AtomicBoolean.compareAndSet(false, true)`. Only the thread that changes the state releases the permit; repeated or concurrent `close()` calls do nothing.

## <a id="day-75"></a> Day 75: Decorate the existing owner-scoped read and release in finally

`BoundedPolicyRenewalReadStore` acquires before delegating once with the original policy and owner IDs, and closes the lease in `finally` on success, empty, and failure. Translate only `CannotGetJdbcConnectionException` to the safe unavailable signal; preserve unrelated domain exceptions.

## <a id="day-76"></a> Day 76: Prove the two-entry ceiling with latches and joined futures

Use a two-party latch to observe both admitted calls inside the delegate, make the third call synchronously return busy before delegation, and join every worker `Future`. Release the barriers in `finally`; then prove recovery after a thrown delegate failure and a complete later admission cycle.

## <a id="day-77"></a> Day 77: Keep recovery testing at the store boundary

Use deterministic test doubles to occupy both permits, assert a saturated call never reaches the delegate, close test-owned leases, and verify a subsequent useful read delegates. Avoid test-only HTTP fault routes; real pool recovery is a separate later test.

## <a id="day-78"></a> Day 78: Map only bounded-capacity signals to fixed safe responses

`PreviewCapacityExceptionHandler` maps busy and pool-unavailable exceptions to their distinct fixed 503 codes without exception text. Leave unrelated exceptions to normal error handling; never infer an outcome by parsing a message.

## <a id="day-79"></a> Day 79: Wrap the existing read bean once as a singleton

In `Sprint6LocalConfiguration`, inject the existing `PolicyRenewalReadStore`, shared `PreviewAdmission`, and `PreviewMetrics` into one singleton `BoundedPolicyRenewalReadStore`. Preserve the exact delegate instance and existing SQL adapter; validate settings before the application serves requests.

## <a id="day-80"></a> Day 80: Record bounded timer and counter series by a closed outcome enum

`PreviewMetrics.record()` writes a request counter and latency timer tagged only with the finite `Outcome` enum. Do not use policy, owner, or request identifiers as tags. The route integration remains responsible for recording success, empty, busy, unavailable, and unexpected failure outcomes.

## <a id="day-81"></a> Day 81: Represent recovery as an accessible state with explicit manual retry

Render busy/unavailable state with the existing policy context, an accessible status/alert, and one Retry button. Only the user's button action invokes the existing preview operation; do not auto-retry, reset the session, or imply policy mutation.

## <a id="day-82"></a> Day 82: Exercise Hikari exhaustion with four test-owned connections

Hold all four test connections, call through the bounded store, observe the configured finite acquisition failure, and release all holders in `finally`. Then prove the same read succeeds and all application permits are available again.

## <a id="day-83"></a> Day 83: Verify the packaged route under a real PostgreSQL relation lock

Launch the fresh local JAR and real Chromium against disposable PostgreSQL. Hold `ACCESS EXCLUSIVE` on the policy table, observe two preview sessions waiting in PostgreSQL, and confirm the third is rejected before JDBC. Release the lock in `finally`, verify manual browser retry and fixed metrics, and compare complete policy/event/idempotency rows.

## <a id="day-84"></a> Day 84: Close with an evidence-based integration review

Use the installed selectors and actual evidence to classify implementation, infrastructure, and integration separately. Record the first unresolved prerequisite and next bounded recovery action; add no feature and do not turn expected TODO-red or a source-shape check into an integration pass.
