"""Reference mechanics for the selected ML extension; never learner evidence."""
from __future__ import annotations

import math
import random
from statistics import NormalDist
from typing import Any, Mapping, Sequence


def _validate_xy(features, labels):
    if not isinstance(features, (list, tuple)) or not features or len(features) != len(labels):
        raise ValueError("features and labels must be nonempty and aligned")
    width = len(features[0])
    if width == 0 or any(len(row) != width for row in features):
        raise ValueError("feature rows must have one consistent nonzero width")
    if any(not math.isfinite(float(value)) for row in features for value in row):
        raise ValueError("features must be finite")
    if any(type(y) is not int or y not in (0, 1) for y in labels):
        raise ValueError("labels must be binary integers")
    if len(set(labels)) != 2:
        raise ValueError("both binary classes must occur")


def sigmoid(value: float) -> float:
    if value >= 0:
        z = math.exp(-min(value, 709.0))
        return 1.0 / (1.0 + z)
    z = math.exp(max(value, -709.0))
    return z / (1.0 + z)


def logistic_probabilities(weights: Sequence[float], features: Sequence[Sequence[float]]) -> list[float]:
    if not weights or any(len(row) != len(weights) for row in features):
        raise ValueError("weight and feature dimensions differ")
    return [sigmoid(sum(float(w) * float(x) for w, x in zip(weights, row))) for row in features]


def predict_logistic_proba(model: Mapping[str, Any], features: Sequence[Sequence[float]]) -> list[float]:
    return logistic_probabilities(model["weights"], features)


def logistic_loss(weights, features, labels, l2: float = 0.0, l1: float = 0.0) -> float:
    _validate_xy(features, labels)
    if len(weights) != len(features[0]) or not math.isfinite(l2) or l2 < 0 or not math.isfinite(l1) or l1 < 0:
        raise ValueError("invalid weight dimensions or regularization")
    probabilities = logistic_probabilities(weights, features)
    eps = 1e-15
    data_loss = -sum(y * math.log(max(eps, p)) + (1-y) * math.log(max(eps, 1-p))
                     for p, y in zip(probabilities, labels)) / len(labels)
    penalty = l2 * sum(float(w) ** 2 for w in weights[1:]) / 2.0 + l1*sum(abs(float(w)) for w in weights[1:])
    return data_loss + penalty


def logistic_gradient(weights, features, labels, l2: float = 0.0, l1: float = 0.0):
    _validate_xy(features,labels)
    if len(weights)!=len(features[0]) or l2<0 or l1<0: raise ValueError("invalid gradient inputs")
    probabilities=logistic_probabilities(weights,features); n=len(labels)
    gradient=[sum((p-y)*row[j] for p,y,row in zip(probabilities,labels,features))/n for j in range(len(weights))]
    for j in range(1,len(weights)):
        gradient[j]+=l2*weights[j]+l1*(1.0 if weights[j]>0 else -1.0 if weights[j]<0 else 0.0)
    return gradient


def check_logistic_gradient(weights, features, labels, *, coordinate: int,
                            l2: float = 0.0, l1: float = 0.0, epsilon: float = 1e-6):
    if type(coordinate) is not int or not 0<=coordinate<len(weights) or not math.isfinite(epsilon) or epsilon<=0:
        raise ValueError("invalid gradient-check coordinate/epsilon")
    plus=list(weights); minus=list(weights); plus[coordinate]+=epsilon; minus[coordinate]-=epsilon
    numeric=(logistic_loss(plus,features,labels,l2,l1)-logistic_loss(minus,features,labels,l2,l1))/(2*epsilon)
    analytic=logistic_gradient(weights,features,labels,l2,l1)[coordinate]
    return {"analytic":analytic,"finite_difference":numeric,"absolute_error":abs(analytic-numeric),
            "coordinate":coordinate,"epsilon":epsilon}


