#!/usr/bin/env sh
set -eu
# CAP-645 learner evidence-gate behavior contract.
# The six-event bundle is synthetic; this is not the live candidate E2E run.
exec ./mvnw -Plearner-tests -Dtest=CSM14AcceptanceTest#cap645_admissionIsDerivedFromBoundUiApiAndDatabaseEvidence test
