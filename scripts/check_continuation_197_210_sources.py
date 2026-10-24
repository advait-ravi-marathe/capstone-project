"""Validate official source registry and optional two-record learner review; never infer study/pass."""
import argparse,datetime,json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1];B=ROOT/'docs/reboot/continuation-197-210'
p=argparse.ArgumentParser();p.add_argument('--review',type=Path,nargs=2,metavar=('SAA_JSON','DEV_JSON'));args=p.parse_args()
s=json.loads((B/'verified-sources.json').read_text());by={x['key']:x for x in s['sources']}
assert by['SAA_GUIDE']['title']=='AWS Certified Solutions Architect - Associate (SAA-C03)' and by['SAA_GUIDE']['sections']==['Exam content']
assert by['SAA_GUIDE']['domain_mapping']==['Design Secure Architectures','Design Resilient Architectures','Design High-Performing Architectures','Design Cost-Optimized Architectures']
assert by['DEV_ASSOCIATE']['title']=='AWS Certified Developer - Associate exam guide'
assert by['RETRIEVAL']['title']=='Retrieval' and by['RETRIEVAL']['sections']==['Retrieval']
assert by['EVALS_LIFECYCLE']['sections']==['Create an eval for a task']
assert 'read-only 2026-10-31' in s['scope'] and 'shuts down 2026-11-30' in s['scope']
def validate_review(path,credential,guide):
 note=json.loads(path.read_text())
 required={'day','reviewed_at','credential','guide_title','guide_version','source_url','domain_headings','study_minutes','learner_notes','assistance'}
 assert set(note)==required
 assert note['day']==207 and note['credential']==credential
 assert note['guide_title']==guide['title'] and note['source_url']==guide['url']
 assert isinstance(note['guide_version'],str) and note['guide_version'].strip()
 assert isinstance(note['reviewed_at'],str);datetime.date.fromisoformat(note['reviewed_at'])
 assert isinstance(note['domain_headings'],list) and note['domain_headings'] and all(isinstance(x,str) and x.strip() for x in note['domain_headings'])
 assert type(note['study_minutes']) is int and 0<=note['study_minutes']<=35
 assert isinstance(note['learner_notes'],str) and note['learner_notes'].strip()
 assert isinstance(note['assistance'],str) and note['assistance'].strip()
 if credential=='SAA-C03':assert set(note['domain_headings'])==set(guide['domain_mapping'])
 return note
if args.review:
 expected=[('SAA-C03',by['SAA_GUIDE']),('AWS Certified Developer - Associate',by['DEV_ASSOCIATE'])]
 for path,(credential,guide) in zip(args.review,expected):
  path=path if path.is_absolute() else ROOT/path
  validate_review(path,credential,guide)
 print('two factual guide records valid; this check does not infer study, readiness or pass')
else:print('official source titles/headings/lifecycle and SAA domains valid; learner study is not verified')
