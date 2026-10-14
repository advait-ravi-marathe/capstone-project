"""Evidence-index seam for CAP-63; evidence policy is learner-owned."""
from __future__ import annotations

from dataclasses import dataclass
from typing import Any, Mapping, Sequence


@dataclass(frozen=True)
class EvidenceReport:
    records: tuple[Mapping[str, Any], ...]
    missing: tuple[str, ...]
    invalid: tuple[str, ...]
    gate: str


def validate_evidence(records: Sequence[Mapping[str, Any]], required_ids: Sequence[str]) -> EvidenceReport:
    # LEARNER TODO: retain every input record, including mismatched historical
    # receipts; require matching CoverStory domain/fixture and complete PASS
    # provenance (command, commit, environment, fixture, exit 0, artifact).
    # Missing evidence holds the gate. Invalid claimed PASS evidence is invalid.
    raise NotImplementedError("CAP-63 learner TODO: implement validate_evidence")
