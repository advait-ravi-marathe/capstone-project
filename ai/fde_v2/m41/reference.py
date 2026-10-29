"""Reference mechanics for M4.1. Fixtures exercise schema, not real users or evaluator judgment."""
from __future__ import annotations
from typing import Any, Mapping
import re

REQUIRED_TOPICS={"workflow","stakeholders","data_access","baseline","risks","success_measures"}
ALLOWED_EVIDENCE={"role_play","self_review","assistant_review","peer","real_user"}
ALLOWED_DECISIONS={"pilot","non_ai","no_go","need_more_information"}
ALLOWED_CLAIM_TYPES={"limitation","proposed_measure","synthetic_observation"}

def _check_unfounded_claim(text: str) -> None:
    pattern=r"\b(customer|customers|client|clients)\b[^.!?\n]{0,80}\b(identity|name|history|in production|using|adopt(?:ion|ed)?|saving|savings|endorse(?:ment|d)?)\b"
    reverse=r"\b(identity|name|history|adopt(?:ion|ed)?|saving|savings|endorse(?:ment|d)?)\b[^.!?\n]{0,80}\b(customer|customers|client|clients)\b"
    if re.search(pattern,text,re.I) or re.search(reverse,text,re.I):
        raise ValueError("unsupported customer identity, adoption, savings, endorsement or production claim")

def validate_discovery(record: Mapping[str, Any]) -> dict[str, Any]:
    if not isinstance(record, Mapping): raise ValueError("discovery record must be a mapping")
    questions=record.get("questions")
    if not isinstance(questions,list) or not questions: raise ValueError("questions must precede design")
    if any(not isinstance(q,Mapping) or q.get("topic") not in REQUIRED_TOPICS or not isinstance(q.get("question"),str) or not q["question"].strip() for q in questions):
        raise ValueError("question topic or text is invalid")
    topics={q["topic"] for q in questions}; missing=sorted(REQUIRED_TOPICS-topics)
    evidence=record.get("evidence_class")
    if evidence not in ALLOWED_EVIDENCE: raise ValueError("evidence class must be explicit")
    decision=record.get("decision")
    if decision not in ALLOWED_DECISIONS: raise ValueError("decision must allow pilot, non-AI or no-go")
    if record.get("solution_designed_before_questions") is not False: raise ValueError("questions must precede solution design")
    notes=record.get("notes")
    if not isinstance(notes,list) or not notes or any(not isinstance(n,Mapping) or n.get("topic") not in REQUIRED_TOPICS or not isinstance(n.get("answer"),str) or not n["answer"].strip() for n in notes):
        raise ValueError("discovery notes need observed answers tied to the question topics")
    missing_answers=sorted(REQUIRED_TOPICS-{n["topic"] for n in notes})
    if decision in {"non_ai","no_go","need_more_information"} and (not isinstance(record.get("decision_rationale"),str) or not record["decision_rationale"].strip()):
        raise ValueError("non-pilot decision needs a rationale")
    if decision in {"non_ai","no_go","need_more_information"}: _check_unfounded_claim(record["decision_rationale"])
    if decision=="pilot":
        required={"pilot_scope","non_goals","responsibilities","acceptance_criteria","rollback_criteria","customer_explanation"}
        if not required.issubset(record): raise ValueError("pilot contract fields are incomplete")
        if any(not isinstance(record[k],str) or not record[k].strip() for k in required-{"non_goals"}): raise ValueError("pilot contract text fields must be nonempty")
        if not isinstance(record["non_goals"],list) or not record["non_goals"] or any(not isinstance(x,str) or not x.strip() for x in record["non_goals"]): raise ValueError("pilot non-goals must be explicit")
        for key in ("pilot_scope","responsibilities","acceptance_criteria","rollback_criteria","customer_explanation"):
            _check_unfounded_claim(record[key])
    if evidence=="real_user" and (record.get("authorized_contact") is not True or not record.get("evidence_ref")):
        raise ValueError("real-user evidence needs actual authorization and evidence reference")
    claims=record.get("claims",[])
    if not isinstance(claims,list): raise ValueError("claims must be a list")
    for claim in claims:
        if not isinstance(claim,Mapping) or set(claim)!={"claim_type","text"} or claim.get("claim_type") not in ALLOWED_CLAIM_TYPES or not isinstance(claim.get("text"),str) or not claim["text"].strip():
            raise ValueError("claims must use a constrained type and nonempty text")
        _check_unfounded_claim(claim["text"])
    missing_all=sorted(set(missing+missing_answers))
    return {"valid":not missing_all,"missing_topics":missing,"missing_answers":missing_answers,"evidence_class":evidence,"decision":decision,
            "real_user_experience":"REVIEW_REQUIRED" if evidence=="real_user" and not missing_all else "NOT_EVIDENCED",
            "status":"READY_FOR_EVALUATOR" if not missing_all else "INCOMPLETE"}

