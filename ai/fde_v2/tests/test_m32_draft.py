"""M3.2 exact-action mechanics; no caller-provided approval files are trusted."""
import hashlib,json,tempfile,unittest
from pathlib import Path
from ai.fde_v2.m32 import learner,reference

class M32Mechanics(unittest.TestCase):
 def test_schedule_budget_and_partial_boundary_are_explicit(self):
  d=json.loads((Path(__file__).parents[1]/'m32'/'draft_schedule.json').read_text())
  self.assertEqual([r['day'] for r in d['days']],list(range(123,144)))
  self.assertEqual(sum(r['substantive_minutes'] for r in d['days']),1470)
  self.assertEqual(sum(r['buffer_minutes'] for r in d['days']),240)
  self.assertEqual(sum(r['visibility_minutes'] for r in d['days']),90)
  self.assertTrue(all(r['dsa_minutes']==60 and r['role_bank_minutes']==30 and r['ai_minutes']==90 for r in d['days']))
  self.assertEqual(sum(r['unallocated_minutes'] for r in d['days']),90)
  self.assertEqual(d['days'][0]['day'],123) # shared boundary with M3.1; day allocation reconciled globally

 def test_digest_is_canonical_but_binds_all_security_relevant_fields(self):
  content=b'patch'; h=hashlib.sha256(content).hexdigest()
  d=reference.canonical_action_digest('write', 'x.patch',content,'source','scope','src.py','session-a')
  self.assertEqual(d,reference.canonical_action_digest('write','x.patch',content,'source','scope','src.py','session-a'))
  for fields in [('write','y.patch',content,'source','scope','src.py','session-a'),('write','x.patch',b'other','source','scope','src.py','session-a'),
                 ('write','x.patch',content,'changed','scope','src.py','session-a'),('write','x.patch',content,'source','changed','src.py','session-a'),
                 ('write','x.patch',content,'source','scope','other.py','session-a'),
                 ('write','x.patch',content,'source','scope','src.py','session-b')]:
   self.assertNotEqual(reference.canonical_action_digest(*fields),d)
  with self.assertRaises(AssertionError): self.assertEqual(d,reference.canonical_action_digest('write','y.patch',content,'source','scope','src.py','session-a'))

 def test_approval_is_exact_expiring_and_one_use_under_replay(self):
  ledger=reference.ApprovalLedger(); digest='a'*64; token=ledger.issue(digest,20,10)
  self.assertFalse(ledger.consume(token,'b'*64,11))
  # Wrong-action attempt consumes the opaque token, so it cannot later be replayed.
  self.assertFalse(ledger.consume(token,digest,11))
  token=ledger.issue(digest,20,10)
  self.assertTrue(ledger.consume(token,digest,11)); self.assertFalse(ledger.consume(token,digest,11))
  expired=ledger.issue(digest,12,10); self.assertFalse(ledger.consume(expired,digest,12))
  with self.assertRaises(AssertionError): self.assertTrue(ledger.consume(expired,digest,12))

 def test_scratch_resource_rejects_traversal_symlink_and_non_files(self):
  with tempfile.TemporaryDirectory() as td, tempfile.TemporaryDirectory() as outside:
   Path(td,'ok.txt').write_text('actual'); Path(outside,'secret').write_text('secret')
   self.assertEqual(Path(reference.authorize_resource(td,'ok.txt')).read_text(),'actual')
   Path(td,'escape').symlink_to(Path(outside,'secret'))
   for target in ('../secret','escape','missing'):
    with self.subTest(target=target), self.assertRaises((PermissionError,FileNotFoundError)): reference.authorize_resource(td,target)

 def test_write_consumes_approval_before_no_overwrite_scratch_mutation(self):
  with tempfile.TemporaryDirectory() as td:
   Path(td,'source.py').write_text('source')
   ledger=reference.ApprovalLedger(); target='review.patch'; body=b'--- proposal only'; source=hashlib.sha256(b'source').hexdigest(); scope='2'*64
   digest=reference.canonical_action_digest('write_patch_proposal',target,body,source,scope,'source.py','session-a'); token=ledger.issue(digest,200,100)
   out=reference.write_proposal(td,ledger,token,'source.py',target,body,source,scope,'session-a',100)
   self.assertEqual(out.read_bytes(),body); self.assertEqual(oct(out.stat().st_mode & 0o777),'0o600')
   with self.assertRaises(PermissionError): reference.write_proposal(td,ledger,token,'source.py',target,body,source,scope,'session-a',100)
   with self.assertRaises(FileExistsError):
    token=ledger.issue(digest,200,100); reference.write_proposal(td,ledger,token,'source.py',target,body,source,scope,'session-a',100)
   with self.assertRaises(PermissionError): reference.write_proposal(td,ledger,str(Path(td)/'approval.json'),'source.py','forged.patch',body,source,scope,'session-a',100)

 def test_learner_seams_remain_todos(self):
  with self.assertRaises(NotImplementedError): learner.canonical_action_digest('x','y',b'z','a','b','src.py','session')

if __name__=='__main__': unittest.main()
