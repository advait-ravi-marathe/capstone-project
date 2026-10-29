"""Integrated instructor workflow path for production-ML learning tasks.

The selected adapter is always called. Defaults point to learner TODOs; reference
mechanics must be explicitly selected in exercises/tests.
"""
from __future__ import annotations

from pathlib import Path
import copy
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
    if (not isinstance(train_indices,(list,tuple)) or not isinstance(validation_indices,(list,tuple))
            or any(type(index) is not int or index<0 or index>=len(features)
            for index in (*train_indices,*validation_indices))
            or len(set(train_indices))!=len(train_indices)
            or len(set(validation_indices))!=len(validation_indices)
            or set(train_indices)&set(validation_indices)
            or set(train_indices)|set(validation_indices)!=set(range(len(features)))
            or len(train_indices)+len(validation_indices)!=len(features)):
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


def validate_torch_batch(features, labels, *, adapter=learner):
    """Call the learner's actual batch validator and check its observable contract."""
    import torch
    result=adapter.validate_torch_batch(features,labels)
    x=result.get("features"); y=result.get("targets")
    if not isinstance(x,torch.Tensor) or not isinstance(y,torch.Tensor):
        raise TypeError("learner batch validator must return actual tensors")
    expected_x=torch.tensor(features,dtype=torch.float32,device="cpu")
    expected_y=torch.tensor(labels,dtype=torch.float32,device="cpu").reshape(-1,1)
    if (x.device.type!="cpu" or y.device.type!="cpu" or x.dtype!=torch.float32 or y.dtype!=torch.float32
            or not torch.equal(x,expected_x) or not torch.equal(y,expected_y)
            or result.get("rows")!=len(features) or result.get("feature_dim")!=len(features[0])):
        raise ValueError("learner batch tensors/metadata differ from the supplied CPU float32 batch")
    return result


def torch_forward_witness(features, labels, *, adapter=learner, seed=7):
    """Exercise the selected learner's real model builder and observed forward shape."""
    import torch
    batch=validate_torch_batch(features,labels,adapter=adapter)
    model=adapter.build_torch_model(batch["feature_dim"],seed=seed)
    if not isinstance(model,torch.nn.Module): raise TypeError("learner model builder must return a torch module")
    modules=list(model.modules())[1:]
    if (sum(isinstance(layer,torch.nn.Linear) for layer in modules)!=2
            or not any(isinstance(layer,(torch.nn.ReLU,torch.nn.Tanh,torch.nn.GELU)) for layer in modules)):
        raise ValueError("learner must build a two-layer MLP with an explicit hidden nonlinearity")
    with torch.no_grad(): logits=model(batch["features"])
    if (not isinstance(logits,torch.Tensor) or tuple(logits.shape)!=(len(features),1)
            or logits.device.type!="cpu" or not torch.isfinite(logits).all()):
        raise ValueError("learner forward pass must produce one finite CPU logit per row")
    return {"model":model,"logits":logits.detach(),"rows":len(features),"feature_dim":batch["feature_dim"]}


def autograd_scalar_witness(x, *, adapter=learner):
    """Compare candidate autograd outputs with the closed-form supplied objective."""
    result=adapter.autograd_scalar_gradient(x)
    expected_value=3.0*x*x+2.0*x+1.0
    expected_gradient=6.0*x+2.0
    if (not isinstance(result,dict) or abs(float(result.get("value",float("nan")))-expected_value)>1e-10
            or abs(float(result.get("gradient",float("nan")))-expected_gradient)>1e-10):
        raise ValueError("autograd candidate differs from the supplied scalar objective")
    return result


def finite_difference_witness(function, x, *, epsilon=1e-6, adapter=learner):
    observed=float(adapter.finite_difference_gradient(function,x,epsilon=epsilon))
    expected=(float(function(x+epsilon))-float(function(x-epsilon)))/(2*epsilon)
    if not math.isfinite(observed) or abs(observed-expected)>1e-12:
        raise ValueError("candidate finite difference differs from central-difference oracle")
    return {"gradient":observed,"epsilon":epsilon}


