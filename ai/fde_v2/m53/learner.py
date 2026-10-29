"""Learner-owned final evidence-map adapters; certification never auto-passes."""
from typing import Any,Mapping
def validate_claims(claims:list[Mapping[str,Any]],evidence:Mapping[str,Any])->dict[str,Any]: raise NotImplementedError("M5.3: link every claim to actual evidence and provenance")
def audit_role_fit(roles:list[Mapping[str,Any]],evidence:Mapping[str,Any])->dict[str,Any]: raise NotImplementedError("M5.3: compare sourced role requirements with verified evidence")
def decide_certification(record:Mapping[str,Any])->dict[str,Any]: raise NotImplementedError("M5.3: make conditional SAA decision; no booking or payment")
def build_partial_readiness(claim_report:Mapping[str,Any],role_report:Mapping[str,Any],gates:Mapping[str,Any])->dict[str,Any]: raise NotImplementedError("M5.3: preserve blockers in honest partial-readiness report")
