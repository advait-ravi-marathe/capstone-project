"""Conditional local M3.1 checks; no model or external repository access."""
from __future__ import annotations
import argparse, json, subprocess, sys
from ai.fde_v2.evidence_receipts import report_receipt

def main(argv=None):
    p=argparse.ArgumentParser(description=__doc__); p.add_argument('--milestone',required=True,choices=['AIFDE-V2-M3.1'])
    p.add_argument('--mode',required=True,choices=['mechanics','learner','integration','evidence'])
    p.add_argument('--receipt',help='hash-verified artifact manifest for independent review'); a=p.parse_args(argv)
    if a.mode=='integration':
        return subprocess.run([sys.executable,'-m','unittest','ai.fde_v2.tests.test_m31_draft','-v'],check=False).returncode
    if a.mode=='evidence':
        return report_receipt(a.receipt,a.milestone,['prerequisite_evidence','tool_trace','scope_denial','patch_proposal','learner_explanation'])
    t='ai.fde_v2.tests.test_m31_draft' if a.mode=='mechanics' else 'ai.fde_v2.learner_acceptance.test_m31_units'
    return subprocess.run([sys.executable,'-m','unittest',t,'-v'],check=False).returncode
if __name__=='__main__': raise SystemExit(main())
