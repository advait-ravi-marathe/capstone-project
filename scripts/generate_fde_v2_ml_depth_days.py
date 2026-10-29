"""Regenerate the 112 conditional daily cards from their canonical schedule."""
from __future__ import annotations
import json
import re
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
PACKAGE=ROOT/'ai/fde_v2/ml_depth'
PAGES=ROOT/'docs/fde-v2/days'
INDEX=ROOT/'docs/fde-v2/ml-depth/README.md'

BLOCKS=[
    {"id":"PML1","name":"Statistics and classical supervised ML","start":271,"buffer_start":295,
     "topics":[
      ("Problem, unit, estimand","Write a one-page task and label contract for a synthetic conversion prediction problem.","OpenIntro Ch.1 Introduction to data"),
      ("Sampling, selection and missingness","Create a table that distinguishes target population, observed sample and missing outcomes.","OpenIntro Ch.1 Introduction to data; Ch.2 Data summaries"),
      ("Base rates and Bernoulli outcomes","Compute base rate and compare accuracy to a majority-class baseline.","OpenIntro Ch.3 Probability"),
      ("Conditional probability and Bayes","Calculate conditional event probabilities and identify a base-rate reversal.","OpenIntro Ch.3 Probability"),
      ("Sampling variation simulation","Resample the generated fixture and plot/record variability of its positive rate.","OpenIntro Ch.4 Distributions of random variables"),
      ("Intervals and bootstrap","Calculate a Wilson interval and a seeded bootstrap interval; state what each interval estimates.","OpenIntro Ch.5 Foundations for inference"),
      ("Hypotheses and decision errors","Build a type-I/type-II error table for a product metric decision.","OpenIntro Ch.5 Foundations for inference"),
      ("Power, MDE and sample size","Use the reference sample-size formula and inspect how MDE/power change required N.","Microsoft Research experiment design/validity sections"),
      ("Randomization unit and interference","Choose a user/account/session unit and construct one interference counterexample.","Microsoft Research experiment design/validity sections"),
      ("Delayed labels and censoring","Mark which synthetic events are not yet observable at the chosen evaluation cutoff.","OpenIntro Ch.5 Foundations for inference"),
      ("Linear regression baseline","Implement squared-error predictions with the supplied feature matrix and compare to the mean baseline.","OpenIntro Ch.8 Introduction to linear regression; CS229 linear regression section"),
      ("Loss functions and robust error","Compare squared, absolute and Huber error on a fixture with one large outlier.","CS229 optimization/loss discussion"),
      ("Logistic model and log loss","Implement learner logistic gradient descent and compare probabilities to majority baseline.","CS229 logistic regression section; sklearn linear models"),
      ("Finite-difference gradient check","Numerically check one learner logistic gradient coordinate against the analytic derivative.","CS229 gradient descent section"),
      ("Gradient descent and convergence","Plot or record objective across learning rates; explain divergence/slow convergence.","CS229 gradient descent section"),
      ("L1/L2 regularization","Compare coefficient norms/path under no penalty, L1 and L2 without touching validation.","CS229 regularization section; sklearn linear models"),
      ("Imbalance and threshold costs","Choose a threshold from explicit false-positive/false-negative costs; report precision/recall.","sklearn model evaluation: classification metrics"),
      ("Calibration and reliability","Compute Brier/ECE and make a reliability table; do not equate confidence with correctness.","sklearn Probability calibration"),
      ("Temporal/group leakage map","Tag each feature by event time and split by group/time before fitting transforms.","sklearn Cross-validation: evaluating estimator performance"),
      ("Boosted-tree baseline","Train the pinned HistGradientBoostingClassifier on train rows only; report validation metrics.","sklearn HistGradientBoostingClassifier API"),
      ("Validation and search budget","Compare candidates while keeping tuning within training/validation; freeze test boundary.","sklearn Tuning the hyper-parameters of an estimator"),
      ("Slice errors and support","Report errors by two predeclared synthetic slices with sample counts and uncertainty.","sklearn Metrics and scoring"),
      ("Paired comparison and uncertainty","Compare two models on the same rows; retain all cases and report uncertainty/limits.","sklearn Metrics and scoring"),
      ("Frozen comparison and model card","Freeze data/config/model hashes, run the held-out once, and document a failure plus limit.","sklearn Model selection and evaluation") ]},
    {"id":"PML2","name":"PyTorch training and debugging","start":299,"buffer_start":323,
     "topics":[
      ("Tensor shapes and dtypes","Inspect generated batch shape/dtype/device and add assertions before forward pass.","PyTorch Learn the Basics: Tensors"),
      ("Dataset and DataLoader","Build deterministic, disjoint train/validation partitions over the supplied training pool and return actual PyTorch Dataset/DataLoader objects; prove the validation loader is stable and no row crosses partitions.","PyTorch Tutorials: Datasets & DataLoaders (`Dataset` and `DataLoader` sections)"),
      ("Autograd scalar example","Evaluate the supplied scalar objective at two inputs and compare autograd values with the analytic derivative at both points.","PyTorch Learn the Basics: Automatic Differentiation with torch.autograd"),
      ("Finite difference check","Compare autograd and numerical gradient on a scalar and one network parameter.","PyTorch Learn the Basics: Automatic Differentiation with torch.autograd"),
      ("Small MLP forward pass","Create a two-layer MLP and verify logits have one row per example.","PyTorch Learn the Basics: Building the Neural Network"),
      ("Binary logit/target shape semantics","Use the learner batch validator to enforce binary float targets shaped (N,1) with logits shaped (N,1); reject a broadcastable but wrong target shape.","PyTorch Learn the Basics: Building the Neural Network"),
      ("The training loop","Implement zero_grad, forward, loss, backward and optimizer step; prove parameters change.","PyTorch Learn the Basics: Optimizing Model Parameters"),
      ("SGD versus Adam","Train the same fixed tiny task with two optimizers and compare traces, not winner claims.","PyTorch Learn the Basics: Optimizing Model Parameters"),
      ("Batch and epoch accounting","Log examples processed and per-epoch loss with correct weighting by batch size.","PyTorch Learn the Basics: Datasets & DataLoaders; Optimizing Model Parameters"),
      ("Seed and repeatability","Run twice with fixed seed and compare CPU outputs; note remaining nondeterminism.","PyTorch Learn the Basics: Tensors; Save and Load the Model"),
      ("Train/eval mode and dropout","Show that model mode changes behavior only where a stochastic layer is active.","PyTorch Learn the Basics: Building the Neural Network"),
      ("Validation without gradients","Add validation loop under eval/no_grad and verify optimizer state is unchanged.","PyTorch Learn the Basics: Optimizing Model Parameters"),
      ("Underfit/fit/overfit traces","Overfit a tiny batch intentionally and distinguish that from generalization.","PyTorch Learn the Basics: Optimizing Model Parameters"),
      ("Weight decay and early stopping","Select epoch from validation-only history and keep held-out rows inaccessible.","PyTorch Learn the Basics: Optimizing Model Parameters"),
      ("Class imbalance","Compare unweighted and weighted loss while keeping label semantics explicit.","PyTorch Learn the Basics: Optimizing Model Parameters"),
      ("Broken-run diagnosis","Inject a disconnected loss, non-finite gradient and wrong label shape; classify each error.","PyTorch Learn the Basics: Automatic Differentiation; Optimizing Model Parameters"),
      ("Checkpoint manifest","Save state_dict, optimizer state, seed, data/split/config hashes and epoch.","PyTorch Learn the Basics: Save and Load the Model"),
      ("Resume and reload parity","Resume a split training run and compare against uninterrupted fixed-batch training.","PyTorch Learn the Basics: Save and Load the Model"),
      ("CPU time and memory","Measure a bounded fixture run; record hardware, rows, epochs and wall-clock time.","PyTorch Learn the Basics: Quickstart"),
      ("Calibration and error slices","Evaluate calibrated probabilities and two supported slices after training.","sklearn Probability calibration; Metrics and scoring"),
      ("Seed sensitivity","Repeat with three declared seeds; report spread rather than selecting the best seed.","PyTorch Learn the Basics: Optimizing Model Parameters"),
      ("Mutation regression","Disable optimizer.step or corrupt labels; require the training/evaluation gate to detect it.","PyTorch Learn the Basics: Optimizing Model Parameters"),
      ("Clean-process reproduction","Load checkpoint in a fresh process from manifest; reject changed data/config hashes.","PyTorch Learn the Basics: Save and Load the Model"),
      ("Training mechanics review","Explain one failure, the repair and why a synthetic overfit is not generalization proof.","PyTorch Learn the Basics: Save and Load the Model") ]},
    {"id":"PML3","name":"Learned recommendation and ranking","start":327,"buffer_start":351,
     "topics":[
      ("Events, impressions and labels","Separate positive events from exposures and unobserved items in the synthetic fixture.","Google Recommender Systems: overview and terminology"),
      ("Existing M5.2 baselines","Reproduce popularity and cosine ranking with an identical candidate set.","M5.2 accepted guide: offline ranking evaluation"),
      ("Temporal and user split","Choose a time/group split and show why random interaction split leaks information.","Google Recommendation Systems: collaborative filtering summary"),
      ("Candidate availability","Define which candidates exist at each prediction timestamp and reject future-only items.","Google Recommendation Systems: candidate generation overview"),
      ("Training-only negative sampling","Implement the learner sampler over training-eligible items only and assert catalog membership.","Google Recommendation Systems: collaborative filtering summary"),
      ("False negatives and exposure","Record limits when an unclicked/unseen item is treated as negative.","Google Recommendation Systems: collaborative filtering summary"),
      ("Preference triples","Represent user, positive item and eligible negative item with stable IDs.","Google Recommendation Systems: collaborative filtering and matrix factorization"),
      ("Pairwise logistic/BPR objective","Derive and implement the pairwise objective; check monotonic loss on the fixture.","Google Recommendation Systems: matrix factorization"),
      ("Gradient update","Finite-difference check one pairwise parameter and preserve deterministic seed.","Google Recommendation Systems: matrix factorization"),
      ("Same candidates for all models","Bind catalog/split hash and assert learned, popularity and cosine rank the same rows.","Google Recommendation Systems: candidate generation; scoring"),
      ("Ranking metric definitions","Calculate NDCG@K, MRR and Recall@K with explicit ties and denominator.","Google Recommendation Systems: scoring"),
      ("Baseline comparison","Compare learned ranker to M5.2 cosine/popularity; no model is required to win.","Google Recommendation Systems: scoring and re-ranking"),
      ("Cold user fallback","Implement transparent popularity fallback for an unseen user and test its status.","Google Recommendation Systems: collaborative filtering summary (cold start)"),
      ("Cold item behavior","Reject future-only item leakage and document an explicit metadata/popularity fallback boundary.","Google Recommendation Systems: collaborative filtering summary (cold start)"),
      ("Sparse-user support","Report per-user event support and abstain from unsupported slice conclusions.","Google Recommendation Systems: collaborative filtering summary"),
      ("Time drift and freshness","Measure training-only item age/popularity and test stale-candidate handling.","Google Recommendation Systems: re-ranking"),
      ("Quality, coverage and exposure slices","Report ranking quality and candidate coverage by predeclared synthetic slices.","Google Recommendation Systems: re-ranking"),
      ("Negative-sample sensitivity","Repeat candidate sampling with declared seeds; report rank/metric variability.","Google Recommendation Systems: collaborative filtering summary"),
      ("Development-only tuning","Tune dimensions/learning rate on development split and record selected config hash.","Google Recommendation Systems: scoring"),
      ("Paired user uncertainty","Aggregate at user level; do not treat repeated events as independent users.","sklearn Metrics and scoring"),
      ("Freeze held-out once","Freeze candidates/model/split/metric before one held-out run and retain failures.","M5.2 accepted guide: held-out runner"),
      ("Logged feedback limits","Explain exposure, position, selection and counterfactual limitations in the report.","Google Recommendation Systems: collaborative filtering summary"),
      ("Retrieval versus ranking","Separate candidate generation from scoring and reranking in the serving sketch.","Google Recommendation Systems: overview/types"),
      ("Ranker error review","Choose based on evidence; accept baseline win/no difference and list next useful data.","Google Recommendation Systems: scoring and re-ranking") ]},
    {"id":"PML4","name":"Experiment design and model lifecycle","start":355,"buffer_start":379,
     "topics":[
      ("Hypothesis and metric contract","Write primary metric, unit, estimand, guardrails and decision before outcomes.","Microsoft Research online experiments: design and validity"),
      ("Stable unit randomization","Implement deterministic user-level allocation; verify repeated exposure stays in one arm.","Microsoft Research online experiments: randomization"),
      ("Power and fixed horizon","Calculate MDE/sample size; reject a planned cohort below the stated power target.","Microsoft Research online experiments: experiment design"),
      ("Sample-ratio mismatch","Inject missing assignments; stop outcome analysis when SRM boundary is exceeded.","Microsoft Research online experiments: validity checks"),
      ("Interference and carryover","Map spillover/novelty risks and decide whether the proposed unit is plausible.","Microsoft Research online experiments: validity"),
      ("Guardrails and safety stop","Set adverse-effect thresholds for latency, errors and opt-outs before simulated outcomes.","Microsoft Research online experiments: guardrail metrics"),
      ("Synthetic analysis only","Analyze a generated randomized assignment and preserve uncertainty; label non-live evidence.","Microsoft Research online experiments: analysis"),
      ("No fake online result","Produce a design-only report with live experiment status BLOCKED/NOT_RUN.","Microsoft Research online experiments: design, validity and decision criteria"),
      ("Feature schema parity","Train and serve with the same named feature schema; reject reordered/missing fields.","MLflow model registry: lineage concepts"),
      ("Lineage hash contract","Bind data, split, code, features, config, metrics and artifact hashes.","MLflow model registry: model version and lineage"),
      ("Versioned local artifact","Write a content-hash-bound model version and load only that verified artifact.","MLflow model registry: versions and aliases"),
      ("Artifact consumed in serving","Serve predictions from the promoted artifact and compare to train-time transform output.","MLflow model registry: model lifecycle"),
      ("Latency/cost protocol","Measure local latency; mark unknown costs rather than estimating unobserved provider spend.","CloudWatch SLO concepts: latency and availability; AWS Budgets: actual and forecast alerts"),
      ("Input quality/freshness checks","Track missing fields, schema violations and feature age on local test traffic.","Feast documentation: point-in-time joins and event-time freshness"),
      ("Prediction/label drift","Measure drift and quality separately; delayed labels are pending, not negatives.","MLflow model monitoring concepts"),
      ("Drift versus degradation","Prove drift alone pages for review but does not automatically trigger retraining.","CloudWatch alarms: metric alarms and missing data; sklearn model evaluation: classification metrics"),
      ("Evidence-based retrain rule","Require both quality degradation and minimum support/recency before recommending retrain.","MLflow model registry: lifecycle"),
      ("Canary and rollback design","Simulate a bounded staged promotion and automatic rollback guard without a live service.","MLflow model registry: version and rollback concepts"),
      ("Promotion history","Preserve immutable active/previous versions and test rollback to a known prior artifact.","MLflow model registry: version lineage"),
      ("Delayed labels and backfill","Reconcile late labels by event time while retaining arrival-time/version provenance; replay the same synthetic backfill and reject a conflicting duplicate version.","Feast documentation: point-in-time joins and late-arriving/backfilled rows"),
      ("Incident drill","Corrupt a feature schema, detect, rollback, restore health, and add a regression test.","AWS Operational Excellence: Prepare and Operate; CloudWatch alarms: missing data and alarm state"),
      ("Privacy and data lineage","Document purpose, source, retention, deletion and least-privilege boundary for fixture data.","AWS IAM best practices: least privilege; Feast documentation: point-in-time joins and event time"),
      ("End-to-end local release","Run train, validation, package, serve, monitor and rollback from one reproducible command.","MLflow model registry; PyTorch save/load; scikit-learn model selection"),
      ("Capability and gap report","Separate local synthetic mechanics from real-data, online, production and experience evidence.","OpenAI and Stripe benchmark briefs; no readiness inference") ]}
]

