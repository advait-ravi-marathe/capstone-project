import json
import threading
import unittest
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

from ai.coverstory_assistant.provider import OpenAIResponsesClient, ProviderConfig, ProviderFailure


class _ResponseState:
    request = None
    status = 200
    payload = {"status": "completed", "output": [{"type": "message", "content": [{"type": "output_text", "text": "Synthetic answer."}]}]}


class _Handler(BaseHTTPRequestHandler):
    def do_POST(self):
        _ResponseState.request = {
            "path": self.path,
            "authorization": self.headers.get("Authorization"),
            "body": json.loads(self.rfile.read(int(self.headers["Content-Length"]))),
        }
        raw = json.dumps(_ResponseState.payload).encode("utf-8")
        self.send_response(_ResponseState.status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(raw)))
        self.end_headers()
        self.wfile.write(raw)

    def log_message(self, *_args):
        pass


class ProviderProtocolTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.server = ThreadingHTTPServer(("127.0.0.1", 0), _Handler)
        cls.thread = threading.Thread(target=cls.server.serve_forever, daemon=True)
        cls.thread.start()
        cls.base_url = f"http://127.0.0.1:{cls.server.server_port}/v1"

    @classmethod
    def tearDownClass(cls):
        cls.server.shutdown()
        cls.thread.join(timeout=2)
        cls.server.server_close()

    def setUp(self):
        _ResponseState.request = None
        _ResponseState.status = 200
        _ResponseState.payload = {"status": "completed", "output": [{"type": "message", "content": [{"type": "output_text", "text": "Synthetic answer."}]}]}

    def client(self, **overrides):
        values = {"api_key": "test-secret-never-live", "model": "fixture-model", "base_url": self.base_url}
        values.update(overrides)
        return OpenAIResponsesClient(ProviderConfig(**values))

    def test_uses_real_responses_request_shape_against_local_fixture(self):
        result = self.client().generate("fixed instructions", "fixed synthetic input")
        self.assertEqual("Synthetic answer.", result)
        self.assertEqual("/v1/responses", _ResponseState.request["path"])
        self.assertEqual("Bearer test-secret-never-live", _ResponseState.request["authorization"])
        self.assertEqual(
            {"model": "fixture-model", "instructions": "fixed instructions", "input": "fixed synthetic input", "max_output_tokens": 256, "store": False},
            _ResponseState.request["body"],
        )

    def test_non_success_and_incomplete_response_fail_without_echoing_secret(self):
        _ResponseState.status = 401
        with self.assertRaises(ProviderFailure) as caught:
            self.client().generate("fixed instructions", "fixed input")
        self.assertNotIn("test-secret-never-live", str(caught.exception))
        _ResponseState.status = 200
        _ResponseState.payload = {"status": "incomplete", "output": []}
        with self.assertRaisesRegex(ProviderFailure, "not completed"):
            self.client().generate("fixed instructions", "fixed input")

    def test_rejects_unbounded_and_non_loopback_http_configuration(self):
        with self.assertRaisesRegex(ValueError, "HTTPS"):
            self.client(base_url="http://example.com/v1")
        with self.assertRaisesRegex(ValueError, "timeout"):
            self.client(timeout_seconds=31)
