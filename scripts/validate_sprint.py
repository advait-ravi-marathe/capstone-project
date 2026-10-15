#!/usr/bin/env python3
"""Offline integrity check for the dated 60-card interview starter."""
import json
from datetime import date, timedelta
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
manifest = json.loads((ROOT / 'SPRINT-MANIFEST.json').read_text())
assert manifest['start_date'] == '2026-10-16' and manifest['end_date'] == '2026-11-14'
assert manifest['daily_minutes'] == 120 and manifest['daily_unallocated_minutes'] == 30
assert len(manifest['assignments']) == 60
expected_dates = [(date(2026, 10, 16) + timedelta(days=i)).isoformat() for i in range(30)]
assert expected_dates[-1] == '2026-11-14'
for day, d in enumerate(expected_dates, 1):
    cards = [a for a in manifest['assignments'] if a['day'] == day]
    assert len(cards) == 2 and {a['date'] for a in cards} == {d}
    assert {a['role'] for a in cards} == {'sde', 'frontend'}
    assert {a['job_id'] for a in cards} == {'10579529', '10575321'}
    for a in cards:
        assert sum(a['timebox_minutes'].values()) == 60
        assert all(a[k].strip() for k in ('title', 'topic', 'coding_prompt', 'design_prompt', 'leadership_principle'))
        assert len(a['cases']) == 2
        role = a['role']
        code = ROOT / ('sde/assignments.py' if role == 'sde' else 'frontend/src/assignments.js')
        tests = ROOT / ('sde/test_assignments.py' if role == 'sde' else 'frontend/test/assignments.test.js')
        assert code.is_file() and tests.is_file()
        fixture = json.loads((ROOT / 'fixtures/cases.json').read_text())[f'{role}_{day:02d}']['cases']
        assert fixture == a['cases']
assert (ROOT / 'assignments/sde.md').is_file() and (ROOT / 'assignments/frontend.md').is_file()
import csv
with (ROOT / 'progress-template.csv').open(newline='') as f:
    progress = list(csv.DictReader(f))
assert len(progress) == 60 and all(row['status'] == 'NOT_STARTED' for row in progress)
for guide in ('assignments/sde.md', 'assignments/frontend.md'):
    text = (ROOT / guide).read_text()
    assert text.count('## Day ') == 30
assert sum(len(a['cases']) for a in manifest['assignments']) == 120
print('PASS: 30 consecutive dates, 60 assignments, 120 minutes/day, 30 minutes unallocated, 120 fixtures (2 per card), 60 code/test mappings, 60 detailed guide sections, 60 unstarted progress rows.')
