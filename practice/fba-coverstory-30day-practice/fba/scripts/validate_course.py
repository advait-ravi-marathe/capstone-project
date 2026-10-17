#!/usr/bin/env python3
"""Validate FBA practice, S01 coverage, and source-derived main continuation."""
import csv, json, re, hashlib
from datetime import date, timedelta
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
TOP=ROOT.parent
REPO=TOP.parent.parent
manifest=json.loads((ROOT/'SPRINT-MANIFEST.json').read_text())
assert manifest['start_date']=='2026-10-17' and manifest['end_date']=='2026-11-15'
assert manifest['days']==30 and manifest['daily_minutes']==90
assert manifest['source']['commit']=='1e569fda6a2463023098f09b0599e7eb4c775473'
assert len(manifest['roles'])==1 and manifest['roles'][0]['job_id']=='10575321' and manifest['roles'][0]['minutes']==90
assert len(manifest['assignments'])==30
for d,a in enumerate(manifest['assignments'],1):
 assert a['day']==d and a['role']=='frontend' and a['job_id']=='10575321'
 assert a['date']==(date(2026,10,17)+timedelta(days=d-1)).isoformat()
 assert a['title'] and a['coding_prompt'] and len(a['cases'])>=2
 assert a['timebox_minutes'] and sum(a['timebox_minutes'].values())==90 and a['timebox_minutes'].get('focused_component_and_adversarial_tests')==30
fixture=json.loads((ROOT/'fixtures/cases.json').read_text())
fe=ROOT/'frontend/src'
assert (fe/'ui.jsx').exists() and (fe/'assignments.js').exists()
js=(fe/'assignments.js').read_text()
assert {f'day{i:02d}' for i in range(1,31)} <= set(re.findall(r'export function (day\d{2})\(',js))
with (ROOT/'docs/source-mapping.csv').open(newline='') as f: rows=list(csv.DictReader(f))
assert len(rows)==30 and all(r['role']=='frontend' and r['job_id']=='10575321' for r in rows)
with (ROOT/'progress-template.csv').open(newline='') as f: progress=list(csv.DictReader(f))
assert len(progress)==30 and all(r['status']=='NOT_STARTED' for r in progress)
expected_s01=[40,40,42,42,44,44,46,46,48,48,50,50,165,165,52,52,53,53,54,54,55,55,56,56,57,57,166,166]
for i in range(1,31):
 d=TOP/'days'/f'day-{i:02d}.md'; assert d.exists()
 text=d.read_text(); assert f'CS90-{i:02d}' in text and f'make test-frontend-day DAY={i}' in text
 assert '**90 minutes**' in text and '**Combined planned time:** 180 minutes' in text
 assert '**Additional 30-minute FBA deliverable:**' in text
 fba_section=text.split('## FBA implementation',1)[1].split('## CoverStory practical contract',1)[0]
 assert 'CAP-' not in fba_section and 'shortlist' not in fba_section.lower() and 'AWS' not in fba_section
 assert f'frontend_{i:02d}' in fba_section and manifest['assignments'][i-1]['title'] in fba_section
 assert fixture[f'frontend_{i:02d}']['cases']==manifest['assignments'][i-1]['cases']
 if i<=28:
  match=re.search(r'saved source: \[`day-\d{3}-cap-(\d+)`\]',text,re.I); assert match
  source_id=int(match.group(1)); assert source_id==expected_s01[i-1]
  source_day={40:1,42:2,44:3,46:4,48:5,50:6,165:7,52:8,53:9,54:10,55:11,56:12,57:13,166:14}[source_id]
  assert (REPO/'docs/assignments/coverstory/saved-days'/f'day-{source_day:03d}-cap-{source_id}.md').is_file()
  assert f'Branch-local practice slot CS90-{i:02d}' in text
 else:
  assert 'authoritative active session' in text.lower()
  assert 'board contract pending' not in text.lower()
  assert ('CAP-630' in text and 'CAP-631' not in text) if i==29 else ('CAP-631' in text and 'CAP-630' not in text)
  assert ('first-14 integration' in text.lower() and 'stale' in text.lower()) if i==29 else ('first-14 integration' in text.lower() and 'rollback' in text.lower())
print('PASS: 30 FBA role assignments, case/source alignment, 30 unstarted progress rows, and 28 source-backed S01 practice mappings; days 29–30 have no invented live IDs.')
# Build expected continuation from original portable saved-source registry.
registry_path=REPO/'docs/source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json'
registry=json.loads(registry_path.read_text())
primaries=sorted((x for x in registry['cards'] if x['role']=='primary'),key=lambda x:x['day_number'])
assert len(primaries)==594
continuation=json.loads((TOP/'transition-continuation.json').read_text())
assert continuation['source_registry_sha256']==hashlib.sha256(registry_path.read_bytes()).hexdigest()
assert continuation['source_unit_at_start']==15 and continuation['source_unit_at_end']==594
assert continuation['first_overlay_date']=='2026-11-16' and continuation['last_overlay_date']=='2028-06-17'
assert continuation['count_primary_units']==580 and len(continuation['units'])==580
for offset,(unit,source) in enumerate(zip(continuation['units'],primaries[14:])):
 assert unit['source_unit']==source['day_number']==offset+15
 assert unit['primary_id']==source['id'] and unit['title']==source['title']
 assert unit['source_date']==source['date']
 assert unit['source_contract_sha256']==source['description_sha256']
 assert unit['overlay_day']==offset+1
 assert unit['overlay_date']==(date(2026,11,16)+timedelta(days=offset)).isoformat()
 assert (REPO/unit['saved_source_card']).is_file(), unit['saved_source_card']
 if unit.get('assignment_guide'): assert (REPO/unit['assignment_guide']).is_file(),unit['assignment_guide']
