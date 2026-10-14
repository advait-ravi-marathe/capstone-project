from __future__ import annotations

import json
import hashlib
import unittest
from pathlib import Path

FIXTURE = Path(__file__).resolve().parents[2] / "fixtures/coverstory/CAP-56/synthetic-v1.json"


class CAP56InfrastructureTest(unittest.TestCase):
    def test_evaluation_fixture_has_query_ids_authority_and_pinned_hashes(self) -> None:
        data = json.loads(FIXTURE.read_text(encoding="utf-8"))
        self.assertEqual("CAP-56-synthetic-v1", data["fixture_id"])
        self.assertEqual(["q1", "q2", "q3", "q4"], [row["query"] for row in data["cases"]])
        self.assertEqual(4, len(data["cases_from_source"]))
        corpus = [{key: row[key] for key in ("id", "query", "relevant", "authorized", "results")}
                  for row in data["cases"]]
        config = {"k": data["k"], "metric": "macro-hit-at-k",
                  "unanswerable": "exclude-from-macro", "authorization": "fail-whole-run"}
        for field, payload in (("corpus_sha256", corpus), ("config_sha256", config)):
            canonical = json.dumps(payload, sort_keys=True, separators=(",", ":"), ensure_ascii=False).encode()
            self.assertEqual(hashlib.sha256(canonical).hexdigest(), data[field])


if __name__ == "__main__": unittest.main()
