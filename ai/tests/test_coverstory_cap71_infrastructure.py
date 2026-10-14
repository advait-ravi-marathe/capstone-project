"""Fixture witness and known defect discrimination; no learner seam call."""
import json
import unittest
from pathlib import Path
FIXTURE = Path(__file__).resolve().parents[2] / "fixtures/coverstory/CAP-71/synthetic-v1.json"

def reference(source_id, version, text, max_chars, overlap):
    if not source_id or not version or max_chars <= 0 or overlap < 0 or overlap >= max_chars:
        raise ValueError("invalid chunk metadata")
    result = []
    start = 0
    while start < len(text):
        end = min(start + max_chars, len(text))
        result.append((start, end, text[start:end]))
        if end == len(text): break
        start = end - overlap
    return result

def off_by_one_defect(text, max_chars, overlap):
    return [(start, min(start + max_chars, len(text)), text[start:min(start + max_chars, len(text))])
            for start in range(0, len(text), max_chars - overlap + 1)]

class CAP71InstructorInfrastructureTest(unittest.TestCase):
    def test_fixture_matches_expected_codepoint_slices(self):
        d=json.loads(FIXTURE.read_text())
        self.assertEqual("CAP-71-synthetic-v1",d["fixture_id"])
        actual=reference(d["source_id"],d["version"],d["text"],d["max_chars"],d["overlap"])
        self.assertEqual([(x["start"],x["end"],x["text"]) for x in d["expected"]],actual)
        self.assertEqual([],reference("s","v1","",4,1))

    def test_positive_and_off_by_one_negative_witness(self):
        good=reference("s","v1","abcdefghij",4,1)
        bad=off_by_one_defect("abcdefghij",4,1)
        self.assertEqual([(0,4,"abcd"),(3,7,"defg"),(6,10,"ghij")],good)
        self.assertNotEqual(good,bad)

    def test_invalid_bounds_rejected(self):
        for args in [("s","v1","x",4,4),("s","","x",4,1),("s","v1","x",0,0)]:
            with self.assertRaises(ValueError): reference(*args)

if __name__ == "__main__": unittest.main()
