# Versioned amendment: production-ML depth track (2026-10-29)

Status: proposed versioned route amendment for independent review. This file does not alter or supersede any accepted source contract. The SHA-256 values below identify the source snapshot this amendment extends.

## Source snapshot and versioning

- Base: cleaned FDE v2.4 core, commit `5b8bf568bbdfadbd0470356e226743c56072be4a`.
- Accepted later-contract source: `docs/fde-v2/accepted/later-milestone-contracts.json`, SHA-256 `97e5aba0f3ff30c46bcb5de3c654660fa85c8510359752fcf1f7d9c2a6bb7cd0`.
- Accepted architecture source: `docs/fde-v2/accepted/repaired-roadmap-architecture.md`, SHA-256 `68d99d68de68acbd4c6957f2c9deefa601442aa29cbabfc52101711134177649`.
- Those accepted originals remain byte-for-byte unchanged. This amendment is a new, separately versioned production-ML extension after the existing core, not a reinterpretation of earlier learner evidence or gates.

## Why add the branch

The 270-day v2.4 base remains applied AI/FDE first: data and retrieval, safe application behavior, real model use when available, cloud gates, bounded tools, customer discovery, operations, and role evidence. The existing M5.2 ranker is a transparent cosine baseline, not model training. For the user's selected dual AI/FDE + production-ML route, this amendment appends the ML preparation sequence after the base core rather than inserting sessions into its accepted milestone calendar: M5.2 supplies the concrete ranking baseline/data/split vocabulary, and M5.3 records the core evidence/gaps needed to choose a responsible model extension. This keeps early end-to-end applied-AI/FDE work runnable and preserves the accepted core's ordering/prerequisite gates. It also means substantive production-ML training begins only after the learner reaches the core handoff; it is not represented as finished by nominal Day270. The prior 270-day FDE-only edition remains available unchanged; the recommended dual-skill route is 382 nominal learning days.

The current user-confirmed North Star for this route is Staff-level Forward Deployed Engineer readiness in applied AI by October 2029, with senior FDE / production-AI ownership and customer-facing scope as the stepping stone. This is a long-term target, not an asserted capability or guaranteed title. This versioned amendment records the updated direction while leaving earlier accepted source bytes intact. Production-ML depth strengthens the ability to diagnose, evaluate and deliver model-based customer solutions; it does not replace customer discovery, end-to-end delivery, live operations or verified customer outcomes. Broad ML Engineer, software/full-stack and Solutions Architect roles are adjacent options supported by the user's existing portfolio and role-bank practice, not competing primary roadmaps or automatic readiness claims.

