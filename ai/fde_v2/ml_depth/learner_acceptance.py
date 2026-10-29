"""Acceptance selectors for learner implementations; reference use is explicit."""
import importlib
import importlib.util
import os
import tempfile
import unittest
from pathlib import Path
from unittest import mock

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

    def test_candidate_fixed_horizon_covers_power_minimum(self):
        small={'experiment_id':'power','unit':'user','primary_metric':'rate','metric_direction':'higher_is_better',
               'baseline_rate':.5,'mde':.3,'alpha':.05,'power':.8,'allocation':{'control':.5,'treatment':.5},
               'fixed_horizon_units':500,'guardrails':['error_rate'],
               'guardrail_limits':{'error_rate':{'adverse_direction':'higher','max_adverse_delta':.02}},
               'stopping_rule':'fixed_horizon_with_safety_stop'}
        stricter={**small,'power':.9}
        ordinary=candidate.design_experiment(small); high_power=candidate.design_experiment(stricter)
        self.assertLessEqual(ordinary['required_total'],ordinary['spec']['fixed_horizon_units'])
        self.assertLessEqual(high_power['required_total'],high_power['spec']['fixed_horizon_units'])
        self.assertGreaterEqual(high_power['required_total'],ordinary['required_total'])

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
    def test_dataset_loader_workflow_rejects_duplicate_partition_indices(self):
        from torch.utils.data import DataLoader, Subset
        class Duplicate:
            @staticmethod
            def make_torch_loaders(features,labels,**kwargs):
                good=candidate.make_torch_loaders(features,labels,**kwargs)
                indexes=list(good['train_indices'])
                duplicated=indexes+[indexes[0]]
                base=good['train_loader'].dataset.dataset
                return good | {'train_indices':duplicated,
                               'train_loader':DataLoader(Subset(base,duplicated),batch_size=4)}
        with self.assertRaisesRegex(ValueError,'disjoint full partition'):
            workflow.prepare_torch_data([[float(i)] for i in range(10)],[i%2 for i in range(10)],adapter=Duplicate())

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

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_candidate_batch_validator_returns_checked_cpu_float32_tensors(self):
        features=[[.1,.2],[-.3,.4]]; labels=[0,1]
        batch=workflow.validate_torch_batch(features,labels,adapter=candidate)
        self.assertEqual(tuple(batch['features'].shape),(2,2))
        self.assertEqual(tuple(batch['targets'].shape),(2,1))
        self.assertEqual(batch['feature_dtype'],'torch.float32')
        self.assertEqual(batch['device'],'cpu')
        with self.assertRaises(ValueError): workflow.validate_torch_batch([[float('nan')],[0.0]],[0,1],adapter=candidate)
        with self.assertRaises(ValueError): workflow.validate_torch_batch([[1.0],[2.0]],[0,1,0],adapter=candidate)
        with self.assertRaises(ValueError): workflow.validate_torch_batch([[1.0],[2.0]],[0,2],adapter=candidate)

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_candidate_mlp_forward_has_one_finite_logit_per_row(self):
        witness=workflow.torch_forward_witness([[-1.,0.],[1.,0.],[0.,1.]],[0,1,1],adapter=candidate,seed=11)
        self.assertEqual(tuple(witness['logits'].shape),(3,1))
        self.assertTrue(__import__('torch').isfinite(witness['logits']).all())
        self.assertEqual(witness['feature_dim'],2)

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_candidate_builder_is_the_model_executed_by_the_training_workflow(self):
        import torch
        class AlternateBuilder:
            validate_torch_batch=staticmethod(candidate.validate_torch_batch)
            @staticmethod
            def build_torch_model(input_dim,*,seed=7):
                torch.manual_seed(seed)
                return torch.nn.Sequential(torch.nn.Linear(input_dim,2),torch.nn.Tanh(),torch.nn.Linear(2,1))
        witness=workflow.torch_forward_witness([[-1.],[1.]],[0,1],adapter=AlternateBuilder(),seed=5)
        self.assertEqual(tuple(witness['logits'].shape),(2,1))
        self.assertEqual(sum(parameter.numel() for parameter in witness['model'].parameters()),7)
        class LinearOnly(AlternateBuilder):
            @staticmethod
            def build_torch_model(input_dim,*,seed=7): return torch.nn.Linear(input_dim,1)
        with self.assertRaisesRegex(ValueError,'two-layer MLP'):
            workflow.torch_forward_witness([[-1.],[1.]],[0,1],adapter=LinearOnly())

    def test_candidate_autograd_and_central_difference_match_supplied_math(self):
        for x in (1.25,-.5):
            observed=workflow.autograd_scalar_witness(x,adapter=candidate)
            self.assertAlmostEqual(observed['value'],3*x*x+2*x+1,places=10)
            self.assertAlmostEqual(observed['gradient'],6*x+2,places=10)
        difference=workflow.finite_difference_witness(lambda value:value**3-2*value,1.3,epsilon=1e-6,adapter=candidate)
        self.assertAlmostEqual(difference['gradient'],3*1.3**2-2,places=8)

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_candidate_dropout_probe_observes_train_variance_eval_stability_and_restores_mode(self):
        import torch
        model=torch.nn.Sequential(torch.nn.Dropout(p=.7),torch.nn.Linear(32,4))
        batch=torch.ones((2,32))
        model.train()
        result=workflow.dropout_mode_witness(model,batch,repeats=5,adapter=candidate)
        self.assertTrue(result['train_varies']); self.assertTrue(result['eval_stable'])
        self.assertTrue(model.training)

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_dropout_oracle_rejects_candidate_fabricated_mode_outputs(self):
        import torch
        class Fabricator:
            @staticmethod
            def dropout_mode_probe(model,batch,*,repeats):
                return {"train_outputs":[torch.zeros((2,4)) for _ in range(repeats)],
                        "eval_outputs":[torch.ones((2,4)) for _ in range(repeats)]}
        model=torch.nn.Sequential(torch.nn.Dropout(p=.7),torch.nn.Linear(32,4))
        with self.assertRaisesRegex(ValueError,"independent seeded model execution"):
            workflow.dropout_mode_witness(model,torch.ones((2,32)),repeats=5,adapter=Fabricator())

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_candidate_validation_loop_is_eval_no_grad_and_state_read_only(self):
        import torch
        features=[[-2.,-1.],[-1.,-2.],[-1.,-1.],[1.,1.],[1.,2.],[2.,1.]]; labels=[0,0,0,1,1,1]
        trained=workflow.torch_training_witness(features,labels,adapter=candidate,seed=41,epochs=20)
        loaders=workflow.prepare_torch_data(features,labels,validation_fraction=.33,batch_size=2,seed=12,adapter=candidate)
        optimizer=torch.optim.Adam(trained['model'].parameters(),lr=.03)
        optimizer.load_state_dict(trained['optimizer_state_dict'])
        result=workflow.evaluate_torch_model(trained['model'],loaders['validation_loader'],optimizer,adapter=candidate)
        self.assertEqual(result['observed_rows'],len(loaders['validation_indices']))
        self.assertEqual(result['evaluation_mode'],'eval/no_grad')
        self.assertTrue(result['parameters_unchanged'])
        self.assertFalse(result['logits'].requires_grad)

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_validation_oracle_rejects_candidate_claiming_a_wrong_logit(self):
        class Liar:
            @staticmethod
            def evaluate_torch_model(model,loader,optimizer):
                result=candidate.evaluate_torch_model(model,loader,optimizer)
                result['logits']=result['logits']+100
                return result
        import torch
        features=[[-2.,-1.],[-1.,-2.],[-1.,-1.],[1.,1.],[1.,2.],[2.,1.]]; labels=[0,0,0,1,1,1]
        trained=workflow.torch_training_witness(features,labels,adapter=candidate,seed=41,epochs=20)
        loaders=workflow.prepare_torch_data(features,labels,validation_fraction=.33,batch_size=2,seed=12,adapter=candidate)
        optimizer=torch.optim.Adam(trained['model'].parameters(),lr=.03)
        optimizer.load_state_dict(trained['optimizer_state_dict'])
        with self.assertRaisesRegex(ValueError,'logits differ'):
            workflow.evaluate_torch_model(trained['model'],loaders['validation_loader'],optimizer,adapter=Liar())

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_candidate_optimizer_comparison_and_minibatch_accounting(self):
        features=[[-2.,-1.],[-1.,-2.],[-1.,-1.],[1.,1.],[1.,2.],[2.,1.]]; labels=[0,0,0,1,1,1]
        outputs={kind:candidate.train_torch_model(features,labels,seed=14,epochs=6,learning_rate=.02,
                    optimizer_kind=kind,batch_size=2) for kind in ('adam','sgd')}
        for kind,result in outputs.items():
            self.assertEqual(result['optimizer_kind'],kind)
            self.assertEqual(result['examples_per_epoch'],[len(features)]*6)
            self.assertEqual(result['updates_per_epoch'],[3]*6)
            self.assertEqual(result['batch_size'],2)
        partial=candidate.train_torch_model(features+[[3.,2.]],[*labels,1],seed=14,epochs=2,
                                             optimizer_kind='sgd',batch_size=2)
        self.assertEqual(partial['examples_per_epoch'],[7,7])
        self.assertEqual(partial['updates_per_epoch'],[4,4])
        self.assertNotEqual(outputs['adam']['loss_history'],outputs['sgd']['loss_history'])

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_weighted_training_witness_uses_the_declared_positive_weight(self):
        features=[[-2.],[-1.],[1.],[2.]]; labels=[0,0,1,1]
        result=workflow.torch_training_witness(features,labels,adapter=candidate,seed=19,epochs=30,
                                                positive_weight=2.5)
        self.assertEqual(result["positive_weight"],2.5)
        self.assertLess(result["observed_final_loss"],result["observed_initial_loss"])

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_training_witness_rejects_tampered_intermediate_epoch_loss(self):
        class Liar:
            validate_torch_batch=staticmethod(candidate.validate_torch_batch)
            build_torch_model=staticmethod(candidate.build_torch_model)
            @staticmethod
            def train_torch_model(features,labels,**kwargs):
                result=candidate.train_torch_model(features,labels,**kwargs)
                result['loss_history'][1]+=0.25
                return result
        with self.assertRaisesRegex(ValueError,"per-epoch history"):
            workflow.torch_training_witness([[-2.],[-1.],[1.],[2.]],[0,0,1,1],adapter=Liar(),epochs=4)

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_training_witness_rejects_adapter_ignoring_initial_checkpoint(self):
        class IgnoresInitialState:
            validate_torch_batch=staticmethod(candidate.validate_torch_batch)
            build_torch_model=staticmethod(candidate.build_torch_model)
            @staticmethod
            def train_torch_model(features,labels,**kwargs):
                kwargs.pop("initial_state",None)
                kwargs.pop("optimizer_state",None)
                kwargs["seed"]+=1
                return candidate.train_torch_model(features,labels,**kwargs)
        with self.assertRaisesRegex(ValueError,"epoch zero snapshot differs"):
            workflow.torch_training_witness([[-2.],[-1.],[1.],[2.]],[0,0,1,1],
                                             adapter=IgnoresInitialState(),seed=15,epochs=4)

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_candidate_training_rejects_invalid_label_values(self):
        with self.assertRaises(ValueError):
            candidate.train_torch_model([[-1.],[1.]],[0,2],epochs=2)

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_candidate_training_rejects_nonfinite_gradients_and_detached_loss(self):
        import torch
        from torch.nn import functional as torch_functional
        with self.assertRaises(ValueError):
            candidate.train_torch_model([[-1.],[1.]],[0,2],epochs=1)
        class NonfiniteBackward(torch.autograd.Function):
            @staticmethod
            def forward(ctx,value): return value
            @staticmethod
            def backward(ctx,gradient): return torch.full_like(gradient,float('nan'))
        class NanGradientLinear(torch.nn.Linear):
            def forward(self,value): return NonfiniteBackward.apply(super().forward(value))
        def exploding_builder(input_dim,*,seed=7):
            torch.manual_seed(seed)
            return torch.nn.Sequential(torch.nn.Linear(input_dim,8),torch.nn.Tanh(),NanGradientLinear(8,1))
        with mock.patch.object(candidate,"build_torch_model",exploding_builder):
            with self.assertRaisesRegex(FloatingPointError,"non-finite gradient"):
                workflow.torch_training_witness([[-1.],[-.5],[.5],[1.]],[0,0,1,1],adapter=candidate,epochs=1)
        class DetachedLoss(torch.nn.Module):
            def __init__(self,**kwargs): super().__init__()
            def forward(self,logits,targets):
                return torch_functional.binary_cross_entropy_with_logits(logits,targets).detach()
        with mock.patch.object(torch.nn,"BCEWithLogitsLoss",DetachedLoss):
            with self.assertRaisesRegex(RuntimeError,"does not require grad|no grad_fn"):
                candidate.train_torch_model([[-1.],[-.5],[.5],[1.]],[0,0,1,1],epochs=1)

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_training_witness_rejects_noop_optimizer_and_corrupted_labels(self):
        import torch
        features=[[-2.],[-1.],[1.],[2.]]; labels=[0,0,1,1]
        class NoStep:
            validate_torch_batch=staticmethod(candidate.validate_torch_batch)
            build_torch_model=staticmethod(candidate.build_torch_model)
            train_torch_model=staticmethod(candidate.train_torch_model)
        with mock.patch.object(torch.optim.Adam,"step",lambda self,*a,**k:None), \
             mock.patch.object(torch.optim.SGD,"step",lambda self,*a,**k:None):
            with self.assertRaises((ValueError,RuntimeError)):
                workflow.torch_training_witness(features,labels,adapter=NoStep(),epochs=4)
        class CorruptLabels:
            validate_torch_batch=staticmethod(candidate.validate_torch_batch)
            build_torch_model=staticmethod(candidate.build_torch_model)
            @staticmethod
            def train_torch_model(x,y,**kwargs):
                return candidate.train_torch_model(x,[1-int(value) for value in y],**kwargs)
        with self.assertRaises(ValueError):
            workflow.torch_training_witness(features,labels,adapter=CorruptLabels(),epochs=50)

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_candidate_tiny_batch_can_overfit_but_is_only_a_mechanics_claim(self):
        features=[[-2.,-1.],[-1.,-2.],[1.,1.],[2.,1.]]; labels=[0,0,1,1]
        trained=workflow.torch_training_witness(features,labels,adapter=candidate,seed=13,epochs=200,learning_rate=.03)
        self.assertLess(trained['observed_final_loss'],trained['observed_initial_loss']*.1)
        self.assertEqual([int(value>=.5) for value in trained['probabilities']],labels)
        self.assertFalse(trained.get('generalization_claim',False))

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_training_witness_rejects_matching_but_fabricated_update_claim(self):
        class Liar:
            validate_torch_batch=staticmethod(candidate.validate_torch_batch)
            build_torch_model=staticmethod(candidate.build_torch_model)
            @staticmethod
            def train_torch_model(features,labels,**kwargs):
                result=candidate.train_torch_model(features,labels,**kwargs)
                result['model'].load_state_dict(kwargs['initial_state'])
                # Lie consistently about both the update flag and history.
                result['parameters_changed']=True
                result['loss_history']=[result['loss_history'][0]]*len(result['loss_history'])
                return result
        with self.assertRaisesRegex(ValueError,'did not prove a parameter update|did not improve'):
            workflow.torch_training_witness([[-2.],[-1.],[1.],[2.]],[0,0,1,1],adapter=Liar(),epochs=5)

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_candidate_torch_training_is_repeatable_with_fixed_seed(self):
        import torch
        features=[[-2.,-1.],[-1.,-2.],[-1.,-1.],[1.,1.],[1.,2.],[2.,1.]]; labels=[0,0,0,1,1,1]
        first=candidate.train_torch_model(features,labels,seed=22,epochs=8,batch_size=2)
        second=candidate.train_torch_model(features,labels,seed=22,epochs=8,batch_size=2)
        for name,value in first['model'].state_dict().items(): torch.testing.assert_close(value,second['model'].state_dict()[name],rtol=0,atol=0)

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_candidate_learning_rate_comparison_records_divergence_not_dropped_trials(self):
        traces={rate:candidate.train_logistic([[1.,-2.],[1.,-1.],[1.,1.],[1.,2.]],[0,0,1,1],
                learning_rate=rate,epochs=20,l2=.01)['loss_history'] for rate in (.01,.1,1.0)}
        self.assertEqual(set(traces),{.01,.1,1.0})
        self.assertTrue(all(len(values)>=2 and all(__import__('math').isfinite(x) for x in values) for values in traces.values()))

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_candidate_l1_and_l2_regularization_change_learned_coefficients(self):
        rows=[[1.,-3.],[1.,-2.],[1.,-1.],[1.,1.],[1.,2.],[1.,3.]]; labels=[0,0,0,1,1,1]
        fits={penalty:candidate.train_logistic(rows,labels,learning_rate=.1,epochs=120,l1=l1,l2=l2)
              for penalty,l1,l2 in [('none',0,0),('l1',.08,0),('l2',0,.08)]}
        norms={key:sum(float(value)**2 for value in fit['weights'][1:])**.5 for key,fit in fits.items()}
        self.assertTrue(all(__import__('math').isfinite(norm) for norm in norms.values()))
        self.assertGreater(len({round(norm,7) for norm in norms.values()}),1)

    def test_candidate_early_stopping_selects_validation_minimum_and_patience(self):
        choice=workflow.select_torch_epoch([.8,.6,.4,.45,.5],patience=2,adapter=candidate)
        self.assertEqual((choice['best_epoch'],choice['stop_epoch'],choice['selection_partition']),(2,4,'validation_only'))
        with self.assertRaises(ValueError): candidate.select_torch_epoch([.8,float('nan')],patience=1)

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_candidate_weighted_loss_changes_actual_trained_parameters(self):
        import torch
        features=[[-1.],[-.5],[.1],[.3],[.5],[.7],[.9],[1.1]]; labels=[0,0,0,0,0,0,0,1]
        ordinary=candidate.train_torch_model(features,labels,seed=8,epochs=20,batch_size=4,positive_weight=1)
        weighted=candidate.train_torch_model(features,labels,seed=8,epochs=20,batch_size=4,positive_weight=6)
        self.assertNotEqual(ordinary['loss_history'],weighted['loss_history'])
        self.assertTrue(any(not torch.equal(ordinary['model'].state_dict()[key],weighted['model'].state_dict()[key])
                            for key in ordinary['model'].state_dict()))

    def test_default_regression_dataloader_and_backfill_tasks_remain_todos(self):
        from . import learner
        with self.assertRaises(NotImplementedError): workflow.compare_regression_losses([0,1],[0,1])
        with self.assertRaises(NotImplementedError): learner.reconcile_delayed_labels([])
        with self.assertRaises(NotImplementedError): learner.logistic_gradient([0,1],[[1,0],[1,1]],[0,1])
        with self.assertRaises(NotImplementedError): learner.pairwise_loss_gradient([0.1],[0.2],[0.3])
        with self.assertRaises(NotImplementedError): learner.cold_start_fallback(['a'],{'a':1})
        with self.assertRaises(NotImplementedError): learner.autograd_scalar_gradient(1.0)
        with self.assertRaises(NotImplementedError): learner.finite_difference_gradient(lambda x:x*x,1.0)
        with self.assertRaises(NotImplementedError): learner.dropout_mode_probe(None,None)
        with self.assertRaises(NotImplementedError): learner.validate_torch_batch([[1.0]],[1])
        with self.assertRaises(NotImplementedError): learner.build_torch_model(1)
        with self.assertRaises(NotImplementedError): learner.evaluate_torch_model(None,None,None)
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

    def test_cold_start_fallback_is_learner_owned_and_training_catalog_bounded(self):
        result=workflow.compare_rankers(EVENTS,[{'user_id':'new-user','item_id':'a','timestamp':9}],
                                        ['a','b','c','d'],adapter=candidate,k=3)
        row=result['rows'][0]
        training_counts={'a':2,'b':2,'c':2,'d':0}
        expected=candidate.cold_start_fallback(result['candidate_items'],training_counts)
        self.assertEqual(row['cold_start_fallback'],'global_popularity')
        self.assertEqual(row['learned_top_k'],expected[:3])
        with self.assertRaises(ValueError): candidate.cold_start_fallback(['a','b'],{'a':1})

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
        self.test_training_witness_rejects_adapter_ignoring_initial_checkpoint()

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
