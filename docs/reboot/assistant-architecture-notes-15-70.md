# Assistant-provided architecture notes: Days 15–70

These decisions belong to the capstone implementation. The assistant supplies
the concise what/why before implementation; the learner implements and tests
the existing contract. Use each note inside that day's 25-minute
concept/course allocation. No capstone design proposal, design choice, or
post-hoc architecture justification is assigned.

System-design interview practice remains separate and uses only the assigned
question-bank prompts and dedicated mock sessions. Learner proposals belong
inside those question sessions only.

**Sprint 1 boundary carried forward:** Day 9's `QuoteRepositoryAdapter.find`
scopes its lookup by `(journeyId, quoteVersionId)`. The adapter has no owner
argument or owner column; authenticated owner resolution is a separate
boundary introduced by the Day 12 principal guard. Later owner-scoped reads
use the seams specified by their own contracts. Do not retrofit Day 9's
composite lookup with an owner parameter.

## Sprint 2: Days 15–30

| Day | Assistant-provided decision and reason |
|---:|---|
| <a id="day-15"></a> 15 | Project the journey summary with an explicit parent/optional-child join and allowlisted fields; this preserves absent-versus-empty semantics and prevents private columns from leaking. |
| <a id="day-16"></a> 16 | Page with the contract's stable composite order and a strict row limit; ties then remain deterministic across requests. |
| <a id="day-17"></a> 17 | Keep the product-choice component typed and presentational over supplied props; this isolates accessible rendering from transport and state ownership. |
| <a id="day-18"></a> 18 | Use controlled form state with validation at the existing client boundary; the visible input and submitted request then share one source of truth. |
| <a id="day-19"></a> 19 | Render the returned quote snapshot as supplied; do not recalculate premium or mutate it from current form values. |
| <a id="day-20"></a> 20 | Submit through the existing async action seam and represent pending/result states explicitly; a UI event must not imply a successful write before the response. |
| <a id="day-21"></a> 21 | Keep same-origin session cookies and the CSRF token header as separate controls on writes; CSRF validity does not establish identity. |
| <a id="day-22"></a> 22 | Attach a bounded request-correlation identifier at the server boundary and carry it through logs; avoid logging request bodies or personal data. |
| <a id="day-23"></a> 23 | Expose only the minimum health signal needed by the local runtime; do not disclose configuration or application data through health output. |
| <a id="day-24"></a> 24 | Add an index only after measuring the exact query plan and preserve its specified result order; this ties optimization to observed workload and keeps semantics fixed. |
| <a id="day-25"></a> 25 | Enforce repeated-selection uniqueness at the database transaction boundary; concurrent requests must converge on one durable selection. |
| <a id="day-26"></a> 26 | Append the smallest audit record in the same transaction as the selected mutation; avoid storing secrets or duplicating full request payloads. |
| <a id="day-27"></a> 27 | Fix the smallest evidenced regression and add one focused failing-before/passing-after test; avoid unrelated refactoring. |
| <a id="day-28"></a> 28 | Verify the existing user path with real Chromium and disposable PostgreSQL through the in-process Spring test route serving the built frontend; this is browser-to-database evidence, not executable-JAR packaging evidence. |
| <a id="day-29"></a> 29 | Keep reset operations limited to a uniquely identified embedded/disposable database with an explicit refusal guard; never accept an external URI for destructive reset. |
| <a id="day-30"></a> 30 | Treat the evidence checklist as a status ledger with linked observations and open blockers; a review conversation cannot promote missing evidence to pass. |

## Sprint 3: Days 31–44

| Day | Assistant-provided decision and reason |
|---:|---|
| <a id="day-31"></a> 31 | Validate issue eligibility in the domain service from the trusted owner, selected quote, and current lifecycle facts; transport data alone must not authorize issuance. |
| <a id="day-32"></a> 32 | Key the idempotency receipt by trusted owner and client key, and compare the existing request intent on replay; same intent returns the original result while changed intent conflicts. |
| <a id="day-33"></a> 33 | Persist an immutable issue snapshot of the accepted quote and effective date; later catalog changes must not rewrite issued policy facts. |
| <a id="day-34"></a> 34 | Keep the controller as a translation boundary from domain outcomes to stable HTTP status/body; it must not duplicate eligibility or transaction logic. |
| <a id="day-35"></a> 35 | Persist policy, lifecycle event, and idempotency receipt on one PostgreSQL transaction; rollback prevents a partial issuance record set. |
| <a id="day-36"></a> 36 | Scope the policy read by the authenticated principal's owner identifier inside the repository query; request-supplied owner values are untrusted. |
| <a id="day-37"></a> 37 | Validate endorsement intent before any write, using the explicit command fields and current lifecycle state; invalid commands must have no side effects. |
| <a id="day-38"></a> 38 | Append an immutable endorsement version and preserve prior snapshots; history remains auditable instead of being overwritten. |
| <a id="day-39"></a> 39 | Use a compare-and-set version check for concurrent updates; only one writer may advance a given revision. |
| <a id="day-40"></a> 40 | Represent cancellation as an explicit scheduled state and effective date; retain the issued snapshot until the defined transition occurs. |
| <a id="day-41"></a> 41 | Apply due cancellation using the injected clock and an idempotent state transition; repeated processing must not duplicate history. |
| <a id="day-42"></a> 42 | Gate servicing writes on lifecycle state in the domain service and preserve reads; the HTTP adapter maps the rejected decision but does not invent it. |
| <a id="day-43"></a> 43 | Render the servicing summary from typed supplied data; keep display state separate from backend lifecycle authority. |
| <a id="day-44"></a> 44 | Use the existing integration selector as the readiness boundary and report its first real stop; don't replace missing prior behavior with test-only success logic. |

