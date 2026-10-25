import copy, json, re, shlex, unittest
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
        m=self.manifest; self.assertEqual("work/eai-sprint2-onward",m["branch"])
        self.assertEqual("41946e432b2a61ab4ba1c0fb938da07aff9e5ded",m["base_commit"])
        self.assertEqual(list(range(183,197)),[d["day"] for d in m["days"]])
        self.assertEqual(date(2027,5,17),date.fromisoformat(m["days"][0]["date"]))
        self.assertEqual(date(2027,5,30),date.fromisoformat(m["days"][-1]["date"]))
        expected_dates=["2027-05-17","2027-05-18","2027-05-19","2027-05-20","2027-05-21","2027-05-22","2027-05-23","2027-05-24","2027-05-25","2027-05-26","2027-05-27","2027-05-28","2027-05-29","2027-05-30"]
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
        self.assertEqual({"start":"2027-05-17","end":"2027-05-30","recovery_days":[189,196]},
                         {k:v for k,v in self.plan["dates"].items() if not k.startswith("historical_")})
        self.assertEqual("2027-05-17",m["start_date"])
        self.assertEqual("2027-05-30",m["end_date"])
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
            self.assertEqual(210,owner["budget_minutes"])
            self.assertTrue(owner["study_guidance"])
            self.assertEqual(d["engineering_architecture"], owner["engineering_architecture"])
            self.assertEqual(d["engineering_architecture"], source["engineering_architecture"])
            self.assertIn(f"#day-{d['day']}", d["engineering_architecture"]["note_ref"])
            self.assertFalse(d["engineering_architecture"]["capstone_proposal_required"])
            self.assertEqual(45, d["budget"]["shared_interview_or_remaining_reviews_minutes"])
            shared=d["shared_interview_block"]
            self.assertEqual(45,shared["minutes"])
            self.assertFalse(shared["within_engineering_minutes"])
            self.assertEqual(210,d["budget"]["engineering_minutes"]+shared["minutes"])
            self.assertEqual({"requirements":5,"capacity_and_constraints":5,
                              "initial_architecture_and_data_model":15,
                              "bottlenecks_reliability_scaling":10,
                              "tradeoffs_failures_followups":10}
                             if shared["mode"]=="system_design_question_session"
                             else {"question_or_review":30,"retrieval_or_explain_back":10,"evidence_and_next_due":5},
                             shared["allocation_minutes"])
            self.assertEqual(shared["minutes"],sum(shared["allocation_minutes"].values()))
            self.assertEqual("45_minute_mock" if d["day"] in (185,188,192,195) else "shared_question_or_review",
                             owner["interview_practice"]["mode"])
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
            self.assertIn(f"# Engineering unit Day {d['day']} · Calendar study day {d['day']+21} · {d['date']}",text)
            self.assertIn("Input → steps → expected output",text)
            self.assertIn("165 minutes engineering",text)
            self.assertNotIn("165minutes",text)
            self.assertNotIn(f"{d['day']}, {d['day']}",text)
            self.assertIn("Assistant-supplied architecture", text)
            self.assertIn("Prior A/B evidence gate", text)
            self.assertIn("python3 scripts/check_streamforge_169_182_entry_gate.py", text)
            self.assertIn("The learner implements and tests this supplied choice", text)
            self.assertNotIn("proposal-before-guidance", text.lower())
            self.assertIn("shared interview/review block", text.lower())

    def test_question_sessions_are_separate_and_have_real_bank_anchors(self):
        bank=(ROOT/"docs/reboot/interview/question-bank.md").read_text(encoding="utf-8")
        owner_days={card["day_number"]:card for card in self.owner["cards"]}
        mock_days=[]
        for day in self.manifest["days"]:
            shared=day["shared_interview_block"]
            if shared["mode"]=="system_design_question_session":
                mock_days.append(day["day"])
                question=shared["question_id"]
                self.assertRegex(bank,rf'<a id="{question.lower()}"></a>')
                self.assertIn(question,bank)
                self.assertFalse(shared["within_engineering_minutes"])
                self.assertEqual(210,day["budget"]["engineering_minutes"]+shared["minutes"])
                exported=owner_days[day["day"]]["interview_practice"]
                self.assertEqual(question,exported["question_id"])
                self.assertEqual(45,exported["duration_minutes"])
                self.assertEqual(shared["allocation_minutes"],exported["allocation_minutes"])
        self.assertEqual([185,188,192,195],mock_days)
        schedule=(BATCH/"interview-and-review-schedule.md").read_text(encoding="utf-8")
        for day in mock_days:
            question=next(item["shared_interview_block"]["question_id"] for item in self.manifest["days"] if item["day"]==day)
            self.assertIn(f"[{question}",schedule)

    def test_assistant_architecture_notes_match_all_exports(self):
        notes=(BATCH/"assistant-architecture-notes.md").read_text(encoding="utf-8")
        plan_days={day["day"]:day for day in self.plan["days"]}
        owner_days={day["day_number"]:day for day in self.owner["cards"]}
        for day in self.manifest["days"]:
            number=day["day"]
            decision=day["engineering_architecture"]["decision"]
            self.assertIn(f'<a id="day-{number}"></a>',notes)
            self.assertIn(decision,notes)
            self.assertEqual(decision,plan_days[number]["engineering_architecture"]["decision"])
            self.assertEqual(decision,owner_days[number]["engineering_architecture"]["decision"])

    def test_batch_markdown_local_links_resolve(self):
        files=list(BATCH.rglob("*.md"))
        for source in files:
            text=source.read_text(encoding="utf-8")
            for target in re.findall(r"\[[^\]]*\]\(([^)]+)\)", text):
                if "://" in target or target.startswith("#"):
                    continue
                path=target.split("#",1)[0]
                if not path:
                    continue
                with self.subTest(source=str(source.relative_to(ROOT)),target=target):
                    self.assertTrue((source.parent/path).resolve().is_file())

    def test_ab_gate_is_dynamic_and_shared_across_exports(self):
        from scripts.check_streamforge_169_182_entry_gate import evaluate_records, read_actual_records
        gate = self.manifest["prior_readiness_gate"]
        self.assertEqual(gate, self.owner["prior_readiness_gate"])
        self.assertEqual(gate, self.plan["prior_readiness_gate"])
        self.assertEqual(gate, self.guidance["prior_readiness_gate"])
        records = read_actual_records(ROOT)
        self.assertEqual(["evidence/continuation-141-154/day-153.json",
                          "evidence/continuation-141-154/day-154.json"],
                         [row["path"] for row in gate["evidence_records"]])
        self.assertEqual(gate["current_status"], evaluate_records(records)["gate"])
        valid = {
            "status":"completed", "evidence_status":"verified", "learner_completion":True,
            "temporary_witness":False, "tested_source_sha":"a"*40,
            "artifact_hashes":[{"role":"baseline","sha256":"1"*64},
                               {"role":"candidate","sha256":"2"*64}],
            "matrix_selector":"observed matrix selector", "matrix_exit_code":0,
            "matrix_result":"passed",
            "source_provenance":{"baseline":{"source":"sha","build_recipe":"build"},
                                 "candidate":{"source":"sha","build_recipe":"build"}},
            "proof_limits":"Synthetic local result only.",
        }
        self.assertEqual("met", evaluate_records([copy.deepcopy(valid), copy.deepcopy(valid)])["gate"])
        witness=copy.deepcopy(valid); witness["temporary_witness"]=True
        self.assertEqual("not_met", evaluate_records([witness, copy.deepcopy(valid)])["gate"])
