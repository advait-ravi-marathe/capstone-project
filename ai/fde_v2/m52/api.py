"""Small bounded local serving seam. It returns predictions only, not online outcome claims."""
from __future__ import annotations
from typing import Any,Mapping,Sequence
from fastapi import FastAPI,HTTPException
from pydantic import BaseModel,ConfigDict,Field
from . import learner

class RecommendRequest(BaseModel):
    model_config=ConfigDict(extra='forbid')
    user_id:str=Field(min_length=1,max_length=64)
    candidates:list[str]=Field(min_length=1,max_length=100)

def create_app(training_events:Sequence[Mapping[str,Any]]|None=None,manifest:Mapping[str,Any]|None=None,*,adapter=learner)->FastAPI:
    app=FastAPI(title='M5.2 local ranker sample',version='draft')
    ready=isinstance(training_events,(list,tuple)) and bool(training_events) and isinstance(manifest,Mapping)
    if ready:
        app.state.dataset=adapter.validate_dataset_manifest(dict(manifest))
        app.state.events=[dict(e) for e in training_events]
        app.state.dataset_hash=manifest['sha256']
    else:
        app.state.dataset=None; app.state.events=[]; app.state.dataset_hash=None
    @app.post('/recommend')
    def recommend(body:RecommendRequest):
        if app.state.dataset is None: raise HTTPException(status_code=503,detail='dataset/model evidence is unavailable')
        if len(set(body.candidates))!=len(body.candidates) or any(not i or len(i)>64 for i in body.candidates):
            raise HTTPException(status_code=422,detail='candidate IDs must be unique bounded strings')
        try: ranked=adapter.rank_items(app.state.events,body.user_id,body.candidates)
        except (ValueError,KeyError) as exc: raise HTTPException(status_code=422,detail=str(exc)) from exc
        return {'user_id':body.user_id,'ranked':ranked,'algorithm':'explicit-cosine-sample-v1','dataset_hash':app.state.dataset_hash,
                'evidence_class':app.state.dataset['source_kind'],'online_uplift_claim':False}
    return app
