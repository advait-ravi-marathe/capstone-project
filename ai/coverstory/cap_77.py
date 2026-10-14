"""Extractive answer/source consistency seam for CAP-77.

This is a deterministic provenance check. It does not decide whether a source
is current operational truth, and it cannot update payments or proposals.
"""
from __future__ import annotations

from dataclasses import dataclass
from typing import Literal, Mapping

from ai.coverstory.cap_59 import Citation, RegistryEntry


@dataclass(frozen=True)
class Claim:
    text: str
    citation: Citation


@dataclass(frozen=True)
class StructuredAnswer:
    claims: tuple[Claim, ...]


AuthorizedSource = RegistryEntry


ConsistencyCode = Literal[
    "CLAIM_TEXT_MISMATCH",
    "UNKNOWN_SOURCE",
    "SOURCE_NOT_AUTHORIZED",
    "SOURCE_NOT_APPLICABLE",
    "CITATION_MISMATCH",
]


@dataclass(frozen=True)
class ConsistencyIssue:
    claim_index: int
    code: ConsistencyCode


@dataclass(frozen=True)
class ConsistencyReport:
    status: Literal["SUPPORTED", "UNSUPPORTED", "ABSTAIN_REQUIRED"]
    valid_claims: int
    issues: tuple[ConsistencyIssue, ...] = ()
    # A matching extract proves provenance consistency only, never live state.
    authoritative_state: Literal[False] = False


AuthorizedSources = Mapping[tuple[str, str], RegistryEntry]


def check_answer(answer: StructuredAnswer, authorizedSources: AuthorizedSources) -> ConsistencyReport:
    # LEARNER TODO: require every nonempty claim to match its exact authorized,
    # applicable source slice and citation; one invalid claim makes the answer
    # UNSUPPORTED. Empty claims require ABSTAIN_REQUIRED. This seam verifies
    # extractive consistency only and never asserts authoritative payment state.
    raise NotImplementedError("CAP-77 learner TODO: implement check_answer")
