#!/usr/bin/env python3
"""Offline CAP-96 local evidence gate. This never publishes artifacts."""

from __future__ import annotations

import argparse
import json
from dataclasses import asdict, dataclass
from enum import StrEnum
from pathlib import Path
from typing import Any


class Decision(StrEnum):
    PASS = "PASS"
    FAIL = "FAIL"
    INCOMPLETE = "INCOMPLETE"
    HARNESS_INVALID = "HARNESS_INVALID"


@dataclass(frozen=True)
class GateInput:
    command_exit: int
    smoke_report: dict[str, Any] | None
    learner_report: dict[str, Any] | None
    oracle_report: dict[str, Any] | None
    todo_sentinel: bool = False
    known_defect: bool = False


def _valid_report(report: dict[str, Any] | None) -> bool:
    return bool(report and report.get("report_id") and isinstance(report.get("assertions"), list))


def evaluate(data: GateInput) -> dict[str, Any]:
    """Classify local evidence; learner failures are distinct from scaffold health."""
    smoke_ok = _valid_report(data.smoke_report) and all(
        assertion.get("status") == "PASS" for assertion in data.smoke_report["assertions"]
    )
    learner_present = _valid_report(data.learner_report)
    oracle_valid = _valid_report(data.oracle_report) and all(
        assertion.get("status") == "PASS" for assertion in data.oracle_report["assertions"]
    )

    if not smoke_ok or data.todo_sentinel or (data.known_defect and not oracle_valid):
        decision = Decision.HARNESS_INVALID
    elif not learner_present or not oracle_valid:
        decision = Decision.INCOMPLETE
    elif data.known_defect:
        decision = Decision.FAIL if data.oracle_report.get("defect_rejected") is True else Decision.HARNESS_INVALID
    elif data.command_exit != 0 or any(
        assertion.get("status") != "PASS" for assertion in data.learner_report["assertions"]
    ):
        decision = Decision.FAIL
    else:
        decision = Decision.PASS

    return {
        "decision": decision.value,
        "package_eligible_locally": decision is Decision.PASS,
        "publication_claim": False,
        "smoke_healthy": smoke_ok,
        "learner_report_present": learner_present,
        "oracle_report_valid": oracle_valid,
        "oracle_evidence_retained": data.oracle_report is not None,
        "command_exit": data.command_exit,
    }


def _read_optional(path: str | None) -> dict[str, Any] | None:
    if path is None:
        return None
    return json.loads(Path(path).read_text(encoding="utf-8"))


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("input", help="local JSON containing CAP-96 evidence report references")
    parser.add_argument("--output", required=True, help="write local JSON decision report")
    args = parser.parse_args()
    raw = json.loads(Path(args.input).read_text(encoding="utf-8"))
    data = GateInput(
        command_exit=int(raw["command_exit"]),
        smoke_report=_read_optional(raw.get("smoke_report")),
        learner_report=_read_optional(raw.get("learner_report")),
        oracle_report=_read_optional(raw.get("oracle_report")),
        todo_sentinel=bool(raw.get("todo_sentinel", False)),
        known_defect=bool(raw.get("known_defect", False)),
    )
    result = evaluate(data)
    Path(args.output).write_text(json.dumps(result, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print(result["decision"])
    return 0 if result["decision"] == "PASS" else 1


if __name__ == "__main__":
    raise SystemExit(main())
