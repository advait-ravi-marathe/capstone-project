#!/usr/bin/env python3
"""Validate Days 197–210 canonical/owner exports, minutes, notes and contracts."""
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]; B=ROOT/'docs/reboot/continuation-197-210'
m=json.loads((B/'manifest.json').read_text());o=json.loads((B/'scrum-owner-manifest.json').read_text());p=json.loads((B/'days-197-210-transfer-plan.json').read_text()); notes=(B/'assistant-architecture-notes.md').read_text()
assert [d['day'] for d in m['days']]==list(range(197,211))
assert [d['day_number'] for d in o['cards']]==list(range(197,211))
assert [d['day'] for d in p['days']]==list(range(197,211))
totals={}
for d,oc,pd in zip(m['days'],o['cards'],p['days']):
 a=d['workstream_minutes']; assert sum(a.values())==165,(d['day'],a)
 assert d['budget']['workstream_minutes']==a
 assert d['budget']['engineering_minutes']==165
 assert d['budget']['study']==a['certification_core_concepts_study_minutes']
 assert d['budget']['practice_and_tests']==a['coverstory_project_recovery_minutes']+a['offline_ai_implementation_evaluation_minutes']+a['certification_offline_practice_minutes']
 for k,v in a.items():totals[k]=totals.get(k,0)+v
 for exp in (oc,pd):
  assert exp['workstream_minutes']==a,(d['day'],exp.get('day_number',exp.get('day')))
  assert exp['title']==d['title'] and exp['selectors']==d['selectors']
  assert exp['study_refs']==d['study_refs'] and exp['study_guidance']==d['study_guidance']
  assert exp['engineering_architecture']==d['engineering_architecture']
 assert oc['engineering']['allocation']==a
 assert oc['interview_practice']==d['shared_interview_block']
 assert pd['budget']['allocation']==a
 assert d['selectors'] and all(c in (B/'contracts'/f'day-{d["day"]}.md').read_text() for c in d['selectors'])
 contract=(B/'contracts'/f'day-{d["day"]}.md').read_text();assert d['title'] in contract and all(str(v) in contract for v in a.values())
 sb=d['shared_interview_block'];assert sb['minutes']==45 and sum(sb['allocation_minutes'].values())==45
 assert (sb['question_id'] and sb['question_title']) or (sb.get('review_task') and sb['mode']=='unfinished_review')
 anchor=f'<a id="day-{d["day"]}"></a>'; assert anchor in notes
 section=notes.split(anchor,1)[1].split('\n<a id=',1)[0]
 assert d['title'] in section and d['engineering_architecture']['decision'] in section
assert totals=={'coverstory_project_recovery_minutes':1380,'offline_ai_implementation_evaluation_minutes':180,'certification_core_concepts_study_minutes':490,'certification_offline_practice_minutes':50,'evidence_minutes':210},totals
assert m['engineering_allocation']['engineering_minutes_total']==sum(totals.values())==2310
assert {k:v for k,v in m['engineering_allocation'].items() if k!='engineering_minutes_total'}==totals
assert m['engineering_allocation']==totals|{'engineering_minutes_total':2310}
for d in m['days']:
 if d['day'] in (203,209):
  assert d['selectors'][:2]==['scripts/package-sprint5-demo.sh','python3 scripts/check_sprint5_package_evidence.py']
  assert 'LocalPackagedBrowserAcceptanceTest' in d['selectors'][2]
print('allocation, dates, selectors, study sources, notes, shared sessions and all exports agree')
