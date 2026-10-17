#!/usr/bin/env python3
"""Validate date continuity, role scope, source maps, and the paired task manifest."""
import csv, json, re
from datetime import date, timedelta
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
TOP=ROOT.parent
manifest=json.loads((ROOT/'SPRINT-MANIFEST.json').read_text())
assert manifest['start_date']=='2026-10-17' and manifest['end_date']=='2026-11-15'
assert manifest['days']==30 and manifest['daily_minutes']==60
assert manifest['source']['commit']=='1e569fda6a2463023098f09b0599e7eb4c775473'
assert len(manifest['roles'])==1 and manifest['roles'][0]['job_id']=='10575321'
assert len(manifest['assignments'])==30
for d,a in enumerate(manifest['assignments'],1):
 assert a['day']==d and a['role']=='frontend' and a['job_id']=='10575321'
 assert a['date']==(date(2026,10,17)+timedelta(days=d-1)).isoformat()
 assert a['title'] and a['coding_prompt'] and len(a['cases'])>=2
 assert a['timebox_minutes'] and sum(a['timebox_minutes'].values())==60
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
 # Paired original CoverStory source card and referenced acceptance harness must exist.
 match=re.search(r'saved source seam CAP-(\d+)',text); assert match
 source_id=int(match.group(1)); idx=[40,42,44,46,48,50,165,52,53,54,55,56,57,166,58][(i-1)//2]
 assert source_id==idx
 test_paths=re.findall(r'(?:instructor|learner) `([^`]+)`',text); assert len(test_paths)==2
 for test_path in test_paths: assert (TOP.parent.parent/test_path).is_file(), test_path
 daynum=[40,42,44,46,48,50,165,52,53,54,55,56,57,166,58].index(idx)+1
 assert (TOP.parent.parent/'docs/assignments/coverstory/saved-days'/f'day-{daynum:03d}-cap-{idx}.md').is_file()
print('PASS: 30 consecutive dates; 30 FBA assignments for job 10575321; 30 paired CoverStory cards and source references; 30 NOT_STARTED progress rows; frontend task functions and source maps present.')

# The Nov 16 transition adds 15 dated, path-backed units without changing learner state.
source_ids=[59,60,61,62,63,64,65,66,67,68,69,70,71,72,73]
for i,cap in enumerate(source_ids,1):
 guide=TOP/'transition-days'/f'day-{i:02d}-cap-{cap}.md'; assert guide.is_file()
 text=guide.read_text(); assert f'CAP-{cap}' in text and '150 practical + 30 AWS' in text
 paths=re.findall(r'`((?:ai|src)/[^`]+\.(?:py|java))`',text)
 assert len(paths)>=2
 for path in paths: assert (TOP.parent.parent/path).exists(), path
print('PASS: transition has 15 concrete source-mapped units and all cited harness/source files exist.')
