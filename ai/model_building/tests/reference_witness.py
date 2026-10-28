"""Instructor-only independent candidates for validating the learner acceptance path.

This module is never imported by learner.py and is not a learner solution. Tests
patch candidates into the public acceptance tests, then substitute bad mutants to
prove those same assertions distinguish the contract.
"""

import json
import math


def sigmoid(z):
    if not math.isfinite(z):
        raise ValueError("finite score required")
    return 1 / (1 + math.exp(-z))


def train_one_logistic_step(weights, examples, learning_rate):
    if not examples or learning_rate <= 0 or not math.isfinite(learning_rate):
        raise ValueError("nonempty batch and positive finite rate required")
    if not weights or any(len(x) + 1 != len(weights) for x, _ in examples):
        raise ValueError("bias-first weights must be one longer than each feature row")
    if any(y not in (0, 1) for _, y in examples):
        raise ValueError("binary labels required")
    gradient = [0.0] * len(weights)
    for x, y in examples:
        error = sigmoid(weights[0] + sum(w * v for w, v in zip(weights[1:], x))) - y
        for j, value in enumerate([1.0, *x]):
            gradient[j] += error * value / len(examples)
    return [w - learning_rate * g for w, g in zip(weights, gradient)]


def mean_batch_gradient_python(rows, labels, weights):
    if not rows or len(rows)!=len(labels) or not weights or any(len(row)+1!=len(weights) for row in rows):
        raise ValueError("aligned nonempty rows and bias-first weights required")
    if any(label not in (0,1) for label in labels): raise ValueError("binary labels required")
    gradient=[0.0]*len(weights)
    for row,label in zip(rows,labels):
        probability=1/(1+math.exp(-(weights[0]+sum(w*x for w,x in zip(weights[1:],row)))))
        error=probability-label
        for index,value in enumerate([1.0,*row]): gradient[index]+=error*value/len(rows)
    return gradient


def mean_binary_cross_entropy(probabilities, labels):
    if not probabilities or len(probabilities) != len(labels):
        raise ValueError("aligned nonempty arrays required")
    if any(y not in (0, 1) for y in labels) or any(not 0 < p < 1 for p in probabilities):
        raise ValueError("valid binary probabilities and labels required")
    return sum(-y * math.log(p) - (1-y) * math.log(1-p) for p, y in zip(probabilities, labels)) / len(labels)


def dot(a, b):
    if not a or len(a) != len(b):
        raise ValueError("equal nonempty vectors required")
    return sum(x*y for x, y in zip(a, b))


def central_difference(f, x, epsilon=1e-5):
    if epsilon <= 0:
        raise ValueError("epsilon must be positive")
    return (f(x+epsilon) - f(x-epsilon)) / (2*epsilon)


def deterministic_split(records, train_count, validation_count):
    ids = [r["id"] for r in records]
    if len(set(ids)) != len(ids) or train_count < 1 or validation_count < 0 or train_count + validation_count >= len(ids):
        raise ValueError("unique IDs and nonempty partitions required")
    return ids[:train_count], ids[train_count:train_count+validation_count], ids[train_count+validation_count:]


def find_target_leakage(names, forbidden):
    blocked = set(forbidden)
    return [name for name in names if name in blocked]


def fit_threshold_classifier(training_values, training_labels):
    if not training_values or len(training_values)!=len(training_labels) or any(y not in (0,1) for y in training_labels):
        raise ValueError("aligned nonempty binary training examples required")
    if any(not math.isfinite(v) for v in training_values): raise ValueError("finite features required")
    candidates=sorted(set(training_values))
    candidates=[candidates[0]-1.0,*candidates]
    best=None
    for threshold in candidates:
        predictions=[int(value>=threshold) for value in training_values]
        correct=sum(a==b for a,b in zip(training_labels,predictions))
        if best is None or (correct,threshold)>(best[0],best[1]): best=(correct,threshold)
    return {"threshold":best[1],"training_count":len(training_values)}


