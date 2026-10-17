#!/usr/bin/env sh
set -eu
# CAP-645 learner runner. It must use the actual UI, API and independent PostgreSQL oracle.
# The starter intentionally routes to a tagged learner acceptance test that fails at TODO.
exec ./mvnw -Plearner-tests -Dtest=CSM14AcceptanceTest#cap645_admissionRunnerChecksUiApiAndIndependentPostgres test
