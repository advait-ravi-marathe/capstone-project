"""Independent CAP-73 instructor oracle; no model or golden labels involved."""
import json
import re
import unittest
from pathlib import Path

FIXTURE = Path(__file__).resolve().parents[2] / "fixtures/coverstory/CAP-73/synthetic-v1.json"

def normalize(question: str) -> str:
    return " ".join(question.lower().split())

def reference_validate(dev, golden):
    violations=[]
    if not dev or not golden: violations.append("INVALID_SPLIT")
    d_ids={x.get("case_id") for x in dev}; g_ids={x.get("case_id") for x in golden}
    d_questions={normalize(x.get("question","")) for x in dev}; g_questions={normalize(x.get("question","")) for x in golden}
    d_families={x.get("source_family_id") for x in dev}; g_families={x.get("source_family_id") for x in golden}
    if d_ids & g_ids: violations.append("CASE_ID_OVERLAP")
    if d_questions & g_questions: violations.append("QUESTION_OVERLAP")
    if d_families & g_families: violations.append("SOURCE_FAMILY_OVERLAP")
    return {"valid":not violations,"development_count":len(dev),"golden_count":len(golden),"violations":tuple(violations)}

class CAP73InstructorInfrastructureTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls): cls.data=json.loads(FIXTURE.read_text())
    def test_distinct_split_has_no_violations(self):
        r=reference_validate(self.data['development'],self.data['golden']); self.assertTrue(r['valid']); self.assertEqual((1,1,()),(r['development_count'],r['golden_count'],r['violations']))
    def test_case_id_overlap(self):
        c=self.data['overlap_case_id']; r=reference_validate(c['development'],c['golden']); self.assertIn('CASE_ID_OVERLAP',r['violations'])
    def test_normalized_question_overlap(self):
        c=self.data['overlap_question']; r=reference_validate(c['development'],c['golden']); self.assertIn('QUESTION_OVERLAP',r['violations'])
    def test_source_family_overlap(self):
        c=self.data['overlap_family']; r=reference_validate(c['development'],c['golden']); self.assertIn('SOURCE_FAMILY_OVERLAP',r['violations'])
    def test_empty_golden_is_invalid(self):
        r=reference_validate(self.data['development'],[]); self.assertIn('INVALID_SPLIT',r['violations'])

if __name__ == '__main__': unittest.main()
