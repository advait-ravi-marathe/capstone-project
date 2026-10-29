"""Acceptance selectors for learner implementations; reference use is explicit."""
import importlib
import importlib.util
import os
import tempfile
import unittest
from pathlib import Path

from . import workflow

candidate=importlib.import_module(os.environ.get("ML_DEPTH_CANDIDATE","ai.fde_v2.ml_depth.learner"))
X=[[-3.0],[-2.0],[-1.0],[1.0],[2.0],[3.0]]
Y=[0,0,0,1,1,1]
EVENTS=[{"user_id":"u1","item_id":"a","timestamp":1},
        {"user_id":"u1","item_id":"b","timestamp":2},
        {"user_id":"u2","item_id":"b","timestamp":3},
        {"user_id":"u2","item_id":"c","timestamp":4},
        {"user_id":"u3","item_id":"c","timestamp":5},
        {"user_id":"u3","item_id":"a","timestamp":6}]


class MLDepthLearnerAcceptance(unittest.TestCase):
    def test_linear_regression_baseline(self):
        features=[[1.0,-2],[1.0,-1],[1.0,1],[1.0,2]]; targets=[-3,-1,1,3]
        model=candidate.train_linear_regression(features,targets,learning_rate=.08,epochs=200)
        self.assertLess(model['loss_history'][-1],model['loss_history'][0])
        predictions=candidate.predict_linear_regression(model,features)
        model_mse=sum((p-y)**2 for p,y in zip(predictions,targets))/len(targets)
        mean=sum(targets)/len(targets)
        mean_mse=sum((mean-y)**2 for y in targets)/len(targets)
        self.assertLess(model_mse,mean_mse)

    def test_declared_threshold_cost_rule(self):
        result=candidate.threshold_cost_analysis([.1,.4,.6,.9],[0,1,0,1],false_positive_cost=1,false_negative_cost=5)
        self.assertEqual(result['total_cost'],1)
        self.assertEqual((result['threshold'],result['false_positives'],result['false_negatives']),(.4,1,0))

    def test_training_catalog_negative_sampling(self):
        train=EVENTS+[{'user_id':'u4','item_id':'d','timestamp':7}]
        pairs=candidate.sample_negative_pairs(train,['a','b','c','d','e'],per_positive=2,seed=23)
        seen={u:{row['item_id'] for row in train if row['user_id']==u} for u in {row['user_id'] for row in train}}
        self.assertTrue(pairs)
        self.assertTrue(all(negative not in seen[user] and negative in {'a','b','c','d','e'}
                            for user,_positive,negative in pairs))
        with self.assertRaises(ValueError): candidate.sample_negative_pairs(
            [{'user_id':'u','item_id':'a'},{'user_id':'u','item_id':'b'}],['a','b'],seed=23)

    def test_pairwise_ranker_learns_and_scores_pairs(self):
        pairs=candidate.sample_negative_pairs(EVENTS,['a','b','c','d'],per_positive=1,seed=4)
        model=candidate.train_pairwise_ranker(pairs,dimensions=4,epochs=120,learning_rate=.05,seed=4)
        self.assertLess(model['loss_history'][-1],model['loss_history'][0])
        self.assertNotEqual(candidate.predict_pairwise_score(model,'u1','a'),
                            candidate.predict_pairwise_score(model,'u1','d'))

    def test_ndcg_cutoff_and_tie_behavior(self):
        self.assertAlmostEqual(candidate.ndcg_at_k([1,0,1],2),1/(1+1/__import__('math').log2(3)))
        self.assertEqual(candidate.ndcg_at_k([0,0],2),0.0)
        with self.assertRaises(ValueError): candidate.ndcg_at_k([1],0)

    def test_experiment_design_rejects_underpowered_fixed_horizon(self):
        spec={'experiment_id':'acceptance','unit':'user','primary_metric':'conversion',
              'metric_direction':'higher_is_better','baseline_rate':.5,'mde':.3,'alpha':.05,'power':.8,
              'allocation':{'control':.5,'treatment':.5},'fixed_horizon_units':20,
              'guardrails':['opt_out'],'guardrail_limits':{'opt_out':{'adverse_direction':'higher','max_adverse_delta':.02}},
              'stopping_rule':'fixed_horizon_with_safety_stop'}
        with self.assertRaises(ValueError): candidate.design_experiment(spec)

    def test_assignment_is_stable_balanced_and_unit_bound(self):
        units=[f'user-{i}' for i in range(500)]
        first=candidate.assign_experiment_units(units,experiment_id='stable',seed=29)
        self.assertEqual(first,candidate.assign_experiment_units(units,experiment_id='stable',seed=29))
        self.assertEqual(set(first),set(units))
        treatment=sum(arm=='treatment' for arm in first.values())
        self.assertGreater(treatment,180); self.assertLess(treatment,320)

    def test_srm_boundary_blocks_outcome_analysis(self):
        self.assertFalse(candidate.srm_check(50,50)['sample_ratio_mismatch'])
        self.assertTrue(candidate.srm_check(90,10)['sample_ratio_mismatch'])

    def test_ab_estimate_uses_unit_level_samples(self):
        result=candidate.analyze_ab([0,0,1,1],[1,1,2,2])
        self.assertAlmostEqual(result['control_mean'],.5)
        self.assertAlmostEqual(result['treatment_mean'],1.5)
        self.assertAlmostEqual(result['absolute_effect'],1.0)
        self.assertGreater(result['standard_error'],0)
        with self.assertRaises(ValueError): candidate.analyze_ab([1],[2])

    def test_retraining_requires_drift_and_quality_degradation(self):
        thresholds={'max_drift':.2,'min_quality_delta':.05}
        drift=candidate.retraining_decision({'drift_score':.5,'quality_metric':.8,'baseline_quality':.8,'sample_count':100},thresholds)
        degraded=candidate.retraining_decision({'drift_score':.5,'quality_metric':.6,'baseline_quality':.8,'sample_count':100},thresholds)
        self.assertTrue(drift['drift_alert']); self.assertFalse(drift['retraining_recommended'])
        self.assertTrue(degraded['observed_quality_degradation']); self.assertTrue(degraded['retraining_recommended'])

    def test_regression_losses_compare_squared_absolute_and_huber_on_outlier(self):
        result=workflow.compare_regression_losses([0.0,10.0],[0.0,0.0],huber_delta=1.0,adapter=candidate)
        self.assertAlmostEqual(result['mse'],50.0)
        self.assertAlmostEqual(result['mae'],5.0)
        self.assertAlmostEqual(result['huber'],4.75)
        self.assertLess(result['huber'],result['mse'])

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_dataset_loader_workflow_rejects_a_non_partition(self):
        class Broken:
            @staticmethod
            def make_torch_loaders(features,labels,**kwargs):
                return candidate.make_torch_loaders(features,labels,**kwargs) | {
                    'validation_indices':candidate.make_torch_loaders(features,labels,**kwargs)['train_indices'][:1]}
        with self.assertRaisesRegex(ValueError,'disjoint full partition'):
            workflow.prepare_torch_data([[float(i)] for i in range(10)],[i%2 for i in range(10)],adapter=Broken())

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_dataset_loader_workflow_rejects_claimed_indices_over_different_tensors(self):
        import torch
        from torch.utils.data import DataLoader, TensorDataset, Subset
        class Liar:
            @staticmethod
            def make_torch_loaders(features,labels,**kwargs):
                good=candidate.make_torch_loaders(features,labels,**kwargs)
                wrong=TensorDataset(torch.full((len(features),1),999.0),torch.tensor(labels,dtype=torch.float32))
                indices=good['train_indices']; validation=good['validation_indices']
                return good | {'train_loader':DataLoader(Subset(wrong,indices),batch_size=4)}
        with self.assertRaisesRegex(ValueError,'exactly match'):
            workflow.prepare_torch_data([[float(i)] for i in range(10)],[i%2 for i in range(10)],adapter=Liar())

    def test_default_regression_dataloader_and_backfill_tasks_remain_todos(self):
        from . import learner
        with self.assertRaises(NotImplementedError): workflow.compare_regression_losses([0,1],[0,1])
        with self.assertRaises(NotImplementedError): learner.reconcile_delayed_labels([])
        with self.assertRaises(NotImplementedError): learner.logistic_gradient([0,1],[[1,0],[1,1]],[0,1])
        with self.assertRaises(NotImplementedError): learner.pairwise_loss_gradient([0.1],[0.2],[0.3])
        self.assertIn('make_torch_loaders',learner.make_torch_loaders.__doc__ or learner.make_torch_loaders.__name__)

    def test_loss_threshold_calibration_and_ranking_metrics_have_hand_cases(self):
        import math
        features=[[1.0,-2.0],[1.0,2.0]]; labels=[0,1]
        self.assertLess(candidate.logistic_loss([0.0,2.0],features,labels),
                        candidate.logistic_loss([0.0,-2.0],features,labels))
        self.assertTrue(math.isfinite(candidate.logistic_loss([0.0,1000.0],features,labels)))
        cost=candidate.threshold_cost_analysis([.1,.4,.6,.9],[0,1,0,1],false_positive_cost=1,false_negative_cost=5)
        self.assertEqual(cost['total_cost'],1)
        self.assertEqual((cost['threshold'],cost['false_positives'],cost['false_negatives']),(.4,1,0))
        self.assertAlmostEqual(candidate.brier_score([.2,.8],[0,1]),.04)
        self.assertAlmostEqual(candidate.expected_calibration_error([.2,.8],[0,1],bins=2),.2)
        self.assertAlmostEqual(candidate.ndcg_at_k([0,1,0],2),1/__import__('math').log2(3))
        with self.assertRaises(ValueError): candidate.logistic_loss([1.0],features,labels)

    def test_regularized_logistic_fit_and_train_only_transform(self):
        model=candidate.train_logistic([[1.0,-2],[1.0,-1],[1.0,1],[1.0,2]],[0,0,1,1],
                                       learning_rate=.1,epochs=100,l2=.02)
        self.assertLess(model['loss_history'][-1],model['loss_history'][0])
        self.assertEqual([int(p>=.5) for p in candidate.predict_logistic_proba(model,[[1.0,-2],[1.0,2]])],[0,1])
        scaler=candidate.fit_standardizer([[-2.0],[2.0]])
        transformed=candidate.transform_features([[100.0]],scaler)
        self.assertEqual(scaler['fit_partition'],'train'); self.assertEqual(scaler['fitted_rows'],2)
        self.assertGreater(transformed[0][0],20)

    def test_logistic_gradient_matches_finite_difference_with_l2(self):
        features=[[1.0,-2.0],[1.0,-.5],[1.0,1.0],[1.0,3.0]]
        labels=[0,0,1,1]; weights=[.2,.7]; l2=.13; epsilon=1e-6
        numeric=[]
        for index in range(len(weights)):
            plus=list(weights); minus=list(weights)
            plus[index]+=epsilon; minus[index]-=epsilon
            numeric.append((candidate.logistic_loss(plus,features,labels,l2=l2)-
                            candidate.logistic_loss(minus,features,labels,l2=l2))/(2*epsilon))
        actual=candidate.logistic_gradient(weights,features,labels,l2=l2)
        self.assertEqual(len(actual),len(numeric))
        for observed,expected in zip(actual,numeric): self.assertAlmostEqual(observed,expected,places=6)

    def test_pairwise_vector_gradients_match_finite_difference(self):
        user=[.2,-.4]; positive=[.7,.1]; negative=[-.2,.5]; epsilon=1e-6
        actual=candidate.pairwise_loss_gradient(user,positive,negative,l2=.03)
        for vector_name,key in (('user','user_gradient'),('positive','positive_gradient'),('negative','negative_gradient')):
            vector=list({'user':user,'positive':positive,'negative':negative}[vector_name])
            for index in range(len(vector)):
                plus={name:list(values) for name,values in (('user',user),('positive',positive),('negative',negative))}
                minus={name:list(values) for name,values in (('user',user),('positive',positive),('negative',negative))}
                plus[vector_name][index]+=epsilon; minus[vector_name][index]-=epsilon
                numeric=(candidate.pairwise_loss_gradient(plus['user'],plus['positive'],plus['negative'],l2=.03)['loss']-
                         candidate.pairwise_loss_gradient(minus['user'],minus['positive'],minus['negative'],l2=.03)['loss'])/(2*epsilon)
                self.assertAlmostEqual(actual[key][index],numeric,places=6)

    def test_experiment_statistics_and_retraining_rules(self):
        spec={'experiment_id':'acceptance','unit':'user','primary_metric':'conversion',
              'metric_direction':'higher_is_better','baseline_rate':.5,'mde':.3,'alpha':.05,'power':.8,
              'allocation':{'control':.5,'treatment':.5},'fixed_horizon_units':500,
              'guardrails':['opt_out_rate'],'guardrail_limits':{'opt_out_rate':{'adverse_direction':'higher','max_adverse_delta':.02}},
              'stopping_rule':'fixed_horizon_with_safety_stop'}
        design=candidate.design_experiment(spec)
        units=[f'u{i}' for i in range(500)]
        assignment=candidate.assign_experiment_units(units,experiment_id='acceptance',seed=5)
        self.assertEqual(assignment,candidate.assign_experiment_units(units,experiment_id='acceptance',seed=5))
        self.assertFalse(candidate.srm_check(250,250)['sample_ratio_mismatch'])
        self.assertTrue(candidate.srm_check(350,150)['sample_ratio_mismatch'])
        result=candidate.analyze_ab([0,0,1,1],[1,1,2,2])
        self.assertAlmostEqual(result['absolute_effect'],1.0); self.assertGreaterEqual(result['standard_error'],0)
        drift=candidate.retraining_decision({'drift_score':.5,'quality_metric':.8,'baseline_quality':.8,'sample_count':100},
                                             {'max_drift':.2,'min_quality_delta':.05})
        degraded=candidate.retraining_decision({'drift_score':.5,'quality_metric':.6,'baseline_quality':.8,'sample_count':100},
                                                {'max_drift':.2,'min_quality_delta':.05})
        self.assertFalse(drift['retraining_recommended']); self.assertTrue(degraded['retraining_recommended'])
        self.assertGreater(design['required_total'],0)

    def test_candidate_analysis_waits_for_declared_horizon_before_calling_evaluator(self):
        class Spy:
            called=False
            @staticmethod
            def srm_check(control,treatment): return {'sample_ratio_mismatch':False,'outcome_analysis_allowed':True}
            @classmethod
            def analyze_ab(cls,control,treatment):
                cls.called=True
                return candidate.analyze_ab(control,treatment)
        assignments={f'u{i}':('control' if i%2==0 else 'treatment') for i in range(100)}
        outcomes={unit:float(index%2) for index,unit in enumerate(assignments)}
        design={'required_total':80,'fixed_horizon_units':500,
                'spec':{'fixed_horizon_units':500,'guardrails':[],'guardrail_limits':{},
                        'alpha':.05,'metric_direction':'higher_is_better'}}
        with self.assertRaisesRegex(ValueError,'declared fixed horizon'):
            workflow.analyze_experiment(assignments,outcomes,design=design,adapter=Spy,guardrails={})
        self.assertFalse(Spy.called)

    def test_learner_logistic_and_training_only_transform_drive_comparison(self):
        result=workflow.compare_classical(X,Y,[[-2.5],[-.5],[.5],[2.5]],[0,0,1,1],adapter=candidate,epochs=120)
        self.assertLess(result["logistic"]["model"]["loss_history"][-1],result["logistic"]["model"]["loss_history"][0])
        self.assertEqual(result["transform"]["fit_partition"],"train")

    def test_candidate_boosted_comparison_uses_same_validation_rows(self):
        validation_x=[[-2.5],[-.5],[.5],[2.5]]; validation_y=[0,0,1,1]
        result=workflow.compare_classical(X,Y,validation_x,validation_y,adapter=candidate,epochs=120)
        self.assertEqual(result['logistic']['metrics']['rows'],len(validation_x))
        self.assertEqual(result['boosted_tree']['metrics']['rows'],len(validation_x))
        self.assertEqual(len(result['logistic']['probabilities']),len(result['boosted_tree']['probabilities']))
        self.assertFalse(result['validation_used_for_fit'])
        self.assertFalse(result['model_winner_claim'])

    def test_learner_pairwise_ranker_is_used_by_comparison_path(self):
        result=workflow.compare_rankers(EVENTS,[{"user_id":"u1","item_id":"c","timestamp":7}],
                                        ["a","b","c","d"],adapter=candidate,k=3)
        self.assertTrue(result["same_candidate_set"])
        self.assertEqual(result["scored_users"],1)
        pairs=candidate.sample_negative_pairs(EVENTS,['a','b','c','d'],per_positive=1,seed=4)
        model=candidate.train_pairwise_ranker(pairs,dimensions=4,epochs=100,learning_rate=.05,seed=4)
        self.assertLess(model['loss_history'][-1],model['loss_history'][0])
        self.assertNotEqual(candidate.predict_pairwise_score(model,'u1','a'),
                            candidate.predict_pairwise_score(model,'u1','d'))

    def test_learner_experiment_design_and_assignment_are_integrated(self):
        spec={"experiment_id":"learner-test","unit":"user","primary_metric":"conversion",
              "metric_direction":"higher_is_better","baseline_rate":.5,"mde":.3,"alpha":.05,"power":.8,
              "allocation":{"control":.5,"treatment":.5},"fixed_horizon_units":500,
              "guardrails":["opt_out_rate"],
              "guardrail_limits":{"opt_out_rate":{"adverse_direction":"higher","max_adverse_delta":.02}},
              "stopping_rule":"fixed_horizon_with_safety_stop"}
        result=workflow.experiment_assignment([f"u{i}" for i in range(500)],spec,adapter=candidate)
        self.assertEqual(len(result["assignments"]),500)
        self.assertFalse(result["online_run"])

    def test_candidate_model_artifact_is_consumed_by_serving(self):
        train_x=[[-3.0],[-2.0],[-1.0],[1.0],[2.0],[3.0]]; train_y=[0,0,0,1,1,1]
        scaler=candidate.fit_standardizer(train_x)
        scaled=candidate.transform_features(train_x,scaler)
        design=[[1.0,*row] for row in scaled]
        model=candidate.train_logistic(design,train_y,learning_rate=.1,epochs=180,l2=.01)
        rows=[{'signal':-2.5},{'signal':2.5}]
        with tempfile.TemporaryDirectory() as directory:
            workflow.write_linear_bundle(directory,version='learner-v1',model=model,scaler=scaler,
                feature_names=['signal'],training_data_sha256='d'*64)
            workflow.promote_model(directory,'learner-v1')
            served=workflow.serve_logistic(directory,rows,adapter=candidate)
            raw=candidate.transform_features([[-2.5],[2.5]],scaler)
            expected=candidate.predict_logistic_proba(model,[[1.0,*row] for row in raw])
        self.assertEqual(served['model_version'],'learner-v1')
        for actual,want in zip(served['scores'],expected): self.assertAlmostEqual(actual,want,places=12)

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_torch_task_runs_actual_training_adapter(self):
        features=[[-2,-1],[-1,-2],[-1,-1],[1,1],[1,2],[2,1]]
        trained=workflow.torch_training_witness(features,Y,adapter=candidate,seed=5,epochs=80)
        self.assertLess(trained["loss_history"][-1],trained["loss_history"][0])
        self.assertTrue(trained["observed_parameters_changed"])
        self.assertLess(trained["observed_final_loss"],trained["observed_initial_loss"])

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_torch_dataset_loaders_partition_the_actual_training_pool(self):
        features=[[float(i),float(i%2)] for i in range(20)]; labels=[i%2 for i in range(20)]
        prepared=workflow.prepare_torch_data(features,labels,validation_fraction=.2,batch_size=4,seed=17,adapter=candidate)
        self.assertEqual(set(prepared['train_indices'])|set(prepared['validation_indices']),set(range(20)))
        self.assertFalse(set(prepared['train_indices'])&set(prepared['validation_indices']))
        again=workflow.prepare_torch_data(features,labels,validation_fraction=.2,batch_size=4,seed=17,adapter=candidate)
        self.assertEqual(prepared['train_indices'],again['train_indices'])
        self.assertEqual(prepared['validation_indices'],again['validation_indices'])
        self.assertEqual(sum(len(batch[1]) for batch in prepared['validation_loader']),4)

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_candidate_checkpoint_resume_uses_saved_optimizer_and_weights(self):
        import torch
        from . import reference
        features=[[-2.0,-1.0],[-1.0,-2.0],[-1.0,-1.0],[1.0,1.0],[1.0,2.0],[2.0,1.0]]
        labels=[0,0,0,1,1,1]
        initial=reference.build_torch_model(2,seed=31).state_dict()
        uninterrupted=candidate.train_torch_model(features,labels,seed=31,epochs=20,learning_rate=.03,
                                                   initial_state=initial)
        first=workflow.torch_training_witness(features,labels,adapter=candidate,seed=31,epochs=12,
                                               initial_state=initial)
        manifest={'seed':31,'fixture':'candidate-resume-v1','epochs':12}
        with tempfile.TemporaryDirectory() as directory:
            path=Path(directory)/'checkpoint.pt'
            workflow.save_torch_checkpoint(path,first,manifest=manifest)
            restored=workflow.resume_torch_training(path,features,labels,
                build_model=lambda:reference.build_torch_model(2,seed=31),adapter=candidate,
                expected_manifest=manifest,epochs=8,learning_rate=.03)
        for name,value in restored['model'].state_dict().items():
            torch.testing.assert_close(value,uninterrupted['model'].state_dict()[name],rtol=1e-5,atol=1e-6)
        self.assertTrue(restored['observed_parameters_changed'])
        self.assertLess(restored['observed_final_loss'],restored['observed_initial_loss'])

    def test_delayed_label_reconciliation_is_versioned_and_idempotent(self):
        records=[{'event_id':'e1','user_id':'u1','item_id':'a','event_time':10,'arrival_time':12,'label':0,'label_version':1},
                 {'event_id':'e2','user_id':'u2','item_id':'b','event_time':11,'arrival_time':15,'label':1,'label_version':1},
                 {'event_id':'e1','user_id':'u1','item_id':'a','event_time':10,'arrival_time':20,'label':1,'label_version':2}]
        result=candidate.reconcile_delayed_labels(records)
        self.assertEqual(result,candidate.reconcile_delayed_labels(result+result))
        self.assertEqual((result[0]['event_time'],result[0]['arrival_time'],result[0]['label']),(10,20,1))
        self.assertEqual(len(result),2)
        with self.assertRaises(ValueError): candidate.reconcile_delayed_labels(records+[dict(records[-1],label=0)])


if __name__=="__main__": unittest.main()
