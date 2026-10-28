from __future__ import annotations

import ast
import hashlib
import json
import os
import re
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / "docs/fde-v2"
ACCEPTED = DOCS / "accepted"
EXPECTED_RECEIPT_SHA256 = "c0244d623a18c7fae6593f12895ebd3790099078f839d7831cf75c76c23746b5"


class Sprint1ContractTests(unittest.TestCase):
    def test_accepted_receipt_and_all_eight_source_hashes(self):
        receipt_path = ACCEPTED / "plan-review-receipt.json"
        self.assertEqual(hashlib.sha256(receipt_path.read_bytes()).hexdigest(), EXPECTED_RECEIPT_SHA256)
        receipt = json.loads(receipt_path.read_text(encoding="utf-8"))
        self.assertEqual(len(receipt["files"]), 8)
        for name, expected in receipt["files"].items():
            with self.subTest(source=name):
                self.assertEqual(hashlib.sha256((ACCEPTED / name).read_bytes()).hexdigest(), expected)

    def test_exact_first_14_schedule_segments_and_budget(self):
        schedule = json.loads((ACCEPTED / "first-30-days.json").read_text(encoding="utf-8"))["schedule"]
        self.assertEqual(len(schedule), 30)
        for day in schedule[:14]:
            number = day["planned_learning_day"]
            page = (DOCS / "days" / f"day-{number:02d}.md").read_text(encoding="utf-8")
            with self.subTest(day=number):
                self.assertEqual(day["dsa_minutes"], 60)
                self.assertEqual(day["role_interview_minutes"], 30)
                self.assertEqual(day["ai_total_minutes"], 90)
                self.assertEqual(sum(item["minutes"] for item in day["ai_segments"])
                                 + day["visibility_minutes"], 90)
                self.assertIn("Status: Not started", page)
                self.assertIn("180 minutes", page)
                for segment in day["ai_segments"]:
                    contract_id = segment["unit_id"].rsplit("-", 1)[1].lower()
                    self.assertTrue((DOCS / "units" / f"{contract_id}.md").is_file())
                    self.assertIn(f"../units/{contract_id}.md", page)

    def test_u011_is_only_an_explicit_partial_day14_slice(self):
        unit = (DOCS / "units/u011.md").read_text(encoding="utf-8")
        day14 = (DOCS / "days/day-14.md").read_text(encoding="utf-8")
        self.assertIn("first 30 minutes", unit)
        self.assertIn("does not claim U011 completion", unit)
        self.assertIn("does not exercise changed-version handling", unit)
        self.assertIn("not required to complete the Day 14 slice", unit)
        self.assertIn("30 minutes only", day14)
        self.assertIn("remaining segments continue", day14)
        self.assertFalse((DOCS / "days/day-15.md").exists())
        self.assertFalse((DOCS / "units/u012.md").exists())

    def test_u009_documents_browser_cancellation_without_claiming_server_abort(self):
        unit = (DOCS / "units/u009.md").read_text(encoding="utf-8")
        self.assertIn("explicit cancellation", unit)
        self.assertIn("does not stop generation already running on the server", unit)

    def test_only_sprint1_learner_seams_are_active(self):
        tree = ast.parse((ROOT / "ai/fde_v2/coverstory/learner.py").read_text(encoding="utf-8"))
        names = [node.name for node in tree.body if isinstance(node, ast.FunctionDef)]
        self.assertEqual(names, ["parse_document_record", "generate_text", "parse_answer_json",
                                 "upsert_document_metadata"])
        self.assertNotIn("qdrant-client", (ROOT / "requirements-fde-v2-sprint1.txt").read_text())
        self.assertFalse((ROOT / "ai/fde_v2/coverstory/vector_store.py").exists())
        self.assertFalse((ROOT / "ai/fde_v2/coverstory/pipeline.py").exists())

    def test_role_aws_source_mapping_is_specific_and_honest(self):
        aws = (DOCS / "aws-bank.md").read_text(encoding="utf-8")
        self.assertIn("course ID **2196488**", aws)
        self.assertIn("Personal paid-video entitlement remains unverified", aws)
        self.assertIn("course title, ID and mapped curriculum durations are recorded", aws)
        self.assertIn("Task3.4** (Determine high-performing and/or scalable network architectures)", aws)
        self.assertIn("Task3.5 concerns data ingestion and transformation", aws)
        self.assertNotIn("Tasks3.4 and3.5** (cost-optimized and high-performing network architectures)", aws)
        self.assertIn("Tasks 1.1, 1.2 and 1.3", aws)
        self.assertIn("Tasks 3.1–3.5", aws)
        self.assertIn("Tasks 4.1–4.4", aws)
        for day in range(1, 15):
            self.assertTrue((DOCS / "days" / f"day-{day:02d}.md").is_file())

    def test_day14_seed_cli_imports_from_repository_root_without_completing_learner_work(self):
        help_result = subprocess.run(
            [sys.executable, "scripts/fde_v2_seed_corpus.py", "--help"], cwd=ROOT,
            text=True, capture_output=True, check=False, timeout=10,
        )
        self.assertEqual(help_result.returncode, 0, help_result.stderr)
        self.assertIn("Day 14 accepts only the first synthetic record", help_result.stdout)
        with tempfile.TemporaryDirectory() as data_dir:
            run_result = subprocess.run(
                [sys.executable, "scripts/fde_v2_seed_corpus.py"], cwd=ROOT,
                env={**os.environ, "FDE_V2_DATA_DIR": data_dir},
                text=True, capture_output=True, check=False, timeout=10,
            )
        self.assertNotEqual(run_result.returncode, 0, "the unimplemented learner seam must stay red")
        self.assertIn("NotImplementedError: Day 3: implement parse_document_record", run_result.stderr)
        self.assertNotIn("ModuleNotFoundError", run_result.stderr)

    def test_relative_markdown_links_in_active_sprint_docs_resolve(self):
        paths = [DOCS / "README.md", DOCS / "implementation-scope.md", DOCS / "phase-map.md",
                 DOCS / "aws-bank.md", DOCS / "role-bank.md", DOCS / "role-benchmarks.md",
                 *sorted((DOCS / "days").glob("*.md")), *sorted((DOCS / "units").glob("*.md"))]
        link_pattern = re.compile(r"\[[^\]]+\]\(([^)]+)\)")
        for path in paths:
            text = path.read_text(encoding="utf-8")
            for target in link_pattern.findall(text):
                if target.startswith(("http://", "https://", "mailto:")):
                    continue
                relative, _, anchor = target.partition("#")
                dest = (path.parent / relative).resolve() if relative else path.resolve()
                with self.subTest(file=str(path.relative_to(ROOT)), link=target):
                    self.assertTrue(dest.exists(), f"broken relative link: {target}")
                    if anchor and dest.is_file() and dest.suffix == ".md":
                        headings = [line for line in dest.read_text(encoding="utf-8").splitlines()
                                    if line.startswith("#")]
                        slugs = {re.sub(r"[^a-z0-9 -]", "", line.lstrip("#").strip().lower())
                                 .replace(" ", "-") for line in headings}
                        self.assertIn(anchor, slugs, f"missing markdown anchor: {target}")


if __name__ == "__main__":
    unittest.main()
