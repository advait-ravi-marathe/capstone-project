#!/usr/bin/env python3
"""Validate daily contract/export alignment for Days 197–210."""
import json, re
from datetime import date
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
B=ROOT/'docs/reboot/continuation-197-210'
m=json.loads((B/'manifest.json').read_text()); o=json.loads((B/'scrum-owner-manifest.json').read_text()); p=json.loads((B/'days-197-210-transfer-plan.json').read_text())
assert [d['day'] for d in m['days']]==list(range(197,211))
assert [d['day_number'] for d in o['cards']]==list(range(197,211))
assert [d['day'] for d in p['days']]==list(range(197,211))
for d,owner,plan in zip(m['days'],o['cards'],p['days']):
 text=(B/'contracts'/f"day-{d['day']}.md").read_text()
 for needle in (d['title'],d['date'],d['selectors'][0]):
  assert needle in text,(d['day'],needle)
 assert date.fromisoformat(d['date']).isoformat()==d['date']
 assert d['engineering_architecture']['note_ref'].endswith(f'#day-{d["day"]}')
 assert re.search(rf'<a id="day-{d["day"]}"></a>',(B/'assistant-architecture-notes.md').read_text())
 assert d['selectors']==owner['selectors']==plan['selectors']
 assert d['shared_interview_block']==owner['interview_practice']
 assert d['engineering_architecture']==owner['engineering_architecture']==plan['engineering_architecture']
print('Days 197–210 contract, anchors, selector, and export consistency PASS')
