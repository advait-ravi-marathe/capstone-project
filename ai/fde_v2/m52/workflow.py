"""Development feature path: comparison is bound to explicit train/validation rows."""
from . import learner
from .reference import popularity,dataset_sha256
from typing import Mapping
from pathlib import Path
import hashlib,json,inspect,os


def _code_sha256(function):
    try: source=inspect.getsource(function)
    except (OSError,TypeError) as exc: raise ValueError("selected learner implementation source is unavailable") from exc
    return hashlib.sha256(source.encode()).hexdigest()


def _canonical_hash(value):
    return hashlib.sha256(json.dumps(value,sort_keys=True,separators=(",",":"),ensure_ascii=False,
                                   allow_nan=False).encode()).hexdigest()


def _safe_existing_directory(root):
    supplied=Path(root)
    if supplied.is_symlink():
        raise PermissionError("artifact root cannot be a symlink")
    resolved=supplied.resolve(strict=True)
    if not resolved.is_dir():
        raise PermissionError("artifact root must be a directory")
    return resolved


def save_model_bundle(root,events,source_manifest,model_config,*,adapter=learner):
    """Write a content-bound local data/config/code bundle exactly once."""
    base=Path(root)
    if base.is_symlink(): raise PermissionError("bundle path cannot be a symlink")
    base.mkdir(parents=True,exist_ok=True)
    if any(base.iterdir()): raise FileExistsError("model bundle requires an empty directory")
    copied=[dict(row) for row in events]
    data_hash=dataset_sha256(copied)
    if source_manifest.get("sha256")!=data_hash: raise ValueError("source manifest hash does not match exact event rows")
    adapter.validate_dataset_manifest(dict(source_manifest),copied)
    if not isinstance(model_config,Mapping) or not model_config:
        raise ValueError("explicit model configuration is required")
    model={"model_kind":"explicit-cosine-sample","config":dict(model_config),
           "ranker_code_sha256":_code_sha256(adapter.rank_items),
           "metric_code_sha256":_code_sha256(adapter.ndcg_at_k)}
    model["model_sha256"]=_canonical_hash(model)
    data_path=base/"training-events.json"
    manifest_path=base/"model-manifest.json"
    for path,content in ((data_path,copied),(manifest_path,
            {"source":dict(source_manifest),"model":model,"data_sha256":data_hash})):
        with path.open("x",encoding="utf-8") as stream:
            json.dump(content,stream,sort_keys=True,separators=(",",":"),ensure_ascii=False,allow_nan=False)
    return {"status":"BUNDLE_WRITTEN","root":str(base.resolve()),"data_sha256":data_hash,
            "model_sha256":model["model_sha256"],"model_manifest_sha256":_canonical_hash({"source":dict(source_manifest),"model":model,"data_sha256":data_hash})}


def load_model_bundle(root,*,adapter=learner):
    base=_safe_existing_directory(root)
    for name in ("training-events.json","model-manifest.json"):
        path=base/name
        if path.is_symlink() or not path.resolve(strict=True).is_relative_to(base):
            raise PermissionError("model bundle path escaped its root")
    events=json.loads((base/"training-events.json").read_text(encoding="utf-8"))
    manifest=json.loads((base/"model-manifest.json").read_text(encoding="utf-8"))
    actual_hash=dataset_sha256(events)
    if actual_hash!=manifest.get("data_sha256") or actual_hash!=manifest.get("source",{}).get("sha256"):
        raise ValueError("stored model bundle data hash mismatch")
    source=manifest["source"]; model=manifest["model"]
    adapter.validate_dataset_manifest(source,events)
    if (model.get("ranker_code_sha256")!=_code_sha256(adapter.rank_items)
            or model.get("metric_code_sha256")!=_code_sha256(adapter.ndcg_at_k)
            or model.get("model_sha256")!=_canonical_hash({k:v for k,v in model.items() if k!="model_sha256"})):
        raise ValueError("model code/config hash mismatch")
    return {"events":events,"source_manifest":source,"model":model,"data_sha256":actual_hash,
            "model_sha256":model["model_sha256"]}