def train_logistic(features, labels, *, learning_rate: float = 0.1,
                   epochs: int = 100, l2: float = 0.0, l1: float = 0.0) -> dict[str, Any]:
    _validate_xy(features, labels)
    if not math.isfinite(learning_rate) or learning_rate <= 0 or type(epochs) is not int or epochs < 1:
        raise ValueError("positive learning rate and epoch count required")
    if not math.isfinite(l2) or l2 < 0 or not math.isfinite(l1) or l1 < 0:
        raise ValueError("L2 penalty must be finite and nonnegative")
    width = len(features[0])
    weights = [0.0] * width
    history = [logistic_loss(weights, features, labels, l2,l1)]
    n = len(labels)
    for _ in range(epochs):
        gradients = logistic_gradient(weights,features,labels,l2,l1)
        weights = [w - learning_rate*g for w, g in zip(weights, gradients)]
        if any(not math.isfinite(w) for w in weights):
            raise FloatingPointError("non-finite logistic weights")
        history.append(logistic_loss(weights, features, labels, l2,l1))
    return {"weights": weights, "loss_history": history, "algorithm": "batch-logistic-gradient-descent",
            "regularization": {"kind": "elastic-net-subgradient", "l1_strength":l1,"l2_strength":l2}, "trained_on_rows": n}


def linear_regression_loss(weights, features, targets, l2: float = 0.0) -> float:
    if not features or len(features)!=len(targets) or len(weights)!=len(features[0]) or l2<0:
        raise ValueError("aligned regression data, weights and nonnegative L2 required")
    errors=[sum(float(w)*float(x) for w,x in zip(weights,row))-float(target) for row,target in zip(features,targets)]
    return .5*sum(error*error for error in errors)/len(errors)+l2*sum(float(w)*float(w) for w in weights[1:])/2


def linear_regression_gradient(weights, features, targets, l2: float = 0.0) -> list[float]:
    if not features or len(features)!=len(targets) or len(weights)!=len(features[0]) or l2<0:
        raise ValueError("aligned regression data, weights and nonnegative L2 required")
    errors=[sum(float(w)*float(x) for w,x in zip(weights,row))-float(target) for row,target in zip(features,targets)]
    gradient=[sum(error*row[j] for error,row in zip(errors,features))/len(errors) for j in range(len(weights))]
    for j in range(1,len(weights)): gradient[j]+=l2*weights[j]
    return gradient


def train_linear_regression(features, targets, *, learning_rate: float = 0.01,
                            epochs: int = 100, l2: float = 0.0) -> dict[str, Any]:
    if not features or len(features) != len(targets) or not features[0]:
        raise ValueError("nonempty aligned regression data required")
    if any(len(row) != len(features[0]) for row in features) or any(not math.isfinite(float(v)) for row in features for v in row):
        raise ValueError("feature matrix must be rectangular and finite")
    if any(not math.isfinite(float(y)) for y in targets) or learning_rate <= 0 or epochs < 1 or l2 < 0:
        raise ValueError("invalid targets or optimizer controls")
    weights = [0.0]*len(features[0])
    history=[]
    for _ in range(epochs+1):
        # Objective: half mean squared error + L2/2 on non-intercept weights.
        # Its derivative is mean((prediction-target)*x), matching the update below.
        history.append(linear_regression_loss(weights,features,targets,l2))
        if len(history)>epochs: break
        gradients=linear_regression_gradient(weights,features,targets,l2)
        weights=[w-learning_rate*g for w,g in zip(weights,gradients)]
    return {"weights":weights,"loss_history":history,"algorithm":"batch-linear-gradient-descent","trained_on_rows":len(targets)}


def predict_linear_regression(model, features):
    weights=model.get("weights")
    if not weights or any(len(row)!=len(weights) for row in features): raise ValueError("model/features differ")
    return [sum(float(w)*float(x) for w,x in zip(weights,row)) for row in features]


def regression_loss_profile(predictions, targets, *, huber_delta: float = 1.0):
    """Mean squared, absolute and Huber losses; Huber is quadratic near zero."""
    if not predictions or len(predictions)!=len(targets): raise ValueError("aligned nonempty predictions and targets required")
    if not math.isfinite(huber_delta) or huber_delta<=0: raise ValueError("positive finite Huber delta required")
    errors=[float(prediction)-float(target) for prediction,target in zip(predictions,targets)]
    if any(not math.isfinite(error) for error in errors): raise ValueError("finite prediction errors required")
    absolute=[abs(error) for error in errors]
    huber=[.5*error*error if error<=huber_delta else huber_delta*(error-.5*huber_delta) for error in absolute]
    return {"mse":sum(error*error for error in errors)/len(errors),
            "mae":sum(absolute)/len(absolute),"huber":sum(huber)/len(huber),
            "huber_delta":huber_delta,"rows":len(errors)}


