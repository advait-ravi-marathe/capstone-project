"""Conditional M3.3 checks; local mechanics do not establish real-agent capability."""
from __future__ import annotations
import argparse, json, subprocess, sys

def main(argv=None):
    p=argparse.ArgumentParser(); p.add_argument('--milestone',required=True,choices=['AIFDE-V2-M3.3'])
    p.add_argument('--mode',required=True,choices=['mechanics','learner','integration','evidence']); a=p.parse_args(argv)
    if a.mode in ('integration','evidence'):
        print(json.dumps({'status':'BLOCKED','reasons':['M3.1/M3.2 learner prerequisites are unproven',
             'no authorized real planning model, trusted human denial receipt, or deployment receipt exists'],
             'milestone_pass':False},indent=2)); return 2
    mod='ai.fde_v2.tests.test_m33' if a.mode=='mechanics' else 'ai.fde_v2.learner_acceptance.test_m33_units'
    return subprocess.run([sys.executable,'-m','unittest',mod,'-v']).returncode
if __name__=='__main__': raise SystemExit(main())