def predict_threshold_classifier(model, values):
    if set(model)!={"threshold","training_count"} or model["training_count"]<=0 or not math.isfinite(model["threshold"]):
        raise ValueError("invalid fitted threshold model")
    if any(not math.isfinite(value) for value in values): raise ValueError("finite values required")
    return [int(value>=model["threshold"]) for value in values]


def fit_standardizer(training_rows, heldout_rows):
    if not training_rows or not training_rows[0]:
        raise ValueError("nonempty matrix required")
    width = len(training_rows[0])
    if any(len(row) != width for row in [*training_rows, *heldout_rows]):
        raise ValueError("consistent feature width required")
    means = [sum(row[j] for row in training_rows) / len(training_rows) for j in range(width)]
    scales = []
    for j, mean in enumerate(means):
        variance = sum((row[j]-mean)**2 for row in training_rows) / len(training_rows)
        scales.append(math.sqrt(variance) or 1.0)
    apply = lambda rows: [[(row[j]-means[j])/scales[j] for j in range(width)] for row in rows]
    return means, scales, apply(training_rows), apply(heldout_rows)


def majority_prediction(labels):
    if not labels or any(y not in (0, 1) for y in labels):
        raise ValueError("binary labels required")
    return int(sum(labels) * 2 > len(labels))


def nearest_centroid_predictions(training_rows, training_labels, query_rows):
    if not training_rows or len(training_rows) != len(training_labels):
        raise ValueError("aligned training rows required")
    width = len(training_rows[0])
    if width == 0 or any(len(r) != width for r in [*training_rows, *query_rows]):
        raise ValueError("consistent feature width required")
    classes = list(dict.fromkeys(training_labels))
    centroids = {c:[sum(row[j] for row,y in zip(training_rows,training_labels) if y==c) /
                     sum(y==c for y in training_labels) for j in range(width)] for c in classes}
    return [min(classes, key=lambda c: sum((v-m)**2 for v,m in zip(row,centroids[c]))) for row in query_rows]


def confusion_matrix(labels, predictions):
    if not labels or len(labels) != len(predictions) or any(y not in (0,1) for y in [*labels,*predictions]):
        raise ValueError("aligned binary values required")
    return {"tp":sum(y==1 and p==1 for y,p in zip(labels,predictions)),
            "tn":sum(y==0 and p==0 for y,p in zip(labels,predictions)),
            "fp":sum(y==0 and p==1 for y,p in zip(labels,predictions)),
            "fn":sum(y==1 and p==0 for y,p in zip(labels,predictions))}


def precision_recall(c):
    if set(c) != {"tp","tn","fp","fn"} or any(type(v) is not int or v < 0 for v in c.values()):
        raise ValueError("valid confusion counts required")
    pden, rden = c["tp"]+c["fp"], c["tp"]+c["fn"]
    return {"precision":c["tp"]/pden if pden else None, "recall":c["tp"]/rden if rden else None}


def threshold_predictions(probabilities, threshold):
    if not 0 < threshold < 1 or any(not 0 <= p <= 1 for p in probabilities):
        raise ValueError("probabilities and threshold must be in range")
    return [int(p >= threshold) for p in probabilities]


def metrics_by_slice(labels, predictions, slices, expected_slices=None):
    if not labels or len(labels)!=len(predictions) or len(labels)!=len(slices):
        raise ValueError("aligned arrays required")
    groups=list(expected_slices) if expected_slices is not None else list(dict.fromkeys(slices))
    if len(set(groups))!=len(groups) or any(group not in groups for group in slices):
        raise ValueError("declared slice set must be unique and complete")
    out={}
    for group in groups:
        pairs=[(y,p) for y,p,s in zip(labels,predictions,slices) if s==group]
        c=confusion_matrix([y for y,_ in pairs],[p for _,p in pairs]) if pairs else {"tp":0,"tn":0,"fp":0,"fn":0}
        den_pos=c["tp"]+c["fn"]; den_pred=c["tp"]+c["fp"]
        out[group]={"count":len(pairs),**c,
                    "observed_positive_fraction":c["tp"]+c["fn"] and c["tp"]/den_pos if pairs else None,
                    "precision":c["tp"]/den_pred if den_pred else None,
                    "recall":c["tp"]/den_pos if den_pos else None}
    return out