RESOURCE_URLS={
 "OpenIntro":"https://www.openintro.org/book/os/","CS229":"https://cs229.stanford.edu/main_notes.pdf",
 "sklearn":"https://scikit-learn.org/stable/user_guide.html","PyTorch":"https://docs.pytorch.org/tutorials/beginner/basics/",
 "Google Recommendation":"https://developers.google.com/machine-learning/recommendation/overview/types",
 "Google Recommender":"https://developers.google.com/machine-learning/recommendation/overview/types",
 "Microsoft Research":"https://www.microsoft.com/en-us/research/publication/online-controlled-experiments-at-scale-lessons-and-challenges/",
 "OpenAI Growth":"https://openai.com/careers/machine-learning-engineer-growth-san-francisco/",
 "Stripe ML Engineer":"https://stripe.com/careers/listing/machine-learning-engineer-growth-platform/8224915",
 "CloudWatch SLO concepts":"https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/CloudWatch-ServiceLevelObjectives.html",
 "AWS Budgets":"https://docs.aws.amazon.com/cost-management/latest/userguide/budgets-managing-costs.html",
 "CloudWatch alarms":"https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/CloudWatch_Alarms.html",
 "AWS Operational Excellence":"https://docs.aws.amazon.com/wellarchitected/latest/operational-excellence-pillar/welcome.html",
 "AWS IAM best practices":"https://docs.aws.amazon.com/IAM/latest/UserGuide/best-practices.html",
 "Feast":"https://docs.feast.dev/getting-started/concepts/point-in-time-joins",
 "MLflow":"https://mlflow.org/docs/latest/ml/model-registry/"}

