"""Conditional M3.2 checks. Integration uses the installed MCP SDK over stdio."""
from __future__ import annotations
import argparse,json,subprocess,sys
def main(argv=None):
 p=argparse.ArgumentParser(description=__doc__); p.add_argument('--milestone',required=True,choices=['AIFDE-V2-M3.2'])
 p.add_argument('--mode',required=True,choices=['mechanics','learner','integration','evidence']); a=p.parse_args(argv)
 if a.mode=='evidence':
  print(json.dumps({'status':'BLOCKED','reasons':['no human-operated trusted MCP client approval receipt exists',
   'M3.1 prerequisite and learner evidence are not passed'],'milestone_pass':False},indent=2)); return 2
 t={'mechanics':'ai.fde_v2.tests.test_m32_draft','learner':'ai.fde_v2.learner_acceptance.test_m32_units',
    'integration':'ai.fde_v2.tests.test_m32_stdio'}[a.mode]
 return subprocess.run([sys.executable,'-m','unittest',t,'-v'],check=False).returncode
if __name__=='__main__': raise SystemExit(main())
