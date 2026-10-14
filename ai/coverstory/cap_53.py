"""CAP-53 instructor data types; safe loader remains learner-owned."""
from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path


class LoadError(ValueError):
    def __init__(self, filename: str, code: str):
        super().__init__(f"{filename}:{code}")
        self.filename = filename
        self.code = code


@dataclass(frozen=True)
class PublicWording:
    source_id: str
    version: str
    text: str
    evidence_status: str
    classification: str


def load_wording(root: Path) -> list[PublicWording]:
    # LEARNER TODO: load only listed direct-child JSON files; validate the
    # entire set, reject duplicates, and never return a partial corpus.
    raise NotImplementedError("CAP-53 learner TODO: implement load_wording")
