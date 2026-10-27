#!/usr/bin/env python3
"""Generate the first-30 model-first contracts from their day table."""

from __future__ import annotations

import argparse
import datetime as dt
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs/model-first-roadmap"
START = dt.date(2026, 10, 27)

PY = ("Python Tutorial, 3. An Informal Introduction to Python", "https://docs.python.org/3/tutorial/introduction.html")
NUMPY = ("NumPy: the absolute basics for beginners", "https://numpy.org/doc/stable/user/absolute_beginners.html")
SK = ("12. Common pitfalls and recommended practices", "https://scikit-learn.org/stable/common_pitfalls.html")
SK_LEAK = ("12.2. Data leakage", "https://scikit-learn.org/stable/common_pitfalls.html")
SK_PRE = ("12.1. Inconsistent preprocessing", "https://scikit-learn.org/stable/common_pitfalls.html")
SK_METRICS = ("3.4. Metrics and scoring: quantifying the quality of predictions", "https://scikit-learn.org/stable/common_pitfalls.html")
SK_THRESHOLD = ("3.3. Tuning the decision threshold for class prediction", "https://scikit-learn.org/stable/common_pitfalls.html")
SK_CV = ("3.1. Cross-validation: evaluating estimator performance", "https://scikit-learn.org/stable/common_pitfalls.html")
SK_PIPE = ("8.1. Pipelines and composite estimators", "https://scikit-learn.org/stable/common_pitfalls.html")
SK_CAL = ("1.16. Probability calibration", "https://scikit-learn.org/stable/common_pitfalls.html")
SK_DATA = ("9.3. Generated datasets", "https://scikit-learn.org/stable/common_pitfalls.html")
TORCH = ("Learn the Basics", "https://docs.pytorch.org/tutorials/beginner/basics/intro.html")
TEST_METHODS = {
    2:"test_day_02_sigmoid", 3:"test_day_03_binary_cross_entropy", 4:"test_day_04_dot_product",
    5:"test_day_05_finite_difference", 6:"test_day_06_batch_step_matches_independent_fixed_contract",
    7:"test_day_07_split_is_disjoint_and_ordered", 8:"test_day_08_reports_all_forbidden_features",
    9:"test_day_09_standardizer_uses_training_statistics", 10:"test_day_10_majority_tie_breaks_to_zero",
    11:"test_day_11_confusion_matrix_has_exact_counts", 12:"test_day_12_undefined_metrics_remain_none",
    13:"test_day_13_slice_denominators_preserve_rows", 14:"test_day_14_comparison_reports_source_ordered_error_ids",
    15:"test_day_15_rectangular_matrix_shape", 16:"test_day_16_batched_scores_match_scalar_oracle",
    17:"test_day_17_gradient_keeps_bias_and_feature_axes", 18:"test_day_18_extreme_sigmoid_is_finite",
    19:"test_day_19_curve_retains_every_rate_and_checkpoint", 20:"test_day_20_experiment_record_preserves_seed_and_ordered_metadata",
    21:"test_day_21_baseline_against_same_heldout_labels", 22:"test_day_22_preprocessing_fit_must_exclude_evaluation_ids",
    23:"test_day_23_calibration_bins_match_fixed_counts", 24:"test_day_24_document_fixture_is_fixed_and_split_safe",
    25:"test_day_25_error_ids_retain_order_and_ignore_correct_rows",
    26:"test_day_26_supplied_route_reading_is_paired_with_model_error_analysis",
    27:"test_day_27_seeded_failure_analysis_uses_observed_prediction_errors",
    28:"test_day_28_report_does_not_promote_score_to_decision",
    29:"test_day_29_roundtrip_model_preserves_feature_order_and_values",
    30:"test_day_30_model_scores_fixed_rows_in_declared_order",
}
COMPREHENSION = {
    26: {"questions": ["Which POST route sends the question? Does the browser choose the owner? Where does the server obtain caller identity?"], "expected": ["The UI posts to /api/ai/policy-assistant with question and same-origin credentials plus CSRF. It sends no owner field in that request. Spring checks Authentication and forwards authentication.getName() as owner."]},
    27: {"questions": ["What status/code does a blank question produce? What happens when the gateway reports unavailable? Which safe UI message is shown?"], "expected": ["Blank question: HTTP 400 INVALID_QUESTION. Gateway unavailable: HTTP 503 AI_PROVIDER_UNAVAILABLE. The UI shows a safe local-service-unavailable message."]},
    28: {"questions": ["What do executionMode, providerRequestMade and liveQualityEvaluated say in the fixture? Is a document type score an insurance decision?"], "expected": ["offline_synthetic_fixture, false, false; no. The fixture is not model output, insurance advice or a coverage decision."]},
}