assert continuation['units'][0]['primary_id']=='CAP-58' and continuation['units'][-1]['primary_id']=='CAP-601'
assert (TOP/'transition-days/day-00-cap-58.md').is_file()
# Retirement is only planned after coverage of the exact original S01 set; never changes status or evidence.
transition=json.loads((TOP/'sprint-transition-plan.json').read_text())
first_sprint=[x for x in registry['cards'] if x['day_number']<=14]
assert len(first_sprint)==20 and len(transition['source_assignments'])==20
assert [x['id'] for x in transition['source_assignments']]==[x['id'] for x in first_sprint]
assert transition['scope'].startswith('planning only')
assert 'do not mark learner work done' in transition['after_coverage_check']['status_rule'].lower()
assert len(transition['practice_sessions'])==2 and [x['official_board_ticket_id'] for x in transition['practice_sessions']]==['CAP-630','CAP-631']
for assignment in transition['source_assignments']:
 source=REPO/assignment['source_card']
 assert source.is_file(), assignment['source_card']
 assert assignment['id'] in source.read_text(), (assignment['id'],assignment['source_card'])
assert [x['evidence_file'] for x in transition['practice_sessions']]==['docs/evidence/coverstory/CAP-630/result.json','docs/evidence/coverstory/CAP-631/result.json']
assert all((REPO/x['evidence_file']).is_file() for x in transition['practice_sessions'])
# The complete structured mastery contracts are saved in four source batches.
batch_dir=TOP/'appended-mastery-contract-batches'
contract_batches=[json.loads(p.read_text()) for p in sorted(batch_dir.glob('batch-*.json'))]
assert [b['batch'] for b in contract_batches]==[1,2,3,4]
assert [len(b['contracts']) for b in contract_batches]==[4,4,4,2]
received=[c for b in contract_batches for c in b['contracts']]
assert [c['planning_id'] for c in received]==[f'CSM14-{i:02d}' for i in range(1,15)]
assert [c['date'] for c in received]==[(date(2028,6,18)+timedelta(days=i-1)).isoformat() for i in range(1,15)]
for i,c in enumerate(received,1):
 assert c['role']=='primary' and c['readiness']=='PLANNED_NOT_READY' and c['evidence']=='NOT_RUN'
 assert c['paths_verified'] is False and c['proposed_paths']
 assert c['budget_minutes']=={'daily_max':180,'practical':125,'aws':30,'dsa':15,'evidence_design':10,'dsm_extra':0}
 assert c['acceptance'] and c['implementation_task'] and c['fixture'] and c['architecture_evidence']
# The appended plan is a structured-contract materialization, not a ticket-ID claim.
mastery=json.loads((TOP/'appended-mastery-sprint-14.json').read_text())
assert mastery['start_date']=='2028-06-18' and mastery['end_date']=='2028-07-01'
assert mastery['daily_budget_minutes']=={'practical':125,'aws':30,'dsa':15,'evidence_design':10}
assert len(mastery['units'])==14
for materialized,source_contract in zip(mastery['units'],received):
 assert all(materialized[k]==v for k,v in source_contract.items())
assert [c['native_issue_id'] for c in mastery['units']]==[f'CAP-{i}' for i in range(632,646)]
assert [c['issue_url'] for c in mastery['units']]==[f'https://dwait-to-adwait.youtrack.cloud/issue/CAP-{i}' for i in range(632,646)]
assert all('STARTER_SEAM_ADDED' in c['implementation_status'] for c in mastery['units'])
assert mastery['scaffold_status'].startswith('STARTER_SEAMS_ADDED')
native_map=json.loads((TOP/'appended-mastery-native-id-map.json').read_text())
assert native_map['search_total']==native_map['search_hit_count']==14
assert [x['native_issue_id'] for x in native_map['native_urls']]==[f'CAP-{i}' for i in range(632,646)]
path_map=json.loads((TOP/'IMPLEMENTATION-PATH-MAP.json').read_text())
assert len(path_map['entries'])==14
for entry in path_map['entries']:
 assert all((REPO/x['actual_path'].split('#')[0]).exists() for x in entry['paths']),entry['native_issue_id']
assert not (TOP/'proposed-extension-15.json').exists()
# Every shell directory change in daily instructions must be in a subshell.
for p in list((TOP/'days').glob('day-*.md'))+[ROOT/'assignments/frontend.md']:
 assert not re.search(r'(?m)^\s*cd\s',p.read_text()),p
assert 'required e2e acceptance' in (TOP/'days/day-28.md').read_text().lower()
print('PASS: source units 15–594 scheduled 16 Nov 2026–17 Jun 2028; S01 coverage gate; 14 contracts mapped CSM14-01…14 → CAP-632…645; all actual starter paths resolve; learner logic remains TODO.')
