import unittest
from fastapi.testclient import TestClient
from ai.fde_v2.m52 import learner,reference,workflow,acceptance,api
EVENTS=[{"event_id":str(i),"user_id":f"u{i%2}","item_id":f"i{i%3}","timestamp":i} for i in range(10)]
class M52Test(unittest.TestCase):
 def test_chronological_frozen_holdout_and_duplicate_rejection(self):
  s=reference.chronological_split(EVENTS)
  self.assertLess(max(x['timestamp'] for x in s['train']),min(x['timestamp'] for x in s['validation']))
  self.assertLess(max(x['timestamp'] for x in s['validation']),min(x['timestamp'] for x in s['heldout']))
  self.assertTrue(s['test_frozen'])
  with self.assertRaises(ValueError): reference.chronological_split(EVENTS+[EVENTS[0]])
 def test_cosine_is_learner_owned_math_with_edges_and_ndcg(self):
  self.assertAlmostEqual(reference.cosine_similarity([1,0],[1,1]),2**-.5)
  self.assertEqual(reference.cosine_similarity([0,0],[1,0]),0.0)
  self.assertAlmostEqual(reference.ndcg_at_k([3,2,1],3),1.0)
  with self.assertRaises(ValueError): reference.cosine_similarity([1],[1,2])
  with self.assertRaises(ValueError): reference.ndcg_at_k([float('nan')],1)
  with self.assertRaises(NotImplementedError): learner.cosine_similarity([1],[1])
 def test_prediction_routes_through_learner_and_evidence_stays_blocked(self):
  calls=[]
  class Spy:
   def rank_items(self,*a): calls.append('rank_items'); return reference.rank_items(*a)
  result=workflow.predict(EVENTS,'u0',['i0','i1','i2','i9'],adapter=Spy())
  self.assertEqual(calls,['rank_items']); self.assertFalse(result['online_uplift_claim'])
  self.assertEqual(acceptance.evaluate_receipt({"synthetic_fixture":True})['status'],'BLOCKED')
  m={"source_kind":"synthetic","source":"in-memory fixture","license":"CC0","sha256":"a"*64}
  self.assertFalse(reference.validate_dataset_manifest(m)['synthetic_is_evidence'])
  with self.assertRaises(ValueError): reference.validate_dataset_manifest({**m,"license":""})
 def test_comparison_keeps_common_candidate_set_and_routes_metric(self):
  calls=[]
  class Spy:
   def rank_items(self,*a): calls.append('rank'); return reference.rank_items(*a)
   def ndcg_at_k(self,*a): calls.append('ndcg'); return reference.ndcg_at_k(*a)
  full=reference.chronological_split(EVENTS)
  split={k:full[k] for k in ('train','validation','test_frozen')}
  out=workflow.compare_validation(split,'u0',['i0','i1','i2','i9'],3,adapter=Spy())
  self.assertEqual(out['split'],'validation'); self.assertFalse(out['heldout_opened']); self.assertEqual(calls,['rank','ndcg','ndcg'])
  with self.assertRaises(ValueError): workflow.compare_validation(full,'u0',['i0','i1','i2','i9'],3,adapter=Spy())
  contaminated={**split,'train':split['train']+split['validation'][:1]}
  with self.assertRaises(ValueError): workflow.compare_validation(contaminated,'u0',['i0','i1','i2','i9'],3,adapter=Spy())
  with self.assertRaises(ValueError): workflow.compare_validation({**split,'test_frozen':False},'u0',['i0','i1','i2','i9'],3,adapter=Spy())
 def test_local_api_serves_bounded_prediction_via_selected_learner(self):
  manifest={'source_kind':'synthetic','source':'in-memory test fixture','license':'CC0','sha256':'c'*64}
  app=api.create_app(EVENTS,manifest,adapter=reference); client=TestClient(app)
  response=client.post('/recommend',json={'user_id':'u0','candidates':['i0','i1','i2','i9']})
  self.assertEqual(response.status_code,200); self.assertEqual(response.json()['evidence_class'],'synthetic')
  self.assertFalse(response.json()['online_uplift_claim']); self.assertNotIn('i0',[x['item_id'] for x in response.json()['ranked']])
  self.assertEqual(client.post('/recommend',json={'user_id':'u0','candidates':['i1']*101}).status_code,422)
  self.assertEqual(TestClient(api.create_app()).post('/recommend',json={'user_id':'u0','candidates':['i1']}).status_code,503)
if __name__=='__main__': unittest.main()