def fit_standardizer(training_features):
    if not training_features or not training_features[0]: raise ValueError("training features are required")
    width=len(training_features[0])
    if any(len(row)!=width for row in training_features): raise ValueError("features must be rectangular")
    means=[sum(float(row[j]) for row in training_features)/len(training_features) for j in range(width)]
    scales=[]
    for j,mean in enumerate(means):
        variance=sum((float(row[j])-mean)**2 for row in training_features)/len(training_features)
        scales.append(math.sqrt(variance) or 1.0)
    return {"mean":means,"scale":scales,"fitted_rows":len(training_features),"fit_partition":"train"}


def transform_features(features, scaler):
    means,scales=scaler.get("mean"),scaler.get("scale")
    if not means or not scales or len(means)!=len(scales) or any(len(row)!=len(means) for row in features):
        raise ValueError("scaler and feature dimensions differ")
    if any(not math.isfinite(float(s)) or s<=0 for s in scales): raise ValueError("invalid feature scale")
    return [[(float(value)-float(mean))/float(scale) for value,mean,scale in zip(row,means,scales)] for row in features]


def threshold_cost_analysis(probabilities, labels, *, false_positive_cost, false_negative_cost):
    if len(probabilities)!=len(labels) or not labels or false_positive_cost<0 or false_negative_cost<0:
        raise ValueError("aligned labels and nonnegative explicit costs required")
    candidates=sorted({0.0,1.0,*[float(p) for p in probabilities]})
    best=None
    for threshold in candidates:
        fp=sum(p>=threshold and y==0 for p,y in zip(probabilities,labels))
        fn=sum(p<threshold and y==1 for p,y in zip(probabilities,labels))
        cost=fp*false_positive_cost+fn*false_negative_cost
        row={"threshold":threshold,"false_positives":fp,"false_negatives":fn,"total_cost":cost}
        if best is None or (cost,threshold)<(best["total_cost"],best["threshold"]): best=row
    return best


def brier_score(probabilities: Sequence[float], labels: Sequence[int]) -> float:
    if not probabilities or len(probabilities) != len(labels):
        raise ValueError("probabilities and labels must be nonempty and aligned")
    if any(not math.isfinite(float(p)) or not 0 <= p <= 1 for p in probabilities):
        raise ValueError("probabilities must be finite values in [0, 1]")
    if any(type(y) is not int or y not in (0, 1) for y in labels):
        raise ValueError("labels must be binary integers")
    return sum((p-y)**2 for p, y in zip(probabilities, labels)) / len(labels)


def binary_slice_report(labels: Sequence[int], predictions: Sequence[int], slices: Mapping[str,Sequence[str]], *, minimum_support: int=2):
    if len(labels)!=len(predictions) or not labels or type(minimum_support) is not int or minimum_support<1:
        raise ValueError("aligned labels/predictions and positive slice support required")
    output=[]
    for name,groups in slices.items():
        if len(groups)!=len(labels): raise ValueError("slice rows must align with labels")
        for group in sorted(set(groups)):
            indices=[i for i,value in enumerate(groups) if value==group]
            output.append({"slice":name,"value":group,"support":len(indices),
              "status":"reported" if len(indices)>=minimum_support else "insufficient_support",
              "accuracy":sum(labels[i]==predictions[i] for i in indices)/len(indices) if len(indices)>=minimum_support else None})
    return output


def expected_calibration_error(probabilities: Sequence[float], labels: Sequence[int], bins: int = 10) -> float:
    if not probabilities or len(probabilities) != len(labels) or type(bins) is not int or bins < 1:
        raise ValueError("nonempty aligned values and positive integer bins required")
    if any(not math.isfinite(float(p)) or not 0 <= p <= 1 for p in probabilities):
        raise ValueError("probabilities must be finite values in [0, 1]")
    if any(type(y) is not int or y not in (0, 1) for y in labels):
        raise ValueError("labels must be binary integers")
    count = len(labels)
    total = 0.0
    for index in range(bins):
        lower, upper = index / bins, (index + 1) / bins
        selected = [(p, y) for p, y in zip(probabilities, labels)
                    if lower <= p < upper or (index == bins-1 and p == 1.0)]
        if selected:
            confidence = sum(p for p, _ in selected) / len(selected)
            frequency = sum(y for _, y in selected) / len(selected)
            total += len(selected) / count * abs(confidence-frequency)
    return total


