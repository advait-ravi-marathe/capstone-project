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
    def test_learner_logistic_and_training_only_transform_drive_comparison(self):
        result=workflow.compare_classical(X,Y,[[-2.5],[-.5],[.5],[2.5]],[0,0,1,1],adapter=candidate,epochs=120)
        self.assertLess(result["logistic"]["model"]["loss_history"][-1],result["logistic"]["model"]["loss_history"][0])
        self.assertEqual(result["transform"]["fit_partition"],"train")

    def test_learner_pairwise_ranker_is_used_by_comparison_path(self):
        result=workflow.compare_rankers(EVENTS,[{"user_id":"u1","item_id":"c","timestamp":7}],
                                        ["a","b","c","d"],adapter=candidate,k=3)
        self.assertTrue(result["same_candidate_set"])
        self.assertEqual(result["scored_users"],1)

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

    @unittest.skipUnless(importlib.util.find_spec("torch"),"pinned PyTorch is unavailable in this interpreter")
    def test_torch_task_runs_actual_training_adapter(self):
        features=[[-2,-1],[-1,-2],[-1,-1],[1,1],[1,2],[2,1]]
        trained=workflow.torch_training_witness(features,Y,adapter=candidate,seed=5,epochs=80)
        self.assertLess(trained["loss_history"][-1],trained["loss_history"][0])
        self.assertTrue(trained["parameters_changed"])


if __name__=="__main__": unittest.main()
