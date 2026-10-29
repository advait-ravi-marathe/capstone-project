"""Conditional M4.2 checks; local receipts do not imply cloud or sustained reliability."""
from __future__ import annotations
import argparse, json, subprocess, sys
from ai.fde_v2.evidence_receipts import report_receipt

def main(argv=None):
    p=argparse.ArgumentParser(); p.add_argument('--milestone',required=True,choices=['AIFDE-V2-M4.2']); p.add_argument('--mode',required=True,choices=['mechanics','learner','integration','evidence'])
    p.add_argument('--receipt',help='hash-verified artifact manifest for independent review'); a=p.parse_args(argv)
    if a.mode=='integration': return subprocess.run([sys.executable,'-m','unittest','ai.fde_v2.tests.test_m42','-v'],check=False).returncode
    if a.mode=='evidence':
        return report_receipt(a.receipt,a.milestone,['change_diff','regression_run','migration_receipt','incident_recovery','retention_check','live_cloud_observations'])
    mod='ai.fde_v2.tests.test_m42' if a.mode=='mechanics' else 'ai.fde_v2.learner_acceptance.test_m42_units'
    return subprocess.run([sys.executable,'-m','unittest',mod,'-v']).returncode
if __name__=='__main__': raise SystemExit(main())
