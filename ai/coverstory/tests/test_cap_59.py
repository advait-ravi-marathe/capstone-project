from __future__ import annotations

import hashlib
import json
import unittest
from pathlib import Path

from ai.coverstory.cap_59 import Citation, RegistryEntry, verify_citation

FIXTURE = Path(__file__).resolve().parents[3] / "fixtures/coverstory/CAP-59/synthetic-v1.json"


class CAP59AcceptanceTest(unittest.TestCase):
    def setUp(self) -> None:
        self.fixture = json.loads(FIXTURE.read_text(encoding="utf-8"))
        self.registry = {(row["source_id"], row["version"]): RegistryEntry(row["text"], row["authorized"], row["applicable"])
                         for row in self.fixture["registry"]}

    def citation(self, *, version: str = "v2", start: int = 0, end: int = 7,
                 excerpt: str = "waiting", digest: str | None = None) -> Citation:
        text = self.registry[("source-A", "v2")].text
        return Citation("source-A", version, start, end, excerpt, digest or hashlib.sha256(text.encode()).hexdigest())

    def test_exact_source_version_and_codepoint_excerpt_is_valid(self) -> None:
        result = verify_citation(self.citation(), self.registry)
        self.assertEqual("VALID", result.status); self.assertEqual("waiting", result.exact_text)

    def test_empty_slice_is_rejected(self) -> None:
        self.assertEqual("EMPTY_EXCERPT", verify_citation(self.citation(start=0, end=0, excerpt=""), self.registry).status)

    def test_unknown_source_and_version_never_fall_back_to_latest(self) -> None:
        self.assertEqual("UNKNOWN_SOURCE", verify_citation(Citation("missing", "v2", 0, 1, "x", "0"*64), self.registry).status)
        self.assertEqual("UNKNOWN_VERSION", verify_citation(self.citation(version="v9"), self.registry).status)

    def test_excerpt_and_content_hash_tampering_have_distinct_statuses(self) -> None:
        self.assertEqual("EXCERPT_MISMATCH", verify_citation(self.citation(excerpt="period"), self.registry).status)
        self.assertEqual("CONTENT_HASH_MISMATCH", verify_citation(self.citation(digest="0"*64), self.registry).status)

    def test_authority_and_applicability_are_required_even_when_hash_matches(self) -> None:
        text = "waiting period"; digest = hashlib.sha256(text.encode()).hexdigest()
        citation = Citation("source-A", "v2", 0, 7, "waiting", digest)
        self.assertEqual("NOT_AUTHORIZED", verify_citation(citation, {("source-A", "v2"): RegistryEntry(text, False, True)}).status)
        self.assertEqual("NOT_APPLICABLE", verify_citation(citation, {("source-A", "v2"): RegistryEntry(text, True, False)}).status)

    def test_offsets_are_python_code_points_not_utf8_byte_offsets(self) -> None:
        text = "café"; entry = RegistryEntry(text)
        citation = Citation("unicode", "v1", 0, 4, text, hashlib.sha256(text.encode()).hexdigest())
        self.assertEqual("VALID", verify_citation(citation, {("unicode", "v1"): entry}).status)


if __name__ == "__main__": unittest.main()
