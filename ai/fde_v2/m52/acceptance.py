"""Conditional M5.2 evidence gate; mechanics fixtures cannot pass the held-out gate."""
import argparse,json,subprocess,sys
from ai.fde_v2.evidence_receipts import report_receipt
def evaluate_receipt(receipt):
 required=("dataset_source","dataset_hash","split_hash","model_hash","heldout_results","learner_explanation")
 missing=[k for k in required if not receipt.get(k)]
 if missing: return {"status":"BLOCKED","missing":missing,"online_uplift_claim":False}
 if receipt.get("synthetic_fixture") is True or receipt.get("heldout_frozen") is not True: return {"status":"BLOCKED","reason":"synthetic or unfrozen heldout data cannot establish M5.2 evidence","online_uplift_claim":False}
 return {"status":"REQUIRES_REVIEW","missing":[],"online_uplift_claim":False}
def main(argv=None):
 p=argparse.ArgumentParser(); p.add_argument('--milestone',required=True,choices=['AIFDE-V2-M5.2']); p.add_argument('--mode',required=True,choices=['mechanics','learner','evidence'])
 p.add_argument('--receipt',help='hash-verified artifact manifest for independent review'); a=p.parse_args(argv)
 if a.mode=='evidence': return report_receipt(a.receipt,a.milestone,['dataset_source_and_license','dataset_digest','chronological_split','learner_fit_artifact','frozen_heldout_results','learner_explanation'])
 return subprocess.run([sys.executable,'-m','unittest','ai.fde_v2.tests.test_m52' if a.mode=='mechanics' else 'ai.fde_v2.learner_acceptance.test_m52_units','-v']).returncode
if __name__=='__main__': raise SystemExit(main())
