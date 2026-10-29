"""M3.2 learner acceptance; approval and resource seams intentionally unsolved."""
import importlib,os,tempfile,unittest
from pathlib import Path
C=importlib.import_module(os.environ.get('M32_CANDIDATE','ai.fde_v2.m32.learner'))

class M32LearnerAcceptance(unittest.TestCase):
 def test_digest_binds_action_target_content_source_and_scope(self):
  f=C.canonical_action_digest('write_patch_proposal','a.patch',b'patch','a'*64,'b'*64,'src.py','session-a')
  self.assertEqual(len(f),64)
  for args in [('write_patch_proposal','b.patch',b'patch','a'*64,'b'*64,'src.py','session-a'),
               ('write_patch_proposal','a.patch',b'changed','a'*64,'b'*64,'src.py','session-a'),
               ('write_patch_proposal','a.patch',b'patch','c'*64,'b'*64,'src.py','session-a'),
               ('write_patch_proposal','a.patch',b'patch','a'*64,'d'*64,'src.py','session-a'),
               ('write_patch_proposal','a.patch',b'patch','a'*64,'b'*64,'other.py','session-a'),
               ('write_patch_proposal','a.patch',b'patch','a'*64,'b'*64,'src.py','session-b')]:
   with self.subTest(args=args): self.assertNotEqual(C.canonical_action_digest(*args),f)

 def test_resource_confined_and_write_consumes_exact_single_use_approval(self):
  from ai.fde_v2.m32.reference import ApprovalLedger
  with tempfile.TemporaryDirectory() as td:
   Path(td,'source.txt').write_text('source'); target='x.patch'; body=b'proposal'; source=__import__('hashlib').sha256(b'source').hexdigest(); scope='b'*64; now=100.0
   digest=C.canonical_action_digest('write_patch_proposal',target,body,source,scope,'source.txt','session-a')
   ledger=ApprovalLedger(); token=ledger.issue(digest,now+10,now)
   out=C.write_patch_proposal(td,ledger,token,target,body,source,scope,'source.txt','session-a',now)
   self.assertEqual(out.read_bytes(),body)
   with self.assertRaises((PermissionError,ValueError,FileExistsError)):
    C.write_patch_proposal(td,ledger,token,target,body,source,scope,'source.txt','session-a',now)
   with self.assertRaises((PermissionError,ValueError)):
    C.authorize_resource(td,'../outside')

if __name__=='__main__': unittest.main()