# Explicit learner seams for implementation-focused days; every other focused
# day gets its own task-specific artifact and manual evidence rubric.
LEARNER_INTERFACES={
 281:("ai/fde_v2/ml_depth/learner.py", "train_linear_regression"),
 282:("ai/fde_v2/ml_depth/learner.py", "regression_loss_profile"),
 300:("ai/fde_v2/ml_depth/learner.py", "make_torch_loaders"),
 374:("ai/fde_v2/ml_depth/learner.py", "reconcile_delayed_labels"),
 283:("ai/fde_v2/ml_depth/learner.py", "logistic_loss and train_logistic"),
 284:("ai/fde_v2/ml_depth/learner.py", "logistic_gradient"),
 287:("ai/fde_v2/ml_depth/learner.py", "threshold_cost_analysis"),
 290:("ai/fde_v2/ml_depth/workflow.py", "compare_classical(adapter=learner); supplied boosted-tree baseline paired on same validation rows"),
 305:("ai/fde_v2/ml_depth/learner.py", "train_torch_model"),
 316:("ai/fde_v2/ml_depth/workflow.py", "resume_torch_training(adapter=learner)"),
 331:("ai/fde_v2/ml_depth/learner.py", "sample_negative_pairs"),
 334:("ai/fde_v2/ml_depth/learner.py", "train_pairwise_ranker"),
 335:("ai/fde_v2/ml_depth/learner.py", "pairwise_loss_gradient"),
 337:("ai/fde_v2/ml_depth/learner.py", "ndcg_at_k"),
 338:("ai/fde_v2/ml_depth/workflow.py", "compare_rankers(adapter=learner)"),
 339:("ai/fde_v2/ml_depth/learner.py", "cold_start_fallback"),
 355:("ai/fde_v2/ml_depth/learner.py", "design_experiment"),
 356:("ai/fde_v2/ml_depth/learner.py", "assign_experiment_units"),
 357:("ai/fde_v2/ml_depth/learner.py", "design_experiment"),
 358:("ai/fde_v2/ml_depth/learner.py", "srm_check"),
 361:("ai/fde_v2/ml_depth/learner.py", "analyze_ab"),
 366:("ai/fde_v2/ml_depth/workflow.py", "serve_logistic(adapter=learner)"),
 371:("ai/fde_v2/ml_depth/learner.py", "retraining_decision"),
 299:("ai/fde_v2/ml_depth/learner.py", "validate_torch_batch"),
 301:("ai/fde_v2/ml_depth/learner.py", "autograd_scalar_gradient"),
 302:("ai/fde_v2/ml_depth/learner.py", "finite_difference_gradient"),
 303:("ai/fde_v2/ml_depth/learner.py", "build_torch_model"),
 304:("ai/fde_v2/ml_depth/learner.py", "validate_torch_batch; train_torch_model"),
 306:("ai/fde_v2/ml_depth/learner.py", "train_torch_model(optimizer_kind=...)"),
 307:("ai/fde_v2/ml_depth/learner.py", "train_torch_model(batch_size=...)"),
 308:("ai/fde_v2/ml_depth/learner.py", "train_torch_model(seed=...)"),
 309:("ai/fde_v2/ml_depth/learner.py", "dropout_mode_probe"),
 310:("ai/fde_v2/ml_depth/learner.py", "evaluate_torch_model"),
 311:("ai/fde_v2/ml_depth/learner.py", "train_torch_model"),
 312:("ai/fde_v2/ml_depth/learner.py", "select_torch_epoch"),
 313:("ai/fde_v2/ml_depth/learner.py", "train_torch_model(positive_weight=...)"),
 314:("ai/fde_v2/ml_depth/learner.py", "train_torch_model; validate_torch_batch"),
 315:("ai/fde_v2/ml_depth/workflow.py", "save_torch_checkpoint/load_torch_checkpoint"),
 317:("ai/fde_v2/ml_depth/workflow.py", "torch_training_witness; bounded profile script"),
 318:("ai/fde_v2/ml_depth/learner.py", "brier_score; expected_calibration_error"),
 319:("ai/fde_v2/ml_depth/learner.py", "train_torch_model(seed=...)"),
 320:("ai/fde_v2/ml_depth/workflow.py", "torch_training_witness"),
 321:("ai/fde_v2/ml_depth/workflow.py", "load_torch_checkpoint in a fresh Python process"),
 322:("ai/fde_v2/ml_depth/learner.py", "explain the observed training/evaluation failure and repair"),
}

