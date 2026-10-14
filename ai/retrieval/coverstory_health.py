"""CAP-41 public synthetic wording scaffold; retrieval ordering is learner work."""
from dataclasses import dataclass


@dataclass(frozen=True, slots=True)
class PublicWording:
    source_id: str
    version: str | None
    evidence_status: str
    text: str


@dataclass(frozen=True, slots=True)
class Hit:
    source_id: str
    version: str
    evidence_status: str
    excerpt: str
    score: int


def tokenize(text: str) -> set[str]:
    """Instructor-provided lowercase whitespace tokenizer."""
    return set(text.lower().split())


def retrieve(query: str, documents: list[PublicWording], k: int) -> list[Hit]:
    """Return traceable evidence hits. TODO: implement the CAP-41 contract."""
    raise NotImplementedError("CAP-41 learner TODO: implement deterministic evidence retrieval")
