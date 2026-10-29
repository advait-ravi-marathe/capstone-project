"""Conditional M4.3 acceptance command; scaffold evidence is not earned completion."""
import argparse,json,subprocess,sys
def main(argv=None):
 p=argparse.ArgumentParser(); p.add_argument('--milestone',required=True,choices=['AIFDE-V2-M4.3']); p.add_argument('--mode',required=True,choices=['mechanics','learner','evidence']); a=p.parse_args(argv)
 if a.mode=='evidence': print(json.dumps({'status':'BLOCKED','reason':'no learner attempts or authorized peer review supplied','milestone_pass':False})); return 2
 return subprocess.run([sys.executable,'-m','unittest','ai.fde_v2.tests.test_m43' if a.mode=='mechanics' else 'ai.fde_v2.learner_acceptance.test_m43_units','-v']).returncode
if __name__=='__main__': raise SystemExit(main())
