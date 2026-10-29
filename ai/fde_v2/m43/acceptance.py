"""Conditional M4.3 acceptance command; scaffold evidence is not earned completion."""
import argparse,json,subprocess,sys
from ai.fde_v2.evidence_receipts import report_receipt
def main(argv=None):
 p=argparse.ArgumentParser(); p.add_argument('--milestone',required=True,choices=['AIFDE-V2-M4.3']); p.add_argument('--mode',required=True,choices=['mechanics','learner','evidence'])
 p.add_argument('--receipt',help='hash-verified artifact manifest for independent review'); a=p.parse_args(argv)
 if a.mode=='evidence': return report_receipt(a.receipt,a.milestone,['attempt_a','attempt_b','patch_tests','clean_run_or_blocker','reviewer_feedback_or_unavailable','demo_receipt'])
 return subprocess.run([sys.executable,'-m','unittest','ai.fde_v2.tests.test_m43' if a.mode=='mechanics' else 'ai.fde_v2.learner_acceptance.test_m43_units','-v']).returncode
if __name__=='__main__': raise SystemExit(main())
