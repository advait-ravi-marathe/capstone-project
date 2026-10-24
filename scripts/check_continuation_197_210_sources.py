"""Validate official source registry and optional two-record learner review; never infer study/pass."""
import argparse,datetime,json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]; B=ROOT/'docs/reboot/continuation-197-210'
parser=argparse.ArgumentParser();parser.add_argument('--review',type=Path,nargs=2,metavar=('SAA_JSON','DEV_JSON'));args=parser.parse_args()
sources=json.loads((B/'verified-sources.json').read_text());by={x['key']:x for x in sources['sources']}
SAA_DOMAINS=['Content Domain 1: Design Secure Architectures','Content Domain 2: Design Resilient Architectures','Content Domain 3: Design High-Performing Architectures','Content Domain 4: Design Cost-Optimized Architectures']
assert by['SAA_GUIDE']['title']=='AWS Certified Solutions Architect - Associate (SAA-C03)' and by['SAA_GUIDE']['sections']==['Exam content']
assert by['SAA_GUIDE']['domain_mapping']==SAA_DOMAINS
assert by['DEV_ASSOCIATE']['title']=='AWS Certified Developer - Associate' and by['DEV_ASSOCIATE']['sections']==['Review the exam guide']
assert by['RETRIEVAL']['title']=='Retrieval' and by['RETRIEVAL']['sections']==['Retrieval']
assert by['EVALS_LIFECYCLE']['sections']==['Create an eval for a task']
assert 'read-only 2026-10-31' in sources['scope'] and 'shuts down 2026-11-30' in sources['scope']

def validate_review(path,credential,guide):
    note=json.loads(path.read_text())
    required={'day','reviewed_at','credential','guide_title','guide_version','version_status','source_url','domain_headings','study_minutes','learner_notes','assistance'}
    assert set(note)==required
    assert note['day']==207 and note['credential']==credential
    assert note['guide_title']==guide['title'] and note['source_url']==guide['url']
    assert note['version_status'] in ('verified','not_verified')
    if note['version_status']=='verified':
        assert isinstance(note['guide_version'],str) and note['guide_version'].strip()
    else:
        assert note['guide_version'] is None
    assert isinstance(note['reviewed_at'],str);datetime.date.fromisoformat(note['reviewed_at'])
    assert isinstance(note['domain_headings'],list) and note['domain_headings'] and all(isinstance(x,str) and x.strip() for x in note['domain_headings'])
    assert type(note['study_minutes']) is int and 0<=note['study_minutes']<=35
    assert isinstance(note['learner_notes'],str) and note['learner_notes'].strip()
    assert isinstance(note['assistance'],str) and note['assistance'].strip()
    if credential=='SAA-C03':
        assert note['domain_headings']==SAA_DOMAINS
    else:
        assert 'Review the exam guide' in note['domain_headings']
    return note
if args.review:
    expected=[('SAA-C03',by['SAA_GUIDE']),('AWS Certified Developer - Associate',by['DEV_ASSOCIATE'])]
    minutes=0
    for path,(credential,guide) in zip(args.review,expected):
        path=path if path.is_absolute() else ROOT/path
        minutes+=validate_review(path,credential,guide)['study_minutes']
    assert minutes<=35,'combined Day 207 guide-study minutes exceed the 35-minute block'
    print('two factual guide records valid; total study minutes within 35; no readiness/pass claim inferred')
else:
    print('official source titles/headings/lifecycle and SAA domains valid; learner study is not verified')
