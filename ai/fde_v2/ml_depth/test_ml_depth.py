"""Instructor mechanics: executable local witnesses, never learner completion."""
from __future__ import annotations

import json
import tempfile
import unittest
from pathlib import Path

from . import learner, reference, workflow


class StatisticsAndClassicalMLTests(unittest.TestCase):
    def setUp(self):
        self.train_x=[[-3.0],[-2.0],[-1.0],[1.0],[2.0],[3.0]]
        self.train_y=[0,0,0,1,1,1]
        self.validation_x=[[-2.5],[-0.5],[0.5],[2.5]]
        self.validation_y=[0,0,1,1]

    def test_losses_intervals_power_and_input_validation(self):
        low=reference.logistic_loss([0.0,1.0],[[1,-2],[1,2]],[0,1])
        self.assertLess(low,reference.logistic_loss([0.0,-1.0],[[1,-2],[1,2]],[0,1]))
        gradient=reference.check_logistic_gradient([.2,.7],[[1,-2],[1,2],[1,.5]],[0,1,1],coordinate=1,l2=.03)
        self.assertLess(gradient['absolute_error'],1e-6)
        self.assertEqual(reference.brier_score([0.0,1.0],[0,1]),0.0)
        self.assertAlmostEqual(reference.expected_calibration_error([0.1,0.9],[0,1],bins=2),0.1)
        self.assertLess(reference.wilson_interval(50,100)[0],0.5)
        self.assertGreater(reference.required_sample_per_arm(.5,.3),1)
        with self.assertRaises(ValueError): reference.required_sample_per_arm(.1,.2)
        with self.assertRaises(ValueError): reference.brier_score([1.2],[1])

    def test_logistic_training_reduces_loss_and_classical_path_calls_selected_adapter(self):
        model=reference.train_logistic([[1,*row] for row in self.train_x],self.train_y,
                                       learning_rate=.1,epochs=120,l2=.01)
        self.assertLess(model['loss_history'][-1],model['loss_history'][0])
        regression=reference.train_linear_regression([[1,*row] for row in self.train_x],[-3,-2,-1,1,2,3],learning_rate=.05,epochs=120,l2=.01)
        self.assertLess(regression['loss_history'][-1],regression['loss_history'][0])
        calls=[]
        class Spy:
            brier_score=staticmethod(reference.brier_score)
            expected_calibration_error=staticmethod(reference.expected_calibration_error)
            def fit_standardizer(self,x): calls.append(('fit_scaler',len(x))); return reference.fit_standardizer(x)
            def transform_features(self,x,s): calls.append(('transform',len(x))); return reference.transform_features(x,s)
            def train_linear_regression(self,*a,**kw): calls.append(('linear',len(a[0]))); return reference.train_linear_regression(*a,**kw)
            def predict_linear_regression(self,*a): return reference.predict_linear_regression(*a)
            def train_logistic(self,*a,**kw): calls.append(('train',len(a[0]))); return reference.train_logistic(*a,**kw)
            def predict_logistic_proba(self,*a): calls.append(('predict',)); return reference.predict_logistic_proba(*a)
        result=workflow.compare_classical(self.train_x,self.train_y,self.validation_x,self.validation_y,
                                          adapter=Spy(),epochs=120)
        self.assertEqual([x[0] for x in calls],['fit_scaler','transform','transform','linear','train','predict'])
        self.assertEqual(calls[0],('fit_scaler',len(self.train_x)))
        self.assertFalse(result['validation_used_for_fit'])
        self.assertEqual(result['logistic']['metrics']['rows'],len(self.validation_y))
        self.assertGreaterEqual(result['boosted_tree']['metrics']['brier'],0.0)
        self.assertLessEqual(result['boosted_tree']['metrics']['brier'],1.0)
        slice_rows=reference.binary_slice_report(self.validation_y,[int(p>=.5) for p in result['logistic']['probabilities']],
                        {'group':['A','A','B','B'],'support':['x','x','x','y']},minimum_support=2)
        self.assertEqual(sum(row['status']=='reported' for row in slice_rows),3)
        self.assertEqual(sum(row['status']=='insufficient_support' for row in slice_rows),1)

    def test_training_transform_does_not_refit_on_validation_distribution(self):
        scaler=reference.fit_standardizer(self.train_x)
        validation=reference.transform_features([[103.0]],scaler)
        self.assertGreater(validation[0][0],10.0)
        self.assertEqual(scaler['fitted_rows'],len(self.train_x))
        with self.assertRaises(ValueError): reference.temporal_split([{'timestamp':1},{'timestamp':2}])

    def test_default_learner_tasks_are_real_integrated_todos(self):
        with self.assertRaises(NotImplementedError):
            workflow.compare_classical(self.train_x,self.train_y,self.validation_x,self.validation_y)
        self.assertEqual(learner.train_logistic.__module__,'ai.fde_v2.ml_depth.learner')

    def test_broken_training_adapter_is_rejected_in_the_workflow(self):
        class Weight:
            def numel(self): return 1
        class Model:
            def state_dict(self): return {'weight':Weight()}
        class Broken:
            def train_torch_model(self,*args,**kwargs):
                return {'model':Model(),'loss_history':[.7,.7],'parameters_changed':False}
        with self.assertRaisesRegex(ValueError,'parameter update'):
            workflow.torch_training_witness(self.train_x,self.train_y,adapter=Broken())


