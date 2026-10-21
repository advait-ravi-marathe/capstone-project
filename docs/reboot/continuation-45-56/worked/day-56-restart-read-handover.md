# Day 56 — Read after restart without changing stored rows

The independent parcel snapshot example shows two successful reads and complete ordered before/after values. The toy only creates a new reader object over the same in-memory data; it says nothing about Spring or PostgreSQL lifecycle. It also shows how a mutation can preserve row count while changing state.

Transfer: the supplied renewal test keeps the same disposable PostgreSQL alive, captures every policy/event/idempotency row, serves one GET, closes one Spring server context and starts a second context in the same test JVM, with a fresh browser process for each read, then serves the same GET again without reseeding. Assert HTTP200 for both responses, semantic JSON equality, all display values on both runs, and full database equality at baseline/after each phase. This proves the observed restarted Spring server-context scope within one test JVM only, not PostgreSQL crash durability, packaged-JAR launch, or production authentication.

Study25: 4:00 safe GET/no-write/layer recap,17:00 worked restart trace and handover,4:00 readiness. Practice85: 15 prerequisite/recovery gate,40 restart read,20 full comparisons,10 concise handover. Evidence10 records actual outputs, limitations and next step. Official: [RFC9110 safe methods](https://www.rfc-editor.org/rfc/rfc9110.html#section-9.2.1), [PostgreSQL read-only transaction mode](https://www.postgresql.org/docs/18/sql-set-transaction.html), [runbook steps](https://docs.aws.amazon.com/wellarchitected/latest/operational-excellence-pillar/ops_ready_to_support_use_runbooks.html).

Command: `npm --prefix frontend run build && CHROMIUM_BIN=/usr/bin/chromium ./mvnw -Plearner-tests -Dtest=RenewalPreviewTransportInfrastructureTest#day56PreviewReadIsStableAcrossRestartAndDoesNotWrite test`. Keep instructor smoke separate; it cannot establish learner completion.

### Supplied fictional observation and bounded handover

Use [the frozen synthetic expected sheet](day-56-observation.expected.json) to predict the two read results. The separate [renewal observation record](../../../../evidence/continuation-45-56/renewal-preview-day-56.observation.json) is an empty `not_run` template with actual fields null/empty; never copy expectations into actuals. It does not alter the existing daily attempt record.

Worked handover (fictional expected outcome, not an observed run):
1. **Scope:** two Spring server contexts in one test JVM, each read by a fresh browser process, sharing one disposable PostgreSQL instance.
2. **Expected:** both GETs return200 with identical eight-field semantic response and the same reference-only panel values.
3. **Expected data check:** complete ordered policy, event, and idempotency rows match at each live-context checkpoint; no reseed.
4. **Limit:** this does not prove PostgreSQL crash durability, packaged-JAR execution, production authentication, renewal pricing, or customer acceptance.
5. **Next command:** use the canonical Day56 build + learner selector shown above after prerequisites pass.
6. **Decision:** if a prerequisite or first read fails, preserve the first failure and carry the restart case forward; do not mark the rehearsal passed.

The actual learner's handover must replace this fictional expected account with recorded observations only after running the command. The current evidence template remains `not_run`.
