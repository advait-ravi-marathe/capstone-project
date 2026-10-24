# Day 56 — 2026-12-18: Prove preview stability across restart without writes

## Contract

**Learner-owned seam:** `read-only preview integration`
**Status:** Contract defined; learner implementation not attempted.

### Expected behavior

Capture complete ordered database rows before the first GET, immediately after that GET while the first server context is alive, after closing it, after starting a second Spring server context in the same test JVM but before its GET, immediately after the second GET while the context is alive, and after close. Both statuses are200, semantic responses match exactly, and UI shows all fields on each run. A fresh browser process performs each read; the same PostgreSQL instance remains open and is not reseeded. This does not prove packaged-JAR execution or PostgreSQL crash durability.

### Readiness before the task

- Actual Day55 gate and complete DB snapshot helper
- Existing fixed-clock/same-database restart harness

### Acceptance and expected TODO state

The fixed selectors remain learner-owned. Before the named TODO is completed, the semantic test is expected to stop at that exact seam. Instructor references and transport smoke are separate evidence, not learner completion. Do not implement behavior in starter code or weaken assertions. Record the actual command, result and first blocker in `evidence/continuation-45-56/renewal-preview-day-56.observation.json` only after a real learner attempt.

**Acceptance commands:**
- `./mvnw -Plearner-tests -Dtest=RenewalPreviewTransportInfrastructureTest#day56PreviewReadIsStableAcrossRestartAndDoesNotWrite test`

**Instructor infrastructure/reference commands:**
- `npm --prefix frontend run build && CHROMIUM_BIN=/usr/bin/chromium ./mvnw -Dtest=RenewalPreviewTransportInfrastructureTest#testOnlyReadOnlyBrowserTransportSurvivesSpringRestart test`

## Interview-first system-design mock (45 minutes; within the 85-minute practice block)

**Prompt:** [Q12 — Scheduler: missed or duplicate execution](../../interview/question-bank.md#q12). Save your initial proposal during the 15-minute architecture phase before hints, reference answers, or AI. Use the [100-point rubric](../../interview/mock-rubric.md) with 5/5/15/10/10-minute phases. This is a design-transfer exercise; it adds no product implementation.

**Existing engineering work (40 minutes):** 5 minutes verify actual Day55 gate and same-database/fixed-clock preconditions; 25 minutes run the named learner restart selector and inspect the first checkpoint; 10 minutes record exact statuses, response and row observations or the first blocker. This is bounded observation/debug work, not a claim that the complete restart gate passed.

**Runnable acceptance:** Use the exact learner acceptance command(s) already named above. For Day 56, those remain `./mvnw -Plearner-tests -Dtest=RenewalPreviewTransportInfrastructureTest#day56PreviewReadIsStableAcrossRestartAndDoesNotWrite test`. The command may stop at a prerequisite TODO; record that exact result without substituting an instructor witness.

**Deferred queue:** **S4-D56-1** — Keep every original live-context and post-close database snapshot checkpoint, both 200 responses, complete semantic UI/response equality and the same PostgreSQL instance, seeded once before the first context and not reseeded between contexts. If the 40-minute slice cannot finish all observations, carry the remaining named checkpoints here; do not infer packaged-JAR execution or crash durability.

## Practice and evidence

**Practice allocation (85 minutes):** 45 minutes for the named mock, then 40 minutes for the existing engineering task: 5 minutes to confirm its preconditions and predict the first result, 25 minutes on the bounded learner seam or observation, and 10 minutes for the named selector and actual blocker/result record. This replaces the prior full 85-minute implementation breakdown on this mock day only. Original acceptance remains required; carry any unfinished portion in the named queue without overtime.

**10-minute evidence allocation:** record the saved pre-assistance proposal; actual acceptance command, result or first blocker; changed paths and assistance; and which original acceptance remains open. Do not claim completion from a mock, smoke test, or partial slice.

## Study block — exactly 25 minutes

- No course video is required for this day; use the supplied focused official references and project-authored worked example.
- Worked guide: [Read after restart without changing stored rows](../worked/day-56-restart-read-handover.md). The exact 25-minute segments are in `fde45-56-v2-curriculum.json`; all study items share that cap.

**Focused official sources:**
- [RFC 9110: HTTP Semantics](https://www.rfc-editor.org/rfc/rfc9110.html#section-9.2.1) — 9.2.1 Safe Methods; 9.3.1 GET.
- [PostgreSQL 18: SET TRANSACTION](https://www.postgresql.org/docs/18/sql-set-transaction.html) — Description: transaction access mode READ ONLY.
- [AWS Well-Architected: Use runbooks to perform procedures](https://docs.aws.amazon.com/wellarchitected/latest/operational-excellence-pillar/ops_ready_to_support_use_runbooks.html) — Implementation guidance; Implementation steps.

Optional references are deferred and replacement-only; no purchase/enrollment or extra minutes are required.

## Safety and scope

This is a synthetic, read-only reference preview. It does not renew, accept, bind, price a new offer, collect payment or promise coverage. The browser harness uses a test-injected principal; that fixture is not production authentication. Real PostgreSQL/browser checks are restricted to disposable local test infrastructure.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14). This engineering contract contains no DSA assignment.
