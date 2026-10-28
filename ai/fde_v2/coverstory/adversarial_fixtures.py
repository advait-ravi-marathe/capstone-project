"""Structural checks for the Day 30/31 U023 fixture and observation slices.

This validates the learner-authored fixture plan only. It does not run a model,
test prompt-injection resistance, or establish U023/P1 completion.
"""
from __future__ import annotations

from collections.abc import Mapping, Sequence

from .models import MODEL_ID, MODEL_REVISION


_SCENARIOS = {"indirect_instruction", "misleading_evidence", "access_boundary"}
_BOUNDARY_KEYS = {"tool_permission", "filesystem_permission", "external_action_permission"}


def validate_day30_fixture_records(records: Sequence[Mapping[str, object]]) -> int:
    """Validate exactly one planned case per Day 30 scenario with no result claim."""
    if not isinstance(records, Sequence) or isinstance(records, (str, bytes)):
        raise ValueError("fixtures must be an ordered sequence of records")
    if len(records) != len(_SCENARIOS):
        raise ValueError("Day 30 requires one fixture for each of three bounded scenarios")
    seen_ids: set[str] = set()
    seen_scenarios: set[str] = set()
    for record in records:
        if not isinstance(record, Mapping):
            raise ValueError("each fixture must be an object")
        if set(record) != {"case_id", "scenario", "document_text", "expected_boundaries", "observation_status"}:
            raise ValueError("fixture fields differ from the Day 30 authoring contract")
        case_id = record["case_id"]
        scenario = record["scenario"]
        text = record["document_text"]
        expected = record["expected_boundaries"]
        if (not isinstance(case_id, str) or not case_id.strip() or len(case_id) > 80
                or case_id in seen_ids):
            raise ValueError("case IDs must be unique, nonempty and bounded")
        if not isinstance(scenario, str) or scenario not in _SCENARIOS or scenario in seen_scenarios:
            raise ValueError("each required scenario must appear exactly once")
        if not isinstance(text, str) or not text.strip() or len(text) > 2_000:
            raise ValueError("fixture text must be nonempty synthetic bounded text")
        if not text.lstrip().startswith("SYNTHETIC"):
            raise ValueError("fixture content must be clearly synthetic")
        if not isinstance(expected, Mapping) or set(expected) != _BOUNDARY_KEYS:
            raise ValueError("all deterministic permission boundaries must be stated")
        if any(value is not False for value in expected.values()):
            raise ValueError("the current local app has no tool, filesystem or external-action permission")
        if record["observation_status"] != "not_run_day30_scope":
            raise ValueError("Day 30 records expected boundaries only, not observed model behavior")
        seen_ids.add(case_id)
        seen_scenarios.add(scenario)
    if seen_scenarios != _SCENARIOS:
        raise ValueError("fixture scenario set is incomplete")
    return len(records)


def validate_day31_observation_records(
    planned: Sequence[Mapping[str, object]], observed: Sequence[Mapping[str, object]],
) -> int:
    """Check one recorded outcome per authored case without judging model safety.

    The checker deliberately cannot mark U023 complete. A structurally valid
    observation records either an actual pinned-model response or an explicit
    blocked/failed attempt; deterministic permission assertions are separate
    and a few cases never establish general prompt-injection resistance.
    """
    expected_ids = [row.get("case_id") for row in planned]
    if len(expected_ids) != 3 or len(set(expected_ids)) != 3:
        raise ValueError("Day31 requires the three distinct Day30 authored case IDs")
    if not isinstance(observed, Sequence) or isinstance(observed, (str, bytes)):
        raise ValueError("observations must be an ordered sequence")
    if len(observed) != len(expected_ids):
        raise ValueError("one outcome is required for each authored case")
    seen: list[str] = []
    for row in observed:
        if not isinstance(row, Mapping) or set(row) != {
            "case_id", "attempt_status", "model_id", "model_revision", "output_or_error",
            "tool_permission", "filesystem_permission", "external_action_permission",
        }:
            raise ValueError("observation fields differ from the Day31 contract")
        case_id = row["case_id"]
        if not isinstance(case_id, str) or case_id not in expected_ids or case_id in seen:
            raise ValueError("observation IDs must match the authored cases exactly once")
        if row["attempt_status"] not in {"generated", "blocked", "failed"}:
            raise ValueError("record actual generation or an explicit blocked/failed attempt")
        for key in ("model_id", "model_revision", "output_or_error"):
            if not isinstance(row[key], str) or not row[key].strip():
                raise ValueError(f"{key} must record actual runtime metadata or failure")
        for key in _BOUNDARY_KEYS:
            if row[key] is not False:
                raise ValueError("this prototype grants no tool/filesystem/external-action permission")
        if row["attempt_status"] == "generated":
            if row["model_id"] != MODEL_ID or row["model_revision"] != MODEL_REVISION:
                raise ValueError("generated status requires the pinned local model identity")
            if row["model_revision"] == "fixture" or row["model_id"].startswith("test-double"):
                raise ValueError("test fixtures cannot be represented as model observations")
        seen.append(case_id)
    if seen != expected_ids:
        raise ValueError("observation order must match the authored case order")
    return len(observed)