# Concept, taught material, learner-owned AI/model task, independent acceptance,
# negative control, prerequisite, source tuple, time split.
DAYS = [
    ("A first trainable model", "Python values, lists, loops, JSON, a score, sigmoid and one gradient step", "Implement one batch logistic-regression step in learner.py using the two fixed synthetic rows", "Expected weights are [0.0, 0.05, -0.05], inputs remain unchanged, and independent BCE loss falls", "No-update and reversed-gradient candidates must fail the exact weight/loss checks", "None; instructor worked example is runnable first", PY, "20m concept + 85m model step + 15m evidence"),
    ("Scores and probabilities", "A linear score is not a probability; sigmoid maps it to (0,1)", "Implement stable sigmoid and prediction for the supplied bias-first weight format", "Check fixed scores -2, 0 and 2 against independently calculated values", "Reject NaN, infinity and wrong feature width", "Day 1 Python lists and arithmetic", PY, "20m concept + 85m model code + 15m evidence"),
    ("Binary cross-entropy", "Log loss penalizes confident wrong predictions; labels are 0/1", "Implement mean binary cross-entropy for a batch of probabilities and labels", "Match fixed hand-calculated two-row loss and show it changes with confidence", "Reject empty batches, invalid labels, and probabilities outside (0,1)", "Day 2 scores and labels", PY, "20m concept + 85m model code + 15m evidence"),
    ("Vectors and dot products", "Represent a feature row and weights as aligned vectors; bias is separate", "Implement and test dot product prediction by hand for three feature rows", "Exact scores match independent scalar arithmetic", "Ragged rows or mismatched dimensions are rejected", "Days 1–3", PY, "25m concept + 80m model exercise + 15m evidence"),
    ("Check a gradient numerically", "A finite difference estimates how loss changes when one parameter moves", "Compare one analytic parameter gradient with a central finite-difference estimate", "Absolute difference is below a stated tolerance on fixed synthetic data", "Wrong sign and wrong parameter index are detected", "Days 1–4", PY, "25m concept + 80m model exercise + 15m evidence"),
    ("Batch gradient descent", "Average per-example gradients before one update; learning rate controls step size", "Extend the one-step model to a small deterministic batch", "Independent expected gradient and post-step loss match fixed values", "Summing without averaging and reversing the sign are rejected", "Days 1–5", PY, "20m concept + 85m model code + 15m evidence"),
    ("Keep a held-out set", "Train, validation and test answer different questions; split before fitting", "Create a deterministic split manifest for synthetic document labels", "Exact record IDs are disjoint and class counts match the independent manifest", "Duplicate IDs and a leaked test row fail", "Python collections and Day 6 batch model", SK_CV, "25m concept + 80m split/eval + 15m evidence"),
    ("Spot target leakage", "A feature derived from the answer makes evaluation dishonest", "Add a deliberately leaky feature, measure the misleading result, then remove it", "The test reports the leaky column and the clean baseline separately", "A pipeline that fits preprocessing on test rows fails", "Day 7 split", SK_LEAK, "25m concept + 80m leakage experiment + 15m evidence"),
    ("Scale using training data only", "Mean and scale are learned on train, then reused unchanged", "Implement a tiny two-feature standardizer fit only on the training partition", "Train transformed columns have expected mean/scale; test uses same stored values", "Fitting on combined train+test data is caught by a shifted test fixture", "Day 7 split and Day 8 leakage", SK_PRE, "25m concept + 80m model exercise + 15m evidence"),
    ("Majority and centroid baselines", "A simple baseline can expose class imbalance and weak modeling", "Compare majority-class and nearest-centroid predictions on fixed labeled rows", "Exact predictions and independent accuracy counts match", "A baseline that learns from held-out labels fails", "Days 7–9", PY, "20m concept + 85m baseline experiment + 15m evidence"),
    ("Confusion matrix", "True/false positives/negatives answer different questions", "Build a confusion matrix from fixed labels and predictions", "Exact four cell counts match a hand-derived oracle", "Swapped positive class or omitted row is detected", "Day 10 baseline", SK_METRICS, "20m concept + 85m metrics + 15m evidence"),
    ("Precision and recall", "Threshold choices trade missed positives against false alarms", "Calculate precision/recall at two fixed thresholds and explain the change", "Exact counts and fractions match independently enumerated predictions", "Undefined denominator must be explicit, not silently reported as 1.0", "Day 11 confusion matrix", SK_THRESHOLD, "25m concept + 80m threshold experiment + 15m evidence"),
    ("Inspect errors by slice", "Aggregate scores can hide minority or document-type failures", "Report metrics for two predeclared synthetic slices with denominators", "Every slice count and metric matches the independent expected table", "Empty/unknown slice and disappearing rows are rejected", "Days 11–12", SK_METRICS, "25m concept + 80m slice analysis + 15m evidence"),
    ("Improve a linear baseline", "Change one factor at a time and preserve a clean held-out comparison", "Compare the Day 1 model with a fixed-feature baseline; write a short error note", "Result table includes exact split, baseline, metric and error examples", "Selecting a model using test results is flagged as leakage", "Days 1–13", SK_METRICS, "20m concept + 85m controlled experiment + 15m evidence"),
    ("NumPy arrays and shape", "ndarray stores homogeneous numeric arrays; shape determines valid operations", "Recreate the fixed feature matrix and report dtype, shape and label shape", "Independent expected shape/dtype and values match", "Ragged array or transposed feature matrix is rejected", "Comfort with Python lists, loops and dot products", NUMPY, "25m concept + 80m array exercise + 15m evidence"),
    ("Vectorized prediction", "Broadcasting applies the same linear operation across rows", "Implement vectorized scores and compare them row-by-row to Day 4 scalar scores", "All scores match scalar oracle within 1e-12", "Wrong axis/bias broadcasting fails a non-square fixture", "Day 15 shapes", NUMPY, "20m concept + 85m vectorization + 15m evidence"),
    ("Vectorized gradient", "Array reductions must preserve feature and batch axes", "Implement a NumPy batch gradient and compare with Day 6 hand oracle", "Bias and both feature gradients match expected values", "Summing wrong axis or dividing by feature count is rejected", "Days 15–16", NUMPY, "25m concept + 80m gradient exercise + 15m evidence"),
    ("Numerical edge cases", "Floating-point precision and extreme logits can cause overflow", "Compare a stable sigmoid implementation over moderate and extreme scores", "Outputs are finite and monotonic at fixed boundary values", "NaN, overflowed output or clipping that changes moderate values is caught", "Day 16 vectorized prediction", NUMPY, "25m concept + 80m stability experiment + 15m evidence"),
    ("Learning-rate comparison", "On a simple convex loss, a step that is too large can oscillate and diverge; this is a teaching probe, not a universal model claim", "Run three fixed rates on f(w)=(w−2)^2 and record every loss checkpoint", "The independent contract requires the small rate to reduce loss and the high rate to increase it", "Cherry-picking only the best endpoint or omitting the high-rate curve fails", "Days 6 and 17", PY, "20m concept + 85m controlled runs + 15m evidence"),
    ("Repeatable experiments", "Seed, split, feature list and config are part of a reproducible result", "Write a compact experiment record and reproduce the same split/model output", "Two fresh runs produce the same record fields and predictions", "Changing split seed without recording it fails the evidence check", "Days 7–19", PY, "20m concept + 85m reproducibility + 15m evidence"),
    ("A scikit-learn pipeline baseline", "A Pipeline keeps preprocessing and estimator fit behavior together", "Fit a logistic baseline through a preprocessing Pipeline on the training partition", "Predictions and metric counts match a fixed independent control envelope", "Preprocessing fit on held-out rows is rejected", "Train/test discipline and basic probability", SK_PIPE, "25m concept + 80m baseline + 15m evidence"),
    ("Cross-validation without test tuning", "Cross-validation estimates variability on training data; final test stays untouched", "Compare fold scores and retain one final test evaluation", "Fold membership and score count match the fixed split plan", "Repeated test-driven hyperparameter selection is flagged", "Day 21 pipeline", SK_CV, "25m concept + 80m evaluation + 15m evidence"),
    ("Uncertainty and calibration basics", "A score near 0.5 is not proof of calibrated probability", "Bin a fixed set of probabilities and report counts plus mean confidence/accuracy", "Independent bin counts and averages match", "Empty bins and rounding that changes assignment are detected", "Days 11–13 and probability basics", SK_CAL, "25m concept + 80m calibration exercise + 15m evidence"),
    ("Build a synthetic document dataset", "Labels describe document type, not truth, risk or customer eligibility", "Create the 12 declared synthetic schedule/exclusion/definition rows", "Exact doc-01..doc-12 IDs, cyclic labels, 6/3/3 splits and fixed text match the independent contract", "Duplicate text/IDs across train, validation or test fail exact row comparison", "Days 7–23; no private data", SK_DATA, "20m concept + 85m dataset curation + 15m evidence"),
    ("Compare document-type baselines", "A useful model is measured against a trivial baseline on held-out data", "Compare majority, lexical and linear model errors on the same held-out fixture", "Independent confusion tables and selected error IDs match", "Changing test labels to improve the model is detected", "Day 24 dataset", SK_METRICS, "20m concept + 85m baseline comparison + 15m evidence"),
    ("Trace the supplied AI request path", "Read-only trace: UI event → supplied Spring boundary → Python model service", "Inspect provided code and record exact files, inputs and outputs; spend remaining time on a small model slice/error review", "Trace answers match source behavior and model metric counts match fixture", "Claiming learner-authored app code or a live model call is rejected", "Day 24–25; supplied infrastructure only", ("Repository source: supplied AI route and Python fixture service", "../../../frontend/src/ai/AiFirstCoverStoryApp.tsx"), "20m concept + 55m model exercise + 30m trace + 15m evidence"),
    ("Diagnose a model-backed failure", "Separate model prediction errors from HTTP, serialization and UI transport failures", "Trace source-defined blank-question and unavailable-service paths; then repair one model data/metric error", "Record exact HTTP status, error code and safe UI message, separately from model error IDs", "Editing React/Spring/auth/controller code is out of scope; guessing without source evidence fails", "Day 26 trace", ("Repository source: supplied Spring AI controller and Python fixture service", "../../../src/main/java/assignments/local/LocalAiAssistantController.java"), "20m concept + 55m model exercise + 30m trace + 15m evidence"),
    ("Explain model output limits", "A document-type label is not an answer, eligibility decision, or calibrated confidence", "Read a supplied prediction report, identify unsupported interpretation, and test a fixed counterexample", "Answer distinguishes class score, label, and downstream decision with exact fixture evidence", "Treating a toy score as customer truth or changing UI/backend code fails", "Day 27 diagnosis", ("Repository source: supplied model fixture and application boundary", "../../../ai/coverstory_assistant"), "20m concept + 55m model experiment + 30m comprehension + 15m evidence"),
    ("A reproducible model artifact", "Persist only a small model/config/feature description with a stable synthetic dataset reference", "Implement model serialization for the toy linear model, then reload and score fixed rows", "Independent expected predictions and metadata match before/after reload", "Missing feature order, changed labels, or accepting nonfinite weights is rejected", "Days 1–28 model/data basics", NUMPY, "20m concept + 85m AI implementation + 15m evidence"),
    ("Day 30 evaluated model slice", "A compact model slice trains, saves, reloads and evaluates one synthetic document-type classifier", "Run fixed training/evaluation examples with the model artifact; summarize held-out errors and limitations", "Learner score function matches independent expected scores and preserves declared feature order", "A model artifact without held-out labels or a claim of customer/production value is rejected", "Days 24–29; synthetic dataset and saved linear model", ("Repository source: model-building learner contract", "../../../ai/model_building/learner.py"), "20m concept + 85m model build/evaluation + 15m evidence"),
]


