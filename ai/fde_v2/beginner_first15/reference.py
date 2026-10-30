"""Instructor reference functions for worked examples; never learner evidence."""
from __future__ import annotations

import re
from collections.abc import Mapping, Sequence
from typing import Any

_WORD = re.compile(r"[a-z0-9]+", re.IGNORECASE)


def document_title(record: Mapping[str, Any]) -> str:
    value = record.get("title", "")
    return value.strip() if isinstance(value, str) else ""


def normalize_question(question: str) -> str:
    return question.strip().lower()


def question_words(question: str) -> list[str]:
    return _WORD.findall(normalize_question(question))


def record_words(record: Mapping[str, Any]) -> list[str]:
    title = record.get("title", "")
    text = record.get("text", "")
    return _WORD.findall(f"{title} {text}".lower())


def is_blank_question(question: str) -> bool:
    return not isinstance(question, str) or not question.strip()


def matching_records(question: str, records: Sequence[Mapping[str, Any]]) -> list[Mapping[str, Any]]:
    words = set(question_words(question))
    if not words:
        return []
    return [record for record in records if words.intersection(record_words(record))]


def unique_question_words(question: str) -> set[str]:
    return set(question_words(question))


def overlap_score(question: str, record: Mapping[str, Any]) -> int:
    return len(unique_question_words(question).intersection(record_words(record)))


def rank_records(question: str, records: Sequence[Mapping[str, Any]], limit: int = 3) -> list[Mapping[str, Any]]:
    if type(limit) is not int or limit < 1:
        raise ValueError("limit must be a positive integer")
    scored = [(record, overlap_score(question, record)) for record in records]
    positive = [(record, score) for record, score in scored if score > 0]
    positive.sort(key=lambda pair: (-pair[1], str(pair[0].get("document_id", ""))))
    return [record for record, _ in positive[:limit]]


def best_record(question: str, records: Sequence[Mapping[str, Any]]) -> Mapping[str, Any] | None:
    ranked = rank_records(question, records, 1)
    return ranked[0] if ranked else None


def source_label(record: Mapping[str, Any]) -> str:
    source = str(record.get("source", "unknown source")).strip()
    title = str(record.get("title", "untitled document")).strip()
    return f"{title} — {source}"


def search_records(question: str, records: Sequence[Mapping[str, Any]], limit: int = 3) -> list[Mapping[str, Any]]:
    if is_blank_question(question):
        return []
    return rank_records(question, matching_records(question, records), limit)


def format_search_results(question: str, records: Sequence[Mapping[str, Any]]) -> str:
    results = search_records(question, records)
    if not results:
        return "No matching documents."
    return "\n".join(source_label(row) for row in results)


def search_response(question: str, records: Sequence[Mapping[str, Any]]) -> str:
    normalized = normalize_question(question)
    if is_blank_question(normalized):
        return "Please enter a question."
    return format_search_results(normalized, records)