class LearnedRankingTests(unittest.TestCase):
    def setUp(self):
        self.train=[{'user_id':'u1','item_id':'a','timestamp':1},
                    {'user_id':'u1','item_id':'b','timestamp':2},
                    {'user_id':'u2','item_id':'b','timestamp':3},
                    {'user_id':'u2','item_id':'c','timestamp':4},
                    {'user_id':'u3','item_id':'c','timestamp':5},
                    {'user_id':'u3','item_id':'a','timestamp':6}]
        self.eval=[{'user_id':'u1','item_id':'c','timestamp':7},
                   {'user_id':'u2','item_id':'a','timestamp':8},
                   {'user_id':'new-user','item_id':'a','timestamp':9},
                   {'user_id':'new-user-2','item_id':'future-item','timestamp':10}]

    def test_negative_sampling_is_training_catalog_bounded_and_reproducible(self):
        a=reference.sample_negative_pairs(self.train,['a','b','c','d'],per_positive=2,seed=9)
        b=reference.sample_negative_pairs(self.train,['a','b','c','d'],per_positive=2,seed=9)
        self.assertEqual(a,b)
        seen={user:{row['item_id'] for row in self.train if row['user_id']==user} for user in {row['user_id'] for row in self.train}}
        self.assertTrue(all(negative not in seen[user] and negative in {'a','b','c','d'} for user,_,negative in a))
        all_seen=[{'user_id':'u','item_id':'a'},{'user_id':'u','item_id':'b'}]
        with self.assertRaises(ValueError): reference.sample_negative_pairs(all_seen,['a','b'],seed=9)
        with self.assertRaises(ValueError): reference.sample_negative_pairs(self.train+[{'user_id':'u9','item_id':'future','timestamp':0}],['a','b','c','d'],seed=9)

    def test_pairwise_training_lowers_preference_loss_and_has_cold_start(self):
        pairs=reference.sample_negative_pairs(self.train,['a','b','c','d'],per_positive=1,seed=4)
        model=reference.train_pairwise_ranker(pairs,dimensions=8,epochs=150,learning_rate=.05,seed=4)
        self.assertLess(model['loss_history'][-1],model['loss_history'][0])
        self.assertEqual(reference.predict_pairwise_score(model,'unknown','a'),0.0)
        result=workflow.compare_rankers(self.train,self.eval,['a','b','c','d'],adapter=reference,k=3)
        self.assertTrue(result['same_candidate_set'])
        self.assertTrue(result['training_catalog_only'])
        self.assertEqual(result['metrics'].keys(),{'learned_ndcg','popularity_ndcg','cosine_ndcg'})
        cold=next(row for row in result['rows'] if row['user_id']=='new-user')
        self.assertEqual(cold['cold_start_fallback'],'global_popularity')
        future=next(row for row in result['rows'] if row['user_id']=='new-user-2' and row['status']!='scored')
        self.assertEqual(future['status'],'not_scored_cold_or_unavailable_target')
        self.assertFalse(result['heldout_performance_claim'])
        slices=workflow.summarize_ranker_slices(result,{'u1':{'activity':'low'},'u2':{'activity':'high'},'new-user':{'activity':'cold'}},min_support=2)
        self.assertTrue(all(row['status']=='insufficient_support' for row in slices['slices']))

    def test_default_rank_workflow_cannot_silently_use_reference(self):
        with self.assertRaises(NotImplementedError):
            workflow.compare_rankers(self.train,self.eval,['a','b','c','d'])