# Concrete code-session contracts. These name an editable learner surface and
# a repeatable observation; the general evidence rubric below is not sufficient
# for a session whose task asks for code.
CODE_DAY_DETAILS={
 281:("learner.py::train_linear_regression", "Run `test_linear_regression_baseline`; record initial/final MSE and mean-baseline MSE. Pass only when held-out-from-training fixture predictions beat the mean baseline and every coefficient is finite."),
 282:("learner.py::regression_loss_profile", "Run `test_regression_losses_compare_squared_absolute_and_huber_on_outlier`; record all three values for the single outlier and verify Huber is below squared error while MAE remains linear in the residual."),
 283:("learner.py::train_logistic` and `logistic_loss", "Run `test_regularized_logistic_fit_and_train_only_transform`; record loss before/after and probabilities on the fixed fixture. Fail if probabilities leave [0,1], loss is non-finite, or the majority baseline is not reported."),
 284:("learner.py::logistic_gradient", "Run `test_logistic_gradient_matches_finite_difference_with_l2`; compare every coordinate to central differences at epsilon 1e-6, including the intercept exclusion rule. Maximum absolute discrepancy must be <=1e-5."),
 287:("learner.py::threshold_cost_analysis", "Run `test_declared_threshold_cost_rule`; preserve the declared FP/FN costs, enumerate candidate thresholds, and show the selected confusion counts and total cost. A threshold chosen from accuracy alone fails."),
 290:("workflow.py::compare_classical(adapter=learner)", "Run `test_candidate_boosted_comparison_uses_same_validation_rows`; record train-only transform hash, row IDs and validation metrics for logistic and HistGradientBoosting. Validation IDs must not appear in any fit call."),
 291:("learning-artifacts/production-ml/day-291-search-budget.json", "Run three declared candidate configurations on the same train/validation IDs; log every attempted configuration and score, then show the untouched held-out split was never loaded during selection."),
 293:("learning-artifacts/production-ml/day-293-paired-comparison.json", "Join candidate predictions by stable row ID; verify both models cover the exact same rows and compute paired delta/interval. Removing one hard row must make the row-set assertion fail."),
 294:("ai/fde_v2/m52/workflow.py::freeze_heldout and run_heldout_once", "Freeze canonical data/config/model/split/metric hashes before one held-out run; persist every result including errors/timeouts and reject a second run under the same consumed split ID."),
 299:("learner.py::validate_torch_batch", "Run `test_candidate_batch_validator_returns_checked_cpu_float32_tensors`; inspect actual tensors and record shapes, dtype/device. The NaN mutation and mismatched-row mutation must be rejected."),
 300:("learner.py::make_torch_loaders", "Run `test_torch_dataset_loaders_partition_the_actual_training_pool`; record train/validation row IDs, disjointness, full-union count and a second-run validation order. Both must be real DataLoaders over the exact supplied tensors."),
 301:("learner.py::autograd_scalar_gradient", "Execute the scalar autograd cell and compare the observed derivative with the hand derivative; then perturb the scalar formula and show the expected derivative changes. Record tensor, scalar loss and gradient."),
 302:("learner.py::finite_difference_gradient", "Run central finite differences for one scalar and one named network parameter; record epsilon, analytic value, numeric value and relative error. Repeat at epsilon/10 and explain round-off change."),
 303:("learner.py::build_torch_model", "Run `test_candidate_mlp_forward_has_one_finite_logit_per_row`; inspect the actual module and run it on batch sizes1 and3. Require a two-layer trainable CPU module and finite logits shaped (N,1), not a canned tensor."),
 304:("learner.py::train_torch_model", "Run `test_torch_task_runs_actual_training_adapter`; record parameter hashes before/after and independently measured initial/final BCE. Fail if optimizer.step is removed or the reported loss is fabricated."),
 305:("learner.py::train_torch_model", "Run `test_torch_task_runs_actual_training_adapter`; inspect one epoch's zero_grad→forward→loss→backward→step trace and confirm at least one parameter changes and independently recomputed objective decreases."),
 306:("learner.py::train_torch_model(optimizer_kind=...)", "Run `test_candidate_optimizer_comparison_and_minibatch_accounting`; use identical seed/data/steps for Adam and SGD, save both loss traces and update counts, and explain observed differences without declaring a universal winner."),
307:("learner.py::train_torch_model(batch_size=...)", "Run `test_candidate_optimizer_comparison_and_minibatch_accounting` and `test_training_witness_rejects_tampered_intermediate_epoch_loss`; for6 rows,batch2,epochs6 require [6]*6 examples and [3]*6 updates, then verify every full-training weighted objective against its corresponding epoch state snapshot."),
 308:("learner.py::train_torch_model(seed=...)", "Run `test_candidate_torch_training_is_repeatable_with_fixed_seed`; compare every state_dict tensor and probability exactly across two CPU runs, then record one deliberate seed change and its changed state hash."),
 309:("learner.py::dropout_mode_probe", "Run `test_candidate_dropout_probe_observes_train_variance_eval_stability_and_restores_mode`; use the supplied active Dropout module, compare five train outputs and five eval outputs, require train variance/eval equality and unchanged parameters/prior mode."),
 310:("learner.py::evaluate_torch_model", "Run `test_candidate_validation_loop_is_eval_no_grad_and_state_read_only` plus `test_validation_oracle_rejects_candidate_claiming_a_wrong_logit`; record validation row count, eval mode, no-grad logits, unchanged parameters/grads/optimizer state, and independently recomputed BCE."),
311:("learner.py::train_torch_model", "Run learner selector `test_candidate_tiny_batch_can_overfit_but_is_only_a_mechanics_claim`; record tiny-batch loss ratio and predictions, then explicitly label this as memorization mechanics rather than generalization evidence."),
 312:("learner.py::select_torch_epoch", "Run candidate early-stopping selector with losses [.8,.6,.4,.45,.5], patience2; require earliest best epoch2 and stop epoch4. Inject NaN and a later equal minimum; non-finite must fail and ties choose the earliest epoch."),
 313:("learner.py::train_torch_model(positive_weight=...)", "Run `test_candidate_weighted_loss_changes_actual_trained_parameters`; compare identical-seed weighted/unweighted models, logits and observed BCE; do not report class weighting as calibration."),
314:("learner.py::train_torch_model` and `validate_torch_batch", "Run `test_candidate_training_rejects_nonfinite_gradients_and_detached_loss`; it covers invalid labels, injected non-finite gradients and detached loss. Capture each exception and verify failure occurs before a trained result can be accepted."),
 315:("ai/fde_v2/ml_depth/workflow.py::save_torch_checkpoint", "Save a checkpoint with model/optimizer/data/split/config hashes; reopen the JSON manifest and binary artifact, compare each hash, then mutate one config field and require load rejection."),
 317:("learner.py::train_torch_model plus bounded profile receipt", "Run the supplied bounded 6-row,10-epoch CPU profile3 times; record Python/PyTorch versions, wall-clock, batch/update counts and peak memory if available. Report median/range only; no production scaling inference."),
 318:("learning-artifacts/production-ml/day-318-calibration-slices.json", "Use saved validation probabilities only; record per-slice n, positive count, Brier and calibration bins. A slice with fewer than5 rows is descriptive only; do not tune on heldout."),
 319:("learning-artifacts/production-ml/day-319-seed-sensitivity.json", "Run the same train/validation manifest with seeds11,23,47; record each model hash and metric, then summarize range and mean. Never select the best seed as the sole result."),
 321:("ai/fde_v2/ml_depth/workflow.py::load_torch_checkpoint", "Start a fresh subprocess from the documented command, load the saved artifact against its manifest, compare predictions to the originating process, then change one data hash and require rejection."),
 322:("learning-artifacts/production-ml/day-322-training-review.md", "Trace one captured failure from symptom through tensor/gradient evidence to repair; reproduce the failure and repaired run. State explicitly why tiny-fixture overfit is not generalization evidence."),
316:("workflow.py::resume_torch_training(adapter=learner)", "Run `test_candidate_checkpoint_resume_uses_saved_optimizer_and_weights`; compare resumed versus uninterrupted state_dict and predictions after equal total updates, and verify the workflow rejects a learner that ignores the exact supplied initial checkpoint."),
 317:("learning-artifacts/production-ml/day-317-bounded-profile.py", "Run the supplied 6-row,10-epoch CPU profile3 times; record Python/PyTorch versions, wall-clock, batch/update counts and peak memory if available. Report median and range only; no production scaling inference."),
 318:("workflow.py::torch_training_witness` plus learner probability metrics", "Run the calibration/slice worksheet on saved predictions; show per-slice n, positive count, Brier and calibration bins. A slice with fewer than5 rows is labeled descriptive only."),
320:("workflow.py::torch_training_witness", "Run `test_training_witness_rejects_noop_optimizer_and_corrupted_labels`; disable optimizer.step and separately corrupt labels; both must fail before an artifact is accepted."),
 331:("learner.py::sample_negative_pairs", "Run `test_training_catalog_negative_sampling`; print the training catalog hash and every positive/negative triple; reject user-seen negatives and an item added only after cutoff."),
 334:("learner.py::train_pairwise_ranker", "Run `test_pairwise_ranker_learns_and_scores_pairs`; record initial/final pairwise loss and score margin for fixed positive/negative pairs, then test a held-back user/item pair."),
 335:("learner.py::pairwise_loss_gradient", "Run `test_pairwise_vector_gradients_match_finite_difference`; compare user/positive/negative vector coordinates against central differences at epsilon1e-6; maximum absolute discrepancy <=1e-5."),
 337:("learner.py::ndcg_at_k", "Run `test_ndcg_cutoff_and_tie_behavior`; hand-calculate DCG/IDCG for tied relevance and K boundary, report zero-IDCG convention, NDCG and denominator."),
 338:("workflow.py::compare_rankers(adapter=learner)", "Run `test_learner_pairwise_ranker_is_used_by_comparison_path`; preserve identical candidate IDs for popularity/cosine/learned rankers and report per-user metric/support. No winner is required."),
 339:("learner.py::cold_start_fallback", "Run `test_cold_start_fallback_is_learner_owned_and_training_catalog_bounded`; score an unseen user from training-only popularity and show every result is a complete, duplicate-free permutation of the frozen catalog."),
 355:("learner.py::design_experiment", "Run `test_experiment_design_rejects_underpowered_fixed_horizon`; record MDE, alpha, power, computed required N, fixed horizon and guardrails. Reject horizon below power requirement."),
 356:("learner.py::assign_experiment_units", "Run `test_assignment_is_stable_balanced_and_unit_bound`; hash 500 stable user IDs and show same ID never changes arms across repeat calls; report allocation counts."),
 357:("learner.py::design_experiment` and `workflow.py::analyze_experiment", "Run `test_candidate_fixed_horizon_covers_power_minimum` and the early-analysis mutation; a sample meeting power minimum but below declared fixed horizon must remain incomplete."),
 358:("learner.py::srm_check", "Run `test_srm_boundary_blocks_outcome_analysis`; compare balanced and90/10 assignments at declared alpha, record expected/observed counts and confirm outcome analysis is blocked on SRM."),
 361:("learner.py::analyze_ab", "Run `test_ab_estimate_uses_unit_level_samples`; aggregate repeated events by randomized user first, show unit counts/means/SE, and reject empty or singleton invalid arms."),
 366:("workflow.py::serve_logistic(adapter=learner)", "Run `test_candidate_model_artifact_is_consumed_by_serving`; promote the content-bound artifact, serve known rows, compare to train-time transform and predictions, then tamper with the artifact and require rejection."),
 371:("learner.py::retraining_decision", "Run `test_retraining_requires_drift_and_quality_degradation`; contrast drift-only, degradation-only, sufficient-evidence and insufficient-support cases; only conjunction with recency/support may recommend retraining."),
 374:("learner.py::reconcile_delayed_labels", "Run `test_delayed_label_reconciliation_is_versioned_and_idempotent`; replay same late-label batch twice, require stable result, and reject conflicting duplicate event versions while preserving event/arrival times."),
}