def ndcg_at_k(relevance: Sequence[float], k: int) -> float:
    if type(k) is not int or k < 1 or any(not math.isfinite(float(x)) or x < 0 for x in relevance):
        raise ValueError("positive cutoff and finite nonnegative relevance required")
    def dcg(values): return sum((2.0**value-1.0)/math.log2(index+2) for index,value in enumerate(values[:k]))
    ideal=sorted(relevance,reverse=True); denominator=dcg(ideal)
    return dcg(relevance)/denominator if denominator else 0.0


def wilson_interval(successes: int, total: int, confidence: float = 0.95) -> tuple[float, float]:
    if type(successes) is not int or type(total) is not int or total < 1 or not 0 <= successes <= total:
        raise ValueError("success count and sample size are invalid")
    if not 0 < confidence < 1:
        raise ValueError("confidence must be in (0, 1)")
    z = NormalDist().inv_cdf(0.5 + confidence/2)
    p = successes / total
    denominator = 1 + z*z/total
    center = (p + z*z/(2*total)) / denominator
    radius = z * math.sqrt(p*(1-p)/total + z*z/(4*total*total)) / denominator
    return max(0.0, center-radius), min(1.0, center+radius)


def required_sample_per_arm(baseline_rate: float, minimum_detectable_effect: float,
                            alpha: float = 0.05, power: float = 0.8) -> int:
    values = (baseline_rate, minimum_detectable_effect, alpha, power)
    if any(not math.isfinite(x) for x in values) or not 0 < baseline_rate < 1:
        raise ValueError("baseline must be a finite probability strictly between zero and one")
    if not 0 < minimum_detectable_effect < min(baseline_rate, 1-baseline_rate):
        raise ValueError("MDE must be positive and feasible for the baseline")
    if not 0 < alpha < 1 or not 0 < power < 1:
        raise ValueError("alpha and power must be in (0, 1)")
    normal = NormalDist()
    z_alpha = normal.inv_cdf(1-alpha/2)
    z_power = normal.inv_cdf(power)
    p2 = baseline_rate + minimum_detectable_effect
    pbar = (baseline_rate+p2)/2
    numerator = (z_alpha*math.sqrt(2*pbar*(1-pbar)) +
                 z_power*math.sqrt(baseline_rate*(1-baseline_rate)+p2*(1-p2)))**2
    return math.ceil(numerator / minimum_detectable_effect**2)


def temporal_split(rows: Sequence[Mapping[str, Any]], *, time_key: str = "timestamp",
                   train_fraction: float = 0.6, validation_fraction: float = 0.2) -> dict[str, list[dict[str, Any]]]:
    if not rows or not 0 < train_fraction < 1 or not 0 < validation_fraction < 1:
        raise ValueError("nonempty rows and valid split fractions required")
    if train_fraction+validation_fraction >= 1:
        raise ValueError("a nonempty held-out partition is required")
    copied = [dict(row) for row in rows]
    if any(time_key not in row for row in copied):
        raise ValueError("every row needs its time key")
    ordered = sorted(copied, key=lambda row: (row[time_key], row.get("event_id", "")))
    train_end = max(1, int(len(ordered)*train_fraction))
    validation_end = max(train_end+1, int(len(ordered)*(train_fraction+validation_fraction)))
    if validation_end >= len(ordered):
        raise ValueError("not enough rows for strict temporal partitions")
    result = {"train": ordered[:train_end], "validation": ordered[train_end:validation_end],
              "heldout": ordered[validation_end:]}
    if not result["validation"] or not result["heldout"]:
        raise ValueError("each partition must be nonempty")
    return result


def fit_boosted_tree(train_x, train_y, *, max_iter: int = 80, learning_rate: float = 0.08):
    """Fit a real sklearn histogram-gradient-boosting baseline (local CPU only)."""
    from sklearn.ensemble import HistGradientBoostingClassifier
    _validate_xy(train_x, train_y)
    model = HistGradientBoostingClassifier(max_iter=max_iter, learning_rate=learning_rate,
                                           max_leaf_nodes=7, l2_regularization=1.0,
                                           random_state=17)
    model.fit(train_x, train_y)
    return model


