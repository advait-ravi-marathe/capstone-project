"""Conditional M5.3 capability audit; no public action, exam, or readiness is inferred."""
import argparse,json,subprocess,sys
from ai.fde_v2.evidence_receipts import report_receipt
def main(argv=None):
 p=argparse.ArgumentParser(); p.add_argument('--milestone',required=True,choices=['AIFDE-V2-M5.3']); p.add_argument('--mode',required=True,choices=['mechanics','learner','evidence'])
 p.add_argument('--receipt',help='hash-verified artifact manifest for independent review'); a=p.parse_args(argv)
 if a.mode=='evidence': return report_receipt(a.receipt,a.milestone,['evidence_map','case_study','role_fit_sources','aws_readiness_map','certification_decision','external_action_permissions'])
 return subprocess.run([sys.executable,'-m','unittest','ai.fde_v2.tests.test_m53' if a.mode=='mechanics' else 'ai.fde_v2.learner_acceptance.test_m53_units','-v']).returncode
if __name__=='__main__': raise SystemExit(main())