CODE_DAY_DETAILS.update({
 285:("learner.py::train_logistic", "Run the regularized logistic candidate selector at learning rates0.01,0.1,1.0 for the same row order; record objective per epoch and coefficient norm. Mark diverging/nonfinite runs as failures, not omitted trials."),
 286:("learner.py::train_logistic(logistic_loss, l1, l2)", "Run `test_regularized_logistic_fit_and_train_only_transform` with no penalty, L1 and L2; record coefficient norms and train/validation scores while fitting only training rows. Do not select penalty on heldout."),
 288:("learner.py::brier_score; expected_calibration_error", "Run `test_loss_threshold_calibration_and_ranking_metrics_have_hand_cases`; record Brier and fixed-bin ECE on the same probabilities and labels, including empty-bin handling."),
 328:("workflow.py::compare_rankers(adapter=reference)", "Run the baseline ranking fixture with one frozen candidate set; persist each model's ordered item IDs and candidate hash. Popularity and cosine must see identical IDs and all results retain source labels."),
 330:("ai/fde_v2/ml_depth/workflow.py::compare_rankers(training_catalog=...) and timestamped catalog artifact", "For each prediction timestamp, materialize the eligible catalog from events with availability_time<=cutoff; append a future-only item and verify it is excluded from candidate IDs and catalog digest."),
 336:("workflow.py::compare_rankers", "Run the comparison and assert exact candidate-list equality for popularity, cosine and learned ranker for every user; mutate one candidate list and require comparison to fail."),
 342:("learning-artifacts/production-ml/day-342-freshness.json", "Group items by training cutoff and compute event age from training rows only; append a later event and verify it cannot change the frozen historical freshness report."),
 343:("learning-artifacts/production-ml/day-343-ranking-slices.json", "For each predeclared synthetic slice, record users, support, candidate coverage and ranking metric with denominator; reject a slice absent from the manifest or below minimum support."),
 344:("learner.py::sample_negative_pairs", "Run the negative sampler with seeds11,23,47 over the same training catalog; record negative-pair hashes and metric spread, and never select the best seed as the only result."),
 345:("learner.py::train_pairwise_ranker", "Compare exactly three predeclared dimensions/learning-rate pairs using development users only; save all configs, scores and selected-config hash. Heldout IDs must not be read by the tuning loop."),
 347:("ai/fde_v2/m52/workflow.py::freeze_heldout and run_heldout_once", "Freeze candidate, split, model and metric hashes before one heldout call; retain every user outcome and prevent a second evaluation using the consumed split ID."),
 363:("learner.py::fit_standardizer and transform_features", "Run a supplied feature row through training and serving transforms; preserve ordered feature names and values, then reorder/remove one field and verify fail-closed schema validation."),
 364:("workflow.py::_canonical_sha and model bundle manifest", "Compute lineage digest from data/split/code/features/config/metric/artifact inputs; change each input independently and verify the resulting digest changes."),
 365:("workflow.py::write_linear_bundle; promote_model", "Write two content-bound local versions and promote one; verify active pointer matches artifact digest and prior version remains immutable after the next promotion."),
 367:("learning-artifacts/production-ml/day-367-local-latency.py", "Run the same bounded local request set10 times after one warmup; record all durations, errors and sample count. Cost is `unknown` absent observed provider billing; do not extrapolate production SLO."),
 368:("learning-artifacts/production-ml/day-368-input-quality.json", "Feed valid, missing, wrong-type and stale-feature rows through supplied validation; record per-rule counts and confirm malformed rows are rejected before model scoring."),
 369:("learning-artifacts/production-ml/day-369-drift-vs-quality.json", "Compute a declared distribution-drift statistic and supervised quality only for labels observed by cutoff; mark later labels pending and keep their records out of quality denominator."),
 370:("learner.py::retraining_decision", "Run drift-only and stable-quality observations through the decision function; drift may open review but must not independently recommend retraining. Record both inputs and output fields."),
 372:("workflow.py::promote_model; rollback_model", "Simulate promotion to candidate version, inject the declared guardrail breach, then call rollback and read back the active version/digest; preserve both artifacts and the failed promotion receipt."),
 373:("workflow.py::promote_model; rollback_model", "Promote v1 then v2, rollback once, and verify active pointer returns to v1 while v2 remains on disk and history records the transition."),
 375:("ai/fde_v2/ml_depth/workflow.py::serve_logistic; rollback_model", "Change one named feature at serving, require schema rejection, rollback to prior artifact, re-run health predictions and add a regression case that replays the incompatible schema."),
 376:("learning-artifacts/production-ml/day-376-data-lineage.json", "Trace each synthetic field to source, purpose, retention and deletion owner; run the local delete fixture and verify no row remains searchable while tombstone state is preserved."),
 377:("ai/fde_v2/ml_depth/workflow.py::torch_training_witness; write_linear_bundle; promote_model; serve_logistic; rollback_model", "Run one local synthetic release command and save return code, model/data/config hashes, served prediction, monitoring summary and rollback readback; list unavailable live gates separately."),
})

