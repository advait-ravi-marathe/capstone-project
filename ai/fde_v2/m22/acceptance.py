"""Run M2.2 mechanics, learner seams, or the real local integration harness.

Integration exercises FastAPI + SQLite + embedded Qdrant with a synthetic
claims callback and explicit reference adapter. It is useful local wiring
evidence only; actual identity, P1 prerequisites, and learner evidence remain
separate blocked gates.
"""
from __future__ import annotations

import argparse
import json
import subprocess
import sys
from ai.fde_v2.evidence_receipts import report_receipt
from .reference import REQUIRED_EVIDENCE


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--milestone", required=True, choices=("AIFDE-V2-M2.2",))
    parser.add_argument("--mode", required=True, choices=("mechanics", "learner", "integration", "evidence"))
    parser.add_argument("--receipt", help="hash-verified artifact manifest for independent review")
    args = parser.parse_args(argv)
    if args.mode == "integration":
        result = subprocess.run([sys.executable, "-m", "unittest",
                                 "ai.fde_v2.tests.test_m22_integration", "-v"], check=False)
        print(json.dumps({
            "milestone": args.milestone,
            "local_integration_mechanics": "PASS" if result.returncode == 0 else "FAIL",
            "identity_provider": "SYNTHETIC_CLAIMS_FIXTURE",
            "adapter": "EXPLICIT_INSTRUCTOR_REFERENCE",
            "prerequisite_and_learner_evidence": "BLOCKED",
            "m22_pass": False,
        }, indent=2))
        return result.returncode
    if args.mode == "evidence":
        return report_receipt(args.receipt,args.milestone,REQUIRED_EVIDENCE)
    test = ("ai.fde_v2.tests.test_m22_draft" if args.mode == "mechanics"
            else "ai.fde_v2.learner_acceptance.test_m22_units")
    result = subprocess.run([sys.executable, "-m", "unittest", test, "-v"], check=False)
    return result.returncode


if __name__ == "__main__":
    raise SystemExit(main())
