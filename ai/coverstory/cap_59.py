"""CAP-59 citation data contract; verification remains learner-owned."""
from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class Citation:
    source_id: str
    version: str
    start: int
    end: int
    excerpt: str
    content_sha256: str


@dataclass(frozen=True)
class RegistryEntry:
    text: str
    authorized: bool = True
    applicable: bool = True


@dataclass(frozen=True)
class Verification:
    status: str
    exact_text: str | None = None


def verify_citation(citation: Citation, registry: dict[tuple[str, str], RegistryEntry]) -> Verification:
    # LEARNER TODO: exact version lookup, authorization/applicability, Python
    # code-point bounds, nonempty excerpt, exact text and SHA-256 verification.
    raise NotImplementedError("CAP-59 learner TODO: implement verify_citation")