def build_torch_model(input_dim: int, *, seed: int = 7):
    import torch
    from torch import nn
    if type(input_dim) is not int or input_dim < 1: raise ValueError("positive input dimension required")
    torch.manual_seed(seed)
    return nn.Sequential(nn.Linear(input_dim, 8), nn.Tanh(), nn.Linear(8, 1))


def make_torch_loaders(features, labels, *, validation_fraction: float = .2,
                       batch_size: int = 8, seed: int = 7):
    """Create a deterministic random holdout using only the supplied training pool."""
    import torch
    from torch.utils.data import DataLoader, TensorDataset, Subset
    if not features or len(features)!=len(labels) or len(features)<4:
        raise ValueError("at least four aligned examples are required")
    if not math.isfinite(validation_fraction) or not 0<validation_fraction<.5:
        raise ValueError("validation fraction must be in (0, 0.5)")
    if type(batch_size) is not int or batch_size<1 or type(seed) is not int:
        raise ValueError("positive batch size and integer seed required")
    x=torch.tensor(features,dtype=torch.float32); y=torch.tensor(labels,dtype=torch.float32)
    dataset=TensorDataset(x,y)
    generator=torch.Generator().manual_seed(seed)
    indices=torch.randperm(len(dataset),generator=generator).tolist()
    validation_count=max(1,round(len(indices)*validation_fraction))
    validation_indices=indices[:validation_count]; train_indices=indices[validation_count:]
    train=Subset(dataset,train_indices); validation=Subset(dataset,validation_indices)
    train_loader=DataLoader(train,batch_size=batch_size,shuffle=True,generator=torch.Generator().manual_seed(seed))
    validation_loader=DataLoader(validation,batch_size=batch_size,shuffle=False)
    return {"train_loader":train_loader,"validation_loader":validation_loader,
            "train_indices":train_indices,"validation_indices":validation_indices,
            "dataset_rows":len(dataset),"seed":seed,"fit_partition":"train_only"}


def train_torch_model(features, labels, *, seed: int = 7, epochs: int = 60,
                      learning_rate: float = 0.03, initial_state=None,
                      optimizer_state=None):
    """Train a real tiny CPU PyTorch MLP; uses generated in-memory data only."""
    import torch
    from torch import nn
    _validate_xy(features, labels)
    if type(seed) is not int or type(epochs) is not int or epochs < 1 or learning_rate <= 0:
        raise ValueError("invalid torch training controls")
    torch.manual_seed(seed)
    torch.use_deterministic_algorithms(True)
    x = torch.tensor(features, dtype=torch.float32, device="cpu")
    y = torch.tensor(labels, dtype=torch.float32, device="cpu").view(-1, 1)
    model = build_torch_model(x.shape[1],seed=seed)
    if initial_state is not None: model.load_state_dict(initial_state,strict=True)
    optimizer = torch.optim.Adam(model.parameters(), lr=learning_rate, weight_decay=1e-4)
    if optimizer_state is not None: optimizer.load_state_dict(optimizer_state)
    criterion = nn.BCEWithLogitsLoss()
    history = []
    initial = {key: value.detach().clone() for key, value in model.state_dict().items()}
    model.eval()
    with torch.no_grad(): history.append(float(criterion(model(x),y)))
    for _ in range(epochs):
        model.train()
        optimizer.zero_grad(set_to_none=True)
        logits = model(x)
        loss = criterion(logits, y)
        loss.backward()
        if any(parameter.grad is not None and not torch.isfinite(parameter.grad).all()
               for parameter in model.parameters()):
            raise FloatingPointError("non-finite gradient")
        optimizer.step()
        history.append(float(loss.detach()))
    model.eval()
    with torch.no_grad():
        history[-1]=float(criterion(model(x),y))
    if all(torch.equal(initial[key], value.detach()) for key, value in model.state_dict().items()):
        raise RuntimeError("optimizer did not update model parameters")
    model.eval()
    with torch.no_grad():
        probabilities = torch.sigmoid(model(x)).flatten().tolist()
    return {"model": model, "loss_history": history, "probabilities": probabilities,
            "optimizer_state_dict": optimizer.state_dict(),
            "parameters_changed": any(not torch.equal(initial[key], value.detach()) for key,value in model.state_dict().items()),
            "device": "cpu", "training_rows": len(labels), "seed": seed,
            "pretrained_weights": False, "network_access": False}


