"""Integrated instructor workflow path for production-ML learning tasks.

The selected adapter is always called. Defaults point to learner TODOs; reference
mechanics must be explicitly selected in exercises/tests.
"""
from __future__ import annotations

from pathlib import Path
import hashlib
import json
import math
import os
from typing import Any, Mapping, Sequence

from . import learner
from . import reference


def _canonical_sha(value: Any) -> str:
    raw=json.dumps(value,sort_keys=True,separators=(",",":"),ensure_ascii=False,allow_nan=False)
    return hashlib.sha256(raw.encode()).hexdigest()


def _binary_metrics(probabilities, labels, *, adapter):
    if len(probabilities)!=len(labels) or not probabilities: raise ValueError("aligned nonempty validation rows required")
    predictions=[int(p>=0.5) for p in probabilities]
    return {"rows":len(labels),"accuracy":sum(a==b for a,b in zip(predictions,labels))/len(labels),
            "brier":adapter.brier_score(probabilities,labels),
            "ece":adapter.expected_calibration_error(probabilities,labels,bins=5)}


def compare_classical(train_x, train_y, validation_x, validation_y, *, adapter=learner,
                      learning_rate=0.1, epochs=200, l2=0.01):
    """Train learner logistic model and a real sklearn boosted-tree baseline.

    Transforms are fit only on train. Validation is read only for scoring.
    """
    if len(train_x)!=len(train_y) or len(validation_x)!=len(validation_y) or not validation_x:
        raise ValueError("train and validation rows/labels must align")
    scaler=adapter.fit_standardizer(train_x)
    train_scaled=adapter.transform_features(train_x,scaler)
    validation_scaled=adapter.transform_features(validation_x,scaler)
    # First column is a fixed intercept and is excluded from L2 in the reference.
    train_design=[[1.0,*row] for row in train_scaled]
    validation_design=[[1.0,*row] for row in validation_scaled]
    linear=adapter.train_linear_regression(train_design,train_y,learning_rate=learning_rate,epochs=epochs)
    logistic=adapter.train_logistic(train_design,train_y,learning_rate=learning_rate,epochs=epochs,l2=l2)
    logistic_prob=adapter.predict_logistic_proba(logistic,validation_design)
    tree=reference.fit_boosted_tree(train_x,train_y)
    tree_prob=[float(row[1]) for row in tree.predict_proba(validation_x)]
    linear_prediction=adapter.predict_linear_regression(linear,validation_design)
    return {"linear_regression":{"model":linear,"predictions":linear_prediction,"fit_partition":"train"},
            "logistic":{"model":logistic,"metrics":_binary_metrics(logistic_prob,validation_y,adapter=adapter),
                        "probabilities":logistic_prob},
            "boosted_tree":{"estimator":tree,"metrics":_binary_metrics(tree_prob,validation_y,adapter=adapter),
                            "probabilities":tree_prob},
            "transform":scaler,"fit_partition":"train","validation_used_for_fit":False,
            "model_winner_claim":False,"adapter_module":getattr(adapter,"__name__",adapter.__class__.__name__)}


def analyze_threshold_costs(probabilities, labels, *, false_positive_cost, false_negative_cost, adapter=learner):
    """Run the selected learner threshold rule against declared costs."""
    return adapter.threshold_cost_analysis(probabilities,labels,
        false_positive_cost=false_positive_cost,false_negative_cost=false_negative_cost)


def compare_regression_losses(predictions, targets, *, huber_delta=1.0, adapter=learner):
    """Run the selected learner's squared/absolute/Huber comparison."""
    return adapter.regression_loss_profile(predictions,targets,huber_delta=huber_delta)


