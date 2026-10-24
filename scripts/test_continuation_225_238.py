"""Instructor checks for synchronized Days 225–238 roadmap artifacts."""
from __future__ import annotations

import importlib.util
import json
import re
import subprocess
import sys
import unittest
from pathlib import Path
from types import SimpleNamespace

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs/reboot/continuation-225-238"
GENERATOR = ROOT / "scripts/generate_continuation_225_238.py"
EXPECTED = {
    "project_implementation_tests": 1140,
    "ai_workflow_implementation_evaluation": 360,
    "certification_labs_questions": 110,
    "ai_concept_source_study": 150,
    "certification_domain_study": 120,
    "core_systems_security_cloud_study": 220,
    "evidence": 210,
}


def load_module(name, path):
    spec = importlib.util.spec_from_file_location(name, path)
    module = importlib.util.module_from_spec(spec)
    sys.modules[name] = module
    spec.loader.exec_module(module)
    return module


class Continuation225ManifestTests(unittest.TestCase):
    def test_generator_is_reproducible_and_all_exports_match_source(self):
        names = ["manifest.json", "days-225-238-transfer-plan.json", "scrum-owner-manifest.json", "verified-sources.json"]
        before = {name: (OUT / name).read_bytes() for name in names}
        subprocess.run([sys.executable, str(GENERATOR)], cwd=ROOT, check=True, capture_output=True)
        after = {name: (OUT / name).read_bytes() for name in names}
        self.assertEqual(before, after)
        source = json.loads((OUT / "plan-source.json").read_text())
        manifest = json.loads(after["manifest.json"])
        transfer = json.loads(after["days-225-238-transfer-plan.json"])
        owner = json.loads(after["scrum-owner-manifest.json"])
        self.assertEqual(list(range(225, 239)), [d["day"] for d in manifest["days"]])
        self.assertEqual(14, owner["story_count"])
        self.assertEqual(14, len(transfer["days"]))
        self.assertEqual(EXPECTED, manifest["engineering_allocation"])
        self.assertEqual(EXPECTED, transfer["budget"]["window_workstreams"])
        self.assertEqual(EXPECTED, {k: sum(c["engineering"]["allocation"][k] for c in owner["cards"]) for k in EXPECTED})
        self.assertEqual(source["gate"], manifest["gate_policy"])
        self.assertEqual(source["base_commit"], owner["base_commit"])
        self.assertFalse(owner["board_writes"])
        for i, (day, card, task) in enumerate(zip(manifest["days"], owner["cards"], source["tasks"])):
            self.assertEqual(task["day"], day["day"])
            self.assertEqual(day["workstream_minutes"], card["engineering"]["allocation"])
            self.assertEqual(165, sum(day["workstream_minutes"].values()))
            self.assertEqual(45, day["shared_interview_block"]["minutes"])
            self.assertEqual(day["date"], card["date"])
            self.assertEqual(f"2027-06-{5+i:02d}", day["date"])
            self.assertEqual(task["selector"], day["selectors"][1])
            self.assertTrue(day["task_minutes"]["project_implementation_tests"])
            self.assertTrue(day["task_minutes"]["ai_workflow_implementation_evaluation"])
            for category in ("ai_concept_source_study", "certification_domain_study", "core_systems_security_cloud_study", "evidence"):
                self.assertTrue(day["task_minutes"][category], (day["day"], category))
            self.assertEqual("unobserved_not_implied_by_scaffold", day["learner_status"])
            self.assertFalse(day["engineering_architecture"]["capstone_proposal_required"])
            self.assertEqual("separate_interview_question_bank_only", day["engineering_architecture"]["system_design_scope"])
            self.assertEqual(day["study_refs"], [s["url"] for s in day["study_guidance"]])
        self.assertEqual(4, sum(d["shared_interview_block"]["mode"] == "system_design_question_session" for d in manifest["days"]))
        self.assertEqual(4, sum(d["shared_interview_block"]["mode"] == "certification_retrieval_review" for d in manifest["days"]))
        self.assertEqual(6, sum(d["shared_interview_block"]["mode"] == "unfinished_review" for d in manifest["days"]))

    def test_day_anchors_contract_links_exact_sources_and_no_day239(self):
        notes = (OUT / "assistant-architecture-notes.md").read_text()
        readme = (OUT / "README.md").read_text()
        self.assertIn("entry-gate-and-recovery", readme)
        self.assertNotIn("Day 239", notes)
        source = json.loads((OUT / "plan-source.json").read_text())
        verified = json.loads((OUT / "verified-sources.json").read_text())
        verified_by_key = {s["key"]: s for s in verified["sources"]}
        self.assertEqual(source["sources"], verified["sources"])
        for n in range(225, 239):
            contract_path = OUT / f"contracts/day-{n}.md"
            contract = contract_path.read_text()
            self.assertIn(f'<a id="day-{n}"></a>', notes)
            self.assertIn(f'<a id="day-{n}"></a>', contract)
            self.assertIn("Engineering budget (165m)", contract)
            self.assertIn("Study references:", contract)
            self.assertIn("Shared 45-minute block:", contract)
            for target in re.findall(r"\]\(([^)]+)\)", contract):
                if target.startswith(("http://", "https://")):
                    continue
                path, _, fragment = target.partition("#")
                resolved = (contract_path.parent / path).resolve()
                self.assertTrue(resolved.exists(), f"broken link {target} in Day {n}")
                if fragment:
                    self.assertIn(f'id="{fragment}"', resolved.read_text())
        for day in json.loads((OUT / "manifest.json").read_text())["days"]:
            task = day["task"]
            for ref in day["study_guidance"]:
                canonical = verified_by_key[ref["key"]]
                self.assertEqual(canonical["title"], ref["title"])
                self.assertEqual(canonical["url"], ref["url"])
                self.assertTrue(set(ref["sections"]).issubset(set(canonical["sections"])))
                contract = (OUT / f"contracts/day-{day['day']}.md").read_text()
                self.assertIn(ref["title"], contract)
                for section in ref["sections"]:
                    self.assertIn(section, contract)
            self.assertIn("python3 scripts/check_continuation_211_entry_gate.py", day["selectors"])
            self.assertEqual("SAA_GUIDE" if task["cert_source_section"].startswith("Content Domain") else "DEV_ASSOCIATE", task["cert_source_key"])

    def test_review_assertion_path_rejects_mutated_results(self):
        acceptance = load_module("continuation_225_acceptance_for_oracle", ROOT / "ai/grounded/learner_acceptance/test_continuation_225.py")
        event_type = load_module("continuation_225_for_oracle", ROOT / "ai/grounded/continuation_225.py").AuditEvent
        good_event = event_type(1, "draft-225-001", "owner-a", "reviewer-17", "approve", "DRAFT", "APPROVED", "policy-v7", "a" * 64, "review-req-225-001")
        good = SimpleNamespace(status="approved", ledger=SimpleNamespace(drafts=(SimpleNamespace(status="APPROVED"),), audit=(good_event,)), event=good_event)
        acceptance._assert_success_contract(self, good)
        mutants = (
            SimpleNamespace(status="approved", ledger=SimpleNamespace(drafts=(SimpleNamespace(status="APPROVED"),), audit=()), event=None),
            SimpleNamespace(status="approved", ledger=SimpleNamespace(drafts=(SimpleNamespace(status="APPROVED"),), audit=(good_event, good_event)), event=good_event),
            SimpleNamespace(status="approved", ledger=SimpleNamespace(drafts=(SimpleNamespace(status="APPROVED"),), audit=(good_event,)), event=event_type(1, "draft-225-001", "owner-b", "reviewer-17", "approve", "DRAFT", "APPROVED", "policy-v7", "a" * 64, "review-req-225-001")),
            SimpleNamespace(status="replayed", ledger=good.ledger, event=good_event),
        )
        for mutant in mutants:
            with self.subTest(mutant=mutant):
                with self.assertRaises(AssertionError):
                    acceptance._assert_success_contract(self, mutant)


if __name__ == "__main__":
    unittest.main()
