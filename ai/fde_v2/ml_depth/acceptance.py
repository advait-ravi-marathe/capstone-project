"""Command interface for the selected production-ML route extension."""
from __future__ import annotations
import argparse
import importlib.util
import json
import unittest

from ai.fde_v2.evidence_receipts import report_receipt


def _suite(module):
    loaded=unittest.defaultTestLoader.loadTestsFromName(module)
    result=unittest.TextTestRunner(verbosity=2).run(loaded)
    return result.wasSuccessful(), result.testsRun, len(result.failures)+len(result.errors), len(result.skipped)


def main(argv=None):
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--mode",choices=("mechanics","learner","torch","integration","evidence"),required=True)
    parser.add_argument("--receipt",help="hash-bound artifact manifest for independent review")
    args=parser.parse_args(argv)
    torch_available=importlib.util.find_spec("torch") is not None
    if args.mode=="evidence":
        return report_receipt(args.receipt,"AIFDE-V2-ML-DEPTH-1",
            ("classical_run","torch_checkpoint_resume","ranking_comparison","experiment_analysis",
             "model_serve_rollback","learner_attempts"))
    if args.mode=="learner":
        success,ran,failed,skipped=_suite("ai.fde_v2.ml_depth.learner_acceptance")
        print(json.dumps({"status":"PASS" if success else "FAIL_OR_TODO_INCOMPLETE","tests":ran,
                          "failures":failed,"skipped":skipped,"candidate":"learner-default-or-selected",
                          "learner_completion":False},indent=2))
        return 0 if success else 1
    if args.mode=="torch" and not torch_available:
        print(json.dumps({"status":"BLOCKED","reason":"pinned PyTorch not installed; no install/network attempt made"},indent=2))
        return 2
    if args.mode=="integration" and not torch_available:
        print(json.dumps({"status":"BLOCKED","reason":"actual local PyTorch training dependency unavailable",
                          "other_mechanics":"run with --mode mechanics","learner_completion":False},indent=2))
        return 2
    module="ai.fde_v2.ml_depth.test_ml_depth"
    success,ran,failed,skipped=_suite(module)
    result={"status":"PASS" if success else "FAIL","tests":ran,"failures":failed,"skipped":skipped,
            "mode":args.mode,"reference_mechanics_only":True,"synthetic_results_are_learner_evidence":False,
            "torch_available":torch_available}
    if args.mode=="torch" and not torch_available: result["status"]="BLOCKED"
    print(json.dumps(result,indent=2))
    if not success: return 1
    return 2 if args.mode=="torch" and not torch_available else 0


if __name__=="__main__": raise SystemExit(main())
