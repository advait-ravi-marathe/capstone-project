"""Check exact source titles/headings recorded for Days 197–210."""
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
source=json.loads((ROOT/'docs/reboot/continuation-197-210/verified-sources.json').read_text())
by_key={s['key']:s for s in source['sources']}
assert by_key['RETRIEVAL']['title']=='Retrieval'
assert by_key['RETRIEVAL']['sections']==['Retrieval']
assert by_key['EVALS_LIFECYCLE']['sections']==['Create an eval for a task']
assert 'read-only 2026-10-31' in source['scope'] and 'shutdown 2026-11-30' in source['scope']
assert source['course_assignment'] is None
print('Source titles/headings and Evals lifecycle caveat are synchronized')
