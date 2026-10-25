"""Instructor witness and mutation checks for the final closeout assignments.

The witness is kept here and does not modify or complete learner starters.
"""
from __future__ import annotations
import hashlib, importlib.util, json, subprocess, sys, unittest
from pathlib import Path
from unittest.mock import patch

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/"docs/reboot/continuation-267-270"
GEN=ROOT/"scripts/generate_continuation_267_270.py"

def load(name,path):
    spec=importlib.util.spec_from_file_location(name,path); mod=importlib.util.module_from_spec(spec)
    sys.modules[name]=mod; spec.loader.exec_module(mod); return mod

portfolio_tests=load("closeout_portfolio_acceptance",ROOT/"ai/grounded/learner_acceptance/test_closeout_portfolio_267.py")
readiness_tests=load("closeout_readiness_acceptance",ROOT/"ai/grounded/learner_acceptance/test_cert_readiness_267.py")
portfolio=portfolio_tests.portfolio
readiness=readiness_tests.readiness
reference=load("closeout_reference",ROOT/"scripts/continuation_267_270_reference.py")

def run_portfolio(method, mutant=None):
    def witness(*args, **kwargs):
        if mutant=="allow_symlink":
            # Model a reviewer that silently accepts every symlink path.
            return {"source_sha":"0"*40,"artifacts":[],"artifact_manifest_sha256":"0"*64,
                    "portfolio_status":"local_artifacts_receipted","learner_completion_awarded":False,
                    "professional_ownership_claimed":False}
        result=reference.build_receipt(*args,**kwargs)
        if mutant=="wrong_digest": result["artifact_manifest_sha256"]="0"*64
        elif mutant=="source_as_artifact": result["artifact_manifest_sha256"]=result["source_sha"]
        elif mutant=="missing_file": result["artifacts"]=result["artifacts"][:1]
        elif mutant=="awarded": result["learner_completion_awarded"]=True
        return result
    with patch.object(portfolio,"build_receipt",side_effect=witness): method()

def run_readiness(method, mutant=None):
    def witness(record, guide):
        result=reference.review_readiness(record,guide)
        if mutant=="always_ready":
            return readiness.Readiness("ready_for_separate_booking_review",(),len(record.get("attempts",[])),False,False)
        if mutant=="pass_claim":
            return readiness.Readiness(result.status,result.reason_codes,result.assessment_count,True,True)
        if mutant=="wrong_count":
            return readiness.Readiness(result.status,result.reason_codes,result.assessment_count+1,result.exam_passed,result.booking_authorized)
        if mutant=="accept_invalid_domain_score" and any(
                isinstance(a,dict) and isinstance(a.get("domain_scores"),dict) and
                any(type(score) is not int or score<0 or score>100 for score in a["domain_scores"].values())
                for a in record.get("attempts",[])):
            return readiness.Readiness("ready_for_separate_booking_review",(),len(record.get("attempts",[])),False,False)
        return result
    with patch.object(readiness,"review_readiness",side_effect=witness): method()

