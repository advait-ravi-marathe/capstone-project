"""Local-only HTTP adapter for CAP-41's fixed synthetic wording fixture.

The adapter never accepts caller-provided source documents and binds to loopback
by default. Retrieval behavior remains in ``coverstory_health.retrieve``.
"""
from __future__ import annotations

import json
import ipaddress
import os
from dataclasses import asdict
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from typing import Callable
from urllib.parse import urlsplit

from ai.retrieval.coverstory_health import Hit, PublicWording, retrieve


ROOT = Path(__file__).resolve().parents[2]
FIXTURE_PATH = ROOT / "ai/fixtures/coverstory/CAP-41/synthetic-v1.json"


def load_public_fixture() -> list[PublicWording]:
    fixture = json.loads(FIXTURE_PATH.read_text(encoding="utf-8"))
    rows = fixture["cases"]["normal"]["documents"]
    return [PublicWording(
        source_id=row["source_id"], version=row["version"],
        evidence_status=row["evidence_status"], text=row["text"],
    ) for row in rows]


class RetrievalHandler(BaseHTTPRequestHandler):
    server_version = "CoverStoryLocalFixture/1"
    sys_version = ""
    documents = load_public_fixture()
    retrieve_fn: Callable[[str, list[PublicWording], int], list[Hit]] = staticmethod(retrieve)

    def log_message(self, _format: str, *_args: object) -> None:
        # Do not write user queries into terminal or retained logs.
        return

    def _respond(self, status: int, payload: dict[str, object]) -> None:
        body = json.dumps(payload, separators=(",", ":")).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Cache-Control", "no-store")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self) -> None:  # noqa: N802 - stdlib handler API
        self._respond(405, {"code": "METHOD_NOT_ALLOWED"})

    def do_POST(self) -> None:  # noqa: N802 - stdlib handler API
        if urlsplit(self.path).path != "/retrieval/health":
            self._respond(404, {"code": "NOT_FOUND"})
            return
        raw_length = self.headers.get("Content-Length", "")
        if not raw_length.isdigit() or int(raw_length) > 4096:
            self._respond(413, {"code": "REQUEST_TOO_LARGE"})
            return
        try:
            body = json.loads(self.rfile.read(int(raw_length)))
        except (json.JSONDecodeError, UnicodeDecodeError):
            self._respond(400, {"code": "MALFORMED_JSON"})
            return
        if (not isinstance(body, dict) or set(body) != {"query", "k"}
                or not isinstance(body.get("query"), str)
                or len(body["query"]) > 200
                or isinstance(body.get("k"), bool)
                or not isinstance(body.get("k"), int)
                or body["k"] not in (1, 2, 3)):
            self._respond(400, {"code": "INVALID_RETRIEVAL_REQUEST"})
            return
        try:
            hits = self.retrieve_fn(body["query"], self.documents, body["k"])
        except NotImplementedError:
            self._respond(501, {"code": "CAP41_NOT_IMPLEMENTED"})
            return
        except ValueError:
            self._respond(400, {"code": "INVALID_RETRIEVAL_REQUEST"})
            return
        except Exception:
            # Do not serialize exception text, source text, or the query to the client.
            self._respond(500, {"code": "INTERNAL_ERROR"})
            return
        self._respond(200, {"schemaMajor": 1, "classification": "SYNTHETIC",
                            "hits": [asdict(hit) for hit in hits]})


def create_server(host: str = "127.0.0.1", port: int = 8090,
                  retrieve_fn: Callable[[str, list[PublicWording], int], list[Hit]] | None = None
                  ) -> ThreadingHTTPServer:
    if not ipaddress.ip_address(host).is_loopback:
        raise ValueError("CAP-41 fixture server must bind to loopback")
    handler = RetrievalHandler
    if retrieve_fn is not None:
        handler = type("ConfiguredRetrievalHandler", (RetrievalHandler,), {
            "retrieve_fn": staticmethod(retrieve_fn),
        })
    return ThreadingHTTPServer((host, port), handler)


if __name__ == "__main__":
    host = os.environ.get("COVERSTORY_RETRIEVAL_HOST", "127.0.0.1")
    port = int(os.environ.get("COVERSTORY_RETRIEVAL_PORT", "8090"))
    create_server(host, port).serve_forever()