def assess_rubric(evaluation: Mapping[str, Any]) -> dict[str, Any]:
    if not isinstance(evaluation,Mapping): raise ValueError("evaluator record required")
    kind=evaluation.get("evaluator_kind"); ref=evaluation.get("evaluator_ref")
    if kind not in {"self_review","assistant_review","peer","real_user"} or not isinstance(ref,str) or not ref.strip():
        return {"status":"BLOCKED","reason":"evaluator identity/type and receipt are absent","score":None}
    scores=evaluation.get("scores"); expected={"questions_first","baseline","data_access","risks","success_criteria","non_ai_or_no_go"}
    if not isinstance(scores,Mapping) or set(scores)!=expected: raise ValueError("rubric must include each declared criterion exactly once")
    if any(not isinstance(v,int) or isinstance(v,bool) or not 1<=v<=5 for v in scores.values()):
        raise ValueError("rubric scores must be integers from 1 to 5")
    if not isinstance(evaluation.get("evidence_ref"),str) or not evaluation["evidence_ref"].strip():
        return {"status":"BLOCKED","reason":"evaluator evidence reference is absent","score":None}
    total=sum(scores.values()); threshold=evaluation.get("minimum_total",24)
    if not isinstance(threshold,int) or isinstance(threshold,bool) or not 6<=threshold<=30: raise ValueError("invalid predeclared rubric threshold")
    return {"status":"UNVERIFIED_SCORE","evaluator_kind":kind,"evaluator_ref":ref,"evidence_ref":evaluation["evidence_ref"],
            "score":total,"threshold":threshold,"exercise_result":"MEETS_EXERCISE_RUBRIC" if total>=threshold else "BELOW_EXERCISE_RUBRIC",
            "evaluator_verification":"BLOCKED","real_customer_experience":"BLOCKED"}

def build_pilot_contract(discovery: Mapping[str, Any], assessment: Mapping[str, Any]) -> dict[str, Any]:
    d=validate_discovery(discovery)
    if not d["valid"]: return {"status":"BLOCKED","reason":"discovery questions are incomplete"}
    decision=discovery["decision"]
    # Role-play is an actionable local exercise even without a customer. Its
    # status never implies customer evidence or trusted evaluator verification.
    if discovery["evidence_class"] == "role_play":
        return {"status":"ROLEPLAY_EXERCISE_AVAILABLE",
            "reason":"local practice only; evaluator and customer evidence remain unverified",
            "decision":decision,"scope":discovery.get("pilot_scope") if decision=="pilot" else None,
            "non_goals":discovery.get("non_goals",[]),"responsibilities":discovery.get("responsibilities") if decision=="pilot" else None,
            "acceptance_criteria":discovery.get("acceptance_criteria") if decision=="pilot" else None,
            "rollback_criteria":discovery.get("rollback_criteria") if decision=="pilot" else None,
            "customer_explanation":discovery.get("customer_explanation") if decision=="pilot" else None,
            "evidence_class":discovery["evidence_class"],
            "real_user_gate":"BLOCKED",
            "rubric_status":assessment.get("status","NOT_ASSESSED") if isinstance(assessment,Mapping) else "NOT_ASSESSED",
            "evaluator_verification":"BLOCKED",
            "decision_rationale":discovery.get("decision_rationale") if decision!="pilot" else None,
            "claims_permitted":False}
    # Non-role-play evidence still requires a trusted evaluation path. A caller's
    # JSON fields or self-asserted verified flag are not verification.
    return {"status":"BLOCKED","reason":"no trusted actual rubric evaluator integration is available",
            "decision":decision,"scope":discovery.get("pilot_scope") if decision=="pilot" else None,
            "non_goals":discovery.get("non_goals",[]),"responsibilities":discovery.get("responsibilities") if decision=="pilot" else None,
            "acceptance_criteria":discovery.get("acceptance_criteria") if decision=="pilot" else None,
            "rollback_criteria":discovery.get("rollback_criteria") if decision=="pilot" else None,
            "customer_explanation":discovery.get("customer_explanation") if decision=="pilot" else None,
            "evidence_class":discovery["evidence_class"],"real_user_gate":"REVIEW_REQUIRED" if discovery["evidence_class"]=="real_user" else "BLOCKED",
            "decision_rationale":discovery.get("decision_rationale") if decision!="pilot" else None,"claims_permitted":False}
