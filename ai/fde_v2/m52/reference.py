"""Transparent mechanics for chronological split, popularity, manual cosine and NDCG."""
from __future__ import annotations
from math import sqrt,log2,isfinite
import re
import hashlib,json
from typing import Any,Sequence
def dataset_sha256(events:Sequence[dict[str,Any]])->str:
 if not isinstance(events,(list,tuple)) or not events: raise ValueError('nonempty event dataset required')
 ordered=sorted((dict(row) for row in events),key=lambda row:(row.get("timestamp",0),row.get("event_id","")))
 canonical=json.dumps(ordered,sort_keys=True,separators=(',',':'),ensure_ascii=False,allow_nan=False)
 return hashlib.sha256(canonical.encode()).hexdigest()
def validate_dataset_manifest(m:dict[str,Any],events:Sequence[dict[str,Any]]|None=None)->dict[str,Any]:
 if not isinstance(m,dict) or m.get('source_kind') not in {'public','synthetic'}: raise ValueError('dataset must be public with source/license or explicitly synthetic')
 if not isinstance(m.get('source'),str) or not m['source'].strip() or not isinstance(m.get('license'),str) or not m['license'].strip(): raise ValueError('dataset source and license/fixture provenance required')
 if not isinstance(m.get('sha256'),str) or not re.fullmatch(r'[0-9a-f]{64}',m['sha256']): raise ValueError('dataset content hash required')
 if events is not None and m['sha256']!=dataset_sha256(events): raise ValueError('dataset hash does not match supplied training events')
 return {'valid':True,'source_kind':m['source_kind'],'download_performed':False,'synthetic_is_evidence':False}
def chronological_split(events:Sequence[dict[str,Any]])->dict[str,Any]:
 if not isinstance(events,(list,tuple)) or len(events)<3: raise ValueError("at least three timestamped events required")
 rows=list(events)
 if any(not isinstance(r,dict) or not r.get("event_id") or not r.get("user_id") or not r.get("item_id") or type(r.get("timestamp")) is not int for r in rows): raise ValueError("events need stable IDs and integer UTC timestamps")
 if len({r["event_id"] for r in rows})!=len(rows): raise ValueError("event IDs must be unique")
 rows.sort(key=lambda r:(r["timestamp"],r["event_id"])); n=len(rows); a=max(1,int(n*.6)); b=max(a+1,int(n*.8)); b=min(b,n-1)
 train,val,test=rows[:a],rows[a:b],rows[b:]
 if max(x["timestamp"] for x in train)>=min(x["timestamp"] for x in val) or max(x["timestamp"] for x in val)>=min(x["timestamp"] for x in test): raise ValueError("chronological boundaries must be strict")
 return {"train":train,"validation":val,"heldout":test,"test_frozen":False,
         "holdout_manifest_required":True,"random_row_split":False}
def cosine_similarity(left:Sequence[float],right:Sequence[float])->float:
 if len(left)!=len(right) or not left: raise ValueError("vectors must have equal nonzero dimension")
 if any(not isinstance(x,(int,float)) or not isfinite(x) for x in (*left,*right)): raise ValueError("vectors must be finite numbers")
 dot=sum(float(a)*float(b) for a,b in zip(left,right)); nl=sqrt(sum(float(a)**2 for a in left)); nr=sqrt(sum(float(b)**2 for b in right))
 return 0.0 if nl==0 or nr==0 else dot/(nl*nr)
def popularity(events:Sequence[dict[str,Any]],candidates:Sequence[str])->list[dict[str,Any]]:
 counts={i:0 for i in candidates}
 for e in events:
  if e["item_id"] in counts: counts[e["item_id"]]+=1
 return [{"item_id":i,"score":counts[i]} for i in sorted(counts,key=lambda x:(-counts[x],x))]
def rank_items(events:Sequence[dict[str,Any]],user_id:str,candidates:Sequence[str])->list[dict[str,Any]]:
 users=sorted({e["user_id"] for e in events}|{user_id}); items=sorted({e["item_id"] for e in events}|set(candidates)); vectors={i:[1.0 if any(e["user_id"]==u and e["item_id"]==i for e in events) else 0.0 for u in users] for i in items}
 seen={e["item_id"] for e in events if e["user_id"]==user_id}; out=[]
 for item in candidates:
  if item in seen: continue
  score=sum(cosine_similarity(vectors[item],vectors[s]) for s in seen)/len(seen) if seen else 0.0
  out.append({"item_id":item,"score":score})
 return sorted(out,key=lambda x:(-x["score"],x["item_id"]))
def ndcg_at_k(relevance:Sequence[float],k:int)->float:
 if type(k) is not int or k<1: raise ValueError("k must be positive integer")
 vals=list(relevance)
 if any(not isinstance(v,(int,float)) or not isfinite(v) or v<0 for v in vals): raise ValueError("relevance must be finite and nonnegative")
 def dcg(xs): return sum((2.0**v-1.0)/log2(i+2) for i,v in enumerate(xs[:k]))
 ideal=sorted(vals,reverse=True); denom=dcg(ideal)
 return dcg(vals)/denom if denom else 0.0