def prepare_torch_data(features, labels, *, validation_fraction=.2, batch_size=8, seed=7, adapter=learner):
    """Exercise the learner's actual PyTorch Dataset/DataLoader construction."""
    import torch
    result=adapter.make_torch_loaders(features,labels,validation_fraction=validation_fraction,
                                      batch_size=batch_size,seed=seed)
    train_indices=result.get("train_indices",[]); validation_indices=result.get("validation_indices",[])
    if set(train_indices)&set(validation_indices) or set(train_indices)|set(validation_indices)!=set(range(len(features))):
        raise ValueError("train and validation indexes must be a disjoint full partition")
    train_loader=result.get("train_loader"); validation_loader=result.get("validation_loader")
    if not isinstance(train_loader,torch.utils.data.DataLoader) or not isinstance(validation_loader,torch.utils.data.DataLoader):
        raise TypeError("learner must return actual PyTorch DataLoaders")
    if result["dataset_rows"]!=len(features) or result.get("fit_partition")!="train_only":
        raise ValueError("dataset provenance does not match supplied training pool")
    for loader,expected_indices in ((train_loader,train_indices),(validation_loader,validation_indices)):
        subset=loader.dataset
        if not isinstance(subset,torch.utils.data.Subset) or list(subset.indices)!=list(expected_indices):
            raise ValueError("each loader must consume its declared partition indices")
        base=subset.dataset
        if not isinstance(base,torch.utils.data.TensorDataset) or len(base.tensors)!=2:
            raise ValueError("loaders must share a feature/label TensorDataset")
        actual_x,actual_y=base.tensors
        supplied_x=torch.tensor(features,dtype=actual_x.dtype)
        supplied_y=torch.tensor(labels,dtype=actual_y.dtype)
        if not torch.equal(actual_x.cpu(),supplied_x) or not torch.equal(actual_y.cpu(),supplied_y):
            raise ValueError("loader tensors must exactly match the supplied training pool")
    return result


def reconcile_delayed_labels(records, *, adapter=learner):
    """Run learner-owned idempotent event-time label reconciliation."""
    return adapter.reconcile_delayed_labels(records)


def torch_training_witness(features, labels, *, adapter=learner, seed=7, epochs=80, learning_rate=0.03,
                           initial_state=None, optimizer_state=None):
    """Execute the selected adapter and independently inspect its fixed MLP updates/loss."""
    import torch
    from torch.nn import functional as torch_functional
    if not features or len(features)!=len(labels): raise ValueError("aligned nonempty training rows required")
    if initial_state is None:
        baseline=reference.build_torch_model(len(features[0]),seed=seed)
        initial_state={key:value.detach().clone() for key,value in baseline.state_dict().items()}
    else:
        initial_state={key:value.detach().clone() for key,value in initial_state.items()}
    trained=adapter.train_torch_model(features,labels,seed=seed,epochs=epochs,learning_rate=learning_rate,
                                      initial_state=initial_state,optimizer_state=optimizer_state)
    model=trained.get("model") if isinstance(trained,dict) else None
    if not isinstance(model,torch.nn.Module):
        raise TypeError("selected adapter must return an actually trained torch module")
    state=model.state_dict()
    if not state or any(not isinstance(value,torch.Tensor) for value in state.values()) or not any(value.numel() for value in state.values()):
        raise ValueError("trained module has no parameter state")
    if set(state)!=set(initial_state) or any(tuple(state[key].shape)!=tuple(initial_state[key].shape) for key in state):
        raise ValueError("trained model does not match the declared MLP checkpoint architecture")
    observed_update=any(not torch.equal(state[key].detach().cpu(),initial_state[key].detach().cpu()) for key in state)
    if not observed_update:
        raise ValueError("adapter did not prove a parameter update")
    history=trained.get("loss_history",[])
    if len(history)<2 or not math.isfinite(float(history[0])) or not math.isfinite(float(history[-1])):
        raise ValueError("training adapter must return finite initial/final loss")
    baseline=reference.build_torch_model(len(features[0]),seed=seed)
    baseline.load_state_dict(initial_state,strict=True)
    baseline.eval(); model.eval()
    x=torch.tensor(features,dtype=torch.float32,device="cpu")
    y=torch.tensor(labels,dtype=torch.float32,device="cpu").view(-1,1)
    with torch.no_grad():
        initial_loss=float(torch_functional.binary_cross_entropy_with_logits(baseline(x),y))
        final_loss=float(torch_functional.binary_cross_entropy_with_logits(model.cpu()(x),y))
    if final_loss >= initial_loss:
        raise ValueError("observed model objective did not improve from supplied initial weights")
    if abs(float(history[0])-initial_loss)>1e-4:
        raise ValueError("reported initial loss does not match independently measured objective")
    if abs(float(history[-1])-final_loss)>1e-4:
        raise ValueError("reported final loss does not match independently measured objective")
    if not torch.isfinite(torch.tensor(final_loss)):
        raise ValueError("trained model has non-finite objective")
    return {**trained,"observed_parameters_changed":True,"observed_initial_loss":initial_loss,
            "observed_final_loss":final_loss,"parameters_changed":True}


