# Day 89 — 2027-01-16: Run one unchanged owner-scoped read inside the timed decorator

## Contract

**Engineering scope:** TimedPolicyRenewalReadStore.findByPolicyIdAndOwnerId
**Status:** Instructor fixture/selector installed; learner implementation remains unattempted.
**Daily capacity:** 180 minutes: 60 minutes Java DSA separately, plus 120 minutes engineering (85 implementation/testing, 25 concept/course, 10 evidence). No extra mandatory setup work.

### Expected behavior

Place timed store in Components.renewalReadStore, keeping the Day79 bounded decorator outermost. Preserve arguments and Optional present/empty; delegate exactly once. No thread hops, REQUIRES_NEW, manual commit, physical close or extra timeout mechanism.

### Acceptance and expected TODO state

The selector is installed and runnable with learner TODOs restored. It may fail at named incomplete learner/prerequisite seams; missing classes, selectors, fixtures, tools or browser setup are infrastructure defects. The default Maven suite is the instructor infrastructure suite and excludes `learnerAcceptance` tests.

**Acceptance selector:** `src/test/java/assignments/renewal/TimedRenewalReadAcceptanceTest.java`
**Acceptance command:** `./mvnw -Plearner-tests -Dtest=TimedRenewalReadAcceptanceTest#delegatesExactlyOnceAndPreservesPresentAndEmptyFacts test`
**Infrastructure selector:** `src/test/java/assignments/renewal/Day85To98InfrastructureTest.java`
**Infrastructure command:** `./mvnw -Dtest=Day85To98InfrastructureTest test`

### Scope and safety limits

Keep Hikari maximum pool size 4, outer admission maximum 2, and acquisition timeout 1,000 ms. Make no HTTP deadline claim. Use one top-level REQUIRED read-only transaction; reject ambient transactions before mutation or delegate invocation. The transaction manager and JdbcTemplate use raw Hikari. Only the unchanged Day48 JdbcPolicyRenewalReadStore gets TransactionAwareDataSourceProxy. Place timed store in Components.renewalReadStore; keep the bounded decorator outermost.

Apply only transaction-local, parameterized PostgreSQL `set_config(..., true)` for lock_timeout and statement_timeout. Do not add automatic retries, production cancellation registry, HTTP-to-JDBC bridge, fault route, extra transaction/JDBC/socket timeout, thread hop, REQUIRES_NEW, manual commit, physical-connection close, dependency or infrastructure. Unknown SQL, mapping, rollback and cleanup failures must remain failures.

### Concept/course block — 25 minutes

**Course (reference only; access unconfirmed):** [Master Hibernate and JPA with Spring Boot in 100 Steps — in28Minutes Official](https://www.udemy.com/course/hibernate-jpa-tutorial-for-beginners-in-100-steps).
- **Step 63 - Implementing Transaction Management - 3 Things to Decide** — section **Transaction Management**, 4:38 (278 seconds); catalog title verified, video content not audited.
Use this as transaction foundation only; it does not replace the supplied JDBC/PostgreSQL worked example.

**Worked example (within the same cap):** Trace the top-level read-only TransactionTemplate callback: validate positive PostgreSQL budget values, apply only the transaction-local PostgreSQL settings, call the unchanged owner-scoped adapter once, and preserve present/empty. Do not add TransactionTemplate.setTimeout or another timeout mechanism; PostgreSQL statement_timeout is the only SQL-execution budget in this slice.

**Focused sources:**
- [Spring programmatic transaction management — TransactionTemplate, transaction settings and rollback](https://docs.spring.io/spring-framework/reference/data-access/transaction/programmatic.html)
- [Spring DataSourceTransactionManager API — Transaction-bound JDBC and transaction timeout integration](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/datasource/DataSourceTransactionManager.html)

Video viewing: 4:38; allow at most 16:17 for the worked example and focused reading, then 4:05 for teach-back. Total is 25 minutes including pauses. Carry unfinished study forward and reduce implementation scope; no added hours. Viewing is not learner completion.


**Sketch prompt:** Draw the lock holder, transaction-bound backend, local setting lifetime, cancellation/rollback owner and when the outer permit can be released.

**Teach-back:** Explain what proves same-backend use, how settings reset, and which layer owns cleanup.

### Separate Java DSA block — 60 minutes

Run the actual-attempt review queue first. D+3, D+7 and D+21 clocks start only from an actual independent first attempt; planned dates never start review clocks. Failed/overdue reviews replace new work. The shared attempt log is empty, so no due review is forecast. LC236 is deferred because prerequisite retrieval has no recorded evidence.

- 25 minutes pattern recall or new learning only if due queue permits
- 20 minutes independent solving or due cold review
- 15 minutes active retrieval and error log

**Queue:** `python3 scripts/dsa-review-queue.py --on-date 2027-01-16`
**Queue tests:** `python3 -m unittest scripts/test_dsa_review_queue.py scripts/test_dsa_review_queue_45_56.py scripts/test_dsa_review_queue_57_70.py scripts/test_dsa_review_queue_71_84.py`
**Shared actual-attempt log:** `docs/reboot/continuation-31-44/dsa/actual-attempt-log.json`

### Evidence block — 10 minutes

Record exact commands, exit status, observed values, cleanup result, learner-owned paths and blockers in `evidence/continuation-85-98/day-89.json`. Keep actual attempt dates blank until evidenced and separate temporary-witness validation from learner completion.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date 2027-01-16 --roadmap-day 89` first. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../../dsa/guided-transfer-policy.md](../../dsa/guided-transfer-policy.md) and [the DSA error log](../../dsa-error-log-template.md).
