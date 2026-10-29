"""Unsolved M3.3 adapters. The evaluation feature path calls these directly."""
from __future__ import annotations
from typing import Any, Mapping, Sequence


def split_by_group(cases: Sequence[Mapping[str, Any]], development_groups: set[str]) -> dict[str, list[str]]:
    raise NotImplementedError("M3.3: split by root-defect group without leakage")


def score_case(case: Mapping[str, Any], trace: Mapping[str, Any]) -> dict[str, Any]:
    raise NotImplementedError("M3.3: score observable task outcome and authorization safety")


def evaluate_release(expected_case_ids: Sequence[str], rows: Sequence[Mapping[str, Any]],
                     frozen: Mapping[str, Any]) -> dict[str, Any]:
    raise NotImplementedError("M3.3: fail closed on incomplete, unsafe or unfrozen held-out evaluation")


def reconcile_action(state: Mapping[str, Any]) -> dict[str, Any]:
    raise NotImplementedError("M3.3: reconcile uncertain side effects without blind retry")