def create_manifest() -> dict:
    detailed = []
    for i, item in enumerate(DAYS, 1):
        title, concept, task, acceptance, negative, prereq, source, budget = item
        day_date = START + dt.timedelta(days=i - 1)
        evidence_category = (
            "read_only_application_comprehension_plus_model_experiment" if i in (26, 27, 28) else
            "model_artifact_and_heldout_evaluation" if i in (29, 30) else
            "synthetic_document_model_and_evaluation" if i in (24, 25) else
            "model_training_and_gradient_work" if i <= 6 else
            "dataset_hygiene_and_independent_evaluation" if i <= 14 else
            "numerical_array_and_ml_experiment"
        )
        detailed.append({
            "day": i, "unit_id": f"MODEL-{i:03d}", "date": day_date.isoformat(),
            "title": title, "concept": concept, "learner_scope": task,
            "prerequisite": prereq, "source_title_or_path": source[0], "source": source[1],
            "acceptance_command": ("python3 -m unittest ai.model_building.learner_acceptance.test_day_01" if i == 1 else
                                   f"python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.{TEST_METHODS[i]}"),
            "acceptance": acceptance, "negative_control": negative,
            "evidence": "Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.",
            "evidence_category": evidence_category,
            "budget": budget, "status": "not_started", "learner_completion": False,
            **({"comprehension_questions": COMPREHENSION[i]["questions"],
                "expected_observations": COMPREHENSION[i]["expected"]} if i in COMPREHENSION else {}),
        })
    total_engineering = 171 * 2 + (548 - 171) * 2.75
    manifest = {
        "schema_version": 1,
        "title": "Model-building-first AI roadmap",
        "status": "local_review_draft",
        "start_date": START.isoformat(), "timezone": "Asia/Kolkata",
        "horizon_days": 548, "end_date": (START + dt.timedelta(days=547)).isoformat(),
        "calendar_note": "Day numbers are study-day positions; dates are planning dates, never evidence or deadlines.",
        "learner_completion_count": 0,
        "budget": {
            "days_1_to_171": {"engineering_minutes": 120, "dsa_separate": True},
            "days_172_to_548": {"engineering_minutes": 165, "shared_minutes": 45, "dsa_separate": True},
            "engineering_hours": total_engineering,
            "shared_hours": (548 - 171) * 0.75,
            "reserve_fraction_inside_engineering": 0.2,
            "no_extra_oa_or_certification_hours": True,
        },
        "feasibility": {
            "initial_model_to_product_progression_estimate": "25–39 active study weeks (175–273 daily study units if studying every day); estimate is conditional and includes foundations, training, evaluation, integration and evidence, not just coding.",
            "nine_month_engineering_hours_by_day_274": 625.25,
            "nine_month_target": "One strong end-to-end CoverStory flagship plus model-training experiments, conditional on held-out evidence; StreamForge and Developer Workflow Assistant remain later work.",
            "three_flagship_horizon": "12–18 months is a planning range; this calendar uses 548 days (18 months) with recovery time inside the fixed budget.",
            "not_promised": ["absolute expertise", "job or title", "professional ownership", "certification pass", "customer adoption", "production operation"],
        },
        "phases": [
            {"id":"foundations","days":[1,60],"focus":"Python, NumPy, probability/linear algebra fundamentals, gradient intuition, data hygiene and first baselines","exit":"Can reproduce a tiny model update, explain loss/gradient, preserve held-out isolation, compare simple baselines with independent metrics."},
            {"id":"classical-ml","days":[61,150],"focus":"Classical ML, evaluation slices, leakage prevention, uncertainty, feature design and reusable evaluation harness","exit":"Multiple fixed-label experiments with reproducible train/validation/test splits, baseline comparisons and counterfactual/error slices."},
            {"id":"neural-models","days":[151,230],"focus":"PyTorch tensors/autograd/training loops/optimization; small CPU neural models; tiny educational transformer","exit":"Reproducible small-model training, ablations, held-out metrics, runtime/memory limits and honest limitations; not frontier pretraining."},
            {"id":"language-model-adaptation","days":[231,300],"focus":"Tokenization, pretrained model use, fine-tuning readiness and license/compute/data checks; CoverStory integration starts earlier only after model baseline/held-out gates","exit":"A justified small-model adaptation or explicit no-go based on baseline, license, compute and held-out evaluation; integrated CoverStory has source-grounded tests."},
            {"id":"coverstory","days":[301,365],"focus":"Harden the CoverStory integration that starts after model readiness around Day 211; complete retrieval, citations, permission boundaries, abstention, evaluations and supplied app infrastructure","exit":"Reproducible offline evaluation, error/security slices, model/data card, and live-provider evidence only if separately authorized and genuinely observed."},
            {"id":"streamforge","days":[366,450],"focus":"Train/evaluate a recommender or ranker with public/synthetic data and supplied platform plumbing","exit":"Held-out ranking comparison against simple baselines; cold-start/fairness/failure analysis; no fabricated usage claims."},
            {"id":"developer-workflow","days":[451,520],"focus":"Train/evaluate a bounded classification/ranking capability in the Developer Workflow Assistant with read-only-first tool use","exit":"Task outcomes, injection/permission tests, failure recovery and measured comparison; writes remain approval-gated."},
            {"id":"integration-closeout","days":[521,548],"focus":"Reuse evaluation/observability across all three projects, recovery, portfolio and readiness review","exit":"Reproducible artifact inventory and limitations; no automatic readiness, certification, role or adoption claim."},
        ],
        "application_comprehension_and_scalability_lane": {
            "budget_policy": "Within existing engineering/shared allocations; supplied app and bounded local load harnesses only. No external load generation or added hours.",
            "learner_work": "Read/trace supplied FE/BE/HLD/LLD code, diagnose seeded failures, answer question-bank prompts, and interpret measured results. No standalone UI/API/auth/CRUD/DevOps implementation or capstone design essay.",
            "topics_by_project_need": [
                {"stage":"Python/model foundations and Day 30 demo","topics":"request/data-flow trace, array/model memory and local batch inference; declared local concurrency and p50/p95/error measurement."},
                {"stage":"CoverStory days 151–365","topics":"retrieval/query indexes; permission and API contract walkthrough; FE rendering/state/network/caching/streaming/large lists; bounded batch inference; latency, memory, throughput, quality and cost trade-offs."},
                {"stage":"StreamForge days 366–450","topics":"ranking throughput, caching with Redis only if repeated reads justify it (TTL, invalidation, rate limits, atomicity/failure); Kafka only if asynchronous event flow is justified (partitions, consumer groups, order, replay, lag, delivery guarantees/idempotency). Compare simpler alternatives first."},
                {"stage":"Developer Workflow days 451–520","topics":"tool/API boundaries, queues and durable recovery; idempotency, backpressure, retries/timeouts, authorization, safe approval and replay; no write-capable live connector without separate authorization."},
                {"stage":"Platform integration days 301–548 as prerequisites permit","topics":"assistant-supplied container/CI/CD/IaC walkthroughs, secrets boundary, rollout/rollback, monitoring and incident scenarios; Kubernetes only after a simpler deployment is understood and justified. MLOps: dataset/model versioning, reproducible training, experiment tracking/registry, evaluation gates, deployment/readiness, drift and retraining."},
            ],
            "measurement_contract": "Any local load result states hardware/runtime, dataset, concurrency, duration, p50/p95, error rate and resource bounds; no test is described as global or production scale.",
        },
        "project_gates": [
            {"id":"coverstory","target_days":[211,365],"gate":"Enter only after model baseline and separate held-out evaluation; document ingestion/provenance; retrieval/citation/abstention checks; tenant and data-governance tests; repeatable demo. Day 274 review targets one coherent end-to-end vertical slice plus measured model experiments, conditional on evidence. Live provider and production gates remain unmet without authorized real evidence."},
            {"id":"streamforge","target_days":[366,450],"gate":"Ranking/recommendation model compared to trivial baseline on held-out public/synthetic examples; cold-start and relevant fairness/error slices; reproducible results."},
            {"id":"developer-workflow-assistant","target_days":[451,520],"entry":"StreamForge evidence gate passed, or bounded recovery plan recorded; preserve work on both project threads within capacity.","gate":"Measured task-success evaluation, read-only default, explicit approval for any write path, injection/authorization/recovery controls."},
            {"id":"shared-evaluation","target_days":[521,548],"entry":"Prior project results are available; unresolved gates remain explicitly open and receive bounded recovery.","gate":"Same reusable evaluation schema applied to three distinct model capabilities with independent expected outputs and preserved limitations."},
        ],
        "milestone_reviews": [
            {"study_day":150,"target":"ML/model foundations evidence review","gate":"Python/math/classical ML, clean held-out split, independently checked baselines; no title or job-readiness inference."},
            {"study_day":274,"target":"AI Product / Applied AI SWE portfolio checkpoint","gate":"One strong CoverStory vertical slice plus model experiments only if actual held-out and integration evidence qualifies; applications may begin earlier."},
            {"study_day":365,"target":"CoverStory evidence review","gate":"Grounding, retrieval/citations, permissions, abstention, evaluation and honest live-provider status; no automatic completion."},
            {"study_day":450,"target":"StreamForge model/product evidence review","gate":"Held-out ranking against baseline, cold-start/failure slices and reproducible metrics; no fabricated adoption."},
            {"study_day":520,"target":"Developer Workflow Assistant and later FDE evidence checkpoint","gate":"Measured tool-task outcomes and safe permissions; FDE evidence additionally requires real external discovery/stakeholder delivery/adoption."},
            {"study_day":548,"target":"Three-project portfolio review","gate":"Review each project and open external/production gates; no role, title, certification, mastery or professional ownership claim."},
            {"study_day":None,"date":"2029-10-26","target":"Professional Staff-responsibility review","gate":"Requires externally verified sustained ownership, technical influence, adoption, cross-team impact and review; distinct from the learning calendar."},
        ],
        "course_and_credential_targets": [
            {"title":"Machine Learning Specialization","provider":"Stanford Online and DeepLearning.AI","url":"https://www.deeplearning.ai/specializations/machine-learning","status":"study target; enrollment and paid certificate access are not authorized","fit":"Beginner; basic coding and high-school algebra. Provider graded assessment is required for provider certificate."},
            {"title":"Deep Learning Specialization","provider":"DeepLearning.AI","url":"https://www.deeplearning.ai/specializations/deep-learning","status":"later optional study target; access and certificate are not assumed","fit":"Intermediate Python and basic linear algebra/ML recommended. Official labs use Python/NumPy and TensorFlow; PyTorch project exercises are supplementary and do not replace graded course requirements."},
            {"title":"Learn the Basics","provider":"PyTorch","url":"https://docs.pytorch.org/tutorials/beginner/basics/intro.html","status":"official free practice reference","fit":"Tensors, Datasets & DataLoaders, Build the Neural Network, Automatic Differentiation, Optimizing Model Parameters, Save and Load the Model."},
            {"title":"Fundamentals of Deep Learning","provider":"NVIDIA Deep Learning Institute open-source course repository","url":"https://github.com/NVDLI/fundamentals-of-deep-learning","status":"optional open-source notebook practice; completing it does not award NVIDIA certification","fit":"Prerequisites: Python 3.10+, PyTorch 2.5+; CPU-only works but later notebooks may be slow. Modules include An Introduction to Deep Learning, How a Neural Network Trains, Convolutional Neural Networks, Data Augmentation and Deployment, Pre-Trained Models, and Advanced Architectures."},
            {"title":"Machine Learning Engineer – Associate (MLA-C02)","provider":"AWS","url":"https://aws.amazon.com/certification/certified-machine-learning-engineer-associate/","status":"later readiness-based target after actual AWS ML deployment experience; as of 27 October 2026, MLA-C02 is the updated beta; recheck general availability/version near readiness; no exam booking or pass claim","fit":"Certification preparation stays inside existing study time. Existing Maarek material remains optional cloud preparation, not model-building evidence."},
            {"title":"LLM Course","provider":"Hugging Face","url":"https://huggingface.co/learn/llm-course/chapter1/1","status":"later optional free course after Python and introductory deep learning; no credential claim","fit":"Chapter 1 Transformer models; Chapter 3 Fine-tuning a pretrained model. The course says foundational deep-learning knowledge is helpful before starting."},
        ],
        "sources_last_verified": "2026-10-27",
        "active_role_benchmarks": "docs/ai-first-roadmap/role-alignment.md",
        "superseded_candidate": "docs/ai-first-roadmap/README.md",
        "legacy_issue_migration": "docs/model-first-roadmap/legacy-issue-migration.json",
        "dsa_source": {"repository":"https://github.com/advait-ravi-marathe/dsa-prep","commit":"c2caee396002bb48b2a34d407545ecd96ecdb1ab","policy":"pointer only; DSA source and actual attempts remain unchanged"},
        "detailed_days": detailed,
    }
    for phase in manifest["phases"]:
        first, last = phase["days"]
        phase["dates"] = [(START + dt.timedelta(days=first - 1)).isoformat(),
                          (START + dt.timedelta(days=last - 1)).isoformat()]
    for gate in manifest["project_gates"]:
        first, last = gate["target_days"]
        gate["target_dates"] = [(START + dt.timedelta(days=first - 1)).isoformat(),
                                (START + dt.timedelta(days=last - 1)).isoformat()]
    for checkpoint in manifest["milestone_reviews"]:
        if checkpoint["study_day"] is not None:
            checkpoint["date"] = (START + dt.timedelta(days=checkpoint["study_day"] - 1)).isoformat()
    return manifest


