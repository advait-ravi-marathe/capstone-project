# Bounded AI integration ownership amendment

This is a review-branch candidate. The published first-30 schedule and its Days 26–28 read-only comprehension lessons remain unchanged. The parent owns a separate 548-day planning manifest; target slots below require reconciliation with that manifest before board or calendar publication.

**Status:** `review_branch_only_not_merged_or_board_published`

The assistant supplies application and model-serving scaffolding. The learner owns a few bounded end-to-end AI integration changes, their tests, error handling and debugging. These replace equivalent comprehension/review tasks in the later plan; they do not add days, engineering minutes, or shared-block work. This is a scoped exception to the general read/trace-only FE/BE lane for OWN-INT-225, OWN-INT-332, OWN-INT-333, OWN-INT-420 and OWN-INT-507 only; first-30 contracts remain unchanged.

The parent maintains a separate 548-day manifest. These target slots are a proposed mapping for that plan, not new published daily contracts or YouTrack IDs. Reconcile against its actual tasks and budgets before board publication. Future target slots are not yet published as daily contracts.

## Bounded replacement slots

### Study Day 225 — Model-serving request/response contract

**Date:** 2027-06-08 · **Project:** CoverStory AI · **Unit:** `OWN-INT-225`<br>
**Status:** `starter_and_test_harness_in_this_review_branch; learner_adapter_remains_todo`

**Budget:** 165 engineering minutes + one 45-minute shared block. This task replaces equivalent engineering work, and the shared block remains unchanged.<br>
**Prerequisite:** The learner has passed Day 30's model artifact and held-out acceptance; an instructor witness or fixed fixture does not satisfy the learner gate.<br>
Implement the model adapter that checks exact feature names/order, finite numeric values and width, calls the Day 30 classifier, and returns only a synthetic document-type label with explicit scope. Raise InvalidPredictionRequest for caller-controlled validation failures; allow inference exceptions to reach the separate Day 332 error mapper.