## Sprint 4: Days 45–56

| Day | Assistant-provided decision and reason |
|---:|---|
| <a id="day-45"></a> 45 | Model policy term start as inclusive and the next anniversary as exclusive using `LocalDate`; explicit boundaries avoid leap-year and time-zone drift. |
| <a id="day-46"></a> 46 | Check lifecycle eligibility and renewal-window dates in the contract's declared precedence; a cancelled state cannot become renewable just because its date is in range. |
| <a id="day-47"></a> 47 | Freeze the original policy and renewal reference into an immutable preview snapshot; later source changes must not rewrite what the preview described. |
| <a id="day-48"></a> 48 | Read the preview through a parameterized owner-scoped JDBC projection with explicit nullable mapping; the query returns only approved fields. |
| <a id="day-49"></a> 49 | Derive owner identity from the authenticated principal and pass it to the read store; query/header values cannot select another owner's record. |
| <a id="day-50"></a> 50 | Translate missing and ineligible preview outcomes into the contract's stable non-disclosing HTTP responses; do not reveal whether a foreign record exists. |
| <a id="day-51"></a> 51 | Return an allowlisted, explicitly non-binding preview DTO; avoid policy mutation, payment fields, or language that implies coverage renewal. |
| <a id="day-52"></a> 52 | Keep the reference panel presentational over typed props; supplied data is rendered without performing issuance or changing the policy. |
| <a id="day-53"></a> 53 | Represent loading, unavailable, empty, and ready as accessible explicit states; users must not mistake absent data for a valid preview. |
| <a id="day-54"></a> 54 | Compare the actual preview response to complete seeded PostgreSQL state before and after the read; a read path must not write. |
| <a id="day-55"></a> 55 | Fetch the preview with same-origin session credentials and display the validated response; keep identity server-derived and do not retry writes automatically. |
| <a id="day-56"></a> 56 | Compare read-only preview across two Spring server contexts in the same test JVM, each read by a fresh browser process against the same seeded disposable PostgreSQL and fixed clock; this proves context-restart read stability, not logout invalidation or a separate-process active-cookie restart. |

## Sprint 5: Days 57–70

| Day | Assistant-provided decision and reason |
|---:|---|
| <a id="day-57"></a> 57 | Require each named prior learner evidence flag and fail closed for missing, null, or false values; unrelated evidence cannot substitute for a required gate. |
| <a id="day-58"></a> 58 | Compose the bounded local runtime with explicit imports and the existing two-argument clock/data-source composition; this avoids scanning future runtime TODOs. |
| <a id="day-59"></a> 59 | Run immutable migrations V001–V003 to target 3 on a fresh disposable database and verify a repeat is a no-op; preserve the sentinel and history. |
| <a id="day-60"></a> 60 | Read policy by policy ID plus principal owner with bound JDBC parameters and nullable mapping; the read adapter must leave all policy/event/receipt rows unchanged. |
| <a id="day-61"></a> 61 | Reuse the supplied security filter chain and session repository; distinguish authorization, handler-level session selection, and CSRF checks. |
| <a id="day-62"></a> 62 | Derive ownership only from a known authenticated synthetic principal and reject anonymous/invalid states; request parameters never override it. |
| <a id="day-63"></a> 63 | Keep the recovery gate within the remaining 0–85 practice-minute range and do not add work; invalid time input is rejected. |
| <a id="day-64"></a> 64 | Let the issue adapter translate the lifecycle decision and persist only the contracted create result; replay, conflict, and anonymous paths do not write. |
| <a id="day-65"></a> 65 | Preserve CSRF on every unsafe request and test it independently from authentication; rejected requests must not reach handlers or mutate rows. |
| <a id="day-66"></a> 66 | Decode HTTP status before parsing a body, validate successful JSON as `unknown`, and model UI states as a discriminated union; errors never become success-shaped data. |
| <a id="day-67"></a> 67 | Build a fresh dedicated executable archive and launch only that artifact against uniquely disposable loopback resources; source tests and stale JARs do not prove packaging. |
| <a id="day-68"></a> 68 | Use independent expected API/DOM/database projections and a snapshot barrier after every browser action; whole-flow counts can hide a temporary illegal write. |
| <a id="day-69"></a> 69 | Clear private preview state immediately on owner change/logout and invalidate late responses; test restart only with the exact saved cookie proven active before shutdown. |
| <a id="day-70"></a> 70 | Use actual pass/fail/blocked/not-run evidence to select recovery and handover content; no new feature or fictional customer result is introduced. |