def save_torch_checkpoint(path, trained, *, manifest: Mapping[str,Any]):
    import torch
    target=Path(path)
    if target.exists() or target.is_symlink(): raise FileExistsError("checkpoint path must be new")
    model=trained["model"]
    payload={"state_dict":model.state_dict(),"optimizer_state_dict":trained.get("optimizer_state_dict"),
             "epoch_count":len(trained["loss_history"]),"loss_history":trained["loss_history"],
             "manifest":dict(manifest)}
    payload["manifest_sha256"]=_canonical_sha(payload["manifest"])
    torch.save(payload,target)
    return {"path":str(target),"manifest_sha256":payload["manifest_sha256"],"checkpoint_sha256":hashlib.sha256(target.read_bytes()).hexdigest()}


def load_torch_checkpoint(path, *, build_model, expected_manifest: Mapping[str,Any]):
    import torch
    target=Path(path)
    if target.is_symlink() or not target.is_file(): raise PermissionError("checkpoint must be a regular local file")
    payload=torch.load(target,map_location="cpu",weights_only=True)
    if payload.get("manifest")!=dict(expected_manifest) or payload.get("manifest_sha256")!=_canonical_sha(dict(expected_manifest)):
        raise ValueError("checkpoint training/config manifest mismatch")
    model=build_model()
    model.load_state_dict(payload["state_dict"],strict=True)
    model.eval()
    return {"model":model,"optimizer_state_dict":payload.get("optimizer_state_dict"),
            "manifest":payload["manifest"],"loss_history":payload["loss_history"]}


def resume_torch_training(path, features, labels, *, build_model, adapter=learner,
                          expected_manifest: Mapping[str,Any], epochs: int, learning_rate: float):
    restored=load_torch_checkpoint(path,build_model=build_model,expected_manifest=expected_manifest)
    return torch_training_witness(features,labels,adapter=adapter,
        seed=int(expected_manifest["seed"]),epochs=epochs,learning_rate=learning_rate,
        initial_state=restored["model"].state_dict(),optimizer_state=restored["optimizer_state_dict"])