**Assistant supplies:** loopback-only Python HTTP route, model artifact loading and process lifecycle, independent fixed model and request fixture.<br>
**Acceptance:** A real localhost HTTP success request and direct adapter cases return independently expected schedule, exclusion and definition labels; changed centroids change the expected label, ruling out a constant result. Direct malformed-input cases reject missing/extra keys, wrong feature order/width, booleans and non-finite values. The Day 225 HTTP selector tests success only; safe HTTP error mapping is taught and selected on Day 332.<br>
**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_integration_ownership.IntegrationOwnershipAcceptance.test_own_001_loopback_success_path_is_available_before_day332 ai.model_building.learner_acceptance.test_integration_ownership.IntegrationOwnershipAcceptance.test_own_001_builds_narrow_response_from_day30_model ai.model_building.learner_acceptance.test_integration_ownership.IntegrationOwnershipAcceptance.test_own_001_uses_classifier_for_independent_labels_across_models ai.model_building.learner_acceptance.test_integration_ownership.IntegrationOwnershipAcceptance.test_own_001_calls_day30_classifier ai.model_building.learner_acceptance.test_integration_ownership.IntegrationOwnershipAcceptance.test_own_001_rejects_malformed_and_order_confused_requests`<br>
**Local run command:** `python3 -m ai.model_building.model_api_scaffold --artifact PATH_TO_DAY30_MODEL_JSON --host 127.0.0.1 --port 8765`<br>
**Negative control:** Reject an adapter that ignores feature order, coerces booleans, returns a different class, accepts malformed requests, or echoes private exception details.<br>
**Sources:** [http.server — HTTP servers — BaseHTTPRequestHandler](https://docs.python.org/3/library/http.server.html); [json — JSON encoder and decoder — json — JSON encoder and decoder](https://docs.python.org/3/library/json.html)

### Study Day 332 — API failure handling and debugging

**Date:** 2027-09-23 · **Project:** CoverStory AI · **Unit:** `OWN-INT-332`<br>
**Status:** `starter_and_test_harness_in_this_review_branch; learner_error_handling_remains_todo`

**Budget:** 165 engineering minutes + one 45-minute shared block. This task replaces equivalent engineering work, and the shared block remains unchanged.<br>
**Prerequisite:** OWN-INT-225 valid and malformed request cases pass against the learner-owned adapter.<br>
Trace and repair one supplied model/API failure. Implement explicit invalid-request versus inference-failure handling and test status, safe body, feature order and unchanged synthetic model state.

**Assistant supplies:** local route, process startup and request-size limit, test-only monkeypatch fault injection with synthetic exception text; the scaffold does not expose an operational fault switch or emit request logs.<br>
**Acceptance:** Malformed requests return a stable 400 contract; unexpected model failures return a generic 500 contract; valid requests remain usable after a failure.<br>
**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_integration_ownership.IntegrationOwnershipAcceptance.test_own_002_actual_http_path_maps_validation_and_model_failures_safely ai.model_building.learner_acceptance.test_integration_ownership.IntegrationOwnershipAcceptance.test_own_002_distinguishes_input_errors_from_inference_failures ai.model_building.learner_acceptance.test_integration_ownership.IntegrationOwnershipAcceptance.test_own_002_internal_failure_is_generic_and_never_leaks_exception_text ai.model_building.learner_acceptance.test_integration_ownership.IntegrationOwnershipAcceptance.test_own_002_failure_does_not_mutate_model_and_same_server_recovers`<br>
**Local run command:** `python3 -m ai.model_building.model_api_scaffold --artifact PATH_TO_DAY30_MODEL_JSON --host 127.0.0.1 --port 8765`<br>
**Negative control:** A mutant that reports 200 for a failed prediction, exposes exception details, or leaves a stale prediction on error is rejected.<br>
**Sources:** [http.server — HTTP servers — BaseHTTPRequestHandler](https://docs.python.org/3/library/http.server.html)

### Study Day 333 — Small UI/API change and stale-response fix

**Date:** 2027-09-24 · **Project:** CoverStory AI · **Unit:** `OWN-INT-333`<br>
**Status:** `starter_and_test_harness_in_this_review_branch; learner_component_remains_todo`

**Budget:** 165 engineering minutes + one 45-minute shared block. This task replaces equivalent engineering work, and the shared block remains unchanged.<br>
**Prerequisite:** OWN-INT-225 and OWN-INT-332 pass; the loopback route is runnable and the UI route proxy is configured.<br>
Complete one bounded React request/result interaction against the supplied same-origin route; show safe pending, success and error states, and prevent an older response overwriting the latest request.

**Assistant supplies:** controlled numeric inputs and route proxy, local API lifecycle and fixed synthetic model.<br>
**Acceptance:** The UI sends the ordered feature contract, renders an allowed label only, handles rejected/malformed responses without details, and keeps the latest result under out-of-order responses.<br>
**Acceptance command:** `cd frontend && npm run test:model-integration`<br>
**Local run command:** `From frontend/: VITE_MODEL_API_TARGET=http://127.0.0.1:8765 npm run dev`<br>
**Instructor witness command:** `cd frontend && npm run test:model-integration-witness`<br>
**Negative control:** No-request, any-label, leaked-error, or stale-response mutants fail the same user-visible acceptance assertions.<br>
**Sources:** [<input> – React — Providing a label for an input; Controlling an input with a state variable](https://react.dev/reference/react-dom/components/input); [Using the Fetch API — Checking response status; Reading the response body](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch)

### Study Day 420 — Score presentation with model limitations

**Date:** 2027-12-20 · **Project:** StreamForge · **Unit:** `OWN-INT-420`<br>
**Status:** `planning_only_not_yet_published_as_a_daily_assignment`

**Budget:** 165 engineering minutes + one 45-minute shared block. This task replaces equivalent engineering work, and the shared block remains unchanged.<br>
**Prerequisite:** StreamForge's held-out ranking baseline and score semantics have independent evidence; toy CoverStory labels do not qualify.<br>
Make one small result-presentation change that preserves the distinction among ranking score, ordering and calibrated probability; use an unfamiliar score fixture and explain visible behavior through a test, not an architecture essay.

**Assistant supplies:** ranking endpoint and presentation shell, fixed synthetic ranking fixture.<br>
**Acceptance:** Displayed scores/order match the independent fixture and are not labeled as probability or user preference without validation.<br>
**Acceptance command:** not published yet; this outcome remains planning-only.<br>
**Negative control:** Reject swapped scores, stale result state, or probability/confidence labels unsupported by calibration evidence.<br>
**Sources:** [3.4. Metrics and scoring: quantifying the quality of predictions — 3.4.1. Which scoring function should I use?](https://scikit-learn.org/stable/modules/model_evaluation.html)

### Study Day 507 — User-facing interaction states and approval boundary

**Date:** 2028-03-16 · **Project:** Developer Workflow Assistant · **Unit:** `OWN-INT-507`<br>
**Status:** `planning_only_not_yet_published_as_a_daily_assignment`

**Budget:** 165 engineering minutes + one 45-minute shared block. This task replaces equivalent engineering work, and the shared block remains unchanged.<br>
**Prerequisite:** The Developer Workflow Assistant's read-only path and approval/denial contract have independent tests; no actual write authority is implied.<br>
Implement or debug one small UI/API state transition across idle, pending, success, denial and retry states while keeping the assistant read-only unless a separately authorized approval is explicit.

**Assistant supplies:** tool execution and approval service, synthetic read-only and denied-action fixtures.<br>
**Acceptance:** The UI mirrors independently supplied server state, prevents duplicate or unauthorized writes, and distinguishes retryable transport failure from policy denial.<br>
**Acceptance command:** not published yet; this outcome remains planning-only.<br>
**Negative control:** Reject stale state, implicit approval, duplicated action, hidden denial, or a UI that claims a write succeeded when the server rejected it.<br>
**Sources:** [Using the Fetch API — Checking response status; Reading the response body](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch)

## Capacity and evidence

Every target replaces an existing task of the same day's budget in the parent plan. Through Day 171 preserve 120 engineering minutes; later preserve 165 engineering minutes plus one shared 45-minute block. DSA practice remains in its separate repository and interview questions remain in the shared block. Certification/course time stays inside existing caps.

- No task implies learner completion.
- Loopback synthetic tests do not prove production ownership, scale, customer adoption, or certification.
- No external service, paid model, credentials, cloud spend, or deployment is part of this amendment.
