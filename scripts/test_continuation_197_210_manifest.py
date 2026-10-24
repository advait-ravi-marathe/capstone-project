import json
import shlex
import unittest
from datetime import date, timedelta
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BATCH = ROOT / "docs/reboot/continuation-197-210"


class Continuation197210ManifestTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.manifest = json.loads((BATCH / "manifest.json").read_text())
        cls.owner = json.loads((BATCH / "scrum-owner-manifest.json").read_text())
        cls.plan = json.loads((BATCH / "days-197-210-transfer-plan.json").read_text())

    def test_dates_budget_and_exports(self):
        days = self.manifest["days"]
        self.assertEqual(list(range(197, 211)), [d["day"] for d in days])
        self.assertEqual(date(2027, 5, 8), date.fromisoformat(days[0]["date"]))
        self.assertEqual(date(2027, 5, 21), date.fromisoformat(days[-1]["date"]))
        self.assertEqual([203, 210], [d["day"] for d in days if d["recovery_day"]])
        self.assertEqual(14, self.owner["story_count"])
        self.assertFalse(self.owner["board_writes"])
        self.assertIsNone(self.manifest["course_assignment"])
        owner_by_day = {d["day_number"]: d for d in self.owner["cards"]}
        plan_by_day = {d["day"]: d for d in self.plan["days"]}
        for d in days:
            b = d["budget"]
            self.assertEqual(165, b["study"] + b["practice_and_tests"] + b["evidence"])
            self.assertEqual(210, b["engineering_minutes"] + b["shared_interview_or_remaining_reviews_minutes"])
            self.assertEqual("unobserved_not_implied_by_scaffold", d["learner_status"])
            self.assertEqual(d["engineering_architecture"], owner_by_day[d["day"]]["engineering_architecture"])
            self.assertEqual(d["engineering_architecture"], plan_by_day[d["day"]]["engineering_architecture"])
            self.assertEqual(d["selectors"], owner_by_day[d["day"]]["selectors"])
            shared=d["shared_interview_block"]
            self.assertIsNotNone(shared["question_title"])
            self.assertEqual(45, shared["minutes"])
            if shared["question_id"]:
                question_id=int(shared["question_id"][1:])
                bank=(ROOT/"docs/reboot/interview/question-bank.md").read_text()
                self.assertIn(f'<a id="q{question_id:02d}"></a>', bank)
            else:
                self.assertTrue(shared.get("review_task"))
            contract = BATCH / "contracts" / f"day-{d['day']}.md"
            self.assertTrue(contract.exists())

    def test_scoped_test_selectors_resolve(self):
        loader = unittest.defaultTestLoader
        for d in self.manifest["days"]:
            for command in d["selectors"]:
                words = shlex.split(command)
                if words[0] == "python3" and words[1] == "-m":
                    self.assertEqual(["python3", "-m", "unittest"], words[:3])
                    names = words[3:]
                elif words[0] == "./mvnw":
                    testarg=next((w for w in words if w.startswith("-Dtest=")),None)
                    self.assertIsNotNone(testarg)
                    self.assertIn(testarg.split("=",1)[1].split(",")[0].split("#")[0], [p.stem for p in (ROOT/"src/test/java").rglob("*Test.java")])
                    names = []
                elif words[0] == "python3" and len(words)>1 and words[1].endswith(".py"):
                    self.assertTrue((ROOT/words[1]).is_file(), command)
                    names = []
                elif words[0].endswith(".sh"):
                    self.assertTrue((ROOT/words[0]).is_file(), command)
                    names = []
                elif words[0]=="scripts/package-sprint5-demo.sh":
                    self.assertTrue((ROOT/words[0]).is_file(),command); names=[]
                else:
                    self.fail(f"unsupported selector command: {command}")
                suite = unittest.TestSuite(loader.loadTestsFromName(x) for x in names)
                if not names:
                    continue
                cases = []
                def visit(node):
                    if isinstance(node, unittest.TestSuite):
                        for child in node:
                            visit(child)
                    else:
                        cases.append(node)
                visit(suite)
                self.assertTrue(cases)
                self.assertFalse(any(isinstance(case, unittest.loader._FailedTest) for case in cases))

    def test_allocation_notes_sources_and_package_prerequisites_are_synchronized(self):
        import subprocess
        result=subprocess.run(["python3", "scripts/generate_continuation_197_210.py"], cwd=ROOT, text=True, capture_output=True)
        self.assertEqual(0,result.returncode,result.stdout+result.stderr)
        for d in self.manifest["days"]:
            self.assertEqual(165,sum(d["workstream_minutes"].values()))
            self.assertEqual(d["workstream_minutes"],d["budget"]["workstream_minutes"])
            self.assertEqual(d["selectors"],next(x for x in self.owner["cards"] if x["day_number"]==d["day"])["selectors"])
            contract=(BATCH/"contracts"/f"day-{d['day']}.md").read_text()
            self.assertIn(d["engineering_architecture"]["decision"],(BATCH/"assistant-architecture-notes.md").read_text())
            self.assertTrue(all(x in contract for x in d["selectors"]))
        self.assertEqual(1380,sum(d["workstream_minutes"]["coverstory_project_recovery_minutes"] for d in self.manifest["days"]))
        self.assertEqual(180,sum(d["workstream_minutes"]["offline_ai_implementation_evaluation_minutes"] for d in self.manifest["days"]))
        for day in (203,209):
            d=self.manifest["days"][day-197]
            self.assertEqual(["scripts/package-sprint5-demo.sh","python3 scripts/check_sprint5_package_evidence.py"],d["selectors"][:2])
            self.assertIn("LocalPackagedBrowserAcceptanceTest",d["selectors"][2])

    def test_no_later_activation_or_prohibited_scope(self):
        text = "\n".join(p.read_text() for p in BATCH.rglob("*.md"))
        self.assertIn("does not satisfy or waive", self.manifest["gate_policy"])
        self.assertNotIn("learner_completion", self.manifest)
        self.assertIn("course_assignment", self.manifest)
        self.assertEqual("not_met", self.manifest["inspection_findings"]["day153_day154_gate"])
        self.assertTrue(all(d["shared_interview_block"]["question_title"] for d in self.manifest["days"]))
        self.assertIn("DSA remains external", text)


if __name__ == "__main__":
    unittest.main()
