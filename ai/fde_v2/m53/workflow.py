"""Private evidence-review plumbing; facts and claim truth stay learner-owned."""
from . import reference as application_adapter
def review(claims,evidence,roles,certification,gates,*,adapter=application_adapter):
 c=adapter.validate_claims(claims,evidence); r=adapter.audit_role_fit(roles,evidence); d=adapter.decide_certification(certification)
 return {"claims":c,"role_fit":r,"certification":d,"readiness":adapter.build_partial_readiness(c,r,gates),"external_actions":False}