def compare_rankers(train_interactions: Sequence[Mapping[str,Any]],
                    evaluation_interactions: Sequence[Mapping[str,Any]],
                    training_catalog: Sequence[str], *, adapter=learner, k=5,
                    negatives_per_positive=2, seed=17):
    """Compare pairwise ranker, popularity and cosine on the same users/candidates."""
    if not training_catalog or len(set(training_catalog))!=len(training_catalog):
        raise ValueError("unique training-time candidate catalog required")
    catalog=set(training_catalog)
    if any(row["item_id"] not in catalog for row in train_interactions):
        raise ValueError("training interaction outside training catalog")
    pairs=adapter.sample_negative_pairs(train_interactions,sorted(catalog),
                                         per_positive=negatives_per_positive,seed=seed)
    model=adapter.train_pairwise_ranker(pairs,dimensions=8,epochs=120,learning_rate=0.03,seed=seed)
    from ai.fde_v2.m52 import reference as m52_reference
    metric=adapter.ndcg_at_k
    common=[]
    by_user={}
    for row in evaluation_interactions:
        by_user.setdefault(row["user_id"],[]).append(row["item_id"])
    train_by_user={}
    for row in train_interactions: train_by_user.setdefault(row["user_id"],set()).add(row["item_id"])
    popularity=m52_reference.popularity(train_interactions,sorted(catalog))
    popularity_score={row["item_id"]:row["score"] for row in popularity}
    for user,targets in sorted(by_user.items()):
        seen=train_by_user.get(user,set())
        candidates=sorted(catalog-seen)
        relevant=set(target for target in targets if target in candidates)
        if not candidates or not relevant:
            common.append({"user_id":user,"status":"not_scored_cold_or_unavailable_target","candidate_count":len(candidates)})
            continue
        cold_user=user not in train_by_user
        learned=sorted(candidates,key=lambda item:(-popularity_score[item],item) if cold_user
                       else (-adapter.predict_pairwise_score(model,user,item),item))
        pop=sorted(candidates,key=lambda item:(-popularity_score[item],item))
        cosine=[row["item_id"] for row in m52_reference.rank_items(train_interactions,user,candidates)]
        if set(learned)!=set(pop) or set(pop)!=set(cosine): raise AssertionError("ranker candidate sets differ")
        relevance=lambda ranked:[1.0 if item in relevant else 0.0 for item in ranked]
        common.append({"user_id":user,"status":"scored","candidate_count":len(candidates),
            "target_count":len(relevant),"learned_ndcg":metric(relevance(learned),k),
            "popularity_ndcg":metric(relevance(pop),k),"cosine_ndcg":metric(relevance(cosine),k),
            "learned_top_k":learned[:k],"popularity_top_k":pop[:k],"cosine_top_k":cosine[:k],
            "cold_start_fallback":"global_popularity" if cold_user else None})
    scored=[row for row in common if row["status"]=="scored"]
    def mean(key): return sum(row[key] for row in scored)/len(scored) if scored else None
    return {"model":model,"pairs":pairs,"rows":common,"scored_users":len(scored),
            "unscored_users":len(common)-len(scored),"candidate_items":sorted(catalog),
            "metrics":{"learned_ndcg":mean("learned_ndcg"),"popularity_ndcg":mean("popularity_ndcg"),
                       "cosine_ndcg":mean("cosine_ndcg")},
            "heldout_performance_claim":False,"synthetic_or_offline_only":True,
            "same_candidate_set":True,"training_catalog_only":True}


def experiment_assignment(unit_ids, spec, *, adapter=learner, seed=31):
    validated=adapter.design_experiment(spec)
    if len(unit_ids)<spec["fixed_horizon_units"]:
        raise ValueError("assignment cohort is below the predeclared fixed horizon")
    assignments=adapter.assign_experiment_units(unit_ids,experiment_id=spec["experiment_id"],seed=seed)
    if set(assignments)!=set(unit_ids) or any(arm not in {"control","treatment"} for arm in assignments.values()):
        raise ValueError("assignment adapter returned invalid units or arms")
    return {"design":validated,"assignments":assignments,"online_run":False,
            "synthetic_assignment":True,"production_impact_claim":False}


