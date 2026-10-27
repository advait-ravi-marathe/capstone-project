"""Small real OpenAI Responses API transport with bounded, redacted failures.

The module never reads credentials from disk. The caller must supply a key
from an environment or another user-managed source. Tests point it at a local
HTTP server and make no external request.
"""
from __future__ import annotations

import json
from dataclasses import dataclass
from urllib.error import HTTPError, URLError
from urllib.parse import urlparse
from urllib.request import Request, urlopen


class ProviderFailure(RuntimeError):
    """Safe-to-report provider failure; deliberately excludes request secrets."""


@dataclass(frozen=True)
class ProviderConfig:
    api_key: str
    model: str
    base_url: str = "https://api.openai.com/v1"
    timeout_seconds: float = 10.0
    max_output_tokens: int = 256


class OpenAIResponsesClient:
    def __init__(self, config: ProviderConfig, opener=urlopen):
        self._config = config
        self._opener = opener
        self._validate_config()

    def _validate_config(self) -> None:
        c = self._config
        if not isinstance(c.api_key, str) or not c.api_key.strip():
            raise ValueError("provider API key is required")
        if not isinstance(c.model, str) or not c.model.strip():
            raise ValueError("provider model is required")
        parsed = urlparse(c.base_url)
        local_http = parsed.scheme == "http" and parsed.hostname in {"127.0.0.1", "localhost", "::1"}
        if parsed.scheme != "https" and not local_http:
            raise ValueError("provider URL must use HTTPS; HTTP is allowed only for loopback tests")
        if not parsed.netloc or parsed.query or parsed.fragment:
            raise ValueError("provider URL must be an origin/path without query or fragment")
        if not 0 < c.timeout_seconds <= 30:
            raise ValueError("timeout must be in (0, 30] seconds")
        if not 1 <= c.max_output_tokens <= 2048:
            raise ValueError("max_output_tokens must be in [1, 2048]")

    def generate(self, instructions: str, input_text: str) -> str:
        if not isinstance(instructions, str) or not instructions.strip():
            raise ValueError("instructions must be nonblank")
        if not isinstance(input_text, str) or not input_text.strip():
            raise ValueError("input must be nonblank")
        url = self._config.base_url.rstrip("/") + "/responses"
        body = {
            "model": self._config.model,
            "instructions": instructions,
            "input": input_text,
            "max_output_tokens": self._config.max_output_tokens,
            "store": False,
        }
        request = Request(
            url,
            data=json.dumps(body, separators=(",", ":")).encode("utf-8"),
            headers={
                "Authorization": "Bearer " + self._config.api_key,
                "Content-Type": "application/json",
                "Accept": "application/json",
            },
            method="POST",
        )
        try:
            with self._opener(request, timeout=self._config.timeout_seconds) as response:
                raw = response.read(1_000_001)
        except HTTPError as exc:
            # Do not include provider response bodies: they may echo prompt or credentials.
            raise ProviderFailure(f"provider returned HTTP {exc.code}") from None
        except (URLError, TimeoutError, OSError) as exc:
            reason = "timeout" if isinstance(exc, TimeoutError) else "transport error"
            raise ProviderFailure(f"provider {reason}") from None
        if len(raw) > 1_000_000:
            raise ProviderFailure("provider response exceeded 1000000 bytes")
        try:
            payload = json.loads(raw)
        except (UnicodeDecodeError, json.JSONDecodeError):
            raise ProviderFailure("provider response was not valid JSON") from None
        if not isinstance(payload, dict) or payload.get("status") != "completed":
            raise ProviderFailure("provider response was not completed")
        output = payload.get("output")
        if not isinstance(output, list):
            raise ProviderFailure("provider response omitted output items")
        texts: list[str] = []
        for item in output:
            if not isinstance(item, dict) or item.get("type") != "message":
                continue
            content = item.get("content")
            if not isinstance(content, list):
                continue
            for part in content:
                if isinstance(part, dict) and part.get("type") == "output_text":
                    text = part.get("text")
                    if isinstance(text, str):
                        texts.append(text)
        result = "".join(texts)
        if not result.strip():
            raise ProviderFailure("provider response contained no output text")
        return result
