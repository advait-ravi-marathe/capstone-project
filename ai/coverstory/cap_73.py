"""CAP-73 learner-owned development/held-out split seam."""
from __future__ import annotations

from dataclasses import dataclass
from typing import Iterable, Mapping


@dataclass(frozen=True)
class SplitReport:
    valid: bool
    development_count: int
    golden_count: int
    violations: tuple[str, ...]


def validate_split(dev: Iterable[Mapping[str, str]], golden: Iterable[Mapping[str, str]]) -> SplitReport:
    # LEARNER TODO: validate required non-empty fields, reject empty golden,
    # and detect case-ID, normalized-question, and source-family overlap.
    # Golden labels must never participate in threshold/model tuning.
    raise NotImplementedError("CAP-73 learner TODO: validate held-out split")
