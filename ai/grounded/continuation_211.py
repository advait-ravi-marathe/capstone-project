"""Starter seams for the bounded, offline Days 211–224 policy explanation path.

All inputs are synthetic. The learner implements the TODOs and tests the
acceptance contract; this module is intentionally not a working retrieval stack.
"""
from __future__ import annotations
from dataclasses import dataclass
from typing import Literal

MAX_BUNDLE_BYTES = 16_384
MAX_RECORDS = 12
MAX_SNIPPET_CHARS = 512
MAX_RETRIEVAL_RESULTS = 3
ABSTAIN_TEXT = "Not enough approved evidence to answer."

@dataclass(frozen=True)
class ClauseRecord:
    source_id: str
    version: str
    owner_id: str
    status: Literal["approved", "draft"]
    text: str

@dataclass(frozen=True)
class CorpusBundle:
    bundle_id: str
    version: str
    records: tuple[ClauseRecord, ...]

@dataclass(frozen=True)
class EvidenceSnippet:
    source_id: str
    version: str
    excerpt: str

    @property
    def citation(self) -> str:
        return f"{self.source_id}@{self.version}"

@dataclass(frozen=True)
class DraftExplanation:
    text: str
    citations: tuple[str, ...]
    abstained: bool = False

@dataclass(frozen=True)
class GroundedExplanation:
    status: Literal["cited", "abstain"]
    text: str
    citations: tuple[str, ...]


def load_bundle(payload: bytes) -> CorpusBundle:
    """Parse a bounded JSON bundle with exact fields and unique source/version keys.

    TODO Days 212–215: reject invalid UTF-8/JSON, oversized payloads, extra or
    missing keys, malformed IDs/status/text, too many rows, and duplicate
    source/version pairs. Return immutable records without mutating input.
    """
    raise NotImplementedError("Days 212–215 learner TODO: validate and freeze the synthetic source bundle")


def rank_candidates(question: str, authorized_records: tuple[ClauseRecord, ...]) -> tuple[ClauseRecord, ...]:
    """Return deterministic literal candidates from already authorized records.

    TODO Day 216–217: score only records supplied by the authorization filter,
    order ties by stable source/version ID, and never return more than three.
    This is a transparent baseline, not semantic retrieval.
    """
    raise NotImplementedError("Days 216–217 learner TODO: rank the bounded authorized candidate set")


def retrieve(question: str, principal_id: str, bundle: CorpusBundle, *, limit: int = 3) -> tuple[EvidenceSnippet, ...]:
    """Filter owner and approved status before ranking; return exact text only.

    TODO Days 215–218: fail closed on invalid principal/query/limit, exclude
    draft and foreign-owner rows before ranking, and return exact source/version
    snippets within the retrieval cap.
    """
    raise NotImplementedError("Days 215–218 learner TODO: authorize, retrieve, and preserve source identity")


def validate_draft(draft: DraftExplanation, retrieved: tuple[EvidenceSnippet, ...]) -> GroundedExplanation:
    """Validate draft citations/text against this query's retrieved snippets.

    TODO Days 218–219: accept exact text with exact retrieved IDs only; reject
    unknown/stale citations, altered text, missing citations, and empty cited
    results. Fail closed to ABSTAIN_TEXT; never generate policy decisions.
    """
    raise NotImplementedError("Days 218–219 learner TODO: enforce citation grounding and explicit fallback")


def canonical_bundle_bytes(bundle: CorpusBundle) -> bytes:
    """Serialize the immutable bundle in the single documented canonical form.

    TODO Days 222–223: UTF-8 JSON, sorted object keys, compact separators,
    stable record ordering, one final LF; do not hash normalized text casually.
    """
    raise NotImplementedError("Days 222–223 learner TODO: serialize a reproducible source snapshot")


def evaluate_cases(cases, bundle: CorpusBundle) -> dict[str, int]:
    """Evaluate fixed synthetic cases against independent expected citations.

    TODO Day 220: implement local metrics only. Cases are tuples of
    (question, principal_id, expected_citation_or_None); don't use a model or
    derive expected labels from the workflow under test.
    """
    raise NotImplementedError("Day 220 learner TODO: compute independent offline evaluation metrics")