class CloseoutInventoryTests(unittest.TestCase):
    def test_generator_exports_four_complete_contracts_and_only_then_inventory(self):
        names=("manifest.json","days-267-270-transfer-plan.json","scrum-owner-manifest.json","verified-sources.json")
        before={n:(OUT/n).read_bytes() for n in names}
        subprocess.run([sys.executable,str(GEN)],cwd=ROOT,check=True,capture_output=True)
        self.assertEqual(before,{n:(OUT/n).read_bytes() for n in names})
        src=json.loads((OUT/"plan-source.json").read_text())
        man=json.loads((OUT/"manifest.json").read_text())
        owner=json.loads((OUT/"scrum-owner-manifest.json").read_text())
        inventory=json.loads((ROOT/"docs/reboot/continuation-197-270-inventory.json").read_text())
        self.assertEqual(list(range(267,271)),[r["day"] for r in man["days"]])
        self.assertEqual({"project_implementation_tests":150,"ai_workflow_implementation_evaluation":180,
            "certification_labs_questions":130,"ai_concept_source_study":90,"certification_domain_study":30,
            "core_systems_security_cloud_study":20,"evidence":60},man["engineering_allocation"])
        self.assertFalse(owner["board_writes"]); self.assertEqual(4,owner["story_count"])
        self.assertEqual(270,inventory["through_day"]); self.assertFalse(inventory["learner_completion_awarded"])
        self.assertEqual([(197,210),(211,224),(225,238),(239,252),(253,266),(267,270)],
                         [(x["start_day"],x["end_day"]) for x in inventory["windows"]])
        for i,(task,row,card) in enumerate(zip(src["days"],man["days"],owner["cards"])):
            self.assertEqual((__import__("datetime").date(2027,8,9)+__import__("datetime").timedelta(days=i)).isoformat(),row["date"])
            self.assertEqual(task["date"],row["date"]); self.assertEqual(165,sum(row["workstream_minutes"].values()))
            self.assertEqual(45,row["shared_interview_block"]["minutes"])
            self.assertEqual("unobserved_not_implied_by_scaffold",row["learner_status"])
            self.assertEqual(row["workstream_minutes"],card["engineering"]["allocation"])
            for category,minutes in row["workstream_minutes"].items():
                entry=row["task_minutes"][category]
                self.assertEqual(minutes,entry["minutes"])
                self.assertTrue(entry["task"].strip() and entry["command"].strip() and entry["evidence"].strip())
                if minutes:
                    self.assertTrue(entry["source_paths"] or entry["source_keys"],(row["day"],category))
            contract=(OUT/f"contracts/day-{row['day']}.md").read_text()
            self.assertIn(f'id="day-{row["day"]}"',contract)
            self.assertIn("Assistant-supplied architecture",contract)
            import re
            for target in re.findall(r"\]\(([^)]+)\)",contract):
                if target.startswith(("http://","https://")): continue
                dest,_,anchor=target.partition("#"); path=(OUT/"contracts"/dest).resolve()
                self.assertTrue(path.exists(),target)
                if anchor:self.assertIn(f'id="{anchor}"',path.read_text())
        self.assertEqual({"certification_retrieval_review"},
                         {x["shared_interview_block"]["mode"] for x in man["days"]})
        self.assertEqual(4,len({x["shared_interview_block"]["source"] for x in man["days"]}))
        self.assertEqual(27,man["shared_allocation_reconciliation"]["published_days_197_266_sessions"]["interview_question_bank"])
        self.assertEqual(16,man["shared_allocation_reconciliation"]["final_assigned_sessions"]["certification_retrieval_review"])
        verified=json.loads((OUT/"verified-sources.json").read_text())
        source_rows={x["key"]:x for x in verified["sources"]}
        self.assertEqual("2026-10-24",verified["verified_at"])
        self.assertEqual("AWS Certified Solutions Architect - Associate (SAA-C03) - AWS Certified Solutions Architect - Associate",source_rows["SAA_GUIDE"]["title"])
        self.assertIn("Content Domain 4: Design Cost-Optimized Architectures",source_rows["SAA_GUIDE"]["sections"])
        self.assertIn("DVA-C02",source_rows["DEV_GUIDE"]["title"])
        self.assertIn("Content Domain 4: Troubleshooting and Optimization",source_rows["DEV_GUIDE"]["sections"])
        self.assertEqual("Design your eval process",source_rows["OPENAI_EVAL"]["sections"][0])
        readme=(OUT/"README.md").read_text()
        for phrase in ("SDE II knowledge/application comparison","Senior/SDE III knowledge/application comparison","Staff/Principal/FDE"):
            self.assertIn(phrase,readme)
        for day in (267,268,269,270):
            self.assertTrue((OUT/"contracts"/f"day-{day}.md").is_file())

    def test_dynamic_live_gates_and_actual_evaluation_outputs(self):
        gate239=load("closeout_gate239",ROOT/"scripts/check_continuation_239_entry_gate.py")
        gate253=load("closeout_gate253",ROOT/"scripts/check_continuation_253_entry_gate.py")
        prior=gate239.check(); current=gate253.check()
        self.assertIn(prior["status"],{"not_met","blocked","eligible_to_attempt"})
        self.assertIn(current["status"],{"not_met","blocked","eligible_to_attempt"})
        self.assertFalse(prior["completion_awarded"]); self.assertFalse(current["completion_awarded"])
        from ai.grounded.evaluation import CASES,evaluate,Case
        from ai.grounded.fixtures import CLAUSES
        expected={"total":3,"cited":2,"abstained":1,"exact_match":3,"citation_mismatch":0}
        self.assertEqual(expected,evaluate(CASES,CLAUSES))
        wrong=(Case(CASES[0].question,CASES[0].owner_id,"cited",("foreign@r9",)),)
        self.assertEqual({"total":1,"cited":1,"abstained":0,"exact_match":0,"citation_mismatch":1},evaluate(wrong,CLAUSES))
        with patch("ai.grounded.evaluation.explain",return_value=__import__("ai.grounded.evidence",fromlist=["Answer"]).Answer("cited","wrong",("foreign@r9",))):
            observed=evaluate((CASES[0],),CLAUSES)
        self.assertEqual(0,observed["exact_match"]); self.assertEqual(1,observed["citation_mismatch"])

