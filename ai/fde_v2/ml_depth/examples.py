"""Runnable worked examples for each extension block.

All records are generated in memory. Outputs are mechanics demonstrations, not
learner receipts, production metrics, customer data, or experimental outcomes.
"""
from __future__ import annotations
import argparse
import json

from . import reference, workflow
from .fixtures import classification_fixture, recommendation_fixture, experiment_fixture


def run_block(block: str):
    if block=="PML1":
        rows=classification_fixture()
        train=rows[:96]; validation=rows[96:128]
        result=workflow.compare_classical([r["features"] for r in train],[r["label"] for r in train],
             [r["features"] for r in validation],[r["label"] for r in validation],adapter=reference,epochs=250)
        robust=workflow.compare_regression_losses([0.0,10.0],[0.0,0.0],huber_delta=1.0,adapter=reference)
        return {"block":block,"training_rows":len(train),"validation_rows":len(validation),
                "logistic_loss_start_end":[result["logistic"]["model"]["loss_history"][0],result["logistic"]["model"]["loss_history"][-1]],
                "logistic_metrics":result["logistic"]["metrics"],"boosted_tree_metrics":result["boosted_tree"]["metrics"],
                "one_outlier_regression_losses":robust,
                "fit_partition":"train","evidence_class":"synthetic_mechanics_only"}
    if block=="PML2":
        try: import torch  # noqa: F401
        except ImportError: return {"block":block,"status":"BLOCKED","reason":"pinned PyTorch is not installed; no network/install attempted"}
        rows=classification_fixture(80)
        features=[r["features"] for r in rows]; labels=[r["label"] for r in rows]
        loaders=workflow.prepare_torch_data(features,labels,adapter=reference,validation_fraction=.2,batch_size=8,seed=7)
        trained=reference.train_torch_model(features,
                labels,seed=7,epochs=80)
        return {"block":block,"training_rows":len(rows),"loss_start_end":[trained["loss_history"][0],trained["loss_history"][-1]],
                "dataset_rows":loaders["dataset_rows"],"loader_train_rows":len(loaders["train_indices"]),
                "loader_validation_rows":len(loaders["validation_indices"]),
                "actual_torch_dataloaders":True,"disjoint_indices":not bool(set(loaders["train_indices"])&set(loaders["validation_indices"])),
                "device":"cpu","parameters_changed":trained["parameters_changed"],"pretrained_weights":False,
                "evidence_class":"synthetic_mechanics_only"}
    if block=="PML3":
        all_rows=recommendation_fixture(); split=reference.temporal_split(all_rows)
        catalog=sorted({row["item_id"] for row in split["train"]})
        result=workflow.compare_rankers(split["train"],split["validation"],catalog,adapter=reference,k=5)
        return {"block":block,"catalog_items":len(catalog),"metrics":result["metrics"],
                "same_candidates":result["same_candidate_set"],"scored_users":result["scored_users"],
                "evidence_class":"synthetic_offline_mechanics_only"}
    if block=="PML4":
        spec={"experiment_id":"example-only","unit":"user","primary_metric":"synthetic conversion",
              "metric_direction":"higher_is_better","baseline_rate":.2,"mde":.08,"alpha":.05,"power":.8,
              "allocation":{"control":.5,"treatment":.5},"fixed_horizon_units":5000,
              "guardrails":["opt_out_rate"],"guardrail_limits":{"opt_out_rate":{"adverse_direction":"higher","max_adverse_delta":.01}},
              "stopping_rule":"fixed_horizon_with_safety_stop"}
        design=reference.design_experiment(spec)
        assignments=reference.assign_experiment_units(experiment_fixture(5000),experiment_id="example-only",seed=9)
        counts={arm:sum(value==arm for value in assignments.values()) for arm in ("control","treatment")}
        labels=workflow.reconcile_delayed_labels([
            {"event_id":"event-1","user_id":"user-1","item_id":"item-a","event_time":100,"arrival_time":105,"label":0,"label_version":1},
            {"event_id":"event-1","user_id":"user-1","item_id":"item-a","event_time":100,"arrival_time":130,"label":1,"label_version":2}],adapter=reference)
        return {"block":block,"required_total":design["required_total"],"assigned_counts":counts,
                "delayed_label_reconciliation":labels,
                "online_experiment":False,"evidence_class":"design_fixture_only"}
    raise ValueError("block must be PML1, PML2, PML3 or PML4")


def main():
    parser=argparse.ArgumentParser(description=__doc__); parser.add_argument("--block",choices=("PML1","PML2","PML3","PML4"),required=True)
    print(json.dumps(run_block(parser.parse_args().block),indent=2))


if __name__=="__main__": main()
