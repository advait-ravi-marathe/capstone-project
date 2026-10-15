# Assignment harness coverage

`harness-matrix.json` is a generated index for 12 bounded 50-day JSON partitions under `harness-matrix/`, all derived from the canonical 600-card source. The integrity checker independently recomputes each source description hash, task excerpt, acceptance excerpt, component mapping, and planned-path list. Coverage states distinguish imported/mapped source from authored executable scaffolds, green fixture checks, assignment readiness, and learner results.

## Current verified state

- 600 source contracts imported and hash-verified; 594 primary guides mapped across 228 + 366 days; six linked components verified.
- 228 primary cards enumerate explicit planned files and 366 provide no planned repository path. A planned path is not proof that a file exists.
- **67** daily primary contracts have instructor scaffold records and fixture-backed checks: CAP-40/42/44/46/48/50/53/54/55/56/57/59/61/62/63/64/65/66/67/69/71/72/74/75/76/77/78/79/80/81/82/83/84/85/86/88/89/90/91/92/93/94/95/96/97/98/99/100/101/102/103/104/105/106/107/108/165/52/58/60/68/70/73/167/168/169/170. Learner behavior remains unimplemented; CAP-96 is instructor-owned gate infrastructure, and its saved learner work is local command/report wiring.
- Of the 594 primary contracts, 67 have these scaffold records and 527 remain mapped-only. The generated matrix shows exact family/path/source mapping and acceptance IDs. Among Phase 2, 344 independent experiment contracts are mapped; one hidden-fault task requires a private preflight.
- Every one of the 67 primary scaffold records now has a per-card result receipt under `docs/evidence/coverstory/`; linked-component receipts are separately recorded. Thirty-eight structured fixture digests were rechecked against local bytes during this validation; older receipts without structured digests remain clearly distinct.
- Days 001–010 now have separate chronological instructor-readiness records in [the Phase 1 day-start report](../../evidence/coverstory/phase1-days-001-010/README.md) and machine-readable [readiness data](../../evidence/coverstory/phase1-days-001-010/readiness.json). All ten are `READY_TO_START` based on a current passing command set, checked synthetic fixtures, positive/negative controls, and the listed module wiring. This readiness is independent of learner TODO completion and unmeasured elapsed time. The report includes exact commands, raw logs, fixture hashes, reset/isolation model, module scope and remaining learner requirements for every day.
- Learner acceptance is still separate: the first ten days' seams remain explicit TODOs and their learner evidence remains in each card receipt. A passing instructor run does not mark an assignment complete. The matrix's `assignable_now` is a learner completion/mastery counter; it is 0, and no learner assignment is complete or marked mastered. The other 57 authored scaffolds outside Days 001–010 still need their own chronological run/readiness receipt.
- Instructor setup checks have passed for the recorded family receipts. Days 002, 003, 005, and 006 use fresh embedded PostgreSQL harnesses for resettable schema/data; Days 001 and 002/008 also cross actual Spring app/module wiring. CAP-83 exercises Spring MVC error mapping; Python and Java harnesses use synthetic fixtures and independent reference oracles. The day report states each integration boundary and each deferred production adapter; harness readiness is not product completion.
- Day 002 CAP-42's PostgreSQL learner query now runs through the Catalog-owned evidence-total port/use case in tests. Linked CAP-43's sequential identity seam is wired through a Catalog-owned service and is discoverable in the running application context. Both learner methods remain TODO; no persistent import store, authenticated route, concurrent guarantee, or runtime datasource is present.
- Day 005 CAP-48/49 instructor checks pass against embedded PostgreSQL UTC/timezone mutants and a synthetic citation failure taxonomy. Day 006 CAP-50/51 checks cover a test-only exact-hash replay fake and SQL attempt-fanout/owner-scope controls. Day 042 CAP-170 has an independent pure-Java attempt-identity oracle with owner/event scoping and duplicate/order mutants; its learner seam remains TODO. See each day guide and `docs/evidence/coverstory/` receipts. Learner logic stays TODO.
- Partial integrations stay explicit in `harness-matrix.json` and the family receipts: CAP-88 has a local role-policy oracle but no Spring auth filter/routes; CAP-89 and CAP-96 are local gates without CI; CAP-90 has field-cipher checks but no persisted envelope or cross-process key restore; CAP-93 has a simulated outage but no integrated worker probe output; CAP-94 has no actual artifact pair to compare; CAP-97 is a four-case synthetic evaluation only; CAP-98 learner acceptance now calls its seam with disposable PostgreSQL and checks actual schema/rows/rollback/restore; the learner seam remains TODO, CAP-99 has no actual product gate receipts, CAP-100 has only synthetic rows, CAP-101 has no provider route, CAP-102 has a JDBC-backed learner adapter contract but no production worker, and CAP-103 has only synthetic latency math. CAP-91/95 use fake assistance ports; CAP-92 is a synthetic dry-run and makes no legal retention claim.
- CAP-96 supplies an instructor-owned local evidence gate rather than a learner business-logic seam; its saved learner work is to wire a local command to explicit reports. It is deliberately excluded from the current-candidate TODO-run count.

