# Assignment harness coverage

`harness-matrix.json` is generated from the canonical 600-card source. The integrity checker independently recomputes each source description hash, task excerpt, acceptance excerpt, component mapping, and planned-path list. Coverage states distinguish imported/mapped source from authored executable scaffolds, green fixture checks, assignment readiness, and learner results.

## Current verified state

- 600 source contracts imported and hash-verified; 594 primary guides mapped across 228 + 366 days; six linked components verified.
- 228 primary cards enumerate explicit planned files (including labelled path bullets); 366 provide no planned repository path. A source path is a plan, not evidence that a file exists.
- Twelve Phase 1 assignments have an authored CoverStory scaffold family at this checkpoint: CAP-40, CAP-42, CAP-44, CAP-46, CAP-48, CAP-50, CAP-53, CAP-56, CAP-59, CAP-61, CAP-62, CAP-165. CAP-40’s earlier focused scaffold remains conditional on its required timed rehearsal. CAP-42/48 use actual embedded PostgreSQL with synthetic rows/date-spine data; CAP-44/53/56/59/61/62 are typed Python learner seams; CAP-46/50/165 have typed Java learner TODOs and fixture-driven acceptance suites. Independent instructor smoke is green; learner behavior has not been implemented or marked complete.
- Eight early catalog contracts are assigned to reusable family candidates (selection identity, catalog aggregation, public ranking, bounded review queue, UTC aggregation, import counting, source-version identity, and import API). Twelve assignment scaffolds currently have local code: CAP-40/42/44/46/48/50/53/56/59/61/62/165; CAP-52 remains a mapped family candidate, not a supported assignment.
- The remaining 582 primary assignments are mapped-only in the current candidate. Phase 2 uses progressively faded experiment/evaluation contracts; entries that specify hidden fresh faults require a private preflighted defect and symptom-only task setup. No hidden-fault candidate is claimed ready.
- Current-candidate learner outcomes are `NOT_RUN`. Prior evidence files remain historical and must not be read as a result for this candidate.
- No assignment is marked `ASSIGNABLE`: none has a recorded timed rehearsal and all assignment-specific prerequisites have not been checked. Maven static compile and fixture smoke do not establish runtime behavior or time fit.

## Commands

```sh
python3 scripts/materialize_assignment_harnesses.py
python3 scripts/check_coverstory_curriculum.py
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

Eleven current-candidate learner suites were run for CAP-42/44/46/48/50/53/56/59/61/62/165 and stopped at their explicit unimplemented TODO seams. CAP-40's learner outcome remains `NOT_RUN` on this candidate; its earlier result is historical. The other 582 mapped-only assignments have no learner suite to run. `CatalogInstructorHarnessTest` verifies fixture content, case identity, and learner stub discovery. `CAP165HarnessDiscriminationTest` separately accepts an exact pair-identity witness and rejects delimiter concatenation. Other families still lack their own correct/defective witness checks, so all remain `CONDITIONAL`, not ready. Current test totals are in [the candidate validation receipt](evidence/harness-candidate-2026-10-14.md). Compilation, discovery, fixture parsing, assertion setup, and infrastructure failures are harness defects and must be fixed before interpreting learner outcomes.
