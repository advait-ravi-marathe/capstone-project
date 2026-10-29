"""Run conditional M2.2 mechanics, learner, integration, or evidence checks.

The default learner is unsolved. Integration is blocked until P1/M2.1 and real
local identity/access runtimes exist; synthetic mechanics do not clear that gate.
"""
from __future__ import annotations

import argparse
import json
import subprocess
import sys


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--milestone", required=True, choices=("AIFDE-V2-M2.2",))
    parser.add_argument("--mode", required=True, choices=("mechanics", "learner", "integration", "evidence"))
    args = parser.parse_args(argv)
    if args.mode == "integration":
        print(json.dumps({"status": "BLOCKED", "milestone": args.milestone,
                          "reasons": ["P1 actual inference and M2.1 learner evidence are not passed",
                                      "actual local identity exchange and server/retrieval integration are not implemented"],
                          "m22_pass": False}, indent=2))
        return 2
    if args.mode == "evidence":
        print(json.dumps({"status": "BLOCKED", "milestone": args.milestone,
                          "reasons": ["no actual M2.2 runtime or learner evidence bundle exists"],
                          "m22_pass": False}, indent=2))
        return 2
    test = ("ai.fde_v2.tests.test_m22_draft" if args.mode == "mechanics"
            else "ai.fde_v2.learner_acceptance.test_m22_units")
    result = subprocess.run([sys.executable, "-m", "unittest", test, "-v"], check=False)
    return result.returncode


if __name__ == "__main__":
    raise SystemExit(main())
