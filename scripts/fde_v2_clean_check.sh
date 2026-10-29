#!/usr/bin/env bash
set -euo pipefail

# Run after the two pinned v2 requirements files and frontend lockfile are
# installed. No model weights, credentials, cloud calls or provider endpoints.
python3 -m unittest scripts.test_fde_v2_sprint1 scripts.test_fde_v2_sprint2 scripts.test_fde_v2_p1_closeout scripts.test_fde_v2_m21_draft
FDE_V2_CANDIDATE=ai.fde_v2.coverstory.reference python3 -m unittest \
  ai.fde_v2.learner_acceptance.test_units \
  ai.fde_v2.learner_acceptance.test_continuation_units \
  ai.fde_v2.tests.test_sprint1 ai.fde_v2.tests.test_sprint1_witnesses \
  ai.fde_v2.tests.test_continuation_witnesses ai.fde_v2.tests.test_continuation_api \
  ai.fde_v2.tests.test_local_pipeline_witness ai.fde_v2.tests.test_m21_draft
npm --prefix frontend run test:fde-v2
(cd frontend && RUN_FDE_V2=true npx vitest run)
