"""Loopback-only synthetic adapter for the assistant-owned Spring API.

This mode returns fixed fixtures and is deliberately not a model call. It is
used to exercise the browser -> Spring -> Python serving path without sending
data to a provider.
"""
from __future__ import annotations

import argparse
import json
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from typing import Any

POLICY = {
    "owner-a": {"document_id": "SYN-POL-A", "version": "v1", "section": "Exclusions", "text": "Hail damage is excluded."},
    "owner-b": {"document_id": "SYN-POL-B", "version": "v1", "section": "Coverage", "text": "Hail damage is covered up to the listed limit."},
}


def offline_answer(request: dict[str, Any]) -> dict[str, Any]:
    if set(request) != {"question", "tenant_id"}:
        raise ValueError("request must contain only question and tenant_id")
    question = request["question"]
    tenant_id = request["tenant_id"]
    if not isinstance(question, str) or not question.strip() or len(question) > 1000:
        raise ValueError("question must be nonblank and at most 1000 characters")
    document = POLICY.get(tenant_id)
    if document is None:
        raise PermissionError("tenant unavailable")
    # Fixed synthetic fixture behavior; intentionally not an AI model result.
    if "hail" in question.lower():
        answer = document["text"]
        abstained = False
        citations = [{"documentId": document["document_id"], "version": document["version"], "section": document["section"]}]
    else:
        answer = "I don't know."
        abstained = True
        citations = []
    return {
        "answer": answer,
        "citations": citations,
        "abstained": abstained,
        "executionMode": "offline_synthetic_fixture",
        "providerRequestMade": False,
        "liveQualityEvaluated": False,
    }


class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path != "/health":
            self._send(404, {"code": "NOT_FOUND"})
            return
        self._send(200, {"status": "ready", "executionMode": "offline_synthetic_fixture", "providerRequestMade": False})

    def do_POST(self):
        if self.path != "/answer":
            self._send(404, {"code": "NOT_FOUND"})
            return
        try:
            length = int(self.headers.get("Content-Length", "-1"))
        except ValueError:
            length = -1
        if length < 0 or length > 4096:
            self._send(413, {"code": "REQUEST_TOO_LARGE"})
            return
        try:
            body = json.loads(self.rfile.read(length))
            if not isinstance(body, dict):
                raise ValueError("request must be an object")
            result = offline_answer(body)
        except PermissionError:
            self._send(403, {"code": "TENANT_UNAVAILABLE"})
            return
        except (ValueError, json.JSONDecodeError, UnicodeDecodeError):
            self._send(400, {"code": "INVALID_REQUEST"})
            return
        self._send(200, result)

    def _send(self, status: int, value: dict[str, Any]) -> None:
        raw = json.dumps(value, sort_keys=True, separators=(",", ":")).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(raw)))
        self.end_headers()
        self.wfile.write(raw)

    def log_message(self, *_args):
        # Synthetic questions and tenant values are not written to access logs.
        pass


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=8765)
    args = parser.parse_args()
    if args.host not in {"127.0.0.1", "localhost", "::1"}:
        parser.error("the synthetic training service binds to loopback only")
    server = ThreadingHTTPServer((args.host, args.port), Handler)
    print(f"offline synthetic fixture service listening on {args.host}:{server.server_port}; no provider calls")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        server.shutdown()
        server.server_close()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
