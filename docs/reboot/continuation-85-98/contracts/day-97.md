# Day 97 — 2027-01-24: Prove packaged authenticated timeout recovery and one explicit retry

## Contract

**Engineering scope:** Actual JAR + authenticated UI + real relation lock
**Status:** Instructor fixture/selector installed; learner implementation remains unattempted.
**Daily capacity:** 180 minutes: 60 minutes Java DSA separately, plus 120 minutes engineering (85 implementation/testing, 25 concept/course, 10 evidence). No extra mandatory setup work.

### Expected behavior

On the existing renewal route, show a safe bounded-read response under real ACCESS EXCLUSIVE lock; release holder; require one explicit user retry to succeed. Preserve ownership, bounded metrics and byte-for-byte no-write snapshots. Start the packaged test app with fixed test clock `2027-01-01T12:00:00Z`, placing the seeded `2026-01-15` policy inside its deterministic renewal window; do not depend on the machine's current date. Earlier Day48–81 learner seams remain prerequisites.

### Acceptance and expected TODO state

The selector is installed and runnable with learner TODOs restored. It may fail at named incomplete learner/prerequisite seams; missing classes, selectors, fixtures, tools or browser setup are infrastructure defects. The default Maven suite is the instructor infrastructure suite and excludes `learnerAcceptance` tests.

**Acceptance selector:** `src/test/java/assignments/local/Day97PackagedTimeoutBrowserAcceptanceTest.java`
**Acceptance command:** `npm --prefix frontend ci`; `npm --prefix frontend run build:local`; `./mvnw -Plocal-app -DskipTests package`; then `CHROMIUM_BIN=/usr/bin/chromium ./mvnw -Plearner-tests -Plocal-app -Dtest=Day97PackagedTimeoutBrowserAcceptanceTest test` (or install the project-pinned Playwright Chromium in CI).
**Infrastructure selector:** `src/test/java/assignments/renewal/Day85To98InfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=Day85To98InfrastructureTest test`

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


**Sketch prompt:** Draw the lock holder, transaction-bound backend, local setting lifetime, cancellation/rollback owner and when the outer permit can be released.

**Teach-back:** Explain what proves same-backend use, how settings reset, and which layer owns cleanup.

### Separate Java DSA block — 60 minutes

Run the actual-attempt review queue first. D+3, D+7 and D+21 clocks start only from an actual independent first attempt; planned dates never start review clocks. Failed/overdue reviews replace new work. The shared attempt log is empty, so no due review is forecast. LC236 is deferred because prerequisite retrieval has no recorded evidence.

- 25 minutes pattern recall or new learning only if due queue permits
- 20 minutes independent solving or due cold review
- 15 minutes active retrieval and error log

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2027-01-24`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py scripts/test_dsa_review_queue_71_84.py`
**Shared actual-attempt log:** `docs/reboot/continuation-31-44/dsa/actual-attempt-log.json`

### Evidence block — 10 minutes

Record exact commands, exit status, observed values, cleanup result, learner-owned paths and blockers in `evidence/continuation-85-98/day-97.json`. Keep actual attempt dates blank until evidenced and separate temporary-witness validation from learner completion.