class ExperimentAndLifecycleTests(unittest.TestCase):
    def spec(self):
        return {'experiment_id':'synthetic-growth-01','unit':'user','primary_metric':'conversion',
            'metric_direction':'higher_is_better','baseline_rate':.5,'mde':.3,'alpha':.05,'power':.8,
            'allocation':{'control':.5,'treatment':.5},'fixed_horizon_units':500,
            'guardrails':['opt_out_rate'],
            'guardrail_limits':{'opt_out_rate':{'adverse_direction':'higher','max_adverse_delta':0.02}},
            'stopping_rule':'fixed_horizon_with_safety_stop'}

    def test_experiment_requires_power_fixed_horizon_and_safe_assignment(self):
        plan=workflow.experiment_assignment([f'u{i}' for i in range(500)],self.spec(),adapter=reference)
        self.assertEqual(set(plan['assignments']),{f'u{i}' for i in range(500)})
        replay=reference.assign_experiment_units([f'u{i}' for i in range(500)],experiment_id='synthetic-growth-01',seed=31)
        self.assertEqual(plan['assignments'],replay)
        self.assertFalse(plan['online_run'])
        outcomes={u:(1.0 if arm=='treatment' else 0.0) for u,arm in plan['assignments'].items()}
        report=workflow.analyze_experiment(plan['assignments'],outcomes,design=plan['design'],adapter=reference,
              guardrails={'opt_out_rate':{u:(1.0 if arm=='treatment' else 0.0) for u,arm in plan['assignments'].items()}})
        self.assertIn(report['status'],{'SYNTHETIC_ANALYSIS_ONLY','INVALID_ASSIGNMENT_SRM'})
        if report['status']=='SYNTHETIC_ANALYSIS_ONLY':
            self.assertTrue(report['safety_stop_triggered'])
            self.assertIsInstance(report['predeclared_primary_signal'],bool)
        with self.assertRaises(ValueError): workflow.experiment_assignment(['u1'],{**self.spec(),'stopping_rule':'peek until significant'},adapter=reference)
        with self.assertRaises(ValueError): workflow.experiment_assignment(['u1'],{**self.spec(),'fixed_horizon_units':2},adapter=reference)
        with self.assertRaises(ValueError): workflow.experiment_assignment(['u1'],self.spec(),adapter=reference)

    def test_srm_stops_outcome_analysis_and_retraining_separates_drift(self):
        assignments={f'u{i}':('treatment' if i<5 else 'control') for i in range(100)}
        outcomes={u:0.5 for u in assignments}
        result=workflow.analyze_experiment(assignments,outcomes,design={'required_total':100},adapter=reference)
        self.assertEqual(result['status'],'INVALID_ASSIGNMENT_SRM')
        self.assertFalse(result['outcome_analysis_performed'])
        drift_only=reference.retraining_decision({'drift_score':.5,'quality_metric':.8,'baseline_quality':.8,'sample_count':500},
                                                  {'max_drift':.2,'min_quality_delta':.05})
        self.assertTrue(drift_only['drift_alert']); self.assertFalse(drift_only['retraining_recommended'])
        degraded=reference.retraining_decision({'drift_score':.5,'quality_metric':.6,'baseline_quality':.8,'sample_count':500},
                                                {'max_drift':.2,'min_quality_delta':.05})
        self.assertTrue(degraded['retraining_recommended'])

    def test_versioned_model_is_consumed_by_serving_and_can_roll_back(self):
        x=[[-2.0],[-1.0],[1.0],[2.0]]; y=[0,0,1,1]
        scaler=reference.fit_standardizer(x); z=reference.transform_features(x,scaler)
        design=[[1.0,*row] for row in z]
        model=reference.train_logistic(design,y,learning_rate=.1,epochs=150,l2=.01)
        with tempfile.TemporaryDirectory() as td:
            workflow.write_linear_bundle(td,version='v1',model=model,scaler=scaler,feature_names=['signal'],
                                         training_data_sha256='a'*64)
            workflow.promote_model(td,'v1')
            served=workflow.serve_logistic(td,[{'signal':-2.0},{'signal':2.0}],adapter=reference)
            self.assertLess(served['scores'][0],served['scores'][1])
            expected=reference.predict_logistic_proba(model,[design[0],design[-1]])
            self.assertEqual(len(served['scores']),len(expected))
            for actual,want in zip(served['scores'],expected): self.assertAlmostEqual(actual,want,places=12)
            self.assertEqual(served['model_version'],'v1')
            altered={**model,'weights':[0.0,0.0]}
            workflow.write_linear_bundle(td,version='v2',model=altered,scaler=scaler,feature_names=['signal'],training_data_sha256='b'*64)
            workflow.promote_model(td,'v2')
            self.assertEqual(workflow.rollback_model(td)['version'],'v1')
            self.assertEqual(workflow.serve_logistic(td,[{'signal':2.0}],adapter=reference)['model_version'],'v1')
            with self.assertRaises(ValueError): workflow.serve_logistic(td,[{'different':2.0}],adapter=reference)
            artifact=Path(td)/'v1'/'artifact.json'; data=json.loads(artifact.read_text()); data['model']['weights'][0]+=1
            artifact.write_text(json.dumps(data))
            with self.assertRaises(ValueError): workflow.serve_logistic(td,[{'signal':2.0}],adapter=reference)


