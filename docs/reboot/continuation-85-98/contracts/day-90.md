# Day 90 — 2027-01-17: Prove read-only enforcement and mapping-failure rollback

## Contract

**Engineering scope:** TimedPolicyRenewalReadStore transaction outcome
**Status:** Instructor fixture/selector installed; learner implementation remains unattempted.
**Daily capacity:** 180/195/210 minutes depending on 1–2/3/4 substantive DSA exercises; engineering remains 120 minutes (25 study, 85 practice/testing, 10 evidence). No extra mandatory setup work.

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

**Course (reference only; access unconfirmed):** [Master Hibernate and JPA with Spring Boot in 100 Steps — in28Minutes Official](https://www.udemy.com/course/hibernate-jpa-tutorial-for-beginners-in-100-steps).
- **FAQ 3 - Do read only methods need a transaction?** — section **Let's review with a few FAQs about Hibernate and JPA**, 4:58 (298 seconds); catalog title verified, video content not audited.
Use this as transaction foundation only; it does not replace the supplied JDBC/PostgreSQL worked example.

**Worked example (within the same cap):** Compare successful commit with thrown mapping error/rollback. A Spring readOnly flag alone does not prove no write; use fixed PostgreSQL snapshots and an attempted write.

**Focused sources:**
- [Spring programmatic transaction management — TransactionTemplate rollback rules](https://docs.spring.io/spring-framework/reference/data-access/transaction/programmatic.html)
- [Spring DataSourceTransactionManager API — Read-only transaction behavior and connection cleanup](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/datasource/DataSourceTransactionManager.html)

Video viewing: 4:58; allow at most 16:01 for the worked example and focused reading, then 4:01 for teach-back. Total is 25 minutes including pauses. Carry unfinished study forward and reduce implementation scope; no added hours. Viewing is not learner completion.


**Sketch prompt:** Draw the lock holder, transaction-bound backend, local setting lifetime, cancellation/rollback owner and when the outer permit can be released.

**Teach-back:** Explain what proves same-backend use, how settings reset, and which layer owns cleanup.

### Evidence block — 10 minutes

Record exact commands, exit status, observed values, cleanup result, learner-owned paths and blockers in `evidence/continuation-85-98/day-90.json`. Keep actual attempt dates blank until evidenced and separate temporary-witness validation from learner completion.

## DSA study (workload-tiered; actual attempts only)

**Time:** 1–2 substantive exercises = 60 minutes; 3 = 75 minutes; 4 = 90 minutes. Keep engineering at 120 minutes (25 study, 85 practice/test, 10 evidence), for 180/195/210 total minutes. Brief recall and logging are inside the tier; do not add exercises to fill time or exceed four. Failed/overdue retrieval displaces new guidance; excess due work carries forward with its original due date, including D+21.

**Guided/transfer selection:** First process actual failed or overdue D+3/D+7/D+21 retrieval, then due retrieval, then an eligible guided first encounter in the preserved prerequisite order. New introductions stop after roadmap Day 150. Candidate transfers come from the verified catalog only after checking prior exposure, access, and prerequisite readiness; unknown freshness is not fresh. Do not assign by old even/odd cadence.

Guided learning may use LeetCode Premium/editorial explanations. Before opening an editorial, attempt independently; if assistance is used, label the event guided/hinted/editorial-assisted even if accepted. A transfer is independent only after an unaided attempt, with the intended invariant explained; platform acceptance alone is insufficient. D, D+3, D+7, and D+21 are based on the earliest actual first-attempt local date, including guided attempts; never use planned dates or reset the clock. A transfer target does not start another anchor cycle.

Run `python3 scripts/dsa-review-queue.py --on-date "$(TZ=Asia/Kolkata date +%F)" --roadmap-day 90` first. The date command uses the actual Asia/Kolkata local study date; dates shown elsewhere are forecasts only. Record actual activity, assistance, outcome, and evidence in the append-only log; never infer completion from this contract. See [../../dsa/guided-transfer-policy.md](../../dsa/guided-transfer-policy.md) and [the DSA error log](../../dsa-error-log-template.md).
