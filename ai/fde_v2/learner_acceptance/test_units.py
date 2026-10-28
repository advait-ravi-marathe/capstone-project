"""Actual Sprint 1 learner acceptance paths; default TODOs are expected red."""
from __future__ import annotations

import hashlib
import importlib
import json
import os
import sqlite3
import unittest

from ai.fde_v2.coverstory.contracts import Document
from ai.fde_v2.coverstory.corpus import SCHEMA


candidate = importlib.import_module(os.environ.get("FDE_V2_CANDIDATE", "ai.fde_v2.coverstory.learner"))


class First30LearnerAcceptance(unittest.TestCase):
    def test_u003_parse_document_record(self):
        text = "TRAINING FIXTURE\nSynthetic deductible summary."
        record = {"document_id": "acceptance-doc", "version": 2, "title": "Deductible",
                  "source": "synthetic://acceptance", "text": text}
        parsed = candidate.parse_document_record(record)
        self.assertEqual(parsed.document_id, "acceptance-doc")
        self.assertEqual(parsed.version, 2)
        self.assertEqual(parsed.content_sha256, hashlib.sha256(text.encode()).hexdigest())
        for invalid in (
            {**record, "extra": "not allowed"},
            {**record, "version": True},
            {**record, "version": 0},
            {**record, "title": "  "},
            {key: value for key, value in record.items() if key != "source"},
            {**record, "text": ""},
        ):
            with self.subTest(invalid=invalid):
                with self.assertRaises(ValueError):
                    candidate.parse_document_record(invalid)

    def test_u008_generate_text_bounds_and_forwards_to_real_adapter(self):
        class RecordingGenerator:
            def __init__(self):
                self.calls = []

            def generate_messages(self, messages, max_new_tokens):
                self.calls.append((messages, max_new_tokens))
                return "an instructor test double response", 8.25

        generator = RecordingGenerator()
        answer, elapsed = candidate.generate_text(generator, "Explain the synthetic workflow", 47)
        self.assertEqual((answer, elapsed), ("an instructor test double response", 8.25))
        self.assertEqual(generator.calls, [([{"role": "user", "content": "Explain the synthetic workflow"}], 47)])
        for prompt, limit in (("", 8), (" \t\n ", 8), ("question", 0), ("question", 257), ("question", True),
                              ("x" * 4001, 8)):
            with self.subTest(prompt=prompt, limit=limit):
                before = len(generator.calls)
                with self.assertRaises(ValueError):
                    candidate.generate_text(generator, prompt, limit)
                self.assertEqual(len(generator.calls), before, "invalid request must not reach model adapter")

        class InvalidResultGenerator:
            def __init__(self, result):
                self.result = result

            def generate_messages(self, messages, max_new_tokens):
                return self.result

        for result in (("", 1.0), ("answer", float("nan")), ("answer", -1.0),
                       ("answer", "8.25"), ["answer", 8.25]):
            with self.subTest(result=result):
                with self.assertRaises(RuntimeError):
                    candidate.generate_text(InvalidResultGenerator(result), "valid question", 8)

    def test_u010_answer_json_rejects_wrong_shapes_and_types(self):
        valid = '{"answer":"noted","citations":["policy-v1"],"abstained":false}'
        self.assertEqual(candidate.parse_answer_json(valid), {
            "answer": "noted", "citations": ["policy-v1"], "abstained": False,
        })
        invalid = (
            "not json",
            '{"answer":"x","citations":[],"abstained":false,"extra":1}',
            '{"answer":"x","citations":[],"abstained":"false"}',
            '{"citations":[],"abstained":false}',
            '{"answer":"x","citations":"policy-v1","abstained":false}',
            '{"answer":7,"citations":[],"abstained":false}',
            '{"answer":"x","citations":["policy-v1",7],"abstained":false}',
            '{"answer":"x","citations":["same","same"],"abstained":false}',
        )
        for value in invalid:
            with self.subTest(value=value):
                with self.assertRaises(ValueError):
                    candidate.parse_answer_json(value)

    def test_u011_first_insert_and_identical_reingest(self):
        text = "TRAINING FIXTURE\nThe synthetic individual deductible is $500."
        record = Document("acceptance-deductible", 1, "Deductible", "synthetic://acceptance", text,
                          hashlib.sha256(text.encode("utf-8")).hexdigest())
        with sqlite3.connect(":memory:") as db:
            db.executescript(SCHEMA)
            candidate.upsert_document_metadata(record, db)
            first = db.execute("SELECT document_id,version,title,source,content_sha256 FROM documents").fetchall()
            candidate.upsert_document_metadata(record, db)
            second = db.execute("SELECT document_id,version,title,source,content_sha256 FROM documents").fetchall()
        expected = [(record.document_id, 1, record.title, record.source, record.content_sha256)]
        self.assertEqual(first, expected)
        self.assertEqual(second, expected)
