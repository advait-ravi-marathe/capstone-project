# Day 56 — Read after restart without changing stored rows

The independent parcel snapshot example shows two successful reads and complete ordered before/after values. Its “restart” is only a new reader object over the same in-memory data; do not describe it as a process or database restart. It also shows how a mutation can preserve row count while changing state.

Transfer: the supplied renewal test keeps the same disposable PostgreSQL alive, captures every policy/event/idempotency row, serves one GET, stops Spring, starts a fresh Spring context and browser, then serves the same GET again without reseeding. Assert HTTP200 for both responses, semantic JSON equality, all display values on both runs, and full database equality at baseline/after each phase. This proves the observed Spring-process restart scope only, not PostgreSQL crash durability, packaged-JAR launch, or production authentication.

Study25: 4:00 safe GET/no-write/layer recap,17:00 worked restart trace and handover,4:00 readiness. Practice85: 15 prerequisite/recovery gate,40 restart read,20 full comparisons,10 concise handover. Evidence10 records actual outputs, limitations and next step. Official: [RFC9110 safe methods](https://www.rfc-editor.org/rfc/rfc9110.html#section-9.2.1), [PostgreSQL read-only transaction mode](https://www.postgresql.org/docs/18/sql-set-transaction.html), [runbook steps](https://docs.aws.amazon.com/wellarchitected/latest/operational-excellence-pillar/ops_ready_to_support_use_runbooks.html).

Command: `npm --prefix frontend run build && CHROMIUM_BIN=/usr/bin/chromium ./mvnw -Plearner-tests -Dtest=RenewalPreviewTransportInfrastructureTest#day56PreviewReadIsStableAcrossRestartAndDoesNotWrite test`. Keep instructor smoke separate; it cannot establish learner completion.
