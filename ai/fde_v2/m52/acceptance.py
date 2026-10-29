"""Conditional M5.2 evidence gate; mechanics fixtures cannot pass the held-out gate."""
import argparse,json,subprocess,sys
def evaluate_receipt(receipt):
 required=("dataset_source","dataset_hash","split_hash","model_hash","heldout_results","learner_explanation")
 missing=[k for k in required if not receipt.get(k)]
 if missing: return {"status":"BLOCKED","missing":missing,"online_uplift_claim":False}
 if receipt.get("synthetic_fixture") is True or receipt.get("heldout_frozen") is not True: return {"status":"BLOCKED","reason":"synthetic or unfrozen heldout data cannot establish M5.2 evidence","online_uplift_claim":False}
 return {"status":"REQUIRES_REVIEW","missing":[],"online_uplift_claim":False}
def main(argv=None):
 p=argparse.ArgumentParser(); p.add_argument('--milestone',required=True,choices=['AIFDE-V2-M5.2']); p.add_argument('--mode',required=True,choices=['mechanics','learner','evidence']); a=p.parse_args(argv)
 if a.mode=='evidence': print(json.dumps({'status':'BLOCKED','reason':'no reviewed public/synthetic source manifest, learner fit, or actual frozen held-out results supplied','milestone_pass':False})); return 2
 return subprocess.run([sys.executable,'-m','unittest','ai.fde_v2.tests.test_m52' if a.mode=='mechanics' else 'ai.fde_v2.learner_acceptance.test_m52_units','-v']).returncode
if __name__=='__main__': raise SystemExit(main())
