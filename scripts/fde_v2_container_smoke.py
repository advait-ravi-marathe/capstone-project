#!/usr/bin/env python3
"""Build and smoke the assistant-supplied API image with host loopback binding."""
from __future__ import annotations

import json
import socket
import subprocess
import sys
import time
import urllib.error
import urllib.request
import uuid


def run(command: list[str], *, timeout: int) -> subprocess.CompletedProcess:
    return subprocess.run(command, check=True, text=True, capture_output=True, timeout=timeout)


def main() -> int:
    image = "coverstory-fde-v2-smoke:" + uuid.uuid4().hex[:12]
    name = "coverstory-fde-v2-" + uuid.uuid4().hex[:10]
    with socket.socket() as listener:
        listener.bind(("127.0.0.1", 0))
        host_port = listener.getsockname()[1]
    container_started = False
    try:
        run(["docker", "build", "--file", "Dockerfile.fde-v2-api", "--tag", image, "."], timeout=900)
        run(["docker", "run", "--detach", "--rm", "--name", name,
             "--publish", f"127.0.0.1:{host_port}:8765", image], timeout=60)
        container_started = True
        base = f"http://127.0.0.1:{host_port}"
        health = None
        for _ in range(30):
            try:
                with urllib.request.urlopen(base + "/health", timeout=2) as response:
                    health = json.loads(response.read())
                break
            except (urllib.error.URLError, TimeoutError, json.JSONDecodeError):
                time.sleep(1)
        if health != {"status": "ok", "mode": "local-learning-prototype"}:
            raise RuntimeError("container did not return the exact loopback health contract")
        request = urllib.request.Request(base + "/ask", data=b'{"question":"coverage?"}',
                                         headers={"Content-Type": "application/json"}, method="POST")
        try:
            urllib.request.urlopen(request, timeout=5)
        except urllib.error.HTTPError as exc:
            # No model/embedding weights are copied into this image; a 503 is
            # the correct fail-closed response, not evidence of RAG readiness.
            if exc.code != 503:
                raise RuntimeError(f"expected model-unavailable 503, got {exc.code}") from exc
        else:
            raise RuntimeError("container unexpectedly served /ask without verified real model artifacts")
        print(json.dumps({"container_smoke": "passed", "health": health,
                          "ask_without_model": "503 fail-closed (expected)",
                          "model_or_rag_evidence": False, "host_binding": "127.0.0.1"}, indent=2))
        return 0
    finally:
        if container_started:
            subprocess.run(["docker", "stop", name], check=False, text=True, capture_output=True, timeout=30)
        subprocess.run(["docker", "image", "rm", image], check=False, text=True, capture_output=True, timeout=60)


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (OSError, subprocess.SubprocessError, RuntimeError) as exc:
        print(f"CONTAINER_SMOKE_BLOCKED_OR_FAILED: {type(exc).__name__}: {exc}", file=sys.stderr)
        raise SystemExit(2)