## Commands

```sh
python3 scripts/materialize_assignment_harnesses.py
python3 scripts/check_coverstory_curriculum.py
mvn -Dtest=CAP40ScaffoldInfrastructureTest test
mvn -Dtest=CAP54InstructorInfrastructureTest test
mvn -Plearner-tests -Dtest=CAP54AcceptanceTest test
mvn -Dtest=CAP55InstructorInfrastructureTest test
mvn -Plearner-tests -Dtest=CAP55AcceptanceTest test
mvn -Plearner-tests -Dtest=CAP40AcceptanceTest test
mvn -Dtest=CAP98InstructorInfrastructureTest test
mvn -Plearner-tests -Dtest=CAP98AcceptanceTest test
mvn -Dtest=CAP102InstructorInfrastructureTest test
mvn -Plearner-tests -Dtest=CAP102AcceptanceTest test
mvn -Dtest=CAP104InstructorInfrastructureTest,CAP105InstructorInfrastructureTest,CAP106InstructorInfrastructureTest,CAP107InstructorInfrastructureTest,CAP108InstructorInfrastructureTest test
mvn -Plearner-tests -Dtest=CAP104AcceptanceTest,CAP105AcceptanceTest,CAP106AcceptanceTest,CAP107AcceptanceTest,CAP108AcceptanceTest test
python3 -m unittest discover -s ai/tests -p 'test_coverstory_cap41_infrastructure.py'
python3 -m unittest discover -s ai/tests -p 'test_coverstory_cap41_acceptance.py'
mvn -Dtest=CatalogInstructorHarnessTest test
mvn -Dtest=CAP42InstructorInfrastructureTest,CAP48InstructorInfrastructureTest,CAP50InstructorInfrastructureTest,CAP51InstructorInfrastructureTest,CatalogInstructorHarnessTest test
mvn -Plearner-tests -Dtest=CAP42SqlAcceptanceTest test
mvn -Plearner-tests -Dtest=CAP48SqlAcceptanceTest,CAP50AcceptanceTest,CAP51SqlAcceptanceTest test
mvn -Plearner-tests -Dtest=CAP46AcceptanceTest test
mvn -Plearner-tests -Dtest=CAP50AcceptanceTest test
mvn -Plearner-tests -Dtest=CAP165AcceptanceTest test
mvn -Dtest=CAP170InstructorInfrastructureTest test
mvn -Plearner-tests -Dtest=CAP170Test test
python3 -m unittest discover -s ai/tests -p 'test_coverstory_cap44_infrastructure.py'
python3 -m unittest discover -s ai/coverstory/tests -p 'test_cap_44.py'
python3 -m unittest discover -s ai/tests -p 'test_coverstory_cap53_infrastructure.py'
python3 -m unittest discover -s ai/coverstory/tests -p 'test_cap_53.py'
python3 -m unittest discover -s ai/tests -p 'test_coverstory_cap56_infrastructure.py'
python3 -m unittest discover -s ai/coverstory/tests -p 'test_cap_56.py'
python3 -m unittest discover -s ai/tests -p 'test_coverstory_cap59_infrastructure.py'
python3 -m unittest discover -s ai/coverstory/tests -p 'test_cap_59.py'
python3 -m unittest discover -s ai/tests -p 'test_coverstory_cap61_infrastructure.py'
python3 -m unittest discover -s ai/coverstory/tests -p 'test_cap_61.py'
python3 -m unittest discover -s ai/tests -p 'test_coverstory_cap62_infrastructure.py'
python3 -m unittest discover -s ai/coverstory/tests -p 'test_cap_62.py'
```

All 60 primary learner acceptance suites across the 67 scaffold records have been run separately from instructor setup; linked CAP-49 and CAP-51 acceptance runs are separately recorded. Days 001–010 now carry `READY_TO_ATTEMPT` status in the matrix from current instructor-only runs, synthetic positive/negative controls, and resolved module wiring. This status does not depend on learner TODO completion or measured time; the individual TODOs and their prior acceptance outcomes remain explicit. The day report distinguishes instructor setup from actual app integration: Day 001 starts Spring/React and the fixed local CAP-41 service; Day 002 confirms the Catalog service/port graph; Day 008 verifies the registered import route fails closed without trusted principal injection; Day 010 validates the Application public ports with a test-only fixture adapter, not a production auth endpoint. Days 002, 003, 005 and 006 use fresh embedded PostgreSQL instances. The other 57 authored scaffolds still need their own chronological run/readiness receipt; 527 primary cards remain mapped-only. The saved authenticated login-return, owned shortlist persistence, quote refresh/review and full UI/API/PostgreSQL flow remain deferred. Source-specific defect witnesses and independent fixtures are recorded per family. No learner assignment is marked complete or mastered. Compilation, discovery, fixture parsing, assertion setup, and infrastructure failures are harness defects and must be fixed before interpreting learner outcomes.

See [`harness-matrix.json`](harness-matrix.json) for counts and partition links, [`harness-matrix/`](harness-matrix/) for generated per-day rows, and [`docs/COVERAGE.md`](../../COVERAGE.md) for project-wide runtime/security limitations.
