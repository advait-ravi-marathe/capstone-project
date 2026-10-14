from __future__ import annotations
import json
import unittest
from pathlib import Path
from ai.coverstory.cap_71 import Chunk, chunk_text

FIXTURE = Path(__file__).resolve().parents[3] / "fixtures/coverstory/CAP-71/synthetic-v1.json"

class CAP71AcceptanceTest(unittest.TestCase):
    def test_known_overlapping_slices(self):
        data = json.loads(FIXTURE.read_text())
        chunks = chunk_text(data["source_id"], data["version"], data["text"], data["max_chars"], data["overlap"])
        self.assertEqual([(x["start"], x["end"], x["text"]) for x in data["expected"]],
                         [(c.start, c.end, c.text) for c in chunks])

    def test_exact_size_text_is_one_chunk_and_empty_is_none(self):
        self.assertEqual([Chunk("s", "v1", 0, 4, "abcd")], chunk_text("s", "v1", "abcd", 4, 1))
        self.assertEqual([], chunk_text("s", "v1", "", 4, 1))

    def test_invalid_overlap_or_missing_version_fails(self):
        with self.assertRaises(ValueError): chunk_text("s", "v1", "abcd", 4, 4)
        with self.assertRaises(ValueError): chunk_text("s", "", "abcd", 4, 1)

    def test_chunk_offsets_reconstruct_exact_source_slices(self):
        source = "public synthetic payment status"
        chunks = chunk_text("s", "v2", source, 8, 2)
        self.assertTrue(all(source[c.start:c.end] == c.text for c in chunks))

if __name__ == "__main__": unittest.main()
