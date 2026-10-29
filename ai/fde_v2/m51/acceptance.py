"""Conditional M5.1 rehearsal checks; not an employer cutoff or learner result."""
import argparse,json,subprocess,sys
def main(argv=None):
 p=argparse.ArgumentParser(); p.add_argument('--milestone',required=True,choices=['AIFDE-V2-M5.1']); p.add_argument('--mode',required=True,choices=['mechanics','learner','evidence']); a=p.parse_args(argv)
 if a.mode=='evidence': print(json.dumps({'status':'BLOCKED','reason':'no dated learner attempt artifacts or feedback supplied','milestone_pass':False})); return 2
 return subprocess.run([sys.executable,'-m','unittest','ai.fde_v2.tests.test_m51' if a.mode=='mechanics' else 'ai.fde_v2.learner_acceptance.test_m51_units','-v']).returncode
if __name__=='__main__': raise SystemExit(main())
