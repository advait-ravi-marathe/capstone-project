"""Conditional M2.3 checks; no AWS or hosted-model invocation is performed."""
from __future__ import annotations
import argparse, json, subprocess, sys
from ai.fde_v2.evidence_receipts import report_receipt

def main(argv=None):
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--milestone', required=True, choices=['AIFDE-V2-M2.3'])
    p.add_argument('--mode', required=True, choices=['mechanics','learner','integration','evidence'])
    p.add_argument('--receipt',help='hash-verified artifact manifest for independent review')
    a = p.parse_args(argv)
    if a.mode=='integration':
        return subprocess.run([sys.executable,'-m','unittest','ai.fde_v2.tests.test_m23_draft','-v'],check=False).returncode
    if a.mode=='evidence':
        keys=['authorized_resource_readback','live_app_request','live_trace','rollback','teardown',
              'authorized_model_request','provider_request_response','observed_usage_or_unavailable_reason']
        return report_receipt(a.receipt,a.milestone,keys)
    test = 'ai.fde_v2.tests.test_m23_draft' if a.mode == 'mechanics' else 'ai.fde_v2.learner_acceptance.test_m23_units'
    return subprocess.run([sys.executable,'-m','unittest',test,'-v'],check=False).returncode

if __name__ == '__main__': raise SystemExit(main())
