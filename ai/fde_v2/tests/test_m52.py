import unittest
import tempfile,json,importlib.util
from pathlib import Path
from fastapi.testclient import TestClient
from ai.fde_v2.m52 import learner,reference,workflow,acceptance,api
EVENTS=[{"event_id":str(i),"user_id":f"u{i%2}","item_id":f"i{i%3}","timestamp":i} for i in range(10)]
class M52Test(unittest.TestCase):
 def test_chronological_frozen_holdout_and_duplicate_rejection(self):
  s=reference.chronological_split(EVENTS)
  self.assertLess(max(x['timestamp'] for x in s['train']),min(x['timestamp'] for x in s['validation']))
  self.assertLess(max(x['timestamp'] for x in s['validation']),min(x['timestamp'] for x in s['heldout']))
  self.assertFalse(s['test_frozen']); self.assertTrue(s['holdout_manifest_required'])
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
  m={"source_kind":"synthetic","source":"in-memory fixture","license":"CC0","sha256":reference.dataset_sha256(EVENTS)}
  self.assertFalse(reference.validate_dataset_manifest(m,EVENTS)['synthetic_is_evidence'])
  with self.assertRaises(ValueError): reference.validate_dataset_manifest({**m,"license":""})
  with self.assertRaises(ValueError): reference.validate_dataset_manifest({**m,"sha256":"a"*64},EVENTS)
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
  self.assertEqual(workflow.compare_validation({**split,'test_frozen':False},'u0',['i0','i1','i2','i9'],3,adapter=Spy())['split'],'validation')
 def test_local_api_serves_bounded_prediction_via_selected_learner(self):
  manifest={'source_kind':'synthetic','source':'in-memory test fixture','license':'CC0','sha256':reference.dataset_sha256(EVENTS)}
  app=api.create_app(EVENTS,manifest,adapter=reference); client=TestClient(app)
  response=client.post('/recommend',json={'user_id':'u0','candidates':['i0','i1','i2','i9']})
  self.assertEqual(response.status_code,200); self.assertEqual(response.json()['evidence_class'],'synthetic')
  self.assertFalse(response.json()['online_uplift_claim']); self.assertNotIn('i0',[x['item_id'] for x in response.json()['ranked']])
  self.assertEqual(client.post('/recommend',json={'user_id':'u0','candidates':['i1']*101}).status_code,422)
  self.assertEqual(TestClient(api.create_app()).post('/recommend',json={'user_id':'u0','candidates':['i1']}).status_code,503)
 def test_frozen_heldout_runner_binds_data_model_split_and_consumes_once(self):
  with tempfile.TemporaryDirectory() as td:
   manifest={'source_kind':'synthetic','source':'locally authored fixture events','license':'CC0',
             'sha256':reference.dataset_sha256(EVENTS)}
   frozen=workflow.freeze_heldout(td,EVENTS,manifest,{'algorithm':'user-item-cosine','version':1},adapter=reference)
   self.assertTrue(frozen['frozen'])
   self.assertNotEqual(frozen['source_sha256'],frozen['training_sha256'])
   rows=json.loads((Path(td)/'heldout.json').read_text())
   self.assertEqual(len(rows),frozen['partition_counts']['heldout'])
   result=workflow.run_heldout_once(td,sorted({event['item_id'] for event in EVENTS}),adapter=reference)
   self.assertEqual(result['classification'],'local_fixture_results_require_review')
   self.assertEqual(result['unit_count'],len(rows))
   self.assertEqual(result['error_count'],0)
   self.assertEqual([row['event_id'] for row in result['rows']],frozen['heldout_event_ids'])
   self.assertFalse(result['learner_completion'])
   with self.assertRaises(FileExistsError):
    workflow.run_heldout_once(td,sorted({event['item_id'] for event in EVENTS}),adapter=reference)
 def test_frozen_runner_rejects_partition_tamper_and_candidate_or_metric_drift(self):
  manifest={'source_kind':'synthetic','source':'locally authored fixture events','license':'CC0',
            'sha256':reference.dataset_sha256(EVENTS)}
  for mutation in ('tamper','candidate','metric'):
   with self.subTest(mutation=mutation),tempfile.TemporaryDirectory() as td:
    frozen=workflow.freeze_heldout(td,EVENTS,manifest,{'algorithm':'user-item-cosine','version':1},adapter=reference)
    candidates=sorted({event['item_id'] for event in EVENTS})
    k=5
    if mutation=='tamper':
     path=Path(td)/'heldout.json'; altered=json.loads(path.read_text()); altered[0]['item_id']='tampered'; path.write_text(json.dumps(altered))
    if mutation=='candidate': candidates=candidates+['new-item']
    if mutation=='metric': k=4
    with self.assertRaises((ValueError,FileExistsError)):
     workflow.run_heldout_once(td,candidates,k=k,adapter=reference)
 def test_bundle_api_reload_uses_hash_verified_saved_training_rows(self):
  with tempfile.TemporaryDirectory() as td:
   manifest={'source_kind':'synthetic','source':'local fixture','license':'CC0',
             'sha256':reference.dataset_sha256(EVENTS)}
   bundle=workflow.save_model_bundle(td,EVENTS,manifest,{'algorithm':'explicit-cosine-sample','version':1},adapter=reference)
   loaded=workflow.load_model_bundle(td,adapter=reference)
   self.assertEqual(loaded['source_manifest']['sha256'],reference.dataset_sha256(EVENTS))
   client=TestClient(api.create_app_from_bundle(td,adapter=reference))
   response=client.post('/recommend',json={'user_id':'u0','candidates':['i0','i1','i2','i9']})
   self.assertEqual(response.status_code,200)
   self.assertEqual(response.json()['dataset_hash'],bundle['data_sha256'])
   self.assertEqual(response.json()['model_code_sha256'],loaded['model']['ranker_code_sha256'])
   self.assertEqual(response.json()['adapter_package_sha256'],loaded['model']['adapter_package_sha256'])
   path=Path(td)/'training-events.json'; events=json.loads(path.read_text()); events[0]['item_id']='mutated'; path.write_text(json.dumps(events))
   with self.assertRaises(ValueError): workflow.load_model_bundle(td,adapter=reference)
 def test_bundle_hash_covers_helper_only_scoring_changes(self):
  with tempfile.TemporaryDirectory() as td:
   root=Path(td); bundle_root=root/'bundle'; source=Path(reference.__file__).read_text(encoding='utf-8')
   manifest={'source_kind':'synthetic','source':'local fixture','license':'CC0',
             'sha256':reference.dataset_sha256(EVENTS)}
   workflow.save_model_bundle(bundle_root,EVENTS,manifest,{'algorithm':'explicit-cosine-sample','version':1},adapter=reference)
   changed=source.replace('dot/(nl*nr)','dot/(nl*nr)*0.5')
   self.assertNotEqual(source,changed)
   helper_path=root/'changed_reference.py'; helper_path.write_text(changed,encoding='utf-8')
   spec=importlib.util.spec_from_file_location('changed_m52_reference',helper_path)
   changed_module=importlib.util.module_from_spec(spec); spec.loader.exec_module(changed_module)
   with self.assertRaisesRegex(ValueError,'code/config hash mismatch'):
    workflow.load_model_bundle(bundle_root,adapter=changed_module)
 def test_default_candidate_catalog_excludes_heldout_only_items(self):
  with tempfile.TemporaryDirectory() as td:
   events=EVENTS+[{'event_id':'late-only','user_id':'u0','item_id':'heldout-only-item','timestamp':1000}]
   manifest={'source_kind':'synthetic','source':'local fixture','license':'CC0',
             'sha256':reference.dataset_sha256(events)}
   frozen=workflow.freeze_heldout(td,events,manifest,{'algorithm':'explicit-cosine-sample','version':1},adapter=reference)
   self.assertNotIn('heldout-only-item',frozen['candidate_items'])
   self.assertEqual(frozen['candidate_items_source'],'training_partition')
   self.assertEqual(frozen['candidate_items_source_sha256'],frozen['training_sha256'])
   self.assertNotIn('heldout-only-item',sorted({row['item_id'] for row in events[:6]}))
  with tempfile.TemporaryDirectory() as td:
   manifest={'source_kind':'synthetic','source':'local fixture','license':'CC0',
             'sha256':reference.dataset_sha256(EVENTS)}
   with self.assertRaises(ValueError):
    workflow.freeze_heldout(td,EVENTS,manifest,{'algorithm':'explicit-cosine-sample','version':1},
                            candidate_items=['heldout-only-item'],adapter=reference)
if __name__=='__main__': unittest.main()
