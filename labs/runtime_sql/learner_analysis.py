"""Learner-owned analyzer for the captured PostgreSQL 18.6 lab observation."""
from __future__ import annotations
from typing import Any


def analyze_observation(observation: dict[str, Any]) -> dict[str, Any]:
    """Validate rows, compare root estimates/actuals and report observed buffers.

    TODO(learner): validate the full fixture shape and compare complete ordered
    owner/policy/revision rows with independent expected_ids. Report the
    estimate/actual ratio with explicit zero-row handling and root buffer
    counters. Never infer general performance from one execution or treat a
    planner node, cost, or elapsed time as a correctness rule.
    """
    raise NotImplementedError("Day 271: implement the independent observation analysis")
