"""Instructor-owned loopback HTTP scaffold for the model integration exercises.

This is a local training harness, not a production server. It binds only to
127.0.0.1, accepts one small JSON request at a time, and never returns exception
details. Learner-owned behavior lives in integration_learner.py.
"""

from __future__ import annotations

import json
import argparse
from http.server import BaseHTTPRequestHandler, HTTPServer
from pathlib import Path
from threading import Thread
from typing import Iterator
from contextlib import contextmanager

from ai.model_building import integration_learner
from ai.model_building import learner


def handler_for(model: dict):
    class ModelHandler(BaseHTTPRequestHandler):
        def do_POST(self):
            if self.path != "/api/model/predict":
                self._write_json(404, {"error": "not_found"})
                return
            try:
                try:
                    length = int(self.headers.get("Content-Length", "-1"))
                except ValueError as length_error:
                    raise integration_learner.InvalidPredictionRequest("invalid request length") from length_error
                if length < 0 or length > 4096:
                    raise integration_learner.InvalidPredictionRequest("request body length out of range")
                body = self.rfile.read(length)
                try:
                    payload = json.loads(body.decode("utf-8"), parse_constant=lambda _: (_ for _ in ()).throw(ValueError("non-finite JSON")))
                except (UnicodeDecodeError, json.JSONDecodeError, ValueError) as parse_error:
                    raise integration_learner.InvalidPredictionRequest("invalid JSON request") from parse_error
                result = integration_learner.build_prediction_response(model, payload)
            except integration_learner.InvalidPredictionRequest as error:
                self._write_error(error)
            except Exception as error:
                self._write_error(error)
            else:
                self._write_json(200, result)

        def _write_json(self, status: int, body: dict):
            encoded = json.dumps(body, separators=(",", ":"), allow_nan=False).encode("utf-8")
            self.send_response(status)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_header("Content-Length", str(len(encoded)))
            self.send_header("Cache-Control", "no-store")
            self.end_headers()
            self.wfile.write(encoded)

        def _write_error(self, error: Exception):
            try:
                status, body = integration_learner.prediction_error_response(error)
                self._write_json(status, body)
            except Exception:
                # Keep an unfinished or faulty learner mapper from exposing a
                # traceback or dropping the connection during local practice.
                self._write_json(500, {"error": "prediction_failed"})

        def log_message(self, format, *args):
            return

    return ModelHandler


@contextmanager
def running_model_api(model: dict) -> Iterator[str]:
    """Start an ephemeral loopback-only HTTP server for tests and local demos."""
    server = HTTPServer(("127.0.0.1", 0), handler_for(model))
    thread = Thread(target=server.serve_forever, daemon=True)
    thread.start()
    try:
        yield f"http://127.0.0.1:{server.server_port}"
    finally:
        server.shutdown()
        thread.join(timeout=2)
        server.server_close()


def serve_artifact(path: Path, host: str = "127.0.0.1", port: int = 8765):
    """Serve a previously saved Day 30 artifact on loopback for local practice."""
    if host != "127.0.0.1" or not 1024 <= port <= 65535:
        raise ValueError("the training server must bind to loopback and an unprivileged port")
    raw = path.read_text(encoding="utf-8")
    if len(raw.encode("utf-8")) > 256_000:
        raise ValueError("model artifact exceeds the local teaching limit")
    model = learner.load_document_type_model(raw)
    HTTPServer((host, port), handler_for(model)).serve_forever()


def main():
    parser = argparse.ArgumentParser(description="Serve a synthetic Day 30 classifier on loopback only")
    parser.add_argument("--artifact", type=Path, required=True)
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=8765)
    args = parser.parse_args()
    serve_artifact(args.artifact, args.host, args.port)


if __name__ == "__main__":
    main()
