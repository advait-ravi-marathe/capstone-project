#!/usr/bin/env python3
"""Validate date continuity, role scope, source maps, and the paired task manifest."""
import csv, json, re
from datetime import date, timedelta
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
TOP=ROOT.parent
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
for i in range(1,31):
 d=TOP/'days'/f'day-{i:02d}.md'; assert d.exists()
 text=d.read_text(); assert f'CAP-{601+i}' in text and f'make test-frontend-day DAY={i}' in text
 assert '**90 minutes**' in text and '**Combined planned time:** 180 minutes' in text
 assert '**Additional 30-minute FBA deliverable:**' in text
 fba_section=text.split('## FBA implementation',1)[1].split('## CoverStory practical contract',1)[0]
 assert 'CAP-' not in fba_section and 'shortlist' not in fba_section.lower() and 'AWS' not in fba_section
 assert f'frontend_{i:02d}' in fba_section and manifest['assignments'][i-1]['title'] in fba_section
 assert fixture[f'frontend_{i:02d}']['cases']==manifest['assignments'][i-1]['cases']
 # Paired original CoverStory source card and referenced acceptance harness must exist.
 match=re.search(r'saved primary CAP-(\d+)',text); assert match
 source_id=int(match.group(1)); idx=[40,40,42,42,44,44,46,46,48,48,50,50,165,165,52,52,53,53,54,54,55,55,56,56,57,57,166,166,58,58][i-1]
 assert source_id==idx
 test_paths=re.findall(r'`((?:src|ai|web)/[^`]+)`',text)
 assert len(test_paths)>=2, (d,test_paths)
 for test_path in test_paths: assert (TOP.parent.parent/test_path.split('::',1)[0]).is_file(), test_path
 daynum={40:1,42:2,44:3,46:4,48:5,50:6,165:7,52:8,53:9,54:10,55:11,56:12,57:13,166:14,58:15}[idx]
 assert (TOP.parent.parent/'docs/assignments/coverstory/saved-days'/f'day-{daynum:03d}-cap-{idx}.md').is_file()
print('PASS: 30 consecutive dates; 30 FBA assignments for job 10575321; 30 paired CoverStory cards and source references; 30 NOT_STARTED progress rows; frontend task functions and source maps present.')

# Original roadmap continues in source order; proposed extension is separate and append-only.
continuation=json.loads((TOP/'transition-continuation.json').read_text())
assert [x['primary_id'] for x in continuation['units']]==[59,60,61,62,63,167,64,65,66,67,68,69,168,70,71,72,73,74,75,169]
assert [x['overlay_date'] for x in continuation['units']]==[(date(2026,11,16)+timedelta(days=n)).isoformat() for n in range(20)]
for unit in continuation['units']:
 assert (TOP.parent.parent/unit['assignment_guide']).is_file(), unit['assignment_guide']
 assert (TOP.parent.parent/unit['saved_source_card']).is_file(), unit['saved_source_card']
 assert unit.get('learner_test_kind')=='learner acceptance'
 learner_test=unit.get('focused_learner_test'); assert learner_test and (TOP.parent.parent/learner_test).is_file(), learner_test
 assert 'infrastructure' not in learner_test.lower() and 'Instructor' not in learner_test
ext=json.loads((TOP/'proposed-extension-15.json').read_text())
assert len(ext['units'])==15 and ext['units'][0]['id']=='CAP-632' and ext['units'][-1]['id']=='CAP-646'
assert ext['units'][0]['date']=='2028-05-30' and ext['units'][-1]['date']=='2028-06-13'
assert all('proposed new content' in x['status'] and x['task'] and x['acceptance'] for x in ext['units'])
# Every shell directory change in daily instructions must be a subshell so later root commands still work.
for p in list((TOP/'days').glob('day-*.md'))+[ROOT/'assignments/frontend.md']:
 text=p.read_text(); assert not re.search(r'(?m)^\s*cd\s',text), p
day28=(TOP/'days/day-28.md').read_text().lower()
assert 'required e2e acceptance' in day28 and 'e2e acceptance below' not in day28
print('PASS: original roadmap continuation order and separate 15-card proposed extension.')
