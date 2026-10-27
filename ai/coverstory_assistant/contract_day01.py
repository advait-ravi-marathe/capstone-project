"""Independent Day 1 acceptance oracle shared by learner and mutation tests."""
from __future__ import annotations

from collections.abc import Callable

PromptBuilder = Callable[[str, str], dict[str, str]]

QUESTION = "Is hail damage covered?"
EXCERPT = "Hail damage is excluded."
EXPECTED = {
    "instructions": "Answer only from the supplied policy excerpt. If it does not answer the question, say exactly: I don't know.",
    "input": "Policy excerpt:\nHail damage is excluded.\nQuestion:\nIs hail damage covered?",
}


def assert_day01(candidate: PromptBuilder) -> None:
    actual = candidate(QUESTION, EXCERPT)
    if actual != EXPECTED:
        raise AssertionError(f"prompt contract mismatch: {actual!r}")
    if set(actual) != {"instructions", "input"}:
        raise AssertionError("request must contain exactly instructions and input")
    for invalid in ("", "   ", "\n"):
        try:
            candidate(invalid, EXCERPT)
        except (TypeError, ValueError):
            continue
        raise AssertionError("blank question must be rejected")
    for invalid in ("", "   ", "\n"):
        try:
            candidate(QUESTION, invalid)
        except (TypeError, ValueError):
            continue
        raise AssertionError("blank policy excerpt must be rejected")
