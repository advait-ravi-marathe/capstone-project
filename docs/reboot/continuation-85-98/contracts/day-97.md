# Day 97 — 2027-01-28: Prove packaged authenticated timeout recovery and one explicit retry

## Contract

**Engineering scope:** Actual JAR + authenticated UI + real relation lock
**Status:** Instructor fixture/selector installed; learner implementation remains unattempted.

### Expected behavior

On the existing renewal route, show a safe bounded-read response under real ACCESS EXCLUSIVE lock; release holder; require one explicit user retry to succeed. Preserve ownership, bounded metrics and byte-for-byte no-write snapshots. Start the packaged test app with fixed test clock `2027-01-01T12:00:00Z`, placing the seeded `2026-01-15` policy inside its deterministic renewal window; do not depend on the machine's current date. Earlier Day48–81 learner seams remain prerequisites.

### Acceptance and expected TODO state

The selector is installed and runnable with learner TODOs restored. It may fail at named incomplete learner/prerequisite seams; missing classes, selectors, fixtures, tools or browser setup are infrastructure defects. The default Maven suite is the instructor infrastructure suite and excludes `learnerAcceptance` tests.

**Acceptance selector:** `src/test/java/assignments/local/Day97PackagedTimeoutBrowserAcceptanceTest.java`
**Acceptance commands:** First run `./scripts/package-local-demo.sh`, which clean-builds the frontend-bearing local executable archive; then run `CHROMIUM_BIN=/usr/bin/chromium ./mvnw -Plearner-tests,local-app -Dtest=Day97PackagedTimeoutBrowserAcceptanceTest test` (or use the project-pinned Playwright Chromium in CI). This is an actual packaged-JAR/browser journey, not the separate source-shape infrastructure check.
**Infrastructure selector:** `src/test/java/assignments/renewal/Day85To98InfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=Day85To98InfrastructureTest test`

### Separate system-design interview practice — 45 minutes of the 85-minute practice block

This is a separate interview exercise, not a capstone design assignment. Attempt the interview question before using hints, references, or AI feedback. Capstone architecture is supplied above. These 45 minutes replace part of the existing 85-minute practice block; the remaining 40 minutes are the bounded implementation slice below. This replaces, and does not supplement, the full practice allocation.

- **Question:** [Q32 — Streaming: adaptive delivery](../../interview/question-bank.md#q32). Which signals should change bitrate, and how do buffering and regional failures affect playback?
- **Interview patterns:** [streaming-delivery](../../interview/pattern-library.md#streaming-delivery), [resilience](../../interview/pattern-library.md#resilience).
- **Mock phases (45 minutes total):** 5 minutes Requirements; 5 minutes Capacity and constraints; 15 minutes Initial architecture and data model; 10 minutes Bottlenecks, reliability, scaling; 10 minutes Trade-offs, failures, follow-ups.
- **Remaining engineering slice (40 minutes total):** 5 minutes preconditions and prediction; 25 minutes implementation or observation; 10 minutes selector and blocker record.
- **Bounded capstone task:** Run the fresh-JAR browser lock/retry journey to its first actual learner prerequisite. Record browser, HTTP, and database observations; implement only the first named blocked seam during the 40-minute engineering slice.
- **Deferred acceptance:** `S7-D97-MOCK` — Carry every unmet original acceptance assertion under this ID. Resume from the first real learner TODO or blocked prerequisite; do not add practice minutes or infer completion from the interview mock.
- **Runnable acceptance command(s):**
  - `./scripts/package-local-demo.sh`
  - `CHROMIUM_BIN=/usr/bin/chromium ./mvnw -Plearner-tests,local-app -Dtest=Day97PackagedTimeoutBrowserAcceptanceTest test`

### Scope and safety limits

Keep Hikari maximum pool size 4, outer admission maximum 2, and acquisition timeout 1,000 ms. Make no HTTP deadline claim. Use one top-level REQUIRED read-only transaction; reject ambient transactions before mutation or delegate invocation. The transaction manager and JdbcTemplate use raw Hikari. Only the unchanged Day48 JdbcPolicyRenewalReadStore gets TransactionAwareDataSourceProxy. Place timed store in Components.renewalReadStore; keep the bounded decorator outermost.

Apply only transaction-local, parameterized PostgreSQL `set_config(..., true)` for lock_timeout and statement_timeout. Do not add automatic retries, production cancellation registry, HTTP-to-JDBC bridge, fault route, extra transaction/JDBC/socket timeout, thread hop, REQUIRES_NEW, manual commit, physical-connection close, dependency or infrastructure. Unknown SQL, mapping, rollback and cleanup failures must remain failures.

### Concept/course block — 25 minutes

**Course (reference only; access unconfirmed):** [React - The Complete Guide (incl. Next.js, Redux) — Academind / Maximilian Schwarzmüller](https://www.udemy.com/course/react-the-complete-guide-incl-redux).
No exact matching course lesson was verified for this PostgreSQL/Hikari/JDBC behavior. Use the concrete worked example and focused official sources below; no unrelated video is padded in.
If the existing React state concepts need review, the selected course has prior reference titles **Rendering Content Conditionally** (section **React Essentials - Components, JSX, Props, State & More**) and **Handling Loading States** (section **Sending Http Requests (e.g. Connecting to a Database)**). This is optional retrieval, not new assigned viewing; it must replace part of this same 25-minute cap.

**Worked example (within the same cap):** With the fixed eligible test clock, select owner in the packaged browser, trigger real locked preview, inspect safe response while holder remains locked, release holder, then click one explicit Retry. Verify no writes and retained context.

**Focused sources:**
- [MDN ARIA status role — Polite status announcements](https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Roles/status_role)
- [MDN ARIA alert role — Urgent message announcements](https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Roles/alert_role)

Video viewing: 0:00; allow at most 20:00 for the worked example and focused reading, then 5:00 for teach-back. Total is 25 minutes including pauses. Carry unfinished study forward and reduce implementation scope; no added hours. Viewing is not learner completion.

**Assistant-provided architecture (what and why):** [Day 97 note](../assistant-architecture-notes.md#day-97). The note supplies capstone architecture decisions. Implement and test the named seam; no capstone design proposal or post-hoc justification is assigned.

**Teach-back:** Explain the supplied boundary in your own words and cite one observed acceptance result; no design-choice essay is required.

### Evidence block — 10 minutes

Record exact commands, exit status, observed values, cleanup result, learner-owned paths and blockers in `evidence/continuation-85-98/day-97.json`. Keep actual attempt dates blank until evidenced and separate temporary-witness validation from learner completion.

## Separate DSA track

The daily DSA assignment, tests, review schedule, and attempt history live only in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). This engineering contract contains no DSA assignment.
