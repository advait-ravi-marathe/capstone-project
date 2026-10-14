# CoverStory journey, audit, analytics and encryption contract

Status: proposed teaching design, not implemented. This adds the user's 14 October requirement to the recovered CoverStory model. It does not replace Application, ApplicationRevision, PaymentAttempt, immutable money journals, adjustment allocations, submission records, Policy or Support. All data and provider behavior remain synthetic/mock until separately authorized integrations are verified.

## Ownership and source of truth

- Anonymous discovery, comparison and device-local save/export remain available. Public click/page-load tracking uses an optional pseudonymous browser-session identifier only under the configured purpose/consent policy. Pseudonymous does not mean anonymous. No sensitive draft is stored for anonymous resume in the MVP.
- After login, explicitly validate selected catalog/scenario versions and create an owned journey. Importing device-local selections is not accepting a quote, transferring an existing private journey, merging identities or treating prior browser events as trusted history.
- Journey UUIDs identify records. Every read, write and link checks the authenticated owner or separately authorized assigned-support scope; knowing the UUID grants no permission.
- Application and its exact revisions own business answers, accepted terms and lifecycle truth. Journey state is a server-maintained navigation projection referencing these records, not another editable copy of application answers or financial state.

## Proposed relational additions

1. journey
   - id UUID primary key; owner_user_id UUID not null; category; current_step; journey_status; version bigint not null; shortlist_id nullable; application_id nullable; application_revision bigint nullable; quote_id nullable; created_at; updated_at; expires_at; retention_policy_version.
   - Foreign keys reference the actual owned domain records. Preserve the recovered ApplicationRevision composite key: (application_id, application_revision) references (application_id, revision); do not invent or rekey an existing revision UUID. Owner consistency is verified through module interfaces and database constraints where expressible.
   - current_step and journey_status are bounded enums/projections. Resumability and allowed actions are recomputed against the current application, quote validity and source state; a stale projection cannot authorize a skipped step or payment.
   - Index owner_user_id, journey_status, updated_at DESC, id DESC for bounded homepage retrieval.

2. journey_step_event
   - id UUID; journey_id; sequence bigint; event_code; from_step; to_step; actor_ref; domain_record_type; domain_record_id; domain_revision; causation_id; request_id; occurred_at; recorded_at.
   - Unique(journey_id, sequence) and unique(journey_id, causation_id, event_code). Only server-validated commands generate authoritative events. No arbitrary browser state-change endpoint.
   - A domain transition, required transaction-audit event, journey projection update and corresponding step event commit in one PostgreSQL unit of work using public module interfaces. On rollback none of these changes commits. This does not put an external provider call inside the SQL transaction.
   - A request retry reuses the durable domain operation reference. Changed payload/revision under the same idempotency key conflicts. Stale expected versions reject; no browser timestamp determines authoritative order.

3. transaction_audit_event
   - id UUID; aggregate_type; aggregate_id; aggregate_sequence; journey_id nullable; operation_reference; event_code; prior_state; new_state; amount_minor nullable; currency nullable; reason_code; mock_provider_reference nullable; request_id; occurred_at; recorded_at; protected_snapshot_ref nullable; retention_policy_version.
   - References existing PaymentAttempt, journal effect, refund allocation, submission or renewal records instead of inventing a second transaction ledger. Unique(aggregate_type, aggregate_id, aggregate_sequence), with operation/effect uniqueness enforced by each owning module.
   - Preserve initiated, pending, failed, unknown, reconciled and succeeded observations, including their references. Persist a pending attempt/operation reference before the external mock call; commit a definitive provider failure as a FAILED state plus its audit event. If the result-recording SQL transaction rolls back, retain the prior pending/unknown reference for reconciliation; do not pretend an audit row survived the rollback or fabricate a definite provider outcome. Append corrections, reversals and recovery observations; never overwrite an earlier outcome as if it had not occurred.
   - Amounts, if present, are server-derived exact integer minor units plus currency. Audit observations do not themselves prove a posted money effect: link the actual journal/effect record. No raw provider payload, credentials, full consumer form or free-text exception is copied here.

4. analytics_event
   - event_id UUID; schema_version=1; pseudonymous_session_id; validated_journey_id nullable; event_name; page_id; step_code nullable; target_id nullable; optional_duration_ms nullable; client_time nullable; server_received_at; policy_version; expires_at.
   - Unique(event_id). A repeated exact validated payload produces the same receipt; reuse with different allowlisted payload conflicts. Idempotency is for telemetry ingestion only and never authorizes a business action.
   - Event names initially PAGE_VIEW and CONTROL_CLICK. page_id, step_code and target_id come from an instructor-supplied versioned allowlist. No arbitrary properties map, form values, names, contact details, health answers, document text, amounts, card data, credentials, raw URL/query/referrer or DOM text.
   - Client time and event order are untrusted observations. Store server receive time separately. Late/out-of-order telemetry does not regress a journey. Link a private journey only after ownership validation; missing/foreign UUID cannot create a link.
   - Browser analytics is best-effort and may be dropped, deduplicated or retried within a bounded queue. Transaction and journey audit are durable server records. Checkout, draft save and homepage resume continue when telemetry is blocked or unavailable.

