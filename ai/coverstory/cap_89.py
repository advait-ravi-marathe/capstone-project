"""CAP-89 synthetic evaluation gate contract.

The learner owns the gate decision and its focused local command.  This module
contains no model, retrieval, CI publication, or customer-data integration.
"""
from __future__ import annotations

from dataclasses import dataclass
from typing import Any, Literal


ExitKind = Literal["PASS", "FAIL", "INVALID"]


@dataclass(frozen=True)
class Baseline:
    mean_rr: float


@dataclass(frozen=True)
class GatePolicy:
    required_cases: tuple[str, ...]
    allowed_regression: float = 0.02


@dataclass(frozen=True)
class GateResult:
    kind: ExitKind
    exit_code: int
    reasons: tuple[str, ...]


def gate(report: dict[str, Any], baseline: Baseline, policy: GatePolicy) -> GateResult:
    """Evaluate one synthetic report.

    LEARNER TODO: validate report shape and finite values, require each named
    case, reject safety violations before considering ranking quality, then
    compare mean RR against baseline minus the allowed regression. Return
    PASS/0, FAIL/1, or INVALID/2. Never average a safety violation away.
    """
    raise NotImplementedError("CAP-89 learner TODO: implement evaluation gate")