def compare_predictions(ids, labels, candidates):
    if not ids or len(ids)!=len(labels) or any(len(p)!=len(ids) for p in candidates.values()):
        raise ValueError("aligned candidate predictions required")
    out={}
    for name, predictions in candidates.items():
        errors=[rid for rid,y,p in zip(ids,labels,predictions) if y!=p]
        out[name]={"accuracy":(len(ids)-len(errors))/len(ids),"error_ids":errors}
    return out


def matrix_shape(rows, labels):
    import numpy as np
    array=np.asarray(rows,dtype=np.int64); target=np.asarray(labels,dtype=np.int64)
    if array.ndim!=2 or 0 in array.shape or target.shape!=(array.shape[0],):
        raise ValueError("rectangular numeric features and one label per row required")
    return array,target


def linear_scores(rows, weights, bias):
    import numpy as np
    x=np.asarray(rows,dtype=float); w=np.asarray(weights,dtype=float)
    if x.ndim!=2 or w.ndim!=1 or x.shape[1]!=w.size or not math.isfinite(bias):
        raise ValueError("matrix/weight shape mismatch")
    return x @ w + bias


def batch_gradient(rows, labels, weights):
    import numpy as np
    x=np.asarray(rows,dtype=float); y=np.asarray(labels,dtype=float); w=np.asarray(weights,dtype=float)
    if x.ndim!=2 or len(x)==0 or y.shape!=(len(x),) or w.shape!=(x.shape[1]+1,):
        raise ValueError("batch and bias-first weight shapes mismatch")
    augmented=np.column_stack((np.ones(len(x)),x))
    errors=1/(1+np.exp(-(augmented@w)))-y
    return (augmented.T@errors)/len(x)


def finite_sigmoid(z):
    if not math.isfinite(z): raise ValueError("finite score required")
    if z>=0: return 1/(1+math.exp(-z))
    e=math.exp(z); return e/(1+e)


def train_curve(initial, rates, steps):
    if not rates or any(rate<=0 for rate in rates) or steps<1: raise ValueError("positive rates and steps required")
    out=[]
    for rate in rates:
        value=initial; losses=[(value-2)**2]
        for _ in range(steps):
            value-=rate*2*(value-2); losses.append((value-2)**2)
        out.append({"learning_rate":rate,"losses":losses})
    return out


def experiment_record(seed, split_ids, feature_names, predictions):
    return {"seed":seed,"split_ids":list(split_ids),"feature_names":list(feature_names),"predictions":list(predictions)}


def reproducible_predictions(record_ids, labels, predictions, seed):
    if not record_ids or len(record_ids)!=len(labels) or len(labels)!=len(predictions) or len(set(record_ids))!=len(record_ids):
        raise ValueError("aligned rows with unique IDs required")
    if type(seed) is not int:
        raise ValueError("integer seed required")
    return {"seed":seed,"ordered_ids":list(record_ids),"labels":list(labels),"predictions":list(predictions)}


def sklearn_pipeline_baseline(features, labels, train_indices, test_indices):
    from sklearn.pipeline import make_pipeline
    from sklearn.preprocessing import StandardScaler
    from sklearn.linear_model import LogisticRegression
    if not train_indices or not test_indices or set(train_indices)&set(test_indices): raise ValueError("disjoint train/test required")
    model=make_pipeline(StandardScaler(),LogisticRegression(random_state=0))
    model.fit([features[i] for i in train_indices],[labels[i] for i in train_indices])
    predictions=model.predict([features[i] for i in test_indices]).tolist()
    return {"fit_indices":list(train_indices),"test_indices":list(test_indices),"predictions":predictions,
            "correct_count":sum(a==b for a,b in zip((labels[i] for i in test_indices),predictions)),"pipeline":model}


def fit_id_guard(training_ids, transform_fit_ids, evaluation_ids):
    return bool(training_ids) and set(transform_fit_ids)<=set(training_ids) and not (set(transform_fit_ids)&set(evaluation_ids))


