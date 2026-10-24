"""Check source register and optional learner-authored Day 207 review shape.
This validates references, not study, readiness, booking, or exam outcome.
"""
import argparse, datetime, json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]; B=ROOT/'docs/reboot/continuation-197-210'
parser=argparse.ArgumentParser();parser.add_argument('--review',type=Path);args=parser.parse_args()
sources=json.loads((B/'verified-sources.json').read_text()); by={s['key']:s for s in sources['sources']}
assert (by['RETRIEVAL']['title'],by['RETRIEVAL']['sections'])==('Retrieval',['Retrieval'])
assert by['EVALS_LIFECYCLE']['sections']==['Create an eval for a task']
assert 'read-only 2026-10-31' in sources['scope'] and 'shutdown 2026-11-30' in sources['scope']
assert by['SAA_GUIDE']['sections']==['Exam content']
expected=['Design Secure Architectures','Design Resilient Architectures','Design High-Performing Architectures','Design Cost-Optimized Architectures']
assert by['SAA_GUIDE']['domain_mapping']==expected
assert by['DEV_ASSOCIATE']['sections']==['Exam guide']
if args.review:
 path=args.review if args.review.is_absolute() else ROOT/args.review
 note=json.loads(path.read_text()); schema=json.loads((B/'certification-review.schema.json').read_text())
 required=set(schema['required']); assert set(note)==required
 assert note['day']==207 and 0<=note['study_minutes']<=35 and note['learner_notes'].strip()
 datetime.date.fromisoformat(note['reviewed_at'])
 if note['credential']=='SAA-C03': assert set(note['domain_headings'])==set(expected)
 print('learner review record shape and official SAA domain mapping valid; no study/readiness/pass claim inferred')
else: print('source metadata titles/headings/lifecycle and SAA domain facts valid; learner study is not verified')