def analyze_experiment(assignments: Mapping[str,str], outcomes: Mapping[str,float], *,
                       design: Mapping[str,Any], adapter=learner, guardrails=None):
    if set(assignments)!=set(outcomes) or not assignments: raise ValueError("every randomized unit must have an observed outcome")
    if any(arm not in {"control","treatment"} for arm in assignments.values()): raise ValueError("assignment arm is invalid")
    declared_horizon=design.get("spec",{}).get("fixed_horizon_units",design.get("fixed_horizon_units"))
    power_minimum=design["required_total"]
    if type(declared_horizon) is not int or declared_horizon < power_minimum:
        raise ValueError("validated design must bind a fixed horizon at least as large as its power target")
    if len(assignments)!=declared_horizon:
        raise ValueError("observed cohort must exactly match the declared fixed horizon before analysis")
    controls=[outcomes[u] for u,a in assignments.items() if a=="control"]
    treatments=[outcomes[u] for u,a in assignments.items() if a=="treatment"]
    srm=adapter.srm_check(len(controls),len(treatments))
    if srm["sample_ratio_mismatch"]:
        return {"status":"INVALID_ASSIGNMENT_SRM","srm":srm,"outcome_analysis_performed":False}
    guardrail_results=[]
    if set(guardrails or {})!=set(design["spec"]["guardrails"]):
        raise ValueError("all predeclared guardrail measurements are required")
    for name,values in (guardrails or {}).items():
        if set(values)!=set(assignments): raise ValueError(f"guardrail {name} is missing randomized units")
        c=[values[u] for u,a in assignments.items() if a=="control"]
        t=[values[u] for u,a in assignments.items() if a=="treatment"]
        effect=sum(t)/len(t)-sum(c)/len(c)
        limits=design["spec"]["guardrail_limits"]
        if name not in limits: raise ValueError(f"undeclared guardrail supplied: {name}")
        limit=limits[name]
        adverse=effect if limit["adverse_direction"]=="higher" else -effect
        guardrail_results.append({"name":name,"effect":effect,"adverse_effect":adverse,"rows":len(values),
                                 "safety_stop_triggered":adverse>limit["max_adverse_delta"]})
    stop=any(row["safety_stop_triggered"] for row in guardrail_results)
    primary=adapter.analyze_ab(controls,treatments)
    alpha=float(design["spec"]["alpha"])
    direction=design["spec"]["metric_direction"]
    favorable=(primary["absolute_effect"]>0 if direction=="higher_is_better" else primary["absolute_effect"]<0)
    primary_signal=bool(favorable and primary["normal_approx_two_sided_p"]<alpha)
    return {"status":"SYNTHETIC_ANALYSIS_ONLY","srm":srm,"primary":primary,
            "guardrails":guardrail_results,"safety_stop_triggered":stop,
            "fixed_horizon_complete":True,
            "fixed_horizon_units":declared_horizon,"observed_units":len(assignments),"online_experiment":False,
            "predeclared_alpha":alpha,"predeclared_primary_signal":primary_signal,
            "decision":"stop for guardrail breach" if stop else "primary result is reviewed only at fixed horizon"}


def summarize_ranker_slices(result: Mapping[str,Any], user_slices: Mapping[str,Mapping[str,str]], *,
                            min_support: int = 2):
    """Summarize already-scored users by declared slice; never infer slice labels."""
    if type(min_support) is not int or min_support < 1: raise ValueError("positive slice support threshold required")
    metric_rows={row["user_id"]:row for row in result.get("rows",[]) if row.get("status")=="scored"}
    buckets={}
    for user,attributes in user_slices.items():
        if user not in metric_rows: continue
        if not isinstance(attributes,Mapping): raise ValueError("slice values must be explicit mappings")
        for name,value in attributes.items():
            if not isinstance(name,str) or not isinstance(value,str): raise ValueError("slice name/value must be strings")
            buckets.setdefault((name,value),[]).append(metric_rows[user])
    output=[]
    for (name,value),rows in sorted(buckets.items()):
        enough=len(rows)>=min_support
        output.append({"slice":name,"value":value,"support":len(rows),"status":"reported" if enough else "insufficient_support",
            "learned_ndcg":sum(r["learned_ndcg"] for r in rows)/len(rows) if enough else None,
            "popularity_ndcg":sum(r["popularity_ndcg"] for r in rows)/len(rows) if enough else None,
            "cosine_ndcg":sum(r["cosine_ndcg"] for r in rows)/len(rows) if enough else None})
    return {"slices":output,"minimum_support":min_support,"groups_inferred":False}


