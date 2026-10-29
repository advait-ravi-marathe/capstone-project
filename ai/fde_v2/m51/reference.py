"""Deterministic scoring mechanics; no attempt or professional outcome is authored."""
from __future__ import annotations
from datetime import date
import hashlib,re
from typing import Any,Mapping
GATES={"coding_dsa","unfamiliar_repository","system_design","ai_evaluation_delivery","discovery_ambiguity","behavioral_project"}
def validate_attempt(a:Mapping[str,Any])->dict[str,Any]:
 if not isinstance(a,Mapping): raise ValueError("attempt record required")
 required=("gate","task_type","attempt_id","actual_date","task_content_sha256","task_hash","unseen_confirmation","tool_allowance","help_used","artifact_ref","reviewer_type","critical_failure")
 if any(k not in a for k in required): raise ValueError("attempt dates, task, allowances, artifact and reviewer provenance required")
 if a["gate"] not in GATES or a["task_type"]!=a["gate"] or not a["attempt_id"] or not a["actual_date"] or not a["artifact_ref"]: raise ValueError("gate and actual attempt references required")
 if not isinstance(a["task_content_sha256"],str) or re.fullmatch(r"[0-9a-f]{64}",a["task_content_sha256"]) is None: raise ValueError("task content must be bound by a lowercase SHA-256 digest")
 expected=hashlib.sha256(f"{a['gate']}:{a['task_content_sha256']}".encode()).hexdigest()
 if not isinstance(a["task_hash"],str) or a["task_hash"]!=expected: raise ValueError("task hash must bind gate and exact task content")
 if a["unseen_confirmation"] is not True: raise ValueError("attempt must be confirmed unseen before start")
 try: date.fromisoformat(a['actual_date'])
 except (TypeError,ValueError): raise ValueError("actual attempt date must be ISO YYYY-MM-DD")
 if a["reviewer_type"] not in {"self","assistant","peer","instructor"}: raise ValueError("reviewer type must be labeled")
 if type(a["critical_failure"]) is not bool: raise ValueError("critical failure flag must be boolean")
 return {"valid":True,"gate":a["gate"],"attempt_id":a["attempt_id"],"actual_date":a["actual_date"],
         "task_hash":a["task_hash"],"task_content_sha256":a["task_content_sha256"]}
def score_gate(attempts:list[Mapping[str,Any]])->dict[str,Any]:
 if not isinstance(attempts,list) or len(attempts)!=2: raise ValueError("each gate needs two separate attempts")
 checked=[validate_attempt(a) for a in attempts]
 if (checked[0]["gate"]!=checked[1]["gate"] or checked[0]["attempt_id"]==checked[1]["attempt_id"]
     or checked[0]["task_hash"]==checked[1]["task_hash"] or checked[0]["actual_date"]==checked[1]["actual_date"]):
  raise ValueError("same gate requires two distinct original tasks on separate actual dates")
 dimensions={"correctness","reasoning","communication","safety_integrity"}; results=[]
 for a in attempts:
  scores=a.get("scores")
  if not isinstance(scores,Mapping) or set(scores)!=dimensions or any(type(v) is not int or not 1<=v<=4 for v in scores.values()): raise ValueError("all declared dimensions need 1..4 scores")
  passed=all(v>=3 for v in scores.values()) and not a["critical_failure"]
  results.append({"attempt_id":a["attempt_id"],"scores":dict(scores),"passed":passed,"reviewer_type":a["reviewer_type"]})
 ok=all(x["passed"] for x in results)
 return {"gate":checked[0]["gate"],"attempts":results,"status":"REHEARSAL_THRESHOLD_MET" if ok else "REPAIR_REQUIRED","threshold_is_employer_cutoff":False,"professional_tenure_inferred":False}
def remediation(gates:list[Mapping[str,Any]])->dict[str,Any]:
 if not isinstance(gates,list): raise ValueError("gate reports required")
 failed=[g.get("gate") for g in gates if g.get("status")!="REHEARSAL_THRESHOLD_MET"]
 return {"failed_gates":failed,"next_action":"bounded_repair" if failed else "continue_normal_practice","passed_topics_do_not_establish_tenure":True}
def validate_formal_assessment_ledger(entries:list[Mapping[str,Any]])->dict[str,Any]:
 if not isinstance(entries,list): raise ValueError("formal assessment entries required")
 minutes=0; days=set()
 for e in entries:
  if not isinstance(e,Mapping) or not e.get("actual_date") or e.get("lane") not in {"role_bank","dsa"} or type(e.get("minutes")) is not int or e["minutes"]<=0: raise ValueError("actual date, lane and positive minutes required")
  try: date.fromisoformat(e['actual_date'])
  except (TypeError,ValueError): raise ValueError("ledger dates must be actual ISO YYYY-MM-DD dates")
  if e.get("kind")=="AWS": continue
  if e.get("kind")!="formal_interview_assessment": continue
  if e.get('lane')!='role_bank': raise ValueError("non-DSA formal assessments must use the role-bank lane")
  if e['minutes']>30: raise ValueError("formal assessment cannot exceed the 30-minute role-bank window")
  if e['actual_date'] in days: raise ValueError("at most one formal assessment entry per actual role-bank day")
  minutes+=e["minutes"]; days.add(e["actual_date"])
 return {"non_dsa_formal_minutes":minutes,"distinct_actual_days":len(days),"status":"20_HOUR_TARGET_MET" if minutes>=1200 else "ACCRUING","aws_minutes_excluded":True,"learner_attempts_verified":False}