def sample_negative_pairs(interactions, training_catalog, *, per_positive: int = 1, seed: int = 17):
    if type(per_positive) is not int or per_positive < 1 or type(seed) is not int:
        raise ValueError("positive negative count and integer seed required")
    catalog = sorted(set(training_catalog))
    if not catalog or any(not isinstance(item, str) or not item for item in catalog):
        raise ValueError("training catalog must contain item IDs")
    seen: dict[str, set[str]] = {}
    for row in interactions:
        if not row.get("user_id") or not row.get("item_id") or row["item_id"] not in catalog:
            raise ValueError("training interactions must be inside eligible catalog")
        seen.setdefault(row["user_id"], set()).add(row["item_id"])
    rng = random.Random(seed)
    result = []
    for row in interactions:
        eligible = [item for item in catalog if item not in seen[row["user_id"]]]
        if not eligible:
            raise ValueError(f"user {row['user_id']} has no eligible negative")
        for _ in range(per_positive):
            result.append((row["user_id"], row["item_id"], rng.choice(eligible)))
    return result


def _dot(left, right):
    return sum(a*b for a, b in zip(left, right))


def pairwise_loss_gradient(user_vector, positive_vector, negative_vector, *, l2: float = 1e-4):
    if (not user_vector or len(user_vector)!=len(positive_vector) or len(user_vector)!=len(negative_vector)
            or not math.isfinite(l2) or l2<0
            or any(not math.isfinite(float(value)) for vector in (user_vector,positive_vector,negative_vector) for value in vector)):
        raise ValueError("finite equal-width vectors and nonnegative L2 are required")
    margin=_dot(user_vector,positive_vector)-_dot(user_vector,negative_vector)
    slope=1.0/(1.0+math.exp(max(-60.0,min(60.0,margin))))
    loss=(math.log1p(math.exp(-max(-60.0,min(60.0,margin))))+
          l2*sum(value*value for vector in (user_vector,positive_vector,negative_vector) for value in vector)/2)
    user_gradient=[-slope*(p-n)+l2*u for u,p,n in zip(user_vector,positive_vector,negative_vector)]
    positive_gradient=[-slope*u+l2*p for u,p in zip(user_vector,positive_vector)]
    negative_gradient=[slope*u+l2*n for u,n in zip(user_vector,negative_vector)]
    return {"loss":loss,"margin":margin,"user_gradient":user_gradient,
            "positive_gradient":positive_gradient,"negative_gradient":negative_gradient}


def train_pairwise_ranker(pairs, *, dimensions: int = 8, epochs: int = 100,
                          learning_rate: float = 0.03, seed: int = 17):
    if not pairs or type(dimensions) is not int or dimensions < 1 or type(epochs) is not int or epochs < 1:
        raise ValueError("pairs, positive dimension and epochs required")
    if not math.isfinite(learning_rate) or learning_rate <= 0 or type(seed) is not int:
        raise ValueError("invalid optimizer controls")
    rng = random.Random(seed)
    users = sorted({pair[0] for pair in pairs})
    items = sorted({item for pair in pairs for item in pair[1:]})
    scale = 0.1
    user_vectors = {u: [rng.uniform(-scale, scale) for _ in range(dimensions)] for u in users}
    item_vectors = {i: [rng.uniform(-scale, scale) for _ in range(dimensions)] for i in items}
    def loss():
        return sum(pairwise_loss_gradient(user_vectors[user],item_vectors[positive],item_vectors[negative])['loss']
                   for user,positive,negative in pairs)/len(pairs)
    history = [loss()]
    for _ in range(epochs):
        for user, positive, negative in pairs:
            u = user_vectors[user]
            pos = item_vectors[positive]
            neg = item_vectors[negative]
            gradients=pairwise_loss_gradient(u,pos,neg)
            for k in range(dimensions):
                u[k]-=learning_rate*gradients['user_gradient'][k]
                pos[k]-=learning_rate*gradients['positive_gradient'][k]
                neg[k]-=learning_rate*gradients['negative_gradient'][k]
        history.append(loss())
    return {"user_vectors": user_vectors, "item_vectors": item_vectors,
            "loss_history": history, "dimensions": dimensions, "seed": seed,
            "training_pairs": len(pairs), "cold_start": "global popularity fallback required"}


