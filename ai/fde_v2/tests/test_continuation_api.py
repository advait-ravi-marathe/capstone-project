"""HTTP wiring tests; injected pipeline answers are fixtures, never model evidence."""
from __future__ import annotations

import unittest
from unittest.mock import patch

from fastapi.testclient import TestClient

from ai.fde_v2.coverstory.api import create_app
from ai.fde_v2.coverstory.contracts import Answer, Chunk, RetrievedChunk


class FixedPipeline:
    def __init__(self, fail=False):
        self.fail = fail
        self.calls = []
        text = "Synthetic fixture passage."
        self.hit = RetrievedChunk(Chunk("d:v1:0", "d", 1, "Fixture", "CC0 synthetic", 0,
                                        len(text), text), 0.8)

    def retrieve(self, question, limit=3):
        self.calls.append(("retrieve", question, limit))
        if self.fail:
            raise RuntimeError("private fixture failure")
        return [self.hit]

    def answer(self, question):
        self.calls.append(("answer", question))
        if self.fail:
            raise RuntimeError("private fixture failure")
        answer = Answer("Synthetic answer", ("d:v1:0",), ("d:v1:0",), False,
                        "test-double-only", "fixture")
        return answer, [self.hit], {"retrieval": 1.0, "generation": 2.0}


class ContinuationApiTests(unittest.TestCase):
    def test_retrieve_and_ask_http_contracts_use_injected_pipeline(self):
        pipeline = FixedPipeline()
        client = TestClient(create_app(lambda: object(), rag_pipeline_factory=lambda: pipeline))
        retrieved = client.post("/retrieve", json={"question": "deductible?", "limit": 1})
        self.assertEqual(retrieved.status_code, 200)
        self.assertEqual(retrieved.json()["results"][0]["chunk_id"], "d:v1:0")
        self.assertEqual(pipeline.calls[0], ("retrieve", "deductible?", 1))
        generated = client.post("/ask", json={"question": "deductible?"})
        self.assertEqual(generated.status_code, 200)
        payload = generated.json()
        self.assertEqual((payload["answer"], payload["citations"], payload["sources"][0]["chunk_id"]),
                         ("Synthetic answer", ["d:v1:0"], "d:v1:0"))
        self.assertEqual(payload["model_id"], "test-double-only")
        self.assertEqual(payload["stage_timings_ms"], {"retrieval": 1.0, "generation": 2.0})

    def test_invalid_and_unavailable_pipeline_requests_fail_closed(self):
        pipeline = FixedPipeline()
        client = TestClient(create_app(lambda: object(), rag_pipeline_factory=lambda: pipeline))
        for payload in ({"question": "  "}, {"question": "question", "limit": 11}):
            response = client.post("/retrieve", json=payload)
            self.assertEqual(response.status_code, 422)
        failed = TestClient(create_app(lambda: object(), rag_pipeline_factory=lambda: FixedPipeline(True)))
        response = failed.post("/ask", json={"question": "deductible?"})
        self.assertEqual(response.status_code, 502)
        self.assertNotIn("private fixture failure", response.text)


if __name__ == "__main__":
    unittest.main()