def fold_evaluation_summary(names, fold_labels, fold_predictions, test_labels, test_predictions):
    if not names or len(names)!=len(fold_labels) or len(names)!=len(fold_predictions) or not test_labels or len(test_labels)!=len(test_predictions):
        raise ValueError("one aligned nonempty result per fold and final test required")
    if len(set(names))!=len(names): raise ValueError("fold names must be unique")
    scores=[]
    for ys,ps in zip(fold_labels,fold_predictions):
        if not ys or len(ys)!=len(ps): raise ValueError("fold labels/predictions must align")
        scores.append(sum(a==b for a,b in zip(ys,ps))/len(ys))
    return {"fold_names":list(names),"fold_accuracies":scores,"mean_cv_accuracy":sum(scores)/len(scores),
            "final_test_accuracy":sum(a==b for a,b in zip(test_labels,test_predictions))/len(test_labels),
            "final_test_count":len(test_labels)}


def deterministic_folds(record_ids, fold_count):
    if not record_ids or len(set(record_ids))!=len(record_ids) or type(fold_count) is not int or not 2<=fold_count<=len(record_ids):
        raise ValueError("unique IDs and between two and n folds required")
    return [list(record_ids[index::fold_count]) for index in range(fold_count)]


def calibration_bins(probabilities, labels, edges):
    if not probabilities or len(probabilities)!=len(labels) or len(edges)<2 or edges[0]!=0 or edges[-1]!=1 or sorted(edges)!=list(edges):
        raise ValueError("aligned data and increasing full-range edges required")
    if any(not 0<=p<=1 for p in probabilities): raise ValueError("probability out of range")
    out=[]
    for lo,hi in zip(edges,edges[1:]):
        indexes=[i for i,p in enumerate(probabilities) if lo<=p<(hi if hi<1 else hi+1e-12)]
        out.append({"count":len(indexes),"mean_confidence":sum(probabilities[i] for i in indexes)/len(indexes) if indexes else None,
                    "observed_positive_fraction":sum(labels[i] for i in indexes)/len(indexes) if indexes else None})
    return out


def build_document_fixture():
    texts=["annual premium is due","excluded loss wording","insured person definition","coverage period schedule","waiting period exclusion","deductible definition","renewal date schedule","not covered exclusion","claim notice definition","named premises schedule","policy limitation exclusion","policy term definition"]
    labels=["schedule","exclusion","definition"]*4
    return [{"id":f"doc-{i+1:02d}","text":text,"label":labels[i],"split":"train" if i<6 else "validation" if i<9 else "test"} for i,text in enumerate(texts)]


def prediction_error_ids(ids, labels, predictions):
    if not ids or len(ids)!=len(labels) or len(ids)!=len(predictions): raise ValueError("aligned rows required")
    return [rid for rid,y,p in zip(ids,labels,predictions) if y!=p]


def report_model_limit(score, label, decision):
    if not 0<=score<=1 or label not in (0,1) or decision not in ("not_assessed","not_decided"):
        raise ValueError("toy outputs cannot make downstream decisions")
    return {"score":score,"predicted_label":label,"downstream_decision":decision}


def serialize_linear_model(weights, feature_names):
    if len(weights)!=len(feature_names)+1 or any(not math.isfinite(v) for v in weights) or len(set(feature_names))!=len(feature_names):
        raise ValueError("bias-first finite weights and unique feature names required")
    return json.dumps({"weights":list(weights),"feature_names":list(feature_names)},separators=(",",":"),allow_nan=False)


def load_linear_model(blob):
    try: obj=json.loads(blob,parse_constant=lambda _: (_ for _ in ()).throw(ValueError("nonfinite")))
    except Exception as exc: raise ValueError("invalid model artifact") from exc
    if set(obj)!={"weights","feature_names"} or len(obj["weights"])!=len(obj["feature_names"])+1 or any(not math.isfinite(v) for v in obj["weights"]):
        raise ValueError("invalid model structure")
    return obj


