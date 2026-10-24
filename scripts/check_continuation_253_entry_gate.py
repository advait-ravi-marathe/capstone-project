#!/usr/bin/env python3
"""Dynamic eligibility for Days 253–266 using the actual Day 252 closeout record.

This composes the existing Day 210/238 evidence gate with Day 252's assigned
learner record. It introduces no completion credit and never creates evidence.
"""
from __future__ import annotations
import hashlib,json,re,sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
sys.path.insert(0,str(ROOT/'scripts'))
import check_continuation_239_entry_gate as prior_gate
DAY252=ROOT/'evidence/continuation-239-252/day-252.json'
HEX40=re.compile(r'^[0-9a-f]{40}$'); HEX64=re.compile(r'^[0-9a-f]{64}$')
DAY252_SELECTOR='ai.grounded.learner_acceptance.test_continuation_239.Continuation239Acceptance.test_day252_reports_known_blind_spots_without_claiming_real_customer_outcomes'
REQUIRED_PATHS={'ai/grounded/continuation_239.py','ai/grounded/learner_acceptance/test_continuation_239.py'}

def valid_day252(row: object) -> bool:
    if not isinstance(row,dict):return False
    paths=row.get('paths'); output=row.get('output'); command=row.get('command')
    return (row.get('day')==252 and isinstance(row.get('source_sha'),str) and HEX40.fullmatch(row['source_sha']) is not None and
        isinstance(row.get('artifact_sha256'),str) and HEX64.fullmatch(row['artifact_sha256']) is not None and
        isinstance(row.get('status'),str) and row['status'].strip().casefold() in {'pass','passed','completed'} and
        isinstance(command,str) and DAY252_SELECTOR in command and
        isinstance(output,str) and 'Ran 1 test' in output and output.rstrip().endswith('OK') and
        isinstance(paths,list) and all(isinstance(x,str) for x in paths) and REQUIRED_PATHS.issubset(set(paths)) and
        isinstance(row.get('assistance'),str) and isinstance(row.get('blockers'),str) and
        isinstance(row.get('next_step'),str) and bool(row['next_step'].strip()))

def check(*,prior_result:dict|None=None,record_path:Path=DAY252)->dict:
    prior=prior_result if prior_result is not None else prior_gate.check()
    prior_status=prior.get('status')
    if prior_status!='eligible_to_attempt':
        return {'status':'blocked' if prior_status=='blocked' else 'not_met',
            'reason':'existing Day 210/238 entry gate is not eligible; follow its reported recovery selectors',
            'prior_gate':prior,'day252_record_sha256':None,'eligibility_only':True,'completion_awarded':False}
    if not record_path.is_file():
        return {'status':'not_met','reason':'the assigned learner-authored Day 252 closeout record is absent; run the Day 252 selector and record its actual result before Days 253–266',
            'prior_gate':prior,'day252_record_sha256':None,'eligibility_only':True,'completion_awarded':False}
    raw=record_path.read_bytes()
    try:row=json.loads(raw)
    except (UnicodeDecodeError,json.JSONDecodeError):row=None
    if not valid_day252(row):
        return {'status':'blocked','reason':'Day 252 closeout record does not match its assigned selector/output and evidence fields',
            'prior_gate':prior,'day252_record_sha256':hashlib.sha256(raw).hexdigest(),'eligibility_only':True,'completion_awarded':False}
    return {'status':'eligible_to_attempt','reason':'existing Day 210/238 prerequisites and assigned Day 252 learner closeout are recorded',
        'prior_gate':prior,'day252_record_sha256':hashlib.sha256(raw).hexdigest(),'eligibility_only':True,'completion_awarded':False}

if __name__=='__main__':
    print(json.dumps(check(),sort_keys=True));sys.exit(0)
