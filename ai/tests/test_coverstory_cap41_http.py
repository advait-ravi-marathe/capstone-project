"""Transport integration for the local-only CAP-41 fixture endpoint."""
from __future__ import annotations

from http.client import HTTPConnection
import json
import threading
import unittest

from ai.retrieval import coverstory_health_api
from ai.retrieval.coverstory_health import Hit, PublicWording
from ai.tests.coverstory_cap41_contract import expected_hits


class CoverStoryCap41HttpIntegrationTests(unittest.TestCase):
    def start_server(self, retrieve_fn=None):
        server = coverstory_health_api.create_server("127.0.0.1", 0, retrieve_fn)
        thread = threading.Thread(target=server.serve_forever, daemon=True)
        thread.start()
        self.addCleanup(server.shutdown)
        self.addCleanup(server.server_close)
        self.addCleanup(thread.join, 2)
        return server.server_address[1]

    def request(self, port, body, path="/retrieval/health", method="POST"):
        connection = HTTPConnection("127.0.0.1", port, timeout=2)
        self.addCleanup(connection.close)
        encoded = body if isinstance(body, bytes) else json.dumps(body).encode("utf-8")
        connection.request(method, path, body=encoded, headers={"Content-Type": "application/json"})
        response = connection.getresponse()
        return response.status, json.loads(response.read())

    def test_http_adapter_calls_the_supplied_retriever_on_fixed_synthetic_documents(self):
        fixture = json.loads(coverstory_health_api.FIXTURE_PATH.read_text())
        case = fixture["cases"]["normal"]
        seen = {}

        def witness(query: str, docs: list[PublicWording], k: int) -> list[Hit]:
            seen["request"] = (query, k)
            seen["documents"] = [(row.source_id, row.version, row.evidence_status, row.text) for row in docs]
            return expected_hits(case)

        port = self.start_server(witness)
        status, payload = self.request(port, {"query": "waiting period", "k": 2})
        self.assertEqual(200, status)
        self.assertEqual(("waiting period", 2), seen["request"])
        self.assertEqual([(row["source_id"], row["version"], "SYNTHETIC", row["text"])
                          for row in case["documents"]], seen["documents"])
        self.assertEqual("SYNTHETIC", payload["classification"])
        self.assertEqual(["w-health-A", "w-health-B"], [row["source_id"] for row in payload["hits"]])
        self.assertEqual(["v1", "v1"], [row["version"] for row in payload["hits"]])
        self.assertEqual([2, 1], [row["score"] for row in payload["hits"]])

    def test_bad_shape_bounds_and_arbitrary_documents_are_rejected_before_retrieval(self):
        called = []

        def witness(*args):
            called.append(args)
            return []

        port = self.start_server(witness)
        for body in (
            {"query": "x", "k": 0},
            {"query": "x", "k": True},
            {"query": "x" * 201, "k": 1},
            {"query": "x", "k": 1, "documents": [{"text": "caller content"}]},
            {"query": 1, "k": 1},
        ):
            with self.subTest(body=body):
                status, _ = self.request(port, body)
                self.assertEqual(400, status)
        status, _ = self.request(port, b"{broken")
        self.assertEqual(400, status)
        self.assertEqual([], called)

    def test_size_method_path_and_safe_todo_status_boundaries(self):
        port = self.start_server(lambda *_: (_ for _ in ()).throw(NotImplementedError("private details")))
        status, payload = self.request(port, {"query": "x", "k": 1})
        self.assertEqual((501, {"code": "CAP41_NOT_IMPLEMENTED"}), (status, payload))
        self.assertNotIn("private details", json.dumps(payload))
        status, _ = self.request(port, {"query": "x", "k": 1}, path="/wrong")
        self.assertEqual(404, status)
        status, _ = self.request(port, {}, method="GET")
        self.assertEqual(405, status)
        status, _ = self.request(port, b" " * 4097)
        self.assertEqual(413, status)

    def test_unexpected_failure_is_generic_and_does_not_echo_query_or_exception(self):
        port = self.start_server(lambda *_: (_ for _ in ()).throw(RuntimeError("private query detail")))
        status, payload = self.request(port, {"query": "synthetic search text", "k": 1})
        self.assertEqual((500, {"code": "INTERNAL_ERROR"}), (status, payload))
        self.assertNotIn("synthetic search text", json.dumps(payload))
        self.assertNotIn("private query detail", json.dumps(payload))

    def test_server_refuses_non_loopback_binding(self):
        with self.assertRaises(ValueError):
            coverstory_health_api.create_server("0.0.0.0", 0)


if __name__ == "__main__":
    unittest.main()
