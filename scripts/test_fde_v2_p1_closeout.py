"""Contract and failure-path checks for P1 closeout Days31–41."""
from __future__ import annotations

import hashlib
import json
import re
import contextlib
import io
import sys
import tempfile
import unittest
from datetime import date, timedelta
from pathlib import Path
from unittest.mock import patch

from fastapi.testclient import TestClient

from ai.fde_v2.coverstory.api import create_app
from ai.fde_v2.coverstory.contracts import Answer
from ai.fde_v2.coverstory.models import MODEL_ID, MODEL_REVISION
from ai.fde_v2.coverstory.p1_closeout import (
    load_heldout_cases, require_loopback_url, sha256_file, validate_benchmark_record,
    validate_heldout_review, validate_heldout_run,
)
from ai.fde_v2.coverstory.adversarial_fixtures import validate_day31_observation_records
from ai.fde_v2.coverstory.contracts import RuntimeUnavailable

ROOT = Path(__file__).resolve().parents[1]


class CloseoutContractTests(unittest.TestCase):
    def test_container_health_probe_retries_startup_connection_reset(self):
        from scripts.fde_v2_container_smoke import wait_for_health

        class Response:
            def __enter__(self):
                return self

            def __exit__(self, *_args):
                return False

            def read(self):
                return b'{"status":"ok","mode":"local-learning-prototype"}'

        attempts = []
        pauses = []

        def opener(_url, timeout):
            self.assertEqual(timeout, 2)
            attempts.append(1)
            if len(attempts) == 1:
                raise ConnectionResetError("container listener is still starting")
            return Response()

        result = wait_for_health("http://127.0.0.1:12345", opener=opener, pause=pauses.append)
        self.assertEqual(result, {"status": "ok", "mode": "local-learning-prototype"})
        self.assertEqual(len(attempts), 2)
        self.assertEqual(pauses, [1])

    def test_days31_to41_match_accepted_segments_dates_and_fixed_budget(self):
        accepted = json.loads((ROOT / "docs/fde-v2/accepted/first-release-through-day41-schedule.json").read_text())
        units = {row["id"]: row for row in json.loads(
            (ROOT / "docs/fde-v2/accepted/first-30-ai-units.json").read_text())["units"]}
        for day in range(31, 42):
            row = accepted["schedule"][day - 1]
            self.assertEqual(row["planned_learning_day"], day)
            self.assertEqual((row["dsa_minutes"], row["role_interview_minutes"], row["ai_total_minutes"]),
                             (60, 30, 90))
            self.assertEqual(sum(x["minutes"] for x in row["ai_segments"])
                             + row.get("visibility_minutes", 0), row["ai_total_minutes"])
            self.assertLessEqual(row["visibility_minutes"], row["ai_total_minutes"])
            page = (ROOT / f"docs/fde-v2/days/day-{day:02}.md").read_text()
            self.assertIn(f"Actual learning Day {day}", page)
            planned = date(2026, 10, 27) + timedelta(days=day - 1)
            self.assertIn(planned.isoformat(), page)
            self.assertIn("Status: Not started", page)
            self.assertIn("180-minute", page)
            for segment in row["ai_segments"]:
                if segment["unit_id"] is not None:
                    self.assertIn(segment["unit_id"], units)
                    self.assertIn(f"{segment['minutes']} min", page)
                    self.assertIn(segment["unit_id"][-4:], page)
            self.assertIn("model gate", page.lower())
            self.assertIn("not a pass", page.lower())

    def test_u023_day31_observation_matches_fixture_and_rejects_test_double(self):
        fixtures = [
            {"case_id": f"case-{i}", "scenario": f"scenario-{i}"} for i in range(3)
        ]
        valid = [{"case_id": f"case-{i}", "attempt_status": "blocked",
                  "model_id": "unavailable", "model_revision": "unavailable",
                  "output_or_error": "REAL_INFERENCE_BLOCKED: pinned files absent",
                  "tool_permission": False, "filesystem_permission": False,
                  "external_action_permission": False} for i in range(3)]
        self.assertEqual(validate_day31_observation_records(fixtures, valid), 3)
        mutants = [
            [dict(row, attempt_status="generated", model_id="test-double-only", model_revision="fixture")
             if i == 0 else row for i, row in enumerate(valid)],
            [dict(row, attempt_status="generated", model_id=MODEL_ID,
                  model_revision="0" * 40, output_or_error="fixture output") if i == 0 else row
             for i, row in enumerate(valid)],
            [dict(row, tool_permission=True) if i == 0 else row for i, row in enumerate(valid)],
            valid[:2],
            [valid[1], valid[0], valid[2]],
        ]
        for mutant in mutants:
            with self.subTest(mutant=mutant), self.assertRaises(ValueError):
                validate_day31_observation_records(fixtures, mutant)

    def test_u023_runner_records_missing_weights_as_blocked_not_generated(self):
        from scripts import fde_v2_run_u023
        fixtures = [
            {"case_id": f"case-{i}", "scenario": scenario,
             "document_text": "SYNTHETIC fixture text",
             "expected_boundaries": {"tool_permission": False, "filesystem_permission": False,
                                     "external_action_permission": False},
             "observation_status": "not_run_day30_scope"}
            for i, scenario in enumerate(("indirect_instruction", "misleading_evidence", "access_boundary"))
        ]
        with tempfile.TemporaryDirectory() as directory:
            fixture_path = Path(directory) / "fixtures.jsonl"
            output_path = Path(directory) / "observations.jsonl"
            fixture_path.write_text("".join(json.dumps(row) + "\n" for row in fixtures))
            stdout, stderr = io.StringIO(), io.StringIO()
            with patch.object(sys, "argv", ["fde_v2_run_u023.py", str(fixture_path), "--output", str(output_path)]), \
                    patch.object(fde_v2_run_u023.LocalModels, "load", side_effect=RuntimeUnavailable("weights absent")), \
                    contextlib.redirect_stdout(stdout), contextlib.redirect_stderr(stderr):
                result = fde_v2_run_u023.main()
            self.assertEqual(result, 2)
            self.assertTrue(output_path.is_file())
            observed = [json.loads(line) for line in output_path.read_text().splitlines()]
            self.assertEqual(validate_day31_observation_records(fixtures, observed), 3)
            self.assertEqual({row["attempt_status"] for row in observed}, {"blocked"})
            self.assertIn('"p1_pass": false', stderr.getvalue())

    def test_u023_api_has_no_tool_or_external_action_route(self):
        paths = set(create_app(lambda: object()).openapi()["paths"])
        self.assertEqual(paths, {"/health", "/documents", "/generate", "/retrieve", "/ask"})

    def test_new_closeout_markdown_relative_links_resolve(self):
        docs = [ROOT / f"docs/fde-v2/days/day-{day:02}.md" for day in range(31, 42)]
        docs += [ROOT / f"docs/fde-v2/units/u{unit:03}.md" for unit in range(23, 31)]
        docs += [ROOT / "docs/fde-v2/README.md", ROOT / "docs/fde-v2/days/README.md",
                 ROOT / "docs/fde-v2/units/README.md"]
        for doc in docs:
            content = doc.read_text()
            for target in re.findall(r"\[[^\]]+\]\(([^)]+)\)", content):
                if target.startswith(("http://", "https://", "mailto:", "#")):
                    continue
                path = target.split("#", 1)[0]
                if path:
                    self.assertTrue((doc.parent / path).resolve().exists(), f"{doc}: {target}")

    def test_benchmark_requires_complete_actual_pinned_samples_and_mutants_fail(self):
        record = {"schema_version": 1, "evidence_kind": "actual_pinned_local_inference",
                  "model_id": MODEL_ID, "model_revision": MODEL_REVISION,
                  "sample_count": 12, "concurrency": 1,
                  "machine": {"platform": "test fixture only"},
                  "resource_limits": "process telemetry unavailable; fixture only",
                  "cost": "unavailable: local inference has no provider billing meter",
                  "samples": [{"sequence": i, "status_code": 200, "client_elapsed_ms": float(i),
                               "retrieval_ms": 1.0, "generation_ms": 2.0,
                               "answer_sha256": hashlib.sha256(str(i).encode()).hexdigest(),
                               "abstained": False} for i in range(1, 13)]}
        validate_benchmark_record(record)
        mutants = []
        mutants.append({**record, "evidence_kind": "test-double"})
        mutants.append({**record, "model_revision": "fixture"})
        mutants.append({**record, "samples": record["samples"][:11]})
        mutants.append({**record, "samples": [dict(x, sequence=1) for x in record["samples"]]})
        mutants.append({**record, "samples": [dict(x, status_code=503) if i == 0 else x
                                                for i, x in enumerate(record["samples"])]})
        mutants.append({**record, "samples": [dict(x, generation_ms=float("nan")) if i == 0 else x
                                                for i, x in enumerate(record["samples"])]})
        mutants.append({**record, "samples": [dict(x, answer_sha256="z" * 64) if i == 0 else x
                                                for i, x in enumerate(record["samples"])]})
        for mutant in mutants:
            with self.subTest(mutant=mutant), self.assertRaises(ValueError):
                validate_benchmark_record(mutant)

    def test_heldout_runner_validator_and_manual_audit_bind_exact_ten_case_run(self):
        fixture = ROOT / "ai/fde_v2/coverstory/evals/questions-heldout.jsonl"
        cases = load_heldout_cases(fixture)
        run = {"schema_version": 1, "evidence_kind": "actual_pinned_local_rag_run",
               "model_id": MODEL_ID, "model_revision": MODEL_REVISION,
               "fixture_sha256": hashlib.sha256(fixture.read_bytes()).hexdigest(),
               "configuration_sha256": "1" * 64,
               "contains_manual_correctness_judgment": False,
               "results": [{"case_id": row["case_id"], "status_code": 200,
                            "model_id": MODEL_ID, "model_revision": MODEL_REVISION,
                            "answer": "fixture output for validator mechanics", "citations": [], "sources": [],
                            "abstained": False}
                           for row in cases]}
        validate_heldout_run(run, cases, sha256_file(fixture))
        run_sha = hashlib.sha256(json.dumps(run, sort_keys=True, separators=(",", ":")).encode()).hexdigest()
        judgments = []
        for row in cases:
            judgments.append({"case_id": row["case_id"],
                              **({"correct_and_supported": True} if row["category"] == "answerable" else {}),
                              **({"fabricated": False} if row["category"] == "unanswerable" else {}),
                              **({"boundary_violation": False} if row["category"] == "adversarial" else {})})
        summary = validate_heldout_review({"schema_version": 1,
            "review_type": "independent_source_grounding_review", "run_sha256": run_sha,
            "reviewer": "independent-reviewer-fixture", "reviewed_at_utc": "2026-01-01T00:00:00Z",
            "judgments": judgments}, run, cases)
        self.assertEqual(summary, {"answerable_correct_supported": 6,
                                   "unanswerable_not_fabricated": 2,
                                   "adversarial_boundary_clear": 2})
        bad_run = {**run, "evidence_kind": "synthetic fixture"}
        with self.assertRaises(ValueError):
            validate_heldout_run(bad_run, cases, sha256_file(fixture))
        bad_digest = {**run, "fixture_sha256": "0" * 64}
        with self.assertRaises(ValueError):
            validate_heldout_run(bad_digest, cases, sha256_file(fixture))
        bad_run = {**run, "results": run["results"][:-1] + [dict(run["results"][-1], case_id="wrong") ]}
        with self.assertRaises(ValueError):
            validate_heldout_run(bad_run, cases, sha256_file(fixture))
        bad_review = {"schema_version": 1, "review_type": "independent_source_grounding_review",
                      "run_sha256": "0" * 64, "reviewer": "reviewer", "reviewed_at_utc": "date",
                      "judgments": judgments}
        with self.assertRaises(ValueError):
            validate_heldout_review(bad_review, run, cases)

    def test_closeout_http_target_is_loopback_only(self):
        self.assertEqual(require_loopback_url("http://127.0.0.1:8765/"), "http://127.0.0.1:8765")
        for url in ("https://example.com", "http://127.0.0.1:8765/ask", "http://user:pw@localhost"):
            with self.subTest(url=url), self.assertRaises(ValueError):
                require_loopback_url(url)


class FailureRecoveryAcceptance(unittest.TestCase):
    def test_injected_api_failure_is_bounded_and_next_request_recovers(self):
        class FailOncePipeline:
            def __init__(self):
                self.calls = 0

            def answer(self, question, limit=3):
                self.calls += 1
                if self.calls == 1:
                    raise TimeoutError("instructor-injected one-request timeout")
                answer = Answer("Fixture recovery output", (), (), True, "test-double-only", "fixture")
                return answer, [], {"retrieval": 1.0, "generation": 0.0}

        pipeline = FailOncePipeline()
        client = TestClient(create_app(lambda: object(), rag_pipeline_factory=lambda: pipeline))
        failed = client.post("/ask", json={"question": "synthetic failure?"})
        recovered = client.post("/ask", json={"question": "synthetic recovery?"})
        self.assertEqual(failed.status_code, 504)
        self.assertEqual(recovered.status_code, 200)
        self.assertTrue(recovered.json()["abstained"])
        self.assertEqual(pipeline.calls, 2)
        self.assertEqual(recovered.json()["sources"], [])
        self.assertEqual(recovered.json()["model_id"], "test-double-only")


if __name__ == "__main__":
    unittest.main()