def freeze_heldout(root,events,source_manifest,model_config,*,candidate_items=None,k=5,adapter=learner):
    """Create write-once train/validation/held-out partitions and freeze digest."""
    base=Path(root)
    if base.is_symlink(): raise PermissionError("freeze root cannot be a symlink")
    base.mkdir(parents=True,exist_ok=True)
    if any(base.iterdir()): raise FileExistsError("held-out freeze requires a new empty directory")
    split=adapter.chronological_split(events)
    if split.get("test_frozen") is not False or split.get("holdout_manifest_required") is not True:
        raise ValueError("splitter must not claim freeze before an immutable artifact exists")
    partitions={name:[dict(row) for row in split[name]] for name in ("train","validation","heldout")}
    catalog=sorted({row["item_id"] for row in events}) if candidate_items is None else list(candidate_items)
    if (not catalog or any(not isinstance(item,str) or not item for item in catalog)
            or len(set(catalog))!=len(catalog) or type(k) is not int or k<1):
        raise ValueError("frozen candidate catalog and metric cutoff are invalid")
    training_manifest={**dict(source_manifest),"sha256":dataset_sha256(partitions["train"]),
                       "source_dataset_sha256":dataset_sha256(events)}
    bundle=save_model_bundle(base/"bundle",partitions["train"],training_manifest,model_config,adapter=adapter)
    hashes={name:_canonical_hash(rows) for name,rows in partitions.items()}
    manifest={"format":"m52-heldout-freeze-v1","source_sha256":dataset_sha256(events),
        "training_sha256":bundle["data_sha256"],
        "model_sha256":bundle["model_sha256"],"model_manifest_sha256":bundle["model_manifest_sha256"],
        "partition_sha256":hashes,"partition_counts":{key:len(value) for key,value in partitions.items()},
        "heldout_event_ids":[row["event_id"] for row in partitions["heldout"]],
        "candidate_items":catalog,"candidate_items_sha256":_canonical_hash(catalog),"ndcg_k":k,
        "frozen":True,"freeze_sha256":None,"classification":"local_frozen_evaluation_plan"}
    manifest["freeze_sha256"]=_canonical_hash({k:v for k,v in manifest.items() if k!="freeze_sha256"})
    for name,rows in partitions.items():
        with (base/f"{name}.json").open("x",encoding="utf-8") as stream:
            json.dump(rows,stream,sort_keys=True,separators=(",",":"),allow_nan=False)
    with (base/"freeze.json").open("x",encoding="utf-8") as stream:
        json.dump(manifest,stream,sort_keys=True,separators=(",",":"),allow_nan=False)
    return manifest


def run_heldout_once(root,candidate_items,*,k=5,adapter=learner):
    """Score every held-out user once, preserving errors and verifying frozen hashes."""
    base=_safe_existing_directory(root)
    freeze_path=base/"freeze.json"
    manifest=json.loads(freeze_path.read_text(encoding="utf-8"))
    if (not isinstance(candidate_items,(list,tuple)) or not candidate_items
            or any(not isinstance(item,str) or not item for item in candidate_items)
            or len(set(candidate_items))!=len(candidate_items) or type(k) is not int or k<1):
        raise ValueError("held-out candidate set and metric cutoff must be explicit and bounded")
    stored_hash=manifest.get("freeze_sha256")
    expected=_canonical_hash({key:value for key,value in manifest.items() if key!="freeze_sha256"})
    if manifest.get("frozen") is not True or stored_hash!=expected:
        raise ValueError("frozen evaluation manifest is invalid")
    if candidate_items!=manifest.get("candidate_items") or k!=manifest.get("ndcg_k") or _canonical_hash(candidate_items)!=manifest.get("candidate_items_sha256"):
        raise ValueError("candidate set or metric cutoff differs from frozen evaluation")
    bundle=load_model_bundle(base/"bundle",adapter=adapter)
    if bundle["data_sha256"]!=manifest.get("training_sha256") or bundle["model_sha256"]!=manifest.get("model_sha256"):
        raise ValueError("frozen evaluation source/model binding changed")
    partitions={}
    for name in ("train","validation","heldout"):
        path=base/f"{name}.json"
        if path.is_symlink() or not path.resolve(strict=True).is_relative_to(base):
            raise PermissionError("partition escaped frozen evaluation root")
        rows=json.loads(path.read_text(encoding="utf-8"))
        if _canonical_hash(rows)!=manifest["partition_sha256"].get(name):
            raise ValueError(f"{name} partition differs from frozen digest")
        partitions[name]=rows
    reconstructed=partitions["train"]+partitions["validation"]+partitions["heldout"]
    if dataset_sha256(reconstructed)!=manifest.get("source_sha256"):
        raise ValueError("frozen partitions do not reconstruct the source dataset digest")
    lock=base/"heldout-consumed.json"
    fd=os.open(lock,os.O_WRONLY|os.O_CREAT|os.O_EXCL,0o600)
    with os.fdopen(fd,"w",encoding="utf-8") as stream:
        json.dump({"freeze_sha256":stored_hash,"status":"started"},stream,sort_keys=True)
    training=partitions["train"]; heldout=partitions["heldout"]
    train_ids={row["event_id"] for row in training}
    if train_ids & {row["event_id"] for row in heldout}:
        raise ValueError("frozen training and heldout event IDs overlap")
    rows=[]
    for target in heldout:
        user=target["user_id"]
        seen={row["item_id"] for row in training if row["user_id"]==user}
        candidates=[item for item in candidate_items if item not in seen]
        relevant={target["item_id"]}
        row={"event_id":target["event_id"],"user_id":user,"target_item_id":target["item_id"]}
        try:
            baseline=popularity(training,candidates)
            model=adapter.rank_items(training,user,candidates)
            if {x["item_id"] for x in baseline}!={x["item_id"] for x in model}:
                raise ValueError("baseline and candidate item sets differ")
            base_rel=[1.0 if item["item_id"] in relevant else 0.0 for item in baseline]
            model_rel=[1.0 if item["item_id"] in relevant else 0.0 for item in model]
            row.update({"status":"scored","baseline_ndcg":adapter.ndcg_at_k(base_rel,k),
                        "candidate_ndcg":adapter.ndcg_at_k(model_rel,k),
                        "baseline_ranked":baseline,"candidate_ranked":model})
        except Exception as exc:
            row.update({"status":"error","error_type":type(exc).__name__})
        rows.append(row)
    result_ids=[row.get("event_id") for row in rows]
    if result_ids!=manifest["heldout_event_ids"]:
        raise ValueError("held-out output does not account for each event exactly once and in frozen order")
    results={"classification":"local_fixture_results_require_review",
        "freeze_sha256":stored_hash,"model_sha256":manifest["model_sha256"],
        "heldout_event_ids":manifest["heldout_event_ids"],"unit_count":len(rows),
        "rows":rows,"error_count":sum(row["status"]!="scored" for row in rows),
        "learner_completion":False}
    result_path=base/"heldout-results.json"
    with result_path.open("x",encoding="utf-8") as stream:
        json.dump(results,stream,sort_keys=True,separators=(",",":"),allow_nan=False)
    return results