try:
    import torch  # noqa: F401
    TORCH_AVAILABLE=True
except ImportError:
    TORCH_AVAILABLE=False


@unittest.skipUnless(TORCH_AVAILABLE,"pinned PyTorch is not installed in this local interpreter")
class RealTorchTrainingTests(unittest.TestCase):
    def test_real_cpu_parameter_updates_checkpoint_reload_and_resume(self):
        x=[[-2,-1],[-1,-2],[-1,-1],[1,1],[1,2],[2,1]]
        y=[0,0,0,1,1,1]
        trained=workflow.torch_training_witness(x,y,adapter=reference,epochs=80,learning_rate=.03,seed=5)
        self.assertLess(trained['loss_history'][-1],trained['loss_history'][0])
        self.assertEqual(trained['device'],'cpu'); self.assertFalse(trained['pretrained_weights'])
        torch=__import__('torch')
        with tempfile.TemporaryDirectory() as td:
            path=Path(td)/'model.pt'; manifest={'seed':5,'data_sha256':'b'*64,'feature_names':['x1','x2'],'epochs':80}
            workflow.save_torch_checkpoint(path,trained,manifest=manifest)
            restored=workflow.load_torch_checkpoint(path,build_model=lambda:reference.build_torch_model(2,seed=5),expected_manifest=manifest)
            torch.testing.assert_close(restored['model'](torch.tensor(x,dtype=torch.float32)),trained['model'](torch.tensor(x,dtype=torch.float32)))
            resumed=workflow.resume_torch_training(path,x,y,build_model=lambda:reference.build_torch_model(2,seed=5),
                  adapter=reference,expected_manifest=manifest,epochs=10,learning_rate=.03)
            self.assertLess(resumed['loss_history'][-1],resumed['loss_history'][0])
            with self.assertRaises(ValueError): workflow.load_torch_checkpoint(path,build_model=lambda:reference.build_torch_model(2),expected_manifest={**manifest,'seed':99})

    def test_small_batch_can_be_overfit_and_resume_matches_uninterrupted(self):
        import torch
        x=[[-2,-1],[-1,-2],[1,1],[2,1]]; y=[0,0,1,1]
        trained=reference.train_torch_model(x,y,seed=13,epochs=200,learning_rate=.03)
        self.assertLess(trained['loss_history'][-1],trained['loss_history'][0]*.1)
        predicted=[int(p>=.5) for p in trained['probabilities']]
        self.assertEqual(predicted,y)
        first=reference.train_torch_model(x,y,seed=21,epochs=10,learning_rate=.02)
        resumed=reference.train_torch_model(x,y,seed=21,epochs=10,learning_rate=.02,
                    initial_state=first['model'].state_dict(),optimizer_state=first['optimizer_state_dict'])
        full=reference.train_torch_model(x,y,seed=21,epochs=20,learning_rate=.02)
        for key,value in resumed['model'].state_dict().items(): torch.testing.assert_close(value,full['model'].state_dict()[key])


if __name__=='__main__': unittest.main()
