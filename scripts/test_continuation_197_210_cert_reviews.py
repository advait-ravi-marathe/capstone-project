import json,subprocess,tempfile,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
SAA={"day":207,"reviewed_at":"2027-05-18","credential":"SAA-C03","guide_title":"AWS Certified Solutions Architect - Associate (SAA-C03)","guide_version":"SAA-C03","source_url":"https://docs.aws.amazon.com/aws-certification/latest/solutions-architect-associate-03/solutions-architect-associate-03.html","domain_headings":["Content Domain 1: Design Secure Architectures","Content Domain 2: Design Resilient Architectures","Content Domain 3: Design High-Performing Architectures","Content Domain 4: Design Cost-Optimized Architectures"],"study_minutes":17,"version_status":"verified","learner_notes":"Recorded current page facts.","assistance":"None"}
DEV={"day":207,"reviewed_at":"2027-05-18","credential":"AWS Certified Developer - Associate","guide_title":"AWS Certified Developer - Associate","guide_version":None,"source_url":"https://aws.amazon.com/certification/certified-developer-associate/","domain_headings":["Review the exam guide"],"study_minutes":18,"version_status":"not_verified","learner_notes":"Recorded current visible headings.","assistance":"None"}
class CertificationReviewValidationTest(unittest.TestCase):
 def run_checker(self,a,b):
  with tempfile.TemporaryDirectory() as td:
   x=Path(td)/'saa.json';y=Path(td)/'dev.json';x.write_text(json.dumps(a));y.write_text(json.dumps(b))
   return subprocess.run(['python3','scripts/check_continuation_197_210_sources.py','--review',str(x),str(y)],cwd=ROOT,capture_output=True,text=True)
 def test_two_exact_records_pass(self):self.assertEqual(0,self.run_checker(SAA,DEV).returncode)
 def test_rejects_arbitrary_credential(self):
  bad=dict(SAA,credential='anything');self.assertNotEqual(0,self.run_checker(bad,DEV).returncode)
 def test_rejects_mismatched_title_url_or_empty_version(self):
  for changes in ({'guide_title':'invented'},{'source_url':'https://example.com'},{'guide_version':''}):
   self.assertNotEqual(0,self.run_checker(dict(SAA,**changes),DEV).returncode)
 def test_rejects_combined_study_over_35_and_version_status_mismatch(self):
  self.assertNotEqual(0,self.run_checker(dict(SAA,study_minutes=18),dict(DEV,study_minutes=18)).returncode)
  self.assertNotEqual(0,self.run_checker(dict(SAA,guide_version=None),DEV).returncode)
  self.assertNotEqual(0,self.run_checker(dict(SAA,version_status='not_verified'),DEV).returncode)
 def test_rejects_bad_types_and_saa_domain_map(self):
  self.assertNotEqual(0,self.run_checker(dict(SAA,study_minutes=True),DEV).returncode)
  self.assertNotEqual(0,self.run_checker(dict(SAA,domain_headings=['invented']),DEV).returncode)
if __name__=='__main__':unittest.main()