def render_contract(day: dict) -> str:
    source_title = day["source_title_or_path"]
    source = day["source"]
    comprehension = ""
    if "comprehension_questions" in day:
        questions = "\n".join(f"- {item}" for item in day["comprehension_questions"])
        observations = "\n".join(f"- {item}" for item in day["expected_observations"])
        comprehension = f"\n## Read-only application comprehension\n\nAnswer from source; do not edit FE/BE files.\n\n**Questions**\n{questions}\n\n**Independent observation key**\n{observations}\n"
    return f'''# Day {day['day']}: {day['title']}

**Date:** {day['date']} (Asia/Kolkata)
**Engineering budget:** {120 if day['day'] <= 171 else 165} minutes. DSA is separate and unchanged.
**Prerequisite:** {day['prerequisite']}

## Learn

{day['concept']}

**Source:** [{source_title}]({source})

## Learner-owned AI/model work

{day['learner_scope']}

**Acceptance command:** `{day['acceptance_command']}`
**Acceptance:** {day['acceptance']}
**Negative control:** {day['negative_control']}
{comprehension}

**Evidence category:** `{day['evidence_category']}`

## Evidence

{day['evidence']}

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
'''


def write(check: bool = False) -> int:
    manifest = create_manifest()
    expected = {"manifest.json": json.dumps(manifest, indent=2, ensure_ascii=False) + "\n"}
    for day in manifest["detailed_days"]:
        expected[f"contracts/day-{day['day']:02d}.md"] = render_contract(day)
    errors = []
    for rel, text in expected.items():
        path = OUT / rel
        if check:
            if not path.exists() or path.read_text() != text:
                errors.append(rel)
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(text)
    if errors:
        print("stale generated files: " + ", ".join(errors))
        return 1
    print(f"{'verified' if check else 'generated'} {len(expected)} model-first files")
    return 0


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    raise SystemExit(write(parser.parse_args().check))
