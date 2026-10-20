# Day 90 — 2027-01-17: Prove read-only enforcement and mapping-failure rollback

## Contract

**Engineering scope:** TimedPolicyRenewalReadStore transaction outcome
**Status:** Instructor fixture/selector installed; learner implementation remains unattempted.
**Daily capacity:** 180 minutes: 60 minutes Java DSA separately, plus 120 minutes engineering (85 implementation/testing, 25 concept/course, 10 evidence). No extra mandatory setup work.

### Expected behavior

Use supplied read-only wiring. A write attempt is rejected; mapper/delegate failures roll back. Never turn SQL, mapping, rollback or cleanup failure into Optional.empty.

### Acceptance and expected TODO state

The selector is installed and runnable with learner TODOs restored. It may fail at named incomplete learner/prerequisite seams; missing classes, selectors, fixtures, tools or browser setup are infrastructure defects. The default Maven suite is the instructor infrastructure suite and excludes `learnerAcceptance` tests.

**Acceptance selector:** `src/test/java/assignments/renewal/TimedRenewalReadAcceptanceTest.java`
**Acceptance command:** `./mvnw -Plearner-tests -Dtest=TimedRenewalReadAcceptanceTest#readOnlyAndMappingFailureRollbackBeforeReturn test`
**Infrastructure selector:** `src/test/java/assignments/renewal/Day85To98InfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=Day85To98InfrastructureTest test`

### Scope and safety limits

Keep Hikari maximum pool size 4, outer admission maximum 2, and acquisition timeout 1,000 ms. Make no HTTP deadline claim. Use one top-level REQUIRED read-only transaction; reject ambient transactions before mutation or delegate invocation. The transaction manager and JdbcTemplate use raw Hikari. Only the unchanged Day48 JdbcPolicyRenewalReadStore gets TransactionAwareDataSourceProxy. Place timed store in Components.renewalReadStore; keep the bounded decorator outermost.

Apply only transaction-local, parameterized PostgreSQL `set_config(..., true)` for lock_timeout and statement_timeout. Do not add automatic retries, production cancellation registry, HTTP-to-JDBC bridge, fault route, extra transaction/JDBC/socket timeout, thread hop, REQUIRES_NEW, manual commit, physical-connection close, dependency or infrastructure. Unknown SQL, mapping, rollback and cleanup failures must remain failures.

### Concept/course block — 25 minutes

Read the focused primary-source section for 10 minutes, sketch the transaction/resource lifetime for 10 minutes, and teach back the invariant for 5 minutes. These planner allocations are not setup tasks.

- [https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/datasource/DataSourceTransactionManager.html](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/datasource/DataSourceTransactionManager.html) — **Connection binding and read-only setup**

**Sketch prompt:** Draw the lock holder, transaction-bound backend, local setting lifetime, cancellation/rollback owner and when the outer permit can be released.

**Teach-back:** Explain what proves same-backend use, how settings reset, and which layer owns cleanup.

### Separate Java DSA block — 60 minutes

Run the actual-attempt review queue first. D+3, D+7 and D+21 clocks start only from an actual independent first attempt; planned dates never start review clocks. Failed/overdue reviews replace new work. The shared attempt log is empty, so no due review is forecast. LC236 is deferred because prerequisite retrieval has no recorded evidence.

- 25 minutes pattern recall or new learning only if due queue permits
- 20 minutes independent solving or due cold review
- 15 minutes active retrieval and error log

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2027-01-17`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py scripts/test_dsa_review_queue_71_84.py`
**Shared actual-attempt log:** `docs/reboot/continuation-31-44/dsa/actual-attempt-log.json`

### Evidence block — 10 minutes

Record exact commands, exit status, observed values, cleanup result, learner-owned paths and blockers in `evidence/continuation-85-98/day-90.json`. Keep actual attempt dates blank until evidenced and separate temporary-witness validation from learner completion.
