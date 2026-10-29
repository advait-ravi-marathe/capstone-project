"""Conditional M5.1 rehearsal checks; not an employer cutoff or learner result."""
import argparse,json,subprocess,sys
from ai.fde_v2.evidence_receipts import report_receipt
def main(argv=None):
 p=argparse.ArgumentParser(); p.add_argument('--milestone',required=True,choices=['AIFDE-V2-M5.1']); p.add_argument('--mode',required=True,choices=['mechanics','learner','evidence'])
 p.add_argument('--receipt',help='hash-verified artifact manifest for independent review'); a=p.parse_args(argv)
 if a.mode=='evidence': return report_receipt(a.receipt,a.milestone,['six_gate_attempt_matrix','unseen_attempts','reviewer_types','dated_feedback','repair_assignments'])
 return subprocess.run([sys.executable,'-m','unittest','ai.fde_v2.tests.test_m51' if a.mode=='mechanics' else 'ai.fde_v2.learner_acceptance.test_m51_units','-v']).returncode
if __name__=='__main__': raise SystemExit(main())