def predict_pairwise_score(model: Mapping[str, Any], user_id: str, item_id: str) -> float:
    users, items = model.get("user_vectors", {}), model.get("item_vectors", {})
    if user_id not in users or item_id not in items:
        return 0.0
    return _dot(users[user_id], items[item_id])


def cold_start_fallback(candidate_items: Sequence[str], popularity_scores: Mapping[str,float]) -> list[str]:
    if (not candidate_items or len(set(candidate_items))!=len(candidate_items)
            or set(candidate_items)!=set(popularity_scores)
            or any(not math.isfinite(float(score)) for score in popularity_scores.values())):
        raise ValueError("unique candidate items and their finite training-only popularity scores are required")
    return sorted(candidate_items,key=lambda item:(-float(popularity_scores[item]),item))


def srm_check(control_count: int, treatment_count: int, expected_treatment_fraction: float = 0.5,
              chi_square_threshold: float = 3.841458820694124) -> dict[str, Any]:
    if type(control_count) is not int or type(treatment_count) is not int or min(control_count, treatment_count) < 0:
        raise ValueError("arm counts must be nonnegative integers")
    if control_count+treatment_count == 0 or not 0 < expected_treatment_fraction < 1:
        raise ValueError("nonempty assignment and valid expected fraction required")
    total = control_count+treatment_count
    expected = (total*(1-expected_treatment_fraction), total*expected_treatment_fraction)
    observed = (control_count, treatment_count)
    statistic = sum((o-e)**2/e for o, e in zip(observed, expected))
    return {"chi_square_1df": statistic, "threshold": chi_square_threshold,
            "sample_ratio_mismatch": statistic > chi_square_threshold,
            "outcome_analysis_allowed": statistic <= chi_square_threshold}


def analyze_ab(control: Sequence[float], treatment: Sequence[float]) -> dict[str, Any]:
    if len(control) < 2 or len(treatment) < 2:
        raise ValueError("at least two randomized observations per arm required")
    if any(not math.isfinite(float(x)) for x in (*control, *treatment)):
        raise ValueError("outcomes must be finite")
    means = (sum(control)/len(control), sum(treatment)/len(treatment))
    variances = (sum((x-means[0])**2 for x in control)/(len(control)-1),
                 sum((x-means[1])**2 for x in treatment)/(len(treatment)-1))
    se = math.sqrt(variances[0]/len(control)+variances[1]/len(treatment))
    z = (means[1]-means[0])/se if se else (0.0 if means[1] == means[0] else math.copysign(math.inf, means[1]-means[0]))
    return {"control_mean": means[0], "treatment_mean": means[1], "absolute_effect": means[1]-means[0],
            "standard_error": se, "z_statistic": z, "normal_approx_two_sided_p": math.erfc(abs(z)/math.sqrt(2)),
            "inference_limit": "approximate; inspect design, clustering, repeated metrics, and guardrails"}


def retraining_decision(observations: Mapping[str, Any], thresholds: Mapping[str, float]) -> dict[str, Any]:
    """Emit a retrain recommendation only when a quality signal is present."""
    required = ("drift_score", "quality_metric", "baseline_quality", "sample_count")
    if any(key not in observations for key in required):
        raise ValueError("monitoring observation is incomplete")
    if "max_drift" not in thresholds or "min_quality_delta" not in thresholds:
        raise ValueError("explicit drift and quality thresholds required")
    drift = float(observations["drift_score"])
    quality = float(observations["quality_metric"])
    baseline = float(observations["baseline_quality"])
    if type(observations["sample_count"]) is not int or observations["sample_count"] < 1:
        raise ValueError("monitoring sample count must be positive")
    drifted = drift > float(thresholds["max_drift"])
    degraded = baseline-quality > float(thresholds["min_quality_delta"])
    return {"drift_alert": drifted, "observed_quality_degradation": degraded,
            "retraining_recommended": drifted and degraded,
            "reason": "drift and quality degradation observed" if drifted and degraded
                      else "collect/review evidence; drift alone is not a quality failure"}


