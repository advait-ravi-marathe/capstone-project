"""M4.1 learner acceptance; an evaluator is supplied as evidence, never invented here."""
import importlib, os, unittest
from ai.fde_v2.m41 import reference
C=importlib.import_module(os.environ.get('M41_CANDIDATE','ai.fde_v2.m41.learner'))
QUESTIONS=[{"topic":k,"question":f"Describe {k}."} for k in sorted(reference.REQUIRED_TOPICS)]
NOTES=[{"topic":k,"answer":f"Synthetic practice answer: {k}."} for k in sorted(reference.REQUIRED_TOPICS)]
RECORD={"questions":QUESTIONS,"notes":NOTES,"solution_designed_before_questions":False,"evidence_class":"role_play",
        "decision":"no_go","decision_rationale":"Practice exercise supports no-go.","non_goals":[],"claims":[]}

class M41Acceptance(unittest.TestCase):
    def test_provenance_and_no_go_decision_are_preserved(self):
        self.assertTrue(C.validate_discovery(RECORD)["valid"])
        assessment=C.assess_rubric({"evaluator_kind":"peer","evaluator_ref":"peer-ref","evidence_ref":"actual-review",
           "scores":{k:3 for k in ("questions_first","baseline","data_access","risks","success_criteria","non_ai_or_no_go")}})
        self.assertEqual(assessment["status"],"UNVERIFIED_SCORE")
        self.assertEqual(C.build_pilot_contract(RECORD,assessment)["status"],"BLOCKED")
    def test_missing_topics_or_claimed_real_user_without_authority_is_rejected(self):
        with self.assertRaises((ValueError,AssertionError)): C.validate_discovery({**RECORD,"questions":[]})
        with self.assertRaises((ValueError,AssertionError)): C.validate_discovery({**RECORD,"evidence_class":"real_user"})

if __name__=='__main__': unittest.main()