The reviewed official role briefs emphasize the complete modeling loop. OpenAI's [Machine Learning Engineer, Growth](https://openai.com/careers/machine-learning-engineer-growth-san-francisco/) brief describes statistics and optimization under noisy or delayed signals, experiments, deployment, monitoring, and retraining. Stripe's [Machine Learning Engineer, Growth Platform](https://stripe.com/careers/listing/machine-learning-engineer-growth-platform/8224915) describes training and evaluating models, recommendations/ranking, feature pipelines, online experiments, and operating/debugging production ML. Both also require prior professional experience; coursework and synthetic portfolio work do not satisfy that requirement or promise hiring readiness.

## Calendar and unchanged daily budget

This is a **112-session production-ML extension included in the recommended dual-skill route**, relative Days271–382:

- Four 28-day blocks preserve the existing 90 AI minutes/day. Each block has 24 focused instructional days and four protected repair/retest days.
- Focused work: 96 days × 90 AI minutes = 8,640 minutes (144 AI hours). Protected buffers: 16 days × 90 AI minutes = 1,440 minutes (24 AI hours).
- Every day remains 60 minutes DSA in the separate repository + 30 minutes interview/AWS bank + 90 minutes AI work. Existing AWS-bank placement and DSA review clocks continue unchanged. No additional hours are assumed.
- The base FDE-only route still ends at nominal Day270. The recommended dual-skill route continues through nominal Day382. Unpassed core gates remain visible and are never waived; a particular learner's actual elapsed timeline may exceed the nominal schedule. The ML study lane does not pause the recurring DSA/role preparation blocks or imply job readiness.
- Buffers are for measured repair/retest and schedule slips, not new curriculum. If a stage exceeds the available capacity, retain the unfinished status and re-estimate.

Within each 90-minute focused AI block: 15 minutes named reading, 10 minutes worked example, 55 minutes learner model/statistics implementation and testing, 10 minutes evidence/error log. These are ML-domain tasks only. The assistant supplies frontend/backend/API/storage/serving/deployment plumbing; the learner traces and diagnoses that code without implementing it, as specified by the [application-code responsibility overlay](2026-10-29-application-code-responsibility-overlay.md). Each protected buffer day is available for measured repair/retest or remains unused; it is not silently filled with curriculum. Stage resources below list short sections and direct official references, not entire books or courses. Day cards map every focused day to one concrete task and its test target.

## Four focused blocks

| Days | Stage | Learner-owned capability | Integrated runtime witness |
|---|---|---|---|
| 271–298 | PML1: statistics + classical supervised modeling | quantify uncertainty, build linear/logistic/boosted-tree baselines, select losses, regularize, validate temporally/by group, calibrate, and analyze slices | learner logistic/linear training and sklearn boosted-tree comparison on generated data; A/B power and interval mechanics |
| 299–326 | PML2: PyTorch training/debugging | implement a small network/training loop, validate gradients, tiny-batch overfit, diagnose broken runs, checkpoint/resume, and reproduce on CPU | actual local PyTorch parameter training on generated in-memory data; no pretrained weights or downloads |
| 327–354 | PML3: learned recommendation/ranking | sample negatives from training-only eligible catalogs, train a pairwise preference model, compare fairly with popularity/cosine, and inspect cold-start and slices | learned BPR-style ranking adapter integrated with identical candidate sets and frozen evaluation |
| 355–382 | PML4: experiments + model lifecycle | design powered randomized tests, analyze guardrails and sample-ratio mismatch (SRM), apply stopping rules, version artifacts consumed by serving, check feature parity, monitor/retrain, and rollback | local train/evaluate/package/serve/monitor path plus synthetic A/B design/analysis; no live experiment |

### Route and prerequisite handoffs

The new recommended route is one sequence: **Days1–270 existing v2.4 core → Days271–298 PML1 → Days299–326 PML2 → Days327–354 PML3 → Days355–382 PML4**. Do not renumber or overwrite any of Days1–270.

| Handoff | Required evidence before progressing | Why this is the insertion point |
|---|---|---|
| M5.3 / Day270 → PML1 / Day271 | M5.2 dataset/metric/split understanding and a truthful core evidence/gap inventory; if M5.2 is not demonstrated, preserve that gate and use PML1 mechanics as scaffold preparation only | Keeps early runnable AI/FDE and all existing M1–M5 order intact, then deepens statistics/classical models using the already-built ranker data problem |
| PML1 → PML2 | learner-owned classical baseline, split/metric contract, and one reviewed error analysis | PyTorch uses the same prediction/loss/validation concepts rather than skipping directly to deep learning |
| PML2 → PML3 | real local CPU training, optimizer update, tiny-batch overfit, saved/reloaded checkpoint, and debugging note | Learned recommendation reuses training, negative sampling and model-diagnosis capability |
| PML3 → PML4 | learned ranker compared fairly with M5.2 cosine and popularity on identical candidates; cold-start and slice report; held-out remains untouched until frozen evaluation | Experiment/lifecycle work consumes a real trained artifact and its baseline evidence |
| PML4 closeout | versioned model was consumed by the local serving adapter; training/serving features match; rollback/retrain rules and a fixed-horizon experiment plan pass; live results remain unclaimed | Separates correct local engineering mechanics from actual production evidence, which needs a future authorized context |

### North-Star traceability for the added work

| Added block | Existing objective/KR contribution | Evidence that would count later | What this block does not establish |
|---|---|---|---|
| PML1 statistics/classical ML | O4 / KR-4.2: choose a defensible baseline and success measure for a customer workflow; recognize uncertainty, delayed outcomes, leakage and calibration limits. | Learner's model/loss code, frozen split, observed errors and a reasoned baseline decision tied to a real permitted workflow when available. | Customer value, real-data generalization, or a passed FDE release gate from synthetic data. |
| PML2 PyTorch | O4 / KR-4.2: diagnose what a learned model is optimizing and debug a bounded local model path for an applied-AI solution. | Actual parameter changes, tiny-batch overfit, failure diagnosis, checkpoint/resume and reproducible local artifact. | Production GPU/distributed training or employer-level shipped-model experience. |
| PML3 ranking | O4 / KR-4.2: evaluate candidate/re-ranking tradeoffs for a recommendation or retrieval workflow, including cold-start and coverage slices. | Fair paired offline comparison with the M5.2 baselines, explicit candidate provenance and held-out error report. | Online lift, customer adoption or universal model superiority. |
| PML4 experiments/lifecycle | O4 / KR-4.2: make an applied-AI solution observable, testable and recoverable; plan how a customer outcome would be measured safely. | Local artifact-serving parity, rollback/retrain drill, and predeclared experiment protocol/guardrails. | A live A/B result, cloud operations, production reliability tenure or verified customer outcome. |

These KRs are additive preparation in service of the existing applied-AI/FDE North Star. Actual progression still depends on the unchanged core prerequisites and observed evidence; the extension cannot self-complete any core objective.

## Position-level planning targets

These are conditional planning horizons, not promised promotions, title equivalences across companies, curriculum-completion claims, or guarantees of employment. Real usage, repeated delivery and cross-team adoption are evidence gates; where those opportunities are unavailable, record the gap rather than simulate the evidence.

| Horizon | Target evidence to build | Conditions and limits |
|---|---|---|
| Now–April 2027 | Independent product-engineer/interview readiness for roles that fit the verified portfolio | Demonstrate independent DSA and bounded role-specific interviews; capstone plumbing remains assistant-owned and is not learner implementation evidence. |
| May–October 2027 | Applied AI/FDE scope with ownership of one real production AI solution | Requires an actual authorized production context, real usage/operations and customer outcome evidence; synthetic/local mechanics do not satisfy it. |
| November 2027–October 2028 | Senior AI/FDE scope with customer-outcome ownership and mentorship | Requires repeated delivery and observed customer outcomes plus genuine mentoring evidence; the title is employer-specific. |
| November 2028–October 2029 | Staff Applied AI FDE scope with cross-team/customer technical leadership | Requires cross-team adoption and sustained technical leadership evidence; reaching the date or completing this curriculum alone does not establish readiness. |

The production-ML blocks support the same applied-AI/FDE objective. They do not displace customer discovery, customer-facing delivery, AWS/live-operation gates or the three-year North Star. See the concrete [14-day role-bank exercise inventory](../interview-drills/README.md) for bounded practice that remains inside the existing30-minute daily role allocation.

Prerequisites control learner progression, not code authoring. A scaffold or reference pass does not satisfy a learner gate.

## Stage teaching map

Follow each day card's exact reading subsection, worked-example input, learner TODO and acceptance selector. In every block, focus days1–24 carry curriculum; days25–28 are protected buffer days.

### Exact short-reading map

| Block | Reading anchors (read only the named section) |
|---|---|
| PML1 | [OpenIntro Statistics](https://www.openintro.org/book/os/) Ch.1 “Introduction to data”, Ch.3 “Probability”, Ch.4 “Distributions of random variables”, Ch.5 “Foundations for inference”, Ch.6–7 inference, Ch.8 “Introduction to linear regression”; [Stanford CS229 notes](https://cs229.stanford.edu/main_notes.pdf) logistic regression, gradient descent and regularization sections; [scikit-learn linear models](https://scikit-learn.org/stable/modules/linear_model.html), [HistGradientBoostingClassifier](https://scikit-learn.org/stable/modules/generated/sklearn.ensemble.HistGradientBoostingClassifier.html), [cross-validation](https://scikit-learn.org/stable/modules/cross_validation.html), [metrics](https://scikit-learn.org/stable/modules/model_evaluation.html), and [probability calibration](https://scikit-learn.org/stable/modules/calibration.html). |
| PML2 | [PyTorch Learn the Basics](https://docs.pytorch.org/tutorials/beginner/basics/): “Tensors”, “Datasets & DataLoaders”, “Building the Neural Network”, “Automatic Differentiation with torch.autograd”, “Optimizing Model Parameters”, and “Save and Load the Model”. No downloaded image/model dataset; use the generated local fixture. |
| PML3 | [Google Recommendation Systems](https://developers.google.com/machine-learning/recommendation/overview/types): “Candidate generation”, “Scoring”, “Re-ranking”; [candidate generation overview](https://developers.google.com/machine-learning/recommendation/overview/candidate-generation), [collaborative filtering summary/cold start](https://developers.google.com/machine-learning/recommendation/collaborative/summary), and M5.2's accepted cosine baseline code. |
| PML4 | [Microsoft Research, Online Controlled Experiments at Scale](https://www.microsoft.com/en-us/research/publication/online-controlled-experiments-at-scale-lessons-and-challenges/), experiment design and validity; [scikit-learn model evaluation](https://scikit-learn.org/stable/modules/model_evaluation.html), metric choice; [MLflow model registry](https://mlflow.org/docs/latest/ml/model-registry/), version lineage/rollback concepts. The implementation remains an offline local registry; reading this does not authorize installing a tracking server. |

### Daily allocation and progression

The block starts are fixed at Days271,299,327,355. Day cards 271–382 give the one-task-per-session sequence and the same-day 15+10+55+10 minute allocation. Days295–298,323–326,351–354,379–382 are buffer only. The role/AWS bank remains 30 minutes on all days; follow the versioned [14-day role-bank rotation](2026-10-29-application-code-responsibility-overlay.md#existing-30-minute-role-bank-rotation), including its recalibrated AWS allocation. DSA remains in the separate repository. A learner can remain in a buffer day until the previous acceptance suite passes.

### PML1 — statistics and classical supervised modeling (24 instructional days, plus 4 buffer days)

1–6. Problem/unit/estimand; sampling and missingness; Bernoulli/base rates; conditional probability; sampling variation; confidence interval and bootstrap.
7–12. Hypothesis errors/practical significance; power/MDE/sample size; randomization/interference; delayed outcomes/censoring; robust summaries; objective/loss selection.
13–18. Linear regression; squared/absolute/Huber losses; logistic regression/log loss; finite-difference gradient checks; gradient descent; L1/L2 regularization.
19–24. Imbalance/cost thresholds; calibration/reliability; leakage map and temporal/group split; boosted-tree benchmark; uncertainty/slice analysis; frozen model comparison and failure report.

### PML2 — actual PyTorch training (24 instructional days, plus 4 buffer days)

1–6. Tensors/shapes; deterministic Dataset/DataLoader split; autograd; finite differences; small MLP/logits; cross-entropy and label shape.
7–12. Full training loop; SGD/Adam; batch/epoch accounting; gradient norms; seeds/reproducibility; train/eval mode and dropout.
13–18. Validation loop; underfit/fit/overfit traces; weight decay/early stopping; class-weighted loss; finite gradient checks; checkpoint contents.
19–24. Resume from checkpoint; prediction equality; CPU/runtime budget; calibration/error slices; multi-seed variability; corrupted-label/disabled-optimizer mutation and clean-process reproduction.

### PML3 — learned recommendation and ranking (24 instructional days, plus 4 buffer days)

1–6. Event/impression/positive semantics; popularity and M5.2 cosine reproduction; temporal/user split; leakage examples; eligible training-only negatives; false-negative/exposure caveat.
7–12. Preference triples; pairwise logistic/BPR objective; gradient/update; deterministic seeded sampler; identical candidates; NDCG/MRR/Recall definitions.
13–18. Fair comparison against popularity/cosine; cold user/item fallback; metadata fallback without future interactions; sparse support; time drift; quality/coverage/diversity slices.
19–24. Negative-sampler sensitivity; development-only tuning; paired uncertainty at user level; frozen held-out once; logged-feedback/counterfactual limits; candidate generation vs ranking and no-win decision.

### PML4 — experimentation and model lifecycle (24 instructional days, plus 4 buffer days)

1–6. Hypothesis/primary metric/guardrails; user-level randomization; MDE/power; SRM; interference/novelty/carryover; guardrail metrics.
7–12. Synthetic randomized analysis; no-live-experiment evidence; train/serve schema parity; lineage hashes; reusable model bundle; bounded batch serving.
13–18. Local latency/cost measurement; input/freshness checks; prediction/label drift; drift vs quality; evidence-triggered retraining; shadow/canary/rollback simulation.
19–24. Immutable registry/promotion state; delayed labels/backfills; incident drill and regression; privacy/retention; end-to-end local train/evaluate/package/serve/monitor; final capability/gap report.

## Deliverables and release gates

- One synthetic/public-safe classical classification project with temporal/group-aware validation, calibrated probabilities, comparison against a boosted-tree baseline and error slices.
- One genuine local PyTorch training artifact with code/config/seed/data hashes, checkpoint reload, train/validation traces and CPU timing.
- One learned ranking extension to M5.2 with training-only negative sampling, pairwise objective, cold-start fallback, slice analysis and frozen offline comparison.
- A synthetic randomized experiment design/analysis; no real A/B outcome implied.
- Reproducible local model bundle, serving contract, monitoring summary, retraining decision and rollback drill.
- Required learner work remains TODO until actual learner attempts and receipts. Reference mechanics demonstrate runnable examples only.
- Model quality, real-data generalization, online lift, production reliability, team impact, years of experience, job eligibility, and hiring readiness remain separate and unverified. Do not imply a top-company hiring outcome.

## Dependency and evidence policy

- Reuse existing pinned CPU PyTorch in `requirements-fde-v2-sprint1.txt`; no model weights, torchvision dataset download, GPU or cloud requirement.
- Add one pinned scikit-learn dependency for local estimator mechanics; record its exact version and run the CI suite against that pin. NumPy is resolved transitively.
- Fixtures are generated locally from fixed seeds. A small named public dataset is optional, permission/licence checked, and not downloaded in default tests.
- No hidden source fallback, arbitrary network access, fabricated learner evidence, or live product experiment.

## Independent verification target

The additive code must expose learner TODOs on the real workflow path and an explicit reference adapter. Acceptance must include: probability/loss edge cases; split leakage and held-out reuse denial; optimization behavior; real PyTorch parameter updates, tiny-batch overfit, diagnosis and resume; negative-sampling catalog boundary; fair learned/popularity/cosine ranking comparison and slice failure cases; A/B randomization, power, SRM, fixed horizon and safety-stop cases; model bundle tamper/feature-parity checks; artifact consumption and rollback; drift versus quality claim separation; and evidence status that remains unpassed for synthetic tests.
