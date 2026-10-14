"""CAP-75 provider-ingestion failure classification seam; learner logic TODO."""
from __future__ import annotations

from typing import Literal, NotRequired, TypedDict


class ProviderFailureRun(TypedDict):
    kind: Literal["malformed_json", "sql_timeout", "event_hash_conflict", "unknown"]
    batch_id: str
    line_number: NotRequired[int]
    event_id: NotRequired[str]
    existing_hash: NotRequired[str]
    incoming_hash: NotRequired[str]
    commit_state: NotRequired[str]
    exception_type: NotRequired[str]
    exception_text: NotRequired[str]


class ProviderFailure(TypedDict):
    stage: str
    code: str
    retryable: bool
    evidence_ids: list[str]


def classify_provider_failure(run: ProviderFailureRun) -> ProviderFailure:
    # LEARNER TODO: classify from structured stage and durable outcome facts.
    # Evidence may contain identifiers only; never include exception text/secrets.
    raise NotImplementedError("CAP-75 learner TODO: classify_provider_failure")
