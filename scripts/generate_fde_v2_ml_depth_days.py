"""Regenerate the 112 conditional daily cards from their canonical schedule."""
from __future__ import annotations
import json
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
      ("Dataset and DataLoader","Build deterministic train/validation partitions and a local Dataset/DataLoader.","PyTorch Learn the Basics: Datasets & DataLoaders"),
      ("Autograd scalar example","Differentiate a scalar function and compare the gradient to an analytic value.","PyTorch Learn the Basics: Automatic Differentiation with torch.autograd"),
      ("Finite difference check","Compare autograd and numerical gradient on a scalar and one network parameter.","PyTorch Learn the Basics: Automatic Differentiation with torch.autograd"),
      ("Small MLP forward pass","Create a two-layer MLP and verify logits have one row per example.","PyTorch Learn the Basics: Building the Neural Network"),
      ("Cross-entropy shape/semantics","Use logits and integer labels correctly; detect a shape/broadcasting mutation.","PyTorch Learn the Basics: Building the Neural Network"),
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
      ("No fake online result","Produce a design-only report with live experiment status BLOCKED/NOT_RUN.","OpenAI Growth role brief: evaluation/experimentation"),
      ("Feature schema parity","Train and serve with the same named feature schema; reject reordered/missing fields.","MLflow model registry: lineage concepts"),
      ("Lineage hash contract","Bind data, split, code, features, config, metrics and artifact hashes.","MLflow model registry: model version and lineage"),
      ("Versioned local artifact","Write a content-hash-bound model version and load only that verified artifact.","MLflow model registry: versions and aliases"),
      ("Artifact consumed in serving","Serve predictions from the promoted artifact and compare to train-time transform output.","MLflow model registry: model lifecycle"),
      ("Latency/cost protocol","Measure local latency; mark unknown costs rather than estimating unobserved provider spend.","Stripe ML Engineer, Growth Platform: reliability/latency/cost"),
      ("Input quality/freshness checks","Track missing fields, schema violations and feature age on local test traffic.","Stripe ML Engineer, Growth Platform: reliable feature pipelines"),
      ("Prediction/label drift","Measure drift and quality separately; delayed labels are pending, not negatives.","MLflow model monitoring concepts"),
      ("Drift versus degradation","Prove drift alone pages for review but does not automatically trigger retraining.","Stripe ML Engineer, Growth Platform: monitoring and iteration"),
      ("Evidence-based retrain rule","Require both quality degradation and minimum support/recency before recommending retrain.","MLflow model registry: lifecycle"),
      ("Canary and rollback design","Simulate a bounded staged promotion and automatic rollback guard without a live service.","MLflow model registry: version and rollback concepts"),
      ("Promotion history","Preserve immutable active/previous versions and test rollback to a known prior artifact.","MLflow model registry: version lineage"),
      ("Delayed labels and backfill","Retain arrival time and event time; test backfill idempotency on synthetic records.","Stripe ML Engineer, Growth Platform: data/feature pipelines"),
      ("Incident drill","Corrupt a feature schema, detect, rollback, restore health, and add a regression test.","Stripe ML Engineer, Growth Platform: operating/debugging ML systems"),
      ("Privacy and data lineage","Document purpose, source, retention, deletion and least-privilege boundary for fixture data.","Stripe ML Engineer, Growth Platform: data and feature quality"),
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
 "MLflow":"https://mlflow.org/docs/latest/ml/model-registry/"}


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
            days.append({"day":day,"block":block['id'],"block_name":block['name'],"block_day":index,
                "day_kind":"focused","title":title,"task":task,"reading":reading,
                "worked_example":f"python3 -m ai.fde_v2.ml_depth.examples --block {block['id']}",
                "acceptance_command":f"python3 -m unittest ai.fde_v2.ml_depth.test_ml_depth.{selector}",
                "dsa_minutes":60,"role_bank_minutes":30,"ai_minutes":90,"substantive_minutes":90,"buffer_minutes":0,
                "minutes":{"reading":15,"worked_example":10,"learner_build_test":55,"evidence_log":10},
                "status":"not_started"})
        for offset in range(4):
            day=block['buffer_start']+offset
            days.append({"day":day,"block":block['id'],"block_name":block['name'],"block_day":25+offset,
                "day_kind":"buffer","title":f"{block['id']} repair/retest buffer {offset+1}",
                "task":"Use this scheduled block only for measured repair/retest from the preceding focused sessions; run the named failing selector, record before/after artifacts, and preserve remaining failures. If no repair is needed, leave the block unused and record why.",
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
            acceptance=f"`{row['acceptance_command']}`. This is the stage regression suite; it is not proof that this particular day or learner task passed."
        else:
            ai=f"**AI90 buffer:** {row['task']} Worked-example prompt: {row['worked_example']}"
            acceptance=f"`{row['acceptance_command']}`; rerun the exact failed selector as well."
        page=f"""# Conditional learning Day {row['day']}: {row['block']} — {row['title']}

Status: Not started. This page is in the versioned production-ML extension; page existence is not evidence of an attempt.

## Budget

- DSA:60min in the separate repository.
- Interview/AWS role bank:30min following the versioned [14-day rotation](../role-bank-rotation.json) (three AWS slots per14 days after the eight-session baseline).
- AI:90min. Keep DSA60+role30+AI90=180min/day. If a scheduled DSA revision takes75/90min, shift the ordered AI sequence forward at AI75/60; do not compress this task into unpaid hours.

## Today's work

{ai}

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