def fit_document_type_centroids(rows, labels, record_ids, feature_names):
    if not rows or len(rows)!=len(labels) or len(rows)!=len(record_ids) or not feature_names or len(set(record_ids))!=len(record_ids):
        raise ValueError("aligned training rows and unique IDs required")
    width=len(feature_names)
    if any(len(row)!=width or any(not math.isfinite(v) for v in row) for row in rows): raise ValueError("invalid feature matrix")
    classes=list(dict.fromkeys(labels))
    if len(classes)<2: raise ValueError("at least two classes required")
    centroids=[]
    for c in classes:
        group=[r for r,y in zip(rows,labels) if y==c]
        centroids.append([sum(row[j] for row in group)/len(group) for j in range(width)])
    return {"classes":classes,"feature_names":list(feature_names),"training_ids":list(record_ids),"centroids":centroids}


def predict_document_type(model, rows):
    classes=model["classes"]; centroids=model["centroids"]; width=len(model["feature_names"])
    if not classes or len(classes)!=len(centroids) or any(len(row)!=width or any(not math.isfinite(v) for v in row) for row in rows): raise ValueError("model/row shape mismatch")
    return [classes[min(range(len(classes)),key=lambda i:sum((x-c)**2 for x,c in zip(row,centroids[i])))] for row in rows]


def serialize_document_type_model(model):
    required={"classes","feature_names","training_ids","centroids"}
    if set(model)!=required or len(model["classes"])!=len(model["centroids"]): raise ValueError("invalid model")
    return json.dumps(model,separators=(",",":"),allow_nan=False)


def load_document_type_model(blob):
    try: model=json.loads(blob,parse_constant=lambda _: (_ for _ in ()).throw(ValueError("nonfinite")))
    except Exception as exc: raise ValueError("invalid model artifact") from exc
    if set(model)!={"classes","feature_names","training_ids","centroids"}: raise ValueError("invalid model schema")
    if len(set(model["classes"]))!=len(model["classes"]) or len(set(model["feature_names"]))!=len(model["feature_names"]): raise ValueError("duplicate classes/features")
    if len(model["classes"])!=len(model["centroids"]) or len(model["training_ids"])!=len(set(model["training_ids"])): raise ValueError("invalid model dimensions/IDs")
    if any(len(row)!=len(model["feature_names"]) or any(not math.isfinite(v) for v in row) for row in model["centroids"]): raise ValueError("invalid centroid values")
    return model


def score_rows(weights, rows):
    if not weights or any(len(row)!=len(weights)-1 for row in rows): raise ValueError("score shape mismatch")
    return [weights[0]+sum(w*x for w,x in zip(weights[1:],row)) for row in rows]


def build_prediction_response(model, payload):
    """Independent test-only oracle for the OWN-INT-001 learner seam."""
    import math

    if not isinstance(model, dict) or set(model) != {"classes", "feature_names", "training_ids", "centroids"}:
        raise ValueError("invalid model")
    if not isinstance(payload, dict) or set(payload) != {"feature_names", "features"}:
        raise ValueError("invalid request schema")
    names = payload["feature_names"]
    values = payload["features"]
    if type(names) is not list or names != model["feature_names"]:
        raise ValueError("feature order mismatch")
    if type(values) is not list or len(values) != len(names):
        raise ValueError("feature width mismatch")
    if any(type(value) not in (int, float) or not math.isfinite(value) for value in values):
        raise ValueError("features must be finite numbers")
    if not model["classes"] or len(model["classes"]) != len(model["centroids"]):
        raise ValueError("invalid class centroid matrix")
    if any(len(row) != len(names) or any(not math.isfinite(value) for value in row) for row in model["centroids"]):
        raise ValueError("invalid centroid dimensions")
    distances = [sum((value - center) ** 2 for value, center in zip(values, centroid)) for centroid in model["centroids"]]
    label = model["classes"][min(range(len(distances)), key=distances.__getitem__)]
    return {"predicted_label": label, "decision_scope": "synthetic_document_type_only"}


def prediction_error_response(error):
    """Safe instructor-only HTTP error classifier used by the public oracle."""
    if isinstance(error, ValueError):
        return 400, {"error": "invalid_request"}
    return 500, {"error": "prediction_failed"}
