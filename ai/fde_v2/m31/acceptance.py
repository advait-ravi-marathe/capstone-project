"""Conditional local M3.1 checks; no model or external repository access."""
from __future__ import annotations
import argparse, json, subprocess, sys

def main(argv=None):
    p=argparse.ArgumentParser(description=__doc__); p.add_argument('--milestone',required=True,choices=['AIFDE-V2-M3.1'])
    p.add_argument('--mode',required=True,choices=['mechanics','learner','integration','evidence']); a=p.parse_args(argv)
    if a.mode in ('integration','evidence'):
        print(json.dumps({'status':'BLOCKED','reasons':['P1 actual-model/RAG, M2.1 and M2.2 learner prerequisite evidence is not passed',
            'no authorized real planning model or learner workflow evidence exists'],'milestone_pass':False},indent=2)); return 2
    t='ai.fde_v2.tests.test_m31_draft' if a.mode=='mechanics' else 'ai.fde_v2.learner_acceptance.test_m31_units'
    return subprocess.run([sys.executable,'-m','unittest',t,'-v'],check=False).returncode
if __name__=='__main__': raise SystemExit(main())