def dropout_mode_witness(model,batch,*,repeats=4,adapter=learner):
    import torch
    if not isinstance(model,torch.nn.Module) or not isinstance(batch,torch.Tensor) or repeats<2:
        raise TypeError("dropout probe requires a torch module, tensor batch and at least two repeats")
    dropout_layers=[module for module in model.modules()
                    if isinstance(module,(torch.nn.Dropout,torch.nn.Dropout1d,torch.nn.Dropout2d,torch.nn.Dropout3d))
                    and module.p>0]
    if not dropout_layers: raise ValueError("model must contain an active dropout layer")
    before={key:value.detach().clone() for key,value in model.state_dict().items()}
    prior=model.training
    devices=sorted({parameter.device.index for parameter in model.parameters()
                    if parameter.device.type=="cuda" and parameter.device.index is not None})
    with torch.random.fork_rng(devices=devices):
        torch.manual_seed(1847)
        result=adapter.dropout_mode_probe(model,batch,repeats=repeats)
    if not isinstance(result,dict): raise TypeError("learner dropout probe must return train/eval output arrays")
    candidate_train=result.get("train_outputs",[]); candidate_eval=result.get("eval_outputs",[])
    if len(candidate_train)!=repeats or len(candidate_eval)!=repeats or model.training!=prior:
        raise ValueError("mode probe must return requested repeats and restore prior mode")
    if any(not torch.is_tensor(output) for output in candidate_train+candidate_eval):
        raise TypeError("mode probe outputs must be actual tensors")
    if any(not torch.equal(before[key],value.detach()) for key,value in model.state_dict().items()):
        raise ValueError("mode probe changed model parameters or buffers")
    oracle_train_mode=model.training
    with torch.random.fork_rng(devices=devices):
        torch.manual_seed(1847)
        model.train(); train=[]
        with torch.no_grad():
            for _ in range(repeats): train.append(model(batch).detach().cpu())
        model.eval(); evaluation=[]
        with torch.no_grad():
            for _ in range(repeats): evaluation.append(model(batch).detach().cpu())
        model.train(oracle_train_mode)
    if (any(not torch.equal(expected.cpu(),observed.detach().cpu())
            for expected,observed in zip(train,candidate_train))
            or any(not torch.equal(expected.cpu(),observed.detach().cpu())
                   for expected,observed in zip(evaluation,candidate_eval))):
        raise ValueError("candidate dropout outputs differ from independent seeded model execution")
    train_varies=any(not torch.equal(train[0],value) for value in train[1:])
    eval_stable=all(torch.equal(evaluation[0],value) for value in evaluation[1:])
    if not train_varies or not eval_stable:
        raise ValueError("dropout should vary in train mode and be deterministic in eval mode")
    return {**result,"train_varies":train_varies,"eval_stable":eval_stable}


def _nested_equal(left,right,torch):
    if isinstance(left,torch.Tensor) and isinstance(right,torch.Tensor): return torch.equal(left,right)
    if isinstance(left,dict) and isinstance(right,dict):
        return left.keys()==right.keys() and all(_nested_equal(left[key],right[key],torch) for key in left)
    if isinstance(left,(list,tuple)) and isinstance(right,type(left)):
        return len(left)==len(right) and all(_nested_equal(a,b,torch) for a,b in zip(left,right))
    return left==right


