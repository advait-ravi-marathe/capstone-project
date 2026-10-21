# Day 85 — 2027-01-12: Trace PostgreSQL timeout boundaries and prerequisite evidence

## Contract

**Engineering scope:** Day85 baseline/readiness review
**Status:** Instructor fixture/selector installed; learner implementation remains unattempted.
**Daily capacity:** 180 minutes: 60 minutes Java DSA separately, plus 120 minutes engineering (85 implementation/testing, 25 concept/course, 10 evidence). No extra mandatory setup work.

### Expected behavior

No new production behavior. Reconfirm the unchanged 4-connection Hikari pool, 2-permit outer gate and 1,000 ms acquisition timeout. Record which earlier learner seams prevent packaged runtime startup; distinguish incomplete implementation from infrastructure failure.

### Acceptance and expected TODO state

The selector is installed and runnable with learner TODOs restored. It may fail at named incomplete learner/prerequisite seams; missing classes, selectors, fixtures, tools or browser setup are infrastructure defects. The default Maven suite is the instructor infrastructure suite and excludes `learnerAcceptance` tests.

**Learner acceptance:** None (readiness/evidence review only).
**Infrastructure selector:** `src/test/java/assignments/renewal/Day85To98InfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=Day85To98InfrastructureTest test`

### Scope and safety limits

Keep Hikari maximum pool size 4, outer admission maximum 2, and acquisition timeout 1,000 ms. Make no HTTP deadline claim. Use one top-level REQUIRED read-only transaction; reject ambient transactions before mutation or delegate invocation. The transaction manager and JdbcTemplate use raw Hikari. Only the unchanged Day48 JdbcPolicyRenewalReadStore gets TransactionAwareDataSourceProxy. Place timed store in Components.renewalReadStore; keep the bounded decorator outermost.

Apply only transaction-local, parameterized PostgreSQL `set_config(..., true)` for lock_timeout and statement_timeout. Do not add automatic retries, production cancellation registry, HTTP-to-JDBC bridge, fault route, extra transaction/JDBC/socket timeout, thread hop, REQUIRES_NEW, manual commit, physical-connection close, dependency or infrastructure. Unknown SQL, mapping, rollback and cleanup failures must remain failures.

### Concept/course block — 25 minutes

No exact matching course lesson was verified for this PostgreSQL/Hikari/JDBC behavior. Use the concrete worked example and focused official sources below; no unrelated video is padded in.

**Worked example (within the same cap):** Draw admission → Hikari checkout → transaction → PostgreSQL lock/query wait → rollback/cleanup. Label which timeout governs each wait. Hikari checkout timeout does not bound SQL after checkout.

**Focused sources:**
- [PostgreSQL 18 Client Connection Defaults — Statement Behavior: statement_timeout and lock_timeout](https://www.postgresql.org/docs/18/runtime-config-client.html)
- [Spring DataSourceTransactionManager API — Thread-bound connection; Spring transaction timeout mechanism is context only and not configured here](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/datasource/DataSourceTransactionManager.html)

Video viewing: 0:00; allow at most 20:00 for the worked example and focused reading, then 5:00 for teach-back. Total is 25 minutes including pauses. Carry unfinished study forward and reduce implementation scope; no added hours. Viewing is not learner completion.


**Sketch prompt:** Draw the lock holder, transaction-bound backend, local setting lifetime, cancellation/rollback owner and when the outer permit can be released.

**Teach-back:** Explain what proves same-backend use, how settings reset, and which layer owns cleanup.

### Separate Java DSA block — 60 minutes

Run the actual-attempt review queue first. D+3, D+7 and D+21 clocks start only from an actual independent first attempt; planned dates never start review clocks. Failed/overdue reviews replace new work. The shared attempt log is empty, so no due review is forecast. LC236 is deferred because prerequisite retrieval has no recorded evidence.

- 25 minutes pattern recall or new learning only if due queue permits
- 20 minutes independent solving or due cold review
- 15 minutes active retrieval and error log

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2027-01-12`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py scripts/test_dsa_review_queue_71_84.py`
**Shared actual-attempt log:** `docs/reboot/continuation-31-44/dsa/actual-attempt-log.json`

### Evidence block — 10 minutes

Record exact commands, exit status, observed values, cleanup result, learner-owned paths and blockers in `evidence/continuation-85-98/day-85.json`. Keep actual attempt dates blank until evidenced and separate temporary-witness validation from learner completion.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date 2027-01-12 --roadmap-day 85` first. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../../dsa/guided-transfer-policy.md](../../dsa/guided-transfer-policy.md) and [the DSA error log](../../dsa-error-log-template.md).
