"""Conditional M4.1 checks; no customer outreach or evaluator is synthesized."""
from __future__ import annotations
import argparse, json, subprocess, sys
from ai.fde_v2.evidence_receipts import report_receipt

def main(argv=None):
    p=argparse.ArgumentParser(); p.add_argument('--milestone',required=True,choices=['AIFDE-V2-M4.1']); p.add_argument('--mode',required=True,choices=['mechanics','learner','integration','evidence'])
    p.add_argument('--receipt',help='hash-verified artifact manifest for independent review'); a=p.parse_args(argv)
    if a.mode=='integration': return subprocess.run([sys.executable,'-m','unittest','ai.fde_v2.tests.test_m41','-v'],check=False).returncode
    if a.mode=='evidence':
        return report_receipt(a.receipt,a.milestone,['discovery_record','evaluator_receipt','pilot_contract','real_user_authorization_or_roleplay_classification'])
    mod='ai.fde_v2.tests.test_m41' if a.mode=='mechanics' else 'ai.fde_v2.learner_acceptance.test_m41_units'
    return subprocess.run([sys.executable,'-m','unittest',mod,'-v']).returncode
if __name__=='__main__': raise SystemExit(main())