def reconcile_delayed_labels(records):
    """Idempotently keep the latest consistent version, preserving event and arrival times."""
    latest={}
    for source in records:
        required={"event_id","user_id","item_id","event_time","arrival_time","label","label_version"}
        if not isinstance(source,Mapping) or not required.issubset(source):
            raise ValueError("delayed-label record is incomplete")
        row=dict(source)
        if (not row["event_id"] or not row["user_id"] or not row["item_id"]
                or type(row["event_time"]) is not int or type(row["arrival_time"]) is not int
                or row["arrival_time"]<row["event_time"] or type(row["label"]) is not int
                or row["label"] not in (0,1) or type(row["label_version"]) is not int
                or row["label_version"]<1):
            raise ValueError("invalid delayed-label identity, times, label or version")
        prior=latest.get(row["event_id"])
        if prior is None or row["label_version"]>prior["label_version"]:
            latest[row["event_id"]]=row
        elif row["label_version"]==prior["label_version"] and row!=prior:
            raise ValueError("conflicting duplicate label version")
    return sorted(latest.values(),key=lambda row:(row["event_time"],row["event_id"]))


def design_experiment(spec: Mapping[str, Any]) -> dict[str, Any]:
    required={"experiment_id","unit","primary_metric","metric_direction","baseline_rate","mde",
              "alpha","power","allocation","fixed_horizon_units","guardrails","guardrail_limits","stopping_rule"}
    if not isinstance(spec,Mapping) or not required.issubset(spec): raise ValueError("experiment specification is incomplete")
    if not isinstance(spec["experiment_id"],str) or not spec["experiment_id"]: raise ValueError("experiment ID required")
    if spec["unit"] not in {"user","account","session"}: raise ValueError("randomization unit must be declared")
    if spec["metric_direction"] not in {"higher_is_better","lower_is_better"}: raise ValueError("metric direction required")
    if spec["allocation"]!={"control":0.5,"treatment":0.5}: raise ValueError("reference exercise requires balanced allocation")
    if type(spec["fixed_horizon_units"]) is not int or spec["fixed_horizon_units"]<4: raise ValueError("fixed horizon must be declared")
    if spec["stopping_rule"]!="fixed_horizon_with_safety_stop": raise ValueError("no optional stopping; declare fixed horizon and safety stop")
    if not isinstance(spec["guardrails"],list) or not spec["guardrails"]: raise ValueError("at least one guardrail is required")
    if not isinstance(spec["guardrail_limits"],Mapping) or set(spec["guardrail_limits"])!=set(spec["guardrails"]):
        raise ValueError("every guardrail requires an explicit stop threshold")
    for name,limit in spec["guardrail_limits"].items():
        if (not isinstance(limit,Mapping) or limit.get("adverse_direction") not in {"higher","lower"}
                or not math.isfinite(float(limit.get("max_adverse_delta",float("nan"))))
                or limit["max_adverse_delta"]<0):
            raise ValueError(f"invalid safety stop boundary for guardrail {name}")
    per_arm=required_sample_per_arm(float(spec["baseline_rate"]),float(spec["mde"]),float(spec["alpha"]),float(spec["power"]))
    total=2*per_arm
    if spec["fixed_horizon_units"]<total: raise ValueError("fixed horizon is below the stated power target")
    return {"spec":dict(spec),"required_per_arm":per_arm,"required_total":total,
            "power_method":"normal approximation for two independent proportions","online_experiment_run":False}


def assign_experiment_units(unit_ids: Sequence[str], *, experiment_id: str, seed: int) -> dict[str, str]:
    import hashlib
    if not experiment_id or type(seed) is not int or len(set(unit_ids))!=len(unit_ids) or any(not x for x in unit_ids):
        raise ValueError("unique unit IDs, experiment ID and integer seed required")
    assignments={}
    for unit in unit_ids:
        digest=hashlib.sha256(f"{experiment_id}\0{seed}\0{unit}".encode()).digest()
        assignments[unit]="treatment" if int.from_bytes(digest[:8],"big")%2 else "control"
    return assignments
