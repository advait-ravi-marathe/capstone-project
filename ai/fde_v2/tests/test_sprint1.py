"""Deterministic instructor checks for only the bounded Sprint 1 surface."""
from __future__ import annotations

import json
import sqlite3
import threading
import tempfile
import unittest
from concurrent.futures import ThreadPoolExecutor
from unittest.mock import patch
from pathlib import Path

from fastapi.testclient import TestClient

from ai.fde_v2.coverstory import reference
from ai.fde_v2.coverstory.api import create_app
from ai.fde_v2.coverstory.contracts import RuntimeUnavailable
from ai.fde_v2.coverstory.corpus import CorpusStore, SCHEMA
from ai.fde_v2.coverstory.day01_baseline import CORPUS, run
from ai.fde_v2.coverstory.models import MODEL_ID, MODEL_REVISION, LocalModels

ROOT = Path(__file__).resolve().parents[3]


class Sprint1Infrastructure(unittest.TestCase):
    def test_day1_cpu_lexical_baseline_returns_the_known_synthetic_document(self):
        hits = run("What is the individual deductible?")
        self.assertGreaterEqual(len(hits), 1)
        self.assertEqual(hits[0][0], "northstar-deductible")
        self.assertEqual(hits[0][2], 1)
        self.assertEqual(run("quuxzzzz blorfq glorp"), [])

    def test_synthetic_corpus_has_ten_manifested_records(self):
        manifest = json.loads((CORPUS / "manifest.json").read_text(encoding="utf-8"))
        self.assertEqual(len(manifest["documents"]), 10)
        seen = set()
        for filename in manifest["documents"]:
            path = CORPUS / filename
            self.assertTrue(path.is_file())
            record = json.loads(path.read_text(encoding="utf-8"))
            self.assertTrue(record["text"].startswith("TRAINING FIXTURE"))
            self.assertEqual(record["version"], 1)
            self.assertNotIn(record["document_id"], seen)
            seen.add(record["document_id"])

    def test_health_and_document_metadata_routes(self):
        class TestGenerator:
            def generate_messages(self, messages, max_new_tokens):
                return ("bounded test response", 1.25)

        with tempfile.TemporaryDirectory() as temporary:
            with patch.dict("os.environ", {"FDE_V2_DATA_DIR": temporary}):
                client = TestClient(create_app(lambda: TestGenerator(), text_adapter=reference.generate_text))
                self.assertEqual(client.get("/health").json(),
                                 {"status": "ok", "mode": "local-learning-prototype"})
                empty = client.get("/documents", headers={"x-request-id": "u004-empty"})
                self.assertEqual(empty.status_code, 200)
                self.assertEqual(empty.json(), {"documents": []})
                self.assertEqual(empty.headers["x-request-id"], "u004-empty")

                store = CorpusStore(Path(temporary) / "corpus.sqlite3")
                with store.connect() as db:
                    db.execute("INSERT INTO documents VALUES(?,?,?,?,?)",
                               ("fixture-1", 1, "Fixture", "synthetic://one", "a" * 64))
                with self.assertLogs("fde_v2.coverstory", level="INFO") as success_logs:
                    populated = client.get("/documents", headers={"x-request-id": "u004-populated"})
                self.assertEqual(populated.json()["documents"], [{
                    "document_id": "fixture-1", "version": 1, "title": "Fixture",
                    "source": "synthetic://one", "content_sha256": "a" * 64,
                }])
                self.assertEqual(populated.headers["x-request-id"], "u004-populated")
                self.assertIn("request_id=u004-populated", "\n".join(success_logs.output))
                self.assertNotIn("Fixture", "\n".join(success_logs.output))

                (Path(temporary) / "corpus.sqlite3").write_bytes(b"not a sqlite database")
                with self.assertLogs("fde_v2.coverstory", level="INFO") as failure_logs:
                    failed = client.get("/documents", headers={"x-request-id": "u004-failure"})
                self.assertEqual(failed.status_code, 500)
                self.assertNotIn("not a sqlite database", failed.text)
                rendered_logs = "\n".join(failure_logs.output)
                self.assertIn("request_id=u004-failure", rendered_logs)
                self.assertNotIn("TRAINING FIXTURE", rendered_logs)

    def test_generate_route_contract_and_failures(self):
        class TestGenerator:
            def generate_messages(self, messages, max_new_tokens):
                self.asserted = (messages, max_new_tokens)
                return ("local witness only", 3.5)

        client = TestClient(create_app(lambda: TestGenerator(), text_adapter=reference.generate_text))
        response = client.post("/generate", json={"question": " hello "}, headers={"x-request-id": "req-test"})
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json(), {"answer": "local witness only", "elapsed_ms": 3.5,
                                           "provider": "pinned-local-model", "request_id": "req-test"})
        self.assertEqual(response.headers["x-request-id"], "req-test")
        empty = client.post("/generate", json={"question": ""})
        self.assertEqual(empty.status_code, 422)
        whitespace = client.post("/generate", json={"question": " \t\n "})
        self.assertEqual(whitespace.status_code, 422)
        oversized_prompt = "private payload " + "x" * 2001
        oversized = client.post("/generate", json={"question": oversized_prompt})
        self.assertEqual(oversized.status_code, 422)
        self.assertNotIn(oversized_prompt, oversized.text)

        def unavailable():
            raise RuntimeUnavailable("private path detail")

        unavailable_client = TestClient(create_app(unavailable, text_adapter=reference.generate_text))
        unavailable_response = unavailable_client.post("/generate", json={"question": "safe prompt"})
        self.assertEqual(unavailable_response.status_code, 503)
        self.assertNotIn("private path detail", unavailable_response.text)

        class BrokenGenerator:
            def generate_messages(self, messages, max_new_tokens):
                raise RuntimeError("sensitive backend detail")

        broken = TestClient(create_app(lambda: BrokenGenerator(), text_adapter=reference.generate_text)).post(
            "/generate", json={"question": "safe prompt"})
        self.assertEqual(broken.status_code, 502)
        self.assertNotIn("sensitive backend detail", broken.text)

        class TimedOutGenerator:
            def generate_messages(self, messages, max_new_tokens):
                raise TimeoutError("private timeout detail")

        timed_out = TestClient(create_app(lambda: TimedOutGenerator(), text_adapter=reference.generate_text)).post(
            "/generate", json={"question": "safe prompt"})
        self.assertEqual(timed_out.status_code, 504)
        self.assertNotIn("private timeout detail", timed_out.text)

    def test_default_api_uses_the_learner_generation_seam_without_reference_fallback(self):
        calls = []

        def learner_implementation(generator, prompt, max_new_tokens=160):
            calls.append((generator, prompt, max_new_tokens))
            return "learner-seam-output", 7.25

        with patch("ai.fde_v2.coverstory.api.learner.generate_text", learner_implementation):
            client = TestClient(create_app(lambda: object()))
            response = client.post("/generate", json={"question": "learner-owned prompt"})

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json()["answer"], "learner-seam-output")
        self.assertEqual(len(calls), 1)
        self.assertEqual(calls[0][1:], ("learner-owned prompt", 160))

        todo_response = TestClient(create_app(lambda: object())).post(
            "/generate", json={"question": "do not substitute the reference"})
        self.assertEqual(todo_response.status_code, 502)
        self.assertNotIn("reference", todo_response.text.lower())

    def test_generation_is_single_flight_and_rejects_overlap_without_loading_twice(self):
        entered = threading.Event()
        release = threading.Event()
        factory_calls = []
        adapter_calls = []

        def factory():
            factory_calls.append(object())
            return factory_calls[-1]

        def adapter(generator, prompt, max_new_tokens=160):
            adapter_calls.append((generator, prompt))
            entered.set()
            if not release.wait(timeout=3):
                raise TimeoutError("test request was not released")
            return "bounded first response", 2.0

        client = TestClient(create_app(factory, text_adapter=adapter))
        with ThreadPoolExecutor(max_workers=2) as pool:
            first = pool.submit(client.post, "/generate", json={"question": "first"})
            self.assertTrue(entered.wait(timeout=2), "first request never entered the model adapter")
            second = client.post("/generate", json={"question": "overlapping"})
            release.set()
            first_response = first.result(timeout=3)

        self.assertEqual(first_response.status_code, 200)
        self.assertEqual(second.status_code, 429)
        self.assertEqual(len(factory_calls), 1)
        self.assertEqual(adapter_calls, [(factory_calls[0], "first")])

    def test_local_generation_adapter_has_a_bounded_step_aware_timeout(self):
        import time
        import torch
        from transformers import StoppingCriteria, StoppingCriteriaList
        from ai.fde_v2.coverstory.models import LocalGenerator

        class Tokenizer:
            eos_token_id = 0

            def apply_chat_template(self, messages, tokenize, add_generation_prompt):
                return messages[0]["content"]

            def __call__(self, _prompt, **_kwargs):
                return {"input_ids": torch.tensor([[1, 2]], dtype=torch.long)}

            def decode(self, _tokens, skip_special_tokens):
                return "test-only output"

        class Model:
            def generate(self, **kwargs):
                criteria = kwargs["stopping_criteria"]
                self.asserted_max_tokens = kwargs["max_new_tokens"]
                self.asserted_stopped = criteria(torch.tensor([[1, 2]], dtype=torch.long), None)
                return torch.tensor([[1, 2, 3]], dtype=torch.long)

        model = Model()
        local = LocalGenerator(torch, Tokenizer(), model, StoppingCriteria, StoppingCriteriaList, timeout_seconds=5)
        text, elapsed = local.generate_messages([{"role": "user", "content": "bounded request"}], max_new_tokens=7)
        self.assertEqual(text, "test-only output")
        self.assertEqual(model.asserted_max_tokens, 7)
        self.assertFalse(bool(model.asserted_stopped))
        self.assertGreaterEqual(elapsed, 0)

        class SlowTokenizer(Tokenizer):
            def __call__(self, prompt, **kwargs):
                time.sleep(1.05)
                return super().__call__(prompt, **kwargs)

        expired = LocalGenerator(torch, SlowTokenizer(), model, StoppingCriteria, StoppingCriteriaList,
                                 timeout_seconds=1)
        with self.assertRaises(TimeoutError):
            expired.generate_messages([{"role": "user", "content": "slow input"}])

    def test_pinned_generation_configuration(self):
        lock = json.loads((ROOT / "ai/fde_v2/coverstory/model_lock.json").read_text(encoding="utf-8"))
        self.assertEqual(lock["generation"]["repository"], MODEL_ID)
        self.assertEqual(lock["generation"]["revision"], MODEL_REVISION)
        self.assertEqual(lock["generation"]["license"], "Apache-2.0")
        self.assertIs(lock["trust_remote_code"], False)
        self.assertNotIn("embedding", lock, "embedding is outside this 14-day release")
        self.assertEqual(list((ROOT / "ai/fde_v2/coverstory").glob("**/*.safetensors")), [])
        self.assertEqual(list((ROOT / "ai/fde_v2/coverstory").glob("**/pytorch_model.bin")), [])
        with tempfile.TemporaryDirectory() as temporary:
            with self.assertRaises(RuntimeUnavailable):
                LocalModels.load(Path(temporary))

    def test_local_loader_rejects_incomplete_extra_unsafe_and_corrupt_artifacts_before_import(self):
        import builtins
        lock = json.loads((ROOT / "ai/fde_v2/coverstory/model_lock.json").read_text(encoding="utf-8"))

        def write_manifest(root: Path):
            directory = root / "generation"
            directory.mkdir(parents=True)
            hashes = {}
            for name in lock["generation"]["expected_files"]:
                path = directory / name
                path.write_bytes(("inert fixture:" + name).encode())
                import hashlib
                hashes[name] = hashlib.sha256(path.read_bytes()).hexdigest()
            manifest = {"trust_remote_code": False, "generation": {
                "repository": MODEL_ID, "revision": MODEL_REVISION, "files": hashes}}
            (root / "runtime-manifest.json").write_text(json.dumps(manifest), encoding="utf-8")
            return directory, manifest

        def assert_rejected(change):
            with tempfile.TemporaryDirectory() as temporary:
                root = Path(temporary)
                directory, manifest = write_manifest(root)
                change(root, directory, manifest)
                (root / "runtime-manifest.json").write_text(json.dumps(manifest), encoding="utf-8")
                attempted_imports = []
                original_import = builtins.__import__

                def guard_model_import(name, *args, **kwargs):
                    if name == "torch" or name.startswith("transformers"):
                        attempted_imports.append(name)
                        raise AssertionError("model loader reached framework imports before artifact validation")
                    return original_import(name, *args, **kwargs)

                with patch("builtins.__import__", side_effect=guard_model_import):
                    with self.assertRaises(RuntimeUnavailable):
                        LocalModels.load(root)
                self.assertEqual(attempted_imports, [])

        assert_rejected(lambda _root, _directory, manifest:
                        manifest["generation"]["files"].pop("config.json"))
        assert_rejected(lambda _root, _directory, manifest:
                        manifest["generation"]["files"].update({"unapproved.bin": "0" * 64}))
        assert_rejected(lambda _root, directory, _manifest:
                        (directory / "unverified-weights.bin").write_bytes(b"extra"))
        assert_rejected(lambda _root, directory, _manifest:
                        (directory / "config.json").write_bytes(b"corrupted after hashing"))

        def symlink_expected(_root, directory, manifest):
            target = directory.parent / "external-config.json"
            target.write_text("external", encoding="utf-8")
            (directory / "config.json").unlink()
            (directory / "config.json").symlink_to(target)

        assert_rejected(symlink_expected)

    def test_day14_schema_and_independent_insert_witness(self):
        with sqlite3.connect(":memory:") as db:
            db.executescript(SCHEMA)
            raw = {"document_id": "meta-one", "version": 1, "title": "Synthetic",
                   "source": "synthetic://fixture", "text": "TRAINING FIXTURE\nA short source."}
            record = reference.parse_document_record(raw)
            self.assertEqual(reference.upsert_document_metadata(record, db), "inserted")
            self.assertEqual(reference.upsert_document_metadata(record, db), "unchanged")
            rows = db.execute("SELECT document_id,version,title,source,content_sha256 FROM documents").fetchall()
        self.assertEqual(rows, [("meta-one", 1, "Synthetic", "synthetic://fixture",
                                 "cf6b86c3594ce838c0b741f5051d4a8affafd43c71acde0047e0d8a0f777d00d")])
        self.assertEqual(len(rows), 1)


if __name__ == "__main__":
    unittest.main()
