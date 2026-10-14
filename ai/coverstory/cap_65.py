"""Authorized source-subset retrieval seam for CAP-65.

The principal is trusted request context. A query may never select a different
owner. Registry and scorer implementations are instructor supplied interfaces;
the learner owns the authorization, source selection, and ordering of calls.
"""
from __future__ import annotations

from dataclasses import dataclass
from typing import Literal, Protocol


@dataclass(frozen=True)
class Principal:
    subject: str | None
    roles: frozenset[str] = frozenset()

    @property
    def authenticated(self) -> bool:
        return self.subject is not None


@dataclass(frozen=True)
class RetrievalQuery:
    text: str
    # This is an untrusted compatibility field. It must never grant access.
    owner_id: str | None = None
    scope: Literal["public", "private", "all"] = "all"


@dataclass(frozen=True)
class AccessControl:
    owner_id: str | None = None
    assigned_support_ids: frozenset[str] = frozenset()


@dataclass(frozen=True)
class SourceDocument:
    source_id: str
    visibility: Literal["public", "private"]
    public_text: str | None = None
    applicant_text: str | None = None
    support_excerpt: str | None = None
    acl: AccessControl | None = None


@dataclass(frozen=True)
class Candidate:
    """Only the allowed, minimal text crosses the scoring boundary."""

    source_id: str
    text: str


@dataclass(frozen=True)
class Hit:
    source_id: str
    text: str
    score: float


class ScoringRegistry(Protocol):
    documents: tuple[SourceDocument, ...]

    def score(self, query: str, candidates: list[Candidate], k: int) -> list[Hit]: ...


class AccessDenied(PermissionError):
    """A private retrieval request failed closed before scoring."""


def retrieve_for_principal(
    principal: Principal | None,
    query: RetrievalQuery,
    registry: ScoringRegistry,
    k: int,
) -> list[Hit]:
    # LEARNER TODO: reject untrusted owner overrides, build only the public or
    # explicitly authorized source subset, and pass the minimum allowed text
    # to the scorer only after authorization. Denials must occur before score.
    raise NotImplementedError("CAP-65 learner TODO: implement retrieve_for_principal")