def evaluate_torch_model(model, validation_loader, optimizer, *, adapter=learner):
    """Check learner evaluation outputs against real eval/no_grad execution and immutable state."""
    import torch
    from torch.nn import functional as torch_functional
    from torch.utils.data import DataLoader, SequentialSampler
    if not isinstance(model,torch.nn.Module) or not isinstance(validation_loader,DataLoader):
        raise TypeError("actual model and DataLoader are required")
    if not isinstance(validation_loader.sampler,SequentialSampler):
        raise ValueError("validation loader must preserve deterministic row order")
    batches=list(validation_loader)
    if not batches: raise ValueError("nonempty validation loader is required")
    inputs=torch.cat([batch[0] for batch in batches],dim=0)
    targets=torch.cat([batch[1] for batch in batches],dim=0).float().reshape(-1,1)
    before={key:value.detach().clone() for key,value in model.state_dict().items()}
    prior_grads={name:(None if parameter.grad is None else parameter.grad.detach().clone())
                 for name,parameter in model.named_parameters()}
    optimizer_before=copy.deepcopy(optimizer.state_dict()) if optimizer is not None else None
    prior_mode=model.training
    observed=adapter.evaluate_torch_model(model,validation_loader,optimizer)
    if (not isinstance(observed,dict) or observed.get("evaluation_mode")!="eval/no_grad"
            or observed.get("optimizer_state_read_only") is not True):
        raise ValueError("evaluation must report its eval/no_grad and read-only-state contract")
    if model.training!=prior_mode: raise ValueError("evaluation must restore the model's prior train/eval mode")
    if any(not torch.equal(before[key],value.detach()) for key,value in model.state_dict().items()):
        raise ValueError("evaluation must not modify model parameters or buffers")
    if optimizer is not None and not _nested_equal(optimizer_before,optimizer.state_dict(),torch):
        raise ValueError("evaluation must not modify optimizer state")
    for name,parameter in model.named_parameters():
        prior=prior_grads[name]
        if (prior is None)!=(parameter.grad is None) or (prior is not None and not torch.equal(prior,parameter.grad)):
            raise ValueError("evaluation must not create or change parameter gradients")
    logits=observed.get("logits") if isinstance(observed,dict) else None
    if not isinstance(logits,torch.Tensor) or logits.requires_grad:
        raise ValueError("evaluation must return detached logits computed without gradients")
    if tuple(logits.shape)!=(len(targets),1) or observed.get("rows")!=len(targets):
        raise ValueError("evaluation output must account for every validation row exactly once")
    if not torch.isfinite(logits).all() or not math.isfinite(float(observed.get("loss",float("nan")))):
        raise ValueError("evaluation logits and loss must be finite")
    model.eval()
    with torch.no_grad(): expected_logits=model(inputs).detach().cpu()
    expected_loss=float(torch_functional.binary_cross_entropy_with_logits(expected_logits,targets.cpu()))
    model.train(prior_mode)
    if not torch.allclose(logits.detach().cpu(),expected_logits,rtol=1e-5,atol=1e-6):
        raise ValueError("reported validation logits differ from the actual selected model")
    if abs(float(observed["loss"])-expected_loss)>1e-6:
        raise ValueError("reported validation loss differs from the actual selected model")
    return {**observed,"observed_loss":expected_loss,"observed_rows":len(targets),"parameters_unchanged":True}


def select_torch_epoch(validation_losses, *, patience, adapter=learner):
    """Run the learner's validation-only early-stopping decision."""
    result=adapter.select_torch_epoch(validation_losses,patience=patience)
    if not isinstance(result,dict) or result.get("selection_partition")!="validation_only":
        raise ValueError("epoch choice must declare validation-only selection")
    best=result.get("best_epoch"); stop=result.get("stop_epoch")
    if (type(best) is not int or type(stop) is not int or not 0<=best<len(validation_losses)
            or not best<=stop<len(validation_losses) or float(validation_losses[best])!=min(map(float,validation_losses))):
        raise ValueError("learner epoch choice must identify the earliest validation minimum and bounded patience stop")
    return result


def reconcile_delayed_labels(records, *, adapter=learner):
    """Run learner-owned idempotent event-time label reconciliation."""
    return adapter.reconcile_delayed_labels(records)


