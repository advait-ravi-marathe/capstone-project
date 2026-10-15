#!/usr/bin/env python3
"""Learner contract probe for the SQLite day-19 query; never contains a solution."""
import json
import sqlite3
import sys
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
source_path = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else ROOT / 'sde/sql/day19.sql'
source = source_path.read_text()
manifest = json.loads((ROOT / 'SPRINT-MANIFEST.json').read_text())
card = next(x for x in manifest['assignments'] if x['role'] == 'sde' and x['day'] == 19)
# Verify fixture shape and the synthetic SQL schema even while the learner query is TODO.
for case in card['cases']:
    db = sqlite3.connect(':memory:')
    db.execute('CREATE TABLE registration_events (event_id INTEGER PRIMARY KEY, seller_id TEXT NOT NULL, state TEXT NOT NULL, created_at TEXT NOT NULL)')
    db.executemany('INSERT INTO registration_events VALUES (:event_id,:seller_id,:state,:created_at)', case['input']['rows'])
    count = db.execute('SELECT COUNT(*) FROM registration_events').fetchone()[0]
    if count != len(case['input']['rows']):
        raise SystemExit('SQL Day 19: fixture/schema setup mismatch.')
if 'TODO 1:' in source or 'TODO 2:' in source:
    print(f"SQL Day 19: {len(card['cases'])} fixture/schema cases loaded; learner TODO (latest-per-seller query not written).")
    sys.exit(1)
statements = [part.strip() for part in source.split(';') if part.strip() and not part.lstrip().startswith('--')]
if not statements:
    print('SQL Day 19: no executable query found.')
    sys.exit(1)
for case in card['cases']:
    db = sqlite3.connect(':memory:')
    db.execute('CREATE TABLE registration_events (event_id INTEGER PRIMARY KEY, seller_id TEXT NOT NULL, state TEXT NOT NULL, created_at TEXT NOT NULL)')
    db.executemany('INSERT INTO registration_events VALUES (:event_id,:seller_id,:state,:created_at)', case['input']['rows'])
    actual = None
    try:
        for statement in statements:
            cursor = db.execute(statement)
            if cursor.description:
                actual = [{'state': state, 'count': count} for state, count in cursor.fetchall()]
    except sqlite3.Error as exc:
        print(f"FAIL SQL Day 19: {exc}")
        sys.exit(1)
    if actual != case['expected']:
        print(f"FAIL SQL Day 19: expected {case['expected']}, got {actual}")
        sys.exit(1)
print(f"PASS SQL Day 19: {len(card['cases'])} executable learner cases.")
