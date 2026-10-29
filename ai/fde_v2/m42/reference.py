"""Reference mechanics for local change/incident evidence; no production claims."""
from __future__ import annotations
import math
from typing import Any, Mapping, Sequence

REQUIRED_CHANGE={"requirement","old_behavior","new_behavior","risk","rollback_target","compatibility"}
INCIDENT_STAGES=("detection","diagnosis","mitigation","recovery","prevention")
SUCCESS_OUTCOMES={"detection":"detected","diagnosis":"identified","mitigation":"mitigated",
                  "recovery":"recovered","prevention":"prevention_added"}
ALLOWED_OUTCOMES={"detection":{"detected","missed","not_observed"},
                  "diagnosis":{"identified","misdiagnosed","unknown"},
                  "mitigation":{"mitigated","not_mitigated","not_attempted"},
                  "recovery":{"recovered","not_recovered","not_attempted"},
                  "prevention":{"prevention_added","not_added","deferred"}}

def validate_change_contract(record: Mapping[str, Any]) -> dict[str, Any]:
    if not isinstance(record,Mapping) or not REQUIRED_CHANGE.issubset(record): raise ValueError("change contract is incomplete")
    if any(not isinstance(record[k],str) or not record[k].strip() for k in REQUIRED_CHANGE): raise ValueError("change fields must be nonempty text")
    if record.get("scope") not in {"minimal","expanded"}: raise ValueError("scope must be explicit")
    if record.get("scope")!="minimal": return {"status":"BLOCKED","reason":"scope is not the minimal requested change"}
    tests=record.get("regression_tests")
    if not isinstance(tests,list) or not tests or any(not isinstance(t,str) or not t for t in tests):
        raise ValueError("regression test selectors are required")
    return {"status":"READY_FOR_TEST_EVIDENCE","requirement":record["requirement"],
            "rollback_target":record["rollback_target"],"risk":record["risk"],"compatibility":record["compatibility"]}

def summarize_observations(rows: Sequence[Mapping[str, Any]]) -> dict[str, Any]:
    if not isinstance(rows,(list,tuple)) or not rows: raise ValueError("nonempty complete observation rows required")
    ids=[]; durations=[]; successes=0; errors=0; timeouts=0
    for row in rows:
        if not isinstance(row,Mapping) or not isinstance(row.get("request_id"),str) or not row["request_id"]:
            raise ValueError("each observation needs a request id")
        ids.append(row["request_id"]); status=row.get("status"); ms=row.get("elapsed_ms")
        if status not in {"success","error","timeout"}: raise ValueError("unknown request status")
        if not isinstance(ms,(int,float)) or isinstance(ms,bool) or not math.isfinite(float(ms)) or ms<0:
            raise ValueError("elapsed milliseconds must be finite and nonnegative")
        if status=="success": successes+=1
        elif status=="error": errors+=1
        else: timeouts+=1
        if status=="timeout":
            penalty=row.get("timeout_penalty_ms")
            if not isinstance(penalty,(int,float)) or isinstance(penalty,bool) or not math.isfinite(float(penalty)) or penalty<ms:
                raise ValueError("timeout needs its declared censoring penalty")
            durations.append(float(penalty))
        else: durations.append(float(ms))
    if len(set(ids))!=len(ids): raise ValueError("duplicate request id")
    ordered=sorted(durations); p95=ordered[max(0,math.ceil(.95*len(ordered))-1)]
    successful=sorted(float(r["elapsed_ms"]) for r in rows if r["status"]=="success")
    median=(successful[(len(successful)-1)//2] if len(successful)%2 else
            (successful[len(successful)//2-1]+successful[len(successful)//2])/2) if successful else None
    return {"request_count":len(rows),"success_count":successes,"error_count":errors,"timeout_count":timeouts,
            "availability_fraction":successes/len(rows),"all_request_p95_ms":p95,"success_only_median_ms":median,
            "cost":None,"cost_status":"not_observed","claim_scope":"short_local_observation_only"}

def validate_incident(record: Mapping[str, Any]) -> dict[str, Any]:
    if not isinstance(record,Mapping) or record.get("classification") not in {"local_drill","real_incident"}:
        raise ValueError("incident must be labeled local_drill or real_incident")
    events=record.get("events")
    if not isinstance(events,list) or len(events)!=len(INCIDENT_STAGES): return {"status":"INCOMPLETE","missing":list(INCIDENT_STAGES)}
    observed=[]
    for idx,(event,stage) in enumerate(zip(events,INCIDENT_STAGES)):
        if not isinstance(event,Mapping) or event.get("stage")!=stage or not isinstance(event.get("evidence_ref"),str) or not event["evidence_ref"]:
            return {"status":"INCOMPLETE","missing":list(INCIDENT_STAGES[idx:])}
        time_kind=event.get("time_kind")
        if time_kind not in {"observed","estimated"}: raise ValueError("event time provenance must be explicit")
        if time_kind=="estimated" and not event.get("estimate_reason"): raise ValueError("estimated time needs a reason")
        outcome=event.get("observed_status")
        if not isinstance(outcome,str) or outcome not in ALLOWED_OUTCOMES[stage]:
            raise ValueError(f"{stage} outcome must use a declared value")
        observed.append(outcome)
        if outcome!=SUCCESS_OUTCOMES[stage]:
            return {"status":"INCOMPLETE","missing":[stage],"reason":f"{stage} outcome is {outcome}; incident is not recovered/prevented"}
    return {"status":"COMPLETE_REQUIRES_REVIEW","classification":record["classification"],
            "events":len(events),"outcomes":observed,
            "cloud_incident_evidence":"BLOCKED" if record["classification"]=="local_drill" else "REVIEW_REQUIRED"}

def evaluate_change_gate(change: Mapping[str, Any], observation: Mapping[str, Any], incident: Mapping[str, Any]) -> dict[str, Any]:
    c=validate_change_contract(change)
    if c["status"]!="READY_FOR_TEST_EVIDENCE": return {"status":"BLOCKED","reason":c.get("reason","change contract incomplete")}
    if not isinstance(observation,Mapping) or observation.get("status")!="PASS" or not observation.get("source_hash") or not observation.get("test_run_id"):
        return {"status":"BLOCKED","reason":"actual scoped regression-test receipt is missing"}
    i=validate_incident(incident)
    if i["status"]!="COMPLETE_REQUIRES_REVIEW": return {"status":"BLOCKED","reason":"incident recovery sequence is incomplete"}
    return {"status":"EVIDENCE_REQUIRES_REVIEW","change_status":"TEST_RECEIPT_PRESENT",
            "incident_classification":i["classification"],"production_reliability_claim":False,
            "live_cloud_status":"BLOCKED" if i["classification"]=="local_drill" else "REVIEW_REQUIRED",
            "cost_observed":False}
