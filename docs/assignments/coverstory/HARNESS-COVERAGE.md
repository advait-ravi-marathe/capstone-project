# Assignment harness coverage

`harness-matrix.json` is generated from the canonical 600-card source. The integrity checker independently recomputes each source description hash, task excerpt, acceptance excerpt, component mapping, and planned-path list. Coverage states distinguish imported/mapped source from authored executable scaffolds, green fixture checks, assignment readiness, and learner results.

## Current verified state

- 600 source contracts imported and hash-verified; 594 primary guides mapped across 228 + 366 days; six linked components verified.
- 228 primary cards enumerate explicit planned files and 366 provide no planned repository path. A planned path is not proof that a file exists.
- **51** Phase 1 primary contracts have authored instructor scaffold families and fixture-backed checks: CAP-40/42/44/46/48/50/53/54/55/56/57/59/61/62/63/64/65/66/67/69/71/72/74/75/76/77/78/79/80/81/82/83/84/85/86/88/89/90/91/92/93/94/95/96/97/98/99/100/101/102/103/165. Learner behavior remains unimplemented; CAP-96 is instructor-owned gate infrastructure, and its saved learner work is local command/report wiring. CAP-52 remains a family candidate only.
- Of the 594 primary contracts, 51 have these authored families and 543 remain mapped-only. The generated matrix shows exact family/path/source mapping and acceptance IDs. Among Phase 2, 344 independent experiment contracts are mapped; one hidden-fault task requires a private preflight. Neither group is declared ready.
- The 50 learner acceptance suites across 51 authored families have been run separately from instructor setup; they stop at explicit TODOs or the safe fallback contract. Such failures are expected and are not evidence of completed learner work. No assignment is marked assignable: timed learner rehearsals and per-assignment prerequisite checks remain outstanding.
- Instructor setup checks have passed for the recorded family receipts. CAP-42/48/57/78/80 use embedded PostgreSQL harnesses; CAP-83 exercises Spring MVC error mapping; Python and Java harnesses use synthetic fixtures and independent reference oracles. These checks establish harness discrimination/setup, not the correctness of learner implementations or integrated runtime flows.
- Partial integrations stay explicit in `harness-matrix.json` and the family receipts: CAP-88 has a local role-policy oracle but no Spring auth filter/routes; CAP-89 and CAP-96 are local gates without CI; CAP-90 has field-cipher checks but no persisted envelope or cross-process key restore; CAP-93 has a simulated outage but no integrated worker probe output; CAP-94 has no actual artifact pair to compare; CAP-97 is a four-case synthetic evaluation only; CAP-98 has an isolated logical restore drill, CAP-99 has no actual product gate receipts, CAP-100 has only synthetic rows, CAP-101 has no provider route, CAP-102 has no production worker, and CAP-103 has only synthetic latency math. CAP-91/95 use fake assistance ports; CAP-92 is a synthetic dry-run and makes no legal retention claim.
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

All 50 learner acceptance suites across the 51 authored families have been run separately from instructor setup. CAP-40 and linked CAP-41 also retain historical TODO-only runs. The generated matrix distinguishes the 50 suites from the 543 mapped-only primaries with no suite. Source-specific defect witnesses and independent fixtures are recorded per family; all remain `CONDITIONAL`, not ready, until learner rehearsal and prerequisites are checked. The candidate validation receipt records earlier baseline totals; use the current matrix plus per-family receipts for this batch. Compilation, discovery, fixture parsing, assertion setup, and infrastructure failures are harness defects and must be fixed before interpreting learner outcomes.

See [`harness-matrix.json`](harness-matrix.json) for the generated per-day rows and [`docs/COVERAGE.md`](../../COVERAGE.md) for project-wide runtime/security limitations.