5. protected_payload_envelope
   - id UUID; owner_user_id; object_type; object_id; object_revision; payload_kind; encryption_schema_version; algorithm; nonce; ciphertext_with_tag; wrapped_dek; kek_reference; envelope_revision; created_at; retirement_state.
   - ApplicationRevision and other domain snapshots reference the encrypted payload. Do not add a separate mutable plaintext draft to journey. Store only necessary synthetic consumer fields in the payload.
   - Use a maintained authenticated-encryption adapter, proposed AES-256-GCM with 12-byte IV and 128-bit tag. Instructor supplies the cryptographic provider port and fixed test fixtures; the learner completes bounded validation/access/rotation seams, not a homemade cipher.
   - IV uniqueness must hold for every encryption under a given data key; fixed IVs are test-vector-only. Reject wrong versions, altered ciphertext/tag or AAD, missing/unavailable key and malformed envelopes without partial plaintext or fallback.
   - Local AEAD associated data binds the immutable opaque object ID plus object revision, payload kind and encryption schema version. Authorization checks the actual owner before any decrypt call. AAD is not secret and is never placed in logs or analytics.
   - Deployment KMS encryption context, if later authorized, contains only non-sensitive constant service/payload-class/version labels. Do not put consumer fields, owner IDs, journey IDs, quote details or secrets in that context because it may be logged.
   - Data keys and key-encryption keys are separate. Store only wrapped data keys with ciphertext; protect the wrapping key outside the database, source repository and browser. Local teaching providers use ephemeral test keys, never real credentials. No KMS account, grant, key creation or persistent access is authorized by this plan. Ephemeral in-memory teaching keys last only for their declared test process. Cross-process restart/restore proof is BLOCKED unless an approved retained teaching-key mechanism is actually prepared and verified; losing the test key must return KEY_UNAVAILABLE, never a false recovery pass.
   - Envelope revisions record key rewrapping or re-encryption separately from immutable business event identity. Rotation must not invent a new payment, change accepted business content or erase recovery references. Keep old-key recovery compatibility for retained backups until a reviewed retention decision permits retirement.

## Deployment protection and recovery boundaries

Field envelopes protect selected consumer payloads; they do not encrypt every index, foreign key, routing field or metadata value. Deployment design must separately cover authenticated TLS for browser/API and application/database connections, encrypted database volumes and backups, key access separation, authorized restore access and redaction of application/proxy/error logs. Keep these controls as explicit planned configuration and verification artifacts until actually provisioned and tested. Local loopback test transport is not evidence of deployment TLS or encrypted host storage. No infrastructure/security-setting mutation is authorized by this curriculum draft.

Before accepting encrypted restart/restore evidence, record the actual teaching-key availability/lifetime and authorized recovery mechanism. In-process round-trip, restarted database, restarted application and restored backup are distinct modes. An in-memory test key cannot prove recovery after its process has ended. Retain the ciphertext and fail KEY_UNAVAILABLE rather than silently reset consumer data. Test module boundaries so a different module cannot bypass the authorized encryption/data-access port or reach its private repository.

## Proposed bounded enum registry v1

These are teaching choices, not recovered API enum facts. The implementation must keep one versioned registry and reject unknown values rather than silently map them.

- journey_status: ACTIVE, WAITING, COMPLETED, CANCELLED, EXPIRED.
- current_step/step_code: DISCOVER, COMPARE, QUOTE_REVIEW, PAYMENT_PENDING, ONBOARDING, FINAL_REVIEW, SUBMISSION_PENDING, INSURER_REVIEW, OFFER_REVIEW, ISSUANCE_PENDING, POLICY_HELP, CANCELLATION_REVIEW, RENEWAL_REVIEW, HISTORY. Actual progression remains subject to the linked domain's prerequisites; membership in this enum grants no permission. Completion is not inferred from a click. A fresh renewal receives its own linked application/journey and preserves the old policy journey.
- Initial page_id allowlist: health_discovery, compare, quote_review, home, checkout, onboarding, final_review, policy_detail, help, cancellation, renewal. Later term/motor/ULIP pages extend the same versioned allowlist in their own task.
- Initial target_id allowlist for CONTROL_CLICK: compare_plan, remove_plan, save_local, export_summary, start_quote, confirm_mock_payment, save_draft, submit_proposal, open_policy, open_help, request_cancel, choose_refund, open_renewal. PAGE_VIEW requires target_id absent. No customer-entered text is a target ID.
- optional_duration_ms, when present, is an integer from 0 through 300000; booleans are invalid. client_time is absent or a valid aware timestamp and remains untrusted. Do not infer authoritative ordering or dwell-time certainty from it.

## Proposed APIs

All routes are a reviewed /api/v1 teaching convention, not existing endpoints or aliases.

