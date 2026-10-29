"""Conditional M2.3 checks; no AWS or hosted-model invocation is performed."""
from __future__ import annotations
import argparse, json, subprocess, sys

def main(argv=None):
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--milestone', required=True, choices=['AIFDE-V2-M2.3'])
    p.add_argument('--mode', required=True, choices=['mechanics','learner','integration','evidence'])
    a = p.parse_args(argv)
    if a.mode in ('integration','evidence'):
        print(json.dumps({'status':'BLOCKED','cloud_status':'BLOCKED','model_status':'BLOCKED',
                          'reasons':['no user-authorized AWS account/region/service/spend scope',
                                     'no authorized real AWS-hosted model request or provider receipt'],
                          'milestone_pass':False}, indent=2)); return 2
    test = 'ai.fde_v2.tests.test_m23_draft' if a.mode == 'mechanics' else 'ai.fde_v2.learner_acceptance.test_m23_units'
    return subprocess.run([sys.executable,'-m','unittest',test,'-v'],check=False).returncode

if __name__ == '__main__': raise SystemExit(main())
