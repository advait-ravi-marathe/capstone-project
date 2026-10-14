from __future__ import annotations
import json
import unittest
from pathlib import Path
from ai.coverstory.cap_73 import validate_split

FIXTURE = Path(__file__).resolve().parents[3] / "fixtures/coverstory/CAP-73/synthetic-v1.json"

class CAP73AcceptanceTest(unittest.TestCase):
    def test_distinct_development_and_golden_sets(self):
        data=json.loads(FIXTURE.read_text())
        report=validate_split(data["development"],data["golden"])
        self.assertTrue(report.valid)
        self.assertEqual((1,1,()),(report.development_count,report.golden_count,report.violations))

    def test_normalized_question_overlap_is_rejected(self):
        data=json.loads(FIXTURE.read_text())
        report=validate_split(data["overlap_question"]["development"],data["overlap_question"]["golden"])
        self.assertIn("QUESTION_OVERLAP",report.violations)

    def test_same_source_family_is_rejected_even_for_different_questions(self):
        data=json.loads(FIXTURE.read_text())
        report=validate_split(data["overlap_family"]["development"],data["overlap_family"]["golden"])
        self.assertIn("SOURCE_FAMILY_OVERLAP",report.violations)

    def test_case_id_overlap_or_empty_golden_is_rejected(self):
        data=json.loads(FIXTURE.read_text())
        duplicate=validate_split(data["overlap_case_id"]["development"],data["overlap_case_id"]["golden"])
        empty=validate_split(data["development"],[])
        self.assertIn("CASE_ID_OVERLAP",duplicate.violations)
        self.assertIn("INVALID_SPLIT",empty.violations)

if __name__ == "__main__": unittest.main()
