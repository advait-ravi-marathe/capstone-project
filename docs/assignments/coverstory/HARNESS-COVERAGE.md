# Assignment harness coverage

`harness-matrix.json` is generated from the canonical 600-card source. The integrity checker independently recomputes each source description hash, task excerpt, acceptance excerpt, component mapping, and planned-path list. Coverage states distinguish imported/mapped source from authored executable scaffolds, green fixture checks, assignment readiness, and learner results.

## Current verified state

- 600 source contracts imported and hash-verified; 594 primary guides mapped across 228 + 366 days; six linked components verified.
- 228 primary cards enumerate explicit planned files and 366 provide no planned repository path. A planned path is not proof that a file exists.
- **57** Phase 1 primary contracts have authored instructor scaffold families and fixture-backed checks: CAP-40/42/44/46/48/50/53/54/55/56/57/59/61/62/63/64/65/66/67/69/71/72/74/75/76/77/78/79/80/81/82/83/84/85/86/88/89/90/91/92/93/94/95/96/97/98/99/100/101/102/103/104/105/106/107/108/165/52/58/60/68/70/167/168/169. Learner behavior remains unimplemented; CAP-96 is instructor-owned gate infrastructure, and its saved learner work is local command/report wiring.
- Of the 594 primary contracts, 57 have these authored families and 537 remain mapped-only. The generated matrix shows exact family/path/source mapping and acceptance IDs. Among Phase 2, 344 independent experiment contracts are mapped; one hidden-fault task requires a private preflight. Neither group is declared ready.
- The 56 learner acceptance suites across 57 authored families have been run separately from instructor setup; they stop at explicit TODOs or the safe fallback contract. Such failures are expected and are not evidence of completed learner work. No assignment is marked assignable. CAP-40 has a verified executor preflight and is `READY_TO_ATTEMPT`; its timebox remains `TIMEBOX_NOT_VALIDATED`, so rehearsal is tracked separately and does not block starting.
- Instructor setup checks have passed for the recorded family receipts. CAP-42/48/57/78/80/98/102 use embedded PostgreSQL harnesses; CAP-83 exercises Spring MVC error mapping; Python and Java harnesses use synthetic fixtures and independent reference oracles. These checks establish harness discrimination/setup, not the correctness of learner implementations or integrated runtime flows.
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
mvn -Dtest=CAP42InstructorInfrastructureTest,CAP48InstructorInfrastructureTest test
mvn -Plearner-tests -Dtest=CAP42SqlAcceptanceTest test
mvn -Plearner-tests -Dtest=CAP48SqlAcceptanceTest test
mvn -Plearner-tests -Dtest=CAP46AcceptanceTest test
mvn -Plearner-tests -Dtest=CAP50AcceptanceTest test
mvn -Plearner-tests -Dtest=CAP165AcceptanceTest test
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

All 56 learner acceptance suites across the 57 authored families have been run separately from instructor setup. CAP-40 now has verified executor preflight and READY_TO_ATTEMPT status, distinct from TIMEBOX_NOT_VALIDATED and its explicit TODO result; CAP-41 has shared tie/score/truncation contracts and expected TODO errors. CAP-168 covers only its five-line quote-review parser oracle; the saved authenticated login-return, owned shortlist, quote refresh/review and full UI/API/PostgreSQL flow remain deferred. The generated matrix distinguishes the 56 suites from the 537 mapped-only primaries with no suite. Source-specific defect witnesses and independent fixtures are recorded per family; all other mapped families remain conditional until learner rehearsal and prerequisites are checked; CAP-40 is only ready to attempt, and no learner assignment is marked complete or mastered. Compilation, discovery, fixture parsing, assertion setup, and infrastructure failures are harness defects and must be fixed before interpreting learner outcomes.

See [`harness-matrix.json`](harness-matrix.json) for the generated per-day rows and [`docs/COVERAGE.md`](../../COVERAGE.md) for project-wide runtime/security limitations.
