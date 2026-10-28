"""Structural checks for the Day 30 U023 fixture-authoring slice.

This validates the learner-authored fixture plan only. It does not run a model,
test prompt-injection resistance, or establish U023/P1 completion.
"""
from __future__ import annotations

from collections.abc import Mapping, Sequence


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
