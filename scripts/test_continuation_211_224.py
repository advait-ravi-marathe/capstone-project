import json, subprocess, sys, unittest
import re
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/"docs/reboot/continuation-211-224"
class GeneratedContinuationTests(unittest.TestCase):
    def test_generator_is_reproducible_and_allocations_match_proposal(self):
        before={p.name:p.read_bytes() for p in (OUT/"manifest.json",OUT/"days-211-224-transfer-plan.json",OUT/"scrum-owner-manifest.json")}
        subprocess.run([sys.executable,"scripts/generate_continuation_211_224.py"],cwd=ROOT,check=True,capture_output=True)
        after={p.name:p.read_bytes() for p in (OUT/"manifest.json",OUT/"days-211-224-transfer-plan.json",OUT/"scrum-owner-manifest.json")}
        self.assertEqual(before,after)
        m=json.loads(after["manifest.json"]); t=json.loads(after["days-211-224-transfer-plan.json"]); o=json.loads(after["scrum-owner-manifest.json"])
        self.assertEqual(14,len(m["days"])); self.assertEqual(14,o["story_count"]); self.assertEqual(14,len(t["days"]))
        expected={"project_implementation_tests":1320,"ai_workflow_implementation_evaluation":240,"certification_labs_questions":50,"ai_concept_source_study":120,"certification_domain_study":90,"core_systems_security_cloud_study":280,"evidence":210}
        self.assertEqual(expected,m["engineering_allocation"])
        self.assertEqual(expected,t["budget"]["window_workstreams"])
        self.assertEqual(expected,{k:sum(c["engineering"]["allocation"][k] for c in o["cards"]) for k in expected})
        
        for day,card in zip(m["days"],o["cards"]):
            self.assertEqual(day["workstream_minutes"],card["engineering"]["allocation"])
            self.assertEqual(165,sum(day["workstream_minutes"].values()))
            self.assertEqual(45,day["shared_interview_block"]["minutes"])
            self.assertEqual(day["date"],card["date"])
            self.assertTrue(day["study_guidance"])
            self.assertTrue(day["engineering_architecture"]["decision"] in Path(OUT/"assistant-architecture-notes.md").read_text())
            contract=(OUT/f"contracts/day-{day['day']}.md").read_text()
            self.assertIn("Engineering budget (165m)",contract)
            self.assertIn("Study references:",contract)
            self.assertIn("Shared 45-minute block:",contract)
            for src in day["study_guidance"]:
                self.assertIn(src["title"],contract)
                for section in src["sections"]: self.assertIn(section,contract)
            shared=day["shared_interview_block"]
            if shared["question_title"]: self.assertIn(shared["question_title"],contract)
            else:
                for name in ("requirements 5m","constraints 5m","failures 10m","trade-offs and next step 10m"): self.assertIn(name,contract)
    def test_all_day_anchors_and_local_links_resolve(self):
        notes=(OUT/"assistant-architecture-notes.md").read_text()
        for n in range(211,225):
            self.assertIn(f'<a id="day-{n}"></a>',notes)
            contract=OUT/f"contracts/day-{n}.md"
            self.assertTrue(contract.exists())
            self.assertIn(f'id="day-{n}"',contract.read_text())
            for target in re.findall(r'\]\(([^)]+)\)',contract.read_text()):
                if target.startswith(("http://","https://")): continue
                path,_,frag=target.partition("#"); resolved=(contract.parent/path).resolve()
                self.assertTrue(resolved.exists(),f"broken local link {target} in {contract}")
                if frag: self.assertIn(f'id="{frag}"',resolved.read_text())
    def test_gate_fixture_controls_validate_positive_negative_and_live_consistency(self):
        import importlib.util
        spec=importlib.util.spec_from_file_location("gate",ROOT/"scripts/check_continuation_211_entry_gate.py")
        gate=importlib.util.module_from_spec(spec);sys.modules["gate"]=gate;spec.loader.exec_module(gate)
        valid={"day":210,"learner_owned":True,"status":"pass","synthetic_only":True,"owner_boundary_verified":True,"source_version_verified":True,"citation_boundary_verified":True,"source_sha":"a"*40,"artifact_sha256":"b"*64}
        self.assertEqual("eligible_to_attempt",gate.evaluate_record(json.dumps(valid).encode())["status"])
        for key,value in (("learner_owned",False),("synthetic_only",False),("owner_boundary_verified",False),("source_version_verified",False),("citation_boundary_verified",False),("source_sha","a"*64),("artifact_sha256","b"*40),("status","not_attempted")):
            bad=dict(valid);bad[key]=value
            self.assertEqual("blocked",gate.evaluate_record(json.dumps(bad).encode())["status"],key)
        p=subprocess.run([sys.executable,"scripts/check_continuation_211_entry_gate.py"],cwd=ROOT,text=True,capture_output=True,check=True)
        live=json.loads(p.stdout);self.assertEqual(gate.check()["status"],live["status"]);self.assertFalse(live["completion_awarded"])

if __name__=="__main__":unittest.main()
