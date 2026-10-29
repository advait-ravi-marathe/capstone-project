"""Schema/provenance mechanics. This module does not grant credentials or verify mastery."""
from __future__ import annotations
from typing import Any,Mapping
LEVELS={"local","hosted_provider","live_cloud","real_user","production","organizational_impact"}
ROLE_CATEGORIES={"technical","experience","location","work_authorization","travel","customer_production"}
FORBIDDEN_SCAFFOLD_SOURCES={"starter","scaffold","elapsed_day","course_completion","assistant_authored"}
def validate_claims(claims:list[Mapping[str,Any]],evidence:Mapping[str,Any])->dict[str,Any]:
 if not isinstance(claims,list) or not isinstance(evidence,Mapping): raise ValueError("claims and evidence map required")
 issues=[]
 for i,c in enumerate(claims):
  if not isinstance(c,Mapping) or not isinstance(c.get("claim"),str) or not c["claim"].strip() or c.get("level") not in LEVELS: raise ValueError("claim text and explicit capability level required")
  refs=c.get("evidence_refs")
  if not isinstance(refs,list) or not refs: issues.append({"index":i,"reason":"no linked evidence"}); continue
  for ref in refs:
   row=evidence.get(ref)
   if not isinstance(row,Mapping) or not row.get("sha256") or not row.get("actual_contributor") or not row.get("environment") or row.get("status")!="verified":
    issues.append({"index":i,"reason":"evidence provenance is missing or unverified"}); break
   if row.get('level')!=c['level']:
    issues.append({"index":i,"reason":"evidence capability level does not support the claim level"}); break
   if row.get("source_kind") in FORBIDDEN_SCAFFOLD_SOURCES:
    issues.append({"index":i,"reason":"scaffolds, elapsed days or course completion cannot prove capability"}); break
 return {"status":"SCHEMA_COMPLETE_UNVERIFIED" if not issues else "INCOMPLETE","issues":issues,"claims_checked":len(claims),"professional_tenure_inferred":False,"trusted_evidence_registry_available":False}
def audit_role_fit(roles:list[Mapping[str,Any]],evidence:Mapping[str,Any])->dict[str,Any]:
 if not isinstance(roles,list): raise ValueError("role list required")
 rows=[]
 for r in roles:
  if not isinstance(r,Mapping) or not r.get("role_id") or not r.get("official_source") or not r.get("observed_date"): raise ValueError("role requirement source/date required")
  req=r.get("requirements",[])
  if not isinstance(req,list): raise ValueError("requirements must be a list")
  categories=set()
  matched=[]; unknown=[]
  for item in req:
   if not isinstance(item,Mapping) or not item.get("requirement"): raise ValueError("each requirement needs a text label")
   if item.get('category') not in ROLE_CATEGORIES: raise ValueError("role matrix must type technical, experience, location, work authorization, travel and customer-production requirements")
   categories.add(item['category'])
   ref=item.get("evidence_ref"); row=evidence.get(ref) if ref else None
   (matched if isinstance(row,Mapping) and row.get("status")=="verified" and row.get("sha256") else unknown).append(item["requirement"])
  missing=sorted(ROLE_CATEGORIES-categories)
  if missing: raise ValueError(f"role matrix is missing requirement categories: {missing}")
  rows.append({"role_id":r["role_id"],"source":r["official_source"],"observed_date":r["observed_date"],"supported_requirements":matched,"gaps_or_unknowns":unknown,"categories":sorted(categories),"application_sent":False})
 return {"roles":rows,"status":"EVIDENCE_MAP_ONLY","work_authorization_inferred":False,"applications_sent":False}
def decide_certification(record:Mapping[str,Any])->dict[str,Any]:
 if not isinstance(record,Mapping): raise ValueError("decision record required")
 choice=record.get("decision")
 if choice not in {"pursue_later","defer","not_useful_now"}: raise ValueError("choose conditional decision without booking")
 reasons=record.get("evidence_refs",[])
 return {"decision":choice,"status":"DRAFT_REQUIRES_INDEPENDENT_READINESS_REVIEW","evidence_refs":reasons,"exam_booked":False,"payment_authorized":False,"SAA_readiness_inferred":False,"AIP_is_later_experience_target":True}
def build_partial_readiness(claim_report:Mapping[str,Any],role_report:Mapping[str,Any],gates:Mapping[str,Any])->dict[str,Any]:
 blockers=[k for k,v in gates.items() if v!="PASS"]
 return {"status":"PARTIAL_READINESS_GAPS_REMAIN" if blockers or claim_report.get("status")!="SCHEMA_COMPLETE_UNVERIFIED" else "REQUIRES_INDEPENDENT_EVIDENCE_REVIEW","blocked_gates":blockers,"claims_status":claim_report.get("status"),"role_map_status":role_report.get("status"),"universal_fde_ready_claim":False,"public_posting":False}
