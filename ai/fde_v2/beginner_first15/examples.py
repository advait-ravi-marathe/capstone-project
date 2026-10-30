"""Run the instructor's tiny worked example for one beginner-first day.

These examples use only completed reference functions. They deliberately do
not import the learner module, so a learner TODO never blocks a walkthrough.
Their output is practice material, not learner evidence.
"""
from __future__ import annotations

import argparse

from . import reference


SMALL_RECORDS = [
    {"document_id": "family", "title": "Family", "text": "family deductible", "source": "fixture/family"},
    {"document_id": "appeals", "title": "Appeals", "text": "claim review", "source": "fixture/appeals"},
]
RANKING_RECORDS = [
    {"document_id": "b", "title": "Red", "text": "red"},
    {"document_id": "a", "title": "Red", "text": "red"},
]
DISPLAY_RECORDS = [
    {"document_id": "therapy", "title": "Physical therapy", "text": "Referral required", "source": "synthetic/therapy"},
    {"document_id": "appeals", "title": "Appeals", "text": "Review denied claims", "source": "synthetic/appeals"},
]


def _render(value: object) -> str:
    return value if isinstance(value, str) else repr(value)


def _example(day: int) -> tuple[str, str]:
    """Return a plain-English input line and deterministic output for a day."""
    if day == 1:
        value = reference.document_title({"title": "Appeals"})
        return 'record={"title": "Appeals"}', _render(value)
    if day == 2:
        value = reference.normalize_question("  HELLO  ")
        return 'question="  HELLO  "', _render(value)
    if day == 3:
        value = reference.question_words("Blue, sky!")
        return 'question="Blue, sky!"', repr(value)
    if day == 4:
        value = reference.record_words({"title": "Plan", "text": "Family coverage"})
        return 'record={"title": "Plan", "text": "Family coverage"}', repr(value)
    if day == 5:
        value = reference.is_blank_question("   ")
        return 'question="   "', repr(value)
    if day == 6:
        found = reference.matching_records("family", SMALL_RECORDS)
        inputs = '[{"document_id":"family","text":"family deductible"}, {"document_id":"appeals","text":"claim review"}]'
        return f'question="family"; records={inputs}', repr([row["document_id"] for row in found])
    if day == 7:
        value = reference.unique_question_words("cat cat dog")
        stable_set = "{" + ", ".join(repr(word) for word in sorted(value)) + "}"
        return 'question="cat cat dog"', stable_set
    if day == 8:
        value = reference.overlap_score("red red blue", {"title": "", "text": "blue green"})
        return 'question="red red blue"; record text="blue green"', repr(value)
    if day == 9:
        found = reference.rank_records("red", RANKING_RECORDS, limit=2)
        return 'question="red"; two equal-score records; limit=2', repr([row["document_id"] for row in found])
    if day == 10:
        value = reference.best_record("moonstone", [])
        return 'question="moonstone"; records=[]', repr(value)
    if day == 11:
        value = reference.source_label({"title": "Guide", "source": "fixture/guide"})
        return 'record={"title": "Guide", "source": "fixture/guide"}', _render(value)
    if day == 12:
        record = {"document_id": "toy", "version": 1, "title": "Guide", "source": "fixture/guide", "text": "hello"}
        return 'valid five-field record with version=1', repr(reference.document_record_valid(record))
    if day == 13:
        found = reference.search_records("family plan", SMALL_RECORDS)
        inputs = '[{"document_id":"family","title":"Family","text":"family deductible"}, {"document_id":"appeals","title":"Appeals","text":"claim review"}]'
        return f'question="family plan"; records={inputs}', repr([row["document_id"] for row in found])
    if day == 14:
        matched = reference.format_search_results("therapy referral", DISPLAY_RECORDS)
        missing = reference.format_search_results("moonstone", DISPLAY_RECORDS)
        inputs = '[{"title":"Physical therapy","text":"Referral required","source":"synthetic/therapy"}, {"title":"Appeals","text":"Review denied claims","source":"synthetic/appeals"}]'
        return f'questions="therapy referral" and "moonstone"; records={inputs}', matched + "\\n" + missing
    if day == 15:
        value = reference.search_response("  THERAPY referral?  ", DISPLAY_RECORDS)
        inputs = '[{"title":"Physical therapy","text":"Referral required","source":"synthetic/therapy"}, {"title":"Appeals","text":"Review denied claims","source":"synthetic/appeals"}]'
        return f'question="  THERAPY referral?  "; records={inputs}', _render(value)
    raise ValueError("day must be from 1 through 15")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--day", type=int, required=True, choices=range(1, 16))
    args = parser.parse_args()
    input_line, output = _example(args.day)
    print(f"Day {args.day:02} instructor example (reference only; not learner evidence)")
    print(f"Input: {input_line}")
    print("Output:")
    print(output.replace("\\n", "\n"))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
