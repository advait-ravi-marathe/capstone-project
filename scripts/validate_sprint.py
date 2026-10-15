#!/usr/bin/env python3
"""Offline structural integrity check for the 60-card interview sprint."""
import csv
import json
import re
from datetime import date, timedelta
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
manifest = json.loads((ROOT / 'SPRINT-MANIFEST.json').read_text())
fixtures = json.loads((ROOT / 'fixtures/cases.json').read_text())
assert manifest['start_date'] == '2026-10-16' and manifest['end_date'] == '2026-11-14'
assert manifest['daily_minutes'] == 120 and manifest['daily_unallocated_minutes'] == 30
assert len(manifest['assignments']) == 60
expected_dates = [(date(2026, 10, 16) + timedelta(days=i)).isoformat() for i in range(30)]
assert expected_dates[-1] == '2026-11-14'
java = (ROOT / 'sde/src/main/java/amazon/sprint/LearnerExercises.java').read_text()
interface = (ROOT / 'sde/src/main/java/amazon/sprint/ExerciseSet.java').read_text()
js = (ROOT / 'frontend/src/assignments.js').read_text()
ui = (ROOT / 'frontend/src/ui.jsx').read_text()
methods = set(re.findall(r'^\s{4}(?!record\b)[^\n]+\s+(\w+)\s*\([^;\n]*\);$', interface, re.M))
implementation_methods = set(re.findall(r'^\s*public\s+[\w<>?, .]+\s+(\w+)\s*\([^;]*\)\s*\{', java, re.M))
assert methods == implementation_methods, f'Java contract mismatch: missing {methods-implementation_methods}, extra {implementation_methods-methods}'
assert len(re.findall(r'throw todo\(\d+\)', java)) == 29
assert len(re.findall(r'export function day\d{2}\(', js)) == 30
assert len(re.findall(r'Learner TODO: Day \d{2}', js)) == 30
assert 'function TaskBody(' in ui and 'exerciseFunctions[`day${String(day).padStart(2, \'0\')}`]' in ui
assert 'JSON input' not in ui and 'StructuredTask' not in ui
assert 'AbortController' in ui and 'aria-controls' in ui and 'role="tablist"' in ui
assert 'attempts:4,base:100,cap:800' in ui and 'setTimeout' in ui
assert 'highest request sequence' in next(a['coding_prompt'] for a in manifest['assignments'] if a['role']=='frontend' and a['day']==17)
assert (ROOT / 'sde/sql/day19.sql').is_file() and (ROOT / 'scripts/test_day19_sql.py').is_file()
assert 'created_at descending, then event_id descending' in next(a['coding_prompt'] for a in manifest['assignments'] if a['role']=='sde' and a['day']==19)
assert (ROOT / 'docs/design-star-rubrics.md').is_file()
assert len(fixtures) == 60
for day, d in enumerate(expected_dates, 1):
    cards = [a for a in manifest['assignments'] if a['day'] == day]
    assert len(cards) == 2 and {a['date'] for a in cards} == {d}
    assert {a['role'] for a in cards} == {'sde', 'frontend'}
    assert {a['job_id'] for a in cards} == {'10579529', '10575321'}
    for a in cards:
        assert sum(a['timebox_minutes'].values()) == 60
        assert all(a[k].strip() for k in ('title','topic','coding_prompt','design_prompt','leadership_principle'))
        assert len(a['cases']) >= 2
        key=f"{a['role']}_{day:02d}"
        assert fixtures[key]['cases'] == a['cases']
        assert len(a['cases']) == len(fixtures[key]['cases'])
for guide in ('assignments/sde.md','assignments/frontend.md'):
    text=(ROOT/guide).read_text()
    assert text.count('## Day ') == 30
    assert '60-minute' in text and 'STAR' in text
with (ROOT/'progress-template.csv').open(newline='') as f:
    progress=list(csv.DictReader(f))
assert len(progress)==60 and all(row['status']=='NOT_STARTED' for row in progress)
with (ROOT/'docs/source-mapping.csv').open(newline='') as f:
    mapping=list(csv.DictReader(f))
assert len(mapping)==60 and len({(r['role'],r['day']) for r in mapping})==60
assert all(r['hello_interview_topic_or_reason'] and r['source_quality_note'] for r in mapping)
assert all(r['hello_interview_youtube_url']=='https://www.youtube.com/@hello_interview' and 'not verified' in r['youtube_evidence_note'] for r in mapping)
assert 'strivers-sde-sheet-top-coding-interview-problems' in (ROOT / 'docs/source-mapping.md').read_text()
fe30=next(a for a in manifest['assignments'] if a['role']=='frontend' and a['day']==30)
assert 'attempted versus passed' in fe30['coding_prompt'] and all(all({'attempted','passed'} <= set(x) for x in c['input']['attempts']) for c in fe30['cases'] if c['input'].get('attempts'))
assert sum(len(a['cases']) for a in manifest['assignments']) == sum(len(x['cases']) for x in fixtures.values())
# The corrected budget example is deliberately tied to its stated <= budget semantics.
d2=next(a for a in manifest['assignments'] if a['role']=='sde' and a['day']==2)
assert d2['cases'][0]['expected']==2 and d2['cases'][1]['expected']==0
print(f"PASS: 30 dates, 60 assignments, {sum(len(a['cases']) for a in manifest['assignments'])} synchronized fixtures, 29 Java TODOs + 1 SQL task, 30 React helper mappings, 60 source-map rows, 60 guide sections, 60 unstarted progress rows.")
