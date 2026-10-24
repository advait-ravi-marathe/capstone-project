import json, shlex, unittest
from datetime import date, timedelta
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
BATCH=ROOT/"docs/reboot/continuation-183-196"

class StreamForge183196ManifestTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.manifest=json.loads((BATCH/"manifest.json").read_text(encoding="utf-8"))
        cls.plan=json.loads((BATCH/"streamforge-days-183-196-transfer-plan.json").read_text(encoding="utf-8"))
        cls.owner=json.loads((BATCH/"scrum-owner-manifest.json").read_text(encoding="utf-8"))
        cls.guidance=json.loads((BATCH/"verified-content-features-183-196-guidance.json").read_text(encoding="utf-8"))
    def test_batch_dates_budget_and_daily_chronology(self):
        m=self.manifest; self.assertEqual("learning/streamforge-days-183-196",m["branch"])
        self.assertEqual("fa45d70868e93da79bb2ff84a8c4a4ca77510924",m["base_commit"])
        self.assertEqual(list(range(183,197)),[d["day"] for d in m["days"]])
        self.assertEqual(date(2027,4,24),date.fromisoformat(m["days"][0]["date"]))
        self.assertEqual(date(2027,5,7),date.fromisoformat(m["days"][-1]["date"]))
        expected_dates=["2027-04-24","2027-04-25","2027-04-26","2027-04-27","2027-04-28","2027-04-29","2027-04-30","2027-05-01","2027-05-02","2027-05-03","2027-05-04","2027-05-05","2027-05-06","2027-05-07"]
        self.assertEqual(expected_dates,[d["date"] for d in m["days"]])
        self.assertIsNone(m["days"][6]["module"])
        self.assertIsNone(m["days"][12]["module"])
        self.assertIsNone(m["days"][13]["module"])
        self.assertEqual([189,196],[d["day"] for d in m["days"] if d["recovery_day"]])
        plan_days={d["day"]:d for d in self.plan["days"]}
        owner_days={d["day_number"]:d for d in self.owner["cards"]}
        refmap={r["key"]:r for r in self.guidance["primaryReferences"]}
        self.assertEqual(14,self.owner["story_count"])
        self.assertFalse(self.owner["board_writes"])
        for d in m["days"]:
            b=d["budget"]; self.assertEqual(165,b["engineering_minutes"])
            self.assertEqual(165,b["study"]+b["practice_and_tests"]+b["evidence"])
            self.assertEqual(210,b["engineering_minutes"]+b["shared_interview_or_remaining_reviews_minutes"])
            self.assertEqual(210,b["total"])
            self.assertTrue(all(dep < d["day"] for dep in d["depends_on_days"]))
            source=plan_days[d["day"]];owner=owner_days[d["day"]]
            self.assertEqual((source["date"],source["title"],source["signature"],source["selectors"],source["depends_on_days"],source["recovery_day"]),
                             (d["date"],d["title"],d["signature"],d["selectors"],d["depends_on_days"],d["recovery_day"]))
            self.assertEqual((d["date"],d["title"],d["learner_seam"],d["module"],d["signature"],d["selectors"],d["study_refs"],d["study_guidance"],d["depends_on_days"],d["recovery_day"]),
                             (owner["date"],owner["title"],owner["learner_seam"],owner["module"],owner["signature"],owner["selectors"],owner["study_refs"],owner["study_guidance"],owner["prerequisite_days"],owner["recovery_day"]))
            self.assertEqual(165,owner["engineering"]["minutes"])
            self.assertEqual(165,sum(owner["engineering"]["allocation"].values()))
            self.assertEqual(45,owner["interview_or_remaining_reviews"]["budget_minutes"])
            self.assertEqual({"one_timeboxed_substantive_exercise":30,"recall_or_explain_back":10,"evidence_and_next_due":5},owner["interview_or_remaining_reviews"]["allocation_minutes"])
            self.assertEqual(45,sum(owner["interview_or_remaining_reviews"]["allocation_minutes"].values()))
            self.assertEqual(210,owner["budget_minutes"])
            self.assertFalse(owner["interview_or_remaining_reviews"]["new_guided_anchors_allowed"])
            self.assertIn("Failed repair, oldest overdue",owner["interview_or_remaining_reviews"]["priority"])
            self.assertIn("actual first attempt",owner["interview_or_remaining_reviews"]["actual_attempt_clock"])
            self.assertIn(f"--roadmap-day {d['day']}",owner["interview_or_remaining_reviews"]["queue_command"])
            self.assertTrue(owner["study_guidance"])
            for item in d["study_guidance"]:
                self.assertEqual((refmap[item["key"]]["title"],refmap[item["key"]]["url"],refmap[item["key"]]["sections"]),(item["title"],item["url"],item["sections"]))
    def test_each_exported_selector_resolves_to_nonempty_test(self):
        loader=unittest.defaultTestLoader
        for day in self.manifest["days"]:
            for command in day["selectors"]:
                words=shlex.split(command); self.assertEqual(["python3","-m","unittest"],words[:3])
                suite=unittest.TestSuite(loader.loadTestsFromName(name) for name in words[3:])
                leaves=[]
                def visit(x):
                    if isinstance(x,unittest.TestSuite):
                        for y in x: visit(y)
                    else: leaves.append(x)
                visit(suite)
                self.assertTrue(leaves)
                self.assertFalse(any(isinstance(x,unittest.loader._FailedTest) for x in leaves),command)
                self.assertGreater(sum(x.countTestCases() for x in leaves),0,command)
    def test_contract_exports_exist_with_human_spacing(self):
        for d in self.manifest["days"]:
            p=BATCH/"contracts"/f"day-{d['day']}.md"
            text=p.read_text(encoding="utf-8")
            self.assertIn(f"# Day {d['day']} · {d['date']}",text)
            self.assertIn("Input → steps → expected output",text)
            self.assertIn("165 minutes engineering",text)
            self.assertNotIn("165minutes",text)
            self.assertNotIn(f"{d['day']}, {d['day']}",text)