def torch_training_witness(features, labels, *, adapter=learner, seed=7, epochs=80, learning_rate=0.03,
                           initial_state=None, optimizer_state=None, optimizer_kind="adam",
                           batch_size=None, positive_weight=1.0):
    """Execute the selected adapter and independently inspect its fixed MLP updates/loss."""
    import torch
    from torch.nn import functional as torch_functional
    if not features or len(features)!=len(labels): raise ValueError("aligned nonempty training rows required")
    batch=validate_torch_batch(features,labels,adapter=adapter)
    builder=getattr(adapter,"build_torch_model",None)
    if builder is None: raise TypeError("selected learner adapter must supply build_torch_model")
    if initial_state is None:
        baseline=builder(len(features[0]),seed=seed)
        if not isinstance(baseline,torch.nn.Module): raise TypeError("selected model builder must return a torch module")
        modules=list(baseline.modules())[1:]
        if (sum(isinstance(layer,torch.nn.Linear) for layer in modules)!=2
                or not any(isinstance(layer,(torch.nn.ReLU,torch.nn.Tanh,torch.nn.GELU)) for layer in modules)):
            raise ValueError("selected model builder must provide the declared two-layer MLP")
        initial_state={key:value.detach().clone() for key,value in baseline.state_dict().items()}
    else:
        initial_state={key:value.detach().clone() for key,value in initial_state.items()}
    trained=adapter.train_torch_model(features,labels,seed=seed,epochs=epochs,learning_rate=learning_rate,
                                      optimizer_kind=optimizer_kind,batch_size=batch_size,
                                      positive_weight=positive_weight,
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
    if len(history)!=epochs+1 or any(not math.isfinite(float(loss)) for loss in history):
        raise ValueError("training adapter must return a finite loss for initialization and every epoch")
    epoch_states=trained.get("epoch_state_dicts")
    if (trained.get("epoch_loss_semantics")!="full_training_objective_after_epoch"
            or not isinstance(epoch_states,(list,tuple)) or len(epoch_states)!=epochs+1):
        raise ValueError("training must provide each epoch's state snapshot and objective semantics")
    actual_batch_size=batch_size or len(features)
    expected_updates=math.ceil(len(features)/actual_batch_size)
    if (trained.get("optimizer_kind")!=optimizer_kind or trained.get("batch_size")!=actual_batch_size
            or trained.get("positive_weight")!=positive_weight
            or trained.get("examples_per_epoch")!=[len(features)]*epochs
            or trained.get("updates_per_epoch")!=[expected_updates]*epochs):
        raise ValueError("training metadata must match requested optimizer, rows, batches and positive weight")
    baseline=builder(len(features[0]),seed=seed)
    if not isinstance(baseline,torch.nn.Module): raise TypeError("selected model builder must return a torch module")
    modules=list(baseline.modules())[1:]
    if (sum(isinstance(layer,torch.nn.Linear) for layer in modules)!=2
            or not any(isinstance(layer,(torch.nn.ReLU,torch.nn.Tanh,torch.nn.GELU)) for layer in modules)):
        raise ValueError("selected model builder must provide the declared two-layer MLP")
    baseline.eval(); model.eval()
    x=torch.tensor(features,dtype=torch.float32,device="cpu")
    y=torch.tensor(labels,dtype=torch.float32,device="cpu").view(-1,1)
    objective=torch_functional.binary_cross_entropy_with_logits
    weight=torch.tensor([positive_weight],dtype=torch.float32)
    independently_observed=[]
    for epoch,state in enumerate(epoch_states):
        if not isinstance(state,dict) or set(state)!=set(initial_state):
            raise ValueError(f"epoch {epoch} state snapshot does not match the declared architecture")
        if any(not isinstance(value,torch.Tensor) or tuple(value.shape)!=tuple(initial_state[key].shape)
               or not torch.isfinite(value).all() for key,value in state.items()):
            raise ValueError(f"epoch {epoch} state snapshot must contain finite tensors of the expected shapes")
        if epoch==0 and any(not torch.equal(state[key].detach().cpu(),initial_state[key].detach().cpu())
                            for key in initial_state):
            raise ValueError("epoch zero snapshot differs from the requested initial checkpoint")
        baseline.load_state_dict(state,strict=True)
        baseline.eval()
        with torch.no_grad():
            independently_observed.append(float(objective(baseline(x),y,pos_weight=weight)))
    if any(abs(float(history[index])-loss)>1e-5 for index,loss in enumerate(independently_observed)):
        raise ValueError("per-epoch history differs from independently recomputed weighted training objective")
    if any(not torch.equal(epoch_states[-1][key].detach().cpu(),model.state_dict()[key].detach().cpu())
           for key in model.state_dict()):
        raise ValueError("final epoch snapshot differs from the actually trained model")
    with torch.no_grad():
        initial_loss=independently_observed[0]
        final_loss=float(objective(model.cpu()(x),y,pos_weight=weight))
        actual_probabilities=torch.sigmoid(model(x)).flatten().tolist()
    if final_loss >= initial_loss:
        raise ValueError("observed model objective did not improve from supplied initial weights")
    if abs(float(history[0])-initial_loss)>1e-4:
        raise ValueError("reported initial loss does not match independently measured objective")
    if abs(float(history[-1])-final_loss)>1e-4:
        raise ValueError("reported final loss does not match independently measured objective")
    probabilities=trained.get("probabilities")
    if (not isinstance(probabilities,(list,tuple)) or len(probabilities)!=len(labels)
            or any(not math.isfinite(float(value)) for value in probabilities)
            or any(abs(float(actual)-float(reported))>1e-6
                   for actual,reported in zip(actual_probabilities,probabilities))):
        raise ValueError("reported probabilities must match actual model predictions for every supplied row")
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
        if cold_user:
            learned=adapter.cold_start_fallback(candidates,popularity_score)
            if len(learned)!=len(candidates) or set(learned)!=set(candidates):
                raise ValueError("learner cold-start fallback must return each eligible candidate exactly once")
        else:
            learned=sorted(candidates,key=lambda item:(-adapter.predict_pairwise_score(model,user,item),item))
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