def predict(events,user_id,candidates,*,adapter=learner):
 return {"user_id":user_id,"model_kind":"explicit_cosine_sample","ranked":adapter.rank_items(events,user_id,candidates),"online_uplift_claim":False}
def compare_validation(split,user_id,candidates,k=5,*,adapter=learner):
 if not isinstance(split,Mapping) or set(split)!={'train','validation','test_frozen'}: raise ValueError('development view must contain train and validation only; held-out rows are not accepted')
 if split.get('test_frozen') is not False: raise ValueError('development comparison cannot claim that a heldout partition was frozen')
 train,validation=split['train'],split['validation']
 if not train or not validation: raise ValueError('training and validation partitions must be present')
 def partition_ids(rows):
  if not isinstance(rows,(list,tuple)) or any(not isinstance(e,dict) or not isinstance(e.get('event_id'),str) or not e['event_id'] or type(e.get('timestamp')) is not int for e in rows):
   raise ValueError('development rows require stable event IDs and integer timestamps')
  ids=[e['event_id'] for e in rows]
  if len(set(ids))!=len(ids): raise ValueError('event IDs must be unique within each partition')
  return set(ids)
 train_ids,validation_ids=partition_ids(train),partition_ids(validation)
 if not train_ids.isdisjoint(validation_ids): raise ValueError('event leakage across train and validation')
 if max(e['timestamp'] for e in train)>=min(e['timestamp'] for e in validation): raise ValueError('validation events must follow training events chronologically')
 seen={e['item_id'] for e in train if e['user_id']==user_id}
 eligible=[i for i in candidates if i not in seen]
 baseline=popularity(train,eligible)
 candidate=adapter.rank_items(train,user_id,eligible)
 if {r['item_id'] for r in baseline}!={r['item_id'] for r in candidate}: raise ValueError('baseline and candidate must use the same candidate set')
 relevant={e['item_id'] for e in validation if e['user_id']==user_id}
 if not relevant: raise ValueError('validation partition needs at least one target for this user')
 rel=[1.0 if r['item_id'] in relevant else 0.0 for r in candidate]
 base_rel=[1.0 if r['item_id'] in relevant else 0.0 for r in baseline]
 return {'baseline_ndcg_at_k':adapter.ndcg_at_k(base_rel,k),'candidate_ndcg_at_k':adapter.ndcg_at_k(rel,k),'unit_count':1,'split':'validation','heldout_opened':False,'online_uplift_claim':False}
