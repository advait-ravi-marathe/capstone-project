"""Conditional M4.2 checks; local receipts do not imply cloud or sustained reliability."""
from __future__ import annotations
import argparse, json, subprocess, sys

def main(argv=None):
    p=argparse.ArgumentParser(); p.add_argument('--milestone',required=True,choices=['AIFDE-V2-M4.2']); p.add_argument('--mode',required=True,choices=['mechanics','learner','evidence']); a=p.parse_args(argv)
    if a.mode=='evidence':
        print(json.dumps({'status':'BLOCKED','reasons':['no real M4.1 pilot/change evidence','no actual live cloud operations or sustained production observations exist'], 'milestone_pass':False},indent=2)); return 2
    mod='ai.fde_v2.tests.test_m42' if a.mode=='mechanics' else 'ai.fde_v2.learner_acceptance.test_m42_units'
    return subprocess.run([sys.executable,'-m','unittest',mod,'-v']).returncode
if __name__=='__main__': raise SystemExit(main())
