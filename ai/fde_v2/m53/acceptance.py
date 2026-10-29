"""Conditional M5.3 capability audit; no public action, exam, or readiness is inferred."""
import argparse,json,subprocess,sys
def main(argv=None):
 p=argparse.ArgumentParser(); p.add_argument('--milestone',required=True,choices=['AIFDE-V2-M5.3']); p.add_argument('--mode',required=True,choices=['mechanics','learner','evidence']); a=p.parse_args(argv)
 if a.mode=='evidence': print(json.dumps({'status':'BLOCKED','reason':'actual learner evidence inventory, current role postings and readiness review are not supplied','milestone_pass':False,'external_action':False})); return 2
 return subprocess.run([sys.executable,'-m','unittest','ai.fde_v2.tests.test_m53' if a.mode=='mechanics' else 'ai.fde_v2.learner_acceptance.test_m53_units','-v']).returncode
if __name__=='__main__': raise SystemExit(main())
