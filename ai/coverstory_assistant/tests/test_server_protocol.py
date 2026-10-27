import json
import threading
import unittest
from http.server import ThreadingHTTPServer
from urllib.error import HTTPError
from urllib.request import Request, urlopen

from ai.coverstory_assistant.server import Handler


class ServerProtocolTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.server = ThreadingHTTPServer(("127.0.0.1", 0), Handler)
        cls.thread = threading.Thread(target=cls.server.serve_forever, daemon=True)
        cls.thread.start()
        cls.base = f"http://127.0.0.1:{cls.server.server_port}"

    @classmethod
    def tearDownClass(cls):
        cls.server.shutdown()
        cls.thread.join(timeout=2)
        cls.server.server_close()

    def test_health_and_owner_scoped_synthetic_answer(self):
        with urlopen(self.base + "/health", timeout=2) as response:
            health = json.load(response)
        self.assertEqual({"status": "ready", "executionMode": "offline_synthetic_fixture", "providerRequestMade": False}, health)
        request = Request(self.base + "/answer", data=json.dumps({"question": "Is hail covered?", "tenant_id": "owner-a"}).encode(), headers={"Content-Type": "application/json"}, method="POST")
        with urlopen(request, timeout=2) as response:
            answer = json.load(response)
        self.assertEqual("Hail damage is excluded.", answer["answer"])
        self.assertEqual("SYN-POL-A", answer["citations"][0]["documentId"])
        self.assertFalse(answer["providerRequestMade"])

    def test_unknown_tenant_and_oversized_request_fail_closed(self):
        request = Request(self.base + "/answer", data=b'{"question":"x","tenant_id":"owner-c"}', headers={"Content-Type": "application/json"}, method="POST")
        with self.assertRaises(HTTPError) as caught:
            urlopen(request, timeout=2)
        self.assertEqual(403, caught.exception.code)
        request = Request(self.base + "/answer", data=b" " * 4097, method="POST")
        with self.assertRaises(HTTPError) as caught:
            urlopen(request, timeout=2)
        self.assertEqual(413, caught.exception.code)