LEARNER_ACCEPTANCE_BY_DAY={
 288:"test_loss_threshold_calibration_and_ranking_metrics_have_hand_cases",
 285:"test_candidate_learning_rate_comparison_records_divergence_not_dropped_trials",
 286:"test_candidate_l1_and_l2_regularization_change_learned_coefficients",
 299:"test_candidate_batch_validator_returns_checked_cpu_float32_tensors",
 300:"test_torch_dataset_loaders_partition_the_actual_training_pool",
 301:"test_candidate_autograd_and_central_difference_match_supplied_math",
 302:"test_candidate_autograd_and_central_difference_match_supplied_math",
 303:"test_candidate_mlp_forward_has_one_finite_logit_per_row",
 304:"test_torch_task_runs_actual_training_adapter",
 305:"test_torch_task_runs_actual_training_adapter",
 306:"test_candidate_optimizer_comparison_and_minibatch_accounting",
307:"test_training_witness_rejects_tampered_intermediate_epoch_loss",
 308:"test_candidate_torch_training_is_repeatable_with_fixed_seed",
 309:"test_candidate_dropout_probe_observes_train_variance_eval_stability_and_restores_mode",
 310:"test_candidate_validation_loop_is_eval_no_grad_and_state_read_only",
 311:"test_candidate_tiny_batch_can_overfit_but_is_only_a_mechanics_claim",
 312:"test_candidate_early_stopping_selects_validation_minimum_and_patience",
 313:"test_candidate_weighted_loss_changes_actual_trained_parameters",
314:"test_candidate_training_rejects_nonfinite_gradients_and_detached_loss",
320:"test_training_witness_rejects_noop_optimizer_and_corrupted_labels",
 316:"test_candidate_checkpoint_resume_uses_saved_optimizer_and_weights",
}


