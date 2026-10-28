"""Instructor-only Sprint 1 witnesses; never imported by learner code."""
from __future__ import annotations

import hashlib
import json
import math
from collections.abc import Mapping
from typing import Any

from .contracts import Document


def parse_document_record(raw: Mapping[str, Any]) -> Document:
    required = {"document_id", "version", "title", "source", "text"}
    if not isinstance(raw, Mapping) or set(raw) != required:
        raise ValueError("document fields do not match the contract")
    for name in ("document_id", "title", "source", "text"):
        value = raw[name]
        if not isinstance(value, str) or not value.strip():
            raise ValueError(f"{name} must be a nonempty string")
    if type(raw["version"]) is not int or raw["version"] < 1:
        raise ValueError("version must be a positive integer")
    text = raw["text"]
    return Document(raw["document_id"], raw["version"], raw["title"], raw["source"], text,
                    hashlib.sha256(text.encode("utf-8")).hexdigest())


def generate_text(generator, prompt: str, max_new_tokens: int = 160) -> tuple[str, float]:
    if not isinstance(prompt, str) or not prompt.strip() or len(prompt) > 4000:
        raise ValueError("prompt must contain 1-4000 characters")
    if type(max_new_tokens) is not int or not 1 <= max_new_tokens <= 256:
        raise ValueError("max_new_tokens must be an integer in [1, 256]")
    result = generator.generate_messages([{"role": "user", "content": prompt}], max_new_tokens=max_new_tokens)
    if (not isinstance(result, tuple) or len(result) != 2 or not isinstance(result[0], str)
            or not result[0].strip() or type(result[1]) not in (int, float)
            or not math.isfinite(result[1]) or result[1] < 0):
        raise RuntimeError("generation adapter returned an invalid result")
    return result[0], float(result[1])


def parse_answer_json(text: str) -> dict[str, Any]:
    if not isinstance(text, str):
        raise ValueError("model response must be text")
    try:
        value = json.loads(text)
    except json.JSONDecodeError as exc:
        raise ValueError("model response is not valid JSON") from exc
    if not isinstance(value, dict) or set(value) != {"answer", "citations", "abstained"}:
        raise ValueError("model response fields do not match the answer schema")
    if not isinstance(value["answer"], str) or type(value["abstained"]) is not bool:
        raise ValueError("answer and abstained have invalid types")
    citations = value["citations"]
    if not isinstance(citations, list) or any(not isinstance(item, str) or not item for item in citations):
        raise ValueError("citations must be nonempty string IDs")
    if len(set(citations)) != len(citations):
        raise ValueError("duplicate citations are not accepted")
    return value


def upsert_document_metadata(record: Document, connection) -> str:
    """Instructor witness for only the Day 14 insert/idempotent-reingest slice."""
    if not isinstance(record, Document):
        raise TypeError("record must be a validated Document")
    existing = connection.execute(
        "SELECT version,title,source,content_sha256 FROM documents WHERE document_id = ?",
        (record.document_id,),
    ).fetchone()
    if existing is None:
        connection.execute(
            "INSERT INTO documents(document_id,version,title,source,content_sha256) VALUES(?,?,?,?,?)",
            (record.document_id, record.version, record.title, record.source, record.content_sha256),
        )
        return "inserted"
    values = (existing[0], existing[1], existing[2], existing[3])
    expected = (record.version, record.title, record.source, record.content_sha256)
    if values != expected:
        raise ValueError("changed document requires the later version-update segment")
    return "unchanged"
