"""Run one day's learner check. A failing TODO is an expected learning state."""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from ai.fde_v2.beginner_first15 import learner
from ai.fde_v2.coverstory import learner as coverstory_learner

RECORDS = [
    {"document_id": "appeals", "title": "Appeals", "text": "Ask for a review of a denied claim.", "source": "synthetic/appeals"},
    {"document_id": "deductible", "title": "Deductible", "text": "The individual deductible is 500 dollars.", "source": "synthetic/deductible"},
    {"document_id": "therapy", "title": "Physical therapy", "text": "A referral is needed for therapy visits.", "source": "synthetic/therapy"},
]


def check(day: int) -> str:
    if day == 1:
        assert learner.document_title(RECORDS[0]) == "Appeals"
        assert learner.document_title({}) == ""
    elif day == 2:
        assert learner.normalize_question("  Deductible?  ") == "deductible?"
    elif day == 3:
        assert learner.question_words("The deductible is 500") == ["the", "deductible", "is", "500"]
    elif day == 4:
        assert learner.record_words(RECORDS[1]) == ["deductible", "the", "individual", "deductible", "is", "500", "dollars"]
    elif day == 5:
        assert learner.is_blank_question("  \t") is True
        assert learner.is_blank_question("appeal") is False
    elif day == 6:
        assert [row["document_id"] for row in learner.matching_records("claim denied", RECORDS)] == ["appeals"]
    elif day == 7:
        assert learner.unique_question_words("appeal appeal review") == {"appeal", "review"}
    elif day == 8:
        assert learner.overlap_score("deductible deductible individual", RECORDS[1]) == 2
    elif day == 9:
        rows = [dict(RECORDS[1], document_id="z"), dict(RECORDS[1], document_id="a")]
        assert [row["document_id"] for row in learner.rank_records("deductible", rows, 2)] == ["a", "z"]
    elif day == 10:
        from ai.fde_v2.coverstory.day01_baseline import CORPUS
        manifest = json.loads((CORPUS / "manifest.json").read_text(encoding="utf-8"))
        corpus = [json.loads((CORPUS / filename).read_text(encoding="utf-8"))
                  for filename in manifest["documents"]]
        assert learner.best_record("intergalactic moonstone", corpus) is None
        assert learner.best_record("individual deductible", corpus)["document_id"] == "northstar-deductible"
    elif day == 11:
        assert learner.source_label(RECORDS[0]) == "Appeals — synthetic/appeals"
    elif day == 12:
        raw = {"document_id": "sample", "version": 1, "title": "Sample", "source": "fixture", "text": "Hello"}
        document = coverstory_learner.parse_document_record(raw)
        assert document.document_id == "sample" and document.content_sha256
        for key, bad in (("version", True), ("text", "  ")):
            with_bad = dict(raw, **{key: bad})
            try:
                coverstory_learner.parse_document_record(with_bad)
            except (ValueError, TypeError):
                pass
            else:
                raise AssertionError(f"invalid {key} should be rejected")
    elif day == 13:
        from ai.fde_v2.coverstory.day01_baseline import CORPUS
        manifest = json.loads((CORPUS / "manifest.json").read_text(encoding="utf-8"))
        corpus = [json.loads((CORPUS / filename).read_text(encoding="utf-8"))
                  for filename in manifest["documents"]]
        ranked = learner.search_records("individual deductible", corpus)
        assert ranked and ranked[0]["document_id"] == "northstar-deductible"
        assert learner.search_records("", corpus) == []
    elif day == 14:
        assert learner.format_search_results("appeal denied", RECORDS) == "Appeals — synthetic/appeals"
        assert learner.format_search_results("unmatched phrase", RECORDS) == "No matching documents."
    elif day == 15:
        from ai.fde_v2.coverstory.day01_baseline import CORPUS
        manifest = json.loads((CORPUS / "manifest.json").read_text(encoding="utf-8"))
        corpus = [json.loads((CORPUS / filename).read_text(encoding="utf-8"))
                  for filename in manifest["documents"]]
        expected_record = next(row for row in corpus if row["document_id"] == "northstar-deductible")
        expected = f"{expected_record['title']} — {expected_record['source']}"
        assert learner.search_response("  INDIVIDUAL deductible? ", corpus).splitlines()[0] == expected
        assert learner.search_response("   ", corpus) == "Please enter a question."
        assert learner.search_response("unmatched moonstone", corpus) == "No matching documents."
    else:
        raise ValueError("day must be 1 through 15")
    return f"Day {day:02}: PASS — learner behavior matches the expected result"


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--day", type=int, required=True, choices=range(1, 16))
    args = parser.parse_args()
    try:
        print(check(args.day))
    except (AssertionError, NotImplementedError, ValueError, TypeError) as exc:
        print(f"Day {args.day:02}: NOT YET — {exc}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