def _resource_link(anchor):
    for key,url in RESOURCE_URLS.items():
        if key in anchor: return f"[{anchor}]({url})"
    if anchor.startswith("M5.2") or anchor.startswith("M5.2 accepted"): return "[accepted M5.2 guide](../proposals/m52-conditional-guide.md)"
    return anchor


def build():
    days=[]
    for block in BLOCKS:
        for index,(title,task,reading) in enumerate(block['topics'],start=1):
            day=block['start']+index-1
            selector={"PML1":"StatisticsAndClassicalMLTests","PML2":"RealTorchTrainingTests",
                      "PML3":"LearnedRankingTests","PML4":"ExperimentAndLifecycleTests"}[block['id']]
            learner_acceptance={
              281:"test_linear_regression_baseline",
              282:"test_regression_losses_compare_squared_absolute_and_huber_on_outlier",
              283:"test_regularized_logistic_fit_and_train_only_transform",
              284:"test_logistic_gradient_matches_finite_difference_with_l2",
              287:"test_declared_threshold_cost_rule",
              290:"test_candidate_boosted_comparison_uses_same_validation_rows",
              300:"test_torch_dataset_loaders_partition_the_actual_training_pool",
              305:"test_torch_task_runs_actual_training_adapter",
              316:"test_candidate_checkpoint_resume_uses_saved_optimizer_and_weights",
              331:"test_training_catalog_negative_sampling",
              334:"test_pairwise_ranker_learns_and_scores_pairs",
              335:"test_pairwise_vector_gradients_match_finite_difference",
              337:"test_ndcg_cutoff_and_tie_behavior",
              338:"test_learner_pairwise_ranker_is_used_by_comparison_path",
              339:"test_cold_start_fallback_is_learner_owned_and_training_catalog_bounded",
              355:"test_experiment_design_rejects_underpowered_fixed_horizon",
              356:"test_assignment_is_stable_balanced_and_unit_bound",
              357:"test_candidate_fixed_horizon_covers_power_minimum",
              358:"test_srm_boundary_blocks_outcome_analysis",
 363:"test_regularized_logistic_fit_and_train_only_transform",
              361:"test_ab_estimate_uses_unit_level_samples",
              366:"test_candidate_model_artifact_is_consumed_by_serving",
 370:"test_retraining_requires_drift_and_quality_degradation",
              371:"test_retraining_requires_drift_and_quality_degradation",
              374:"test_delayed_label_reconciliation_is_versioned_and_idempotent",
            }.get(day)
            learner_acceptance=LEARNER_ACCEPTANCE_BY_DAY.get(day,learner_acceptance)
            if index == 1:
                prerequisite = {
                    "PML1":"M5.2 dataset/metric/split and M5.3 core gap-handoff evidence reviewed; otherwise mechanics-only preparation and progression remains blocked.",
                    "PML2":"PML1 learner-owned classical baseline, split/metric contract and reviewed error analysis passed.",
                    "PML3":"PML2 actual CPU parameter update, tiny-batch overfit, diagnosed broken run and checkpoint reload passed.",
                    "PML4":"PML3 learner-trained ranking comparison, identical candidates, cold-start and slice report with frozen holdout passed.",
                }[block['id']]
            else:
                prerequisite = f"Day {day-1} artifact and its named selector reviewed; if incomplete, use the protected block and do not advance."
            interface, functions = LEARNER_INTERFACES.get(day, ("learning-artifact", f"day {day} evidence reviewed against its specific rubric"))
            if day in CODE_DAY_DETAILS:
                target=CODE_DAY_DETAILS[day][0]
                if (target.startswith("ai/") and "::" in target) or target.startswith("learner.py::"):
                    interface,functions=target.split("::",1)
                    if interface.startswith("learner.py"):
                        interface="ai/fde_v2/ml_depth/learner.py"
                elif target.startswith("ai/"):
                    interface=target; functions="use the supplied runtime path and record its observed result"
                elif target.startswith("workflow.py::"):
                    interface="ai/fde_v2/ml_depth/workflow.py"; functions=target.split("::",1)[1]
                else:
                    interface=target; functions="edit and run the named bounded learning artifact"
            artifact = f"learning-artifacts/production-ml/day-{day}-{re.sub(r'[^a-z0-9]+','-',title.lower()).strip('-')}.json"
            objectives = [{"objective":"O4","key_result":"KR-4.2"}]
            acceptance_selector=f"manual-evidence-review/day-{day}"
            acceptance_command=f"python3 -m unittest ai.fde_v2.ml_depth.test_ml_depth.{selector}"
            if learner_acceptance:
                acceptance_selector=f"ai.fde_v2.ml_depth.learner_acceptance.MLDepthLearnerAcceptance.{learner_acceptance}"
                acceptance_command=("ML_DEPTH_CANDIDATE=ai.fde_v2.ml_depth.learner python3 -m unittest "
                                   f"ai.fde_v2.ml_depth.learner_acceptance.MLDepthLearnerAcceptance.{learner_acceptance}")
            rubric={"primary_check":CODE_DAY_DETAILS[day][1] if day in CODE_DAY_DETAILS else task,
                    "evidence_check":"Attach the requested learner-produced calculation, table, trace, code/result diff or diagram; cite fixture rows or runtime receipts where applicable.",
                    "reasoning_check":"Separate measured observations from assumptions, explain the decision, and state one material limitation.",
                    "review_type":"learner self-check with assistant feedback; not independent assessment"}
            days.append({"day":day,"block":block['id'],"block_name":block['name'],"block_day":index,
                "day_kind":"focused","title":title,"task":task,"reading":reading,
                "prerequisite":prerequisite,"learner_interface":{"path":interface,"functions":functions},
                "learner_artifact":artifact,"objective_links":objectives,"evidence_rubric":rubric,
                "acceptance_selector":acceptance_selector,
                "worked_example":f"python3 -m ai.fde_v2.ml_depth.examples --block {block['id']}",
                "acceptance_command":acceptance_command,
                "code_session_detail":CODE_DAY_DETAILS.get(day),
                "dsa_minutes":60,"role_bank_minutes":30,"ai_minutes":90,"substantive_minutes":90,"buffer_minutes":0,
                "minutes":{"reading":15,"worked_example":10,"learner_build_test":55,"evidence_log":10},
                "status":"not_started"})
        for offset in range(4):
            day=block['buffer_start']+offset
            days.append({"day":day,"block":block['id'],"block_name":block['name'],"block_day":25+offset,
                "day_kind":"buffer","title":f"{block['id']} repair/retest buffer {offset+1}",
                "task":"Use this scheduled block only for measured repair/retest from the preceding focused sessions; run the named failing selector, record before/after artifacts, and preserve remaining failures. If no repair is needed, leave the block unused and record why.",
                "prerequisite":"A focused-session failure or documented carryover from this block; otherwise leave unused.",
                "learner_interface":{"path":"learning-artifact","functions":"preserve_failure_and_retest_receipt"},
                "learner_artifact":f"learning-artifacts/production-ml/day-{day}-buffer-retest.json",
                "objective_links":[{"objective":"O4","key_result":"KR-4.2"}],
                "acceptance_selector":"named selector from the carried focused-session failure",
                "reading":"None scheduled; buffer is not new curriculum.","worked_example":"Reproduce the exact failing test or training run from its recorded manifest.",
                "acceptance_command":"python3 -m ai.fde_v2.ml_depth.acceptance --mode mechanics",
                "dsa_minutes":60,"role_bank_minutes":30,"ai_minutes":90,"substantive_minutes":0,"buffer_minutes":90,
                "minutes":{"repair_retest":90},"status":"not_started"})
    schedule={"schema":"fde-v2-production-ml-depth-schedule/v1","status":"versioned_additive_route_proposal_not_learner_progress",
        "base_core_days":[1,270],"recommended_dual_skill_route_days":[1,382],"extension_days":[271,382],
        "core_contract_sha256":"97e5aba0f3ff30c46bcb5de3c654660fa85c8510359752fcf1f7d9c2a6bb7cd0",
        "architecture_sha256":"68d99d68de68acbd4c6957f2c9deefa601442aa29cbabfc52101711134177649",
        "per_day_budget_minutes":{"dsa":60,"role_bank":30,"ai":90},"focused_sessions":96,"buffer_sessions":16,
        "focused_ai_minutes":8640,"buffer_ai_minutes":1440,"days":days}
    (PACKAGE/'draft_schedule.json').write_text(json.dumps(schedule,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
    for row in days:
        resources=', '.join(_resource_link(part.strip()) for part in row['reading'].split(';'))
        if row['day_kind']=='focused':
            ai=f"**AI90:** Read for15min: {resources}. Work the example for10min: `{row['worked_example']}` (reference mechanics only). Build/test for55min: {row['task']} Record for10min: the exact command, result, data/config hash, actual learner contribution, and any failure."
            if row['day'] in LEARNER_INTERFACES:
                module,functions=LEARNER_INTERFACES[row['day']]
                ai += f"\n\n**Learner-owned interface:** `{module}::{functions}`. The selected workflow calls this interface; the default TODO raises until the learner implements it."
            if row['acceptance_selector'].startswith('manual-evidence-review/'):
                acceptance=(f"`{row['acceptance_command']}` runs instructor mechanics only; a green reference suite does not validate this task. "
                            f"Complete the artifact and apply the manual, task-specific evidence rubric above.")
            else:
                acceptance=(f"`{row['acceptance_command']}` invokes the selected learner implementation and checks this task's behavior; "
                            "it is not an independent learner assessment.")
            rubric_text='; '.join(f"{key}: {value}" for key,value in row['evidence_rubric'].items())
            session_contract=f"**Prerequisite:** {row['prerequisite']}\n\n**Learner interface:** `{row['learner_interface']['path']}::{row['learner_interface']['functions']}`. **Artifact:** `{row['learner_artifact']}`. **Acceptance selector:** `{row['acceptance_selector']}`. **Evidence rubric:** {rubric_text}. **Objective link:** {row['objective_links'][0]['objective']} / {row['objective_links'][0]['key_result']}."
            if row.get('code_session_detail'):
                session_contract += f"\n\n**Code check:** {row['code_session_detail'][1]}"
        else:
            ai=f"**AI90 buffer:** {row['task']} Worked-example prompt: {row['worked_example']}"
            acceptance=f"`{row['acceptance_command']}`; rerun the exact failed selector as well."
            session_contract=f"**Prerequisite:** {row['prerequisite']} **Artifact:** `{row['learner_artifact']}`. Buffers do not advance a gate or add curriculum."
        page=f"""# Conditional learning Day {row['day']}: {row['block']} — {row['title']}

Status: Not started. This page is in the versioned production-ML extension; page existence is not evidence of an attempt.

## Budget

- DSA:60min in the separate repository.
- Interview/AWS role bank:30min following the versioned [14-day rotation](../role-bank-rotation.json) (three AWS slots per14 days after the eight-session baseline).
- AI:90min. Keep DSA60+role30+AI90=180min/day. If a scheduled DSA revision takes75/90min, shift the ordered AI sequence forward at AI75/60; do not compress this task into unpaid hours.

## Today's work

{ai}

## Session contract

{session_contract}

## Acceptance and evidence limits

{acceptance}

Scaffold/reference output and synthetic fixtures are mechanics only. They do not establish learner mastery, real-data quality, online impact, production reliability, employment tenure, or hiring readiness. The M1–M5 core gates remain unchanged and must be evidenced before learner progression through their dependent handoffs.

Application FE/BE/service plumbing is assistant-supplied. Learner-owned implementation here is limited to the specified ML/statistics/model-training behavior; see the [binding application-code responsibility overlay](../amendments/2026-10-29-application-code-responsibility-overlay.md). The learner does not implement API, UI, storage or deployment code.
"""
        (PAGES/f"day-{row['day']:02d}.md").write_text(page,encoding='utf-8')
    lines=["# Recommended production-ML extension: Days271–382","",
        "This index extends the unchanged 270-day v2.4 core for the selected dual AI/FDE + production-ML route. It contains 96 focused sessions and 16 protected buffer sessions. A page or green reference test does not indicate learner progress.","",
        "The daily budget remains DSA60 in the separate repository + role/AWS30 + AI90. The route is chronological: existing core through Day270, then PML1, PML2, PML3, PML4. See the [versioned amendment](../amendments/2026-10-29-production-ml-depth-amendment.md) for prerequisites, role benchmark, boundaries and sources, and the [application-code responsibility overlay](../amendments/2026-10-29-application-code-responsibility-overlay.md) for the no-FE/BE-implementation rule.","",
        "| Day | Work | Page |", "|---:|---|---|"]
    for row in days: lines.append(f"| {row['day']} | {row['block']} {row['day_kind']}: {row['title']} | [Day {row['day']}](../days/day-{row['day']}.md) |")
    INDEX.parent.mkdir(parents=True,exist_ok=True)
    INDEX.write_text('\n'.join(lines)+'\n',encoding='utf-8')
    return schedule


if __name__=='__main__':
    schedule=build()
    print(f"wrote {len(schedule['days'])} schedule rows; {sum(r['substantive_minutes'] for r in schedule['days'])} substantive minutes; {sum(r['buffer_minutes'] for r in schedule['days'])} buffer minutes")
