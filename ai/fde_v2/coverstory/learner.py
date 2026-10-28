"""Learner-owned AI seams assigned by the Sprint 1 actual-day contracts.

Application infrastructure (HTTP, model loading, UI and SQLite schema) is supplied.
Only the four named behavior seams below are learner tasks in this bounded release.
"""
from __future__ import annotations

from collections.abc import Mapping
from typing import Any

from .contracts import Document


def parse_document_record(raw: Mapping[str, Any]) -> Document:
    """Day 3: validate a synthetic document record and calculate its text digest."""
    raise NotImplementedError("Day 3: implement parse_document_record")


def generate_text(generator, prompt: str, max_new_tokens: int = 160) -> tuple[str, float]:
    """Day 8: validate bounds and forward a prompt to the real generation adapter."""
    raise NotImplementedError("Day 8: implement generate_text")


def parse_answer_json(text: str) -> dict[str, Any]:
    """Day 13: parse the strict answer shape without coercing malformed output."""
    raise NotImplementedError("Day 13: implement parse_answer_json")


def upsert_document_metadata(record: Document, connection) -> str:
    """Day 14 partial: insert one validated document row idempotently.

    This first 30-minute slice checks insert and identical re-ingest. Version-change
    policy, bulk ingestion, chunking and retrieval are later segments, not this task.
    """
    raise NotImplementedError("Day 14: implement upsert_document_metadata")