class ActualAcceptanceMutationTests(unittest.TestCase):
    def test_portfolio_acceptance_runs_against_real_repository_bytes(self):
        run_portfolio(portfolio_tests.PortfolioReceiptAcceptance("test_day268_committed_source_and_actual_file_bytes"))
        run_portfolio(portfolio_tests.PortfolioReceiptAcceptance("test_day268_rejects_stale_duplicate_missing_and_unsafe_paths"))
        run_portfolio(portfolio_tests.PortfolioReceiptAcceptance("test_day268_rejects_symlink_files_and_symlinked_parent_directories"))

    def test_portfolio_assertions_reject_mutants_on_the_actual_acceptance_methods(self):
        cases=(("test_day268_committed_source_and_actual_file_bytes","wrong_digest"),
               ("test_day268_committed_source_and_actual_file_bytes","source_as_artifact"),
               ("test_day268_committed_source_and_actual_file_bytes","missing_file"),
               ("test_day268_committed_source_and_actual_file_bytes","awarded"),
               ("test_day268_rejects_symlink_files_and_symlinked_parent_directories","allow_symlink"))
        for method_name,mutant in cases:
            method=getattr(portfolio_tests.PortfolioReceiptAcceptance(method_name),method_name)
            with self.subTest(mutant=mutant),self.assertRaises(AssertionError):run_portfolio(method,mutant)

    def test_readiness_acceptance_and_actual_negative_rows(self):
        names=("test_day269_two_current_independent_results_are_only_booking_review_eligibility",
               "test_day269_exactly_eighty_percent_meets_internal_score_floor",
               "test_day269_stale_guide_duplicate_attempt_and_bad_scores_fail_closed")
        for name in names: run_readiness(getattr(readiness_tests.CertificationReadinessAcceptance(name),name))

    def test_readiness_assertions_reject_readiness_and_credential_claim_mutants(self):
        positive=readiness_tests.CertificationReadinessAcceptance("test_day269_two_current_independent_results_are_only_booking_review_eligibility")
        negative=readiness_tests.CertificationReadinessAcceptance("test_day269_stale_guide_duplicate_attempt_and_bad_scores_fail_closed")
        for method,mutant in ((positive.test_day269_two_current_independent_results_are_only_booking_review_eligibility,"pass_claim"),
                              (negative.test_day269_stale_guide_duplicate_attempt_and_bad_scores_fail_closed,"always_ready"),
                              (negative.test_day269_stale_guide_duplicate_attempt_and_bad_scores_fail_closed,"accept_invalid_domain_score"),
                              (positive.test_day269_two_current_independent_results_are_only_booking_review_eligibility,"wrong_count")):
            with self.subTest(mutant=mutant),self.assertRaises(AssertionError):run_readiness(method,mutant)

if __name__=="__main__":unittest.main()