- GET /api/v1/me/journeys?status=active&limit=20&cursor=...: authenticated owner only; limit 1–50, opaque owner-bound cursor; returns minimal journey summaries and allowed resume actions, not decrypted consumer fields. Fresh session or reload uses this authoritative projection.
- GET /api/v1/journeys/{id}: authenticated owner or explicitly assigned support scope; unknown and foreign IDs have the same safe 404 response. Navigation is validated against current domain state.
- Existing application/quote/payment commands carry their domain expected version, reviewed revision and idempotency requirements. If a command also advances journey state, it checks the supplied journey version and atomically records the projection/event. No generic client command may set paid/submitted/issued.
- POST /api/v1/analytics/events: one event or a bounded batch of at most 10; total JSON UTF-8 body at most 8 KiB. Validate schema, name/target allowlists, length/time bounds, rate limit and current purpose/consent policy. Unauthorized journey links reject safely. Unknown fields are rejected rather than silently retaining private values.
- Telemetry response 202 contains per-event RECEIVED, DUPLICATE or DISCARDED_BY_POLICY receipts, with no business-state implication. Same event ID with a different validated payload returns 409 EVENT_ID_CONFLICT. Invalid schema 400; oversized payload 413; rate limit 429. These are proposed teaching statuses. Browser retry is capped at 3 attempts and a queue of 20 events; dropping after the cap is permitted and observable locally. No retry blocks checkout.
- If policy disallows collection, the client emits nothing and the server discards defensive arrivals without persisting their payload. No new third-party analytics destination is introduced.

## Exact cross-cutting acceptance fixtures

Use the shared synthetic customer A/B identities and fixed UUID aliases, never real consumer data.

1. A owns journey jA linked to application A revision 2 and exact quote revision 2. A reload/new session returns jA at its server-valid step. B with the same UUID gets safe 404; decrypt/provider call count 0. A stale browser claiming ISSUED cannot advance the projection.
2. Journey version 4 receives two different commands with expectedVersion 4. One valid command commits version 5 plus one authoritative step event; the other conflicts. Inject failure after each write boundary: domain state, audit and journey/event all remain at their before-state after rollback.
3. PAGE_VIEW event e1 followed by the exact same e1 yields one stored telemetry row and a duplicate receipt. Same e1 with a different valid page_id conflicts. PAGE_VIEW with any target_id is separately schema-invalid (400), not an event-ID conflict. A later client_time can arrive before an earlier one; authoritative journey version stays unchanged.
4. A CONTROL_CLICK with amountMinor, email, healthAnswer, rawUrl, token or arbitrary properties is rejected with zero secret-marker bytes in database, logs and errors. A foreign journey link is rejected before lookup serialization. Blocking analytics still permits an already-authorized mock checkout and owned resume.
5. Simulated failed payment retains its PaymentAttempt and FAILED audit observation. Unknown result preserves the provider reference and requires reconciliation. Duplicate successful callback adds no second journal effect or journey advance. Audit may retain the duplicate observation with a distinct safe diagnostic reference; posted amount is counted once.
6. Authorized encryption/decryption returns the exact synthetic payload. Wrong owner is denied before decrypt. Modified tag, ciphertext, object binding or unavailable key fails closed; plaintext fallback 0. Two encryptions under the same key never reuse an IV outside fixed test-vector mode.
7. Rotate the wrapping-key reference or data-key envelope while preserving object/business IDs and plaintext semantics. A retained old backup remains recoverable through its authorized key version; missing key is KEY_UNAVAILABLE, not empty data or successful restore. No key or plaintext marker appears in serialized receipts.
8. Use distinct configurable fixture retention windows (for example analytics 7 days and abandoned draft 30 days, explicitly teaching values rather than legal policy). Expire/purge only eligible records; a retained audit reference or reviewed hold remains intact. Do not assume indefinite storage or that deleting a key automatically satisfies every retention obligation. Real jurisdiction/insurer/provider requirements remain a launch gate.

## Sources and limits

Official technical references reviewed 14 October 2026: OWASP Cryptographic Storage (authenticated modes, minimization, key separation and rotation), OWASP Logging (excluded data), OWASP IDOR (per-object authorization), PostgreSQL transactions/constraints, Java 21 GCMParameterSpec, and AWS KMS cryptography/encryption-context guidance. The new schema/API/retention numbers are proposed learning choices, not facts recovered from ECOS or a claim of legal compliance.

- https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html
- https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html
- https://cheatsheetseries.owasp.org/cheatsheets/Insecure_Direct_Object_Reference_Prevention_Cheat_Sheet.html
- https://www.postgresql.org/docs/18/tutorial-transactions.html
- https://www.postgresql.org/docs/current/ddl-constraints.html
- https://docs.oracle.com/en/java/javase/21/docs/api/java.base/javax/crypto/spec/GCMParameterSpec.html
- https://docs.aws.amazon.com/kms/latest/developerguide/kms-cryptography.html
- https://docs.aws.amazon.com/kms/latest/developerguide/encrypt_context.html
