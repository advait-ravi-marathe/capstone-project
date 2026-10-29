"""Conditional M3.3 checks; local mechanics do not establish real-agent capability."""
from __future__ import annotations
import argparse, json, subprocess, sys
from ai.fde_v2.evidence_receipts import report_receipt

def main(argv=None):
    p=argparse.ArgumentParser(); p.add_argument('--milestone',required=True,choices=['AIFDE-V2-M3.3'])
    p.add_argument('--mode',required=True,choices=['mechanics','learner','integration','evidence'])
    p.add_argument('--receipt',help='hash-verified artifact manifest for independent review'); a=p.parse_args(argv)
    if a.mode=='integration':
        return subprocess.run([sys.executable,'-m','unittest','ai.fde_v2.tests.test_m33','-v'],check=False).returncode
    if a.mode=='evidence':
        return report_receipt(a.receipt,a.milestone,['frozen_taskset','actual_task_runs','authorization_regressions','recovery_drill','learner_explanation'])
    mod='ai.fde_v2.tests.test_m33' if a.mode=='mechanics' else 'ai.fde_v2.learner_acceptance.test_m33_units'
    return subprocess.run([sys.executable,'-m','unittest',mod,'-v']).returncode
if __name__=='__main__': raise SystemExit(main())