def write_linear_bundle(root, *, version: str, model: Mapping[str,Any], scaler: Mapping[str,Any],
                        feature_names: Sequence[str], training_data_sha256: str):
    base=Path(root); base.mkdir(parents=True,exist_ok=True)
    if base.is_symlink(): raise PermissionError("registry root cannot be a symlink")
    if not version or "/" in version or ".." in version: raise ValueError("invalid immutable model version")
    target=base/version
    target.mkdir()
    body={"version":version,"model":dict(model),"scaler":dict(scaler),"feature_names":list(feature_names),
          "training_data_sha256":training_data_sha256,"artifact_kind":"linear-logistic-cpu-demo"}
    body["artifact_sha256"]=_canonical_sha(body)
    with (target/"artifact.json").open("x",encoding="utf-8") as stream:
        json.dump(body,stream,sort_keys=True,separators=(",",":"),allow_nan=False)
    return {"version":version,"artifact_sha256":body["artifact_sha256"],"path":str(target)}


def promote_model(root, version: str):
    base=Path(root); artifact_path=base/version/"artifact.json"
    if artifact_path.is_symlink() or not artifact_path.is_file(): raise FileNotFoundError("versioned model artifact missing")
    artifact=json.loads(artifact_path.read_text(encoding="utf-8"))
    if artifact.get("artifact_sha256")!=_canonical_sha({k:v for k,v in artifact.items() if k!="artifact_sha256"}):
        raise ValueError("artifact hash mismatch")
    pointer=base/"active.json"
    previous=[]
    if pointer.exists():
        active=json.loads(pointer.read_text(encoding="utf-8")); previous=list(active.get("history",[]))
        previous.append(active["version"])
    state={"version":version,"artifact_sha256":artifact["artifact_sha256"],"history":previous}
    tmp=base/"active.json.tmp"
    tmp.write_text(json.dumps(state,sort_keys=True,separators=(",",":")),encoding="utf-8")
    os.replace(tmp,pointer)
    return state


def rollback_model(root):
    base=Path(root); pointer=base/"active.json"
    if pointer.is_symlink() or not pointer.is_file(): raise FileNotFoundError("active model pointer missing")
    state=json.loads(pointer.read_text(encoding="utf-8"))
    history=list(state.get("history",[]))
    if not history: raise ValueError("no prior model version available for rollback")
    prior=history.pop()
    artifact=json.loads((base/prior/"artifact.json").read_text(encoding="utf-8"))
    restored={"version":prior,"artifact_sha256":artifact["artifact_sha256"],"history":history+[state["version"]]}
    tmp=base/"active.json.tmp"; tmp.write_text(json.dumps(restored,sort_keys=True,separators=(",",":")),encoding="utf-8")
    os.replace(tmp,pointer)
    return restored


def serve_logistic(root, rows: Sequence[Mapping[str,Any]], *, adapter=learner):
    base=Path(root); pointer=json.loads((base/"active.json").read_text(encoding="utf-8"))
    artifact_path=base/pointer["version"]/"artifact.json"
    if artifact_path.is_symlink(): raise PermissionError("model artifact cannot be a symlink")
    artifact=json.loads(artifact_path.read_text(encoding="utf-8"))
    if artifact.get("artifact_sha256")!=pointer.get("artifact_sha256") or pointer["artifact_sha256"]!=_canonical_sha({k:v for k,v in artifact.items() if k!="artifact_sha256"}):
        raise ValueError("active model artifact does not match promoted digest")
    names=artifact["feature_names"]
    if any(set(row)!=set(names) for row in rows): raise ValueError("serving feature schema differs from training")
    raw=[[row[name] for name in names] for row in rows]
    transformed=adapter.transform_features(raw,artifact["scaler"])
    design=[[1.0,*row] for row in transformed]
    scores=adapter.predict_logistic_proba(artifact["model"],design)
    return {"model_version":pointer["version"],"artifact_sha256":pointer["artifact_sha256"],
            "scores":scores,"feature_schema_match":True,"training_serving_transform":"same frozen scaler"}
