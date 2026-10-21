# Current command crosswalk for Days 148, 149, 153 and 154

Additive source-verified correction, not a rewrite of historical receipts. Source: [`3a93ed9ae93dd68a9eda0418518713ac29a38eb6`](https://github.com/advait-ravi-marathe/capstone-project/tree/3a93ed9ae93dd68a9eda0418518713ac29a38eb6).

Existing daily evidence templates retain their historical command names and provenance fields. For a new attempt, use the current selector below and record a new truthful result in the existing evidence workflow. Do not claim a historical result came from this later crosswalk.

## Preconditions and limits

- A and B must be independently built real artifacts with different SHA-256 digests and verified source/build provenance. A is the frozen pre-report baseline; B adds the inspection report. Do not rename one JAR twice or invent labels/provenance.
- `COVERSTORY_RELEASE_PAIR_DIR` must contain `baseline.jar`, `candidate.jar` and the checksum-bound `artifact-pair-manifest.json` supplied by the existing build workflow. A missing baseline is a named blocker, not a reason to fabricate one or solve retained learner TODOs.
- Required system property: `coverstory.release.pair.manifest`. Arbitrary provenance text flags do not replace the manifest.
- Use the existing local-only, run-owned PostgreSQL fixture and documented packaged-app prerequisites. Do not target a user's or remote database.
- The claim is unchanged-schema local A/B compatibility. It does not establish migration rollback, cloud availability, public authentication or production zero-downtime operation.
- This crosswalk verifies current source selectors, not a fresh runtime execution. Runtime is blocked until the real checksum-bound A/B artifact pair exists; this is distinct from a command or selector error.

## Day 148: Run distinct packaged artifacts

Current [manifest](continuation-141-154/manifest.json) and [contract](continuation-141-154/contracts/day-148.md).

```sh
./mvnw -Plocal-app,learner-tests -Dtest=TwoArtifactCompatibilityAcceptanceTest#day148RunsDistinctArtifactsAndReportCapabilityOnly -Dcoverstory.release.baseline.jar="$COVERSTORY_RELEASE_PAIR_DIR/baseline.jar" -Dcoverstory.release.candidate.jar="$COVERSTORY_RELEASE_PAIR_DIR/candidate.jar" -Dcoverstory.release.pair.manifest="$COVERSTORY_RELEASE_PAIR_DIR/artifact-pair-manifest.json" test
```

## Day 149: Prove retained state across A and B

Current [manifest](continuation-141-154/manifest.json) and [contract](continuation-141-154/contracts/day-149.md).

```sh
./mvnw -Plocal-app,learner-tests -Dtest=TwoArtifactCompatibilityAcceptanceTest#day149RunsCrossVersionRetainedStateMatrix -Dcoverstory.release.baseline.jar="$COVERSTORY_RELEASE_PAIR_DIR/baseline.jar" -Dcoverstory.release.candidate.jar="$COVERSTORY_RELEASE_PAIR_DIR/candidate.jar" -Dcoverstory.release.pair.manifest="$COVERSTORY_RELEASE_PAIR_DIR/artifact-pair-manifest.json" test
```

## Day 153: Complete the same-database A to B to A rehearsal

Current [manifest](continuation-141-154/manifest.json) and [contract](continuation-141-154/contracts/day-153.md).

```sh
./mvnw -Plocal-app,learner-tests -Dtest=TwoArtifactCompatibilityAcceptanceTest#day153RunsCompleteObservedPairMatrixAndFallbackDecision -Dcoverstory.release.baseline.jar="$COVERSTORY_RELEASE_PAIR_DIR/baseline.jar" -Dcoverstory.release.candidate.jar="$COVERSTORY_RELEASE_PAIR_DIR/candidate.jar" -Dcoverstory.release.pair.manifest="$COVERSTORY_RELEASE_PAIR_DIR/artifact-pair-manifest.json" test
```

## Day 154: Recover and defend the limited compatibility claim

Current [manifest](continuation-141-154/manifest.json) and [contract](continuation-141-154/contracts/day-154.md).

```sh
./mvnw -Plocal-app,learner-tests -Dtest=InspectionReportContractAcceptanceTest,ReleaseInspectionCommandAcceptanceTest,TwoArtifactCompatibilityAcceptanceTest#day145PackagedInvalidModeFailsBeforeDatasourceInitialization+day146RefusesAbsentSchemaWithoutStartingMigration+day146ExistingSchemaProducesReadOnlyMissingWorkObservation+day146DatabaseReadFailureEmitsNoSuccessFrame+day146SchemaGateAcceptsExactV001ToV005AndRejectsTypmodMutation+day146SchemaGateRejectsMigrationChecksumDriftWithoutRepair+day148RunsDistinctArtifactsAndReportCapabilityOnly+day149RunsCrossVersionRetainedStateMatrix+day153RunsCompleteObservedPairMatrixAndFallbackDecision,BinaryFallbackAcceptanceTest,ReleaseVerifierProcessAcceptanceTest -Dcoverstory.release.baseline.jar="$COVERSTORY_RELEASE_PAIR_DIR/baseline.jar" -Dcoverstory.release.candidate.jar="$COVERSTORY_RELEASE_PAIR_DIR/candidate.jar" -Dcoverstory.release.pair.manifest="$COVERSTORY_RELEASE_PAIR_DIR/artifact-pair-manifest.json" test
```

The installed class is `TwoArtifactCompatibilityAcceptanceTest`; the current Day 148/149/153 selector names above are source-verified. The older `day148RunsFrozenAAndReportCapableB` name is not current. Do not alter old evidence files or overwrite a historical attempt. This document supplies current invocation guidance only.
