import json, shlex, unittest
from datetime import date, timedelta
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
BATCH=ROOT/"docs/reboot/continuation-183-196"

class StreamForge183196ManifestTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.manifest=json.loads((BATCH/"manifest.json").read_text(encoding="utf-8"))
    def test_batch_dates_budget_and_daily_chronology(self):
        m=self.manifest; self.assertEqual("learning/streamforge-days-183-196",m["branch"])
        self.assertEqual("fa45d70868e93da79bb2ff84a8c4a4ca77510924",m["base_commit"])
        self.assertEqual(list(range(183,197)),[d["day"] for d in m["days"]])
        self.assertEqual(date(2027,4,20),date.fromisoformat(m["days"][0]["date"]))
        self.assertEqual(date(2027,5,3),date.fromisoformat(m["days"][-1]["date"]))
        expected_dates=["2027-04-20","2027-04-21","2027-04-22","2027-04-23","2027-04-24","2027-04-25","2027-04-26","2027-04-27","2027-04-28","2027-04-29","2027-04-30","2027-05-01","2027-05-02","2027-05-03"]
        self.assertEqual(expected_dates,[d["date"] for d in m["days"]])
        self.assertIsNone(m["days"][6]["module"])
        self.assertIsNone(m["days"][12]["module"])
        self.assertIsNone(m["days"][13]["module"])
        self.assertEqual([189,196],[d["day"] for d in m["days"] if d["recovery_day"]])
        for d in m["days"]:
            b=d["budget"]; self.assertEqual(165,b["engineering_minutes"])
            self.assertEqual(35+115+15,b["engineering_minutes"])
            self.assertEqual(210,b["total"])
            self.assertFalse(set(d["depends_on_days"]) & set(range(d["day"]+1,197)))
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
