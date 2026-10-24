"""Instructor-only reference behavior for Days 267–270 acceptance tests.

This file is intentionally outside ai/grounded learner modules and must never
be cited as learner work or completion evidence.
"""
from __future__ import annotations
import hashlib, json, re, subprocess
from datetime import date
from pathlib import Path
from urllib.parse import urlparse
from ai.grounded.cert_readiness_267 import Readiness

HEX40=re.compile(r"^[0-9a-f]{40}$")

def build_receipt(root:Path,source_sha:str,paths:tuple[str,...])->dict[str,object]:
    root=root.resolve()
    if not isinstance(source_sha,str) or HEX40.fullmatch(source_sha) is None:
        raise ValueError("source_sha must be a 40-character lowercase commit SHA")
    current=subprocess.run(["git","-C",str(root),"rev-parse","HEAD"],check=True,capture_output=True,text=True).stdout.strip()
    if source_sha!=current: raise ValueError("source_sha must identify the checked-out source commit")
    if not paths or len(paths)!=len(set(paths)): raise ValueError("paths must be non-empty and unique")
    rows=[]
    for raw_path in paths:
        if not isinstance(raw_path,str) or not raw_path or Path(raw_path).is_absolute():
            raise ValueError("artifact paths must be non-empty repository-relative paths")
        candidate=root/raw_path
        parts=Path(raw_path).parts
        if ".." in parts:
            raise ValueError(f"artifact is missing or unsafe: {raw_path}")
        # Check each component, not only the leaf: a symlinked parent can
        # redirect an apparently ordinary file outside the evidence root.
        cursor=root
        for part in parts:
            cursor=cursor/part
            if cursor.is_symlink():
                raise ValueError(f"artifact path contains a symlink: {raw_path}")
        if not candidate.is_file():
            raise ValueError(f"artifact is missing or unsafe: {raw_path}")
        resolved=candidate.resolve()
        if root not in resolved.parents: raise ValueError(f"artifact escapes repository: {raw_path}")
        committed=subprocess.run(["git","-C",str(root),"show",f"{source_sha}:{raw_path}"],check=True,capture_output=True).stdout
        if committed!=candidate.read_bytes(): raise ValueError(f"artifact bytes differ from the selected source commit: {raw_path}")
        rows.append({"path":raw_path,"sha256":hashlib.sha256(committed).hexdigest()})
    canonical=json.dumps(rows,sort_keys=True,separators=(",",":"),ensure_ascii=False).encode()
    return {"source_sha":source_sha,"artifacts":rows,
            "artifact_manifest_sha256":hashlib.sha256(canonical).hexdigest(),
            "portfolio_status":"local_artifacts_receipted","learner_completion_awarded":False,
            "professional_ownership_claimed":False}


def review_readiness(record:dict,current_guide:dict)->Readiness:
    reasons=[]
    if not isinstance(record,dict) or not isinstance(current_guide,dict): return Readiness("blocked",("record_invalid",),0)
    if (record.get("guide_title")!=current_guide.get("title") or record.get("guide_version")!=current_guide.get("version") or
        record.get("guide_checked_on")!=current_guide.get("checked_on") or record.get("domains")!=current_guide.get("domains")):
        reasons.append("guide_mismatch")
    if (type(record.get("full_length_official_exam_available")) is not bool or
        not isinstance(record.get("full_length_availability_note"),str) or not record["full_length_availability_note"].strip()):
        reasons.append("full_length_availability_unverified")
    attempts=record.get("attempts"); domains=current_guide.get("domains")
    if (not isinstance(attempts,list) or not isinstance(domains,list) or not domains or
        any(not isinstance(x,str) or not x.strip() for x in domains) or len(set(domains))!=len(domains)):
        return Readiness("blocked",tuple(reasons+["record_invalid"]),0)
    ids=set(); passed=0; full_length=0
    for attempt in attempts:
        if not isinstance(attempt,dict): reasons.append("attempt_invalid"); continue
        aid=attempt.get("attempt_id")
        if not isinstance(aid,str) or not aid.strip() or aid in ids: reasons.append("attempt_not_distinct")
        else: ids.add(aid)
        url=attempt.get("material_url"); host=urlparse(url).hostname if isinstance(url,str) else None
        if (attempt.get("material_type")!="current_official_practice" or
            not isinstance(attempt.get("material_title"),str) or not attempt["material_title"].strip() or
            not isinstance(url,str) or not url.startswith("https://") or host not in {"aws.amazon.com","skillbuilder.aws"} or
            attempt.get("timed") is not True): reasons.append("practice_source_or_timing_unverified")
        if type(attempt.get("full_length_official")) is not bool: reasons.append("full_length_flag_invalid")
        elif attempt["full_length_official"]: full_length+=1
        try:
            if date.fromisoformat(attempt["attempted_on"])<date.fromisoformat(record["guide_checked_on"]):
                reasons.append("attempt_predates_guide_check")
        except (KeyError,TypeError,ValueError): reasons.append("assessment_date_invalid")
        correct,total=attempt.get("correct"),attempt.get("total")
        if type(correct) is not int or type(total) is not int or total<=0 or correct<0 or correct>total:
            reasons.append("score_invalid")
        elif correct*100<80*total: reasons.append("assessment_below_threshold")
        else: passed+=1
        missed,reviewed=attempt.get("missed_topics"),attempt.get("reviewed_missed_topics")
        if (not isinstance(missed,list) or not isinstance(reviewed,list) or
            any(not isinstance(x,str) for x in missed+reviewed) or sorted(missed)!=sorted(reviewed)):
            reasons.append("misses_not_fully_reviewed")
        scores=attempt.get("domain_scores")
        if scores is not None:
            if not isinstance(scores,dict) or set(scores)!=set(domains): reasons.append("domain_diagnostic_incomplete")
            elif any(type(scores[d]) is not int or scores[d]<0 or scores[d]>100 for d in domains):
                reasons.append("domain_diagnostic_score_invalid")
            elif any(scores[d]<70 for d in domains): reasons.append("domain_below_threshold")
        if attempt.get("unstudied_domains")!=[]: reasons.append("unstudied_domains_remain")
    if len(attempts)<2 or len(ids)<2: reasons.append("two_distinct_assessments_required")
    if passed<2: reasons.append("two_passing_assessments_required")
    if record.get("full_length_official_exam_available") is True and full_length==0:
        reasons.append("full_length_assessment_missing")
    reasons=list(dict.fromkeys(reasons))
    return Readiness("ready_for_separate_booking_review" if not reasons else "not_ready",tuple(reasons),len(attempts),False,False)
