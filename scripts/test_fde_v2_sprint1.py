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

    def test_accepted_schedule_is_preserved_and_beginner_overlay_sets_active_first_15_budget(self):
        schedule = json.loads((ACCEPTED / "first-30-days.json").read_text(encoding="utf-8"))["schedule"]
        self.assertEqual(len(schedule), 30)
        # This accepted input remains historical and byte-preserved; the active overlay is explicit.
        for day in schedule[:14]:
            self.assertEqual(day["dsa_minutes"], 60)
            self.assertEqual(day["role_interview_minutes"], 30)
            self.assertEqual(day["ai_total_minutes"], 90)
            self.assertEqual(sum(item["minutes"] for item in day["ai_segments"])
                             + day["visibility_minutes"], 90)
        overlay = json.loads((DOCS / "amendments/2026-10-30-beginner-first-15.json").read_text())
        self.assertEqual(len(overlay["days"]), 15)
        for day in overlay["days"]:
            number = day["day"]
            page = (DOCS / "days" / f"day-{number:02d}.md").read_text(encoding="utf-8")
            with self.subTest(day=number):
                self.assertEqual(day["ai_minutes"]["lesson"] + day["ai_minutes"]["worked_example"]
                                 + day["ai_minutes"]["practice"] + day["ai_minutes"]["check"], 90)
                self.assertIn("Status: Not started", page)
                self.assertIn("180 minutes", page)
                self.assertIn(day["task"]["function"], page)
                self.assertIn(day["task"]["command"], page)
                self.assertIn("30 min AWS", page)
                self.assertIn("60 min DSA", page)

    def test_historical_u011_schedule_is_preserved_but_beginner_days_do_not_claim_it(self):
        unit = (DOCS / "units/u011.md").read_text(encoding="utf-8")
        day14 = (DOCS / "days/day-14.md").read_text(encoding="utf-8")
        day15 = (DOCS / "days/day-15.md").read_text(encoding="utf-8")
        handoff = (DOCS / "amendments/day16-prerequisite-handoff.md").read_text(encoding="utf-8")
        self.assertIn("Day 14: primary 30 min", unit)
        self.assertIn("Day 15: primary 60 min", unit)
        self.assertIn("does not implement or complete U011", day14)
        self.assertIn("does **not** satisfy the accepted U011 contract", day15)
        self.assertIn("does not complete U011", handoff)
        self.assertNotIn("upsert_document_metadata", day14)
        self.assertNotIn("ingest_versioned_document", day15)
        self.assertIn("changed record must advance exactly one version", unit)
        self.assertIn("same-version changed text", unit)
        self.assertTrue((DOCS / "days/day-15.md").is_file())
        self.assertTrue((DOCS / "units/u012.md").is_file())

    def test_u009_documents_browser_cancellation_without_claiming_server_abort(self):
        unit = (DOCS / "units/u009.md").read_text(encoding="utf-8")
        self.assertIn("explicit cancellation", unit)
        self.assertIn("does not stop generation already running on the server", unit)
        self.assertIn("real-backend gate remains required", unit)
        self.assertIn("currently **BLOCKED**", unit)
        self.assertIn("do not waive it", unit)

    def test_day03_prose_matches_scheduled_unit_minutes(self):
        day = (DOCS / "days/day-03.md").read_text(encoding="utf-8")
        self.assertIn("Function:** `question_words`", day)
        self.assertIn("python3 -m ai.fde_v2.beginner_first15.acceptance --day 3", day)
        self.assertIn("split a normalized question", (ROOT / "ai/fde_v2/beginner_first15/learner.py").read_text())

    def test_shared_dsa_guidance_uses_explicit_roadmap_position_and_actual_date(self):
        guide = (DOCS / "README.md").read_text(encoding="utf-8")
        self.assertIn("python3 tools/dsa_operations.py plan --day N", guide)
        self.assertIn("python3 tools/dsa_operations.py queue --on-date ACTUAL_LOCAL_DATE --roadmap-day N", guide)
        self.assertIn("Always pass `--roadmap-day N`", guide)
        self.assertIn("The CLI does not persist a position cursor", guide)
        self.assertIn("Record attempts only on the dates they actually happen", guide)
        self.assertIn("never backdate attempts or clear review history", guide)
        self.assertIn("Once per seven actual DSA study sessions", guide)
        self.assertIn("inside the same 60-minute DSA block", guide)
        self.assertIn("Keep scheduled DSA review anchors first", guide)
        self.assertIn("[Prachub](https://prachub.com/)", guide)
        self.assertIn("https://prachub.com/companies/openai", guide)
        self.assertIn("https://prachub.com/coding-questions/implement-a-crash-resilient-lru-cache", guide)
        self.assertIn("community-reported", guide)
        self.assertIn("not a guarantee that a question appeared or will appear", guide)
        self.assertIn("if a question is locked or does not match", guide)
        self.assertIn("do not copy paid problem text or solutions", guide)

    def test_course_map_preserves_history_but_links_current_runtime_and_behavior(self):
        guide = (DOCS / "sources/sprint1-course-map.md").read_text(encoding="utf-8")
        self.assertIn("Historical provenance: the initial implementation snapshot was commit **6b291d9", guide)
        self.assertIn("[Current loopback API](../../../ai/fde_v2/coverstory/api.py)", guide)
        self.assertIn("[Current React question-answer screen](../../../frontend/src/fde-v2/QuestionAnswerApp.tsx)", guide)
        self.assertIn("concurrent overlap 429", guide)
        self.assertIn("explicit Cancel", guide)
        self.assertIn("Weights remain absent and real inference blocked/unverified", guide)
        course_map = json.loads((DOCS / "sources/sprint1-course-map.json").read_text(encoding="utf-8"))
        runtime = course_map["runtime_alignment"]
        self.assertEqual(runtime["commit"], "6b291d95567de5c9f6943931f004e74c5c9d4d3a")
        self.assertEqual(runtime["http_contract"]["known_error_statuses"], [422, 429, 502, 503, 504])
        self.assertIn("generation_slot_busy", runtime["current_http_behavior"])
        self.assertIn("Explicit Cancel", runtime["ui_cancellation"])
        for source_id in ("CODE-RUNTIME", "CODE-MODELS", "CODE-LOCK", "CODE-API", "CODE-UI", "CODE-SCOPE"):
            source = course_map["sources"][source_id]
            with self.subTest(source=source_id):
                current = (DOCS / "sources" / source["current_path"]).resolve()
                self.assertTrue(current.is_file(), f"missing current source: {source['current_path']}")
                self.assertIn("blob/6b291d9", source["historical_url"])

    def test_sprint1_seams_remain_first_and_continuation_seams_are_explicit(self):
        tree = ast.parse((ROOT / "ai/fde_v2/coverstory/learner.py").read_text(encoding="utf-8"))
        names = [node.name for node in tree.body if isinstance(node, ast.FunctionDef)]
        self.assertEqual(names[:4], ["parse_document_record", "generate_text", "parse_answer_json",
                                     "upsert_document_metadata"])
        self.assertEqual(names[4:17], ["ingest_versioned_document", "chunk_document", "embed_texts",
                                      "keyword_search", "index_vectors", "retrieve_vectors",
                                      "evaluate_retrieval", "compose_chat_messages", "validate_citations",
                                      "answer_with_evidence", "should_abstain", "classify_failure",
                                      "build_eval_record"])
        self.assertEqual(names[17:], ["validate_fixture_path"])
        self.assertNotIn("qdrant-client", (ROOT / "requirements-fde-v2-sprint1.txt").read_text())
        self.assertIn("qdrant-client==1.20.0", (ROOT / "requirements-fde-v2-sprint2.txt").read_text())
        self.assertTrue((ROOT / "ai/fde_v2/coverstory/vector_store.py").is_file())
        self.assertTrue((ROOT / "ai/fde_v2/coverstory/pipeline.py").is_file())

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
        self.assertIn("Day 14 partial seed accepts one row", help_result.stdout)
        with tempfile.TemporaryDirectory() as data_dir:
            run_result = subprocess.run(
                [sys.executable, "scripts/fde_v2_seed_corpus.py"], cwd=ROOT,
                env={**os.environ, "FDE_V2_DATA_DIR": data_dir},
                text=True, capture_output=True, check=False, timeout=10,
            )
        self.assertNotEqual(run_result.returncode, 0, "the unimplemented learner seam must stay red")
        self.assertIn("NotImplementedError: Day 12: implement document_record_valid",
                      run_result.stderr)
        self.assertNotIn("ModuleNotFoundError", run_result.stderr)

    def test_relative_markdown_links_in_active_sprint_docs_resolve(self):
        paths = [DOCS / "README.md", DOCS / "implementation-scope.md", DOCS / "phase-map.md",
                 DOCS / "aws-bank.md", DOCS / "role-bank.md", DOCS / "role-benchmarks.md",
                 DOCS / "sources/sprint1-course-map.md",
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
