"""M4.1 mechanics distinguish evaluator fixtures, peers and actual real-user evidence."""
import unittest
from ai.fde_v2.m41 import learner, reference, workflow

QUESTIONS=[{"topic":k,"question":f"How does {k} work today?"} for k in sorted(reference.REQUIRED_TOPICS)]
NOTES=[{"topic":k,"answer":f"Synthetic role-play answer about {k}."} for k in sorted(reference.REQUIRED_TOPICS)]
DISCOVERY={"questions":QUESTIONS,"solution_designed_before_questions":False,"evidence_class":"role_play",
           "notes":NOTES,"decision":"non_ai","decision_rationale":"The deterministic checklist already covers the stated need.",
           "pilot_scope":"compare current search with a checklist","non_goals":["automation"],"claims":[]}
RUBRIC={"evaluator_kind":"peer","evaluator_ref":"peer-review-1","evidence_ref":"fixture-1",
        "scores":{k:4 for k in ("questions_first","baseline","data_access","risks","success_criteria","non_ai_or_no_go")},
        "minimum_total":24}

class M41Mechanics(unittest.TestCase):
    def test_question_first_non_ai_decision_and_evaluator_provenance(self):
        out=workflow.review_discovery(DISCOVERY,RUBRIC,adapter=reference)
        self.assertEqual(out["pilot_contract"]["decision"],"non_ai")
        self.assertEqual(out["pilot_contract"]["status"],"ROLEPLAY_EXERCISE_AVAILABLE")
        self.assertEqual(out["real_user_gate"],"BLOCKED")
        self.assertEqual(out["rubric"]["evaluator_kind"],"peer")
        self.assertEqual(out["rubric"]["status"],"UNVERIFIED_SCORE")

    def test_absent_evaluator_and_unauthorized_real_user_remain_blocked(self):
        self.assertEqual(reference.assess_rubric({})["status"],"BLOCKED")
        with self.assertRaises(ValueError): reference.validate_discovery({**DISCOVERY,"evidence_class":"real_user"})
        with self.assertRaises(ValueError): reference.validate_discovery({**DISCOVERY,"solution_designed_before_questions":True})
        with self.assertRaises(ValueError): reference.validate_discovery({**DISCOVERY,"notes":[]})
        with self.assertRaises(ValueError): reference.validate_discovery({**DISCOVERY,"decision_rationale":" "})
        self.assertEqual(reference.assess_rubric({**RUBRIC,"evaluator_kind":"role_play"})["status"],"BLOCKED")

    def test_claimed_real_user_requires_review_never_self_passes(self):
        record={**DISCOVERY,"evidence_class":"real_user","authorized_contact":True,"evidence_ref":"claimed-ref"}
        self.assertEqual(reference.validate_discovery(record)["real_user_experience"],"REVIEW_REQUIRED")
        self.assertEqual(workflow.review_discovery(record,RUBRIC,adapter=reference)["real_user_gate"],"REVIEW_REQUIRED")

    def test_unsubstantiated_savings_claim_and_rubric_mutation_rejected(self):
        with self.assertRaises(ValueError): reference.validate_discovery({**DISCOVERY,"claims":["customer savings achieved"]})
        for text in ("customer identity fabricated","customer in production","customer history exists","we have customers using this"):
            with self.subTest(text=text), self.assertRaises(ValueError):
                reference.validate_discovery({**DISCOVERY,"claims":[{"claim_type":"synthetic_observation","text":text}]})
        safe={**DISCOVERY,"claims":[{"claim_type":"limitation","text":"No customer data is included in this local fixture."}]}
        self.assertTrue(reference.validate_discovery(safe)["valid"])
        bad={**RUBRIC,"scores":{**RUBRIC["scores"],"risks":99}}
        with self.assertRaises(ValueError): reference.assess_rubric(bad)
        with self.assertRaises(AssertionError): self.assertEqual(reference.assess_rubric(RUBRIC)["exercise_result"],"BELOW_EXERCISE_RUBRIC")
        self.assertEqual(reference.assess_rubric(RUBRIC)["evaluator_verification"],"BLOCKED")
        with self.assertRaises(NotImplementedError): learner.validate_discovery(DISCOVERY)

    def test_pilot_decision_requires_complete_customer_facing_contract(self):
        pilot={**DISCOVERY,"decision":"pilot","pilot_scope":"one synthetic lookup workflow","non_goals":["production use"],
               "responsibilities":"learner operates local demo; reviewer observes","acceptance_criteria":"predeclared rubric and rollback trigger",
               "rollback_criteria":"return to keyword baseline on any severe error","customer_explanation":"local prototype; no customer data or production claims"}
        built=reference.build_pilot_contract(pilot,reference.assess_rubric(RUBRIC))
        self.assertEqual(built["status"],"ROLEPLAY_EXERCISE_AVAILABLE")
        built=reference.build_pilot_contract(pilot,{"status":"VERIFIED_ASSESSMENT","exercise_result":"MEETS_EXERCISE_RUBRIC"})
        self.assertEqual(built["status"],"ROLEPLAY_EXERCISE_AVAILABLE")
        self.assertEqual(built["evaluator_verification"],"BLOCKED")
        for key in ("responsibilities","acceptance_criteria","rollback_criteria","customer_explanation"):
            self.assertTrue(built[key])
        self.assertIn("local practice only",built["reason"])
        with self.assertRaises(ValueError): reference.validate_discovery({**pilot,"rollback_criteria":""})

if __name__=='__main__': unittest.main()
